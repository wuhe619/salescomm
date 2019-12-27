package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.be.service.BusiEntityService;
import com.bdaim.bill.dto.TransactionTypeEnum;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.online.zhianxin.dto.BaseResult;
import com.bdaim.online.zhianxin.service.ZAXSearchListService;
import com.bdaim.customersea.dto.CustomSeaTouchInfoDTO;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
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
            sqlstr.append(" and ext_3 = ").append(type);
        }
        if (!"all".equals(cust_id))
            sqlstr.append(" and cust_id='").append(cust_id).append("'");
        if (StringUtil.isNotEmpty(name)) {
            sqlstr.append(" and content->'$.name'='").append(name).append("'");
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

        Map<String, Object> superData = new HashMap(16);
        superData.put("SYS007", "未跟进");
        CustomSeaTouchInfoDTO dto = null;
        Map<String, JSONObject> data = new HashMap(16);
        // 指定企业ID领取方式
        if (mode == 1) {
            data = doClueDataToSeaByIds(companyIds, custId);
            // 指定数量
        } else if (mode == 2) {
            //领取，只返回id
            data = doClueDataToSeaByNumber(param, getNumber, custId, userId, busiType);
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
            for (int i = 0; i < pNumbers.size(); i++) {
                if (StringUtil.isEmpty(pNumbers.getString(i))) {
                    LOG.info("B2B企业ID:{}手机号为空:{}", entId, pNumbers.getString(i));
                    continue;
                }
                dto = new CustomSeaTouchInfoDTO("", custId, String.valueOf(userId), "", "",
                        "", "", "", pNumbers.getString(i),
                        "", "", "",
                        seaId, superData, "", "", "", "",
                        "", "", data.get(entId).getString("entName"),
                        entId, data.get(entId).getString("regLocation"), data.get(entId).getString("regCap"),
                        data.get(entId).getString("entStatus"), data.get(entId).getString("estiblishTime"), pNumbers.size());
                // 保存线索
                int status = seaService.addClueData0(dto, seaType);
                LOG.info("B2B套餐领取线索状态:{},seaType:{},data:{}", status, seaType, JSON.toJSONString(dto));
                // 保存领取记录
                saveTcbClueDataLog(custId, userId, batchId, entId, useB2BTcb.getString("id"), dto.getSuper_id(), JSON.toJSONString(dto));
            }

            }
            consumeNum++;

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
                " where id = ? ";
        jdbcTemplate.update(updateNumSql, consumerNum, consumerNum, id);
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
        for (String id : companyIds) {
            // 已经领取过不可重复领取
            if (b2BTcbLogService.checkClueGetStatus(custId, id)) {
                LOG.info("客户:{},B2B企业ID:{}已经领取过", custId, id);
                continue;
            }
            // 查询企业联系方式
            try {
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
            } catch (Exception e) {
                LOG.warn("客户指定Id领取B2B套餐异常", e);
            }
        }
        return data;
    }


}
