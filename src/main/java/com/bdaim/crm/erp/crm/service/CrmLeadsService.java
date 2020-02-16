package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.PhoneService;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.common.CrmParamValid;
import com.bdaim.crm.erp.crm.entity.CrmLeads;
import com.bdaim.crm.utils.*;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.service.CustomerLabelService;
import com.bdaim.customersea.dao.CustomerSeaDao;
import com.bdaim.customersea.dto.CustomSeaTouchInfoDTO;
import com.bdaim.customersea.dto.CustomerSeaESDTO;
import com.bdaim.customersea.dto.CustomerSeaSearch;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.customersea.entity.CustomerSeaProperty;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.util.*;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CrmLeadsService {

    public static final Logger LOG = LoggerFactory.getLogger(CrmLeadsService.class);
    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private FieldUtil fieldUtil;

    @Resource
    private CrmRecordService crmRecordService;

    @Resource
    private AdminFileService adminFileService;

    @Resource
    private CrmParamValid crmParamValid;

    @Resource
    private AuthUtil authUtil;

    @Resource
    private LkCrmAdminUserDao crmAdminUserDao;

    @Resource
    private LkCrmLeadsDao crmLeadsDao;

    @Resource
    private LkCrmCustomerDao crmCustomerDao;

    @Resource
    private LkCrmAdminRecordDao crmAdminRecordDao;

    @Resource
    private LkCrmOaEventDao crmOaEventDao;

    @Resource
    private LkCrmAdminFieldDao crmAdminFieldDao;

    @Resource
    private LkCrmAdminFieldvDao crmAdminFieldvDao;

    @Resource
    private CustomerSeaDao customerSeaDao;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomGroupDao customGroupDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private PhoneService phoneService;
    @Resource
    private CustomerLabelService customerLabelService;
    @Resource
    private CustomerSeaService customerSeaService;

    /**
     * 默认需要转为super_data的字段名称
     */
    public static Map<String, String> defaultLabels = new HashMap() {{
        put("qq", "SYS002");
        put("email", "SYS003");
        put("profession", "SYS004");
        put("weChat", "SYS001");
        put("company", "SYS005");
        put("followStatus", "SYS007");
        put("invalidReason", "SYS006");

        // 注册地址
        put("regLocation", "SYS009");
        // 注册资金
        put("regCapital", "SYS010");
        // 注册时间
        put("regTime", "SYS011");
        // 经营状态
        put("regStatus", "SYS012");
        // 企业联系人数量
        put("entPersonNum", "SYS013");
        // 企业ID
        put("entId", "SYS014");

        //CRM标签
        put("next_time", "next_time");
        put("remark", "remark");
        put("leads_name", "leads_name");
        put("dept", "dept");
        put("position", "position");
        put("site", "site");
        put("sea_use_count", "sea_use_count");
        put("未跟进天数", "未跟进天数");
        put("剩余回收时间", "剩余回收时间");
        put("退回公海原因", "退回公海原因");
        put("邮件次数", "邮件次数");
        put("营销总次数", "营销总次数");
        put("退回公海原因", "退回公海原因");

    }};

    private Map<String, String> excelDefaultLabels = new HashMap() {{
        put("qq", "SYS002");
        put("QQ", "SYS002");
        put("QQ号", "SYS002");
        put("email", "SYS003");
        put("EMAIL", "SYS003");
        put("weChat", "SYS001");
        put("微信", "SYS001");
        put("职业", "SYS004");
        put("公司", "SYS005");
        put("所在公司", "SYS005");
        put("跟进状态", "SYS007");
        put("无效原因", "SYS006");
        put("姓名", "super_name");
        put("年龄", "super_age");
        put("性别", "super_sex");
        put("手机", "super_telphone");
        put("手机号", "super_telphone");
        put("电话", "super_phone");
        put("电话号码", "super_phone");
        put("邮箱", "SYS003");
        put("省市", "super_address_province_city");
        put("地址", "super_address_street");
    }};

    /**
     * @return
     * @author wyq
     * 分页条件查询线索
     */
    public R pageCluePublicSea(BasePageRequest<CrmLeads> basePageRequest, long seaId, String custId) {
        //String leadsName = basePageRequest.getData().getLeadsName();
        CustomerSea customerSea = customerSeaDao.get(seaId);
        if (ObjectUtil.notEqual(custId, customerSea.getCustId())) {
            return R.error("线索公海不属于该客户");
        }
        String search = basePageRequest.getJsonObject().getString("search");
        if (!ParamsUtil.isValid(search)) {
            return R.error("参数包含非法字段");
        }
        com.bdaim.common.dto.Page page = crmLeadsDao.pageCluePublicSea(basePageRequest.getPage(), basePageRequest.getLimit(), seaId, search);
        if (page != null && page.getData() != null) {
            for (int i = 0; i < page.getData().size(); i++) {
                Map map = (Map) page.getData().get(i);
                // 解析super_data中qq 微信等属性
                getDefaultLabelValue(map);
                map.remove("super_data");
            }
        }
        Page finalPage = new Page();
        finalPage.setList(page.getData());
        finalPage.setTotalRow(page.getTotal());
        return R.ok().put("data", finalPage);
    }

    /**
     * 解析super_data中qq 微信等属性
     *
     * @param data
     */
    private void getDefaultLabelValue(Map<String, Object> data) {
        if (data != null && data.get("super_data") != null) {
            JSONObject jsonObject = JSON.parseObject(String.valueOf(data.get("super_data")));
            if (jsonObject == null || jsonObject.size() == 0) {
                return;
            }
            for (Map.Entry<String, Object> m : jsonObject.entrySet()) {
                for (Map.Entry<String, String> label : defaultLabels.entrySet()) {
                    if (Objects.equals(m.getKey(), label.getValue())) {
                        data.put(label.getKey(), m.getValue());
                        break;
                    }
                }
            }
        }
    }

    /**
     * 处理qq 微信 根据状态等自建属性值存入super_data
     *
     * @param dto
     */
    private void handleDefaultLabelValue(CustomSeaTouchInfoDTO dto, JSONObject param) {
        Map<String, Object> superData = new HashMap<>(16);
        if (dto.getSuperData() != null && dto.getSuperData().size() > 0) {
            for (Map.Entry<String, Object> m : dto.getSuperData().entrySet()) {
                superData.put(m.getKey(), m.getValue());
            }
        }
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(dto));
        if (jsonObject != null && jsonObject.size() > 0) {
            for (Map.Entry<String, Object> m : jsonObject.entrySet()) {
                if (defaultLabels.get(m.getKey()) != null && StringUtil.isNotEmpty(String.valueOf(m.getValue()))) {
                    // qq 微信等系统自建属性
                    superData.put(defaultLabels.get(m.getKey()), m.getValue());
                }
            }
        }
        if (param != null && param.size() > 0) {
            for (Map.Entry<String, Object> m : param.entrySet()) {
                if (defaultLabels.get(m.getKey()) != null && StringUtil.isNotEmpty(String.valueOf(m.getValue()))) {
                    // qq 微信等系统自建属性
                    superData.put(defaultLabels.get(m.getKey()), m.getValue());
                }
            }
        }

        dto.setSuperData(superData);
    }

    /**
     * 添加线索
     *
     * @param dto
     * @return
     */
    public int addClueData0(CustomSeaTouchInfoDTO dto, JSONObject jsonObject) {
        // 处理qq 微信等默认自建属性值
        handleDefaultLabelValue(dto, jsonObject);
        StringBuffer sql = new StringBuffer();
        int status = 0;
        try {
            //查询默认客群
            String cgId = customerSeaService.createDefaultClueCGroup0(NumberUtil.parseLong(dto.getCustomerSeaId()), "公海默认客群", dto.getCust_id());
            dto.setCust_group_id(cgId);

            String superId = MD5Util.encode32Bit("c" + dto.getSuper_telphone());
            dto.setSuper_id(superId);
            CustomerUser user = customerUserDao.get(NumberConvertUtil.parseLong(dto.getUser_id()));
            int dataStatus = 1;
            // 组长和员工数据状态为已分配
            if (2 == user.getUserType()) {
                dataStatus = 0;
            } else {
                // 超管和项目管理员数据状态为未分配
                dto.setUser_id(null);
            }
            LOG.info("开始保存添加线索个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + ",数据:" + dto.toString());
            try {
                customGroupDao.createCgDataTable(NumberConvertUtil.parseInt(dto.getCust_group_id()));
            } catch (HibernateException e) {
                LOG.error("创建用户群表失败,id:" + dto.getCust_group_id(), e);
            }
            List<Map<String, Object>> list = customerDao.sqlQuery("SELECT id FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + " WHERE id= ?", superId);
            if (list.size() > 0) {
                LOG.warn("客群ID:[" + dto.getCust_group_id() + "]添加线索ID:[" + superId + "]已经存在");
                return -1;
            }

            sql.append(" INSERT INTO " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`,update_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), new Timestamp(System.currentTimeMillis()));

            sql = new StringBuffer();
            sql.append(" INSERT INTO " + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`, batch_id, data_source,create_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), dto.getCust_group_id(), 3, new Timestamp(System.currentTimeMillis()));
            // 保存标记信息到es中
            CustomerSeaESDTO esData = new CustomerSeaESDTO(dto);
            esData.setSuper_data(JSON.toJSONString(dto.getSuperData()));
            //es暂时取消
            //saveClueInfoToES(esData);
            // 保存到redis中号码对应关系
            phoneService.setValueByIdFromRedis(superId, dto.getSuper_telphone());
            crmRecordService.updateRecord(jsonObject.getJSONArray("field"), superId);
            adminFieldService.save(jsonObject.getJSONArray("field"), superId);
            // 保存操作记录
            crmRecordService.addRecord(superId, CrmEnum.PUBLIC_SEA_TYPE_KEY.getTypes());

            status = 1;
        } catch (Exception e) {
            status = 0;
            LOG.error("保存添加线索个人信息" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + "失败", e);
        }
        return status;
    }

    /**
     * 保存线索标记的信息
     *
     * @param dto
     * @return
     */
    public boolean updateClueSignData(CustomSeaTouchInfoDTO dto, JSONObject jsonObject) {
        // 处理qq 微信等默认自建属性值
        handleDefaultLabelValue(dto, jsonObject);
        StringBuffer sql = new StringBuffer();
        boolean status;
        try {
            //查询默认客群
            String cgId = customerSeaService.createDefaultClueCGroup0(NumberUtil.parseLong(dto.getCustomerSeaId()), "公海默认客群", dto.getCust_id());
            dto.setCust_group_id(cgId);

            LOG.info("开始更新客户群数据表个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + ",数据:" + dto.toString());
            sql.append("UPDATE " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + " SET ")
                    .append(" super_name= ?, ")
                    .append(" super_age= ?, ")
                    .append(" super_sex= ?, ")
                    .append(" super_telphone= ?, ")
                    .append(" super_phone= ?, ")
                    .append(" super_address_province_city= ?, ")
                    .append(" super_address_street = ?, ")
                    .append(" super_data = ?, ")
                    .append(" update_time = ?, ")
                    .append(" STATUS = '0', ")
                    .append(" user_id = ? ")
                    .append(" WHERE id = ? ");
            crmLeadsDao.executeUpdateSQL(sql.toString(), dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()),
                    new Timestamp(System.currentTimeMillis()), dto.getUser_id(), dto.getSuper_id());

            LOG.info("开始更新公海[" + dto.getCustomerSeaId() + "]数据表个人信息:" + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId() + ",数据:" + dto.toString());
            sql = new StringBuffer();
            sql.append("UPDATE " + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId() + " SET ")
                    .append(" super_name= ?, ")
                    .append(" super_age= ?, ")
                    .append(" super_sex= ?, ")
                    .append(" super_telphone= ?, ")
                    .append(" super_phone= ?, ")
                    .append(" super_address_province_city= ?, ")
                    .append(" super_address_street = ?, ")
                    .append(" super_data = ?, ")
                    .append(" last_mark_time = ?, ")
                    .append(" update_time = ?, ")
                    .append(" STATUS = '0', ")
                    .append(" user_id = ? ")
                    .append(" WHERE id = ? ");
            crmLeadsDao.executeUpdateSQL(sql.toString(), dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), new Timestamp(System.currentTimeMillis()),
                    new Timestamp(System.currentTimeMillis()), dto.getUser_id(), dto.getSuper_id());
            // 保存标记信息到es中
            CustomerSeaESDTO esData = new CustomerSeaESDTO(dto);
            esData.setSuper_data(JSON.toJSONString(dto.getSuperData()));
            //saveClueInfoToES(esData);
            // 保存标记记录
            customerLabelService.saveSuperDataLog(dto.getSuper_id(), dto.getCust_group_id(), "", dto.getUser_id(),
                    dto.getSuperData(), dto.getCustomerSeaId());
            //保存根据记录
            crmRecordService.updateRecord(jsonObject.getJSONArray("field"), dto.getSuper_id());
            //保存标记字段
            adminFieldService.save(jsonObject.getJSONArray("field"), dto.getSuper_id());
            // 保存操作记录
            crmRecordService.addRecord(dto.getSuper_id(), CrmEnum.PUBLIC_SEA_TYPE_KEY.getTypes());

            status = true;
        } catch (Exception e) {
            status = false;
            LOG.error("更新数据表个人信息" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + "失败", e);
        }
        return status;
    }

    /**
     * 根据线索id查询
     */
    public Map queryClueById(long seaId, String id) {
        List<Map<String, Object>> publicSeaClue = crmLeadsDao.getPublicSeaClue(seaId, id);
        if (publicSeaClue.size() > 0) {
            return publicSeaClue.get(0);
        }
        return null;
    }

    /**
     * 公海线索基本信息
     */
    public List<Record> information(Long seaId, String custId, String id) throws TouchException {
        CustomerSea customerSea = customerSeaDao.get(seaId);
        if (ObjectUtil.notEqual(custId, customerSea.getCustId())) {
            throw new TouchException("线索公海不属于该客户");
        }
        StringBuffer sql = new StringBuffer(" SELECT * FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                .append(" WHERE id = ? ");

        List<Map<String, Object>> crmLeads = crmLeadsDao.sqlQuery(sql.toString(), id);
        if (crmLeads.size() == 0) {
            throw new TouchException("线索不存在");
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        JSONObject superData = JSON.parseObject(String.valueOf(crmLeads.get(0).get("super_data")));

        field.set("线索名称", superData.getString("leads_name")).set("电话", String.valueOf(crmLeads.get(0).get("super_phone")))
                .set("手机", String.valueOf(crmLeads.get(0).get("super_telphone"))).set("下次联系时间", DateUtil.formatDateTime(superData.getDate("next_time")))
                .set("地址", String.valueOf(crmLeads.get(0).get("super_address_street"))).set("备注", superData.getString("remark"));
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldvDao.queryCustomField(id));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }


    /**
     * 线索分配
     *
     * @param param
     * @param operate
     * @param assignedList
     * @return
     * @throws TouchException
     */
    public int distributionClue(CustomerSeaSearch param, int operate, JSONArray assignedList) throws TouchException {
        // 单一负责人分配线索|手动领取所选
        if (1 == operate) {
            return singleDistributionClue(param.getSeaId(), param.getUserIds().get(0), param.getSuperIds());
        } else if (2 == operate) {
            // 坐席根据检索条件批量领取线索
            return batchReceiveClue(param, param.getUserIds().get(0));
        } else if (3 == operate) {
            //根据检索条件批量给多人快速分配线索
            return batchDistributionClue(param, assignedList);
        } else if (4 == operate) {
            //坐席指定数量领取线索
            return getReceiveClueByNumber(param.getSeaId(), param.getUserIds().get(0), param.getGetClueNumber());
        }
        return 0;
    }


    /**
     * 线索领取方式 1-手动 2-系统自动
     *
     * @param seaId
     * @return
     */
    private int getCustomerSeaClueGetMode(String seaId) {
        CustomerSeaProperty cp = customerSeaDao.getProperty(seaId, "clueGetMode");
        if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
            return 0;
        }
        return NumberConvertUtil.parseInt(cp.getPropertyValue());
    }

    /**
     * 线索领取限制 1-无限制 2-限制数量
     *
     * @param seaId
     * @return
     */
    private int getCustomerSeaGetRestrict(String seaId) {
        CustomerSeaProperty cp = customerSeaDao.getProperty(seaId, "clueGetRestrict");
        if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
            return 0;
        }
        return NumberConvertUtil.parseInt(cp.getPropertyValue());
    }

    /**
     * 获取用户指定公海当天可领取线索数量
     *
     * @param seaId
     * @return -1 无限制领取 大于0标识可领取数量
     */
    public long getUserReceivableQuantity(String seaId, String userId) throws TouchException {
        int getMode = getCustomerSeaClueGetMode(seaId);
        int getRestrict = getCustomerSeaGetRestrict(seaId);
        // 手动领取
        if (getMode == 1) {
            //限制数量
            if (getRestrict == 2) {
                CustomerSeaProperty cp = customerSeaDao.getProperty(seaId, "clueGetRestrictValue");
                if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
                    return 0L;
                }
                long clueGetRestrictValue = NumberConvertUtil.parseLong(cp.getPropertyValue());

                LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
                StringBuilder sql = new StringBuilder();
                // 查询转交记录表公海下当天用户已经领取的线索数量
                sql.append(" SELECT COUNT(0) count FROM ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" WHERE event_type = 5 AND user_id = ? AND customer_sea_id = ? AND create_time BETWEEN ? AND ?");
                List<Map<String, Object>> list = customerSeaDao.sqlQuery(sql.toString(), userId, seaId, DatetimeUtils.DATE_TIME_FORMATTER.format(min), DatetimeUtils.DATE_TIME_FORMATTER.format(max));
                long value = clueGetRestrictValue - NumberConvertUtil.parseLong(list.get(0).get("count"));
                if (value < 0) {
                    LOG.warn("公海:[" + seaId + "],用户:[" + userId + "]可领取量:[" + value + "]小于0");
                    value = 0;
                }
                return value;
            } else if (getRestrict == 1) {
                // 标识可无限制领取
                return -1;
            }
        } else {
            //throw new TouchException("-1", "当前公海非手动领取模式");
            return -1;
        }
        return 0;
    }

    /**
     * 单一负责人分配线索
     * 领取所选
     *
     * @param seaId
     * @param userId
     * @param superIds
     * @return
     * @throws TouchException
     */
    private int singleDistributionClue(String seaId, String userId, List<String> superIds) throws TouchException {
        LOG.info("分配的userId是：" + userId);
        if (superIds == null || superIds.size() == 0) {
            throw new TouchException("-1", "superIds必填");
        }
        long quantity = getUserReceivableQuantity(seaId, userId);
        List<String> tempList = new ArrayList<>();
        boolean limit = false;
        if (quantity == 0) {
            throw new TouchException("-1", "当天领取线索已达上限");
        } else {
            tempList.addAll(superIds);
            if (quantity < superIds.size()) {
                limit = true;
            }
        }
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`,  `create_time`) ")
                .append(" SELECT ").append(userId).append(" ,id,").append(seaId).append(",batch_id,").append(5).append(",'").append(new Timestamp(System.currentTimeMillis())).append("'")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE status = 1 AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(tempList)).append(")");
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                //.append(" SET status = 0, user_id = ?, user_get_time = ? WHERE status = 1 AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(tempList)).append(")");
                .append(" SET status = 0, user_id = ?, user_get_time = ? WHERE id IN (").append(SqlAppendUtil.sqlAppendWhereIn(tempList)).append(")");
        List<Object> p = new ArrayList<>();
        p.add(userId);
        p.add(new Timestamp(System.currentTimeMillis()));
        if (limit && quantity >= 0) {
            p.add(quantity);
            sql.append(" LIMIT ? ");
            logSql.append(" LIMIT ? ");
        }

        transferToPrivateSea(seaId, userId, superIds);
        // 保存转交记录
        customerSeaDao.executeUpdateSQL(logSql.toString());
        return customerSeaDao.executeUpdateSQL(sql.toString(), p.toArray());
    }

    private int singleDistributionClue1(CustomerSeaSearch param, int operate, JSONArray assignedList) throws TouchException {
        String[] split = param.getCustType().split(",");
        // 根据指定条件删除线索
        StringBuffer stb = new StringBuffer();
        for (String custType : split) {
            stb.append("'");
            stb.append(custType);
            stb.append("',");
        }
        stb.deleteCharAt(stb.length() - 1);
        StringBuffer sql = new StringBuffer();
        sql.append("select id from " + ConstantsUtil.SEA_TABLE_PREFIX + param.getSeaId() + " custG where custG.super_data ->> '$.SYS014' in ( " + stb + ")");
        List<String> list = new ArrayList<>();
        crmLeadsDao.sqlQuery(sql.toString()).stream().forEach(map -> {
            list.add(map.get("id").toString());
        });
        param.setSuperIds(list);
        return singleDistributionClue(param.getSeaId(), param.getUserIds().get(0), param.getSuperIds());
    }

    /**
     * 领取到线索私海
     *
     * @param seaId
     * @param userId
     * @param superIds
     * @return
     */
    private int transferToPrivateSea(String seaId, String userId, List<String> superIds) {
        //添加到线索私海数据
        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM  ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE id IN (")
                .append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(" ) ");
        List<Map<String, Object>> maps = customerSeaDao.sqlQuery(sql.toString());
        int i = 0;
        for (Map<String, Object> m : maps) {
            JSONObject superData = JSON.parseObject(String.valueOf(m.get("super_data")));
            LkCrmLeadsEntity crmLeads = BeanUtil.mapToBean(m, LkCrmLeadsEntity.class, true);
            crmLeads.setLeadsName(superData.getString("SYS014") + (++i));
            // 查询公海线索的标记信息
            List<Map<String, Object>> fieldList = crmAdminFieldvDao.queryCustomField(String.valueOf(m.get("id")));
            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> field : fieldList) {
                jsonArray.add(BeanUtil.mapToBean(field, LkCrmAdminFieldvEntity.class, true));
            }

            String batchId = IdUtil.simpleUUID();
            crmLeads.setBatchId(batchId);
            crmLeads.setCustId(BaseUtil.getUser().getCustId());
            crmRecordService.updateRecord(jsonArray, batchId);
            adminFieldService.save(jsonArray, batchId);
            crmLeads.setCreateTime(DateUtil.date().toTimestamp());
            crmLeads.setUpdateTime(DateUtil.date().toTimestamp());
            crmLeads.setCreateUserId(BaseUtil.getUser().getUserId());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(BaseUtil.getUser().getUserId());
            }
            crmLeads.setBatchId(batchId);
            int id = (int) crmLeadsDao.saveReturnPk(crmLeads);
            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
        }
        return 0;
    }

    /**
     * 根据检索条件批量给多人快速分配线索
     *
     * @param param
     * @param assignedList
     * @return
     */
    private int batchDistributionClue(CustomerSeaSearch param, JSONArray assignedList) throws TouchException {
        int count = 0;
        // 处理多个负责人拆分多个线索分配
        long quantity = 0, number = 0;
        String userId;
        StringBuilder update = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" custG SET custG.status = 0, user_id = ?, user_get_time = ?  WHERE custG.id =? ");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX)
                .append("( `user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, `create_time`) VALUES (?,?,?,?,?,?)");
        StringBuilder select = new StringBuilder("SELECT * FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" custG WHERE custG.status = 1 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getSearch())) {
            p.add(param.getSearch());
            p.add(param.getSearch());
            p.add(param.getSearch());
            p.add(param.getSearch());
            select.append(" and (super_name like '%?%' or super_telphone like '%?%' or super_phone like '%?%' or super_data like '%?%')");
        }
        select.append(" LIMIT ? for update; ");
        for (int i = 0; i < assignedList.size(); i++) {
            userId = assignedList.getJSONObject(i).getString("userId");
            number = assignedList.getJSONObject(i).getInteger("number");
            try {
                quantity = getUserReceivableQuantity(param.getSeaId(), userId);
            } catch (TouchException e) {
                LOG.error("批量快速分配线索异常,fromUserId:" + userId + ",number:" + number, e);
            }
            if (quantity != -1) {
                //-1表示可无限制领取
                if (quantity == 0) {
                    LOG.warn("fromUserId:[" + userId + "],number:[" + number + "]当天领取线索已达上限,quantity:" + quantity);
                    continue;
                } else if (quantity < number) {
                    LOG.warn("fromUserId:[" + userId + "],number:[" + number + "]可分配数量不足");
                    continue;
                }
            }
            List<Map<String, Object>> maps = customerSeaDao.sqlQuery(select.toString(), number, p.toArray());
            List<String> superIds = new ArrayList<>();
            for (Map<String, Object> m : maps) {
                // 更改线索状态
                count += customerSeaDao.executeUpdateSQL(update.toString(), userId, now, m.get("id"));
                // 保存转交记录
                customerSeaDao.executeUpdateSQL(logSql.toString(), userId, m.get("id"), param.getSeaId(), m.get("batch_id"), 5, now);
                superIds.add(String.valueOf(m.get("id")));
            }
            transferToPrivateSea(param.getSeaId(), userId, superIds);
        }
        return count;
    }

    /**
     * 坐席根据检索条件批量领取线索
     *
     * @param param
     * @param userId
     * @return
     */
    private int batchReceiveClue(CustomerSeaSearch param, String userId) throws TouchException {
        StringBuilder update = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" custG SET custG.status = 0, user_id = ?, user_get_time = ?  WHERE custG.id =? ");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX)
                .append("( `user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, `create_time`) VALUES (?,?,?,?,?,?)");

        StringBuilder select = new StringBuilder("SELECT * FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" custG WHERE custG.status = 1  ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getSearch())) {
            p.add(param.getSearch());
            p.add(param.getSearch());
            p.add(param.getSearch());
            p.add(param.getSearch());
            select.append(" and (super_name like '%?%' or super_telphone like '%?%' or super_phone like '%?%' or super_data like '%?%')");
        }
        select.append(" for update; ");
        List<Map<String, Object>> maps = customerSeaDao.sqlQuery(select.toString(), p.toArray());
        int count = 0;
        List<String> superIds = new ArrayList<>();
        for (Map<String, Object> m : maps) {
            // 更改线索状态
            count = customerSeaDao.executeUpdateSQL(update.toString(), userId, now, m.get("id"));
            // 保存转交记录
            customerSeaDao.executeUpdateSQL(logSql.toString(), userId, m.get("id"), param.getSeaId(), m.get("batch_id"), 5, now);
            superIds.add(String.valueOf(m.get("id")));
        }
        transferToPrivateSea(param.getSeaId(), userId, superIds);
        return count;
    }

    /**
     * 坐席指定数量领取线索
     *
     * @param seaId
     * @param userId
     * @param number
     * @return
     * @throws TouchException
     */
    private int getReceiveClueByNumber(String seaId, String userId, int number) throws TouchException {
        long quantity = getUserReceivableQuantity(seaId, userId);
        LOG.info("可领取数量是:" + quantity);
        if (quantity == 0) {
            throw new TouchException("-1", "当天领取线索已达上限");
        }
        int count = 0;
        StringBuilder update = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                .append(" custG SET custG.status = 0, user_id = ?, user_get_time = ?  WHERE custG.id =? ");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX)
                .append("( `user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, `create_time`) VALUES (?,?,?,?,?,?)");

        StringBuilder select = new StringBuilder("SELECT * FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                .append(" custG WHERE custG.status = 1 LIMIT ? for update; ");
        List<Map<String, Object>> maps = customerSeaDao.sqlQuery(select.toString(), number);
        List<String> superIds = new ArrayList<>();
        for (Map<String, Object> m : maps) {
            // 更改线索状态
            count = customerSeaDao.executeUpdateSQL(update.toString(), userId, now, m.get("id"));
            // 保存转交记录
            customerSeaDao.executeUpdateSQL(logSql.toString(), userId, m.get("id"), seaId, m.get("batch_id"), 5, now);
            superIds.add(String.valueOf(m.get("id")));
        }
        transferToPrivateSea(seaId, userId, superIds);
        return count;
    }


    /**
     * @author wyq
     * 分页条件查询线索
     */
    public Page<Record> getLeadsPageList(BasePageRequest<CrmLeads> basePageRequest) {
        String leadsName = basePageRequest.getData().getLeadsName();
        if (!crmParamValid.isValid(leadsName)) {
            return new Page<>();
        }
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        if (StrUtil.isEmpty(leadsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile)) {
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.leads.getLeadsPageList", Kv.by("leadsName", leadsName).set("telephone", telephone).set("mobile", mobile)));
    }

    /**
     * @author wyq
     * 新增或更新线索
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject object) {
        LkCrmLeadsEntity crmLeads = object.getObject("entity", LkCrmLeadsEntity.class);
        crmLeads.setCustId(BaseUtil.getUser().getCustId());
        String batchId = StrUtil.isNotEmpty(crmLeads.getBatchId()) ? crmLeads.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(object.getJSONArray("field"), batchId);
        adminFieldService.save(object.getJSONArray("field"), batchId);
        if (crmLeads.getLeadsId() != null) {
            crmLeads.setCustomerId(0);
            crmLeads.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            crmRecordService.updateRecord(crmLeadsDao.get(crmLeads.getLeadsId()), crmLeads, CrmEnum.LEADS_TYPE_KEY.getTypes());
            //return crmLeads.update() ? R.ok() : R.error();
            crmLeadsDao.saveOrUpdate(crmLeads);
            return R.ok();
        } else {
            crmLeads.setCreateTime(new Timestamp(System.currentTimeMillis()));
            crmLeads.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            crmLeads.setCreateUserId(BaseUtil.getUser().getUserId());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(BaseUtil.getUser().getUserId());
            }
            crmLeads.setBatchId(batchId);
            //boolean save = crmLeads.save();
            int id = (int) crmLeadsDao.saveReturnPk(crmLeads);
            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            return id > 0 ? R.ok() : R.error();
        }
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer leadsId) {
        LkCrmLeadsEntity crmLeads = crmLeadsDao.get(leadsId);
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("线索名称", crmLeads.getLeadsName()).set("电话", crmLeads.getMobile())
                .set("手机", crmLeads.getTelephone()).set("下次联系时间", DateUtil.formatDateTime(crmLeads.getNextTime()))
                .set("地址", crmLeads.getAddress()).set("备注", crmLeads.getRemark());
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldvDao.queryCustomField(crmLeads.getBatchId()));
        //List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"), crmLeads.getBatchId());
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * @author wyq
     * 根据线索id查询
     */
    public Record queryById(Integer leadsId) {
        return JavaBeanUtil.mapToRecord(crmLeadsDao.queryById(leadsId));
        //return Db.findFirst(Db.getSql("crm.leads.queryById"), leadsId);
    }

    /**
     * @author wyq
     * 根据线索名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.leads.queryByName"), name);
    }

    /**
     * @author wyq
     * 根据id 删除线索
     */
    public R deleteByIds(String leadsIds) {
        String[] idsArr = leadsIds.split(",");
        List<String> idsList = new ArrayList<>();
        for (String id : idsArr) {
            //Record record = new Record();
            idsList.add(id);
        }
        List<String> batchIdList = JavaBeanUtil.mapToRecords(crmLeadsDao.queryBatchIdByIds(Arrays.asList(idsArr)));
        return Db.tx(() -> {
            //Db.batch(Db.getSql("crm.leads.deleteByIds"), "leads_id", idsList, 100);
            crmLeadsDao.deleteByIds(idsList);
            crmLeadsDao.executeUpdateSQL("delete from lkcrm_admin_fieldv where batch_id IN( ? )", SqlAppendUtil.sqlAppendWhereIn(batchIdList));
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 变更负责人
     */
    public R updateOwnerUserId(String leadsIds, Integer ownerUserId) {
        String[] ids = leadsIds.split(",");
        int update = crmLeadsDao.updateOwnerUserId(ownerUserId.toString(), Arrays.asList(ids));
        //int update = Db.update(Db.getSqlPara("crm.leads.updateOwnerUserId", Kv.by("ownerUserId", ownerUserId).set("ids", ids)));
        for (String id : ids) {
            crmRecordService.addConversionRecord(Integer.valueOf(id), CrmEnum.LEADS_TYPE_KEY.getTypes(), ownerUserId);
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 线索转客户
     */
    @Before(Tx.class)
    public R translate(String leadsIds) {
        String[] leadsIdsArr = leadsIds.split(",");
        for (String leadsId : leadsIdsArr) {
            List<Map<String, Object>> maps = crmLeadsDao.sqlQuery("select * from leadsview where leads_id = ?", Integer.valueOf(leadsId));
            Record crmLeads = JavaBeanUtil.mapToRecord(maps.get(0));
            if (1 == crmLeads.getInt("is_transform")) {
                return R.error("已转化线索不能再次转化");
            }
            List<Record> leadsFields = adminFieldService.list("1");
            LkCrmCustomerEntity crmCustomer = new LkCrmCustomerEntity();
            crmCustomer.setCustomerName(crmLeads.getStr("leads_name"));
            crmCustomer.setIsLock(0);
            crmCustomer.setNextTime(crmLeads.getTimestamp("next_time"));
            crmCustomer.setMobile(crmLeads.getStr("mobile"));
            crmCustomer.setTelephone(crmLeads.getStr("telephone"));
            crmCustomer.setDealStatus("未成交");
            crmCustomer.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmCustomer.setOwnerUserId(crmLeads.getInt("owner_user_id"));
            crmCustomer.setCreateTime(new Timestamp(System.currentTimeMillis()));
            crmCustomer.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            crmCustomer.setRoUserId(",");
            crmCustomer.setRwUserId(",");
            crmCustomer.setDetailAddress(crmLeads.getStr("address"));
            crmCustomer.setLocation("");
            crmCustomer.setAddress("");
            crmCustomer.setLng("");
            crmCustomer.setLat("");
            crmCustomer.setRemark("");
            String customerBatchId = IdUtil.simpleUUID();
            crmCustomer.setBatchId(customerBatchId);
            List<LkCrmAdminFieldEntity> customerFields = crmLeadsDao.queryListBySql("select field_id,name,field_name,field_type from lkcrm_admin_field where label = '2'", LkCrmAdminFieldEntity.class);
            List<LkCrmAdminFieldvEntity> adminFieldvList = new ArrayList<>();
            for (Record leadsFIeld : leadsFields) {
                for (LkCrmAdminFieldEntity customerField : customerFields) {
                    if (leadsFIeld.get("relevant") != null && customerField.getFieldId().equals(leadsFIeld.get("relevant"))) {
                        if (customerField.getFieldType().equals(1)) {
                            ReflectionUtils.setFieldValue(crmCustomer, customerField.getFieldName(), crmLeads.get(leadsFIeld.get("field_name")));
                            //crmCustomer.set(customerField.getFieldName(), crmLeads.get(leadsFIeld.get("field_name")));
                        } else {
                            LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                            adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                            adminFieldv.setFieldId(customerField.getFieldId());
                            adminFieldv.setName(customerField.getName());
                            adminFieldvList.add(adminFieldv);
                        }
                        continue;
                    }
                    if (!customerField.getFieldType().equals(0)) {
                        continue;
                    }
                    if ("客户来源".equals(customerField.getName()) && "线索来源".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户行业".equals(customerField.getName()) && "客户行业".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户级别".equals(customerField.getName()) && "客户级别".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                }
                ;
            }
            ;
            crmCustomerDao.save(crmCustomer);
            crmRecordService.addConversionCustomerRecord(crmCustomer.getCustomerId(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerName());
            adminFieldService.save(adminFieldvList, customerBatchId);
            crmLeadsDao.executeUpdateSQL("update lkcrm_crm_leads set is_transform = 1,update_time = ?,customer_id = ? where leads_id = ?",
                    DateUtil.date(), crmCustomer.getCustomerId(), Integer.valueOf(leadsId));
            List<LkCrmAdminRecordEntity> adminRecordList = crmAdminUserDao.queryListBySql("select * from lkcrm_admin_record where types = 'crm_leads' and types_id = ?", Integer.valueOf(leadsId), LkCrmAdminRecordEntity.class);
            List<LkCrmAdminFileEntity> adminFileList = new ArrayList<>();
            if (adminRecordList.size() != 0) {
                adminRecordList.forEach(adminRecord -> {
                    List<LkCrmAdminFileEntity> leadsRecordFiles = crmLeadsDao.queryListBySql("select * from lkcrm_admin_file where batch_id = ?", adminRecord.getBatchId(), LkCrmAdminFileEntity.class);
                    String customerRecordBatchId = IdUtil.simpleUUID();
                    leadsRecordFiles.forEach(adminFile -> {
                        adminFile.setBatchId(customerRecordBatchId);
                        adminFile.setFileId(null);
                    });
                    adminFileList.addAll(leadsRecordFiles);
                    adminRecord.setBatchId(customerRecordBatchId);
                    adminRecord.setRecordId(null);
                    adminRecord.setTypes("crm_customer");
                    adminRecord.setTypesId(crmCustomer.getCustomerId().toString());
                    adminRecord.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                });
                //Db.batchSave(adminRecordList, 100);
                crmLeadsDao.batchSaveOrUpdate(adminRecordList);
            }
            List<LkCrmAdminFileEntity> fileList = crmLeadsDao.queryListBySql("select * from lkcrm_admin_file where batch_id = ?", crmLeads.getStr("batch_id"), LkCrmAdminFileEntity.class);
            if (fileList.size() != 0) {
                fileList.forEach(adminFile -> {
                    adminFile.setBatchId(customerBatchId);
                    adminFile.setFileId(null);
                });
            }
            adminFileList.addAll(fileList);
            //Db.batchSave(adminFileList, 100);
            crmLeadsDao.batchSaveOrUpdate(adminFileList);
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer leadsId) {
        Record leads = JavaBeanUtil.mapToRecord(crmAdminUserDao.sqlQuery("select * from leadsview where leads_id = ?", leadsId).get(0));
        return adminFieldService.queryUpdateField(1, leads);
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(LkCrmAdminRecordEntity adminRecord) {
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        adminRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        adminRecord.setTypes("crm_leads");
        if (adminRecord.getIsEvent() != null && 1 == adminRecord.getIsEvent()) {
            LkCrmOaEventEntity oaEvent = new LkCrmOaEventEntity();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setCreateUserId(adminRecord.getCreateUserId());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1).toTimestamp());
            oaEvent.setCreateTime(DateUtil.date().toTimestamp());
            crmOaEventDao.save(oaEvent);
        }
        crmAdminRecordDao.executeUpdateSQL("update lkcrm_crm_leads set followup = 1 where leads_id = ?", adminRecord.getTypesId());
        return (int) crmAdminRecordDao.saveReturnPk(adminRecord) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmLeads> basePageRequest) {
        CrmLeads crmLeads = basePageRequest.getData();
        //List<Record> recordList = Db.find(Db.getSql("crm.leads.getRecord"), crmLeads.getLeadsId());
        List<Record> recordList = crmLeadsDao.getRecord(crmLeads.getLeadsId());
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
        });
        return recordList;
    }

    /**
     * @author wyq
     * 线索导出
     */
    public List<Record> exportLeads(String leadsIds) {
        String[] leadsIdsArr = leadsIds.split(",");
        return Db.find(Db.getSqlPara("crm.leads.excelExport", Kv.by("ids", leadsIdsArr)));
    }

    /**
     * @author wyq
     * 获取线索导入查重字段
     */
    public R getCheckingField() {
        return R.ok().put("data", "线索名称");
    }

    /**
     * @author wyq
     * 导入线索
     */
    public R uploadExcel0(UploadFile file, Integer repeatHandling, Integer ownerUserId) {
        ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()));
        //AdminFieldService adminFieldService = new AdminFieldService();
        Kv kv = new Kv();
        Integer errNum = 0;
        try {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(1);
            List<Record> recordList = adminFieldService.customFieldList("1");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(1);
            fieldList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1) {
                    record.set("name", record.getStr("name") + "(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)) {
                return R.error("请使用最新导入模板");
            }
            Kv nameMap = new Kv();
            fieldList.forEach(record -> nameMap.set(record.getStr("name"), record.getStr("field_name")));
            for (int i = 0; i < list.size(); i++) {
                kv.set(nameMap.get(list.get(i)), i);
            }
            if (read.size() > 2) {
                JSONObject object = new JSONObject();
                for (int i = 2; i < read.size(); i++) {
                    errNum = i;
                    List<Object> leadsList = read.get(i);
                    if (leadsList.size() < list.size()) {
                        for (int j = leadsList.size() - 1; j < list.size(); j++) {
                            leadsList.add(null);
                        }
                    }
                    String leadsName = leadsList.get(kv.getInt("leads_name")).toString();
                    Integer number = Db.queryInt("select count(*) from lkcrm_crm_leads where leads_name = ?", leadsName);
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number > 0 && repeatHandling == 1) {
                        Record leads = Db.findFirst("select leads_id,batch_id from lkcrm_crm_leads where leads_name = ?", leadsName);
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_id", leads.getInt("leads_id"))
                                .fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("batch_id", leads.getStr("batch_id")));
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("name") + "(*)");
                        record.set("value", leadsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    addOrUpdate(object);
                }
            }
        } catch (Exception e) {
            Log.getLog(getClass()).error("", e);
            if (errNum != 0) {
                return R.error("第" + (errNum + 1) + "行错误!");
            }
            return R.error();
        } finally {
            reader.close();
        }
        return R.ok();
    }

    public R uploadExcel(MultipartFile file, Integer repeatHandling, Integer ownerUserId) {
        //AdminFieldService adminFieldService = new AdminFieldService();
        Kv kv = new Kv();
        Integer errNum = 0;
        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(1);
            List<Record> recordList = adminFieldService.customFieldList("1");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(1);
            fieldList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1) {
                    record.set("name", record.getStr("name") + "(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)) {
                return R.error("请使用最新导入模板");
            }
            Kv nameMap = new Kv();
            fieldList.forEach(record -> nameMap.set(record.getStr("name"), record.getStr("field_name")));
            for (int i = 0; i < list.size(); i++) {
                kv.set(nameMap.get(list.get(i)), i);
            }
            if (read.size() > 2) {
                JSONObject object = new JSONObject();
                for (int i = 2; i < read.size(); i++) {
                    errNum = i;
                    List<Object> leadsList = read.get(i);
                    if (leadsList.size() < list.size()) {
                        for (int j = leadsList.size() - 1; j < list.size(); j++) {
                            leadsList.add(null);
                        }
                    }
                    String leadsName = leadsList.get(kv.getInt("leads_name")).toString();
                    Integer number = crmLeadsDao.queryForInt("select count(*) from lkcrm_crm_leads where leads_name = ?", leadsName);
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number > 0 && repeatHandling == 1) {
                        Record leads = JavaBeanUtil.mapToRecord(crmLeadsDao.sqlQuery("select leads_id,batch_id from lkcrm_crm_leads where leads_name = ?", leadsName).get(0));
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_id", leads.getInt("leads_id"))
                                .fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("batch_id", leads.getStr("batch_id")));
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("name") + "(*)");
                        record.set("value", leadsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    addOrUpdate(object);
                }
            }
        } catch (Exception e) {
            LOG.error("", e);
            if (errNum != 0) {
                return R.error("第" + (errNum + 1) + "行错误!");
            }
            return R.error();
        }
        return R.ok();
    }

    public R uploadExcelPublicSea(MultipartFile file, Integer repeatHandling, Integer ownerUserId) {
        Kv kv = new Kv();
        Integer errNum = 0;
        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(1);
            List<Record> recordList = adminFieldService.customFieldList("11");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(11);
            fieldList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1) {
                    record.set("name", record.getStr("name") + "(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)) {
                return R.error("请使用最新导入模板");
            }
            Kv nameMap = new Kv();
            fieldList.forEach(record -> nameMap.set(record.getStr("name"), record.getStr("field_name")));
            for (int i = 0; i < list.size(); i++) {
                kv.set(nameMap.get(list.get(i)), i);
            }
            if (read.size() > 2) {
                JSONObject object = new JSONObject();
                for (int i = 2; i < read.size(); i++) {
                    errNum = i;
                    List<Object> leadsList = read.get(i);
                    if (leadsList.size() < list.size()) {
                        for (int j = leadsList.size() - 1; j < list.size(); j++) {
                            leadsList.add(null);
                        }
                    }
                    String leadsName = leadsList.get(kv.getInt("leads_name")).toString();
                    Integer number = crmLeadsDao.queryForInt("select count(*) from lkcrm_crm_leads where leads_name = ?", leadsName);
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number > 0 && repeatHandling == 1) {
                        Record leads = JavaBeanUtil.mapToRecord(crmLeadsDao.sqlQuery("select leads_id,batch_id from lkcrm_crm_leads where leads_name = ?", leadsName).get(0));
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_id", leads.getInt("leads_id"))
                                .fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("batch_id", leads.getStr("batch_id")));
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("name") + "(*)");
                        record.set("value", leadsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    addOrUpdate(object);
                }
            }
        } catch (Exception e) {
            LOG.error("", e);
            if (errNum != 0) {
                return R.error("第" + (errNum + 1) + "行错误!");
            }
            return R.error();
        }
        return R.ok();
    }
}
