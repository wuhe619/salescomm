package com.bdaim.customer.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.be.service.BusiEntityService;
import com.bdaim.bill.dto.TransactionTypeEnum;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.crm.dao.LkCrmOwnerRecordDao;
import com.bdaim.crm.ent.service.EntDataService;
import com.bdaim.crm.entity.LkCrmAdminFieldvEntity;
import com.bdaim.crm.entity.LkCrmOwnerRecordEntity;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.crm.service.CrmLeadsService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customersea.dto.CustomSeaTouchInfoDTO;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.online.zhianxin.dto.BaseResult;
import com.bdaim.online.zhianxin.service.ZAXSearchListService;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/***
 * B2B企业套餐包管理
 *
 */
@Service("busi_b2b_tcb")
@Transactional
public class B2BTcbService implements BusiService {
    private static Logger LOG = LoggerFactory.getLogger(B2BTcbService.class);

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    ElasticSearchService elasticSearchService;
    @Autowired
    private ZAXSearchListService searchListService;
    @Autowired
    private CustomerSeaService seaService;
    @Autowired
    private BusiEntityService busiEntityService;
    @Autowired
    private B2BTcbLogService b2BTcbLogService;
    @Autowired
    private MarketResourceService marketResourceService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private MarketResourceDao marketResourceDao;
    @Autowired
    private CrmLeadsService crmLeadsService;
    @Autowired
    private AdminFieldService adminFieldService;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private LkCrmOwnerRecordDao crmOwnerRecordDao;
    @Autowired
    private EntDataService entDataService;

