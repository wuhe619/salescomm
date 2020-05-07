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
import com.bdaim.auth.LoginUser;
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
import com.bdaim.customersea.dto.CustomerSeaParam;
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
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Future;
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
    @Resource
    private LkCrmTaskDao crmTaskDao;
    @Autowired
    private LkCrmActionRecordDao crmActionRecordDao;
    @Autowired
    private LkCrmOwnerRecordDao crmOwnerRecordDao;

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
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * @return
     * @author wyq
     * 分页条件查询线索
     */
    public List listCluePublicSea(BasePageRequest<CrmLeads> basePageRequest, long seaId, String custId) throws TouchException {
        //String leadsName = basePageRequest.getData().getLeadsName();
        CustomerSea customerSea = customerSeaDao.get(seaId);
        if (ObjectUtil.notEqual(custId, customerSea.getCustId())) {
            throw new TouchException("线索公海不属于该客户");
        }
        String search = basePageRequest.getJsonObject().getString("search");
        if (!ParamsUtil.isValid(search)) {
            throw new TouchException("参数包含非法字段");
        }
        List<Map<String, Object>> list = crmLeadsDao.listCluePublicSea(seaId, search);
        if (list != null && list != null) {
            for (int i = 0; i < list.size(); i++) {
                Map map = (Map) list.get(i);
                // 解析super_data中qq 微信等属性
                //getDefaultLabelValue(map);
                map.remove("super_data");
            }
        }
        return list;
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
                //dataStatus = 1;
                dto.setUser_id(null);
            } else {
                // 超管和项目管理员数据状态为未分配
                dto.setUser_id(null);
            }
            LOG.info("开始保存添加线索个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + ",数据:" + JSON.toJSONString(dto));
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

            sql.append(" REPLACE INTO " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`,update_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), new Timestamp(System.currentTimeMillis()));

            sql = new StringBuffer();
            sql.append(" REPLACE INTO " + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId())
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
            JSONObject superData = JSON.parseObject(String.valueOf(publicSeaClue.get(0).get("super_data")));
            publicSeaClue.get(0).put("company", superData.getString("SYS005"));
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

//        field.set("线索名称", superData.getString("leads_name")).set("电话", String.valueOf(crmLeads.get(0).get("super_phone")))
//                .set("手机", String.valueOf(crmLeads.get(0).get("super_telphone"))).set("下次联系时间", DateUtil.formatDateTime(superData.getDate("next_time")))
//                .set("地址", String.valueOf(crmLeads.get(0).get("super_address_street"))).set("备注", superData.getString("remark"));
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldvDao.queryCustomField(id));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);


        Set names = new HashSet();
        for (Record r : recordList) {
            names.add(r.getStr("name"));
        }
        List<Record> dbList = JavaBeanUtil.mapToRecords(crmAdminFieldvDao.sqlQuery("SELECT * FROM lkcrm_admin_field WHERE label = ? AND cust_id =?", 11, BaseUtil.getCustId()));
        for (Record r : dbList) {
            if (!names.contains(r.getStr("name")) && !"公司名称".equals(r.getStr("name"))) {
                fieldList.add(r);
            }
        }
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
        int status = 0;
        // 领取所选给自己 普通员工
        if (1 == operate) {
            /*if (BaseUtil.getUserType() == 1) {
                throw new TouchException("管理员不能领取线索");
            }*/
            status = singleDistributionClue(param.getSeaId(), param.getUserIds().get(0), param.getSuperIds());
        } else if (2 == operate) {
            // 分配所选给坐席 管理员
            status = batchReceiveClue(param, param.getUserIds().get(0));
        } else if (3 == operate) {
            // 快速分配给坐席 管理员
            status = batchDistributionClue(param, assignedList);
        } else if (4 == operate) {
            // 快速领取给自己 普通员工
            /*if (BaseUtil.getUserType() == 1) {
                throw new TouchException("管理员不能领取线索");
            }*/
            status = getReceiveClueByNumber(param.getSeaId(), param.getUserIds().get(0), param.getGetClueNumber());
        }
        return status;
    }

    public com.bdaim.common.dto.Page listPublicSea(CustomerSeaParam param, int pageNum, int pageSize) {
        List values = new ArrayList();
        values.add(BaseUtil.getCustId());
        com.bdaim.common.dto.Page page = customerSeaDao.page("from CustomerSea m where custId = ? ", values, pageNum, pageSize);
        return page;
    }

    /**
     * 线索领取方式 1-手动 2-系统自动
     *
     * @param seaId
     * @return
     */
    private int getCustomerSeaClueGetMode(String seaId) {
      /*  CustomerSeaProperty cp = customerSeaDao.getProperty(seaId, "clueGetMode");
        if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
            return 0;
        }
        return NumberConvertUtil.parseInt(cp.getPropertyValue());*/
        return 1;
    }

    /**
     * 线索领取限制 1-无限制 2-限制数量
     *
     * @param seaId
     * @return
     */
    private int getCustomerSeaGetRestrict(String seaId) {
        Map<String, Object> seaRule = crmLeadsDao.queryUniqueSql("SELECT * FROM lkcrm_admin_config WHERE `name` = 'seaRule' AND cust_id = ? ", BaseUtil.getCustId());
        if (seaRule != null && seaRule.size() > 0) {
            JSONObject value, receive;
            value = JSON.parseObject(String.valueOf(seaRule.get("value")));
            if (value != null && value.getJSONObject("receive") != null) {
                receive = value.getJSONObject("receive");
                if (receive.getBooleanValue("status")) {
                    return 2;
                }
            }
        }
        return 1;
    }

    private Long getCustomerSeaGetNum() {
        Map<String, Object> seaRule = crmLeadsDao.queryUniqueSql("SELECT * FROM lkcrm_admin_config WHERE `name` = 'seaRule' AND cust_id = ? ", BaseUtil.getCustId());
        if (seaRule != null && seaRule.size() > 0) {
            JSONObject value, receive;
            value = JSON.parseObject(String.valueOf(seaRule.get("value")));
            if (value != null && value.getJSONObject("receive") != null) {
                receive = value.getJSONObject("receive");
                if (receive.getBooleanValue("status")) {
                    return receive.getLong("num");
                }
            }
        }
        return 0L;
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
               /* CustomerSeaProperty cp = customerSeaDao.getProperty(seaId, "clueGetRestrictValue");
                if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
                    return 0L;
                }*/
                long clueGetRestrictValue = getCustomerSeaGetNum();

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
        //添加领取公海线索统计
        addPublicSeaStats(userId, superIds, null);
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

    private void addPublicSeaStats(String userId, List<String> superIds, Integer number) {
        List<LkCrmOwnerRecordEntity> list = new ArrayList<>();
        int total = !CollectionUtils.isEmpty(superIds) ? superIds.size() : number;
        for (int i = 0; i < total; i++) {
            //添加线索公海统计记录
            LkCrmOwnerRecordEntity crmOwnerRecord = new LkCrmOwnerRecordEntity();
            crmOwnerRecord.setTypeId(0);
            crmOwnerRecord.setType(9);
            crmOwnerRecord.setPostOwnerUserId(Long.valueOf(userId));
            crmOwnerRecord.setCreateTime(DateUtil.date().toTimestamp());
            list.add(crmOwnerRecord);
        }
        if (!CollectionUtils.isEmpty(list)) {
            crmOwnerRecordDao.batchSaveOrUpdate(list);
        }
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
     * 转移私海线索到公海线索
     *
     * @param seaId
     * @param userId
     * @param batchId
     * @return
     */
    public int transferToPublicSea(String seaId, String userId, String batchId) {
        LOG.info("添加到线索私海[{}]数据,batchId:[{}]", seaId, batchId);
        //添加到线索私海数据
        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM lkcrm_crm_leads  WHERE batch_id =? ");
        List<Map<String, Object>> maps = customerSeaDao.sqlQuery(sql.toString(), batchId);
        int i = 0;
        String insertSql = "REPLACE INTO " + ConstantsUtil.SEA_TABLE_PREFIX + seaId + " (`id`, `user_id`, `update_time`, `status`, " +
                "super_name,super_telphone,super_phone,super_data,create_time,batch_id) VALUES(?,?,?,?,?,?,?,?,?,?)";

        LoginUser user = BaseUtil.getUser();
        String leadsview = BaseUtil.getViewSql("leadsview");
        LOG.info("batch_id:{},线索数据为:{}", batchId, JSON.toJSONString(maps));
        for (Map<String, Object> m : maps) {
            JSONObject superData = new JSONObject();
            superData.put("SYS005", m.get("company"));
            String superId = MD5Util.encode32Bit(m.get("leads_id") + "" + m.get("super_telphone"));

            //查询默认客群
            CustomerSeaProperty csp = customerSeaDao.getProperty(String.valueOf(seaId), "defaultClueCgId");
            customerSeaDao.executeUpdateSQL(insertSql, superId, null, new Date(), 1, m.get("leads_name")
                    , m.get("mobile"), m.get("telephone"), superData.toJSONString(), m.get("create_time"), csp.getPropertyValue());
            // 退回到公海线索
            List<Map<String, Object>> list = crmLeadsDao.sqlQuery("select * from " + leadsview + " where batch_id = ?", batchId);
            LOG.info("2batch_id:{},线索数据为:{}", batchId, JSON.toJSONString(maps));
            if (list == null || list.size() == 0) {
                continue;
            }
            Record crmLeads = JavaBeanUtil.mapToRecord(list.get(0));
            List<Record> leadsFields = adminFieldService.list("1");
            List<LkCrmAdminFieldEntity> seaFields = crmLeadsDao.find("from LkCrmAdminFieldEntity where label = '11' AND custId = ? ", user.getCustId());
            List<LkCrmAdminFieldvEntity> adminFieldvList = new ArrayList<>();
            for (Record leadsFIeld : leadsFields) {
                for (LkCrmAdminFieldEntity seaField : seaFields) {
                    if (!seaField.getFieldType().equals(0)) {
                        continue;
                    }
                    if (StringUtil.isNotEmpty(crmLeads.get(leadsFIeld.get("name"))) && leadsFIeld.getStr("name").equals(seaField.getName())) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(seaField.getFieldId());
                        adminFieldv.setName(seaField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldv.setBatchId(superId);
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("线索来源".equals(seaField.getName()) && "线索来源".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(seaField.getFieldId());
                        adminFieldv.setName(seaField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldv.setBatchId(superId);
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户行业".equals(seaField.getName()) && "客户行业".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(seaField.getFieldId());
                        adminFieldv.setName(seaField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldv.setBatchId(superId);
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户级别".equals(seaField.getName()) && "客户级别".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(seaField.getFieldId());
                        adminFieldv.setName(seaField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldv.setBatchId(superId);
                        adminFieldvList.add(adminFieldv);
                    }
                }
            }

            crmLeadsDao.getSession().clear();
            crmLeadsDao.batchSaveOrUpdate(adminFieldvList);

            List<Map<String, Object>> fieldList = crmAdminFieldvDao.queryCustomField(String.valueOf(m.get("batch_id")));
            JSONArray jsonArray = new JSONArray();
            LkCrmAdminFieldvEntity fieldvEntity;
            for (Map<String, Object> field : fieldList) {
                fieldvEntity = BeanUtil.mapToBean(field, LkCrmAdminFieldvEntity.class, true);
                fieldvEntity.setBatchId(superId);
                jsonArray.add(fieldvEntity);
            }
            //String batchId = String.valueOf(m.get("leads_id"));
            crmRecordService.updateRecord(jsonArray, superId);
            //adminFieldService.save(jsonArray, superId);
        }
        return i;
    }

    /**
     * 领取到线索私海
     *
     * @param seaId
     * @param userId
     * @param superIds
     * @return
     */
    public int transferToPrivateSea(String seaId, String userId, List<String> superIds) {
        //添加到线索私海数据
        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM  ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE id IN (")
                .append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(" ) ");
        List<Map<String, Object>> maps = customerSeaDao.sqlQuery(sql.toString());
        int i = 0;
        LoginUser user = BaseUtil.getUser();
        for (Map<String, Object> m : maps) {
            JSONObject superData = JSON.parseObject(String.valueOf(m.get("super_data")));
            LkCrmLeadsEntity crmLeads = BeanUtil.mapToBean(m, LkCrmLeadsEntity.class, true);
            crmLeads.setLeadsName(String.valueOf(m.get("super_name")));
            crmLeads.setMobile(String.valueOf(m.get("super_telphone")));
            crmLeads.setTelephone(String.valueOf(m.get("super_phone")));
            crmLeads.setAddress(String.valueOf(m.get("super_address_province_city")) + String.valueOf(m.get("super_address_street")));
            crmLeads.setCompany(superData.getString("SYS005"));
            crmLeads.setIsTransform(0);
            // 查询公海线索的标记信息
            List<Map<String, Object>> fieldList = crmAdminFieldvDao.queryCustomField(String.valueOf(m.get("id")));
            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> field : fieldList) {
                jsonArray.add(BeanUtil.mapToBean(field, LkCrmAdminFieldvEntity.class, true));
            }

            String batchId = String.valueOf(m.get("id"));
            crmLeads.setBatchId(batchId);
            crmLeads.setCustId(user.getCustId());
            crmRecordService.updateRecord(jsonArray, batchId);
            adminFieldService.save(jsonArray, batchId);
            crmLeads.setCreateTime(DateUtil.date().toTimestamp());
            crmLeads.setUpdateTime(DateUtil.date().toTimestamp());
            crmLeads.setCreateUserId(user.getUserId());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(user.getUserId());
            }
            crmLeads.setBatchId(batchId);
            crmLeads.setSeaId(seaId);
            int id = (int) crmLeadsDao.saveReturnPk(crmLeads);

            // 复制公海线索字段值
            Record seaData = JavaBeanUtil.mapToRecord(queryClueById(NumberConvertUtil.parseLong(seaId), String.valueOf(m.get("id"))));
            List<Record> leadsFields = adminFieldService.list("11");
            List<LkCrmAdminFieldEntity> seaFields = crmLeadsDao.find("from LkCrmAdminFieldEntity where label = '1' AND custId = ? ", user.getCustId());
            List<LkCrmAdminFieldvEntity> adminFieldvList = new ArrayList<>();
            for (Record leadsFIeld : leadsFields) {
                for (LkCrmAdminFieldEntity seaField : seaFields) {
                    if (leadsFIeld.getInt("fieldType") != null && !leadsFIeld.getInt("fieldType").equals(0)) {
                        continue;
                    }
                    if (StringUtil.isNotEmpty(seaData.get(leadsFIeld.get("name"))) && leadsFIeld.getStr("name").equals(seaField.getName())) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(seaData.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(seaField.getFieldId());
                        adminFieldv.setName(seaField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldv.setBatchId(batchId);
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("线索来源".equals(seaField.getName()) && "线索来源".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(seaData.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(seaField.getFieldId());
                        adminFieldv.setName(seaField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldv.setBatchId(batchId);
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户行业".equals(seaField.getName()) && "客户行业".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(seaData.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(seaField.getFieldId());
                        adminFieldv.setName(seaField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldv.setBatchId(batchId);
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户级别".equals(seaField.getName()) && "客户级别".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(seaData.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(seaField.getFieldId());
                        adminFieldv.setName(seaField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldv.setBatchId(batchId);
                        adminFieldvList.add(adminFieldv);
                    }
                }
            }

            crmLeadsDao.getSession().clear();
            crmLeadsDao.batchSaveOrUpdate(adminFieldvList);

            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            // 保存uid对应关系
            phoneService.saveObjU(crmLeads.getLeadsId().toString(), crmLeads.getMobile(), 1, user.getCustId());
            phoneService.saveObjU(crmLeads.getLeadsId().toString(), crmLeads.getTelephone(), 1, user.getCustId());
        }
        return 0;
    }

    public int transferToPrivateSea(String seaId, String company, String userId, List<String> superIds) {
        //添加到线索私海数据
        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM  ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE id IN (")
                .append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(" ) ");
        List<Map<String, Object>> maps = customerSeaDao.sqlQuery(sql.toString());
        int i = 0;
        for (Map<String, Object> m : maps) {
            JSONObject superData = JSON.parseObject(String.valueOf(m.get("super_data")));
            LkCrmLeadsEntity crmLeads = BeanUtil.mapToBean(m, LkCrmLeadsEntity.class, true);
            crmLeads.setLeadsName(company + (++i));
            crmLeads.setCompany(company);
            crmLeads.setIsTransform(0);
            // 查询公海线索的标记信息
            List<Map<String, Object>> fieldList = crmAdminFieldvDao.queryCustomField(String.valueOf(m.get("id")));
            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> field : fieldList) {
                jsonArray.add(BeanUtil.mapToBean(field, LkCrmAdminFieldvEntity.class, true));
            }

            String batchId = String.valueOf(m.get("id"));
            crmLeads.setBatchId(batchId);
            LoginUser user = BaseUtil.getUser();
            crmLeads.setCustId(user.getCustId());
            crmRecordService.updateRecord(jsonArray, batchId);
            adminFieldService.save(jsonArray, batchId);
            crmLeads.setCreateTime(DateUtil.date().toTimestamp());
            crmLeads.setUpdateTime(DateUtil.date().toTimestamp());
            crmLeads.setCreateUserId(user.getUserId());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(user.getUserId());
            }
            crmLeads.setBatchId(batchId);
            crmLeads.setSeaId(seaId);
            int id = (int) crmLeadsDao.saveReturnPk(crmLeads);
            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
        }
        return 0;
    }

    public int transferToPrivateSea(String seaId, String company, String userId, List<String> superIds, int index) {
        //添加到线索私海数据
        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM  ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE id IN (")
                .append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(" ) ");
        List<Map<String, Object>> maps = customerSeaDao.sqlQuery(sql.toString());
        int i = 0;
        for (Map<String, Object> m : maps) {
            JSONObject superData = JSON.parseObject(String.valueOf(m.get("super_data")));
            LkCrmLeadsEntity crmLeads = BeanUtil.mapToBean(m, LkCrmLeadsEntity.class, true);
            crmLeads.setLeadsName(company + index);
            crmLeads.setCompany(company);
            crmLeads.setIsTransform(0);
            // 查询公海线索的标记信息
            List<Map<String, Object>> fieldList = crmAdminFieldvDao.queryCustomField(String.valueOf(m.get("id")));
            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> field : fieldList) {
                jsonArray.add(BeanUtil.mapToBean(field, LkCrmAdminFieldvEntity.class, true));
            }

            String batchId = String.valueOf(m.get("id"));
            crmLeads.setBatchId(batchId);
            LoginUser user = BaseUtil.getUser();
            crmLeads.setCustId(user.getCustId());
            crmRecordService.updateRecord(jsonArray, batchId);
            adminFieldService.save(jsonArray, batchId);
            crmLeads.setCreateTime(DateUtil.date().toTimestamp());
            crmLeads.setUpdateTime(DateUtil.date().toTimestamp());
            crmLeads.setCreateUserId(user.getUserId());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(user.getUserId());
            }
            crmLeads.setBatchId(batchId);
            crmLeads.setSeaId(seaId);
            crmLeads.setMobile(String.valueOf(m.get("super_telphone")));
            int id = (int) crmLeadsDao.saveReturnPk(crmLeads);
            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
        }
        return 0;
    }

    /**
     * @param seaId
     * @param company
     * @param mobile
     * @param telephone
     * @param index
     * @return
     */
    public String transferToPrivateSea(String seaId, String company, String mobile, String telephone, int index) {
        LoginUser user = BaseUtil.getUser();
        //添加到线索私海数据
        LkCrmLeadsEntity crmLeads = new LkCrmLeadsEntity();
        crmLeads.setLeadsName(company + index);
        crmLeads.setCompany(company);
        crmLeads.setIsTransform(0);
        crmLeads.setMobile(mobile);
        crmLeads.setTelephone(telephone);
        // 查询公海线索的标记信息
        String batchId;
        if (StringUtil.isNotEmpty(mobile)) {
            batchId = MD5Util.encode32Bit(mobile);
        } else if (StringUtil.isNotEmpty(telephone)) {
            batchId = MD5Util.encode32Bit(telephone);
        } else {
            batchId = MD5Util.encode32Bit(UUID.randomUUID().toString().replaceAll("-", ""));
        }

        crmLeads.setBatchId(batchId);
        crmLeads.setCustId(user.getCustId());
        crmLeads.setCreateTime(DateUtil.date().toTimestamp());
        crmLeads.setUpdateTime(DateUtil.date().toTimestamp());
        crmLeads.setCreateUserId(user.getUserId());
        if (crmLeads.getOwnerUserId() == null) {
            crmLeads.setOwnerUserId(user.getUserId());
        }
        crmLeads.setBatchId(batchId);
        crmLeads.setSeaId(seaId);
        int id = (int) crmLeadsDao.saveReturnPk(crmLeads);
        crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
        // 保存uid对应关系
        phoneService.saveObjU(crmLeads.getLeadsId().toString(), crmLeads.getMobile(), 1, user.getCustId());
        phoneService.saveObjU(crmLeads.getLeadsId().toString(), crmLeads.getTelephone(), 1, user.getCustId());
        return batchId;
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
        //select.append(" LIMIT ? for update; ");
        for (int i = 0; i < assignedList.size(); i++) {
            userId = assignedList.getJSONObject(i).getString("userId");
            number = assignedList.getJSONObject(i).getInteger("number");
            addPublicSeaStats(userId, null, Integer.parseInt(String.valueOf(number)));
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
            List<Map<String, Object>> maps = customerSeaDao.sqlQuery(select.toString() + " LIMIT " + number + " for update; ", p.toArray());
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
                .append(" custG WHERE custG.status = 1 AND id IN (" + SqlAppendUtil.sqlAppendWhereIn(param.getSuperIds()) + ") ");
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
        // 领取到线索私海
        transferToPrivateSea(param.getSeaId(), userId, superIds);
        // 添加线索公海统计记录
        addPublicSeaStats(userId, superIds, null);
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
        addPublicSeaStats(userId, null, number);
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
    public CrmPage getLeadsPageList(BasePageRequest<CrmLeads> basePageRequest) {
        String leadsName = basePageRequest.getData().getLeadsName();
        if (!crmParamValid.isValid(leadsName)) {
            return new CrmPage();
        }
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        if (StrUtil.isEmpty(leadsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile)) {
            return new CrmPage();
        }
        com.bdaim.common.dto.Page page = crmLeadsDao.pageLeadsList(basePageRequest.getPage(), basePageRequest.getLimit(), leadsName, telephone, mobile);
        return BaseUtil.crmPage(page);
        //return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.leads.getLeadsPageList", Kv.by("leadsName", leadsName).set("telephone", telephone).set("mobile", mobile)));
    }

    /**
     * 新增或更新线索
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject object) {
        CrmLeads entity = object.getObject("entity", CrmLeads.class);
        LkCrmLeadsEntity crmLeads = new LkCrmLeadsEntity();
        BeanUtils.copyProperties(entity, crmLeads);
        if (crmLeads.getIsTransform() == null) {
            crmLeads.setIsTransform(0);
        }

        crmLeads.setCompany(object.getJSONObject("entity").getString("company"));
        LoginUser user = BaseUtil.getUser();
        String custId = user.getCustId();
        crmLeads.setCustId(custId);
        // 查询客户默认公海
        List<CustomerSea> publicSeaList = crmLeadsDao.find(" FROM CustomerSea WHERE custId = ? ", custId);
        if (publicSeaList.size() > 0) {
            crmLeads.setSeaId(publicSeaList.get(0).getId().toString());
        }

        String batchId = StrUtil.isNotEmpty(crmLeads.getBatchId()) ? crmLeads.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(object.getJSONArray("field"), batchId);
        adminFieldService.save(object.getJSONArray("field"), batchId);
        if (entity.getLeadsId() != null) {
            crmLeads.setLeadsId(NumberConvertUtil.parseInt(entity.getLeadsId()));
            crmLeads.setCustomerId(0);
            crmLeads.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            crmRecordService.updateRecord(crmLeadsDao.get(crmLeads.getLeadsId()), crmLeads, CrmEnum.LEADS_TYPE_KEY.getTypes());
            //return crmLeads.update() ? R.ok() : R.error();
            LkCrmLeadsEntity crmLeadsDb = crmLeadsDao.get(crmLeads.getLeadsId());
            BeanUtils.copyProperties(crmLeads, crmLeadsDb, JavaBeanUtil.getNullPropertyNames(crmLeads));
            crmLeadsDao.update(crmLeadsDb);
            // 保存uid对应关系
            phoneService.saveObjU(crmLeads.getLeadsId().toString(), crmLeads.getMobile(), 1, custId);
            phoneService.saveObjU(crmLeads.getLeadsId().toString(), crmLeads.getTelephone(), 1, custId);
            return R.ok();
        } else {
            crmLeads.setCreateTime(new Timestamp(System.currentTimeMillis()));
            crmLeads.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            crmLeads.setCreateUserId(user.getUserId());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(user.getUserId());
            }
            crmLeads.setBatchId(batchId);
            //boolean save = crmLeads.save();
            int id = (int) crmLeadsDao.saveReturnPk(crmLeads);
            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            // 保存uid对应关系
            phoneService.saveObjU(crmLeads.getLeadsId().toString(), crmLeads.getMobile(), 1, custId);
            phoneService.saveObjU(crmLeads.getLeadsId().toString(), crmLeads.getTelephone(), 1, custId);
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
//        field.set("线索名称", crmLeads.getLeadsName()).set("电话", crmLeads.getMobile())
//                .set("手机", crmLeads.getTelephone()).set("下次联系时间", DateUtil.formatDateTime(crmLeads.getNextTime()))
//                .set("地址", crmLeads.getAddress()).set("备注", crmLeads.getRemark())
//                .set("公司名称", crmLeads.getCompany());


        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldvDao.queryCustomField(crmLeads.getBatchId()));
        //List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"), crmLeads.getBatchId());
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);

        Set names = new HashSet();
        for (Record r : recordList) {
            names.add(r.getStr("name"));
        }
        List<Record> dbList = JavaBeanUtil.mapToRecords(crmAdminFieldvDao.sqlQuery("SELECT * FROM lkcrm_admin_field WHERE label = ? AND cust_id =?", 1, BaseUtil.getCustId()));
        for (Record r : dbList) {
            if (!names.contains(r.getStr("name"))) {
                fieldList.add(r);
            }
        }

        List<Record> result = new ArrayList<>();
        for (Record r : fieldList) {
            if (r.getStr("name").equals("公司名称")) {
                continue;
            }
            if (r.getStr("name").equals("当前负责人")) {
                Record record = new Record();
                LkCrmAdminUserEntity createUser = crmAdminUserDao.get(crmLeads.getCreateUserId());
                record.set("name", "创建人").set("value", createUser.getUsername());
                result.add(record);
            }
            result.add(r);
        }
        return result;
    }

    /**
     * @return 根据线索id查询
     */
    public Map queryById(Integer leadsId) {
        return crmLeadsDao.queryById(leadsId);
        //return Db.findFirst(Db.getSql("crm.leads.queryById"), leadsId);
    }

    /**
     * 指定多个id查询
     *
     * @param ids
     * @return
     */
    public List<Map<String, Object>> queryByListId(List ids) {
        return crmLeadsDao.queryByListId(ids);
        //return Db.findFirst(Db.getSql("crm.leads.queryById"), leadsId);
    }

    /**
     * @return
     * @author wyq
     * 根据线索名称查询
     */
    public Map<String, Object> queryByName(String name) {
        return crmLeadsDao.queryByName(name);
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
        if (idsList.size() == 0) {
            R.error("leadsIds不能为空");
        }
        List<Map<String, Object>> batchIdList = crmLeadsDao.queryBatchIdByIds(Arrays.asList(idsArr));
        List batchIds = new ArrayList();
        batchIdList.forEach(s -> batchIds.add(s.get("batch_id")));
        //return Db.tx(() -> {
        //Db.batch(Db.getSql("crm.leads.deleteByIds"), "leads_id", idsList, 100);
        int i = crmLeadsDao.deleteByIds(idsList);
        if (batchIdList.size() > 0) {
            crmLeadsDao.executeUpdateSQL("delete from lkcrm_admin_fieldv where batch_id IN( " + SqlAppendUtil.sqlAppendWhereIn(batchIds) + " )");
        }
        return i > 0 ? R.ok() : R.error("线索删除失败");
        //}) ? R.ok() : R.error();
    }

    /**
     * 根据id 删除线索
     */
    public R deleteByBatchIds(List idsList) {
        if (idsList == null || idsList.size() == 0) {
            return R.error("superIds不能为空");
        }
        int i = crmLeadsDao.deleteByBatchIds(idsList);
        if (idsList.size() > 0) {
            crmLeadsDao.executeUpdateSQL("delete from lkcrm_admin_fieldv where batch_id IN( " + SqlAppendUtil.sqlAppendWhereIn(idsList) + " )");
        }
        return i > 0 ? R.ok() : R.error("线索删除失败");
    }

    public R deletePublicClue(List idsList, String seaId) {
        if (idsList == null || idsList.size() == 0) {
            return R.error("superIds不能为空");
        }
        int i = 0;
        if (idsList.size() > 0) {
            i = crmLeadsDao.executeUpdateSQL("delete from " + ConstantsUtil.SEA_TABLE_PREFIX + seaId + " where id IN( " + SqlAppendUtil.sqlAppendWhereIn(idsList) + " )");
            CustomerSeaProperty csp = customerSeaDao.getProperty(seaId, "defaultClueCgId");
            if (csp != null) {
                i = crmLeadsDao.executeUpdateSQL("delete from " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + csp.getPropertyValue() + " where id IN( " + SqlAppendUtil.sqlAppendWhereIn(idsList) + " )");
            }
        }
        return i > 0 ? R.ok() : R.error("公海线索删除失败");
    }

    public int batchClueBackToSea(Long userId, String userType, String seaId, List<String> superIds, String reason, String remark) {
        // 指定ID退回公海
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                //.append(" SET status = 1, pre_user_id = user_id, user_id = NULL, super_data = '{\"SYS007\":\"未跟进\"}' WHERE status = 0  AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
                .append(" SET status = 1, pre_user_id = user_id, user_id = NULL, super_data = JSON_SET(super_data, '$.SYS007', '未跟进') WHERE status = 0  AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, object_code, `create_time`,reason,remark ) ")
                .append(" SELECT ").append(userId).append(" ,id,").append(seaId).append(",batch_id,").append(7).append(", user_id ,'").append(new Timestamp(System.currentTimeMillis())).append("'").append(" ,? ,? ")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE status = 0  AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        //员工只能处理负责人为自己的数据
        List<Object> p = new ArrayList<>();
        p.add(reason);
        p.add(remark);
        List<Object> param = new ArrayList<>();
        if ("2".equals(userType)) {
            p.add(userId);
            param.add(userId);
            sql.append(" AND user_id = ? ");
            logSql.append(" AND user_id = ? ");
        }
        customerSeaDao.executeUpdateSQL(logSql.toString(), p.toArray());
        int status = customerSeaDao.executeUpdateSQL(sql.toString(), param.toArray());
        for (String id : superIds) {
            List<Map<String, Object>> list = customerSeaDao.sqlQuery("select * from " + ConstantsUtil.SEA_TABLE_PREFIX + seaId + " WHERE id = ? ", id);
            if (list.size() == 0) {
                transferToPublicSea(seaId, userId.toString(), id);
            }
        }
        // 指定ID退回公海时删除私海线索
        deleteByBatchIds(superIds);
        return status;
    }

    /**
     * 变更负责人
     */
    public R updateOwnerUserId(String leadsIds, Long ownerUserId) {
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
        String leadsview = BaseUtil.getViewSql("leadsview");
        LoginUser user = BaseUtil.getUser();
        for (String leadsId : leadsIdsArr) {
            List<Map<String, Object>> maps = crmLeadsDao.sqlQuery("select * from " + leadsview + " where leads_id = ?", Integer.valueOf(leadsId));
            Record crmLeads = JavaBeanUtil.mapToRecord(maps.get(0));
            if (1 == crmLeads.getInt("is_transform")) {
                return R.error("已转化线索不能再次转化");
            }
            String customerName = crmLeads.getStr("company");
            if (StringUtil.isEmpty(customerName)) {
                return R.error("请补全线索的公司名称");
            }
            List<Record> leadsFields = adminFieldService.list("1");
            LkCrmCustomerEntity crmCustomer = new LkCrmCustomerEntity();
            crmCustomer.setCustomerName(customerName);
            crmCustomer.setCustId(user.getCustId());
            crmCustomer.setIsLock(0);
            crmCustomer.setNextTime(crmLeads.getTimestamp("next_time"));
            crmCustomer.setMobile(crmLeads.getStr("mobile"));
            crmCustomer.setTelephone(crmLeads.getStr("telephone"));
            crmCustomer.setDealStatus("未成交");
            crmCustomer.setCreateUserId(user.getUserId());
            crmCustomer.setOwnerUserId(crmLeads.getLong("owner_user_id"));
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
            List<LkCrmAdminFieldEntity> customerFields = crmLeadsDao.queryListBySql("select field_id,name,field_name,field_type from lkcrm_admin_field where label = '2' AND cust_id = ? ", user.getCustId(), LkCrmAdminFieldEntity.class);
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
                            adminFieldv.setCustId(user.getCustId());
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
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户行业".equals(customerField.getName()) && "客户行业".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户级别".equals(customerField.getName()) && "客户级别".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldvList.add(adminFieldv);
                    }
                }
            }
            int customerId = (int) crmCustomerDao.saveReturnPk(crmCustomer);
            // 保存uid对应关系
            phoneService.saveObjU(String.valueOf(customerId), crmLeads.getStr("mobile"), 2, user.getCustId());
            phoneService.saveObjU(String.valueOf(customerId), crmLeads.getStr("telephone"), 2, user.getCustId());

            crmRecordService.addConversionCustomerRecord(crmCustomer.getCustomerId(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerName());
            adminFieldService.save(adminFieldvList, customerBatchId);
            crmLeadsDao.executeUpdateSQL("update lkcrm_crm_leads set is_transform = 1,update_time = ?,customer_id = ? where leads_id = ?",
                    DateUtil.date(), crmCustomer.getCustomerId(), Integer.valueOf(leadsId));
            List<LkCrmAdminRecordEntity> adminRecordList = crmAdminUserDao.queryListBySql("select * from lkcrm_admin_record where types = 'crm_leads' and types_id = ?", leadsId, LkCrmAdminRecordEntity.class);
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

    public R leadsTranslateCustomer(String leadsIds, LkCrmCustomerEntity crmCustomer) {
        String[] leadsIdsArr = leadsIds.split(",");
        String leadsview = BaseUtil.getViewSql("leadsview");
        for (String leadsId : leadsIdsArr) {
            List<Map<String, Object>> maps = crmLeadsDao.sqlQuery("select * from " + leadsview + " where leads_id = ?", Integer.valueOf(leadsId));
            Record crmLeads = JavaBeanUtil.mapToRecord(maps.get(0));
            if (1 == crmLeads.getInt("is_transform")) {
                return R.error("已转化线索不能再次转化");
            }
            List<Record> leadsFields = adminFieldService.list("1");
            LoginUser user = BaseUtil.getUser();
            List<LkCrmAdminFieldEntity> customerFields = crmLeadsDao.find("from LkCrmAdminFieldEntity where label = '2' AND custId = ? ", user.getCustId());
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
                            adminFieldv.setCustId(user.getCustId());
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
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户行业".equals(customerField.getName()) && "客户行业".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户级别".equals(customerField.getName()) && "客户级别".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldv.setCustId(user.getCustId());
                        adminFieldvList.add(adminFieldv);
                    }
                }
            }
            //crmRecordService.addConversionCustomerRecord(crmCustomer.getCustomerId(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerName());
            adminFieldService.save(adminFieldvList, crmCustomer.getBatchId());
            crmLeadsDao.executeUpdateSQL("update lkcrm_crm_leads set is_transform = 1,update_time = ?,customer_id = ? where leads_id = ?",
                    DateUtil.date().toTimestamp(), crmCustomer.getCustomerId(), leadsId);
            List<LkCrmAdminRecordEntity> adminRecordList = crmAdminUserDao.find(" from LkCrmAdminRecordEntity where types = 'crm_leads' and typesId = ?", leadsId);
            List<LkCrmAdminFileEntity> adminFileList = new ArrayList<>();
            if (adminRecordList.size() != 0) {
                adminRecordList.forEach(adminRecord -> {
                    List<LkCrmAdminFileEntity> leadsRecordFiles = crmLeadsDao.find(" from LkCrmAdminFileEntity where batchId = ?", adminRecord.getBatchId());
                    String customerRecordBatchId = IdUtil.simpleUUID();
                    leadsRecordFiles.forEach(adminFile -> {
                        adminFile.setBatchId(customerRecordBatchId);
                        adminFile.setFileId(null);
                    });
                    adminFileList.addAll(leadsRecordFiles);
                    adminRecord.setBatchId(customerRecordBatchId);
                    adminRecord.setRecordId(null);
                    adminRecord.setCustId(user.getCustId());
                    adminRecord.setTypes("crm_customer");
                    adminRecord.setTypesId(crmCustomer.getCustomerId().toString());
                    adminRecord.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                });
                //Db.batchSave(adminRecordList, 100);
                crmLeadsDao.getSession().clear();
                crmLeadsDao.batchSaveOrUpdate(adminRecordList);
            }
            List<LkCrmAdminFileEntity> fileList = crmLeadsDao.find(" from LkCrmAdminFileEntity where batchId = ?", crmLeads.getStr("batch_id"));
            if (fileList.size() != 0) {
                fileList.forEach(adminFile -> {
                    adminFile.setBatchId(crmCustomer.getBatchId());
                    adminFile.setFileId(null);
                });
            }
            adminFileList.addAll(fileList);
            //Db.batchSave(adminFileList, 100);
            crmLeadsDao.getSession().clear();
            crmLeadsDao.batchSaveOrUpdate(adminFileList);
        }
        return R.ok();
    }

    public List<Map<String, Object>> listLeadByCompany(String custId, String company, String notInLeadsIds) {
        String[] notIds = new String[0];
        if (StringUtil.isNotEmpty(notInLeadsIds)) {
            notIds = notInLeadsIds.split(",");
        }
        return crmLeadsDao.listLeadByCompany(custId, company, 0, notIds);
    }

    /**
     * 查询编辑字段
     */
    public List<Record> queryField(Integer leadsId) {
        String leadsview = BaseUtil.getViewSql("leadsview");
        Record leads = JavaBeanUtil.mapToRecord(crmAdminUserDao.sqlQuery("select * from " + leadsview + " where leads_id = ?", leadsId).get(0));
        return adminFieldService.queryUpdateField(1, leads);
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(LkCrmAdminRecordEntity adminRecord) {
        LoginUser user = BaseUtil.getUser();
        adminRecord.setCustId(user.getCustId());
        adminRecord.setCreateUserId(user.getUserId());
        adminRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        adminRecord.setTypes("crm_leads");
        // 添加日程
        if (adminRecord.getIsEvent() != null && 1 == adminRecord.getIsEvent()) {
            LkCrmOaEventEntity oaEvent = new LkCrmOaEventEntity();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setCreateUserId(adminRecord.getCreateUserId());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1).toTimestamp());
            oaEvent.setCreateTime(DateUtil.date().toTimestamp());
            crmOaEventDao.save(oaEvent);
        }
        // 最后跟进时间
        if (adminRecord.getNextTime() != null) {
            Date nextTime = adminRecord.getNextTime();
            LkCrmLeadsEntity leadsEntity = crmLeadsDao.get(NumberConvertUtil.parseInt(adminRecord.getTypesId()));
            if (leadsEntity != null) {
                leadsEntity.setLeadsId(NumberConvertUtil.parseInt(adminRecord.getTypesId()));
                leadsEntity.setNextTime(new Timestamp(nextTime.getTime()));
                crmLeadsDao.update(leadsEntity);
            }
        }
        // 添加任务
        if (adminRecord.getIsTask() != null && 1 == adminRecord.getIsTask()) {
            LkCrmTaskEntity crmTaskEntity = new LkCrmTaskEntity();
            crmTaskEntity.setCustId(user.getCustId());
            crmTaskEntity.setBatchId(IdUtil.simpleUUID());
            crmTaskEntity.setName(adminRecord.getTaskName());
            crmTaskEntity.setDescription(adminRecord.getContent());
            crmTaskEntity.setCreateUserId(adminRecord.getCreateUserId());
            crmTaskEntity.setMainUserId(adminRecord.getCreateUserId());
            crmTaskEntity.setStartTime(adminRecord.getNextTime());
            if (adminRecord.getNextTime() != null) {
                crmTaskEntity.setStopTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1).toTimestamp());
            }
            //完成状态 1正在进行2延期3归档 5结束
            crmTaskEntity.setStatus(1);
            crmTaskEntity.setCreateTime(DateUtil.date().toTimestamp());
            int taskId = (int) crmTaskDao.saveReturnPk(crmTaskEntity);
            adminRecord.setTaskId(taskId);
        }
        crmAdminRecordDao.executeUpdateSQL("update lkcrm_crm_leads set followup = 1 where leads_id = ?", adminRecord.getTypesId());
        return (int) crmAdminRecordDao.saveReturnPk(adminRecord) > 0 ? R.ok() : R.error();
    }

    /**
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmLeads> basePageRequest) {
        CrmLeads crmLeads = basePageRequest.getData();
        //List<Record> recordList = Db.find(Db.getSql("crm.leads.getRecord"), crmLeads.getLeadsId());
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmLeadsDao.getRecord(crmLeads.getLeadsId(), basePageRequest.getPage(), basePageRequest.getLimit()));
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
        });
        return recordList;
    }

    /**
     * 查看代办事项记录
     *
     * @param basePageRequest
     * @param taskStatus
     * @param leadsId
     * @return
     */
    public List<Record> listAgency(BasePageRequest<CrmLeads> basePageRequest, Integer taskStatus, Integer leadsId) {
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmLeadsDao.getRecord(String.valueOf(leadsId), taskStatus, basePageRequest.getPage(), basePageRequest.getLimit()));
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
        return JavaBeanUtil.mapToRecords(crmLeadsDao.excelExport(Arrays.asList(leadsIdsArr)));
    }

    public List<Record> exportPublicSeaClues(long seaId, String superIds) {
        String[] leadsIdsArr = superIds.split(",");
        return JavaBeanUtil.mapToRecords(crmLeadsDao.excelPublicSeaExport(seaId, Arrays.asList(leadsIdsArr)));
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
    @Deprecated
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

    public R uploadExcel(MultipartFile file, Integer repeatHandling, Long ownerUserId) {
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
                                .fluentPut("company", leadsList.get(kv.getInt("company")))
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
                                .fluentPut("company", leadsList.get(kv.getInt("company")))
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
                        if (columnsNum == null) {
                            continue;
                        }
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

    /**
     * 公海线索批量上传
     *
     * @param file
     * @param repeatHandling
     * @param ownerUserId
     * @return
     */
    public R uploadExcelPublicSea(MultipartFile file, Integer repeatHandling, Long ownerUserId, long seaId) {
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
                    String superName = leadsList.get(kv.getInt("leads_name")).toString();
                    Integer number = crmLeadsDao.queryForInt("select count(*) from t_customer_sea_list_" + seaId + " where super_name = ?", superName);
                    CustomSeaTouchInfoDTO dto = new CustomSeaTouchInfoDTO();
                    dto.setCustomerSeaId(String.valueOf(seaId));
                    dto.setUser_id(ownerUserId.toString());
                    if (0 == number) {
                        object.fluentPut("leads_name", superName)
                                .fluentPut("super_telphone", leadsList.get(kv.getInt("super_telphone")))
                                .fluentPut("super_phone", leadsList.get(kv.getInt("super_phone")))
                                .fluentPut("super_address_street", leadsList.get(kv.getInt("super_address_street")))
                                //.fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("user_id", ownerUserId)
                                .fluentPut("super_address_province_city", leadsList.get(kv.getInt("super_address_province_city")));
                        if (kv.getInt("qq") != null) {
                            object.fluentPut("qq", leadsList.get(kv.getInt("qq")));
                        }
                        if (kv.getInt("email") != null) {
                            object.fluentPut("email", leadsList.get(kv.getInt("email")));
                        }
                        if (kv.getInt("weChat") != null) {
                            object.fluentPut("weChat", leadsList.get(kv.getInt("weChat")));
                        }
                        if (kv.getInt("company") != null) {
                            object.fluentPut("company", leadsList.get(kv.getInt("company")));
                        }
                        if (kv.getInt("position") != null) {
                            object.fluentPut("position", leadsList.get(kv.getInt("position")));
                        }
                        if (kv.getInt("site") != null) {
                            object.fluentPut("site", leadsList.get(kv.getInt("site")));
                        }

                    } else if (number > 0 && repeatHandling == 1) {
                        Record leads = JavaBeanUtil.mapToRecord(crmLeadsDao.sqlQuery("select id from t_customer_sea_list_" + seaId + " where super_name = ?", superName).get(0));
                        object.fluentPut("leads_id", leads.getStr("id"))
                                .fluentPut("leads_name", superName)
                                .fluentPut("super_telphone", leadsList.get(kv.getInt("super_telphone")))
                                .fluentPut("super_phone", leadsList.get(kv.getInt("super_phone")))
                                .fluentPut("super_address_street", leadsList.get(kv.getInt("super_address_street")))
                                //.fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("user_id", ownerUserId)
                                .fluentPut("batch_id", leads.getStr("id"))
                                .fluentPut("super_address_province_city", leadsList.get(kv.getInt("super_address_province_city")));
                        if (kv.getInt("qq") != null) {
                            object.fluentPut("qq", leadsList.get(kv.getInt("qq")));
                        }
                        if (kv.getInt("email") != null) {
                            object.fluentPut("email", leadsList.get(kv.getInt("email")));
                        }
                        if (kv.getInt("weChat") != null) {
                            object.fluentPut("weChat", leadsList.get(kv.getInt("weChat")));
                        }
                        if (kv.getInt("company") != null) {
                            object.fluentPut("company", leadsList.get(kv.getInt("company")));
                        }
                        if (kv.getInt("position") != null) {
                            object.fluentPut("position", leadsList.get(kv.getInt("position")));
                        }
                        if (kv.getInt("site") != null) {
                            object.fluentPut("site", leadsList.get(kv.getInt("site")));
                        }
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        //Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("name") + "(*)");
                        Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("field_name"));
                        if (columnsNum == null) {
                            continue;
                        }
                        record.set("value", leadsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    //BeanUtil.mapToBean(object, dto, true);
                    dto.setSuper_name(object.getString("leads_name"));
                    dto.setSuper_telphone(object.getString("super_telphone"));
                    dto.setSuper_phone(object.getString("super_phone"));
                    dto.setCompany(object.getString("company"));
                    // 导入线索
                    if (0 == number) {
                        addClueData0(dto, object);
                    } else if (number > 0 && repeatHandling == 1) {
                        updateClueSignData(dto, object);
                    }
                    //addOrUpdate(object);
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

    /**
     * @author wyq
     * 客户锁定
     */
    public R lock(LkCrmLeadsEntity crmLeads, String ids) {
        String[] idArr = ids.split(",");
        crmRecordService.addIsLockRecord(idArr, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmLeads.getIsLock());
        return crmLeadsDao.lock(crmLeads.getIsLock(), Arrays.asList(ids)) > 0 ? R.ok() : R.error();
    }

    /**
     * 查询跟进记录类型
     */
    public R queryRecordOptions() {
        LoginUser user = BaseUtil.getUser();
        List<LkCrmAdminConfigEntity> list = crmActionRecordDao.find("from LkCrmAdminConfigEntity where name = ? AND custId = ? ", "clueFollowRecordOption", user.getCustId());
        if (list.size() == 0) {
            List<LkCrmAdminConfigEntity> adminConfigList = new ArrayList<>();
            // 初始化数据
            String[] defaults = new String[]{"未跟进", "跟进中", "有意向", "无意向"};
            for (String i : defaults) {
                LkCrmAdminConfigEntity adminConfig = new LkCrmAdminConfigEntity();
                adminConfig.setCustId(user.getCustId());
                adminConfig.setName("clueFollowRecordOption");
                adminConfig.setValue(i);
                adminConfig.setDescription("线索跟进记录选项");
                adminConfig.setIsSystem(1);
                adminConfigList.add(adminConfig);
            }
            crmActionRecordDao.batchSaveOrUpdate(adminConfigList);
            list.addAll(adminConfigList);
        }
        return R.ok().put("data", list);
    }


    public R setRecordOptions(List<String> list) {
        LoginUser user = BaseUtil.getUser();
        crmActionRecordDao.executeUpdateSQL("delete from lkcrm_admin_config where name = 'clueFollowRecordOption' AND cust_id = ? AND is_system <> 1  ", user.getCustId());
        List<LkCrmAdminConfigEntity> adminConfigList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            LkCrmAdminConfigEntity adminConfig = new LkCrmAdminConfigEntity();
            adminConfig.setCustId(user.getCustId());
            adminConfig.setName("clueFollowRecordOption");
            adminConfig.setValue(list.get(i));
            adminConfig.setDescription("线索跟进记录选项");
            adminConfig.setIsSystem(2);
            adminConfigList.add(adminConfig);
        }
        crmActionRecordDao.batchSaveOrUpdate(adminConfigList);
        return R.ok();
    }
}