    /**
     * 企业开通B2B套餐
     *
     * @param busiType
     * @param cust_id
     * @param cust_group_id
     * @param cust_user_id
     * @param id
     * @param info
     * @throws Exception
     */
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        String sql = "select id,content from " + HMetaDataDef.getTable(busiType, "") + " where type=? and cust_id = ? and ext_4 = 1 ";
        List<Map<String, Object>> countList = jdbcTemplate.queryForList(sql, busiType, cust_id);
        if (countList != null && countList.size() > 0) {
            JSONObject jsonObject = JSON.parseObject(String.valueOf(countList.get(0).get("content")));
            if (jsonObject.getLongValue("remain_num") > 0L) {
                LOG.warn("当前套餐有效:{}不能再开通新的套餐", JSON.toJSONString(countList));
                throw new TouchException("当前套餐有效,不能再开通新的套餐");
            }
        }
        // 企业余额判断
        boolean b = marketResourceService.judRemainAmount0(cust_id);
        if (!b) {
            LOG.warn("客户:{}余额不足", cust_id);
            throw new TouchException("客户余额不足");
        }
        // 根据套餐包配置
        ResourcePropertyEntity m = marketResourceDao.getProperty(info.getString("resource_id"), "price_config");
        if (m != null && StringUtil.isNotEmpty(m.getPropertyValue())) {
            JSONObject tcbConfig = JSON.parseObject(m.getPropertyValue());
            // 企业余额和套餐价格判断
            Double remainMoney = customerService.getRemainMoney(cust_id);
            if (remainMoney < NumberConvertUtil.changeY2L(tcbConfig.getDoubleValue("price"))) {
                LOG.warn("客户:{}开通套餐失败,套餐(资源)ID:{},余额:{}", cust_id, info.getString("resource_id"), remainMoney);
                throw new TouchException("客户余额不足");
            }

            // 查询供应商售价配置配置
            m = marketResourceDao.getProperty(tcbConfig.getString("price_res_id"), "price_config");
            if (m == null) {
                LOG.warn("查询套餐包配置异常,套餐包ID:{}", tcbConfig.getString("price_res_id"));
                throw new TouchException("查询套餐包配置异常");
            }
            info.put("ext_2", tcbConfig.getString("name"));
            info.put("ext_3", tcbConfig.getString("type"));
            info.put("ext_4", 1);

            LocalDateTime eTime = LocalDateTime.parse(info.getString("s_time"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")).plusMonths(info.getLongValue("effective_month"));
            info.put("e_time", eTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
            // 基础 定制套餐 供应商 客户扣费
            if ("1".equals(tcbConfig.getString("type")) || "2".equals(tcbConfig.getString("type"))) {
                String name = "";
                if (tcbConfig.getIntValue("type") == 1) {
                    name = "(标准套餐)";
                } else if (tcbConfig.getIntValue("type") == 2) {
                    name = "(定制套餐)";
                }
                tcOpenDeduction(tcbConfig.getString("price_res_id"), tcbConfig.getString("name") + name, tcbConfig.getString("price"), tcbConfig.getIntValue("total"), cust_id, cust_user_id);
            }
        } else {
            LOG.warn("套餐包:{}无效", info.getString("resource_id"));
            throw new TouchException("套餐包无效");
        }
    }

    /**
     * 套餐包企业和供应商扣费
     *
     * @param resourceId
     * @param resName
     * @param price
     * @param total
     * @param custId
     * @param userId
     * @return
     * @throws Exception
     */
    private int tcOpenDeduction(String resourceId, String resName, String price, int total, String custId, long userId) throws Exception {
        MarketResourceEntity resourceEntity = marketResourceDao.get(NumberConvertUtil.parseInt(resourceId));
        if (resourceEntity == null || resourceEntity.getStatus() != 1) {
            LOG.warn("供应商资源:{}无效", resourceEntity);
            throw new TouchException("所选供应商资源无效");
        }
        // 套餐客户售价(厘)
        int custNumberPrice = NumberConvertUtil.changeY2L(price);
        // 套餐供应商售价(厘)
        int supplierNumberPrice = 0;
        // 查询供应商B2B套餐扣费类型
        ResourcePropertyEntity m = marketResourceDao.getProperty(resourceId, "price_config");
        JSONObject config = new JSONObject();
        if (m != null && StringUtil.isNotEmpty(m.getPropertyValue())) {
            config = JSON.parseObject(m.getPropertyValue());
            if (config.getIntValue("type") == 1) {
                // 按条扣费
                supplierNumberPrice = NumberConvertUtil.changeY2L(config.getString("price")) * total;
            } else if (config.getIntValue("type") == 2) {
                // 收入分成(套餐价格*收入百分比)
                BigDecimal bigDecimal = new BigDecimal(config.getString("price")).divide(new BigDecimal("100"));
                supplierNumberPrice = NumberConvertUtil.changeY2L(NumberConvertUtil.parseDouble(price) * bigDecimal.doubleValue());
            }
        }
        LOG.info("客户:{}套餐包开通开始扣费,客户金额:{},供应商金额:{},套餐配置信息:{}", custId, custNumberPrice, supplierNumberPrice, config);
        int status = transactionService.customerSupplierDeduction(custId, TransactionTypeEnum.B2B_TC_DEDUCTION.getType(),
                custNumberPrice, supplierNumberPrice, resourceId, resName, String.valueOf(userId));
        return status;
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws TouchException {
        info.put("ext_2", info.getString("name"));
        info.put("ext_3", info.getString("type"));
        info.put("ext_4", info.getString("status"));
    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5  from "
                + HMetaDataDef.getTable(busiType, "") + " where type='").append(busiType).append("'");
        String name = params.getString("name");
        Integer type = params.getInteger("type");
        if (type != null) {
            sqlParams.add(type);
            sqlstr.append(" and ext_3 = ? ");
        }
        if (!"all".equals(cust_id)) {
            sqlParams.add(cust_id);
            sqlstr.append(" and cust_id=? ");
        }
        if (StringUtil.isNotEmpty(name)) {
            sqlParams.add(name);
            sqlstr.append(" and content->'$.name'=? ");
        }
        sqlstr.append(" ORDER BY create_date DESC ");
        return sqlstr.toString();
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub
    }

    /**
     * 获取企业B2B套餐包剩余量(只有1个套餐包有效)
     *
     * @param custId
     * @return
     */
    public long getB2BTcbQuantity(String custId) {
        String sql = "select id,content from " + HMetaDataDef.getTable(BusiTypeEnum.B2B_TC.getType(), "") + " where type=? and cust_id = ? and ext_4 = 1 ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, BusiTypeEnum.B2B_TC.getType(), custId);
        if (list == null || list.size() == 0) {
            return 0L;
        }
        return JSON.parseObject(String.valueOf(list.get(0).get("content"))).getLongValue("remain_num");
    }

    /**
     * 查询企业检索的数据来源
     *
     * @param custId
     * @return
     */
    public Map getB2BSearchConfig(String custId) {
        Map data = new HashMap();
        data.put("type", 1);
        String sql = "select id,content from " + HMetaDataDef.getTable(BusiTypeEnum.B2B_TC.getType(), "") + " where type=? and cust_id = ? and ext_4 = 1 ";
        List<Map<String, Object>> list = customerDao.sqlQuery(sql, BusiTypeEnum.B2B_TC.getType(), custId);
        if (list != null && list.size() > 0) {
            Map<String, Object> result = list.get(0);
            JSONObject content = JSON.parseObject(String.valueOf(result.get("content")));
            int resourceId = content.getIntValue("resource_id");
            MarketResourceEntity marketResourceEntity = marketResourceDao.get(resourceId);
            String[] suppliers = new String[]{"86", "51", "39"};
            if (marketResourceEntity != null && Arrays.asList(suppliers).contains(marketResourceEntity.getSupplierId())) {
                data.put("type", 2);
            }
        }
        return data;
    }

    /**
     * 获取企业再用的套餐包
     *
     * @param custId
     * @return
     */
    public JSONObject getUseB2BTcb(String custId) {
        String sql = "select id,content from " + HMetaDataDef.getTable(BusiTypeEnum.B2B_TC.getType(), "") + " where type=? and cust_id = ? and ext_4 = 1 ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, BusiTypeEnum.B2B_TC.getType(), custId);
        if (list == null || list.size() == 0) {
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(list.get(0)));
        jsonObject.putAll(JSON.parseObject(String.valueOf(list.get(0).get("content"))));
        return jsonObject;
    }

    /**
     * @param custId
     * @param userId
     * @param seaType    1-公海 2-私海
     * @param mode       1-领取所选 2-指定数量
     * @param seaId      公海或私海ID
     * @param companyIds 企业Id集合
     * @param getNumber  领取数量
     * @param busiType
     * @param param
     * @return
     * @throws Exception
     */
    public int doClueDataToSea(String custId, long userId, int seaType, int mode, String seaId, List<String> companyIds, long getNumber, String busiType, JSONObject param) throws Exception {
        if (getNumber > 500) {
            throw new TouchException("领取上限为500");
        }
        // 判断套餐余量
        long quantity = getB2BTcbQuantity(custId);
        if (quantity == 0) {
            throw new TouchException("套餐余量为0");
        }
        if ((companyIds != null && companyIds.size() > quantity) || getNumber > quantity) {
            throw new TouchException("套餐余量不足");
        }
        // 查询企业在使用的套餐包
        JSONObject useB2BTcb = getUseB2BTcb(custId);
        if (useB2BTcb == null) {
            throw new TouchException("企业无可用套餐包");
        }
        LocalDateTime sTime = LocalDateTime.parse(useB2BTcb.getString("s_time"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        if (sTime.isAfter(LocalDateTime.now())) {
            throw new TouchException("企业套餐未到开始时间");
        }

        LocalDateTime eTime = LocalDateTime.parse(useB2BTcb.getString("s_time"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")).plusMonths(useB2BTcb.getLongValue("effective_month"));
        if (eTime.isBefore(LocalDateTime.now())) {
            throw new TouchException("企业套餐已过期");
        }
        // 查询使用的套餐包类型
        Map config = getB2BSearchConfig(custId);
        int sourceType = (int) config.get("type");

        Map<String, Object> superData = new HashMap(16);
        superData.put("SYS007", "未跟进");
        CustomSeaTouchInfoDTO dto = null;
        Map<String, JSONObject> data = new HashMap(16);
        // 指定企业ID领取方式
        if (mode == 1) {
            if (seaType == 1) {
                addPublicSeaStats(companyIds, null, userId);
            }
            // 已经领取过不可重复领取
            if (companyIds != null && companyIds.size() > 0 &&
                    b2BTcbLogService.checkClueGetStatus(custId, companyIds.get(0))) {
                LOG.warn("该线索已经领取过,entId:{}", companyIds.get(0));
                //throw new TouchException("该线索已经领取过");
            }
            LOG.info("kais doClueDataToSeaByIds");
            if (sourceType == 1) {
                data = doClueDataToSeaByIdsHK(companyIds, custId);
            } else {
                data = doClueDataToSeaByIds(companyIds, custId);
            }
            // 指定数量
        } else if (mode == 2) {
            if (seaType == 1) {
                addPublicSeaStats(null, getNumber, userId);
            }
            LoginUser user = BaseUtil.getUser();
            //领取，只返回id
            if (sourceType == 1) {
                //data = doClueDataToSeaByNumberHK(param, getNumber, custId, userId, busiType);
                return entDataService.addCrmClueQueue(user.getCustId(), user.getUserId(), getNumber, param, 1);
            } else {
                entDataService.addCrmClueQueue(user.getCustId(), user.getUserId(), getNumber, param, 0);
                data = doClueDataToSeaByNumber(param, getNumber, custId, userId, busiType);
            }
        }
        if (data.size() == 0) {
            throw new TouchException("未查询到匹配企业数据");
        }
        String batchId = UUID.randomUUID().toString().replaceAll("-", "");
        Iterator keys = data.keySet().iterator();
        int consumeNum = 0;
        while (keys.hasNext()) {
            String entId = String.valueOf(keys.next());
            JSONArray pNumbers = data.get(entId).getJSONArray("phoneNumber");
            if (pNumbers == null || pNumbers.size() == 0) {
                continue;
            }
            String email = "";
            if (StringUtil.isNotEmpty(data.get(entId).getString("email"))
                    && !"-".equals(data.get(entId).getString("email"))) {
                email = data.get(entId).getString("email");
            }
            for (int i = 0; i < pNumbers.size(); i++) {
                if (StringUtil.isEmpty(pNumbers.getString(i))) {
                    LOG.info("B2B企业ID:{}手机号为空:{}", entId, pNumbers.getString(i));
                    continue;
                }
                dto = new CustomSeaTouchInfoDTO("", custId, String.valueOf(userId), "", "",
                        data.get(entId).getString("entName") + (i + 1), "", "", pNumbers.getString(i),
                        "", "", "",
                        seaId, superData, "", email, "", "",
                        "", "", data.get(entId).getString("entName"),
                        entId, data.get(entId).getString("regLocation"), data.get(entId).getString("regCap"),
                        data.get(entId).getString("entStatus"), data.get(entId).getString("estiblishTime"), pNumbers.size());
                // 保存线索
                int status = seaService.addClueData0(dto, seaType, param.getString("source"));
                LOG.info("B2B套餐领取线索状态:{},seaType:{},data:{}", status, seaType, JSON.toJSONString(dto));
                // 保存领取记录
                saveTcbClueDataLog(custId, userId, batchId, entId, useB2BTcb.getString("id"), dto.getSuper_id(), JSON.toJSONString(dto));
                // 判断是否为crm的线索领取
                if ("crm".equals(param.getString("source")) && status != -1) {
                    // 保存公海标记信息  seaType 1公海 2私海
                    JSONArray list = new JSONArray();
                    String[] values = new String[]{"手机", "电话", "线索名称", "公司名称", "线索来源", "邮箱"};
                    String telephone = "", mobile = "";
                    if (pNumbers.getString(i).replaceAll(" ", "").trim().length() == 11
                            && pNumbers.getString(i).lastIndexOf("-") <= 0
                            && !pNumbers.getString(i).startsWith("0")
                            && NumberUtil.isLong(pNumbers.getString(i))) {
                        mobile = pNumbers.getString(i);
                    } else {
                        telephone = pNumbers.getString(i);
                    }
                    for (String v : values) {
                        String label = seaType == 1 ? "11" : "1";
                        Map<String, Object> field = marketResourceDao.queryUniqueSql("SELECT * FROM lkcrm_admin_field WHERE name = ? AND cust_id = ? AND label =" + label, v, custId);
                        if (field != null) {
                            LkCrmAdminFieldvEntity value = new LkCrmAdminFieldvEntity();
                            value.setFieldId(NumberConvertUtil.parseInt(field.get("field_id")));
                            value.setCustId(custId);
                            value.setName(String.valueOf(field.get("name")));
                            if ("手机".equals(v)) {
                                value.setValue(mobile);
                            } else if ("线索名称".equals(v)) {
                                value.setValue(dto.getSuper_name());
                            } else if ("公司名称".equals(v)) {
                                value.setValue(dto.getCompany());
                            } else if ("线索来源".equals(v)) {
                                value.setValue("发现线索");
                            } else if ("电话".equals(v)) {
                                value.setValue(telephone);
                            } else if ("邮箱".equals(v)) {
                                value.setValue(email);
                            }
                            list.add(value);
                        }
                    }
                    email = "";
                    String id = dto.getSuper_id();
                    //领取到私海
                    if (seaType == 2) {
                        id = crmLeadsService.transferToPrivateSea(seaId, data.get(entId).getString("entName"), mobile, telephone, i + 1);
                    }
                    adminFieldService.save(list, id);

                }
            }
            consumeNum++;
        }
        // 更新套餐余量和消耗量
        updateTbRemain(useB2BTcb.getLong("id"), consumeNum, BusiTypeEnum.B2B_TC.getType());
        return 0;
    }

    private void addPublicSeaStats(List<String> companyIds, Long getNumber, long userId) {
        List<LkCrmOwnerRecordEntity> list = new ArrayList<>();
        int sum = !CollectionUtils.isEmpty(companyIds) ? companyIds.size() : getNumber.intValue();
        for (int i = 0; i < sum; i++) {
            //添加线索公海统计记录
            LkCrmOwnerRecordEntity crmOwnerRecord = new LkCrmOwnerRecordEntity();
            crmOwnerRecord.setTypeId(0);
            crmOwnerRecord.setType(9);
            crmOwnerRecord.setPreOwnerUserId(userId);
            crmOwnerRecord.setCreateTime(DateUtil.date().toTimestamp());
            list.add(crmOwnerRecord);
        }
        if (!CollectionUtils.isEmpty(list)) {
            crmOwnerRecordDao.batchSaveOrUpdate(list);
        }
    }


    public int doClueDataToSeaHK(String custId, long userId, int seaType, int mode, String seaId, List<String> companyIds, long getNumber, String busiType, JSONObject param) throws Exception {
        if (getNumber > 500) {
            throw new TouchException("领取上限为500");
        }
        // 判断套餐余量
        long quantity = getB2BTcbQuantity(custId);
        if (quantity == 0) {
            throw new TouchException("套餐余量为0");
        }
        if ((companyIds != null && companyIds.size() > quantity) || getNumber > quantity) {
            throw new TouchException("套餐余量不足");
        }
        // 查询企业在使用的套餐包
        JSONObject useB2BTcb = getUseB2BTcb(custId);
        if (useB2BTcb == null) {
            throw new TouchException("企业无可用套餐包");
        }
        LocalDateTime sTime = LocalDateTime.parse(useB2BTcb.getString("s_time"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        if (sTime.isAfter(LocalDateTime.now())) {
            throw new TouchException("企业套餐未到开始时间");
        }

        LocalDateTime eTime = LocalDateTime.parse(useB2BTcb.getString("s_time"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")).plusMonths(useB2BTcb.getLongValue("effective_month"));
        if (eTime.isBefore(LocalDateTime.now())) {
            throw new TouchException("企业套餐已过期");
        }

        Map<String, Object> superData = new HashMap(16);
        superData.put("SYS007", "未跟进");
        CustomSeaTouchInfoDTO dto = null;
        Map<String, JSONObject> data = new HashMap(16);
        // 指定企业ID领取方式
        if (mode == 1) {
            // 已经领取过不可重复领取
            if (companyIds != null && companyIds.size() > 0 &&
                    b2BTcbLogService.checkClueGetStatus(custId, companyIds.get(0))) {
                throw new TouchException("该线索已经领取过");
            }
            data = doClueDataToSeaByIdsHK(companyIds, custId);
            // 指定数量
        } else if (mode == 2) {
            //领取，只返回id
            data = doClueDataToSeaByNumberHK(param, getNumber, custId, userId, busiType);
        }
        if (data.size() == 0) {
            throw new TouchException("领取异常,请检查检索条件");
        }
        String batchId = UUID.randomUUID().toString().replaceAll("-", "");
        Iterator keys = data.keySet().iterator();
        int consumeNum = 0;
        while (keys.hasNext()) {
            String entId = String.valueOf(keys.next());
            JSONArray pNumbers = data.get(entId).getJSONArray("phoneNumber");
            if (pNumbers == null || pNumbers.size() == 0) {
                continue;
            }
            for (int i = 0; i < pNumbers.size(); i++) {
                if (StringUtil.isEmpty(pNumbers.getString(i))) {
                    LOG.info("B2B企业ID:{}手机号为空:{}", entId, pNumbers.getString(i));
                    continue;
                }
                dto = new CustomSeaTouchInfoDTO("", custId, String.valueOf(userId), "", "",
                        data.get(entId).getString("entName") + (i + 1), "", "", pNumbers.getString(i),
                        "", "", "",
                        seaId, superData, "", "", "", "",
                        "", "", data.get(entId).getString("entName"),
                        entId, data.get(entId).getString("regLocation"), data.get(entId).getString("regCap"),
                        data.get(entId).getString("entStatus"), data.get(entId).getString("estiblishTime"), pNumbers.size());
                // 保存线索
                int status = seaService.addClueData0(dto, seaType, param.getString("source"));
                LOG.info("B2B套餐领取线索状态:{},seaType:{},data:{}", status, seaType, JSON.toJSONString(dto));
                // 保存领取记录
                saveTcbClueDataLog(custId, userId, batchId, entId, useB2BTcb.getString("id"), dto.getSuper_id(), JSON.toJSONString(dto));
                // 判断是否为crm的线索领取
                if ("crm".equals(param.getString("source")) && status != -1) {
                    // 保存公海标记信息  seaType 1公海 2私海
                    JSONArray list = new JSONArray();
                    String[] values = new String[]{"手机", "电话", "线索名称", "公司名称", "线索来源"};
                    String telephone = "", mobile = "";
                    if (pNumbers.getString(i).replaceAll(" ", "").trim().length() == 11
                            && pNumbers.getString(i).lastIndexOf("-") <= 0
                            && !pNumbers.getString(i).startsWith("0")
                            && NumberUtil.isLong(pNumbers.getString(i))) {
                        mobile = pNumbers.getString(i);
                    } else {
                        telephone = pNumbers.getString(i);
                    }
                    for (String v : values) {
                        String label = seaType == 1 ? "11" : "1";
                        Map<String, Object> field = marketResourceDao.queryUniqueSql("SELECT * FROM lkcrm_admin_field WHERE name = ? AND cust_id = ? AND label =" + label, v, custId);
                        if (field != null) {
                            LkCrmAdminFieldvEntity value = new LkCrmAdminFieldvEntity();
                            value.setFieldId(NumberConvertUtil.parseInt(field.get("field_id")));
                            value.setCustId(custId);
                            value.setName(String.valueOf(field.get("name")));
                            if ("手机".equals(v)) {
                                value.setValue(mobile);
                            } else if ("线索名称".equals(v)) {
                                value.setValue(dto.getSuper_name());
                            } else if ("公司名称".equals(v)) {
                                value.setValue(dto.getCompany());
                            } else if ("线索来源".equals(v)) {
                                value.setValue("发现线索");
                            } else if ("电话".equals(v)) {
                                value.setValue(telephone);
                            }
                            list.add(value);
                        }
                    }
                    String id = dto.getSuper_id();
                    //领取到私海
                    if (seaType == 2) {
                        id = crmLeadsService.transferToPrivateSea(seaId, data.get(entId).getString("entName"), mobile, telephone, i + 1);
                    }
                    adminFieldService.save(list, id);

                }
            }
            consumeNum++;
        }
        // 更新套餐余量和消耗量
        updateTbRemain(useB2BTcb.getLong("id"), consumeNum, BusiTypeEnum.B2B_TC.getType());
        return 0;
    }

    /**
     * 更新套餐余量和消耗量
     *
     * @param id
     * @param consumerNum
     * @param busiType
     */
    private void updateTbRemain(long id, int consumerNum, String busiType) {
        String updateNumSql = "UPDATE " + HMetaDataDef.getTable(busiType, "")
                + " set content = JSON_SET(content, '$.consume_num', JSON_EXTRACT(content, '$.consume_num') + ?), " +
                " content = JSON_SET ( content, '$.remain_num', JSON_EXTRACT(content, '$.remain_num') - ? )" +
                " where id = ? and type=?";
        jdbcTemplate.update(updateNumSql, consumerNum, consumerNum, id, busiType);
    }

    /**
     * 保存B2B套餐领取记录
     *
     * @param custId
     * @param userId
     * @param batchId
     * @param entId
     * @param tcbId
     * @param superId
     * @param content
     */
    private void saveTcbClueDataLog(String custId, long userId, String batchId, String entId, String tcbId, String superId, String content) {
        JSONObject log = new JSONObject();
        // B2B数据企业ID
        log.put("ext_1", entId);
        log.put("entId", entId);
        // 套餐包ID 扩展字段2
        log.put("tcbId", tcbId);
        // 领取批次ID 扩展字段3
        log.put("batchId", batchId);
        // 线索ID 扩展字段4
        log.put("superId", superId);
        log.put("content", content);
        try {
            busiEntityService.saveInfo(custId, "", userId, BusiTypeEnum.B2B_TC_LOG.getType(), 0L, log);
        } catch (Exception e) {
            LOG.warn("保存B2B领取记录失败", e);
        }
    }

    /**
     * 根据指定数量领取B2B线索
     *
     * @param param
     * @param getNumber
     * @param custId
     * @param userId
     * @param busiType
     */
    public Map<String, JSONObject> doClueDataToSeaByNumber(JSONObject param, long getNumber, String custId, long userId, String busiType) throws Exception {
        Map<String, JSONObject> data = new HashMap<>();
        BaseResult baseResult, companyContact;
        JSONObject resultData, contactData;
        JSONArray list;
       /* Random random = new Random();
        long pageNo = random.nextInt((int) getNumber), pageSize = getNumber * 5;*/
        // 预查询数据
        param.put("endTime", "1900-01-01");
        baseResult = searchListService.pageSearch(custId, "", userId, busiType, param);
        resultData = (JSONObject) baseResult.getData();
        if (resultData != null || "100".equals(baseResult.getCode())) {
            if (getNumber > resultData.getLongValue("total")) {
                LOG.warn("领取数据大于可用线索");
                throw new TouchException("领取数据大于可用线索");
            }
        }

        long pageNo = 1, pageSize = getNumber * 5;
        while (getNumber > data.size()) {
            param.put("pageNum", pageNo);
            param.put("pageSize", pageSize);
            try {
                baseResult = searchListService.pageSearch(custId, "", userId, busiType, param);
                resultData = (JSONObject) baseResult.getData();
                if (resultData == null || !"100".equals(baseResult.getCode())) {
                    LOG.warn("领取线索异常:{}", resultData);
                    throw new TouchException("领取线索异常");
                }
                list = resultData.getJSONArray("list");
                pageNo++;
                if (list == null || list.size() == 0) {
                    continue;
                }
                String id = null;
                for (int i = 0; i < list.size(); i++) {
                    /*if (list.getJSONObject(i).getIntValue("list") == 0) {
                        continue;
                    }*/
                    id = list.getJSONObject(i).getString("id");
                    // 已经领取过不可重复领取
                    if (b2BTcbLogService.checkClueGetStatus(custId, id)) {
                        LOG.info("客户:{},B2B企业ID:{}已经领取过", custId, id);
                        continue;
                    }
                    companyContact = searchListService.getCompanyDetail(id, "", "1039");
                    contactData = (JSONObject) companyContact.getData();
                    if (contactData == null || contactData.size() == 0 ||
                            contactData.getJSONArray("phoneNumber") == null ||
                            contactData.getJSONArray("phoneNumber").size() == 0) {
                        continue;
                    }
                    if (getNumber > data.size()) {
                        contactData.putAll(list.getJSONObject(i));
                        data.put(id, contactData);
                    } else {
                        break;
                    }
                   /* if (getNumber > data.size()) {
                        // 查询联系方式
                        companyContact = searchListService.getCompanyDetail(id, "", "1039");
                        contactData = (JSONObject) companyContact.getData();
                        if (contactData == null || contactData.size() == 0 ||
                                contactData.getJSONArray("phoneNumber") == null ||
                                contactData.getJSONArray("phoneNumber").size() == 0) {
                            continue;
                        }
                        // 查询企业名称
                        companyDetail = searchListService.getCompanyDetail(id, "", "1001");
                        detailData = (JSONObject) companyDetail.getData();
                        contactData.putAll(detailData);
                        data.put(id, contactData);
                    } else {
                        break;
                    }*/
                }
            } catch (Exception e) {
                LOG.warn("客户指定数量领取B2B套餐异常", e);
                throw new TouchException("指定数量领取B2B套餐异常");
            }
        }
        return data;
    }

    public Map<String, JSONObject> doClueDataToSeaByNumberHK(JSONObject param, long getNumber, String custId, long userId, String busiType) throws Exception {
        Map<String, JSONObject> data = new HashMap<>();
        Page baseResult;
        JSONObject companyContact;
        List<JSONObject> list;
       /* Random random = new Random();
        long pageNo = random.nextInt((int) getNumber), pageSize = getNumber * 5;*/
        // 预查询数据
        JSONArray pStatus = JSON.parseArray("[{\"value\":\"1\"},{\"value\":\"2\"}]");
        param.put("phoneStatus", pStatus);
        baseResult = entDataService.pageSearch(custId, "", userId, busiType, param);
        if (baseResult.getData() != null && baseResult.getData().size() > 0) {
            if (getNumber > baseResult.getTotal()) {
                LOG.warn("领取数量大于可用线索数量");
                throw new TouchException("领取数量大于可用线索数量");
            }
        }
        long pageNo = 1, pageSize = getNumber * 5;
        int i = 0;
        while (getNumber > data.size() && i <= 10) {
            i++;
            param.put("pageNum", pageNo);
            param.put("pageSize", pageSize);
            try {
                baseResult = entDataService.pageSearch(custId, "", userId, busiType, param);
                if (baseResult == null || baseResult.getData() == null) {
                    LOG.warn("领取线索异常:{}", baseResult);
                    throw new TouchException("领取线索异常");
                }
                list = baseResult.getData();
                pageNo++;
                if (list == null || list.size() == 0) {
                    continue;
                }
                String id = null;
                for (JSONObject jsonObject : list) {
                    /*if (list.getJSONObject(i).getIntValue("list") == 0) {
                        continue;
                    }*/
                    id = jsonObject.getString("id");
                    // 已经领取过不可重复领取
                    if (b2BTcbLogService.checkClueGetStatus(custId, id)) {
                        LOG.info("客户:{},B2B企业ID:{}已经领取过", custId, id);
                        continue;
                    }
                    Set phones = new HashSet();
                    if (jsonObject.containsKey("phone") && StringUtil.isNotEmpty(jsonObject.getString("phone"))) {
                        for (String p : jsonObject.getString("phone").split(",")) {
                            if (StringUtil.isEmpty(p) || "-".equals(p)) {
                                continue;
                            }
                            phones.add(p);
                        }
                    }
                    if (jsonObject.containsKey("phone1") && StringUtil.isNotEmpty(jsonObject.getString("phone1"))) {
                        for (String p : jsonObject.getString("phone1").split(",")) {
                            if (StringUtil.isEmpty(p) || "-".equals(p)) {
                                continue;
                            }
                            phones.add(p);
                        }
                    }
                    jsonObject.put("phoneNumber", phones);
                    if (getNumber > data.size()) {
                        data.put(id, jsonObject);
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                LOG.warn("客户指定数量领取B2B套餐异常", e);
                throw new TouchException("指定数量领取B2B套餐异常");
            }
        }
        return data;
    }

    /**
     * 根据指定企业ID领取B2B线索
     *
     * @param companyIds
     * @param custId
     * @return
     */
    public Map<String, JSONObject> doClueDataToSeaByIds(List<String> companyIds, String custId) {
        Map<String, JSONObject> data = new HashMap<>();
        BaseResult companyContact, companyDetail;
        JSONObject contactData, detailData;
        LOG.info("in doClueDataToSeaByIds mode=1");
        for (String id : companyIds) {
            // 已经领取过不可重复领取
            if (b2BTcbLogService.checkClueGetStatus(custId, id)) {
                LOG.info("客户:{},B2B企业ID:{}已经领取过", custId, id);
                continue;
            }
            LOG.info("Kaiser xunhuan ");
            // 查询企业联系方式
            try {
                companyContact = searchListService.getCompanyDetail(id, "", "1039");
                contactData = (JSONObject) companyContact.getData();
                if (contactData == null || contactData.size() == 0 ||
                        contactData.getJSONArray("phoneNumber") == null ||
                        contactData.getJSONArray("phoneNumber").size() == 0) {
                    continue;
                }
                LOG.info("iiii:" + companyContact);
                // 查询企业名称
                companyDetail = searchListService.getCompanyDetail(id, "", "1001");
                detailData = (JSONObject) companyDetail.getData();
                contactData.putAll(detailData);
                data.put(id, contactData);
            } catch (Exception e) {
                LOG.warn("客户指定Id领取B2B套餐异常", e);
            }
        }
        return data;
    }

    public Map<String, JSONObject> doClueDataToSeaByIdsHK(List<String> companyIds, String custId) {
        Map<String, JSONObject> data = new HashMap<>();
        JSONObject companyContact;
        LOG.info("in doClueDataToSeaByIds mode=1");
        for (String id : companyIds) {
            // 已经领取过不可重复领取
            /*if (b2BTcbLogService.checkClueGetStatus(custId, id)) {
                LOG.info("客户:{},B2B企业ID:{}已经领取过", custId, id);
                continue;
            }*/
            LOG.info("Kaiser xunhuan ");
            // 查询企业联系方式
            try {
                companyContact = entDataService.getCompanyDetail(id, new JSONObject(), "1039", 0);
                if (companyContact == null || companyContact.size() == 0) {
                    continue;
                }
                LOG.info("iiii:" + companyContact);
                // 查询企业名称
                companyContact.put("phoneNumber", companyContact.getJSONArray("phones"));
                data.put(id, companyContact);
            } catch (Exception e) {
                LOG.warn("客户指定Id领取B2B套餐异常", e);
            }
        }
        return data;
    }

    public int saveTcbData(String custId, Long userId, LocalDateTime startTime, JSONObject info) throws Exception {
        info.put("consume_num", 0);
        info.put("cust_id", custId);
        ResourcePropertyEntity m = marketResourceDao.getProperty(info.getString("resource_id"), "price_config");
        if (m != null && StringUtil.isNotEmpty(m.getPropertyValue())) {
            JSONObject tcbConfig = JSON.parseObject(m.getPropertyValue());
            info.put("name", tcbConfig.getString("name"));
            info.put("price", tcbConfig.getString("price"));
            info.put("effective_month", tcbConfig.getIntValue("expire"));
            info.put("remain_num", tcbConfig.getLong("total"));
            info.put("total", tcbConfig.getLong("total"));
            info.put("ext_2", tcbConfig.getString("name"));
            info.put("ext_3", tcbConfig.getString("type"));
            info.put("ext_4", 1);
            LocalDateTime eTime = startTime.plusMonths(info.getLongValue("effective_month"));
            info.put("s_time", startTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
            info.put("e_time", eTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
            String busiType = "b2b_tcb";
            long id = sequenceService.getSeq(busiType);
            String sql = "insert into " + HMetaDataDef.getTable(busiType, "") + "(id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date, ext_1, ext_2, ext_3, ext_4, ext_5 ) value(?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?)";
            LOG.info("套餐包[{}]开通custId:{},userId:{},参数:{}", id, custId, userId, info);
            return marketResourceDao.executeUpdateSQL(sql, id, busiType, info.toJSONString(), custId, null, userId,
                    userId, "", info.getString("ext_2"), info.getString("ext_3"), info.getString("ext_4"), "");
        }
        return 0;
    }

    /**
     * 查询当前客户可用的套餐
     *
     * @param custId
     * @return
     * @throws TouchException
     */
    public int countCustomerTcb(String custId) {
        int code = 0;
        String busiType = "b2b_tcb";
        String sql = "select id,content from " + HMetaDataDef.getTable(busiType, "") + " where type=? and cust_id = ? and ext_4 = 1 ";
        List<Map<String, Object>> countList = jdbcTemplate.queryForList(sql, busiType, custId);
        if (countList != null && countList.size() > 0) {
            JSONObject jsonObject = JSON.parseObject(String.valueOf(countList.get(0).get("content")));
            LocalDateTime eTime = LocalDateTime.parse(jsonObject.getString("e_time"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            if (eTime.isAfter(LocalDateTime.now()) && jsonObject.getLongValue("remain_num") > 0L) {
                LOG.warn("当前套餐:{}不能有效再开通新的套餐", countList.get(0).get("id"));
                code = 1;
            }
        }
        return code;
    }

}
