package com.bdaim.customgroup.service;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.common.CallUtil;
import com.bdaim.callcenter.common.PhoneAreaUtil;
import com.bdaim.callcenter.dto.VoiceLogCallDataDTO;
import com.bdaim.callcenter.dto.XzPullPhoneDTO;
import com.bdaim.common.dto.Page;
import com.bdaim.common.service.PhoneService;
import com.bdaim.common.spring.SpringContextHelper;
import com.bdaim.customer.account.dto.RemainSourceDTO;
import com.bdaim.customer.account.entity.TransactionDO;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerPropertyEnum;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.customer.entity.*;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customer.user.service.UserGroupService;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.dao.CustomerGroupListDao;
import com.bdaim.customgroup.dto.*;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.entity.CustomerGroupProperty;
import com.bdaim.image.service.impl.UploadDowloadImgServiceImpl;
import com.bdaim.industry.service.IndustryPoolService;
import com.bdaim.label.dao.IndustryPoolDao;
import com.bdaim.label.dao.LabelInfoDao;
import com.bdaim.label.entity.IndustryPool;
import com.bdaim.label.entity.LabelCategory;
import com.bdaim.label.entity.LabelInfo;
import com.bdaim.label.entity.SourceDO;
import com.bdaim.label.service.LabelCategoryService;
import com.bdaim.label.service.LabelInfoService;
import com.bdaim.label.service.LabelInterfaceService;
import com.bdaim.label.vo.LabelPriceSumVO;
import com.bdaim.marketproject.dao.MarketProjectDao;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.order.dao.OrderDao;
import com.bdaim.order.entity.OrderDO;
import com.bdaim.order.service.OrderService;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.supplier.dao.SupplierSettlementDao;
import com.bdaim.supplier.dto.SupplierDTO;
import com.bdaim.supplier.entity.SupplierSettlementDO;
import com.bdaim.util.*;
import com.bdaim.util.redis.RedisUtil;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Service("customGroupService")
@Transactional
public class CustomGroupService {
    private static Logger log = LoggerFactory.getLogger(CustomGroupService.class);

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final static DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static long marketDataExportTime = 0;

    private static long marketRecordLogExportTime = 0;

    private static long customerGroupDataExportTime = 0;

    public static final String PROVINCE_NAME = "河北,山西,台湾,辽宁,吉林,黑龙江,江苏,浙江,安徽,福建,江西,山东,河南,湖北,湖南,广东,甘肃,四川,贵州,海南,云南,青海,陕西,广西,西藏,宁夏,新疆,内蒙,澳门,香港";

    public static Set<String> PROVINCE_SET = new HashSet<String>();

    @Resource
    private CustomGroupDao customGroupDao;
    @Resource
    private LabelInfoService labelInfoService;
    @Resource
    private LabelInfoDao labelInfoDao;
    @Resource
    private LabelInterfaceService labelInterfaceService;
    @Resource
    private LabelCategoryService labelCategoryServiceImpl;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private OrderDao orderDao;
    @Resource
    private SupplierSettlementDao supplierSettlementDao;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private CustomerGroupListDao customerGroupListDao;
    @Resource
    OrderService orderService;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private UserGroupService userGroupService;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private IndustryPoolService industryPoolService;
    @Resource
    private IndustryPoolDao industryPoolDao;
    @Resource
    private UploadDowloadImgServiceImpl uploadDowloadImgService;
    @Resource
    private PhoneService phoneService;
    @Resource
    private MarketTaskDao marketTaskDao;
    @Resource
    private CustomerService customerService;
    @Resource
    private MarketProjectDao marketProjectDao;
    @Resource
    private CustomerLabelDao customerLabelDao;
    @Resource
    private RedisUtil redisUtil;


    /*@PostConstruct
    public void init() {
        jdbcTemplate.update("update customer_group set download_status=? where download_status=?",
                Constant.DOWNLOAD_NOTAPPLY, Constant.DOWNLOAD_APPLY);
    }*/


    public Page page(String customer_group_id, String cust_id, String user_id, Integer pageNum, Integer pageSize,
                     String id, String name, Integer status, String callType, String dateStart, String dateEnd,
                     String enterpriseName, String marketProjectId, String propertyName, String propertyValue) {
        StringBuffer hql = new StringBuffer("from CustomGroup m where 1=1");
        List values = new ArrayList();
        if (null != customer_group_id && !"".equals(customer_group_id)) {
            hql.append(" and m.id = ?");
            values.add(Integer.parseInt(customer_group_id));
        }

        if (null != name && !"".equals(name)) {
            hql.append(" and m.name like ?");
            values.add("%" + name + "%");
        }
        if (cust_id != null) {
            hql.append(" and m.custId=?");
            values.add(cust_id);
        }
        if (null != status && !"".equals(status.toString())) {
            hql.append(" and m.status= ?");
            values.add(status);
        }

        if (StringUtil.isNotEmpty(marketProjectId)) {
            hql.append(" and m.marketProjectId= ?");
            values.add(NumberConvertUtil.parseInt(marketProjectId));
        }

        if (StringUtil.isNotEmpty(dateStart)) {
            Date start = DateUtil.fmtStrToDate(dateStart, "yyyy/MM/dd HH:mm:ss");
            hql.append(" and m.createTime >=?");
            values.add(start);
        }

        if (StringUtil.isNotEmpty(dateEnd)) {
            Date end = DateUtil.fmtStrToDate(dateEnd, "yyyy/MM/dd HH:mm:ss");
            hql.append(" and m.createTime <=?");
            values.add(end);
        }

        if (StringUtil.isNotEmpty(enterpriseName)) {
            hql.append(" and m.custId IN (SELECT id FROM Customer WHERE enterpriseName LIKE ?)");
            values.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(propertyName) && StringUtil.isNotEmpty(propertyValue)) {
            hql.append(" and m.id IN (SELECT customerGroupId FROM CustomerGroupProperty WHERE propertyName = ? AND propertyValue = ? )");
            values.add(propertyName);
            values.add(propertyValue);
        }
        hql.append(" ORDER BY m.createTime desc ");
        Page page = customGroupDao.page(hql.toString(), values, pageNum, pageSize);
        if (page.getData() != null && page.getData().size() > 0) {
            CustomGroup customGroup;
            CustomGroupDTO customGroupDTO;
            Customer customer;
            List data = new ArrayList();
            MarketProject marketProject;
            List<CustomerGroupProperty> properties;
            for (int i = 0; i < page.getData().size(); i++) {
                customGroup = (CustomGroup) page.getData().get(i);
                customGroupDTO = new CustomGroupDTO(customGroup);
                if (customGroupDTO != null) {
                    if (customGroup.getCustId() != null && !"".equals(customGroup.getCustId())) {
                        customer = customerDao.get(customGroup.getCustId());
                        customGroupDTO.setEnterpriseName(customer == null ? "" : customer.getEnterpriseName());
                    } else {
                        customGroupDTO.setEnterpriseName("");
                    }
                    if (customGroupDTO.getMarketProjectId() != null) {
                        marketProject = marketProjectDao.selectMarketProject(customGroupDTO.getMarketProjectId());
                        if (marketProject != null) {
                            customGroupDTO.setMarketProjectName(marketProject.getName());
                        } else {
                            customGroupDTO.setMarketProjectName("");
                        }
                    } else {
                        customGroupDTO.setMarketProjectName("");
                    }
                }
                // 查询客群属性
                properties = customGroupDao.listProperty(customGroupDTO.getId());
                if (properties != null && properties.size() > 0) {
                    Map m = new HashMap();
                    for (CustomerGroupProperty p : properties) {
                        m.put(p.getPropertyName(), p.getPropertyValue());
                    }
                    customGroupDTO.setProperties(m);
                }
                data.add(customGroupDTO);
            }
            page.setData(data);
        }

        return page;
    }


    public Integer addCustomGroup(CustomGroup customGroup) {
        Date date = new Date();
        customGroup.setAvailably(Constant.AVAILABLY);
        customGroup.setCreateTime(date);
        customGroup.setUpdateTime(date);
        customGroup.setStatus(Constant.AUDITING);
        // 如果未预览人群结果，获取人群数量
        String groupCondition = customGroup.getGroupCondition();
        Map<String, Long> map = previewByGroupCondition(customGroup.getName(), customGroup.getCycle(), groupCondition);
        customGroup.setUserCount(map.get("count"));
        customGroup.setTotal(map.get("total"));
        // 去掉lids，在groupCondition中取得labelId和categoryId
        /*
         * String lids = customGroup.getLids(); if (null != lids &&
         * (!lids.isEmpty())) { JSONArray array = JSON.parseArray(lids);
         * List<LabelInfo> labels = new ArrayList<LabelInfo>(); for (int i = 0;
         * i < array.size(); i++) { LabelInfo label =
         * labelInfoService.getLabelById(array .getInteger(i)); if (null !=
         * label) labels.add(label); } customGroup.setLabels(labels); }
         */
        Integer id = (Integer) customGroupDao.saveReturnPk(customGroup);
        JSONArray arr = JSONArray.parseArray(groupCondition);
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = arr.getJSONObject(i);
            int type = json.getIntValue("type");
            Integer labelId = null;
            Integer categoryId = null;
            try {
                if (type == 0) {
                    JSONArray leafs = json.getJSONArray("leafs");
                    if (json.containsKey("labelId")) {
                        labelId = json.getInteger("labelId");
                    }
                    if (null == leafs || leafs.size() < 1) {
                        if (json.containsKey("categoryId")) {
                            categoryId = json.getInteger("categoryId");
                            jdbcTemplate.update(
                                    "insert into customer_label_category(group_id,label_id,category_id)values(?,?,?)",
                                    id, labelId, categoryId);
                        }
                    } else {
                        for (int j = 0; j < leafs.size(); j++) {
                            JSONObject leaf = leafs.getJSONObject(j);
                            if (leaf.containsKey("id")) {
                                jdbcTemplate.update(
                                        "insert into customer_label_category(group_id,label_id,category_id)values(?,?,?)",
                                        id, labelId, leaf.getInteger("id"));
                            }
                        }
                    }

                } else if (type == 1) {
                    JSONArray leafs = json.getJSONArray("leafs");
                    for (int j = 0; j < leafs.size(); j++) {
                        JSONObject leaf = leafs.getJSONObject(j);
                        if (leaf.containsKey("id")) {
                            jdbcTemplate.update("insert into customer_label_category(group_id,label_id)values(?,?)", id,
                                    leaf.getInteger("id"));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public void updateCustomGroup(CustomGroup customGroup) {
        customGroupDao.update(customGroup);
    }

    public CustomGroup getCustomGroupById(Integer id) {
        return customGroupDao.get(id);
    }

    public CustomGroup findUniqueById(Integer id) {
        return customGroupDao.findUniqueBy("id", id);
    }

    public List<CustomGroup> getCustomerGroupByProjectId(String custId, String projectIdStr) {
        if (StringUtil.isNotEmpty(projectIdStr)) {
            String hql = "select id,name,market_project_id,status from customer_group t where t.cust_id= '" + custId + "' and market_project_id in (" + projectIdStr + ")";
            RowMapper<CustomGroup> rowMapper = new BeanPropertyRowMapper<>(CustomGroup.class);
            List<CustomGroup> list = jdbcTemplate.query(hql, rowMapper);
            return list;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<CustomGroup> getListByCondition(Map<String, Object> map, Map<String, Object> likeMap, Page page) {
        String hql = "From CustomGroup t where t.availably =1 ";
        Date sTime = null;
        Date eTime = null;
        if (map.containsKey(Constant.FILTER_KEY_PREFIX + "dayType")) {
            Calendar cale = Calendar.getInstance();
            String endTime = CalendarUtil.getDefaultDateString(cale.getTime());
            String startTime = CalendarUtil.getDefaultDateString(
                    CalendarUtil.getDateByBeforeDays(cale, (Integer) map.get(Constant.FILTER_KEY_PREFIX + "dayType")));
            // hql += "and t.createTime between '" + startTime + "' and '"
            // + endTime + "'";
            hql += " and t.createTime between ? and ? ";
            sTime = DateUtil.fmtStrToDate(startTime, "yyyy-MM-dd HH:mm:ss");
            eTime = DateUtil.fmtStrToDate(endTime, "yyyy-MM-dd HH:mm:ss");
        }
        Query query = customGroupDao.getHqlQuery(hql, map, likeMap, "id");
        if (map.containsKey(Constant.FILTER_KEY_PREFIX + "dayType")) {
            query.setTimestamp(0, sTime);
            query.setTimestamp(1, eTime);
        }
        if (null != page) {
            query.setFirstResult(page.getStart()).setMaxResults(page.getLimit());

        }
        List rett = query.list();
        return rett;
    }

    public Integer getCountByCondition(Map<String, Object> map, Map<String, Object> likeMap, Page page) {
        String hql = "select count(id) From CustomGroup t where t.availably =1 ";
        Date sTime = null;
        Date eTime = null;
        if (map.containsKey(Constant.FILTER_KEY_PREFIX + "dayType")) {
            Calendar cale = Calendar.getInstance();
            String endTime = CalendarUtil.getDefaultDateString(cale.getTime());
            String startTime = CalendarUtil.getDefaultDateString(
                    CalendarUtil.getDateByBeforeDays(cale, (Integer) map.get(Constant.FILTER_KEY_PREFIX + "dayType")));
            // hql += "and t.createTime between '" + startTime + "' and '"
            // + endTime + "'";
            hql += " and t.createTime between ? and ? ";
            sTime = DateUtil.fmtStrToDate(startTime, "yyyy-MM-dd HH:mm:ss");
            eTime = DateUtil.fmtStrToDate(endTime, "yyyy-MM-dd HH:mm:ss");
        }
        Query query = customGroupDao.getHqlQuery(hql, map, likeMap, null);
        if (map.containsKey(Constant.FILTER_KEY_PREFIX + "dayType")) {
            query.setTimestamp(0, sTime);
            query.setTimestamp(1, eTime);
        }
        Object count = query.list().get(0);
        return count == null ? 0 : Integer.parseInt(count.toString());
    }

    public Map<String, Long> previewByGroupCondition(String groupName, Integer cycle, String groupCondition) {
        Map<String, Long> map = new HashMap<String, Long>();

        net.sf.json.JSONObject params = ESUtil.format(groupCondition);
        String r = RestUtil.postDataWithParms(params, ESUtil.getUrl() + "_count");
        log.info("通过标签预览人数返回数据:" + r);
        net.sf.json.JSONObject d = net.sf.json.JSONObject.fromObject(r);

        map.put("count", d.getLong("count"));
        map.put("total", 300000000L);
        return map;
    }

    /**
     * 根据客群ID和自建属性值预览满足条件的人数
     *
     * @param customGroupId
     * @param groupCondition
     * @return
     */
    public Map<String, Object> previewCustomGroupInfo(int customGroupId, String groupCondition) {
        Map<String, Object> map = new HashMap<>();
        map.put("count", previewCustomGroupCount(customGroupId, groupCondition));
        // 查询触达方式
        String[] touchModes = new String[]{};
        CustomerGroupProperty cgp = customGroupDao.getProperty(customGroupId, "touchMode");
        if (cgp != null && StringUtil.isNotEmpty(cgp.getPropertyValue())) {
            touchModes = cgp.getPropertyValue().split(",");
        }
        map.put("touchModes", touchModes);
        long total = countCustomGroupPhoneList(String.valueOf(customGroupId));
        map.put("total", total);
        return map;
    }

    public long previewCustomGroupCount(int customGroupId, String groupCondition) {
        log.info("根据自建属性值查询满足条件人数,客群ID:" + customGroupId + ",条件:" + groupCondition);
        StringBuffer sb = new StringBuffer();
        sb.append(" select COUNT(id) count ")
                .append(" from t_customer_group_list_" + customGroupId)
                .append(" where 1=1 ");
        long userCount = 0L;
        List<Map<String, Object>> list = null;
        if (StringUtil.isNotEmpty(groupCondition)) {
            JSONArray condition = JSON.parseArray(groupCondition);
            JSONArray leafs;
            JSONObject jsonObject;
            String labelId;
            String labelDataLikeValue = "%\"{0}\":%{1}%";
            for (int i = 0; i < condition.size(); i++) {
                jsonObject = condition.getJSONObject(i);
                leafs = jsonObject.getJSONArray("leafs");
                if (leafs == null || leafs.size() == 0) {
                    continue;
                }
                labelId = jsonObject.getString("labelId");
                if (jsonObject.getIntValue("type") == 1) {
                    //　呼叫次数
                    if (ConstantsUtil.CALL_COUNT_ID.equals(labelId)) {
                        sb.append(" AND ( ");
                        for (int j = 0; j < leafs.size(); j++) {
                            if (j > 0) {
                                sb.append(" OR ");
                            }
                            if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                sb.append(" call_count >= " + leafs.getJSONObject(j).getString("value").split("")[0]);
                            } else {
                                sb.append(" call_count = " + leafs.getJSONObject(j).getString("value"));
                            }
                        }
                        sb.append(" ) ");
                    }
                    if (ConstantsUtil.CALL_SUCCESS_COUNT_ID.equals(labelId)) {
                        // 接通次数
                        sb.append(" AND ( ");
                        for (int j = 0; j < leafs.size(); j++) {
                            if (j > 0) {
                                sb.append(" OR ");
                            }
                            if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                sb.append(" call_success_count >=" + leafs.getJSONObject(j).getString("value").split("")[0]);
                            } else {
                                sb.append(" call_success_count = " + leafs.getJSONObject(j).getString("value"));
                            }
                        }
                        sb.append(" ) ");
                    } else if (ConstantsUtil.SMS_COUNT_ID.equals(labelId)) {
                        // 短信次数
                        sb.append(" AND ( ");
                        for (int j = 0; j < leafs.size(); j++) {
                            if (j > 0) {
                                sb.append(" OR ");
                            }
                            if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                sb.append(" sms_success_count >=" + leafs.getJSONObject(j).getString("value").split("")[0]);
                            } else {
                                sb.append(" sms_success_count = " + leafs.getJSONObject(j).getString("value"));
                            }
                        }
                        sb.append(" ) ");
                    }
                }
                if (jsonObject.getIntValue("type") == 2) {
                    sb.append(" AND ( ");
                    for (int j = 0; j < leafs.size(); j++) {
                        //　自建属性
                        if (j > 0) {
                            sb.append(" OR ");
                        }
                        sb.append(" super_data LIKE '" + MessageFormat.format(labelDataLikeValue, labelId, leafs.getJSONObject(j).getString("value")) + "'");
                    }
                    sb.append(" ) ");
                }
            }
        }
        try {
            list = this.customGroupDao.sqlQuery(sb.toString());
        } catch (Exception e) {
            log.error("根据自建属性值查询满足条件人数异常,", e);
        }
        if (list != null && list.size() > 0) {
            userCount = NumberConvertUtil.parseLong(list.get(0).get("count"));
        }
        return userCount;
    }


    public JSONObject previewByGroupCondition2(String groupCondition) {
        JSONObject map = new JSONObject();
        log.info("previewByGroupCondition2: " + groupCondition);
        net.sf.json.JSONObject params = null;
        try {
            params = ESUtil.formatForInterface(groupCondition);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("errorDesc", "02");
            return map;
        }
        try {
            String r = RestUtil.postDataWithParms(params, ESUtil.getUrl() + "_count");
            log.info("通过标签查询客户总数:[" + r + "]");
            if (StringUtil.isNotEmpty(r)) {
                JSONObject d = JSON.parseObject(r);
                map.put("userCount", d.getLong("count"));
            } else {
                map.put("userCount", 0);
            }
            map.put("errorDesc", "00");
        } catch (Exception e) {
            log.error("通过标签查询客户总数异常", e);
            map.put("errorDesc", "05");
            return map;
        }
        return map;
    }

    /**
     * 查询客户列表
     *
     * @param groupCondition
     * @return
     */
    public JSONObject searchDetailByGroupCondition(String groupCondition) {
        JSONObject map = new JSONObject();
        log.info("searchDetailByGroupCondition: " + groupCondition);
        net.sf.json.JSONObject params = null;
        try {
            params = ESUtil.formatForInterface(groupCondition);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("errorDesc", "02");
            return map;
        }
        try {
            String r = RestUtil.postDataWithParms(params, ESUtil.getUrl() + "_search");
            log.info("通过标签查询客户列表:" + r);
            JSONObject d = JSON.parseObject(r);
            List<String> data = parseEsResult(d);
            map.put("list", data);
            map.put("errorDesc", "00");
        } catch (Exception e) {
            log.error("通过标签查询客户总数异常", e);
            map.put("errorDesc", "05");
            return map;
        }
        return map;
    }

    private List<String> parseEsResult(JSONObject d) {
        List<String> geoList = new ArrayList<>();
        JSONArray array = d.getJSONObject("hits").getJSONArray("hits");
        if (array.isEmpty()) {
            return geoList;
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            JSONObject source = json.getJSONObject("_source");
            String geo = source.getString("geo");
            if (StringUtil.isNotEmpty(geo)) {
                if (geo.startsWith("[")) {
                    JSONArray geoarr = JSON.parseArray(geo);
                    if (geoarr.size() > 0) {
                        geo = geoarr.getString(0);
                    }
                }
                String geos[] = geo.split(",");
                String newgeo = geos[1] + "|" + geos[0];
                geoList.add(newgeo);
            }
        }
        return geoList;
    }


    /**
     * 获取标签后台需要的格式
     *
     * @param groupCondition
     * @return
     */
    public JSONArray getCustomerGroupTerms(String groupCondition) {
        JSONArray _arr = new JSONArray();
        JSONArray arr = JSONArray.parseArray(groupCondition);
        int symbol = -1;
        for (int i = 0; i < arr.size(); i++) {
            JSONObject _json = new JSONObject();
            JSONObject json = arr.getJSONObject(i);
            String labelId = json.getString("labelId");
            _json.put("labelID", labelId);
            if (json.containsKey("categoryId"))
                _json.put("categoryId", json.getString("categoryId"));
            if (json.containsKey("startTime")) {
                String startTime = json.getString("startTime");
                if (startTime.matches("\\d+$"))
                    startTime = CalendarUtil.getDateString(new Date(json.getLongValue("startTime") * 1000),
                            CalendarUtil.SHORT_DATE_FORMAT);
                _json.put("startTime", startTime);
            }
            if (json.containsKey("endTime")) {
                String endTime = json.getString("endTime");
                if (endTime.matches("\\d+$"))
                    endTime = CalendarUtil.getDateString(new Date(json.getLongValue("endTime") * 1000),
                            CalendarUtil.SHORT_DATE_FORMAT);
                _json.put("endTime", endTime);
            }
            _json.put("type", json.getString("type"));
            // 暂定，后面迁移到constants
            if (symbol != -1)
                _json.put("symbol", symbol);
            if (json.containsKey("symbol"))
                symbol = json.getIntValue("symbol");
            JSONArray leafs = json.getJSONArray("leafs");
            JSONArray vals = new JSONArray();
            for (int j = 0; j < leafs.size(); j++) {
                JSONObject leaf = leafs.getJSONObject(j);
                vals.add(leaf.getString("name"));
            }
            _json.put("values", vals);
            _arr.add(_json);
        }
        return _arr;
    }

    public Map<String, Object> getUserGroupGid(Integer groupId, Integer begin, Integer limit, String searchType,
                                               String searchValue) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONArray result = new JSONArray();
        CustomGroup group = getCustomGroupById(groupId);
        // 查询不到对应人群
        if (group == null)
            return map;
        if ("phone".equals(searchType))
            searchType = "tel";
        JSONObject json = JSONObject
                .parseObject(labelInterfaceService.getUserGroupGid(group, begin, limit, searchType, searchValue));
        if (json.getInteger("isSuccess") == 1) {
            JSONArray customerInfoArr = json.getJSONArray("data");
            for (int i = 0; i < customerInfoArr.size(); i++) {
                JSONObject customerInfo = new JSONObject();
                JSONObject customerInfoJson = customerInfoArr.getJSONObject(i);
                String superid = customerInfoJson.getString("global:superid");
                StringBuffer gid = new StringBuffer();
                StringBuffer qq = new StringBuffer();
                StringBuffer phone = new StringBuffer();
                StringBuffer email = new StringBuffer();
                if (customerInfoJson.containsKey("global:gid")) {
                    JSONArray gidArr = customerInfoJson.getJSONArray("global:gid");
                    for (int idx = 0; idx < gidArr.size(); idx++) {
                        gid.append(gidArr.getString(idx));
                        if (idx < gidArr.size() - 1)
                            gid.append(",");
                    }
                }
                if (customerInfoJson.containsKey("global:qq")) {
                    JSONArray qqArr = customerInfoJson.getJSONArray("global:qq");
                    for (int idx = 0; idx < qqArr.size(); idx++) {
                        qq.append(qqArr.getString(idx));
                        if (idx < qqArr.size() - 1)
                            qq.append(",");
                    }
                }
                if (customerInfoJson.containsKey("global:cp")) {
                    JSONArray phoneArr = customerInfoJson.getJSONArray("global:cp");
                    for (int idx = 0; idx < phoneArr.size(); idx++) {
                        phone.append(phoneArr.getString(idx));
                        if (idx < phoneArr.size() - 1)
                            phone.append(",");
                    }
                }
                if (customerInfoJson.containsKey("global:em")) {
                    JSONArray emailArr = customerInfoJson.getJSONArray("global:em");
                    for (int idx = 0; idx < emailArr.size(); idx++) {
                        email.append(emailArr.getString(idx));
                        if (idx < emailArr.size() - 1)
                            email.append(",");
                    }
                }
                customerInfo.put("superid", superid);
                customerInfo.put("gid", gid.toString());
                customerInfo.put("qq", qq.toString());
                customerInfo.put("phone", phone.toString());
                customerInfo.put("email", email.toString());
                result.add(customerInfo);
            }
        }
        map.put("stores", result);
        map.put("total", json.getLong("total"));
        return map;
    }


    public Map<String, Object> getCharacteristic(CustomGroup group) {
        Map<String, Object> result = new HashMap<String, Object>();
        String str = labelInterfaceService.getCharacteristic(group);
        JSONObject json = JSONObject.parseObject(str);
        if (json.getIntValue("isSuccess") == 0) {
            log.error("特征发现报告预览失败！" + json.getString("_message"));
            throw new RuntimeException("特征发现报告预览失败！");
        }
        result.put("total", json.getLong("total"));
        JSONArray tmp = new JSONArray();
        // 获取具体特征发现
        JSONArray _tmp = json.getJSONArray("data");
        for (int i = 0; i < _tmp.size(); i++) {
            JSONObject _json = _tmp.getJSONObject(i);
            String labelID = _json.getString("labelID");
            LabelInfo label = labelInfoService.getLabelInfoByLabelId(labelID);
            String path = label.getPath();
            String labelName = label.getLabelName();
            if (_json.containsKey("categoryID")) {
                LabelCategory category = labelCategoryServiceImpl
                        .getCategoryByCategoryId(_json.getString("categoryID"));
                path += labelName;
                path += category.getPath();
                labelName = category.getName();
                _json.put("value",
                        labelCategoryServiceImpl.getCategoryByCategoryId(_json.getString("value")).getName());
            }
            _json.put("path", path);
            _json.put("labelName", labelName);
            tmp.add(_json);
        }
        result.put("stores", tmp);
        return result;
    }

    /**
     * download_status 0、未申请；1、已申请；2、下载完成；3、任务下载失败
     */
    public int applyDownloadUserProfileByGroup(Integer groupId) {
        final CustomGroup group = getCustomGroupById(groupId);
        if (group == null)
            return Constant.DOWNLOAD_FAILED;
        Integer downloadStatus = group.getDownloadStatus();
        if (downloadStatus == null || downloadStatus == Constant.DOWNLOAD_NOTAPPLY
                || downloadStatus == Constant.DOWNLOAD_FAILED || downloadStatus == Constant.DOWNLOAD_APPLYFAILED) {
            // 更新下载状态为申请中
            group.setDownloadStatus(Constant.DOWNLOAD_APPLY);
            updateCustomGroup(group);
            // 异步请求下载接口
            try {
                String result = ((LabelInterfaceService) SpringContextHelper.getBean("labelInterfaceService"))
                        .downloadByCustomerGroup(group);
                JSONObject json = JSONObject.parseObject(result);
                if (json.getInteger("isSuccess") == 1) {
                }
            } catch (Exception e) {
                // 数据下载服务异常
                group.setDownloadStatus(Constant.DOWNLOAD_APPLYFAILED);
                ((CustomGroupService) SpringContextHelper.getBean("customGroupService")).updateCustomGroup(group);
                log.error("申请导出失败！", e);
            }
            // new Thread(new Runnable() {
            //
            //
            // public void run() {
            // try {
            // String result = ((LabelInterfaceService) SpringContextHelper
            // .getBean("labelInterfaceService")).downloadByCustomerGroup(group);
            // JSONObject json = JSONObject.parseObject(result);
            // if (json.getInteger("isSuccess") == 1) {
            // group.setDownloadStatus(Constant.DOWNLOAD_FINISH);
            // group.setFilePath(json.getString("filePath"));
            // ((CustomGroupService)
            // SpringContextHelper.getBean("customGroupService"))
            // .updateCustomGroup(group);
            // } else {
            // group.setDownloadStatus(Constant.DOWNLOAD_FAILED);
            // ((CustomGroupService)
            // SpringContextHelper.getBean("customGroupService"))
            // .updateCustomGroup(group);
            // }
            // } catch (Exception e) {
            // // 数据下载服务异常
            // group.setDownloadStatus(Constant.DOWNLOAD_APPLYFAILED);
            // ((CustomGroupService)
            // SpringContextHelper.getBean("customGroupService"))
            // .updateCustomGroup(group);
            // log.error("申请导出失败！", e);
            // }
            // }
            // }).start();
        }
        return group.getDownloadStatus();
    }


    public List<RemainSourceDTO> getRemainSourceByGroupConditionV1(CustomGroup group, Integer industryPoolId) {
        Map<String, Object> mapCondition = new HashMap<String, Object>();
        List<RemainSourceDTO> resultList = new ArrayList<RemainSourceDTO>();
        String hql = "select remark From CustomGroup t where t.availably =1 ";
        mapCondition.put("createUserId", group.getCreateUserId());
        mapCondition.put("groupCondition", "'" + group.getGroupCondition() + "'");
        Query query = customGroupDao.getHqlQuery(hql, mapCondition, new HashMap(), null);
        // 如果用户存在相同条件的用户群，直接用remark中的数据计算
        if (query.list().size() > 0 && query.list().get(0) != null) {
            String remark = (String) query.list().get(0);
            JSONArray jsonArr = JSONArray.parseArray(remark);
            RemainSourceDTO remain;
            LabelPriceSumVO sumPrice;
            for (int i = 0; i < jsonArr.size(); i++) {
                JSONObject json = (JSONObject) jsonArr.get(i);
                remain = new RemainSourceDTO();
                remain.setCount(json.getLong("count"));
                remain.setRemain(json.getLong("remain"));
                remain.setTotal(json.getLong("total"));
                remain.setSourceId(json.getInteger("sourceId"));
                remain.setSourceName(json.getString("sourceName"));
                if (group.getCycle() == null) {
                    sumPrice = this.getCustomGroupPrice(group, remain.getSourceId(), industryPoolId);
                    remain.setSalePrice(sumPrice.getSalePrice());
                }
                resultList.add(remain);
            }

        } else {
            // 未匹配到则调api
            List<SourceDO> sourceList = sourceDao.createQuery("from SourceDO WHERE status = ? ", 1).list();
            Map<String, Long> resultMap = new HashMap<>();
            RemainSourceDTO remain;
            LabelPriceSumVO sumPrice;
            for (SourceDO source : sourceList) {
                resultMap = previewByGroupCondition(group.getName(), group.getCycle(), group.getGroupCondition());
                remain = new RemainSourceDTO();
                remain.setCount(resultMap.get("count"));
                remain.setRemain(resultMap.get("count"));
                remain.setTotal(resultMap.get("total"));
                remain.setSourceId(source.getSourceId());
                remain.setSourceName(source.getSourceName());
                if (group.getCycle() == null) {
                    sumPrice = this.getCustomGroupPrice(group, remain.getSourceId(), industryPoolId);
                    remain.setSalePrice(sumPrice.getSalePrice());
                }
                resultList.add(remain);
            }
        }
        return resultList;
    }


    public Map<String, Object> addCustomGroupV1(CustomerGroupAddDTO customGroupDTO) throws Exception {

        // 0可购买量查询
        CustomGroup customGroup = new CustomGroup();
        customGroup.setGroupCondition(customGroupDTO.getGroupCondition());
        customGroup.setName(customGroupDTO.getName());
        customGroup.setCycle(0);
        customGroup.setCreateUserId(customGroupDTO.getCreateUserId());
        // 默认任务类型为0
        customGroup.setTaskType(0);

        // 1查询标签成本价、销售价、订单金额、订单成本价
        String groupCondition = customGroupDTO.getGroupCondition();
        JSONArray arr = JSONArray.parseArray(groupCondition);
        int labelAmount = industryPoolService.getSalePriceV1(groupCondition, customGroupDTO.getIndustryPoolId(), customGroupDTO.getCustId());
        int labelCostAmount = industryPoolService.getCostPrice0(groupCondition, customGroupDTO.getIndustryPoolId(), customGroupDTO.getQuantity());
        SupplierDTO supplierDTO = industryPoolDao.getSupplierInfo(customGroupDTO.getIndustryPoolId());

        List<QuantityDetailDTO> quantityDetails = customGroupDTO.getQuantityDetail();

        for (QuantityDetailDTO quantityDetail : quantityDetails) {
            quantityDetail.setAmount(labelAmount);// 金额
        }

        // 2生成订单
        OrderDO order = new OrderDO();
        Date date = new Date();
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        order.setOrderId(String.valueOf(IDHelper.getTransactionId()));// 产生订单号
        order.setCustId(customGroupDTO.getCustId());
        order.setOrderType(1);// 客户群
        order.setOrderState(1);// 未付款
        order.setCreateTime(date);
        order.setProductName("");

        String supplierId = null, supplierName = null;
        //supplier_id
        if (supplierDTO != null) {
            supplierId = String.valueOf(supplierDTO.getSupplierId());
            supplierName = String.valueOf(supplierDTO.getName());
        }
        order.setSupplierId(supplierId);
        order.setRemarks("客户群创建");
        order.setProductName("客户群");
        order.setEnterpriseName(customGroupDTO.getEnterpriseName());
        order.setQuantity(customGroupDTO.getQuantity());
        order.setAmount(labelAmount * customGroupDTO.getQuantity());
        order.setCostPrice(labelCostAmount * customGroupDTO.getQuantity());
        String orderId = (String) orderDao.saveReturnPk(order);
        // 3插入supplier_settlement
        JSONArray labelList;
        SupplierSettlementDO supplierSettlementDO;
        for (int i = 0; i < arr.size(); i++) {
            labelList = arr.getJSONObject(i).getJSONArray("leafs");
            for (int j = 0; j < labelList.size(); j++) {
                supplierSettlementDO = new SupplierSettlementDO();
                supplierSettlementDO.setOrderId(orderId);
                supplierSettlementDO.setSourceId(supplierId != null ? NumberConvertUtil.parseInt(supplierId) : 0);
                supplierSettlementDO.setSourceName(supplierName);
                supplierSettlementDO.setCostPrice(labelCostAmount);
                supplierSettlementDO.setSalePrice(labelAmount);
                supplierSettlementDO.setQuantity(customGroupDTO.getQuantity());
                supplierSettlementDO.setLabelId(labelList.getJSONObject(j).getString("id"));
                supplierSettlementDO.setLabelName(labelList.getJSONObject(j).getString("name"));
                supplierSettlementDO.setCreateTime(date);
                supplierSettlementDao.save(supplierSettlementDO);
            }

        }
        // 4保存用户群
        customGroup.setGroupSource(JSON.toJSONString(quantityDetails));
        customGroup.setPurpose(customGroupDTO.getPurpose());
        customGroup.setTotal(customGroupDTO.getTotal().longValue());
        customGroup.setUserCount(customGroupDTO.getQuantity().longValue());
        customGroup.setQuantity(0);
        customGroup.setIndustryPoolId(customGroupDTO.getIndustryPoolId());
        customGroup.setIndustryPoolName(customGroupDTO.getIndustryPoolName());
        customGroup.setCustId(customGroupDTO.getCustId());
        customGroup.setAmount(order.getAmount());
        customGroup.setOrderId(orderId);
        customGroup.setUpdateCycle(0);
        customGroup.setUpdateUserId(customGroupDTO.getUpdateUserId());
        customGroup.setAvailably(Constant.UNAVAILABLY);
        customGroup.setCreateTime(date);
        customGroup.setUpdateTime(date);
        customGroup.setStatus(2);// 待确认
        customGroup.setDataSource(0);
        // 处理所属项目ID
        if (StringUtil.isNotEmpty(customGroupDTO.getMarketProjectId())) {
            customGroup.setMarketProjectId(NumberConvertUtil.parseInt(customGroupDTO.getMarketProjectId()));
        }

        /*String remainsStr = JSON.toJSONString(remains);
        customGroup.setRemark(remainsStr);*/
        Integer id = (Integer) customGroupDao.saveReturnPk(customGroup);
        // 保存客户群触达方式
        if (StringUtil.isNotEmpty(customGroupDTO.getTouchMode())) {
            CustomerGroupProperty cgp = new CustomerGroupProperty(id, "touchMode", customGroupDTO.getTouchMode(), new Timestamp(System.currentTimeMillis()));
            customGroupDao.saveOrUpdate(cgp);
        }
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = arr.getJSONObject(i);
            int type = json.getIntValue("type");
            Integer labelId = null;
            Integer categoryId = null;
            try {
                if (type == 0) {
                    JSONArray leafs = json.getJSONArray("leafs");
                    if (json.containsKey("labelId")) {
                        labelId = json.getInteger("labelId");
                    }
                    if (null == leafs || leafs.size() < 1) {
                        if (json.containsKey("categoryId")) {
                            categoryId = json.getInteger("categoryId");
                            jdbcTemplate.update(
                                    "insert into customer_label_category(group_id,label_id,category_id)values(?,?,?)",
                                    id, labelId, categoryId);
                        }
                    } else {
                        for (int j = 0; j < leafs.size(); j++) {
                            JSONObject leaf = leafs.getJSONObject(j);
                            if (leaf.containsKey("id")) {
                                jdbcTemplate.update(
                                        "insert into customer_label_category(group_id,label_id,category_id)values(?,?,?)",
                                        id, labelId, leaf.getInteger("id"));
                            }
                        }
                    }

                } else if (type == 1) {
                    JSONArray leafs = json.getJSONArray("leafs");
                    for (int j = 0; j < leafs.size(); j++) {
                        JSONObject leaf = leafs.getJSONObject(j);
                        if (leaf.containsKey("id")) {
                            jdbcTemplate.update("insert into customer_label_category(group_id,label_id)values(?,?)", id,
                                    leaf.getInteger("id"));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("创建客户群失败", e);
                e.printStackTrace();
            }
        }
        Map<String, Object> resultMap = orderService.queryCustomerOrdDetail(customGroupDTO.getCustId(), orderId);
        resultMap.put("orderId", orderId);
        return resultMap;
    }

    /**
     * 创建客群,数据状态为处理中,支付状态为已支付
     *
     * @param customGroupDTO
     * @return
     */
    public int saveCustomGroup(CustomerGroupAddDTO customGroupDTO) {
        String orderId = String.valueOf(IDHelper.getTransactionId());
        StringBuffer insertOrder = new StringBuffer();
        insertOrder.append("INSERT INTO  t_order (`order_id`, `cust_id`, `order_type`, `create_time`,  `remarks`, `amount`, `order_state`, `cost_price`) ");
        insertOrder.append(" VALUES ('" + orderId + "','" + customGroupDTO.getCustId() + "','1','" + new Timestamp(System.currentTimeMillis()) + "','导入客户群创建','0','2','0')");
        int status = customGroupDao.executeUpdateSQL(insertOrder.toString());
        LogUtil.info("导入客户群创建订单表状态:" + status);
        if (status == 0) {
            return 0;
        }
        CustomGroup cg = new CustomGroup();
        cg.setName(customGroupDTO.getName());
        cg.setDesc("导入客户群创建");
        cg.setOrderId(orderId);
        cg.setMarketProjectId(NumberConvertUtil.parseInt(customGroupDTO.getMarketProjectId()));
        cg.setStatus(3);
        cg.setDataSource(customGroupDTO.getDataSource());
        cg.setCustId(customGroupDTO.getCustId());
        cg.setCreateTime(new Timestamp(System.currentTimeMillis()));
        cg.setGroupCondition("[{\"symbol\":0,\"leafs\":[{\"name\":\"4\",\"id\":\"87\"}],\"type\":1,\"labelId\":\"84\",\"parentName\":\"家庭人口数\",\"path\":\"人口统计学/基本信息/家庭人口数\"}]");
        cg.setIndustryPoolId(customGroupDTO.getIndustryPoolId());
        cg.setIndustryPoolName(customGroupDTO.getIndustryPoolName());
        log.info("导入客户群插入customer_group表的数据:" + cg);
        int id = (int) customGroupDao.saveReturnPk(cg);
        if (id > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(" create table IF NOT EXISTS t_customer_group_list_");
            sb.append(id);
            sb.append(" like t_customer_group_list");
            try {
                customGroupDao.executeUpdateSQL(sb.toString());
            } catch (HibernateException e) {
                log.error("创建用户群表失败,id:" + id, e);
            }

            // 保存客户群触达方式
            if (StringUtil.isNotEmpty(customGroupDTO.getTouchMode())) {
                CustomerGroupProperty cgp = new CustomerGroupProperty(id, "touchMode", customGroupDTO.getTouchMode(), new Timestamp(System.currentTimeMillis()));
                customGroupDao.saveOrUpdate(cgp);
            }
            // 处理联通平台活动名称字段
            if (StringUtil.isNotEmpty(customGroupDTO.getUnicomActivityName())) {
                CustomerGroupProperty cgp = new CustomerGroupProperty(id, "unicomActivityName", customGroupDTO.getUnicomActivityName(), new Timestamp(System.currentTimeMillis()));
                customGroupDao.saveOrUpdate(cgp);
                // 数据渠道为联通
                cgp = new CustomerGroupProperty(id, "dataChannel", "unicom", new Timestamp(System.currentTimeMillis()));
                customGroupDao.saveOrUpdate(cgp);
                // 拉取时间为空
                cgp = new CustomerGroupProperty(id, "pullTime", "", new Timestamp(System.currentTimeMillis()));
                customGroupDao.saveOrUpdate(cgp);
                // 拉取状态为拉取中
                cgp = new CustomerGroupProperty(id, "pullStatus", "1", new Timestamp(System.currentTimeMillis()));
                customGroupDao.saveOrUpdate(cgp);
            }
            return 1;
        }
        return 0;
    }

    /**
     * 生成大数据平台自动提取数据文件
     *
     * @param groupId   客群ID
     * @param custId    客户ID
     * @param projectId 项目ID
     * @param day       去重天数
     * @param num       需求数量
     * @param condition 标签条件
     * @return
     */
    public String generateCGroupDataConditionFile(String groupId, String custId, String projectId, int day,
                                                  int num, String condition) {
        if (StringUtil.isEmpty(projectId)) {
            projectId = "11";
        }
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groupid", groupId);
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("custid", custId);
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("projectid", projectId);
        jsonArray.add(jsonObject);

        // 查询项目所属行业
        jsonObject = new JSONObject();
        MarketProject marketProject = marketProjectDao.selectMarketProject(NumberConvertUtil.parseInt(projectId));
        if (marketProject != null) {
            jsonObject.put("industryid", String.valueOf(marketProject.getIndustryId()));
        } else {
            jsonObject.put("industryid", "");
        }
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("days", String.valueOf(day));
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("num", String.valueOf(num));
        jsonArray.add(jsonObject);

        JSONArray conditions = JSON.parseArray(condition);
        for (int i = 0; i < conditions.size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("labelId", conditions.getJSONObject(i).getString("labelId"));
            if (conditions.getJSONObject(i).get("leafs") instanceof String) {
                jsonObject.put("leafs", JSON.parseArray(conditions.getJSONObject(i).getString("leafs")));
            } else {
                jsonObject.put("leafs", conditions.getJSONObject(i).getJSONArray("leafs"));
            }
            jsonArray.add(jsonObject);
        }
        log.info("客户群:" + groupId + "自动提取数据文件内容:" + jsonArray.toJSONString());
        return jsonArray.toJSONString();
    }


    public synchronized void addCustomGroupData0(String orderId) throws Exception {
        CustomGroup customGroup = customGroupDao.findUniqueBy("orderId", orderId);
        if (customGroup != null) {
            customGroup.setStatus(3);
            customGroupDao.update(customGroup);
        }
        // 0.建分表(客户群ID为后缀)
        StringBuffer
                sb = new StringBuffer();
        sb.append("create table IF NOT EXISTS t_customer_group_list_");
        sb.append(customGroup.getId());
        sb.append(" like t_customer_group_list");
        jdbcTemplate.update(sb.toString());
        JSONArray remainJSONArr = new JSONArray();
        try {
            // remain groupSource 数组转map,避免嵌套循环
            remainJSONArr = JSON.parseArray(customGroup.getRemark());
            Map<Integer, JSONObject> remainsMap = new HashMap<Integer, JSONObject>();
            JSONArray groupSourceJSONArr = JSON.parseArray(customGroup.getGroupSource());
            Map<Integer, JSONObject> groupSourceMap = new HashMap<Integer, JSONObject>();
            for (int i = 0; i < groupSourceJSONArr.size(); i++) {
                JSONObject groupSource = (JSONObject) groupSourceJSONArr.get(i);
                Integer sourceId = groupSource.getInteger("sourceId");
                groupSourceMap.put(sourceId, groupSource);
            }
            for (int i = 0; i < remainJSONArr.size(); i++) {
                JSONObject remainJSON = (JSONObject) remainJSONArr.get(i);
                Integer sourceId = remainJSON.getInteger("sourceId");
                remainsMap.put(sourceId, remainJSON);
                // 计算remain
                JSONObject groupSource = groupSourceMap.get(sourceId);
                Integer quantity = groupSource.getInteger("quantity");
                Integer remain = remainJSON.getInteger("remain");
                remain = remain - quantity;
                remainJSON.put("remain", remain);
            }

        } catch (Exception e) {
            log.error("订单号:[" + orderId + "]创建客户群详情表失败,", e);
        }

        // 2.更新客户群状态
        customGroup.setAvailably(Constant.AVAILABLY);
        String remainsStr = JSON.toJSONString(remainJSONArr);
        customGroup.setRemark(remainsStr);
        customGroupDao.update(customGroup);
    }


    public List<Map<String, Object>> getCustomGroupDataV3(LoginUser loginUser, String customer_group_id, Integer
            pageNum,
                                                          Integer pageSize, String id, String userName, Integer status, String callType, String action, String
                                                                  intentLevel) {
        List<Map<String, Object>> result = null;
        String userId = String.valueOf(loginUser.getId());
        StringBuffer sb = new StringBuffer();
        CustomGroup customGroup = customGroupDao.get(Integer.parseInt(customer_group_id));
        // 机器人任务查询意向度字段
        if (customGroup.getTaskType() != null && 3 == customGroup.getTaskType()) {
            sb.append(" select custG.id, custG.user_id, custG.STATUS, custG.call_count callCount, custG.last_call_time lastCallTime, custG.intent_level intentLevel,");
        } else {
            sb.append(" select custG.id, custG.user_id, custG.STATUS, custG.call_count callCount, custG.last_call_time lastCallTime, ");
        }

        sb.append(" custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street");
        sb.append("  from " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customer_group_id + " custG ");
        sb.append(" LEFT JOIN t_customer_user user ON user.ID = custG.user_id");
        sb.append(" where 1=1 ");

        if ("2".equals(loginUser.getUserType())) {
            // 组长查组员列表
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                Set<String> userIds = new HashSet<>();
                if (customerUserDTOList.size() > 0) {
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userIds.add(customerUserDTO.getId());
                    }
                    // 分配责任人操作
                    if (userIds.size() > 0) {
                        if ("distribution".equals(action)) {
                            sb.append(" AND (user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR custG.status= 1)");
                        } else {
                            sb.append(" AND user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }

                    }
                }
            } else {
                sb.append(" AND user.id = '" + userId + "'");
            }
        }
        if (null != id && !"".equals(id)) {
            sb.append(" and custG.id like '%" + id + "%'");
        }
        if (null != status && !"".equals(status)) {
            sb.append(" and custG.STATUS = " + status);
        }
        if (StringUtil.isNotEmpty(userName)) {
            sb.append(" and user.account like '%" + userName.trim() + "%'");
        }
        if (null != callType) {
            // 未呼叫
            if ("1".equals(callType)) {
                sb.append(" AND (custG.call_count IS NULL OR custG.call_count=0)");
                //已呼叫
            } else if ("2".equals(callType)) {
                sb.append(" AND custG.call_count > 0");
            }
        }
        if (StringUtil.isNotEmpty(intentLevel)) {
            sb.append(" and custG.intent_level = '" + intentLevel + "'");
        }

        sb.append(" ORDER BY id ASC ");
        if (pageNum != null && !"".equals(pageNum) && pageSize != null && !"".equals(pageSize)) {
            sb.append("  LIMIT " + pageNum + "," + pageSize);
        }
        try {
            result = customGroupDao.sqlQuery(sb.toString());
        } catch (DataAccessException e) {
            log.error("查询客户群列表失败,", e);
            return result;
        }
        CustomerUser user;
        for (Map<String, Object> map : result) {
            if (map != null) {
                map.put("phone", "");
                // 查询用户真实姓名
                if (map.get("user_id") != null) {
                    user = customerUserDao.get(Long.parseLong(String.valueOf(map.get("user_id"))));
                    if (user != null) {
                        map.put("realname", user.getRealname());
                        map.put("realname", user.getAccount());
                        map.put("user_id", String.valueOf(user.getId()));
                    } else {
                        map.put("realname", "");
                    }
                    map.put("user_id", String.valueOf(map.get("user_id")));
                } else {
                    // 默认机器人任务的负责人为空
                    if (customGroup.getTaskType() != null && 3 == customGroup.getTaskType()) {
                        map.put("realname", "");
                    }
                }
                // 处理意向度为空的情况
                if (customGroup.getTaskType() != null && 3 == customGroup.getTaskType() && map.get("intentLevel") == null) {
                    map.put("intentLevel", "");
                }
            }
        }
        return result;
    }

    public Page getCustomGroupDataV4(LoginUser loginUser, String customer_group_id, Integer pageNum,
                                     Integer pageSize, String id, String userName, Integer status, String callType, String action,
                                     String intentLevel, JSONArray custProperty) {
        Page page = null;
        String userId = String.valueOf(loginUser.getId());
        StringBuffer sb = new StringBuffer();
        CustomGroup customGroup = customGroupDao.get(NumberConvertUtil.parseInt(customer_group_id));
        if (customGroup == null) {
            page = new Page();
            return page;
        }
        // 机器人任务查询意向度字段
        if (customGroup.getTaskType() != null && 3 == customGroup.getTaskType()) {
            sb.append(" select custG.id, custG.user_id, custG.STATUS, custG.call_count callCount, custG.last_call_time lastCallTime, custG.intent_level intentLevel,");
        } else {
            sb.append(" select custG.id, custG.user_id, custG.STATUS, custG.call_count callCount, custG.last_call_time lastCallTime, ");
        }

        sb.append(" custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street, custG.super_data ");
        sb.append("  from " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customer_group_id + " custG ");
        sb.append(" LEFT JOIN t_customer_user user ON user.ID = custG.user_id");
        sb.append(" where 1=1 ");

        if ("2".equals(loginUser.getUserType())) {
            // 组长查组员列表
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                Set<String> userIds = new HashSet<>();
                if (customerUserDTOList.size() > 0) {
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userIds.add(customerUserDTO.getId());
                    }
                    // 分配责任人操作
                    if (userIds.size() > 0) {
                        if ("distribution".equals(action)) {
                            sb.append(" AND (user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR custG.status= 1)");
                        } else {
                            sb.append(" AND user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }

                    }
                }
            } else {
                sb.append(" AND user.id = '" + userId + "'");
            }
        }
        if (null != id && !"".equals(id)) {
            sb.append(" and custG.id like '%" + id + "%'");
        }
        if (null != status && !"".equals(status)) {
            sb.append(" and custG.STATUS = " + status);
        }
        if (StringUtil.isNotEmpty(userName)) {
            sb.append(" and user.account like '%" + userName.trim() + "%'");
        }
        if (null != callType) {
            // 未呼叫
            if ("1".equals(callType)) {
                sb.append(" AND (custG.call_count IS NULL OR custG.call_count=0)");
                //已呼叫
            } else if ("2".equals(callType)) {
                sb.append(" AND custG.call_count > 0");
            }
        }
        if (StringUtil.isNotEmpty(intentLevel)) {
            sb.append(" and custG.intent_level = '" + intentLevel + "'");
        }
        // 查询所有自建属性
        List<CustomerLabel> customerLabels = customerLabelDao.listCustomerLabel(loginUser.getCustId());
        Map<String, CustomerLabel> cacheLabel = new HashMap<>();
        for (CustomerLabel c : customerLabels) {
            cacheLabel.put(c.getLabelId(), c);
        }
        if (custProperty != null && custProperty.size() != 0) {
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
                if (jsonObject == null) {
                    continue;
                }
                labelId = jsonObject.getString("labelId");
                optionValue = jsonObject.getString("optionValue");
                // 文本和多选支持模糊搜索
                if (cacheLabel.get(labelId) != null && cacheLabel.get(labelId).getType() != null
                        && (cacheLabel.get(labelId).getType() == 1 || cacheLabel.get(labelId).getType() == 3)) {
                    likeValue = "%\"" + labelId + "\":\"%" + optionValue + "%";
                } else {
                    likeValue = "%\"" + labelId + "\":\"" + optionValue + "\"%";
                }
                sb.append(" AND custG.super_data LIKE '" + likeValue + "' ");
            }
        }

        sb.append(" ORDER BY id ASC ");
        try {
            page = customGroupDao.sqlPageQuery0(sb.toString(), pageNum, pageSize);
        } catch (Exception e) {
            log.error("查询客户群列表失败,", e);
            return new Page();
        }
        CustomerUser user;
        if (page != null && page.getData() != null) {
            Map<String, Object> map, superData, labelData;
            List<Map<String, Object>> labelList;
            for (int i = 0; i < page.getData().size(); i++) {
                map = (Map<String, Object>) page.getData().get(i);
                if (map == null) {
                    continue;
                }
                labelList = new ArrayList<>();
                // 处理自建属性数据
                if (map != null && map.get("super_data") != null
                        && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                    superData = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                    if (superData != null && superData.size() > 0) {
                        for (Map.Entry<String, Object> key : superData.entrySet()) {
                            labelData = new HashMap<>();
                            labelData.put("id", key.getKey());
                            labelData.put("name", cacheLabel.get(key.getKey()) != null ? cacheLabel.get(key.getKey()).getLabelName() : "");
                            labelData.put("value", key.getValue());
                            labelList.add(labelData);
                        }
                        map.put("labelList", labelList);
                    }
                }
                map.remove("super_data");
                map.put("phone", "");
                // 查询用户真实姓名
                if (map.get("user_id") != null) {
                    user = customerUserDao.get(Long.parseLong(String.valueOf(map.get("user_id"))));
                    if (user != null) {
                        map.put("realname", user.getRealname());
                        map.put("realname", user.getAccount());
                        map.put("user_id", String.valueOf(user.getId()));
                    } else {
                        map.put("realname", "");
                    }
                    map.put("user_id", String.valueOf(map.get("user_id")));
                } else {
                    // 默认机器人任务的负责人为空
                    if (customGroup.getTaskType() != null && 3 == customGroup.getTaskType()) {
                        map.put("realname", "");
                    }
                }
                // 处理意向度为空的情况
                if (customGroup.getTaskType() != null && 3 == customGroup.getTaskType() && map.get("intentLevel") == null) {
                    map.put("intentLevel", "");
                }
            }
        }

        return page;
    }


    public long countCustomGroupDataV2(LoginUser loginUser, String customer_group_id, String id, String
            userName, Integer status, String callType, String action) {
        //判断当前用户的user_type
        String userId = String.valueOf(loginUser.getId());
        if ("1".equals(loginUser.getUserType())) {
            //企业用户
            userId = "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("select COUNT(0) count");
        sb.append("  from t_customer_group_list_" + customer_group_id + " custG ");
        sb.append(" LEFT JOIN t_customer_user user ON user.ID = custG.user_id");
        sb.append(" where 1=1 ");

        if (null != id && !"".equals(id)) {
            sb.append(" and custG.id like '%" + id + "%'");
        }

        if (null != status && !"".equals(status)) {
            sb.append(" and custG.STATUS = " + status);
        }

        if ("2".equals(loginUser.getUserType())) {
            // 组长查组员列表
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                Set<String> userIds = new HashSet<>();
                if (customerUserDTOList.size() > 0) {
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userIds.add(customerUserDTO.getId());
                    }
                    // 分配责任人操作
                    if (userIds.size() > 0) {
                        if ("distribution".equals(action)) {
                            sb.append(" AND (user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR custG.status= 1)");
                        } else {
                            sb.append(" AND user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }

                    }
                }
            } else {
                sb.append(" AND user.id = '" + userId + "'");
            }
        }


        if (StringUtil.isNotEmpty(userName)) {
            sb.append(" and user.realname like '%" + userName.trim() + "%'");
        }
        if (null != callType) {
            // 未呼叫
            if ("1".equals(callType)) {
                sb.append(" AND (custG.call_count IS NULL OR custG.call_count=0)");
                //已呼叫
            } else if ("2".equals(callType)) {
                sb.append(" AND custG.call_count > 0");
            }
        }
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString());
        if (list.size() > 0 && list.get(0).get("count") != null) {
            return Long.parseLong(String.valueOf(list.get(0).get("count")));
        }
        return 0;
    }


    private LabelPriceSumVO getCustomGroupPrice(CustomGroup customGroup, Integer sourceId, Integer industryPoolId) {
        // 1查询标签成本价、销售价、订单金额、订单成本价
        String groupCondition = customGroup.getGroupCondition();
        JSONArray arr = JSONArray.parseArray(groupCondition);
        StringBuilder labelsString = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = arr.getJSONObject(i);
            String labelId = json.getString("labelId");
            labelsString.append("'");
            labelsString.append(labelId);
            labelsString.append("'");
            labelsString.append(",");
        }
        // 去掉最后一个逗号
        if (labelsString.length() > 0) {
            labelsString.deleteCharAt(labelsString.length() - 1);
        }
        LabelPriceSumVO sumPrice = labelInfoDao.getLabelSumPrice(sourceId, labelsString.toString(), industryPoolId);
        return sumPrice;
    }


    public String getMicroscopicPhone(String superid, MultiValueMap<String, Object> urlVariables) {

        String result = restTemplate.postForObject(Constant.LABEL_API
                + "/labels/rest.do", urlVariables, String.class);

        return result;
    }

    public Map<String, Object> countCallProgressByCondition0(String userId, String userType, String
            customGroupId, String customId) {
        List<Map<String, Object>> dataList;
        // 判断是否为企业用户
        if (String.valueOf(Constant.ADMIN_USER_TYPE).equals(userType)) {
            userId = "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT custG.id, custG.user_id, t_user.REALNAME `name` FROM  t_customer_group_list_" + customGroupId + " custG ");
        sb.append(" LEFT JOIN t_customer_user t_user ON t_user.ID = custG.user_id ");
        sb.append(" WHERE 1=1 ");
        if (StringUtil.isNotEmpty(userId)) {
            sb.append(" AND custG.user_id=" + userId);
        }
        Set<String> xAxisNames = new HashSet<>();
        Map<String, String> names = new HashMap<>(16);
        Map<String, Long> callCountData = new HashMap<>(16);
        Map<String, Set<String>> userGroupData = new HashMap<>(16);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString());
        Set<String> superIds;
        String userIdKey;
        for (Map<String, Object> map : list) {
            userIdKey = String.valueOf(map.get("user_id"));
            names.put(userIdKey, String.valueOf(map.get("name")));
            xAxisNames.add(String.valueOf(map.get("name")));
            if (userGroupData.get(userIdKey) != null) {
                superIds = userGroupData.get(userIdKey);
            } else {
                superIds = new HashSet<>();
            }
            superIds.add(String.valueOf(map.get("id")));
            userGroupData.put(userIdKey, superIds);
            if (callCountData.get(userIdKey) != null) {
                callCountData.put(userIdKey, callCountData.get(userIdKey) + 1L);
            } else {
                callCountData.put(userIdKey, 1L);
            }
        }

        Map<String, Long> calledData = new HashMap<>(16);
        sb.setLength(0);
        int callOwner = 1;
        for (Map.Entry<String, Set<String>> map : userGroupData.entrySet()) {
            sb.append("SELECT cust_id, user_id, create_time FROM t_touch_voice_log WHERE cust_id = ? AND customer_group_id = ? ");
            if (StringUtil.isNotEmpty(userId)) {
                sb.append(" AND user_id = " + userId);
            } else {
                // 管理员查询全部
                callOwner = 2;
            }
            if ("null".equals(map.getKey())) {
                callOwner = 2;
            }
            sb.append(" AND call_owner = ? AND status = 1001 AND superid IN (");

            for (String superId : map.getValue()) {
                sb.append("'" + superId + "',");
            }
            // 去除逗号
            sb.deleteCharAt(sb.length() - 1);
            sb.append(" ) GROUP BY superid, status ");
            list = jdbcTemplate.queryForList(sb.toString(), new Object[]{customId, customGroupId, callOwner});
            calledData.put(map.getKey(), (long) list.size());
            sb.setLength(0);
        }
        //构造返回数据
        dataList = new ArrayList<>();
        // 处理null为未分配
        names.put("null", "未分配");
        if (xAxisNames.contains("null")) {
            xAxisNames.remove("null");
            xAxisNames.add("未分配");
        }
        Map<String, Object> data;
        for (Map.Entry<String, Long> map : callCountData.entrySet()) {
            data = new HashMap<>();
            data.put("name", names.get(map.getKey()));
            data.put("calledSum", calledData.get(map.getKey()) == null ? 0 : calledData.get(map.getKey()));
            data.put("callSum", map.getValue());
            dataList.add(data);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("series", dataList);
        result.put("xAxis", xAxisNames);
        return result;
    }


    public Map<String, Object> countCallProgressByConditionV3(LoginUser loginUser, String customGroupId) {
        List<Map<String, Object>> dataList;
        // 判断是否为企业用户
        String userId = "";
        if (String.valueOf(Constant.ADMIN_USER_TYPE).equals(loginUser.getUserType())) {
            userId = "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT custG.id, custG.user_id, t_user.REALNAME `name` FROM  t_customer_group_list_" + customGroupId + " custG ");
        sb.append(" LEFT JOIN t_customer_user t_user ON t_user.ID = custG.user_id ");
        sb.append(" WHERE 1=1 ");
        if (StringUtil.isNotEmpty(userId)) {
            sb.append(" AND custG.user_id=" + userId);
        }
        Set<String> xAxisNames = new HashSet<>();
        Map<String, String> names = new HashMap<>(16);
        Map<String, Long> callCountData = new HashMap<>(16);
        Map<String, Set<String>> userGroupData = new HashMap<>(16);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString());
        Set<String> superIds;
        String userIdKey;
        for (Map<String, Object> map : list) {
            userIdKey = String.valueOf(map.get("user_id"));
            names.put(userIdKey, String.valueOf(map.get("name")));
            xAxisNames.add(String.valueOf(map.get("name")));
            if (userGroupData.get(userIdKey) != null) {
                superIds = userGroupData.get(userIdKey);
            } else {
                superIds = new HashSet<>();
            }
            superIds.add(String.valueOf(map.get("id")));
            userGroupData.put(userIdKey, superIds);
            if (callCountData.get(userIdKey) != null) {
                callCountData.put(userIdKey, callCountData.get(userIdKey) + 1L);
            } else {
                callCountData.put(userIdKey, 1L);
            }
        }

        Map<String, Long> calledData = new HashMap<>(16);
        sb.setLength(0);
        for (Map.Entry<String, Set<String>> map : userGroupData.entrySet()) {
            // 检查通话记录月表是否存在
            marketResourceDao.createVoiceLogTableNotExist(DateUtil.getNowMonthToYYYYMM());
            sb.append("SELECT cust_id, user_id, create_time FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + DateUtil.getNowMonthToYYYYMM() + " WHERE cust_id = ? AND customer_group_id = ? ");
            if (StringUtil.isNotEmpty(userId)) {
                sb.append(" AND user_id = " + userId);
            }
            sb.append(" AND status = 1001 AND superid IN (");

            for (String superId : map.getValue()) {
                sb.append("'" + superId + "',");
            }
            // 去除逗号
            sb.deleteCharAt(sb.length() - 1);
            sb.append(" ) GROUP BY superid, status ");
            list = jdbcTemplate.queryForList(sb.toString(), new Object[]{loginUser.getCustId(), customGroupId});
            calledData.put(map.getKey(), (long) list.size());
            sb.setLength(0);
        }
        //构造返回数据
        dataList = new ArrayList<>();
        // 处理null为未分配
        names.put("null", "未分配");
        if (xAxisNames.contains("null")) {
            xAxisNames.remove("null");
            xAxisNames.add("未分配");
        }
        Map<String, Object> data;
        for (Map.Entry<String, Long> map : callCountData.entrySet()) {
            data = new HashMap<>();
            data.put("name", names.get(map.getKey()));
            data.put("calledSum", calledData.get(map.getKey()) == null ? 0 : calledData.get(map.getKey()));
            data.put("callSum", map.getValue());
            dataList.add(data);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("series", dataList);
        result.put("xAxis", xAxisNames);
        return result;
    }


    public Map<String, Object> getCustomerGroupInfo(String customGroupId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM customer_group WHERE id = ?");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), customGroupId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    /**
     * 根据客户或营销任务ID,身份ID获取用户信息
     *
     * @param customerGroupId
     * @param marketTaskId
     * @param superId
     * @return
     */
    public Map<String, Object> getCustomerGroupPersonInfoV4(String customerGroupId, String marketTaskId, String superId) {
        StringBuffer sb = new StringBuffer();
        sb.append("select custG.id, custG.user_id, custG.STATUS, t.realname, '' AS phone, ");
        sb.append(" custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city,custG.super_address_street ");
        sb.append("  , custG.super_data ");
        if (StringUtil.isEmpty(marketTaskId) || "null".equals(marketTaskId)) {
            sb.append(" FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " custG ");
        } else {
            sb.append(" FROM " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " custG ");
        }
        sb.append("  LEFT JOIN t_customer_user t ON custG.user_id = t.id");
        sb.append(" where 1=1 ");
        sb.append(" AND custG.id = ?");
        List<Map<String, Object>> list = null;
        Map<String, Object> map;
        try {
            list = customerDao.sqlQuery(sb.toString(), superId);
            if (list.size() > 0) {
                map = list.get(0);
                Map<String, String> labelDataMap;
                Map<String, Object> superLabel;
                CustomerLabel customerLabel;
                List<Map<String, Object>> selLabel = new ArrayList<>();
                if (StringUtil.isNotEmpty((String) map.get("super_data"))) {
                    labelDataMap = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                    if (labelDataMap != null) {
                        for (Map.Entry<String, String> labelDataMapKey : labelDataMap.entrySet()) {
                            customerLabel = customerLabelDao.getCustomerLabel(labelDataMapKey.getKey());
                            if (customerLabel != null) {
                                superLabel = new HashMap<>();
                                superLabel.put("label_id", labelDataMapKey.getKey());
                                superLabel.put("type", customerLabel.getType());
                                if (1 == customerLabel.getType()) {
                                    superLabel.put("optionValue", labelDataMapKey.getValue());
                                } else {
                                    superLabel.put("optionValue", labelDataMapKey.getValue().split(","));
                                }
                                selLabel.add(superLabel);
                            }
                        }
                    }
                }
                map.put("selLabel", selLabel);
                return map;
            }
        } catch (Exception e) {
            log.error("根据客户或营销任务ID,身份ID获取用户信息异常,", e);
        }
        map = new HashMap<>();
        map.put("selLabel", "[]");
        return map;
    }


    public String getPhoneAreaByPhone(String customerGroupId, String phone) {
        log.info("根据手机号和客户群ID获取归属地,customerGroupId" + customerGroupId + ",phone:" + phone);
        String area = "";
        List<Map<String, Object>> superIdList;
        String superId = phoneService.getSuperIdByPhone(customerGroupId, phone);
        try {
            superIdList = customerDao.sqlQuery("SELECT remark FROM t_customer_group_list_" + customerGroupId + " WHERE id = ?", superId);
            if (superIdList.size() > 0) {
                area = String.valueOf(superIdList.get(0).get("remark"));
                log.info("根据手机号和客户群ID获取到的归属地:" + area);
                return area;
            }
        } catch (Exception e) {
            log.error("根据手机号和客户群ID获取归属地异常,", e);
        }
        return area;
    }

    public String getPhoneAreaBySuperId(String customerGroupId, String superId) {
        log.info("根据手机号和客户群ID获取归属地,customerGroupId:" + customerGroupId + ",superId:" + superId);
        String area = "";
        List<Map<String, Object>> list;
        try {
            list = this.marketResourceDao.sqlQuery("SELECT remark FROM t_customer_group_list_" + customerGroupId + " WHERE id = ? ", superId);
            if (list != null && list.size() > 0) {
                area = String.valueOf(list.get(0).get("remark"));
            }
        } catch (Exception e) {
            log.error("查询客户群数据表归属地异常,superId:" + superId + ",customerGroupId:" + customerGroupId, e);
        }
        return area;
    }

    public int updateCustomerGroupAssigned(String customerGroupId, String marketTaskId, String superId, String userId) {
        int code = 0;
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        try {
            sb.append("update " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " set");
            sb.append(" update_time = ?, ");
            sb.append(" STATUS = '0', ");
            sb.append(" user_id = ? ");
            sb.append(" where id = ?");
            code = this.customerDao.executeUpdateSQL(sb.toString(), new Timestamp(System.currentTimeMillis()), userId, superId);

            sb.setLength(0);
            sb.append("update " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " set");
            sb.append(" update_time = ?, ");
            sb.append(" STATUS = '0', ");
            sb.append(" user_id = ? ");
            sb.append(" where id = ?");
            code = this.customerDao.executeUpdateSQL(sb.toString(), new Timestamp(System.currentTimeMillis()), userId, superId);

            map.put("code", code);
            map.put("message", "分配负责人成功");
            json.put("data", map);

        } catch (Exception e) {
            map.put("code", 000);
            map.put("message", "分配负责人失败");
            json.put("data", map);
        }
        return code;
    }


    /**
     * 获取客户群数据总量
     *
     * @param customerGroupId
     * @return
     */
    public long countCustomGroupPhoneList(String customerGroupId) {
        List<Map<String, Object>> customerList = customGroupDao.sqlQuery("SELECT cust_id FROM customer_group WHERE id = ?", customerGroupId);
        if (customerList.size() == 0) {
            return 0L;
        }
        long total = 0L;
        StringBuffer sb = new StringBuffer();
        sb.append("select COUNT(custG.id) count ");
        sb.append("  from " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " custG ");
        List<Map<String, Object>> ids = null;
        try {
            ids = customGroupDao.sqlQuery(sb.toString());
            if (ids != null && ids.size() > 0) {
                total = NumberConvertUtil.parseLong(ids.get(0).get("count"));
            }
        } catch (Exception e) {
            log.error("查询客户群数量失败,", e);
        }
        return total;
    }


    /**
     * 根据第三方任务ID获取营销任务总用户数
     *
     * @param taskId
     * @return
     */
    public long countMarketTaskUsersByTaskId(String taskId) {
        MarketTask marketTask = marketTaskDao.getMarketTaskByTaskId(taskId);
        if (marketTask == null) {
            return 0L;
        }
        StringBuffer sb = new StringBuffer();
        long total = 0L;
        sb.append("select COUNT(custG.id) count from " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTask.getId() + " custG ");
        List<Map<String, Object>> ids = null;
        try {
            ids = customGroupDao.sqlQuery(sb.toString());
            if (ids != null && ids.size() > 0) {
                total = NumberConvertUtil.parseLong(ids.get(0).get("count"));
            }
        } catch (DataAccessException e) {
            log.error("查询营销任务客户数量失败,", e);
        }
        return total;
    }

    /**
     * 讯众呼叫中心获取号码总数(新版)
     *
     * @param taskId
     * @return
     */
    public Map<String, Object> xzCountMarketTaskUsersByTaskId(String taskId) {
        MarketTask marketTask = marketTaskDao.getMarketTaskByTaskId(taskId);
        if (marketTask == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        long total = 0L, calledCount = 0L;
        sb.append("select COUNT(custG.id) count, IFNULL(SUM(`call_success_count` >= 1), 0) called_count from " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTask.getId() + " custG ");
        List<Map<String, Object>> ids;
        try {
            ids = customGroupDao.sqlQuery(sb.toString());
            if (ids != null && ids.size() > 0) {
                total = NumberConvertUtil.parseLong(ids.get(0).get("count"));
                calledCount = NumberConvertUtil.parseLong(ids.get(0).get("called_count"));
            }
        } catch (DataAccessException e) {
            log.error("查询营销任务客户数量失败,", e);
        }
        // 营销任务数据总数
        data.put("all_customer", total);
        // 营销任务呼叫数
        data.put("called_customer", marketTask.getTaskPhoneIndex());
        // 营销任务接通数
        data.put("connected_customer", calledCount);
        // 号码标识，获取号码时携带
        data.put("cusid", marketTask.getTaskPhoneIndex());
        return data;
    }

    public List<String> getCustomGroupPhoneList(String customerGroupId, Integer pageNum, Integer pageSize) {
        List<Map<String, Object>> customerList = customGroupDao.sqlQuery("SELECT cust_id FROM customer_group WHERE id = ?", customerGroupId);
        if (customerList.size() == 0) {
            return new ArrayList<>();
        }
        StringBuffer sb = new StringBuffer();
        sb.append("select custG.id ");
        sb.append("  from t_customer_group_list_" + customerGroupId + " custG ");
        //sb.append("  LEFT JOIN u u ON u.id = custG.id ");
        sb.append(" where 1=1 ");

        sb.append(" ORDER BY custG.id ASC ");
        if (pageNum != null && !"".equals(pageNum) && pageSize != null && !"".equals(pageSize)) {
            sb.append("  LIMIT " + pageNum + "," + pageSize);
        }
        List<Map<String, Object>> ids = null;
        List<String> phoneList = new ArrayList<>();
        try {
            ids = customGroupDao.sqlQuery(sb.toString());
            String phone;
            for (Map<String, Object> id : ids) {
                if (id != null) {
                    phone = phoneService.getPhoneBySuperId(String.valueOf(id.get("id")));
                    phoneList.add(phone);
                    //保存客群和手机号对应的身份ID到redis
                    phoneService.setCGroupDataToRedis(customerGroupId, String.valueOf(id.get("id")), phone);
                }
            }
        } catch (DataAccessException e) {
            log.error("查询客户群手机号失败,", e);
        }

        return phoneList;
    }


    /**
     * 根据第三方任务ID拉取手机号
     *
     * @param taskId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<String> listMarketTaskPhoneByTaskId(String taskId, Integer pageNum, Integer pageSize) {
        log.info("第三方任务ID:" + taskId + ",拉取手机号,pageNum:" + pageNum + ",pageSize:" + pageSize);
        MarketTask marketTask = marketTaskDao.getMarketTaskByTaskId(taskId);
        if (marketTask == null) {
            return new ArrayList<>();
        }
        int phoneIndex = marketTask.getTaskPhoneIndex();
        log.info("营销任务marketTaskId:" + marketTask.getId() + ",phoneIndexdex:" + phoneIndex);

        StringBuffer sb = new StringBuffer();
        sb.append("select custG.id ");
        sb.append("  from " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTask.getId() + " custG ");
        sb.append(" where 1=1 ");
        sb.append(" ORDER BY custG.n_id ASC ");

        // 如果记录的号码index大于拉取的index,则从记录号码的index开始拉取,防止重复拨打
        if (phoneIndex > pageNum) {
            log.warn("营销任务marketTaskId:" + marketTask.getId() + ",记录的index:" + phoneIndex + ",拉取的index:" + pageNum);
            pageNum = phoneIndex;
        }
        sb.append("  LIMIT " + pageNum + "," + pageSize);

        List<Map<String, Object>> ids;
        List<String> phoneList = new ArrayList<>();
        try {
            ids = customGroupDao.sqlQuery(sb.toString());
            if (ids == null || ids.size() == 0) {
                log.info("营销任务marketTaskId:" + marketTask.getId() + ",手机号拉取完成,phoneIndex:" + phoneIndex);
                return phoneList;
            }
            phoneIndex += ids.size();
            String phone;
            for (Map<String, Object> id : ids) {
                if (id != null) {
                    phone = phoneService.getPhoneBySuperId(String.valueOf(id.get("id")));
                    phoneList.add(phone);
                    //保存客群和手机号对应的身份ID到redis
                    phoneService.setCGroupDataToRedis(String.valueOf(marketTask.getCustomerGroupId()), String.valueOf(id.get("id")), phone);
                }
            }
            marketTask.setTaskPhoneIndex(phoneIndex);
            marketTaskDao.update(marketTask);
        } catch (Exception e) {
            log.error("查询营销任务:" + marketTask.getId() + "拉取手机号失败,", e);
            phoneList = new ArrayList<>();
        }
        return phoneList;
    }

    /**
     * 讯众拉号返回随路参数
     *
     * @param taskId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<XzPullPhoneDTO> pageMarketTaskPhoneByTaskId(String taskId, Integer pageNum, Integer pageSize) {
        log.info("第三方任务ID:" + taskId + ",拉取手机号,pageNum:" + pageNum + ",pageSize:" + pageSize);
        MarketTask marketTask = marketTaskDao.getMarketTaskByTaskId(taskId);
        if (marketTask == null) {
            return new ArrayList<>();
        }
        int phoneIndex = marketTask.getTaskPhoneIndex();
        log.info("营销任务marketTaskId:" + marketTask.getId() + ",phoneIndexdex:" + phoneIndex);

        StringBuffer sb = new StringBuffer();
        sb.append("select custG.id ");
        sb.append("  from " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTask.getId() + " custG ");
        sb.append(" where 1=1 ");
        sb.append(" ORDER BY custG.n_id ASC ");

        // 如果记录的号码index大于拉取的index,则从记录号码的index开始拉取,防止重复拨打
        if (phoneIndex > pageNum) {
            log.warn("营销任务marketTaskId:" + marketTask.getId() + ",记录的index:" + phoneIndex + ",拉取的index:" + pageNum);
            pageNum = phoneIndex;
        }
        sb.append("  LIMIT " + pageNum + "," + pageSize);

        List<Map<String, Object>> ids;
        List<XzPullPhoneDTO> phoneList = new ArrayList<>();
        try {
            ids = customGroupDao.sqlQuery(sb.toString());
            if (ids == null || ids.size() == 0) {
                log.info("营销任务marketTaskId:" + marketTask.getId() + ",手机号拉取完成,phoneIndex:" + phoneIndex);
                return phoneList;
            }
            phoneIndex += ids.size();
            String phone;
            XzPullPhoneDTO dto;
            for (Map<String, Object> id : ids) {
                if (id != null) {
                    phone = phoneService.getPhoneBySuperId(String.valueOf(id.get("id")));
                    phoneList.add(new XzPullPhoneDTO(phone, String.valueOf(id.get("id"))));
                    //保存客群和手机号对应的身份ID到redis
                    phoneService.setCGroupDataToRedis(String.valueOf(marketTask.getCustomerGroupId()), String.valueOf(id.get("id")), phone);
                }
            }
            marketTask.setTaskPhoneIndex(phoneIndex);
            marketTaskDao.update(marketTask);
        } catch (Exception e) {
            log.error("查询营销任务:" + marketTask.getId() + "拉取手机号失败,", e);
            phoneList = new ArrayList<>();
        }
        return phoneList;
    }


    public Page listPage(String customer_group_id, Integer pageNum, Integer pageSize, String id) {
        Page page = new Page();

        try {
            StringBuffer sql = new StringBuffer(" from t_customer_group_list_" + customer_group_id + " where 1=1 ");

            if (null != id && !"".equals(id)) {
                sql.append(" and id='" + id + "'");
            }

            String total = this.jdbcTemplate.queryForObject("select count(1) " + sql.toString(), String.class);
            page.setTotal("".equals(total) ? 0 : Integer.parseInt(total));

            String sql_list = "SELECT id,user_id,call_count,last_call_time,remark,call_success_count,call_fail_count,call_empty_count"
                    + sql.toString() + " ORDER BY id DESC LIMIT " + pageNum + "," + pageSize;
            List<Map<String, Object>> list = this.customerGroupListDao.sqlQuery(sql_list);
            for (Map<String, Object> map : list) {
                if (map != null) {
                    for (Map.Entry<String, Object> keySet : map.entrySet()) {
                        if (keySet.getValue() == null) {
                            keySet.setValue("");
                        }
                    }
                }
            }
            page.setData(list);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return page;
    }


    /**
     * @param timeType        1-按天查询 2-全部 默认不传查当天
     * @param customerGroupId 客户群ID
     * @param loginUser       当前登录用户信息
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param labelName       统计成功数量的自建属性名称
     * @param labelId         统计成功数量的自建属性ID
     * @param optionValue     统计成功数量的自建属性选项值
     * @return
     */
    public Map<String, Object> countCustomerGroupCallDataV4(int timeType, String customerGroupId, LoginUser
            loginUser, String startTime,
                                                            String endTime, String labelName, String labelId, String optionValue) {
        Map<String, Object> data = new HashMap<>();
        // 呼叫量
        data.put("callSum", 0);
        // 接通量
        data.put("calledSum", 0);
        // 接通率
        data.put("calledPercent", 0);
        // 成功量
        data.put("successSum", 0);
        // 成功率,设置精确到小数点后2位
        data.put("successPercent", 0);
        data.put("labelListData", new ArrayList<>());

        try {
            if (StringUtil.isEmpty(customerGroupId)) {
                log.warn("customerGroupId参数异常");
                return data;
            }
            int taskType = -1;
            CustomGroup customGroup = customGroupDao.get(Integer.parseInt(customerGroupId));
            if (customGroup == null) {
                log.warn("客户群为空:" + customerGroupId);
                return data;
            }
            if (customGroup.getTaskType() != null) {
                taskType = customGroup.getTaskType();
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    log.warn("查询客户群统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return data;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (customGroup.getTaskCreateTime() != null && customGroup.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskEndTime().getTime());
                } else if (customGroup.getCreateTime() != null) {
                    localEndDateTime = LocalDateTime.now();
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getCreateTime().getTime());
                    log.warn("营销任务创建和结束为空默认取客户群的创建时间:" + customGroup.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    log.warn("查询客户群统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + customGroup.getTaskCreateTime() + ",taskEndTime:" + customGroup.getTaskEndTime());
                    return data;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            List<Map<String, Object>> callLogList = new ArrayList<>();
            List<Map<String, Object>> callLogListTmp;
            final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
            // 处理跨月查询逻辑
            for (LocalDateTime nowTime = localStartDateTime; nowTime.isBefore(localEndDateTime); ) {
                // 检查通话记录月表是否存在
                marketResourceDao.createVoiceLogTableNotExist(nowTime.format(YYYYMM));
                sqlSb.setLength(0);
                sqlSb.append("SELECT t.status, t.call_data, t.superid FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowTime.format(YYYYMM) + " t ");
                sqlSb.append("WHERE t.cust_id = ? AND t.customer_group_id = ? AND t.create_time >= ? AND t.create_time <= ?");

                //管理员查全部
                if ("1".equals(loginUser.getUserType())) {
                    callLogListTmp = this.customerGroupListDao.sqlQuery(sqlSb.toString(), loginUser.getCustId(), customerGroupId, startTime, endTime);
                } else {
                    // 组长查整个组的外呼统计
                    if ("1".equals(loginUser.getUserGroupRole())) {
                        List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                        Set<String> userIds = new HashSet<>();
                        if (customerUserDTOList.size() > 0) {
                            for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                                userIds.add(customerUserDTO.getId());
                            }
                            // 分配责任人操作
                            if (userIds.size() > 0) {
                                if (3 == taskType) {
                                    sqlSb.append(" AND (t.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR t.call_data LIKE '%level%')");
                                } else {
                                    sqlSb.append(" AND t.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                                }
                            }
                        } else {
                            // 处理组长下没有员工的情况,只查询自己的通话记录
                            if (3 == taskType) {
                                sqlSb.append(" AND (t.user_id = '" + loginUser.getId() + "' OR t.call_data LIKE '%level%')");
                            } else {
                                sqlSb.append(" AND t.user_id = '" + loginUser.getId() + "'");
                            }
                        }
                        callLogListTmp = this.customerGroupListDao.sqlQuery(sqlSb.toString(), loginUser.getCustId(), customerGroupId, startTime, endTime);
                    } else {
                        if (3 == taskType) {
                            sqlSb.append(" AND (t.user_id = ? OR t.call_data LIKE '%level%')");
                        } else {
                            sqlSb.append(" AND t.user_id = ? ");
                        }
                        callLogListTmp = this.customerGroupListDao.sqlQuery(sqlSb.toString(), loginUser.getCustId(), customerGroupId, startTime, endTime, loginUser.getId());
                    }
                }
                // 分月数据汇总到总数据中
                if (callLogListTmp.size() > 0) {
                    callLogList.addAll(callLogListTmp);
                }
                //　当前时间处理到下月1号0点0分0秒
                nowTime = nowTime.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }

            // 呼叫数量
            long callSum = callLogList.size();
            // 接通数量
            long calledSum = 0L;

            Set<String> superIdSets = new HashSet<>();
            // 接通数量计算
            VoiceLogCallDataDTO voiceLogCallDataDTO;
            for (Map<String, Object> map : callLogList) {
                // 呼叫成功
                if (map != null && map.get("call_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("call_data")))
                        && map.get("status") != null && "1001".equals(String.valueOf(map.get("status")))) {
                    voiceLogCallDataDTO = JSON.parseObject(String.valueOf(map.get("call_data")), VoiceLogCallDataDTO.class);
                    if (voiceLogCallDataDTO != null && StringUtil.isNotEmpty(voiceLogCallDataDTO.getCalledDuration())
                            && NumberConvertUtil.parseLong(voiceLogCallDataDTO.getCalledDuration()) > 0) {
                        calledSum++;
                        superIdSets.add(String.valueOf(map.get("superid")));
                    }
                }
            }
            // 接通率
            String calledPercent = "0";
            if (callSum > 0) {
                calledPercent = NumberConvertUtil.getPercent(calledSum, callSum);
            }

            Map<String, Object> customerSingleSelectLabelName = new HashMap<>();
            // 处理标签ID或者标签明显
            List<Map<String, Object>> labelNames = customerGroupListDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", loginUser.getCustId());
            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    // 只存储单选类型的自建属性名称
                    if ("2".equals(String.valueOf(map.get("type")))) {
                        // 初始化所有自建属性名称
                        customerSingleSelectLabelName.put(String.valueOf(map.get("label_id")), map.get("label_name"));
                    }

                    if (StringUtil.isEmpty(labelId)) {
                        if (StringUtil.isNotEmpty(labelName) && labelName.equals(String.valueOf(map.get("label_name")))) {
                            labelId = String.valueOf(map.get("label_id"));
                        }
                    }
                }
            }

            // 拼接like条件,用于查询super_data
            String likeValue = "\"" + labelId + "\":\"" + optionValue + "\"";
            List<Map<String, Object>> successVoiceLogList = new ArrayList<>();
            List<Map<String, Object>> successVoiceLogListTmp;
            // 处理跨月查询逻辑
            for (LocalDateTime nowTime = localStartDateTime; nowTime.isBefore(localEndDateTime); ) {
                sqlSb.setLength(0);
                sqlSb.append("SELECT t.id FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " t  ");
                sqlSb.append(" WHERE 1 = 1 ");

                sqlSb.append(" AND t.id IN( SELECT log.superid FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowTime.format(YYYYMM) + " log ");
                sqlSb.append(" WHERE log.cust_id = ? AND log.customer_group_id = ? AND log.create_time >= ?  AND log.create_time <= ? AND log.status = 1001) ");
                sqlSb.append(" AND t.super_data LIKE '%" + likeValue + "%' ");

                successVoiceLogListTmp = this.customerGroupListDao.sqlQuery(sqlSb.toString(), loginUser.getCustId(), customerGroupId, startTime, endTime);
                // 分月数据汇总到总数据中
                if (successVoiceLogListTmp.size() > 0) {
                    successVoiceLogList.addAll(successVoiceLogListTmp);
                }
                //　当前时间处理到下月1号0点0分0秒
                nowTime = nowTime.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }

            long successSum = 0;
            if (successVoiceLogList != null && successVoiceLogList.size() > 0) {
                for (Map<String, Object> map : successVoiceLogList) {
                    if (map.get("id") != null) {
                        // 在接通电话的superId中查找成功量
                        if (superIdSets.contains(String.valueOf(map.get("id")))) {
                            successSum++;
                        }
                    }
                }
            }

            // 呼叫量
            data.put("callSum", callSum);
            // 接通量
            data.put("calledSum", calledSum);
            // 接通率
            data.put("calledPercent", NumberConvertUtil.parseDouble(calledPercent));
            // 成功量
            data.put("successSum", successSum);
            // 成功率,设置精确到小数点后2位
            String successPercent = "0";
            if (successSum > 0 && calledSum > 0) {
                // 成功率=成功数量/接通量
                successPercent = NumberConvertUtil.getPercent(successSum, calledSum);
            }
            data.put("successPercent", NumberConvertUtil.parseDouble(successPercent));

            // 处理自定义标签统计,只查询自建属性为单选类型的
            List<Map<String, Object>> singleSelectList = new ArrayList<>();

            List<Map<String, Object>> labelDataList = new ArrayList<>();
            List<Map<String, Object>> labelDataListTmp;
            // 处理跨月查询逻辑
            for (LocalDateTime nowTime = localStartDateTime; nowTime.isBefore(localEndDateTime); ) {
                sqlSb.setLength(0);
                sqlSb.append("SELECT t.id, t.super_data FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " t  ");
                sqlSb.append(" WHERE t.super_data IS NOT NULL ");
                sqlSb.append(" AND t.id IN( SELECT log.superid FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowTime.format(YYYYMM) + " log ");
                sqlSb.append(" WHERE log.cust_id = ? AND log.customer_group_id = ? AND log.create_time >= ?  AND log.create_time <= ? AND log.status = 1001) ");

                labelDataListTmp = customerGroupListDao.sqlQuery(sqlSb.toString(), loginUser.getCustId(), customerGroupId, startTime, endTime);
                // 分月数据汇总到总数据中
                if (labelDataListTmp.size() > 0) {
                    labelDataList.addAll(labelDataListTmp);
                }
                //　当前时间处理到下月1号0点0分0秒
                nowTime = nowTime.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }

            Map<String, String> labelDataMap;
            Map<String, Object> customerSingleMap;
            for (Map<String, Object> map : labelDataList) {
                if (map != null && map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                    labelDataMap = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                    if (labelDataMap != null) {
                        for (Map.Entry<String, String> labelDataMapKey : labelDataMap.entrySet()) {
                            // 只处理单选类型的自建属性信息
                            if (customerSingleSelectLabelName.get(labelDataMapKey.getKey()) != null) {
                                customerSingleMap = new HashMap<>();
                                customerSingleMap.put("label_id", labelDataMapKey.getKey());
                                customerSingleMap.put("label_name", customerSingleSelectLabelName.get(labelDataMapKey.getKey()));
                                customerSingleMap.put("option_value", labelDataMapKey.getValue());
                                singleSelectList.add(customerSingleMap);
                            }
                        }
                    }
                }
            }
            Map<String, Object> labelData = new HashMap<>(16);
            Map<String, Long> labelCountData = new HashMap<>(16);
            // 自建属性选项名称
            Map<String, Object> labelOptionName = new HashMap<>();
            Map<String, Object> labelNameMap = new HashMap<>();
            String labelDataMapKey, labelOptionDataMapKey;

            Map<String, Long> optionCallMap;
            for (Map<String, Object> map : singleSelectList) {
                labelDataMapKey = map.get("label_id") + "";
                labelNameMap.put(labelDataMapKey, map.get("label_name"));

                labelOptionDataMapKey = map.get("label_id") + "" + map.get("option_value");
                labelOptionName.put(labelOptionDataMapKey, map.get("option_value"));

                if (labelData.get(labelDataMapKey) != null) {
                    optionCallMap = (Map<String, Long>) labelData.get(labelDataMapKey);
                    if (optionCallMap.get(labelOptionDataMapKey) != null) {
                        optionCallMap.put(labelOptionDataMapKey, optionCallMap.get(labelOptionDataMapKey) + 1L);
                    } else {
                        optionCallMap.put(labelOptionDataMapKey, 1L);
                    }
                    if (labelCountData.get(labelDataMapKey) != null) {
                        labelCountData.put(labelDataMapKey, labelCountData.get(labelDataMapKey) + 1L);
                    } else {
                        labelCountData.put(labelDataMapKey, 1L);
                    }
                } else {
                    optionCallMap = new HashMap<>();
                    optionCallMap.put(labelOptionDataMapKey, 1L);
                    labelCountData.put(labelDataMapKey, 1L);
                }
                labelData.put(labelDataMapKey, optionCallMap);
            }

            List<Map<String, Object>> labelListData = new ArrayList<>();
            List<Map<String, Object>> labelOptionListData;
            List<String> labelOptionNameList;
            Map<String, Object> labelMapData;
            Map<String, Object> labelOptionMap;
            Map<String, Object> valueMap;
            String labelPCheckPercent = "0";
            for (Map.Entry<String, Object> map : labelData.entrySet()) {
                labelMapData = new HashMap<>();
                // 自定义标签名称
                labelMapData.put("title", labelNameMap.get(map.getKey()));
                labelOptionMap = (Map<String, Object>) map.getValue();
                labelOptionListData = new ArrayList<>();
                labelOptionNameList = new ArrayList<>();
                for (Map.Entry<String, Object> labelOption : labelOptionMap.entrySet()) {
                    valueMap = new HashMap<>();
                    valueMap.put("name", String.valueOf(labelOptionName.get(labelOption.getKey())));
                    valueMap.put("count", labelOption.getValue());
                    // 处理比例
                    if (labelOption.getValue() != null && labelCountData.get(map.getKey()) != null) {
                        //labelPCheckPercent = numberFormat.format(Double.parseDouble(String.valueOf(labelOption.getValue())) / labelCountData.get(map.getKey()) * 100);
                        labelPCheckPercent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(String.valueOf(labelOption.getValue())), labelCountData.get(map.getKey()));
                    } else {
                        log.error("参数异常labelOption:" + labelOption + ",labelCountData:" + labelCountData + ",map:" + map);
                    }
                    valueMap.put("percent", labelPCheckPercent);
                    labelOptionListData.add(valueMap);
                    labelOptionNameList.add(String.valueOf(labelOptionName.get(labelOption.getKey())));
                }
                labelMapData.put("names", labelOptionNameList);
                labelMapData.put("values", labelOptionListData);
                labelListData.add(labelMapData);
            }

            data.put("labelListData", labelListData);
        } catch (Exception e) {
            log.error("获取客户群:" + customerGroupId + "统计分析异常,", e);
        }
        return data;
    }

    public Map<String, Object> countCustomerGroupCallDataV5(int timeType, String customerGroupId, UserQueryParam
            userQueryParam, String startTime, String endTime) {
        Map<String, Object> data = new HashMap<>();
        // 呼叫量
        data.put("callSum", 0);
        // 接通量
        data.put("calledSum", 0);
        // 成功量
        data.put("successSum", 0);
        // 未通量
        data.put("failSum", 0);

        data.put("labelListData", new ArrayList<>());

        try {
            if (StringUtil.isEmpty(customerGroupId)) {
                log.warn("customerGroupId参数异常");
                return data;
            }
            int taskType = -1;
            CustomGroup customGroup = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
            if (customGroup == null) {
                log.warn("未查询到指定客户群:" + customerGroupId);
                return data;
            }
            userQueryParam.setCustId(customGroup.getCustId());
            if (customGroup.getTaskType() != null) {
                taskType = customGroup.getTaskType();
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    log.warn("查询客户群统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return data;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (customGroup.getTaskCreateTime() != null && customGroup.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskEndTime().getTime());
                } else if (customGroup.getCreateTime() != null) {
                    localEndDateTime = LocalDateTime.now();
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getCreateTime().getTime());
                    log.warn("营销任务创建和结束为空默认取客户群的创建时间:" + customGroup.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    log.warn("查询客户群统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + customGroup.getTaskCreateTime() + ",taskEndTime:" + customGroup.getTaskEndTime());
                    return data;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ?");

            List<Map<String, Object>> callLogList;
            // 通话记录查询用户权限
            Set voiveUserIds = new HashSet();
            //普通用户权限处理
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiveUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                        voiveUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                    voiveUserIds.add(userQueryParam.getUserId());
                }
            }
            callLogList = this.customerGroupListDao.sqlQuery(sqlSb.toString(), localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                    localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")), customerGroupId);

            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L;
            if (callLogList.size() > 0) {
                callSum = NumberConvertUtil.parseLong(callLogList.get(0).get("caller_sum"));
                calledSum = NumberConvertUtil.parseLong(callLogList.get(0).get("called_sum"));
                busySum = NumberConvertUtil.parseLong(callLogList.get(0).get("busy_sum"));
                noServiceSum = NumberConvertUtil.parseLong(callLogList.get(0).get("no_service_area_sum"));
                phoneOverdueSum = NumberConvertUtil.parseLong(callLogList.get(0).get("phone_overdue_sum"));
                phoneShutdownSum = NumberConvertUtil.parseLong(callLogList.get(0).get("phone_shutdown_sum"));
                spacePhoneSum = NumberConvertUtil.parseLong(callLogList.get(0).get("space_phone_sum"));
                otherSum = NumberConvertUtil.parseLong(callLogList.get(0).get("other_sum"));
                failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                successSum = NumberConvertUtil.parseLong(callLogList.get(0).get("order_sum"));
            }

            // 呼叫量
            data.put("callSum", callSum);
            // 接通量
            data.put("calledSum", calledSum);
            // 未通量
            data.put("failSum", failSum);
            data.put("busySum", busySum);
            data.put("noServiceSum", noServiceSum);
            data.put("phoneOverdueSum", phoneOverdueSum);
            data.put("phoneShutdownSum", phoneShutdownSum);
            data.put("spacePhoneSum", spacePhoneSum);
            data.put("otherSum", otherSum);
            // 成功量
            data.put("successSum", successSum);

            Map<String, Object> customerSingleSelectLabelName = new HashMap<>();
            // 处理标签ID或者标签明显
            List<Map<String, Object>> labelNames = customerGroupListDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", userQueryParam.getCustId());
            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    // 只存储单选类型的自建属性名称
                    if ("2".equals(String.valueOf(map.get("type")))) {
                        // 初始化所有自建属性名称
                        customerSingleSelectLabelName.put(String.valueOf(map.get("label_id")), map.get("label_name"));
                    }
                }
            }
            final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
            // 处理自定义标签统计,只查询自建属性为单选类型的
            List<Map<String, Object>> singleSelectList = new ArrayList<>();

            List<Map<String, Object>> labelDataList = new ArrayList<>();
            List<Map<String, Object>> labelDataListTmp;
            // 处理跨月查询逻辑
            for (LocalDateTime nowTime = localStartDateTime; nowTime.isBefore(localEndDateTime); ) {
                sqlSb.setLength(0);
                sqlSb.append("SELECT t.id, t.super_data FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " t ")
                        .append(" JOIN " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowTime.format(YYYYMM) + " log ON t.id = log.superid")
                        .append(" WHERE t.super_data IS NOT NULL ")
                        .append(" AND log.customer_group_id = ? AND log.create_time >= ?  AND log.create_time <= ? AND log.status = 1001 ");
                if ("-1".equals(userQueryParam.getCustId())) {
                    sqlSb.append(" AND log.cust_id IS NOT NULL ");
                } else {
                    sqlSb.append(" AND log.cust_id = '" + userQueryParam.getCustId() + "' ");
                }
                if (voiveUserIds.size() > 0) {
                    sqlSb.append(" AND log.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(voiveUserIds) + ") ");
                }
                labelDataListTmp = customerGroupListDao.sqlQuery(sqlSb.toString(), customerGroupId, startTime, endTime);
                // 分月数据汇总到总数据中
                if (labelDataListTmp.size() > 0) {
                    labelDataList.addAll(labelDataListTmp);
                }
                //　当前时间处理到下月1号0点0分0秒
                nowTime = nowTime.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }

            Map<String, String> labelDataMap;
            Map<String, Object> customerSingleMap;
            for (Map<String, Object> map : labelDataList) {
                if (map != null && map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                    labelDataMap = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                    if (labelDataMap != null) {
                        for (Map.Entry<String, String> labelDataMapKey : labelDataMap.entrySet()) {
                            // 只处理单选类型的自建属性信息
                            if (customerSingleSelectLabelName.get(labelDataMapKey.getKey()) != null) {
                                customerSingleMap = new HashMap<>();
                                customerSingleMap.put("label_id", labelDataMapKey.getKey());
                                customerSingleMap.put("label_name", customerSingleSelectLabelName.get(labelDataMapKey.getKey()));
                                customerSingleMap.put("option_value", labelDataMapKey.getValue());
                                singleSelectList.add(customerSingleMap);
                            }
                        }
                    }
                }
            }
            Map<String, Object> labelData = new HashMap<>(16);
            Map<String, Long> labelCountData = new HashMap<>(16);
            // 自建属性选项名称
            Map<String, Object> labelOptionName = new HashMap<>();
            Map<String, Object> labelNameMap = new HashMap<>();
            String labelDataMapKey, labelOptionDataMapKey;

            Map<String, Long> optionCallMap;
            for (Map<String, Object> map : singleSelectList) {
                labelDataMapKey = map.get("label_id") + "";
                labelNameMap.put(labelDataMapKey, map.get("label_name"));

                labelOptionDataMapKey = map.get("label_id") + "" + map.get("option_value");
                labelOptionName.put(labelOptionDataMapKey, map.get("option_value"));

                if (labelData.get(labelDataMapKey) != null) {
                    optionCallMap = (Map<String, Long>) labelData.get(labelDataMapKey);
                    if (optionCallMap.get(labelOptionDataMapKey) != null) {
                        optionCallMap.put(labelOptionDataMapKey, optionCallMap.get(labelOptionDataMapKey) + 1L);
                    } else {
                        optionCallMap.put(labelOptionDataMapKey, 1L);
                    }
                    if (labelCountData.get(labelDataMapKey) != null) {
                        labelCountData.put(labelDataMapKey, labelCountData.get(labelDataMapKey) + 1L);
                    } else {
                        labelCountData.put(labelDataMapKey, 1L);
                    }
                } else {
                    optionCallMap = new HashMap<>();
                    optionCallMap.put(labelOptionDataMapKey, 1L);
                    labelCountData.put(labelDataMapKey, 1L);
                }
                labelData.put(labelDataMapKey, optionCallMap);
            }

            List<Map<String, Object>> labelListData = new ArrayList<>();
            List<Map<String, Object>> labelOptionListData;
            List<String> labelOptionNameList;
            Map<String, Object> labelMapData;
            Map<String, Object> labelOptionMap;
            Map<String, Object> valueMap;
            String labelPCheckPercent = "0";
            for (Map.Entry<String, Object> map : labelData.entrySet()) {
                labelMapData = new HashMap<>();
                // 自定义标签名称
                labelMapData.put("title", labelNameMap.get(map.getKey()));
                labelOptionMap = (Map<String, Object>) map.getValue();
                labelOptionListData = new ArrayList<>();
                labelOptionNameList = new ArrayList<>();
                for (Map.Entry<String, Object> labelOption : labelOptionMap.entrySet()) {
                    valueMap = new HashMap<>();
                    valueMap.put("name", String.valueOf(labelOptionName.get(labelOption.getKey())));
                    valueMap.put("count", labelOption.getValue());
                    // 处理比例
                    if (labelOption.getValue() != null && labelCountData.get(map.getKey()) != null) {
                        //labelPCheckPercent = numberFormat.format(Double.parseDouble(String.valueOf(labelOption.getValue())) / labelCountData.get(map.getKey()) * 100);
                        labelPCheckPercent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(String.valueOf(labelOption.getValue())), labelCountData.get(map.getKey()));
                    } else {
                        log.error("参数异常labelOption:" + labelOption + ",labelCountData:" + labelCountData + ",map:" + map);
                    }
                    valueMap.put("percent", labelPCheckPercent);
                    labelOptionListData.add(valueMap);
                    labelOptionNameList.add(String.valueOf(labelOptionName.get(labelOption.getKey())));
                }
                labelMapData.put("names", labelOptionNameList);
                labelMapData.put("values", labelOptionListData);
                labelListData.add(labelMapData);
            }

            data.put("labelListData", labelListData);
        } catch (Exception e) {
            log.error("获取客户群:" + customerGroupId + "统计分析异常,", e);
        }
        return data;
    }

    public Map<String, Object> countCGUserCallData(int timeType, String customerGroupId, UserQueryParam
            userQueryParam, String startTime, String endTime) {
        Map<String, Object> data = new HashMap<>();
        try {
            if (StringUtil.isEmpty(customerGroupId)) {
                log.warn("customerGroupId参数异常");
                return data;
            }
            int taskType = -1;
            CustomGroup customGroup = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
            if (customGroup == null) {
                log.warn("未查询到指定客户群:" + customerGroupId);
                return data;
            }
            userQueryParam.setCustId(customGroup.getCustId());
            if (customGroup.getTaskType() != null) {
                taskType = customGroup.getTaskType();
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    log.warn("查询客户群统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return data;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (customGroup.getTaskCreateTime() != null && customGroup.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskEndTime().getTime());
                } else if (customGroup.getCreateTime() != null) {
                    localEndDateTime = LocalDateTime.now();
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getCreateTime().getTime());
                    log.warn("营销任务创建和结束为空默认取客户群的创建时间:" + customGroup.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    log.warn("查询客户群统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + customGroup.getTaskCreateTime() + ",taskEndTime:" + customGroup.getTaskEndTime());
                    return data;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            // 查询用户呼叫数
            StringBuffer sqlSb = new StringBuffer();
            StringBuffer whereSql = new StringBuffer();
            StringBuffer totalSql = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    "IFNULL(SUM(called_duration),0) called_duration, IFNULL(SUM(call_amount),0)/1000 callAmount, IFNULL(SUM(call_prod_amount),0)/1000 callProdAmount FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ?");
            totalSql.append("SELECT IFNULL(SUM(call_amount),0)/1000 totalCallAmount, IFNULL(SUM(call_prod_amount),0)/1000 totalCallProdAmount FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ? ");
            Page page;

            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                whereSql.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                whereSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            whereSql.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            whereSql.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        whereSql.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        whereSql.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(userQueryParam.getWorkPlaceId())) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(userQueryParam.getWorkPlaceId());
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                whereSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }

            whereSql.append(" AND user_id <>'' ");
            sqlSb.append(whereSql);
            totalSql.append(whereSql);

            sqlSb.append(" GROUP BY user_id ");
            page = this.customerGroupListDao.sqlPageQuery0(sqlSb.toString(), userQueryParam.getPageNum(), userQueryParam.getPageSize(), startTime, endTime, customerGroupId);

            // 呼叫量,接通量,未通量, 成单量
            long calledSum = 0L, successSum = 0L;
            if (page.getData() != null && page.getData().size() > 0) {
                Map<String, Object> m;
                for (int i = 0; i < page.getData().size(); i++) {
                    m = (Map<String, Object>) page.getData().get(i);
                    System.out.println(m);
                    m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                    calledSum += NumberConvertUtil.parseLong(m.get("called_sum"));
                    successSum += NumberConvertUtil.parseLong(m.get("order_sum"));
                }
            }

            data.put("list", page.getData());
            data.put("total", page.getTotal());
            data.put("calledSum", calledSum);
            // 成功量
            data.put("successSum", successSum);
            // 计算通话费用总和
            data.put("totalCallAmount", 0);
            data.put("totalCallProdAmount", 0);
            List<Map<String, Object>> totalValue = marketTaskDao.sqlQuery(totalSql.toString(), startTime, endTime, customerGroupId);
            if (totalValue != null && totalValue.size() > 0) {
                data.put("totalCallAmount", totalValue.get(0).get("totalCallAmount"));
                data.put("totalCallProdAmount", totalValue.get(0).get("totalCallProdAmount"));
            }
        } catch (Exception e) {
            log.error("获取客户群:" + customerGroupId + "统计分析异常,", e);
        }
        return data;
    }

    /**
     * 导出客户群统计数据
     *
     * @param timeType
     * @param customerGroupId
     * @param userQueryParam
     * @param startTime
     * @param endTime
     * @param response
     */
    public void exportCustomerGroupCallData(int timeType, String customerGroupId, UserQueryParam
            userQueryParam, String startTime, String endTime, HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()) {
            if (StringUtil.isEmpty(customerGroupId)) {
                log.warn("customerGroupId参数异常");
                return;
            }
            int taskType = -1;
            CustomGroup customGroup = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
            if (customGroup == null) {
                log.warn("未查询到指定客户群:" + customerGroupId);
                return;
            }
            userQueryParam.setCustId(customGroup.getCustId());
            if (customGroup.getTaskType() != null) {
                taskType = customGroup.getTaskType();
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    log.warn("查询客户群统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (customGroup.getTaskCreateTime() != null && customGroup.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskEndTime().getTime());
                } else if (customGroup.getCreateTime() != null) {
                    localEndDateTime = LocalDateTime.now();
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getCreateTime().getTime());
                    log.warn("营销任务创建和结束为空默认取客户群的创建时间:" + customGroup.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    log.warn("查询客户群统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + customGroup.getTaskCreateTime() + ",taskEndTime:" + customGroup.getTaskEndTime());
                    return;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ?");

            List<Map<String, Object>> callLogList;
            // 通话记录查询用户权限
            Set voiveUserIds = new HashSet();
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiveUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                        voiveUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                    voiveUserIds.add(userQueryParam.getUserId());
                }
            }
            callLogList = this.customerGroupListDao.sqlQuery(sqlSb.toString(), localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                    localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")), customerGroupId);

            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L;
            if (callLogList.size() > 0) {
                callSum = NumberConvertUtil.parseLong(callLogList.get(0).get("caller_sum"));
                calledSum = NumberConvertUtil.parseLong(callLogList.get(0).get("called_sum"));
                busySum = NumberConvertUtil.parseLong(callLogList.get(0).get("busy_sum"));
                noServiceSum = NumberConvertUtil.parseLong(callLogList.get(0).get("no_service_area_sum"));
                phoneOverdueSum = NumberConvertUtil.parseLong(callLogList.get(0).get("phone_overdue_sum"));
                phoneShutdownSum = NumberConvertUtil.parseLong(callLogList.get(0).get("phone_shutdown_sum"));
                spacePhoneSum = NumberConvertUtil.parseLong(callLogList.get(0).get("space_phone_sum"));
                otherSum = NumberConvertUtil.parseLong(callLogList.get(0).get("other_sum"));
                failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                successSum = NumberConvertUtil.parseLong(callLogList.get(0).get("order_sum"));
            }

            String fileName = "客户群统计数据-" + customerGroupId + "-" + System.currentTimeMillis();
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
            int sheetNum = 1;
            BigDecimal bigDecimal, decimal;
            long callDurationTime = 1;
            List<List<String>> data, headers;
            List<String> columnList, head;

            data = new ArrayList<>();

            headers = new ArrayList<>();
            head = new ArrayList<>();
            head.add("呼叫量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("未通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通率");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功率");
            headers.add(head);
            //构造数据
            columnList = new ArrayList<>();
            //呼叫量
            columnList.add(String.valueOf(callSum));
            //接通量
            columnList.add(String.valueOf(calledSum));
            //未通量
            columnList.add(String.valueOf(failSum));
            //成功量
            columnList.add(String.valueOf(successSum));
            //接通率
            if (NumberConvertUtil.parseLong(callSum) == 0) {
                columnList.add(String.valueOf(0));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(calledSum, callSum)));
            }
            //成功率
            if (NumberConvertUtil.parseLong(calledSum) == 0) {
                columnList.add(String.valueOf(0));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(successSum, calledSum)));
            }

            data.add(columnList);

            Sheet sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("外呼数据统计");
            writer.write0(data, sheet);

            // 构造未接通号码统计
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("未通总量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("用户忙");
            headers.add(head);

            head = new ArrayList<>();
            head.add("不在服务区");
            headers.add(head);

            head = new ArrayList<>();
            head.add("停机");
            headers.add(head);

            head = new ArrayList<>();
            head.add("关机");
            headers.add(head);

            head = new ArrayList<>();
            head.add("空号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("其他");
            headers.add(head);
            //构造数量数据
            columnList = new ArrayList<>();
            columnList.add(String.valueOf(failSum));
            columnList.add(String.valueOf(busySum));
            columnList.add(String.valueOf(noServiceSum));
            columnList.add(String.valueOf(phoneOverdueSum));
            columnList.add(String.valueOf(phoneShutdownSum));
            columnList.add(String.valueOf(spacePhoneSum));
            columnList.add(String.valueOf(otherSum));
            data.add(columnList);
            //构造占比数据
            columnList = new ArrayList<>();
            if (NumberConvertUtil.parseLong(callSum) == 0) {
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(failSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(busySum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(noServiceSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(phoneOverdueSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(phoneShutdownSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(spacePhoneSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(otherSum, callSum)));
            }

            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("未接通号码统计");
            writer.write0(data, sheet);

            //　构造用户呼叫列表数据
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("员工");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功率");
            headers.add(head);

            head = new ArrayList<>();
            head.add("总通话时长");
            headers.add(head);

            head = new ArrayList<>();
            head.add("平均通话时长");
            headers.add(head);

            // 查询用户呼叫数
            sqlSb = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    "IFNULL(SUM(called_duration),0) called_duration FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ?");
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }
            }
            sqlSb.append(" GROUP BY user_id ");
            List<Map<String, Object>> list = this.customerGroupListDao.sqlQuery(sqlSb.toString(), startTime,
                    endTime, customerGroupId);

            Map<String, Object> m;
            for (int i = 0; i < list.size(); i++) {
                m = list.get(i);
                m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                columnList = new ArrayList<>();
                columnList.add(String.valueOf(m.get("userName")));
                columnList.add(String.valueOf(m.get("called_sum")));
                columnList.add(String.valueOf(m.get("order_sum")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add(String.valueOf(0));
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(m.get("order_sum")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }
                columnList.add(String.valueOf(m.get("called_duration")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add(String.valueOf(0));
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.divNumber(NumberConvertUtil.parseLong(m.get("called_duration")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }

                data.add(columnList);

            }
            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("员工统计");
            writer.write0(data, sheet);

            Map<String, Object> customerSingleSelectLabelName = new HashMap<>();
            // 处理标签ID或者标签明显
            List<Map<String, Object>> labelNames = customerGroupListDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", userQueryParam.getCustId());
            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    // 只存储单选类型的自建属性名称
                    if ("2".equals(String.valueOf(map.get("type")))) {
                        // 初始化所有自建属性名称
                        customerSingleSelectLabelName.put(String.valueOf(map.get("label_id")), map.get("label_name"));
                    }
                }
            }
            final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
            // 处理自定义标签统计,只查询自建属性为单选类型的
            List<Map<String, Object>> singleSelectList = new ArrayList<>();

            List<Map<String, Object>> labelDataList = new ArrayList<>();
            List<Map<String, Object>> labelDataListTmp;
            // 处理跨月查询逻辑
            for (LocalDateTime nowTime = localStartDateTime; nowTime.isBefore(localEndDateTime); ) {
                sqlSb.setLength(0);
                sqlSb.append("SELECT t.id, t.super_data FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " t ")
                        .append(" JOIN " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowTime.format(YYYYMM) + " log ON t.id = log.superid")
                        .append(" WHERE t.super_data IS NOT NULL ")
                        .append(" AND log.customer_group_id = ? AND log.create_time >= ?  AND log.create_time <= ? AND log.status = 1001 ");
                if ("-1".equals(userQueryParam.getCustId())) {
                    sqlSb.append(" AND log.cust_id IS NOT NULL ");
                } else {
                    sqlSb.append(" AND log.cust_id = '" + userQueryParam.getCustId() + "' ");
                }
                if (voiveUserIds.size() > 0) {
                    sqlSb.append(" AND log.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(voiveUserIds) + ") ");
                }
                labelDataListTmp = customerGroupListDao.sqlQuery(sqlSb.toString(), customerGroupId, startTime, endTime);
                // 分月数据汇总到总数据中
                if (labelDataListTmp.size() > 0) {
                    labelDataList.addAll(labelDataListTmp);
                }
                //　当前时间处理到下月1号0点0分0秒
                nowTime = nowTime.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }

            Map<String, String> labelDataMap;
            Map<String, Object> customerSingleMap;
            for (Map<String, Object> map : labelDataList) {
                if (map != null && map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                    labelDataMap = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                    if (labelDataMap != null) {
                        for (Map.Entry<String, String> labelDataMapKey : labelDataMap.entrySet()) {
                            // 只处理单选类型的自建属性信息
                            if (customerSingleSelectLabelName.get(labelDataMapKey.getKey()) != null) {
                                customerSingleMap = new HashMap<>();
                                customerSingleMap.put("label_id", labelDataMapKey.getKey());
                                customerSingleMap.put("label_name", customerSingleSelectLabelName.get(labelDataMapKey.getKey()));
                                customerSingleMap.put("option_value", labelDataMapKey.getValue());
                                singleSelectList.add(customerSingleMap);
                            }
                        }
                    }
                }
            }
            Map<String, Object> labelData = new HashMap<>(16);
            Map<String, Long> labelCountData = new HashMap<>(16);
            // 自建属性选项名称
            Map<String, Object> labelOptionName = new HashMap<>();
            Map<String, Object> labelNameMap = new HashMap<>();
            String labelDataMapKey, labelOptionDataMapKey;

            Map<String, Long> optionCallMap;
            for (Map<String, Object> map : singleSelectList) {
                labelDataMapKey = map.get("label_id") + "";
                labelNameMap.put(labelDataMapKey, map.get("label_name"));

                labelOptionDataMapKey = map.get("label_id") + "" + map.get("option_value");
                labelOptionName.put(labelOptionDataMapKey, map.get("option_value"));

                if (labelData.get(labelDataMapKey) != null) {
                    optionCallMap = (Map<String, Long>) labelData.get(labelDataMapKey);
                    if (optionCallMap.get(labelOptionDataMapKey) != null) {
                        optionCallMap.put(labelOptionDataMapKey, optionCallMap.get(labelOptionDataMapKey) + 1L);
                    } else {
                        optionCallMap.put(labelOptionDataMapKey, 1L);
                    }
                    if (labelCountData.get(labelDataMapKey) != null) {
                        labelCountData.put(labelDataMapKey, labelCountData.get(labelDataMapKey) + 1L);
                    } else {
                        labelCountData.put(labelDataMapKey, 1L);
                    }
                } else {
                    optionCallMap = new HashMap<>();
                    optionCallMap.put(labelOptionDataMapKey, 1L);
                    labelCountData.put(labelDataMapKey, 1L);
                }
                labelData.put(labelDataMapKey, optionCallMap);
            }

            Map<String, Object> labelOptionMap;
            String labelPCheckPercent = null;
            for (Map.Entry<String, Object> map : labelData.entrySet()) {
                data = new ArrayList<>();
                headers = new ArrayList<>();

                head = new ArrayList<>();
                head.add(String.valueOf(labelNameMap.get(map.getKey())));
                headers.add(head);

                head = new ArrayList<>();
                head.add("数量");
                headers.add(head);

                head = new ArrayList<>();
                head.add("占比");
                headers.add(head);

                labelOptionMap = (Map<String, Object>) map.getValue();
                for (Map.Entry<String, Object> labelOption : labelOptionMap.entrySet()) {
                    // 处理比例
                    if (labelOption.getValue() != null && labelCountData.get(map.getKey()) != null) {
                        //labelPCheckPercent = numberFormat.format(Double.parseDouble(String.valueOf(labelOption.getValue())) / labelCountData.get(map.getKey()) * 100);
                        labelPCheckPercent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(String.valueOf(labelOption.getValue())), labelCountData.get(map.getKey()));
                    } else {
                        log.error("参数异常labelOption:" + labelOption + ",labelCountData:" + labelCountData + ",map:" + map);
                    }
                    columnList = new ArrayList<>();
                    columnList.add(String.valueOf(labelOptionName.get(labelOption.getKey())));
                    columnList.add(String.valueOf(labelOption.getValue()));
                    columnList.add(String.valueOf(labelPCheckPercent));
                    data.add(columnList);
                }
                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                sheet.setSheetName(String.valueOf(labelNameMap.get(map.getKey())));
                writer.write0(data, sheet);
            }
            writer.finish();
        } catch (Exception e) {
            log.error("导出客户群:" + customerGroupId + "统计分析异常,", e);
        }
    }

    /**
     * 导出客群通话统计数据excel
     *
     * @param _rule_          =callAmount导出表头带通话费用列
     * @param timeType
     * @param customerGroupId
     * @param userQueryParam
     * @param startTime
     * @param endTime
     * @param response
     */
    public void exportCustomerGroupCallData(String _rule_, int timeType, String customerGroupId, UserQueryParam
            userQueryParam, String startTime, String endTime, HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()) {
            if (StringUtil.isEmpty(customerGroupId)) {
                log.warn("customerGroupId参数异常");
                return;
            }
            int taskType = -1;
            CustomGroup customGroup = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
            if (customGroup == null) {
                log.warn("未查询到指定客户群:" + customerGroupId);
                return;
            }
            userQueryParam.setCustId(customGroup.getCustId());
            if (customGroup.getTaskType() != null) {
                taskType = customGroup.getTaskType();
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    log.warn("查询客户群统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (customGroup.getTaskCreateTime() != null && customGroup.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskEndTime().getTime());
                } else if (customGroup.getCreateTime() != null) {
                    localEndDateTime = LocalDateTime.now();
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getCreateTime().getTime());
                    log.warn("营销任务创建和结束为空默认取客户群的创建时间:" + customGroup.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    log.warn("查询客户群统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + customGroup.getTaskCreateTime() + ",taskEndTime:" + customGroup.getTaskEndTime());
                    return;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ?");

            List<Map<String, Object>> callLogList;
            // 通话记录查询用户权限
            Set voiveUserIds = new HashSet();
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiveUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                        voiveUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                    voiveUserIds.add(userQueryParam.getUserId());
                }
            }
            callLogList = this.customerGroupListDao.sqlQuery(sqlSb.toString(), localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                    localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")), customerGroupId);

            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L;
            if (callLogList.size() > 0) {
                callSum = NumberConvertUtil.parseLong(callLogList.get(0).get("caller_sum"));
                calledSum = NumberConvertUtil.parseLong(callLogList.get(0).get("called_sum"));
                busySum = NumberConvertUtil.parseLong(callLogList.get(0).get("busy_sum"));
                noServiceSum = NumberConvertUtil.parseLong(callLogList.get(0).get("no_service_area_sum"));
                phoneOverdueSum = NumberConvertUtil.parseLong(callLogList.get(0).get("phone_overdue_sum"));
                phoneShutdownSum = NumberConvertUtil.parseLong(callLogList.get(0).get("phone_shutdown_sum"));
                spacePhoneSum = NumberConvertUtil.parseLong(callLogList.get(0).get("space_phone_sum"));
                otherSum = NumberConvertUtil.parseLong(callLogList.get(0).get("other_sum"));
                failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                successSum = NumberConvertUtil.parseLong(callLogList.get(0).get("order_sum"));
            }

            String fileName = "客户群统计数据-" + customerGroupId + "-" + System.currentTimeMillis();
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
            int sheetNum = 1;
            BigDecimal bigDecimal, decimal;
            long callDurationTime = 1;
            List<List<String>> data, headers;
            List<String> columnList, head;

            data = new ArrayList<>();

            headers = new ArrayList<>();
            head = new ArrayList<>();
            head.add("呼叫量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("未通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通率");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功率");
            headers.add(head);
            //构造数据
            columnList = new ArrayList<>();
            //呼叫量
            columnList.add(String.valueOf(callSum));
            //接通量
            columnList.add(String.valueOf(calledSum));
            //未通量
            columnList.add(String.valueOf(failSum));
            //成功量
            columnList.add(String.valueOf(successSum));
            //接通率
            if (NumberConvertUtil.parseLong(callSum) == 0) {
                columnList.add(String.valueOf(0));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(calledSum, callSum)));
            }
            //成功率
            if (NumberConvertUtil.parseLong(calledSum) == 0) {
                columnList.add(String.valueOf(0));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(successSum, calledSum)));
            }

            data.add(columnList);

            Sheet sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("外呼数据统计");
            writer.write0(data, sheet);

            // 构造未接通号码统计
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("未通总量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("用户忙");
            headers.add(head);

            head = new ArrayList<>();
            head.add("不在服务区");
            headers.add(head);

            head = new ArrayList<>();
            head.add("停机");
            headers.add(head);

            head = new ArrayList<>();
            head.add("关机");
            headers.add(head);

            head = new ArrayList<>();
            head.add("空号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("其他");
            headers.add(head);
            //构造数量数据
            columnList = new ArrayList<>();
            columnList.add(String.valueOf(failSum));
            columnList.add(String.valueOf(busySum));
            columnList.add(String.valueOf(noServiceSum));
            columnList.add(String.valueOf(phoneOverdueSum));
            columnList.add(String.valueOf(phoneShutdownSum));
            columnList.add(String.valueOf(spacePhoneSum));
            columnList.add(String.valueOf(otherSum));
            data.add(columnList);
            //构造占比数据
            columnList = new ArrayList<>();
            if (NumberConvertUtil.parseLong(callSum) == 0) {
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
                columnList.add(String.valueOf("0"));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(failSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(busySum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(noServiceSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(phoneOverdueSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(phoneShutdownSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(spacePhoneSum, callSum)));
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(otherSum, callSum)));
            }

            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("未接通号码统计");
            writer.write0(data, sheet);

            //　构造用户呼叫列表数据
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("员工");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功率");
            headers.add(head);

            head = new ArrayList<>();
            head.add("总通话时长");
            headers.add(head);

            head = new ArrayList<>();
            head.add("平均通话时长");
            headers.add(head);

            // 通话费用导出
            if ("callAmount".equals(_rule_)) {
                head = new ArrayList<>();
                head.add("通话费用(元)");
                headers.add(head);
            }

            // 查询用户呼叫数
            sqlSb = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    "IFNULL(SUM(called_duration),0) called_duration, IFNULL(SUM(call_amount),0)/1000 callAmount, IFNULL(SUM(call_prod_amount),0)/1000 callProdAmount FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ?");
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }
            }
            sqlSb.append(" GROUP BY user_id ");
            List<Map<String, Object>> list = this.customerGroupListDao.sqlQuery(sqlSb.toString(), startTime,
                    endTime, customerGroupId);

            Map<String, Object> m;
            for (int i = 0; i < list.size(); i++) {
                m = list.get(i);
                m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                columnList = new ArrayList<>();
                columnList.add(String.valueOf(m.get("userName")));
                columnList.add(String.valueOf(m.get("called_sum")));
                columnList.add(String.valueOf(m.get("order_sum")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add(String.valueOf(0));
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(m.get("order_sum")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }
                columnList.add(String.valueOf(m.get("called_duration")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add(String.valueOf(0));
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.divNumber(NumberConvertUtil.parseLong(m.get("called_duration")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }
                columnList.add(String.valueOf(m.get("callAmount")));
                data.add(columnList);

            }
            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("员工统计");
            writer.write0(data, sheet);

            Map<String, Object> customerSingleSelectLabelName = new HashMap<>();
            // 处理标签ID或者标签明显
            List<Map<String, Object>> labelNames = customerGroupListDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", userQueryParam.getCustId());
            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    // 只存储单选类型的自建属性名称
                    if ("2".equals(String.valueOf(map.get("type")))) {
                        // 初始化所有自建属性名称
                        customerSingleSelectLabelName.put(String.valueOf(map.get("label_id")), map.get("label_name"));
                    }
                }
            }
            final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
            // 处理自定义标签统计,只查询自建属性为单选类型的
            List<Map<String, Object>> singleSelectList = new ArrayList<>();

            List<Map<String, Object>> labelDataList = new ArrayList<>();
            List<Map<String, Object>> labelDataListTmp;
            // 处理跨月查询逻辑
            for (LocalDateTime nowTime = localStartDateTime; nowTime.isBefore(localEndDateTime); ) {
                sqlSb.setLength(0);
                sqlSb.append("SELECT t.id, t.super_data FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " t ")
                        .append(" JOIN " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowTime.format(YYYYMM) + " log ON t.id = log.superid")
                        .append(" WHERE t.super_data IS NOT NULL ")
                        .append(" AND log.customer_group_id = ? AND log.create_time >= ?  AND log.create_time <= ? AND log.status = 1001 ");
                if ("-1".equals(userQueryParam.getCustId())) {
                    sqlSb.append(" AND log.cust_id IS NOT NULL ");
                } else {
                    sqlSb.append(" AND log.cust_id = '" + userQueryParam.getCustId() + "' ");
                }
                if (voiveUserIds.size() > 0) {
                    sqlSb.append(" AND log.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(voiveUserIds) + ") ");
                }
                labelDataListTmp = customerGroupListDao.sqlQuery(sqlSb.toString(), customerGroupId, startTime, endTime);
                // 分月数据汇总到总数据中
                if (labelDataListTmp.size() > 0) {
                    labelDataList.addAll(labelDataListTmp);
                }
                //　当前时间处理到下月1号0点0分0秒
                nowTime = nowTime.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }

            Map<String, String> labelDataMap;
            Map<String, Object> customerSingleMap;
            for (Map<String, Object> map : labelDataList) {
                if (map != null && map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                    labelDataMap = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                    if (labelDataMap != null) {
                        for (Map.Entry<String, String> labelDataMapKey : labelDataMap.entrySet()) {
                            // 只处理单选类型的自建属性信息
                            if (customerSingleSelectLabelName.get(labelDataMapKey.getKey()) != null) {
                                customerSingleMap = new HashMap<>();
                                customerSingleMap.put("label_id", labelDataMapKey.getKey());
                                customerSingleMap.put("label_name", customerSingleSelectLabelName.get(labelDataMapKey.getKey()));
                                customerSingleMap.put("option_value", labelDataMapKey.getValue());
                                singleSelectList.add(customerSingleMap);
                            }
                        }
                    }
                }
            }
            Map<String, Object> labelData = new HashMap<>(16);
            Map<String, Long> labelCountData = new HashMap<>(16);
            // 自建属性选项名称
            Map<String, Object> labelOptionName = new HashMap<>();
            Map<String, Object> labelNameMap = new HashMap<>();
            String labelDataMapKey, labelOptionDataMapKey;

            Map<String, Long> optionCallMap;
            for (Map<String, Object> map : singleSelectList) {
                labelDataMapKey = map.get("label_id") + "";
                labelNameMap.put(labelDataMapKey, map.get("label_name"));

                labelOptionDataMapKey = map.get("label_id") + "" + map.get("option_value");
                labelOptionName.put(labelOptionDataMapKey, map.get("option_value"));

                if (labelData.get(labelDataMapKey) != null) {
                    optionCallMap = (Map<String, Long>) labelData.get(labelDataMapKey);
                    if (optionCallMap.get(labelOptionDataMapKey) != null) {
                        optionCallMap.put(labelOptionDataMapKey, optionCallMap.get(labelOptionDataMapKey) + 1L);
                    } else {
                        optionCallMap.put(labelOptionDataMapKey, 1L);
                    }
                    if (labelCountData.get(labelDataMapKey) != null) {
                        labelCountData.put(labelDataMapKey, labelCountData.get(labelDataMapKey) + 1L);
                    } else {
                        labelCountData.put(labelDataMapKey, 1L);
                    }
                } else {
                    optionCallMap = new HashMap<>();
                    optionCallMap.put(labelOptionDataMapKey, 1L);
                    labelCountData.put(labelDataMapKey, 1L);
                }
                labelData.put(labelDataMapKey, optionCallMap);
            }

            Map<String, Object> labelOptionMap;
            String labelPCheckPercent = null;
            for (Map.Entry<String, Object> map : labelData.entrySet()) {
                data = new ArrayList<>();
                headers = new ArrayList<>();

                head = new ArrayList<>();
                head.add(String.valueOf(labelNameMap.get(map.getKey())));
                headers.add(head);

                head = new ArrayList<>();
                head.add("数量");
                headers.add(head);

                head = new ArrayList<>();
                head.add("占比");
                headers.add(head);

                labelOptionMap = (Map<String, Object>) map.getValue();
                for (Map.Entry<String, Object> labelOption : labelOptionMap.entrySet()) {
                    // 处理比例
                    if (labelOption.getValue() != null && labelCountData.get(map.getKey()) != null) {
                        //labelPCheckPercent = numberFormat.format(Double.parseDouble(String.valueOf(labelOption.getValue())) / labelCountData.get(map.getKey()) * 100);
                        labelPCheckPercent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(String.valueOf(labelOption.getValue())), labelCountData.get(map.getKey()));
                    } else {
                        log.error("参数异常labelOption:" + labelOption + ",labelCountData:" + labelCountData + ",map:" + map);
                    }
                    columnList = new ArrayList<>();
                    columnList.add(String.valueOf(labelOptionName.get(labelOption.getKey())));
                    columnList.add(String.valueOf(labelOption.getValue()));
                    columnList.add(String.valueOf(labelPCheckPercent));
                    data.add(columnList);
                }
                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                sheet.setSheetName(String.valueOf(labelNameMap.get(map.getKey())));
                writer.write0(data, sheet);
            }
            writer.finish();
        } catch (Exception e) {
            log.error("导出客户群:" + customerGroupId + "统计分析异常,", e);
        }
    }

    /**
     * 更改自动外呼任务ID
     *
     * @param customerId
     * @param groupId
     * @param taskId
     * @return
     */
    public int updateCustomerGroupTaskId(String customerId, String groupId, String taskId) {
        CustomGroup customGroup = customGroupDao.get(Integer.parseInt(groupId));
        log.info("更改任务ID,customerId:" + customerId + ",groupId:" + groupId + ",taskId:" + taskId);
        if (customGroup != null) {
            if (StringUtil.isNotEmpty(customGroup.getCustId()) && customGroup.getCustId().equals(customerId)) {
                customGroup.setTaskId(taskId);
                customGroupDao.saveOrUpdate(customGroup);
                return 1;
            }
        }
        return 0;
    }

    /**
     * 更新自动外呼任务ID和第三方供号置为0
     *
     * @param customerId
     * @param groupId
     * @param taskId
     * @return
     */
    public int updateCustomerGroupTaskIdAndTaskPhoneIndex(String customerId, String groupId, String taskId) {
        CustomGroup customGroup = customGroupDao.get(Integer.parseInt(groupId));
        log.info("更改任务ID和第三方供号置为0,customerId:" + customerId + ",groupId:" + groupId + ",taskId:" + taskId);
        if (customGroup != null) {
            if (StringUtil.isNotEmpty(customGroup.getCustId()) && customGroup.getCustId().equals(customerId)) {
                customGroup.setTaskId(taskId);
                customGroup.setTaskPhoneIndex(0);
                customGroupDao.saveOrUpdate(customGroup);
                return 1;
            }
        }
        return 0;
    }

    /**
     * 创建营销任务
     *
     * @param customerId
     * @param id
     * @param taskId
     * @param taskType
     * @param userGroupId
     * @param taskEndTime
     * @return
     */
    public int createMarketTask(String customerId, String id, String taskId, int taskType, String userGroupId,
                                long taskEndTime) {
        CustomGroup customGroup = customGroupDao.get(Integer.parseInt(id));
        if (customGroup != null) {
            if (StringUtil.isNotEmpty(customGroup.getCustId()) && customGroup.getCustId().equals(customerId)) {
                customGroup.setTaskId(taskId);
                customGroup.setTaskPhoneIndex(0);
                customGroup.setTaskType(taskType);
                customGroup.setTaskEndTime(new Timestamp(taskEndTime));
                customGroup.setTaskCreateTime(new Timestamp(System.currentTimeMillis()));

                customGroupDao.saveOrUpdate(customGroup);
                return 1;
            } else {
                log.warn("当前客户群不属于当前登录客户,客户群:" + id + ",客户群所属客户:" + customGroup.getCustId() + ",登录客户:" + customerId);
            }
        } else {
            log.warn("客户群不存在:" + id);
        }
        return 0;
    }

    /**
     * 更新客户群的项目ID
     *
     * @param customerId
     * @param id
     * @param marketProjectId
     * @return
     */
    public int updateCustomeGroupMarketProject(String customerId, int id, Integer marketProjectId) {
        CustomGroup customGroup = customGroupDao.get(id);
        if (customGroup != null) {
            if (marketProjectId != null) {
                customGroup.setMarketProjectId(marketProjectId);
                customGroupDao.saveOrUpdate(customGroup);
                return 1;
            } else {
                log.warn("新客户群的项目ID参数marketProjectId为空");
            }
        } else {
            log.warn("新客户群的项目ID客户群不存在:" + id);
        }
        return 0;
    }

    public String updateCustomerGroupState(String id) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        //关闭订单状态
        String sql = "UPDATE t_order set order_state=0 where order_id=(SELECT order_id from customer_group where id =?)";
        int flag = customGroupDao.executeUpdateSQL(sql, new Object[]{id});
        //关闭客户群状态
        String sql2 = "UPDATE customer_group set `STATUS`=0 where id=?";
        int flag2 = customGroupDao.executeUpdateSQL(sql2, new Object[]{id});
        if (flag == 1 && flag2 == 1) {
            map.put("code", 0);
            map.put("message", "成功");
        } else {
            map.put("code", 1);
            map.put("message", "失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 更改客户群状态
     *
     * @param id
     * @param status 0.失效  1.处理完成 2.待确认 3-处理中 4-删除状态
     * @return
     */
    public int updateCustomerGroupState(String custId, String id, int status) {
        if (StringUtil.isEmpty(custId)) {
            log.warn("更改客户群状态custId为空,custId:" + custId);
            return 0;
        }

        CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(id));
        if (cg == null) {
            log.warn("更改客户群状态未查询到客户群,id:" + id);
            return 0;
        }
        if (!Objects.equals(cg.getCustId(), custId)) {
            log.warn("更改客户群状态客户群不属于该客户,id:" + id + ",custId:" + custId);
            return 0;
        }

        //关闭订单状态
        String sql = "UPDATE t_order set order_state=0 where order_id=(SELECT order_id from customer_group where id =?)";
        int flag = customGroupDao.executeUpdateSQL(sql, new Object[]{id});
        //客户群逻辑状态
        String sql2 = "UPDATE customer_group set `STATUS`=? where id=?";
        int flag2 = customGroupDao.executeUpdateSQL(sql2, status, id);
        if (flag == 1 && flag2 == 1) {
            return 1;
        }
        return 0;
    }


    public String getCustomGroupPhoneListByTaskId(String taskId, Long pageSize) {
        List<Map<String, Object>> customerList = this.customGroupDao.sqlQuery("SELECT id,cust_id,task_phone_index FROM customer_group WHERE task_id = ?", taskId);
        if (customerList.size() == 0) {
            log.error("新方自动外呼taskId:" + taskId + "未查询到对应的客户群");
            return "新方自动外呼taskId:" + taskId + "未查询到对应外呼号码";
        }
        String customerGroupId = String.valueOf(customerList.get(0).get("id"));
        String custId = String.valueOf(customerList.get(0).get("cust_id"));
        long taskPhoneIndex = 0;
        if (StringUtil.isNotEmpty(String.valueOf(customerList.get(0).get("task_phone_index")))
                && !"null".equals(String.valueOf(customerList.get(0).get("task_phone_index")))) {
            taskPhoneIndex = Long.parseLong(String.valueOf(customerList.get(0).get("task_phone_index")));
        } else {
            log.error("新方自动外呼customerGroupId:" + customerGroupId + "的task_phone_index字段为空,设置为:" + taskPhoneIndex);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(" select custG.id ");
        sb.append("  from t_customer_group_list_" + customerGroupId + " custG ");
        sb.append(" where 1=1 ");

        sb.append(" ORDER BY custG.n_id ASC ");
        if (pageSize != null && !"".equals(pageSize)) {
            sb.append("  LIMIT " + taskPhoneIndex + "," + pageSize);
        }
        List<Map<String, Object>> phones = null;
        List<String> phoneList = new ArrayList<>();
        StringBuffer content = new StringBuffer();
        try {
            phones = this.customGroupDao.sqlQuery(sb.toString());
            String u = "";
            if (phones != null && phones.size() > 0) {
                for (Map<String, Object> phone : phones) {
                    if (phone != null && phone.get("id") != null) {
                        u = phoneService.getPhoneBySuperId(String.valueOf(phone.get("id")));
                        //保存客群和手机号对应的身份ID到redis
                        phoneService.setCGroupDataToRedis(customerGroupId, String.valueOf(phone.get("id")), u);
                        phoneList.add(u);
                        content.append(0)
                                .append(",")
                                .append(u)
                                .append(",")
                                .append(customerGroupId + "_" + custId + "_" + taskPhoneIndex + "_" + phone.get("id"))
                                .append("\r\n");
                    }
                }
            } else {
                //标识结束
                content.append("END")
                        .append("\r\n");
            }

        } catch (DataAccessException e) {
            log.error("新方自动外呼查询客户群手机号失败,", e);
        }
        if (phoneList.size() > 0) {
            taskPhoneIndex += phoneList.size();
            int code = customGroupDao.executeUpdateSQL("UPDATE customer_group SET task_phone_index = ? WHERE id = ?", taskPhoneIndex, customerGroupId);
            if (code > 0) {
                log.info("新方自动外呼customerGroupId:" + customerGroupId + "更新号码最后index成功,task_phone_index:" + taskPhoneIndex + "返回码:" + code);
            } else {
                log.error("新方自动外呼customerGroupId:" + customerGroupId + "更新号码最后index失败,task_phone_index:" + taskPhoneIndex + "返回码:" + code);
            }
        } else {
            content.setLength(0);
            //标识结束
            content.append("END")
                    .append("\r\n");
        }
        return content.toString();
    }


    /**
     * 获取营销任务列表
     *
     * @param loginUser
     * @param param
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> listMarketTask(LoginUser loginUser, CustomerGrpOrdParam param,
                                                    boolean taskTypeIsNotNull) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("total", 0);
        map.put("list", new ArrayList<>());

        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT IFNULL (t2.NAME,'') AS groupName, t2.id AS groupId, ")
                .append(" t2.user_count AS quantity,")
                .append(" t2.task_create_time taskCreateTime, t2.task_end_time taskEndTime, t2.user_group_id userGroupId, t2.task_type taskType, t2.task_id taskId")
                .append(" FROM  t_order t1")
                .append(" RIGHT JOIN customer_group t2 ON t1.order_id = t2.order_id where 1=1 AND t1.order_type=1")
                .append(" AND t1.cust_id ='").append(StringEscapeUtils.escapeSql(loginUser.getCustId())).append("'")
                .append(" AND t2.`status` = 1 ");
        if (StringUtil.isNotEmpty(param.getGroupName())) {
            sql.append(" AND t2.name like '%").append(StringEscapeUtils.escapeSql(param.getGroupName())).append("%'");
        }
        if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
            sql.append(" and t2.id=").append(param.getGroupId());
        }
        // 任务类型
        if (StringUtil.isNotEmpty(param.getTaskType())) {
            sql.append(" and t2.task_type=").append(StringEscapeUtils.escapeSql(param.getTaskType()));
        }
        // 用户群组ID
        /*if (StringUtil.isNotEmpty(param.getUserGroupId())) {
            sql.append(" and t2.user_group_id='").append(StringEscapeUtils.escapeSql(param.getUserGroupId()) + "'");
        }*/
        if (StringUtil.isNotEmpty(param.getStartTime()) || StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                    .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }
        //普通用户只查询创建好的任务
        if ("2".equals(loginUser.getUserType())) {
            sql.append(" and t2.task_type is not null ");
        }
        if (taskTypeIsNotNull) {
            sql.append(" and t2.task_type is not null ");
        }
        // 坐席或者组长区分权限,只查询组负责的数据
        /*if ("2".equals(loginUser.getUserType())) {
            CustomerUserGroupRelDTO customerUserGroupRelDTO = customerUserDao.getCustomerUserGroupByUserId(loginUser.getId());
            if (customerUserGroupRelDTO != null) {
                sql.append(" AND FIND_IN_SET('" + StringEscapeUtils.escapeSql(customerUserGroupRelDTO.getGroupId()) + "', t2.user_group_id) ");
            } else {
                log.warn("用户:" + loginUser.getId() + "未指定用户群组或者组已经被删除");
                result.add(map);
                return result;
            }

        }*/

        sql.append(" ORDER BY if (t2.task_type IS NOT NULL AND t2.task_id IS NOT NULL,0,1), t2.task_create_time DESC, t2.create_time DESC ");
        map.put("total", customGroupDao.getSQLQuery(sql.toString()).list().size());
        List<Map<String, Object>> list = customGroupDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list();
        if (list != null && list.size() > 0) {
            CustomerUserGroup customerUserGroup;
            List<Map<String, Object>> unAssignedUsers = null;
            StringBuffer unAssignedUsersSql = new StringBuffer();
            StringBuffer userGroupId, userGroupName;
            String[] userGroupIds;
            for (Map<String, Object> model : list) {
                if (model != null) {
                    // 处理任务类型未配置
                    if (model.get("taskType") == null) {
                        model.put("taskType", 0);
                    }
                    // 处理用户群组
                    if (model.get("userGroupId") != null) {
                        userGroupId = new StringBuffer();
                        userGroupName = new StringBuffer();
                        userGroupId.append(String.valueOf(model.get("userGroupId")));
                        if (StringUtil.isNotEmpty(userGroupId.toString())) {
                            userGroupIds = userGroupId.toString().split(",");
                            for (String groupId : userGroupIds) {
                                customerUserGroup = customerUserDao.getCustomerUserGroup(groupId);
                                if (customerUserGroup != null) {
                                    userGroupName.append(customerUserGroup.getName()).append(",");
                                }
                            }
                            model.put("userGroupName", userGroupName.deleteCharAt(userGroupName.length() - 1));
                        } else {
                            model.put("userGroupName", "");
                        }
                    } else {
                        model.put("userGroupName", "分组未配置");
                    }
                }
                // 查询未分配数量
                model.put("unassignedQuantity", 0);
                model.put("callCount", 0);
                unAssignedUsersSql.setLength(0);
                // 手动任务查询呼叫次数和未分配数据
                if (2 == NumberConvertUtil.parseInt(String.valueOf(model.get("taskType")))) {
                    unAssignedUsersSql.append("SELECT count(IF(`status` = 1, `id`, NULL)) AS unassignedQuantity, IFNULL(SUM(call_count), 0) AS callCount FROM t_customer_group_list_" + model.get("groupId"));
                    try {
                        unAssignedUsers = customerUserDao.getSQLQuery(unAssignedUsersSql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
                        if (unAssignedUsers != null && unAssignedUsers.size() > 0) {
                            model.put("unassignedQuantity", unAssignedUsers.get(0).get("unassignedQuantity"));
                            model.put("callCount", unAssignedUsers.get(0).get("callCount"));
                        }
                    } catch (Exception e) {
                        log.error("查询客户群未分配数量和呼叫数量失败,", e);
                        model.put("unassignedQuantity", 0);
                        model.put("callCount", 0);
                    }
                } else {// 自动和机器人任务查询通话次数
                    unAssignedUsersSql.append("SELECT IFNULL(SUM(call_count), 0) callCount FROM t_customer_group_list_" + model.get("groupId"));
                    try {
                        unAssignedUsers = customerUserDao.getSQLQuery(unAssignedUsersSql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
                        if (unAssignedUsers != null && unAssignedUsers.size() > 0) {
                            model.put("callCount", unAssignedUsers.get(0).get("callCount"));
                        }
                    } catch (Exception e) {
                        log.error("查询客户群未分配数量和呼叫数量失败,", e);
                        model.put("callCount", 0);
                    }
                }
            }
        }

        map.put("list", list);
        result.add(map);
        return result;
    }

    public List<Map<String, Object>> adminListMarketTask(CustomerGrpOrdParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("total", 0);
        map.put("list", new ArrayList<>());

        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT IFNULL (t2.NAME,'') AS groupName, t2.id AS groupId, t2.cust_id AS custId, ")
                .append(" t2.user_count AS quantity,")
                .append(" t2.task_create_time taskCreateTime, t2.task_end_time taskEndTime, t2.user_group_id userGroupId, t2.task_type taskType, t2.task_id taskId")
                .append(" FROM  t_order t1")
                .append(" RIGHT JOIN customer_group t2 ON t1.order_id = t2.order_id where 1=1 AND t1.order_type=1")
                .append(" AND t2.`status` = 1 ");
        if (StringUtil.isNotEmpty(param.getGroupName())) {
            sql.append(" AND t2.name like '%").append(StringEscapeUtils.escapeSql(param.getGroupName())).append("%'");
        }
        if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
            sql.append(" and t2.id=").append(param.getGroupId());
        }
        // 任务类型
        if (StringUtil.isNotEmpty(param.getTaskType())) {
            sql.append(" and t2.task_type=").append(StringEscapeUtils.escapeSql(param.getTaskType()));
        }
        // 用户名搜索
        if (StringUtil.isNotEmpty(param.getCustUserName())) {
            CustomerUser u = customerUserDao.getCustomerUserByLoginName(param.getCustUserName());
            if (u == null) {
                map.put("list", new ArrayList<>());
                map.put("total", 0);
                result.add(map);
                return result;
            }
            sql.append(" and t2.cust_id='").append(u.getCust_id() + "'");
        }
        // 企业名称搜索
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            sql.append(" and t2.cust_id IN(SELECT cust_id FROM t_customer " +
                    " WHERE enterprise_name LIKE '%" + StringEscapeUtils.escapeSql(param.getEnterpriseName()) + "%')");
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) || StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                    .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }

        sql.append("  ORDER BY t2.create_time DESC ");
        map.put("total", customGroupDao.getSQLQuery(sql.toString()).list().size());
        List<Map<String, Object>> list = customGroupDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list();
        if (list != null && list.size() > 0) {
            CustomerUserGroup customerUserGroup;
            CustomerUser cu;
            List<Map<String, Object>> unAssignedUsers;
            StringBuffer userGroupId, userGroupName;
            String[] userGroupIds;
            for (Map<String, Object> model : list) {
                // 查询企业管理员名称
                cu = customerUserDao.getCustomerAdminUser(String.valueOf(model.get("custId")));
                if (cu != null) {
                    model.put("userName", cu.getAccount());
                } else {
                    model.put("userName", "");
                }
                // 企业名称
                model.put("enterpriseName", customerDao.getEnterpriseName(String.valueOf(model.get("custId"))));
            }
        }

        map.put("list", list);
        result.add(map);
        return result;
    }


    public void exportCustomerGroupRecordFile(HttpServletResponse response, LoginUser loginUser, String
            invitationLabelId, String invitationLabelName,
                                              String invitationLabelValue, String startTime, String endTime, String customerGroupId) {
        OutputStream outputStream = null;
        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        FileOutputStream fos1;
        byte[] buff;
        Map<String, String> msg = new HashMap<>();
        try {
            outputStream = response.getOutputStream();

            if (StringUtil.isEmpty(customerGroupId)) {
                msg.put("msg", "参数异常");
                return;
            }

            CustomGroup customGroup = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
            if (customGroup == null) {
                msg.put("msg", "营销任务不存在");
                return;
            }
            if (!customGroup.getCustId().equals(loginUser.getCustId())) {
                msg.put("msg", "权限不足");
                return;
            }

            // 处理时间
            if (marketRecordLogExportTime >= 0 && (System.currentTimeMillis() - marketRecordLogExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                return;
            }
            marketRecordLogExportTime = System.currentTimeMillis();
            // 处理时间
            String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMATTER);
            String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DATE_TIME_FORMATTER);
            if (StringUtil.isNotEmpty(startTime)) {
                startTimeStr = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTimeStr = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }

            // 处理自建属性
            List<Map<String, Object>> labelNames = customGroupDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", loginUser.getCustId());

            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    if (StringUtil.isEmpty(invitationLabelId)) {
                        if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(String.valueOf(map.get("label_name")))) {
                            invitationLabelId = String.valueOf(map.get("label_id"));
                            break;
                        }
                    }
                }
            }
            if (StringUtil.isNotEmpty(invitationLabelId)) {
                // 根据客户群ID查询用户数据superIds
                Set<String> superIdSets = new HashSet<>();

                // 获取邀约成功,拨打电话成功用户的通话记录
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT voice.user_id, voice.customer_group_id, voice.superid, callback.recordurl, ")
                        .append(" voice.create_time, voice.callSid, t_super_label.option_value FROM t_touch_voice_log voice ")
                        .append(" LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid ")
                        .append(" JOIN t_super_label ON t_super_label.super_id = voice.superid AND t_super_label.label_id = ? AND t_super_label.cust_group_id = voice.customer_group_id ")
                        .append(" WHERE voice.cust_id = ? AND voice.type_code = 1 AND voice.status = 1001 ")
                        .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                        .append(" AND FIND_IN_SET(t_super_label.option_value, ?)")
                        .append(" AND voice.customer_group_id  = ? ");

                if ("2".equals(loginUser.getUserType())) {
                    // 组长查组员列表
                    if ("1".equals(loginUser.getUserGroupRole())) {
                        List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                        Set<String> userIds = new HashSet<>();
                        if (customerUserDTOList.size() > 0) {
                            for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                                userIds.add(customerUserDTO.getId());
                            }
                            // 分配责任人操作
                            if (userIds.size() > 0) {
                                sql.append(" AND voice.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                            }
                        }
                    } else {
                        sql.append(" AND voice.user_id = '" + loginUser.getId() + "'");
                    }
                }

                List<Map<String, Object>> callLogList = customGroupDao.sqlQuery(sql.toString(), invitationLabelId, loginUser.getCustId(), startTimeStr, endTimeStr, invitationLabelValue, customerGroupId);
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("superid") != null) {
                            superIdSets.add(String.valueOf(map.get("superid")));
                        }
                    }
                }

                // 身份ID不为空
                if (superIdSets.size() > 0) {

                    // 查询手机号
                    Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);
                    /*StringBuilder phoneSql = new StringBuilder("SELECT id, phone FROM u WHERE id IN ( " + SqlAppendUtil.sqlAppendWhereIn(superIdSets) + ")");
                    List<Map<String, Object>> phoneList = customGroupDao.sqlQuery(phoneSql.toString());
                    for (Map<String, Object> map : phoneList) {
                        phoneMap.put(String.valueOf(map.get("id")), map.get("phone"));
                    }*/

                    // 拼装录音文件地址,打包zip供用户下载
                    String recordUrl;
                    String zipName = customerGroupId + ".zip";
                    String sourcePath, destPath, copyFileName, callTime;
                    final String audioPath = ConfigUtil.getInstance().get("audiolocation") + File.separator;
                    String tempZipFileBasePath = System.getProperty("java.io.tmpdir") + File.separator + customerGroupId + File.separator;
                    String tempVoicePath = System.getProperty("java.io.tmpdir") + File.separator + customerGroupId + File.separator + "audio";
                    File tempZipFileBase = new File(tempZipFileBasePath);
                    if (tempZipFileBase.exists()) {
                        FileUtil.deleteDir(tempZipFileBase);
                    }

                    /*File source, dest;
                    for (Map<String, Object> row : callLogList) {
                        // 处理录音文件
                        recordUrl = String.valueOf(row.get("recordurl"));
                        if (StringUtil.isNotEmpty(recordUrl) && recordUrl.lastIndexOf("/") > 0) {
                            recordUrl = recordUrl.substring(recordUrl.lastIndexOf("/"), recordUrl.length());
                            // 复制到临时文件夹
                            sourcePath = audioPath + row.get("user_id") + File.separator + recordUrl;
                            source = new File(sourcePath);
                            // 复制出来的文件名格式为 客户群+手机号
                            copyFileName = customerGroupId + phoneMap.get(row.get("superid")) + recordUrl.substring(recordUrl.lastIndexOf("."));
                            destPath = tempVoicePath + File.separator + copyFileName;
                            dest = new File(destPath);
                            // 如果目标文件已经存在
                            if (dest.exists()) {
                                callTime = String.valueOf(row.get("create_time"));
                                if (StringUtil.isNotEmpty(callTime)) {
                                    callTime = LocalDateTime.parse(callTime.substring(0, callTime.lastIndexOf(".")), DATE_TIME_FORMATTER).format(yyyyMMddHHmmss);
                                }
                                // 存在相同手机号,文件名则加入呼叫时间
                                copyFileName = customerGroupId + phoneMap.get(row.get("superid")) + callTime + recordUrl.substring(recordUrl.lastIndexOf("."));
                                destPath = tempVoicePath + File.separator + copyFileName;
                                dest = new File(destPath);
                            }

                            FileUtil.copyFile(source, dest);
                        }
                    }
                    // 打包生成压缩文件
                    fos1 = new FileOutputStream(new File(tempZipFileBasePath + File.separator + zipName));
                    uploadDowloadFileService.toZip(tempVoicePath, fos1, false);
                    msg.put("msg", "导出已执行");

                    // 向浏览器推送文件进行下载
                    response.setHeader("Content-disposition", "attachment;filename=" + customGroup.getName() + "_" + zipName);
                    bis = new BufferedInputStream(new FileInputStream(tempZipFileBasePath + File.separator + zipName));
                    buff = new byte[bis.available()];
                    bis.read(buff);
                    bis.close();
                    outputStream.write(buff);*/
                } else {
                    msg.put("msg", "客户群下无满足条件的数据");
                    marketRecordLogExportTime = 0;
                }
            } else {
                msg.put("msg", "满足条件的自建属性");
                marketRecordLogExportTime = 0;
            }
        } catch (Exception e) {
            log.error("导出营销录音异常,", e);
            msg.put("msg", "导出营销录音异常");
            msg.put("data", String.valueOf(marketDataExportTime));
            msg.put("invitationLabelName", invitationLabelName);
        } finally {
            try {
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                if (bis != null) {
                    bis.close();
                }
                if (zos != null) {
                    zos.close();
                }
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("导出营销录音异常,", e);
            }
        }
    }

    /**
     * 导出满足自建属性的单个客户群的营销数据
     *
     * @param response
     * @param
     * @param customerGroupId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     */
    public void exportCustomerGroupMarketDataToExcelV3(HttpServletResponse response, LoginUser loginUser, String
            customerGroupId, String invitationLabelId, String invitationLabelName,
                                                       String invitationLabelValue, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        msg.put("customerGroupId", customerGroupId);
        msg.put("invitationLabelId", invitationLabelId);
        msg.put("invitationLabelName", invitationLabelName);
        msg.put("invitationLabelValue", invitationLabelValue);

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            String custId = loginUser.getCustId();
            // 处理管理员权限
            if (StringUtil.isEmpty(loginUser.getCustId())) {
                CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
                if (cg != null) {
                    custId = cg.getCustId();
                } else {
                    msg.put("msg", "未查询到该客户");
                    msg.put("data", String.valueOf(customerGroupDataExportTime));
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    return;
                }
            }

            // 处理时间
            if (customerGroupDataExportTime >= 0 && (System.currentTimeMillis() - customerGroupDataExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            customerGroupDataExportTime = System.currentTimeMillis();
            // 处理时间
            String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMATTER);
            String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DATE_TIME_FORMATTER);
            if (StringUtil.isNotEmpty(startTime)) {
                startTimeStr = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTimeStr = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            List<String> labelIdList = new ArrayList<>();
            List<Map<String, Object>> labelNames = customGroupDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", custId);
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
            headNames.add("身份ID");
            headNames.add("客户群ID");
            headNames.add("手机号");
            headNames.add("归属地");
            headNames.add("操作人");
            headNames.add("时间");
            headNames.add("录音");
            headNames.add("人工审核");
            headNames.add("审核失败原因");

            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    head = new ArrayList<>();
                    if (headNames.contains(String.valueOf(map.get("label_name")))) {
                        head.add(String.valueOf(map.get("label_name")) + map.get("label_id"));
                    } else {
                        head.add(String.valueOf(map.get("label_name")));
                        headNames.add(String.valueOf(map.get("label_name")));
                    }
                    headers.add(head);
                    labelIdList.add(String.valueOf(map.get("label_id")));
                    if (StringUtil.isEmpty(invitationLabelId)) {
                        if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(String.valueOf(map.get("label_name")))) {
                            invitationLabelId = String.valueOf(map.get("label_id"));
                        }
                    }
                }
            }
            head = new ArrayList<>();
            head.add("身份ID");
            headers.add(head);

            head = new ArrayList<>();
            head.add("客户群ID");
            headers.add(head);

            head = new ArrayList<>();
            head.add("手机号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("归属地");
            headers.add(head);

            head = new ArrayList<>();
            head.add("操作人");
            headers.add(head);

            head = new ArrayList<>();
            head.add("时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("录音");
            headers.add(head);

            head = new ArrayList<>();
            head.add("人工审核");
            headers.add(head);

            head = new ArrayList<>();
            head.add("审核失败原因");
            headers.add(head);

            if (StringUtil.isNotEmpty(invitationLabelId)) {
                String nowMonth = DateUtil.getNowMonthToYYYYMM();
                if (StringUtil.isNotEmpty(startTimeStr) && StringUtil.isNotEmpty(endTimeStr)) {
                    nowMonth = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyyMM"));
                }

                // 检查通话记录月表是否存在
                marketResourceDao.createVoiceLogTableNotExist(nowMonth);

                String labelDataLikeValue = "\"" + invitationLabelId + "\":\"" + invitationLabelValue + "\"";
                StringBuffer sql = new StringBuffer();
                // 获取邀约成功,拨打电话成功用户的通话记录
                sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.superid, voice.recordurl, voice.clue_audit_status, voice.clue_audit_reason, ")
                        .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                        .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street ")
                        .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowMonth + " voice ")
                        .append(" JOIN t_customer_group_list_" + customerGroupId + " t ON t.id = voice.superid ")
                        .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? ")
                        .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                        .append(" AND voice.status = 1001 ")
                        .append(" AND t.super_data LIKE '%" + labelDataLikeValue + "%' ");

                if ("2".equals(loginUser.getUserType())) {
                    // 组长查组员列表
                    if ("1".equals(loginUser.getUserGroupRole())) {
                        List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), custId);
                        Set<String> userIds = new HashSet<>();
                        if (customerUserDTOList.size() > 0) {
                            for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                                userIds.add(customerUserDTO.getId());
                            }
                            // 分配责任人操作
                            if (userIds.size() > 0) {
                                sql.append(" AND voice.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                            }
                        }
                    } else {
                        sql.append(" AND voice.user_id = '" + loginUser.getId() + "'");
                    }
                }

                List<Map<String, Object>> callLogList = customGroupDao.sqlQuery(sql.toString(), custId, customerGroupId, startTimeStr, endTimeStr);
                // 有满足条件的营销记录
                if (callLogList.size() > 0) {
                    // 组合拼装为map,方便通过label_id和super_id快速查找数据
                    Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                    Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);

                    // 当前客户群下满足条件的身份ID集合
                    Set<String> superIdSets = new HashSet<>();
                    Set<String> userIdSets = new HashSet<>();
                    Map<String, Object> labelData;
                    for (Map<String, Object> map : callLogList) {
                        if (map != null) {
                            if (map.get("superid") != null) {
                                superIdSets.add(String.valueOf(map.get("superid")));
                            }
                            if (map.get("user_id") != null) {
                                userIdSets.add(String.valueOf(map.get("user_id")));
                            }
                            // 拆解用户勾选的自建属性
                            if (map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                                labelData = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                                if (labelData != null && labelData.size() > 0) {
                                    for (Map.Entry<String, Object> key : labelData.entrySet()) {
                                        invitationCustGroupSuperMap.put(customerGroupId + "_" + map.get("superid"), key.getValue());
                                        invitationSuperLabelMap.put(customerGroupId + "_" + key.getKey() + "_" + map.get("superid"), key.getValue());
                                    }
                                }
                            }
                        }
                    }
                    // 查询用户姓名
                    Map<String, Object> realNameMap = new HashMap<>();
                    if (userIdSets.size() > 0) {
                        List<Map<String, Object>> userList = customGroupDao.sqlQuery("SELECT id, account FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                        for (Map<String, Object> map : userList) {
                            realNameMap.put(String.valueOf(map.get("id")), map.get("account"));
                        }
                    }

                    // 根据superId查询手机号
                    Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);

                    //构造excel返回数据
                    String fileName;
                    List<Map<String, Object>> customerList = customGroupDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", custId);
                    if (customerList.size() > 0) {
                        fileName = customerList.get(0).get("enterprise_name") + "";
                    } else {
                        fileName = "客户";
                    }

                    List<List<String>> data = new ArrayList<>();
                    List<String> columnList;
                    String monthYear = "";
                    for (Map<String, Object> row : callLogList) {
                        if (invitationCustGroupSuperMap.get(customerGroupId + "_" + row.get("superid")) != null) {
                            columnList = new ArrayList<>();
                            for (String header : labelIdList) {
                                if (invitationSuperLabelMap.get(customerGroupId + "_" + header + "_" + row.get("superid")) != null) {
                                    columnList.add(String.valueOf(invitationSuperLabelMap.get(customerGroupId + "_" + header + "_" + row.get("superid"))));
                                } else {
                                    columnList.add("");
                                }
                            }
                            columnList.add(String.valueOf(row.get("superid")));
                            //客户群ID
                            columnList.add(String.valueOf(row.get("customer_group_id")));
                            //手机号
                            columnList.add(PhoneAreaUtil.replacePhone(phoneMap.get(String.valueOf(row.get("superid")))));
                            //归属地
                            columnList.add(String.valueOf(row.get("phonearea")));
                            //姓名
                            columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                            // 通话时间
                            columnList.add(String.valueOf(row.get("create_time")));
                            if (StringUtil.isNotEmpty(String.valueOf(row.get("create_time")))) {
                                monthYear = LocalDateTime.parse(String.valueOf(row.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                            }
                            columnList.add(CallUtil.generateRecordUrlMp3(monthYear, row.get("user_id"), row.get("touchId")));
                            // 通话审核状态
                            columnList.add(CallUtil.getClueAuditStatusName(String.valueOf(row.get("clue_audit_status"))));
                            // 通话审核失败原因
                            columnList.add(String.valueOf(row.get("clue_audit_reason")));
                            data.add(columnList);
                        }
                    }
                    if (data.size() > 0) {
                        fileName += "-营销数据-" + LocalDateTime.now().format(DATE_TIME_FORMATTER);
                        final String fileType = ".xlsx";
                        response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                        response.setContentType("application/vnd.ms-excel;charset=utf-8");
                        ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                        log.info("导出表头:" + JSON.toJSONString(headers));
                        log.info("导出表头:" + JSON.toJSONString(labelIdList));
                        Sheet sheet1 = new Sheet(1, 0);
                        sheet1.setHead(headers);
                        sheet1.setSheetName("营销数据");
                        writer.write0(data, sheet1);
                        writer.finish();
                        customerGroupDataExportTime = 0;
                    } else {
                        msg.put("msg", "客户群下无满足条件的客户数据");
                        msg.put("data", String.valueOf(customerGroupDataExportTime));
                        outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                        customerGroupDataExportTime = 0;
                        return;
                    }
                } else {
                    msg.put("msg", "客户群下无满足条件的客户数据");
                    msg.put("data", String.valueOf(customerGroupDataExportTime));
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    customerGroupDataExportTime = 0;
                    return;
                }
            } else {
                msg.put("msg", "无满足条件的自建属性");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                customerGroupDataExportTime = 0;
                return;
            }
        } catch (Exception e) {
            log.error("导出单个客户群营销数据异常,", e);
            customerGroupDataExportTime = 0;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("导出营销数据异常,", e);
            }
        }
    }

    public void exportCustomerGroupMarketDataToExcelV4(HttpServletResponse response, UserQueryParam
            userQueryParam, String customerGroupId, String invitationLabelId, String invitationLabelName,
                                                       String invitationLabelValue, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        msg.put("customerGroupId", customerGroupId);
        msg.put("invitationLabelId", invitationLabelId);
        msg.put("invitationLabelName", invitationLabelName);
        msg.put("invitationLabelValue", invitationLabelValue);

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            String custId = userQueryParam.getCustId();
            // 处理管理员权限
            if ("-1".equals(custId)) {
                CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
                if (cg != null) {
                    custId = cg.getCustId();
                } else {
                    msg.put("msg", "未查询到该客户");
                    msg.put("data", String.valueOf(customerGroupDataExportTime));
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    return;
                }
            }

            // 处理时间
            if (customerGroupDataExportTime >= 0 && (System.currentTimeMillis() - customerGroupDataExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            customerGroupDataExportTime = System.currentTimeMillis();
            // 处理时间
            String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMATTER);
            String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DATE_TIME_FORMATTER);
            if (StringUtil.isNotEmpty(startTime)) {
                startTimeStr = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTimeStr = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            List<String> labelIdList = new ArrayList<>();
            List<Map<String, Object>> labelNames = customGroupDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", custId);
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
            headNames.add("身份ID");
            headNames.add("客户群ID");
            headNames.add("手机号");
            headNames.add("归属地");
            headNames.add("操作人");
            headNames.add("时间");
            headNames.add("录音");

            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    head = new ArrayList<>();
                    if (headNames.contains(String.valueOf(map.get("label_name")))) {
                        head.add(String.valueOf(map.get("label_name")) + map.get("label_id"));
                    } else {
                        head.add(String.valueOf(map.get("label_name")));
                        headNames.add(String.valueOf(map.get("label_name")));
                    }
                    headers.add(head);
                    labelIdList.add(String.valueOf(map.get("label_id")));
                    if (StringUtil.isEmpty(invitationLabelId)) {
                        if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(String.valueOf(map.get("label_name")))) {
                            invitationLabelId = String.valueOf(map.get("label_id"));
                        }
                    }
                }
            }
            head = new ArrayList<>();
            head.add("身份ID");
            headers.add(head);

            head = new ArrayList<>();
            head.add("客户群ID");
            headers.add(head);

            head = new ArrayList<>();
            head.add("手机号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("归属地");
            headers.add(head);

            head = new ArrayList<>();
            head.add("操作人");
            headers.add(head);

            head = new ArrayList<>();
            head.add("时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("录音");
            headers.add(head);

            if (StringUtil.isNotEmpty(invitationLabelId)) {
                String nowMonth = DateUtil.getNowMonthToYYYYMM();
                if (StringUtil.isNotEmpty(startTimeStr) && StringUtil.isNotEmpty(endTimeStr)) {
                    nowMonth = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyyMM"));
                }

                // 检查通话记录月表是否存在
                marketResourceDao.createVoiceLogTableNotExist(nowMonth);

                String labelDataLikeValue = "\"" + invitationLabelId + "\":\"" + invitationLabelValue + "\"";
                StringBuffer sql = new StringBuffer();
                // 获取邀约成功,拨打电话成功用户的通话记录
                sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.superid, voice.recordurl, ")
                        .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                        .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street ")
                        .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowMonth + " voice ")
                        .append(" JOIN t_customer_group_list_" + customerGroupId + " t ON t.id = voice.superid ")
                        .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? ")
                        .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                        .append(" AND voice.status = 1001 ")
                        .append(" AND t.super_data LIKE '%" + labelDataLikeValue + "%' ");

                if ("2".equals(userQueryParam.getUserType())) {
                    // 组长查组员列表
                    if ("1".equals(userQueryParam.getUserGroupRole())) {
                        List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), custId);
                        Set<String> userIds = new HashSet<>();
                        if (customerUserDTOList.size() > 0) {
                            for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                                userIds.add(customerUserDTO.getId());
                            }
                            // 分配责任人操作
                            if (userIds.size() > 0) {
                                sql.append(" AND voice.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                            }
                        }
                    } else {
                        sql.append(" AND voice.user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }

                List<Map<String, Object>> callLogList = customGroupDao.sqlQuery(sql.toString(), custId, customerGroupId, startTimeStr, endTimeStr);
                // 有满足条件的营销记录
                if (callLogList.size() > 0) {
                    // 组合拼装为map,方便通过label_id和super_id快速查找数据
                    Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                    Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);

                    // 当前客户群下满足条件的身份ID集合
                    Set<String> superIdSets = new HashSet<>();
                    Set<String> userIdSets = new HashSet<>();
                    Map<String, Object> labelData;
                    for (Map<String, Object> map : callLogList) {
                        if (map != null) {
                            if (map.get("superid") != null) {
                                superIdSets.add(String.valueOf(map.get("superid")));
                            }
                            if (map.get("user_id") != null) {
                                userIdSets.add(String.valueOf(map.get("user_id")));
                            }
                            // 拆解用户勾选的自建属性
                            if (map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                                labelData = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                                if (labelData != null && labelData.size() > 0) {
                                    for (Map.Entry<String, Object> key : labelData.entrySet()) {
                                        invitationCustGroupSuperMap.put(customerGroupId + "_" + map.get("superid"), key.getValue());
                                        invitationSuperLabelMap.put(customerGroupId + "_" + key.getKey() + "_" + map.get("superid"), key.getValue());
                                    }
                                }
                            }
                        }
                    }
                    // 查询用户姓名
                    Map<String, Object> realNameMap = new HashMap<>();
                    if (userIdSets.size() > 0) {
                        List<Map<String, Object>> userList = customGroupDao.sqlQuery("SELECT id, account FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                        for (Map<String, Object> map : userList) {
                            realNameMap.put(String.valueOf(map.get("id")), map.get("account"));
                        }
                    }

                    // 根据superId查询手机号
                    Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);

                    //构造excel返回数据
                    String fileName;
                    List<Map<String, Object>> customerList = customGroupDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", custId);
                    if (customerList.size() > 0) {
                        fileName = customerList.get(0).get("enterprise_name") + "";
                    } else {
                        fileName = "客户";
                    }

                    List<List<String>> data = new ArrayList<>();
                    List<String> columnList;
                    String monthYear = "";
                    for (Map<String, Object> row : callLogList) {
                        if (invitationCustGroupSuperMap.get(customerGroupId + "_" + row.get("superid")) != null) {
                            columnList = new ArrayList<>();
                            for (String header : labelIdList) {
                                if (invitationSuperLabelMap.get(customerGroupId + "_" + header + "_" + row.get("superid")) != null) {
                                    columnList.add(String.valueOf(invitationSuperLabelMap.get(customerGroupId + "_" + header + "_" + row.get("superid"))));
                                } else {
                                    columnList.add("");
                                }
                            }
                            columnList.add(String.valueOf(row.get("superid")));
                            //客户群ID
                            columnList.add(String.valueOf(row.get("customer_group_id")));
                            //手机号
                            columnList.add(PhoneAreaUtil.replacePhone(phoneMap.get(String.valueOf(row.get("superid")))));
                            //归属地
                            columnList.add(String.valueOf(row.get("phonearea")));
                            //姓名
                            columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                            // 通话时间
                            columnList.add(String.valueOf(row.get("create_time")));
                            if (StringUtil.isNotEmpty(String.valueOf(row.get("create_time")))) {
                                monthYear = LocalDateTime.parse(String.valueOf(row.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                            }
                            columnList.add(CallUtil.generateRecordUrlMp3(monthYear, row.get("user_id"), row.get("touchId")));
                            data.add(columnList);
                        }
                    }
                    if (data.size() > 0) {
                        fileName += "-营销数据-" + LocalDateTime.now().format(DATE_TIME_FORMATTER);
                        final String fileType = ".xlsx";
                        response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                        response.setContentType("application/vnd.ms-excel;charset=utf-8");
                        ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                        log.info("导出表头:" + JSON.toJSONString(headers));
                        log.info("导出表头:" + JSON.toJSONString(labelIdList));
                        Sheet sheet1 = new Sheet(1, 0);
                        sheet1.setHead(headers);
                        sheet1.setSheetName("营销数据");
                        writer.write0(data, sheet1);
                        writer.finish();
                        customerGroupDataExportTime = 0;
                    } else {
                        msg.put("msg", "客户群下无满足条件的客户数据");
                        msg.put("data", String.valueOf(customerGroupDataExportTime));
                        outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                        customerGroupDataExportTime = 0;
                        return;
                    }
                } else {
                    msg.put("msg", "客户群下无满足条件的客户数据");
                    msg.put("data", String.valueOf(customerGroupDataExportTime));
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    customerGroupDataExportTime = 0;
                    return;
                }
            } else {
                msg.put("msg", "无满足条件的自建属性");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                customerGroupDataExportTime = 0;
                return;
            }
        } catch (Exception e) {
            log.error("导出单个客户群营销数据异常,", e);
            customerGroupDataExportTime = 0;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("导出营销数据异常,", e);
            }
        }
    }

    /**
     * 导出时间段内客户群全部的成功营销数据(支持跨月)
     *
     * @param response
     * @param loginUser
     * @param customerGroupId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     */
    public void exportCustomerGroupMarketAllDataToExcel(HttpServletResponse response, LoginUser loginUser, String
            customerGroupId, String invitationLabelId, String invitationLabelName,
                                                        String invitationLabelValue, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        msg.put("customerGroupId", customerGroupId);
        msg.put("invitationLabelId", invitationLabelId);
        msg.put("invitationLabelName", invitationLabelName);
        msg.put("invitationLabelValue", invitationLabelValue);

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            // 处理时间
            if (customerGroupDataExportTime >= 0 && (System.currentTimeMillis() - customerGroupDataExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            CustomGroup customGroup = customGroupDao.get(Integer.parseInt(customerGroupId));
            if (customGroup == null) {
                log.warn("客户群为空:" + customerGroupId);
                msg.put("msg", "客户群为空");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            customerGroupDataExportTime = System.currentTimeMillis();
            LocalDateTime localStartDateTime, localEndDateTime;
            // 处理时间
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                localStartDateTime = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
                localEndDateTime = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER);
            } else if (customGroup.getTaskCreateTime() != null && customGroup.getTaskEndTime() != null) {
                localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskCreateTime().getTime());
                localEndDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskEndTime().getTime());
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            String startTimeStr = localStartDateTime.format(DATE_TIME_FORMATTER);
            String endTimeStr = localEndDateTime.format(DATE_TIME_FORMATTER);

            List<String> labelIdList = new ArrayList<>();
            List<Map<String, Object>> labelNames = customGroupDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", loginUser.getCustId());
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
            headNames.add("身份ID");
            headNames.add("客户群ID");
            headNames.add("手机号");
            headNames.add("归属地");
            headNames.add("操作人");
            headNames.add("时间");
            headNames.add("录音");

            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    head = new ArrayList<>();
                    if (headNames.contains(String.valueOf(map.get("label_name")))) {
                        head.add(String.valueOf(map.get("label_name")) + map.get("label_id"));
                    } else {
                        head.add(String.valueOf(map.get("label_name")));
                        headNames.add(String.valueOf(map.get("label_name")));
                    }
                    headers.add(head);
                    labelIdList.add(String.valueOf(map.get("label_id")));
                    if (StringUtil.isEmpty(invitationLabelId)) {
                        if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(String.valueOf(map.get("label_name")))) {
                            invitationLabelId = String.valueOf(map.get("label_id"));
                        }
                    }
                }
            }
            head = new ArrayList<>();
            head.add("身份ID");
            headers.add(head);

            head = new ArrayList<>();
            head.add("客户群ID");
            headers.add(head);

            head = new ArrayList<>();
            head.add("手机号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("归属地");
            headers.add(head);

            head = new ArrayList<>();
            head.add("操作人");
            headers.add(head);

            head = new ArrayList<>();
            head.add("时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("录音");
            headers.add(head);

            if (StringUtil.isNotEmpty(invitationLabelId)) {
                String labelDataLikeValue = "\"" + invitationLabelId + "\":\"" + invitationLabelValue + "\"";
                final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
                StringBuffer sql = new StringBuffer();
                List<Map<String, Object>> callLogList = new ArrayList<>();
                List<Map<String, Object>> callLogListTmp;
                // 处理跨月查询逻辑
                for (LocalDateTime nowTime = localStartDateTime; nowTime.isBefore(localEndDateTime); ) {
                    // 检查通话记录月表是否存在
                    marketResourceDao.createVoiceLogTableNotExist(nowTime.format(YYYYMM));

                    sql.setLength(0);
                    // 获取邀约成功,拨打电话成功用户的通话记录
                    sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.superid, voice.recordurl, ")
                            .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                            .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street ")
                            .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowTime.format(YYYYMM) + " voice ")
                            .append(" JOIN t_customer_group_list_" + customerGroupId + " t ON t.id = voice.superid ")
                            .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? ")
                            .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                            .append(" AND voice.status = 1001 ")
                            .append(" AND t.super_data LIKE '%" + labelDataLikeValue + "%' ");

                    if ("2".equals(loginUser.getUserType())) {
                        // 组长查组员列表
                        if ("1".equals(loginUser.getUserGroupRole())) {
                            List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                            Set<String> userIds = new HashSet<>();
                            if (customerUserDTOList.size() > 0) {
                                for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                                    userIds.add(customerUserDTO.getId());
                                }
                                // 分配责任人操作
                                if (userIds.size() > 0) {
                                    sql.append(" AND voice.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                                }
                            }
                        } else {
                            sql.append(" AND voice.user_id = '" + loginUser.getId() + "'");
                        }
                    }

                    callLogListTmp = customGroupDao.sqlQuery(sql.toString(), loginUser.getCustId(), customerGroupId, startTimeStr, endTimeStr);
                    if (callLogListTmp.size() > 0) {
                        callLogList.addAll(callLogListTmp);
                    }
                    //　当前时间处理到下月1号0点0分0秒
                    nowTime = nowTime.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                }

                // 有满足条件的营销记录
                if (callLogList.size() > 0) {
                    // 组合拼装为map,方便通过label_id和super_id快速查找数据
                    Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                    Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);

                    // 当前客户群下满足条件的身份ID集合
                    Set<String> superIdSets = new HashSet<>();
                    Set<String> userIdSets = new HashSet<>();
                    Map<String, Object> labelData;
                    String monthYear = "";
                    for (Map<String, Object> map : callLogList) {
                        if (map != null) {
                            if (map.get("superid") != null) {
                                superIdSets.add(String.valueOf(map.get("superid")));
                            }
                            if (map.get("user_id") != null) {
                                userIdSets.add(String.valueOf(map.get("user_id")));
                            }
                            // 拆解用户勾选的自建属性
                            if (map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                                labelData = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                                if (labelData != null && labelData.size() > 0) {
                                    for (Map.Entry<String, Object> key : labelData.entrySet()) {
                                        invitationCustGroupSuperMap.put(customerGroupId + "_" + map.get("superid"), key.getValue());
                                        invitationSuperLabelMap.put(customerGroupId + "_" + key.getKey() + "_" + map.get("superid"), key.getValue());
                                    }
                                }
                            }
                        }
                    }
                    // 查询用户姓名
                    Map<String, Object> realNameMap = new HashMap<>();
                    if (userIdSets.size() > 0) {
                        List<Map<String, Object>> userList = customGroupDao.sqlQuery("SELECT id, account FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                        for (Map<String, Object> map : userList) {
                            realNameMap.put(String.valueOf(map.get("id")), map.get("account"));
                        }
                    }

                    // 根据superId查询手机号
                    Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);
                    //构造excel返回数据
                    String fileName;
                    List<Map<String, Object>> customerList = customGroupDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", loginUser.getCustId());
                    if (customerList.size() > 0) {
                        fileName = customerList.get(0).get("enterprise_name") + "";
                    } else {
                        fileName = "客户";
                    }

                    List<List<String>> data = new ArrayList<>();
                    List<String> columnList;

                    for (Map<String, Object> row : callLogList) {
                        if (invitationCustGroupSuperMap.get(customerGroupId + "_" + row.get("superid")) != null) {
                            columnList = new ArrayList<>();
                            for (String header : labelIdList) {
                                if (invitationSuperLabelMap.get(customerGroupId + "_" + header + "_" + row.get("superid")) != null) {
                                    columnList.add(String.valueOf(invitationSuperLabelMap.get(customerGroupId + "_" + header + "_" + row.get("superid"))));
                                } else {
                                    columnList.add("");
                                }
                            }
                            columnList.add(String.valueOf(row.get("superid")));
                            //客户群ID
                            columnList.add(String.valueOf(row.get("customer_group_id")));
                            //手机号
                            //columnList.add(String.valueOf(phoneMap.get(String.valueOf(row.get("superid")))));
                            columnList.add(PhoneAreaUtil.replacePhone(phoneMap.get(String.valueOf(row.get("superid")))));
                            //归属地
                            columnList.add(String.valueOf(row.get("phonearea")));
                            //姓名
                            columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                            // 通话时间
                            columnList.add(String.valueOf(row.get("create_time")));
                            if (StringUtil.isNotEmpty(String.valueOf(row.get("create_time")))) {
                                monthYear = LocalDateTime.parse(String.valueOf(row.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                            }
                            columnList.add(CallUtil.generateRecordUrlMp3(monthYear, row.get("user_id"), row.get("touchId")));
                            data.add(columnList);
                        }
                    }
                    if (data.size() > 0) {
                        fileName += "-营销数据-" + LocalDateTime.now().format(DATE_TIME_FORMATTER);
                        final String fileType = ".xlsx";
                        response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                        response.setContentType("application/vnd.ms-excel;charset=utf-8");
                        ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                        log.info("导出表头:" + JSON.toJSONString(headers));
                        log.info("导出表头:" + JSON.toJSONString(labelIdList));
                        Sheet sheet1 = new Sheet(1, 0);
                        sheet1.setHead(headers);
                        sheet1.setSheetName("营销数据");
                        writer.write0(data, sheet1);
                        writer.finish();
                        customerGroupDataExportTime = 0;
                    } else {
                        msg.put("msg", "客户群下无满足条件的客户数据");
                        msg.put("data", String.valueOf(customerGroupDataExportTime));
                        outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                        customerGroupDataExportTime = 0;
                        return;
                    }
                } else {
                    msg.put("msg", "客户群下无满足条件的客户数据");
                    msg.put("data", String.valueOf(customerGroupDataExportTime));
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    customerGroupDataExportTime = 0;
                    return;
                }
            } else {
                msg.put("msg", "无满足条件的自建属性");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                customerGroupDataExportTime = 0;
                return;
            }
        } catch (Exception e) {
            log.error("导出单个客户群营销数据异常,", e);
            customerGroupDataExportTime = 0;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("导出营销数据异常,", e);
            }
        }
    }


    public void exportRobotCustomerGroupIntentLevelToExcel(HttpServletResponse response, LoginUser
            loginUser, String customerGroupId, String startTime, String endTime, String callStatus, String
                                                                   intentLevel, List<Map<String, String>> labelValues) {
        Map<String, String> msg = new HashMap<>();
        msg.put("customerGroupId", customerGroupId);
        msg.put("labelValues", JSON.toJSONString(labelValues));

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            // 处理时间
            if (customerGroupDataExportTime >= 0 && (System.currentTimeMillis() - customerGroupDataExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            CustomGroup customGroup = customGroupDao.get(Integer.parseInt(customerGroupId));
            if (customGroup == null) {
                log.warn("客户群为空:" + customerGroupId);
                msg.put("msg", "客户群为空");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            customerGroupDataExportTime = System.currentTimeMillis();
            LocalDateTime localStartDateTime, localEndDateTime;
            // 处理时间
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                localStartDateTime = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
                localEndDateTime = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER);
            } else if (customGroup.getTaskCreateTime() != null && customGroup.getTaskEndTime() != null) {
                localStartDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskCreateTime().getTime());
                localEndDateTime = DateUtil.getDateTimeOfTimestamp(customGroup.getTaskEndTime().getTime());
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            String startTimeStr = localStartDateTime.format(DATE_TIME_FORMATTER);
            String endTimeStr = localEndDateTime.format(DATE_TIME_FORMATTER);

            List<String> labelIdList = new ArrayList<>();
            List<Map<String, Object>> labelNames = customGroupDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", loginUser.getCustId());
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
            headNames.add("意向度");
            headNames.add("身份ID");
            headNames.add("客户群ID");
            headNames.add("手机号");
            headNames.add("归属地");
            headNames.add("操作人");
            headNames.add("时间");
            headNames.add("录音");

            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    head = new ArrayList<>();
                    if (headNames.contains(String.valueOf(map.get("label_name")))) {
                        head.add(String.valueOf(map.get("label_name")) + map.get("label_id"));
                    } else {
                        head.add(String.valueOf(map.get("label_name")));
                        headNames.add(String.valueOf(map.get("label_name")));
                    }
                    headers.add(head);
                    labelIdList.add(String.valueOf(map.get("label_id")));
                }
            }
            head = new ArrayList<>();
            head.add("意向度");
            headers.add(head);

            head = new ArrayList<>();
            head.add("身份ID");
            headers.add(head);

            head = new ArrayList<>();
            head.add("客户群ID");
            headers.add(head);

            head = new ArrayList<>();
            head.add("手机号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("归属地");
            headers.add(head);

            head = new ArrayList<>();
            head.add("操作人");
            headers.add(head);

            head = new ArrayList<>();
            head.add("时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("录音");
            headers.add(head);


            //StringBuffer labelDataLikeSql = "\"" + invitationLabelId + "\":\"" + invitationLabelValue + "\"";
            StringBuffer labelDataLikeSql = new StringBuffer();
            // 处理多个自建属性和属性值的搜索
            if (labelValues != null && labelValues.size() > 0) {
                Map<String, String> m;
                for (int i = 0; i < labelValues.size(); i++) {
                    m = labelValues.get(i);
                    if (m != null) {
                        labelDataLikeSql.append(" AND t.super_data LIKE '%")
                                .append("\"" + m.get("labelId") + "\":\"")
                                .append(m.get("labelValue")).append("\"%' ");
                    }
                }
            }

            final DateTimeFormatter yyyymm = DateTimeFormatter.ofPattern("yyyyMM");
            StringBuffer sql = new StringBuffer();
            List<Map<String, Object>> callLogList = new ArrayList<>();
            List<Map<String, Object>> callLogListTmp;
            // 处理跨月查询逻辑
            for (LocalDateTime nowTime = localStartDateTime; nowTime.isBefore(localEndDateTime); ) {
                // 检查通话记录月表是否存在
                marketResourceDao.createVoiceLogTableNotExist(nowTime.format(yyyymm));

                sql.setLength(0);
                // 获取邀约成功,拨打电话成功用户的通话记录
                sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.superid, voice.recordurl, voice.call_data ,")
                        .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                        .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street ")
                        .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowTime.format(yyyymm) + " voice ")
                        .append(" JOIN t_customer_group_list_" + customerGroupId + " t ON t.id = voice.superid ")
                        .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? ")
                        .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ");

                if (StringUtil.isNotEmpty(callStatus)) {
                    sql.append(" AND voice.status = '" + callStatus + "'");
                }
                if (StringUtil.isNotEmpty(intentLevel)) {
                    sql.append(" AND voice.call_data LIKE '%")
                            .append("\"level\":\"")
                            .append(intentLevel).append("\"%' ");
                }
                if (labelDataLikeSql.length() > 0) {
                    sql.append(labelDataLikeSql.toString());
                }

                if ("2".equals(loginUser.getUserType())) {
                    // 组长查组员列表
                    if ("1".equals(loginUser.getUserGroupRole())) {
                        List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                        Set<String> userIds = new HashSet<>();
                        if (customerUserDTOList.size() > 0) {
                            for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                                userIds.add(customerUserDTO.getId());
                            }
                            // 分配责任人操作
                            if (userIds.size() > 0) {
                                sql.append(" AND voice.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                            }
                        }
                    } else {
                        sql.append(" AND voice.user_id = '" + loginUser.getId() + "'");
                    }
                }

                callLogListTmp = customGroupDao.sqlQuery(sql.toString(), loginUser.getCustId(), customerGroupId, startTimeStr, endTimeStr);
                if (callLogListTmp.size() > 0) {
                    callLogList.addAll(callLogListTmp);
                }
                //　当前时间处理到下月1号0点0分0秒
                nowTime = nowTime.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }

            // 有满足条件的营销记录
            if (callLogList.size() > 0) {
                if (PROVINCE_SET.size() == 0) {
                    String[] provinceNames = PROVINCE_NAME.split(",");
                    for (String p : provinceNames) {
                        PROVINCE_SET.add(p);
                    }
                }

                // 组合拼装为map,方便通过label_id和super_id快速查找数据
                Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);

                // 当前客户群下满足条件的身份ID集合
                Set<String> superIdSets = new HashSet<>();
                Set<String> userIdSets = new HashSet<>();
                Map<String, Object> labelData;
                String monthYear = "";
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("superid") != null) {
                            superIdSets.add(String.valueOf(map.get("superid")));
                        }
                        if (map.get("user_id") != null) {
                            userIdSets.add(String.valueOf(map.get("user_id")));
                        }
                        // 拆解用户勾选的自建属性
                        if (map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                            labelData = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                            if (labelData != null && labelData.size() > 0) {
                                for (Map.Entry<String, Object> key : labelData.entrySet()) {
                                    invitationSuperLabelMap.put(customerGroupId + "_" + key.getKey() + "_" + map.get("superid"), key.getValue());
                                }
                            }
                        }
                    }
                }
                // 查询用户姓名
                Map<String, Object> realNameMap = new HashMap<>();
                if (userIdSets.size() > 0) {
                    List<Map<String, Object>> userList = customGroupDao.sqlQuery("SELECT id, account FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                    for (Map<String, Object> map : userList) {
                        realNameMap.put(String.valueOf(map.get("id")), map.get("account"));
                    }
                }

                // 根据superId查询手机号
                Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);

                //构造excel返回数据
                String fileName;
                List<Map<String, Object>> customerList = customGroupDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", loginUser.getCustId());
                if (customerList.size() > 0) {
                    fileName = customerList.get(0).get("enterprise_name") + "";
                } else {
                    fileName = "客户";
                }

                List<List<String>> data = new ArrayList<>();
                List<String> columnList;
                JSONObject callData;
                String area = "";
                for (Map<String, Object> row : callLogList) {
                    columnList = new ArrayList<>();
                    for (String labelId : labelIdList) {
                        if (invitationSuperLabelMap.get(customerGroupId + "_" + labelId + "_" + row.get("superid")) != null) {
                            columnList.add(String.valueOf(invitationSuperLabelMap.get(customerGroupId + "_" + labelId + "_" + row.get("superid"))));
                        } else {
                            columnList.add("");
                        }
                    }
                    callData = JSON.parseObject(String.valueOf(row.get("call_data")));
                    if (callData != null && callData.getString("level") != null) {
                        columnList.add(callData.getString("level"));
                    } else {
                        columnList.add("");
                    }
                    columnList.add(String.valueOf(row.get("superid")));
                    //客户群ID
                    columnList.add(String.valueOf(row.get("customer_group_id")));
                    //手机号
                    //columnList.add(String.valueOf(phoneMap.get(String.valueOf(row.get("superid")))));
                    columnList.add(PhoneAreaUtil.replacePhone(phoneMap.get(String.valueOf(row.get("superid")))));
                    //归属地
                    area = String.valueOf(row.get("phonearea"));
                    if (StringUtil.isNotEmpty(area)) {
                        for (String p : PROVINCE_SET) {
                            if (area.indexOf(p) >= 0) {
                                area = area.replaceAll(p, "");
                                break;
                            }
                        }
                    }
                    columnList.add(area);

                    //姓名
                    columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                    // 通话时间
                    columnList.add(String.valueOf(row.get("create_time")));
                    if (StringUtil.isNotEmpty(String.valueOf(row.get("create_time")))) {
                        monthYear = LocalDateTime.parse(String.valueOf(row.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                    }
                    columnList.add(CallUtil.generateRecordUrlMp3(monthYear, row.get("user_id"), row.get("touchId")));
                    data.add(columnList);

                }
                if (data.size() > 0) {
                    fileName += "-营销数据-" + LocalDateTime.now().format(DATE_TIME_FORMATTER);
                    final String fileType = ".xlsx";
                    response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                    response.setContentType("application/vnd.ms-excel;charset=utf-8");
                    ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                    Sheet sheet1 = new Sheet(1, 0);
                    sheet1.setHead(headers);
                    sheet1.setSheetName("营销数据");
                    writer.write0(data, sheet1);
                    writer.finish();
                    customerGroupDataExportTime = 0;
                } else {
                    msg.put("msg", "客户群下无满足条件的客户数据");
                    msg.put("data", String.valueOf(customerGroupDataExportTime));
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    customerGroupDataExportTime = 0;
                    return;
                }
            } else {
                msg.put("msg", "客户群下无满足条件的通话营销数据");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                customerGroupDataExportTime = 0;
                return;
            }
        } catch (Exception e) {
            log.error("导出单个客户群营销数据异常,", e);
            msg.put("msg", "导出单个客户群营销数据异常");
            msg.put("data", String.valueOf(customerGroupDataExportTime));
            try {
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
            } catch (IOException e1) {
                log.error("导出营销数据异常,", e);
            }
            customerGroupDataExportTime = 0;
            return;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("导出营销数据异常,", e);
                msg.put("msg", "导出单个客户群营销数据异常");
                msg.put("data", String.valueOf(customerGroupDataExportTime));
                return;
            }
        }
    }


    public int updateMarketTaskTime(int custGroupId, long endTime) {
        CustomGroup customGroup = customGroupDao.get(custGroupId);
        if (customGroup != null) {
            customGroup.setTaskEndTime(new Timestamp(endTime));
            customGroupDao.update(customGroup);
            return 1;
        }
        return 0;
    }

    /**
     * 导出客户群统计数据
     *
     * @return
     */
    public void exportCustGroupStatData(HttpServletResponse response) {
        String sql = "SELECT * FROM stat_c_g_d";
        List<Map<String, Object>> list = customGroupDao.sqlQuery(sql);
        Map<String, String> msg = new HashMap<>();
        OutputStream outputStream = null;
        if (list.size() > 0) {
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            head = new ArrayList<>();
            head.add("统计日期");
            headers.add(head);

            head = new ArrayList<>();
            head.add("客户");
            headers.add(head);

            head = new ArrayList<>();
            head.add("行业");
            headers.add(head);

            head = new ArrayList<>();
            head.add("项目");
            headers.add(head);

            head = new ArrayList<>();
            head.add("客户群");
            headers.add(head);

            head = new ArrayList<>();
            head.add("需求量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("提取量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("呼叫量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("呼通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成单量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("客户群创建时间");
            headers.add(head);

            List<List<String>> data = new ArrayList<>();
            List<String> columnList;
            for (Map<String, Object> m : list) {
                columnList = new ArrayList<>();
                columnList.add(String.valueOf(m.get("stat_time")));
                columnList.add(String.valueOf(m.get("cust_name")));
                columnList.add(String.valueOf(m.get("industry_name")));
                columnList.add(String.valueOf(m.get("project_name")));
                columnList.add(String.valueOf(m.get("cust_group_id")));
                columnList.add(String.valueOf(m.get("demand_sum")));
                columnList.add(String.valueOf(m.get("actual_sum")));
                columnList.add(String.valueOf(m.get("caller_sum")));
                columnList.add(String.valueOf(m.get("called_sum")));
                columnList.add(String.valueOf(m.get("order_sum")));
                columnList.add(String.valueOf(m.get("cust_group_create_time")));
                data.add(columnList);
            }
            try {
                if (data.size() > 0) {
                    String fileName = "客户群统计数据";
                    final String fileType = ".xlsx";
                    outputStream = response.getOutputStream();
                    response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                    response.setContentType("application/vnd.ms-excel;charset=utf-8");
                    ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                    log.info("导出表头:" + JSON.toJSONString(headers));
                    Sheet sheet1 = new Sheet(1, 0);
                    sheet1.setHead(headers);
                    sheet1.setSheetName("营销数据");
                    writer.write0(data, sheet1);
                    writer.finish();
                } else {
                    msg.put("msg", "客户群统计数据为空");
                    return;
                }
            } catch (Exception e) {
                log.error("导出客户群统计数据,", e);
                msg.put("msg", "导出客户群统计数据异常");
                return;
            } finally {
                try {
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                } catch (IOException e) {
                    log.error("导出客户群统计数据,", e);
                }
                try {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                    response.flushBuffer();
                } catch (IOException e) {
                    log.error("导出客户群统计数据,", e);
                }
            }
        }
    }

    /**
     * 判断当前用户是否存在相同条件的待支付的客户群
     *
     * @param custId
     * @param groupCondition
     * @param status
     * @return true-存在 false-不存在
     */
    public boolean checkCGoupOrderStatus(String custId, String groupCondition, int status) {
        //判断当前用户是否存在相同条件的待支付的客户群
        int code = customGroupDao.selectCGoupOrderStatus(custId, groupCondition, status);
        return code >= 1 ? true : false;
    }

    /**
     * 保存导入的客户群基本信息
     *
     * @param custId
     * @param name
     * @param fileName
     * @param headers
     * @return 0-失败 1-成功 2-文件已经导入过
     */
    public int saveImportCustGroupData(String custId, String name, String fileName, JSONArray headers, Integer projectId) {
        CustomerGroupProperty uploadFileStatus = customGroupDao.getProperty("uploadFilePath", fileName);
        if (uploadFileStatus != null) {
            log.warn("导入客户群文件:" + fileName + ",已经成功导入,忽略");
            return 2;
        }

        String orderId = String.valueOf(IDHelper.getTransactionId());
        StringBuffer insertOrder = new StringBuffer();
        insertOrder.append("INSERT INTO  t_order (`order_id`, `cust_id`, `order_type`, `create_time`,  `remarks`, `amount`, `order_state`, `cost_price`) ");
        insertOrder.append(" VALUES ('" + orderId + "','" + custId + "','1','" + new Timestamp(System.currentTimeMillis()) + "','导入客户群创建','0','2','0')");
        int status = customGroupDao.executeUpdateSQL(insertOrder.toString());
        LogUtil.info("导入客户群创建订单表状态:" + status);
        if (status == 0) {
            return 0;
        }

        CustomGroup cg = new CustomGroup();
        cg.setName(name);
        cg.setDesc("导入客户群创建");
        cg.setOrderId(orderId);
        cg.setMarketProjectId(projectId);
        cg.setStatus(3);
        cg.setDataSource(1);
        cg.setCustId(custId);
        cg.setCreateTime(new Timestamp(System.currentTimeMillis()));
        cg.setGroupCondition("[{\"symbol\":0,\"leafs\":[{\"name\":\"4\",\"id\":\"87\"}],\"type\":1,\"labelId\":\"84\",\"parentName\":\"家庭人口数\",\"path\":\"人口统计学/基本信息/家庭人口数\"}]");
        LogUtil.info("导入客户群插入customer_group表的数据:" + cg);
        int id = (int) customGroupDao.saveReturnPk(cg);
        if (id > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(" create table IF NOT EXISTS t_customer_group_list_");
            sb.append(id);
            sb.append(" like t_customer_group_list");
            try {
                customGroupDao.executeUpdateSQL(sb.toString());
            } catch (HibernateException e) {
                log.error("创建用户群表失败,id:" + id, e);
            }

            CustomerGroupProperty cgp = new CustomerGroupProperty(id, "uploadHeaders", StringUtils.join(headers, ","), new Timestamp(System.currentTimeMillis()));
            customGroupDao.saveOrUpdate(cgp);
            cgp = new CustomerGroupProperty(id, "uploadFilePath", fileName, new Timestamp(System.currentTimeMillis()));
            customGroupDao.saveOrUpdate(cgp);
            // 异步处理客户群数据
            new Thread() {
                public void run() {
                    log.info("创建导入客群成功,开始异步处理数据,ID:" + id);
                    try {
                        CustomGroupService cgs = (CustomGroupService) SpringContextHelper.getBean("customGroupService");
                        int code = cgs.handleCustGroupImportData(String.valueOf(id), custId, customGroupDao, customerDao, customerLabelDao, jdbcTemplate, redisUtil, phoneService);
                        log.info("导入客户群数据ID:" + id + "更改状态成功,status:" + status);
                    } catch (Exception e) {
                        log.error("异步处理导入客群异常,", e);
                    }
                }
            }.start();
            return 1;
        }
        return 0;
    }

    /**
     * 保存导入的客户群基本信息
     *
     * @param custId     客户ID
     * @param name       客群名称
     * @param fileName   文件名称
     * @param headers    excel表头
     * @param projectId  项目ID
     * @param touchModes 触达方式 1-电话 2-短信
     * @return
     */
    public int saveImportCustGroupData(String custId, String name, String fileName, JSONArray headers, Integer projectId, String touchModes) {
        CustomerGroupProperty uploadFileStatus = customGroupDao.getProperty("uploadFilePath", fileName);
        if (uploadFileStatus != null) {
            log.warn("导入客户群文件:" + fileName + ",已经成功导入,忽略");
            return 2;
        }

        String orderId = String.valueOf(IDHelper.getTransactionId());
        StringBuffer insertOrder = new StringBuffer();
        insertOrder.append("INSERT INTO  t_order (`order_id`, `cust_id`, `order_type`, `create_time`,  `remarks`, `amount`, `order_state`, `cost_price`) ");
        insertOrder.append(" VALUES ('" + orderId + "','" + custId + "','1','" + new Timestamp(System.currentTimeMillis()) + "','导入客户群创建','0','2','0')");
        int status = customGroupDao.executeUpdateSQL(insertOrder.toString());
        LogUtil.info("导入客户群创建订单表状态:" + status);
        if (status == 0) {
            return 0;
        }

        CustomGroup cg = new CustomGroup();
        cg.setName(name);
        cg.setDesc("导入客户群创建");
        cg.setOrderId(orderId);
        cg.setMarketProjectId(projectId);
        cg.setStatus(3);
        cg.setDataSource(1);
        cg.setCustId(custId);
        cg.setCreateTime(new Timestamp(System.currentTimeMillis()));
        cg.setGroupCondition("[{\"symbol\":0,\"leafs\":[{\"name\":\"4\",\"id\":\"87\"}],\"type\":1,\"labelId\":\"84\",\"parentName\":\"家庭人口数\",\"path\":\"人口统计学/基本信息/家庭人口数\"}]");
        LogUtil.info("导入客户群插入customer_group表的数据:" + cg);
        int id = (int) customGroupDao.saveReturnPk(cg);
        if (id > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(" create table IF NOT EXISTS t_customer_group_list_");
            sb.append(id);
            sb.append(" like t_customer_group_list");
            try {
                customGroupDao.executeUpdateSQL(sb.toString());
            } catch (HibernateException e) {
                log.error("创建用户群表失败,id:" + id, e);
            }

            CustomerGroupProperty cgp = new CustomerGroupProperty(id, "uploadHeaders", StringUtils.join(headers, ","), new Timestamp(System.currentTimeMillis()));
            customGroupDao.saveOrUpdate(cgp);
            cgp = new CustomerGroupProperty(id, "uploadFilePath", fileName, new Timestamp(System.currentTimeMillis()));
            customGroupDao.saveOrUpdate(cgp);
            // 保存客户群触达方式
            if (StringUtil.isNotEmpty(touchModes)) {
                cgp = new CustomerGroupProperty(id, "touchMode", touchModes, new Timestamp(System.currentTimeMillis()));
                customGroupDao.saveOrUpdate(cgp);
            }
            // 异步处理客户群数据
            new Thread() {
                public void run() {
                    log.info("创建导入客群成功,开始异步处理数据,ID:" + id);
                    try {
                        CustomGroupService cgs = (CustomGroupService) SpringContextHelper.getBean("customGroupService");
                        int code = cgs.handleCustGroupImportData(String.valueOf(id), custId, customGroupDao, customerDao, customerLabelDao, jdbcTemplate, redisUtil, phoneService);
                        log.info("导入客户群数据ID:" + id + "更改状态成功,status:" + status);
                    } catch (Exception e) {
                        log.error("异步处理导入客群异常,", e);
                    }
                }
            }.start();
            return 1;
        }
        return 0;
    }

    /**
     * 检查导入客户群的数据
     *
     * @param file
     * @param headers
     * @return 1-通过 -1文件格式不正确 -2表头必须包含手机号 -3表头为空
     */
    public int checkUploadCustGroupData(MultipartFile file, List<String> headers) {
        // 检查excel文件格式是否正确
        boolean ftStatus = uploadDowloadImgService.checkFileTypeByMultipartFile(file, FileUtil.EXCEL_FILE_TYPES);
        int code = 0;
        if (!ftStatus) {
            // 文件格式不正确
            return -1;
        }
        // 检查excel是否包含手机号列名
        try {
            List<String> heads = (List<String>) ExcelUtil.readHeaders(file.getInputStream(), new Sheet(1), false);
            if (heads != null && heads.size() > 0) {
                if (!heads.contains("手机号")) {
                    // 表头必须包含手机号
                    return -2;
                }
                int count = ExcelUtil.readRowCount(file.getInputStream(), new Sheet(1), false);
                // 检查总行数
                if (count == 1) {
                    // 总行数必须大于等于1
                    return -5;
                }
                // 检查总行数
                if (count > 50001) {
                    // 总行数大于50000
                    return -4;
                }
            } else {
                // 表头为空
                return -3;
            }
        } catch (IOException e) {
            log.error("读取excel失败,", e);
        }

        return 1;
    }

    /**
     * 上传客户群excel数据
     *
     * @param file
     * @param headers
     * @return
     */
    public String uploadCustGroupData(MultipartFile file, List<String> headers) {
        // 上传excel
        return uploadDowloadImgService.uploadSingleFile(file, ConstantsUtil.CGROUP_IMPORT_FILE_PATH, FileUtil.EXCEL_FILE_TYPES);
    }

    /**
     * 处理导入客户群数据保存进数据库
     *
     * @param custGroupId
     * @param custId
     * @return
     */
    private int handleCustGroupImportData(String custGroupId, String custId, CustomGroupDao
            customGroupDao, CustomerDao customerDao, CustomerLabelDao customerLabelDao, JdbcTemplate jdbcTemplate, RedisUtil redisUtil, PhoneService phoneService) {
        CustomerGroupProperty uploadFilePath = customGroupDao.getProperty(NumberConvertUtil.parseInt(custGroupId), "uploadFilePath");
        if (uploadFilePath == null) {
            log.warn("导入客户群ID:" + custGroupId + "未查询到文件路径");
            return 0;
        }
        if (StringUtil.isEmpty(uploadFilePath.getPropertyValue())) {
            log.warn("导入客户群ID:" + custGroupId + "文件路径为空," + uploadFilePath);
            return 0;
        }
        CustomerGroupProperty uploadHeaders = customGroupDao.getProperty(NumberConvertUtil.parseInt(custGroupId), "uploadHeaders");
        if (uploadHeaders == null) {
            log.warn("导入客户群ID:" + custGroupId + "未查询到上传表头");
            return 0;
        }
        if (StringUtil.isEmpty(uploadHeaders.getPropertyValue())) {
            log.warn("导入客户群ID:" + custGroupId + "上传为空," + uploadHeaders);
            return 0;
        }
        // 勾选的有效表头
        List<String> headers = Arrays.asList(StringUtils.split(uploadHeaders.getPropertyValue(), ","));
        log.info("导入客户群ID:" + custGroupId + "勾选的表头:" + headers.toString());
        // 读取excel表头,获取对应关系
        String filePath = ConstantsUtil.CGROUP_IMPORT_FILE_PATH + uploadFilePath.getPropertyValue();
        log.info("导入客户群ID:" + custGroupId + "文件路径:" + filePath);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            // excel所有表头
            List<String> excelHeads = (List<String>) ExcelUtil.readHeaders(inputStream, new Sheet(1), false);
            Map<Integer, String> cellMap = new HashMap<>();
            Map<Integer, String> labelMap = new HashMap<>();
            if (excelHeads != null) {
                CustomerLabel cl;
                for (int i = 0; i < excelHeads.size(); i++) {
                    // excel中的表头在勾选的表头中
                    if (!headers.contains(excelHeads.get(i))) {
                        continue;
                    }
                    cellMap.put(i, excelHeads.get(i));
                    cl = customerLabelDao.getCustomerLabelByName(String.valueOf(excelHeads.get(i)), custId);
                    if (cl != null) {
                        labelMap.put(i, cl.getLabelId());
                    }
                }
                List<Map<String, Object>> list = new ArrayList<>();
                inputStream = new FileInputStream(filePath);
                List<Object> excelData = ExcelUtil.readExcel(inputStream, new Sheet(1), false);
                List<Object> row;
                Map<String, Object> rowData;
                // 从第2行开始读取,忽略表头
                for (int i = 1; i < excelData.size(); i++) {
                    row = (List<Object>) excelData.get(i);
                    // 获取每个单元格
                    rowData = new HashMap<>();
                    for (int j = 0; j < row.size(); j++) {
                        if (cellMap.get(j) == null) {
                            continue;
                        }
                        if ("手机号".equals(String.valueOf(cellMap.get(j)))) {
                            rowData.put("phone", row.get(j));
                        } else {
                            rowData.put(labelMap.get(j), row.get(j));
                        }
                    }
                    list.add(rowData);
                }
                //保存数据
                if (list.size() > 0) {
                    List<CGroupImportParam> data = new ArrayList<>();
                    CGroupImportParam param;
                    Map<String, String> u = new HashMap<>();
                    String uid;
                    for (int i = 0; i < list.size(); i++) {
                        param = new CGroupImportParam();
                        param.setPhone(String.valueOf(list.get(i).get("phone")));
                        // 调用API服务根据手机号生成uid
                        uid = phoneService.savePhoneToAPI(param.getPhone());
                        param.setMd5Phone(uid);
                        list.get(i).remove("phone");
                        param.setSuperData(JSON.toJSONString(list.get(i)));
                        param.setStatus(1);
                        data.add(param);
                        u.put(param.getMd5Phone(), param.getPhone());
                    }
                    boolean uCount = redisUtil.batchSet(u);
//                    int uCount = customerDao.insertBatchUData(data);
                    log.info("导入客户群数据ID:" + custGroupId + ",u表插入数量:" + uCount);
                    int gCount = customerDao.insertBatchDataGroupData(custGroupId, data);
                    log.info("导入客户群数据ID:" + custGroupId + ",客户群表插入数量:" + gCount);
                    if (uCount) {
                        log.info("导入客户群数据ID:" + custGroupId + "成功");
                        // 更改客户群状态
                        CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(custGroupId));
                        if (cg != null) {
                            // 处理完成
                            long userCount = customGroupDao.getCustomerGroupListDataCount(NumberConvertUtil.parseInt(custGroupId));
                            int status = jdbcTemplate.update("UPDATE customer_group SET user_count =  ?, quantity = ?,  status = ?, industry_pool_name=?, amount=0  WHERE id = ?", userCount, userCount, 1, "", custGroupId);
                            log.info("导入客户群数据ID:" + custGroupId + "更改状态成功,status:" + status + ",数量:" + userCount);
                        }
                        return 1;
                    }
                }
            } else {
                log.warn("导入客户群ID:" + custGroupId + "读取excel为空," + JSON.toJSONString(excelHeads));
                return 0;
            }
        } catch (Exception e) {
            log.error("导入客户群ID:" + custGroupId + "读取流异常,", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("导入客户群ID:" + custGroupId + "流关闭异常,", e);
                }
            }
        }
        return 0;
    }

    /**
     * 客户群所属客户修改
     *
     * @param groupId
     * @param custId
     * @author chengning@salescomm.net
     * @date 2019/4/1 17:27
     */
    public int customerGroupTransferByCustId(int groupId, String custId) {
        log.info("开始平移客户群所属客户,groupId:" + groupId + ",custId:" + custId);
        Customer customer = customerDao.get(custId);
        if (customer == null) {
            log.warn("客户ID未查询到对应客户:" + custId);
            return 0;
        }
        int status = customGroupDao.customerGroupTransferByCustId(groupId, custId);
        log.info("平移客户群所属客户,groupId:" + groupId + ",custId:" + custId + ",status:" + status);
        return status;
    }


    public JSONObject getCustomGroupList(String custId, String groupId, Integer pageNum, Integer pageSize) {
        JSONObject json = new JSONObject();
        CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(groupId));
        if (cg == null) {
            log.error("客群 " + groupId + " 不存在");
            json.put("errorDesc", "01");
            return json;
        }
        if (!custId.equals(cg.getCustId())) {
            log.error("无权限拉取客群 " + groupId + " 数据");
            json.put("errorDesc", "04");
            return json;
        }
        if (pageNum == null || pageNum < 0) pageNum = 0;
        if (pageSize == null || pageSize > 100 || pageSize <= 0) pageSize = 100;
        try {
            StringBuffer sql = new StringBuffer(" from t_customer_group_list_" + groupId + " ");
            String total = this.jdbcTemplate.queryForObject("select count(1) " + sql.toString(), String.class);

            if ("".equals(total) || "0".equals(total)) {
                json.put("errorDesc", "01");
                return json;
            }

            String sql_list = "SELECT id as superid "
                    + sql.toString() + " ORDER BY id DESC LIMIT " + pageNum + "," + pageSize;
            List<Map<String, Object>> list = this.customerGroupListDao.sqlQuery(sql_list);
            for (Map<String, Object> map : list) {
                if (map != null) {
                    for (Map.Entry<String, Object> keySet : map.entrySet()) {
                        if (keySet.getValue() == null) {
                            keySet.setValue("");
                        }
                    }
                }
            }
            json.put("total", "".equals(total) ? 0 : Integer.parseInt(total));
            json.put("data", list);
            json.put("pageSize", pageSize);
            json.put("pageNum", pageNum);
            json.put("errorDesc", "00");
        } catch (Exception e) {
            log.error(e.getMessage());
            json.put("errorDesc", "05");
        }
        return json;
    }


    public JSONObject addCustomGroupV2(CustomerGroupParamDTO customGroupDTO) throws Exception {
        JSONObject result = new JSONObject();

        // 0可购买量查询
        CustomGroup customGroup = new CustomGroup();
        String label = customGroupDTO.getLabel();
        String groupCondition = buildGroupcondition(label);
        customGroup.setGroupCondition(groupCondition);
        customGroup.setName(customGroupDTO.getName());
        customGroup.setCycle(0);
        customGroup.setCreateUserId(customGroupDTO.getCreateUserId());
        // 默认任务类型为0
        customGroup.setTaskType(0);
        // 处理所属项目ID
        if (StringUtil.isNotEmpty(customGroupDTO.getProjectId())) {
            MarketProject project = marketProjectDao.selectMarketProject(Integer.valueOf(customGroupDTO.getProjectId()));
            if (project == null) {
                result.put("errorDesc", "02");
                log.error("项目" + customGroupDTO.getProjectId() + " 不存在");
                return result;
            }
            CustomerProperty cp = customerDao.getProperty(customGroupDTO.getCustId(), CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + customGroupDTO.getProjectId());
            if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
                result.put("errorDesc", "04");
                log.error("企业" + customGroupDTO.getCustId() + " 未关联项目" + project.getId());
                return result;
            }
            customGroup.setMarketProjectId(NumberConvertUtil.parseInt(customGroupDTO.getProjectId()));
        }
        // 1查询标签成本价、销售价、订单金额、订单成本价
//        String groupCondition = customGroupDTO.getLabel();
        //JSONArray arr = JSONArray.parseArray(groupCondition);
        int labelAmount = industryPoolService.getSalePriceV1(groupCondition, Integer.valueOf(customGroupDTO.getPoolId()), customGroupDTO.getCustId());
        int labelCostAmount = industryPoolService.getCostPriceV1(groupCondition, Integer.valueOf(customGroupDTO.getPoolId()));
        SupplierDTO supplierDTO = industryPoolDao.getSupplierInfo(Integer.valueOf(customGroupDTO.getPoolId()));

        Customer customer = customerDao.get(customGroupDTO.getCustId());
        if (customer != null) {
            customGroupDTO.setEnterpriseName(customer.getEnterpriseName());
        }
        IndustryPool pool = industryPoolDao.get(Integer.valueOf(customGroupDTO.getPoolId()));
        if (pool != null) {
            customGroupDTO.setIndustryPoolName(pool.getName());
        }
        // 2生成订单
        OrderDO order = new OrderDO();
        Date date = new Date();
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        order.setOrderId(String.valueOf(IDHelper.getTransactionId()));// 产生订单号
        order.setCustId(customGroupDTO.getCustId());
        order.setOrderType(1);// 客户群
        order.setOrderState(1);// 未付款
        order.setCreateTime(date);
        order.setProductName("");

        String supplierId = null, supplierName = null;
        //supplier_id
        if (supplierDTO != null) {
            supplierId = String.valueOf(supplierDTO.getSupplierId());
            //supplierName = String.valueOf(supplierDTO.getName());
        }
        order.setSupplierId(supplierId);
        order.setRemarks("客户群创建");
        order.setProductName("客户群");
        order.setEnterpriseName(customGroupDTO.getEnterpriseName());
        order.setQuantity(Integer.valueOf(customGroupDTO.getNum()));
        order.setAmount(labelAmount * Integer.valueOf(customGroupDTO.getNum()));
        order.setCostPrice(labelCostAmount * Integer.valueOf(customGroupDTO.getNum()));
        String orderId = (String) orderDao.saveReturnPk(order);

        // 3保存用户群
        //customGroup.setGroupSource(JSON.toJSONString(quantityDetails));
        customGroup.setPurpose(customGroupDTO.getPurpose() == null ? "" : customGroupDTO.getPurpose());
        customGroup.setTotal(customGroupDTO.getTotal() == null ? 0L : customGroupDTO.getTotal().longValue());
        customGroup.setUserCount(Long.valueOf(customGroupDTO.getNum()));
        customGroup.setQuantity(Integer.valueOf(customGroupDTO.getNum()));
        customGroup.setIndustryPoolId(Integer.valueOf(customGroupDTO.getPoolId()));
        customGroup.setIndustryPoolName(customGroupDTO.getIndustryPoolName());
        customGroup.setCustId(customGroupDTO.getCustId());
        customGroup.setAmount(order.getAmount());
        customGroup.setOrderId(orderId);
        customGroup.setUpdateCycle(0);
        customGroup.setUpdateUserId(customGroupDTO.getUpdateUserId());
        customGroup.setAvailably(Constant.UNAVAILABLY);
        customGroup.setCreateTime(date);
        customGroup.setUpdateTime(date);
        Date taskStartTime = DateUtil.fmtStrToDate(customGroupDTO.getStart_date());
        Timestamp ts = new Timestamp(taskStartTime.getTime());
        customGroup.setTaskCreateTime(ts);
        Date taskendtime = DateUtil.fmtStrToDate(customGroupDTO.getEnd_date());
        Timestamp ts2 = new Timestamp(taskendtime.getTime());
        customGroup.setTaskEndTime(ts2);
        customGroup.setStatus(2);// 待确认
        customGroup.setDataSource(0);


        Integer id = (Integer) customGroupDao.saveReturnPk(customGroup);
        // 保存客户群触达方式
        if (StringUtil.isNotEmpty(customGroupDTO.getTouchType())) {
            CustomerGroupProperty cgp = new CustomerGroupProperty(id, "touchMode", customGroupDTO.getTouchType(), new Timestamp(System.currentTimeMillis()));
            customGroupDao.saveOrUpdate(cgp);
        }

        //Map<String, Object> resultMap = orderService.queryCustomerOrdDetail(customGroupDTO.getCustId(), orderId);
        //resultMap.put("orderId", orderId);
        // 生成大数据平台提取数据文件
//        String content = generateCGroupDataConditionFile(String.valueOf(id), customGroupDTO.getCustId(),
//                customGroupDTO.getProjectId(), 7, Integer.valueOf(customGroupDTO.getNum()), groupCondition);
//        FileUtil.writeFile(ConstantsUtil.CGROUP_AUTO_FILE_PATH, id + ".txt", content);
        //return resultMap;
        buyCustomGroup(orderId, Long.valueOf(customGroupDTO.getCreateUserId()), customGroupDTO.getCustId());
        Long userAcount = 0L;
        try {
            Map<String, Long> s = previewByGroupCondition2(null, null, buildGroupcondition2(customGroupDTO.getLabel()));
            userAcount = s.getOrDefault("count", 0l);
        } catch (Exception e) {
            log.error("预览人数异常", e);
        }
        result.put("errorDesc", "00");
        result.put("groupId", customGroup.getId() + "");
        result.put("userCount", userAcount);
        return result;
    }


    public void buyCustomGroup(String orderNo, Long userId, String custId) {
        CustomerProperty pp = customerDao.getProperty(custId, "pay_password");
        Map<String, String> jsonMap = buyCustomGroup(orderNo, userId, custId, "1", pp.getPropertyValue(), true);
        log.info("购买客群结果 orderNo：" + orderNo + ":" + JSONObject.toJSONString(jsonMap));
        String successBuym = jsonMap.get("code");
        if (successBuym != null && "0".equals(successBuym)) {
            log.info("余额支付之后创建客户群详情表..." + orderNo);
            try {
                addCustomGroupData0(orderNo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.error("购买客群失败：" + JSONObject.toJSONString(jsonMap));
        }
    }


    public Map<String, Long> previewByGroupCondition2(String groupName, Integer cycle, String groupCondition) throws Exception {
        Map<String, Long> map = new HashMap<String, Long>();

        net.sf.json.JSONObject params = ESUtil.formatForInterface(groupCondition);
        String r = RestUtil.postDataWithParms(params, ESUtil.getUrl() + "_count");
        log.info("通过标签预览人数返回数据:" + r);
        net.sf.json.JSONObject d = net.sf.json.JSONObject.fromObject(r);

        map.put("count", d.getLong("count"));
        map.put("total", 300000000L);
        return map;
    }


    public String buildGroupcondition(String label) throws Exception {
        List list = new ArrayList();
        if (StringUtil.isEmpty(label)) return null;
        JSONObject jsonObject = JSON.parseObject(label);
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            Map<String, Object> map = new HashMap<>();
            if (key.equals("geo")) {
                map.put("geo", jsonObject.getJSONArray("geo"));
            } else {
                LabelInfo info = labelInfoDao.get(Integer.valueOf(key));
                JSONArray childNames = jsonObject.getJSONArray(key);
                List<JSONObject> childrenList = new ArrayList<>();
                for (int j = 0; j < childNames.size(); j++) {
                    List<LabelInfo> label2 = labelInfoDao.find("from LabelInfo where parentId=? and labelName=?", info.getId(), childNames.getString(j));
                    if (label2 != null && label2.size() > 0) {
                        JSONObject object = new JSONObject();
                        object.put("name", childNames.getString(j));
                        object.put("id", label2.get(0).getId());
                        childrenList.add(object);
                    }
                }
                map.put("labelId", info.getId());
                map.put("parentName", info.getLabelName());
                map.put("path", info.getPath().substring(1) + info.getLabelName());
                map.put("type", info.getType() == null ? "0" : info.getType().toString());
                map.put("leafs", childrenList);
            }
            map.put("symbol", "0");
            list.add(map);
        }

        return JSONObject.toJSONString(list);
    }


    public String buildGroupcondition2(String label) throws Exception {
        List list = new ArrayList();
        if (StringUtil.isEmpty(label)) return null;
        JSONObject jsonObject = JSON.parseObject(label);
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            Map<String, Object> map = new HashMap<>();
            if (key.equals("geo")) {
                map.put("geo", jsonObject.getJSONArray("geo"));
            } else {
                map.put(key, jsonObject.getJSONArray(key));
            }
            list.add(map);
        }
        Map labelmap = new HashMap();
        labelmap.put("label", list);
        return JSONObject.toJSONString(labelmap);
    }

    /**
     * 检查客户-客户群数据权限
     *
     * @param cGroupId
     * @param custId
     * @return
     */
    public boolean checkCGroupDataPermission(int cGroupId, String custId) {
        CustomGroup cg = getCustomGroupById(cGroupId);
        if (cg == null) {
            log.warn("检查客户-客户群-身份ID数据权限失败,custId:" + custId + ",客户群Id:" + cGroupId + "未查询到对应客户群");
            return false;
        }
        if (Objects.equals(custId, cg.getCustId())) {
            return true;
        }
        log.warn("检查客户-客户群-身份ID数据权限失败,客户群Id:" + cGroupId + ",custId:" + custId + ",客户群所属custId:" + cg.getCustId() + "不匹配");
        return false;
    }

    /**
     * 检查客户-客户群-身份ID数据权限
     *
     * @param cGroupId
     * @param superIds
     * @return
     */
    public boolean checkCGroupListDataPermission(int cGroupId, Set<String> superIds) {
        if (superIds == null || superIds.size() == 0) {
            return false;
        }
        try {
            List<Map<String, Object>> list;
            for (String superId : superIds) {
                list = customGroupDao.sqlQuery("SELECT id FROM t_customer_group_list_" + cGroupId + " WHERE id = ?", superId);
                if (list.size() == 0) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("检查客户-客户群-身份ID数据权限异常,", e);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
//        System.out.println(Timestamp.valueOf("2019-05-01 00:00:00"));
    }

    /**
     * 检查客群是否属于该客户
     *
     * @param customerId
     * @param groupId
     * @return
     */
    public boolean checkCustomerGroupPermission(String customerId, int groupId) {
        CustomGroup customGroup = customGroupDao.get(groupId);
        log.info("检查客群所属客户权限,customerId:" + customerId + ",groupId:" + groupId);
        if (customGroup != null) {
            if (StringUtil.isNotEmpty(customGroup.getCustId()) && customGroup.getCustId().equals(customerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 购买客户群
     */
    public Map<String, String> buyCustomGroup(String orderNo, Long userId, String custId,
                                              String pay_type, String pay_password, Boolean isneibushiyong) {
        Map<String, String> map = new HashMap<String, String>();
        // 判断密码是否正确
        try {
            CustomerProperty pp = customerDao.getProperty(custId, "pay_password"); //支付密码

            if (pp == null || "".equals(pp.getPropertyValue())) {
                map.put("code", "1");
                map.put("message", "未设密码！");
                return map;
            }
            if (isneibushiyong) {
                if (!pp.getPropertyValue().equals(pay_password)) {
                    map.put("code", "2");
                    map.put("message", "支付密码错误！");
                    return map;
                }
            } else {
                if (!pp.getPropertyValue().equals(CipherUtil.generatePassword(pay_password))) {
                    map.put("code", "2");
                    map.put("message", "支付密码错误！");
                    return map;
                }
            }

        } catch (Exception e) {
            log.error("支付失败！" + e.getMessage());
        }

        //---订单---
        OrderDO order = orderDao.get(orderNo);
        // 判断订单状态
        if (order == null || order.getCustId() == null || !order.getCustId().equals(custId)) {
            map.put("code", "5");
            map.put("message", "订单不存在！");
            return map;
        }
        if (order.getOrderState() == 2) { // 已支付
            map.put("code", "5");
            map.put("message", "订单已支付！");
            return map;
        }
        //---客户群---
        CustomGroup cg = orderDao.getCusomGroup(orderNo);


        // 客户群的订单金额
        BigDecimal money = new BigDecimal(order.getAmount());

        // 账户扣钱（余额支付）查询账户余额
        CustomerProperty ra = customerDao.getProperty(custId, "remain_amount");
        Double remain_amount = 0.0;
        try {
            if (ra != null) remain_amount = Double.parseDouble(ra.getPropertyValue());
        } catch (Exception e) {
            log.error("get balance error", e);
        }
        DecimalFormat df = new DecimalFormat("0.00");
        BigDecimal amount = new BigDecimal(remain_amount);
        if (amount.subtract(money).compareTo(new BigDecimal(0)) < 0) {
            map.put("code", "3");
            map.put("message", "余额不足支付！");
            return map;
        }
        // 更新企业账户余额（余额扣款）
        if (ra == null) {
            ra = new CustomerProperty(custId, "remain_amount", "0");
        }
        ra.setPropertyValue(df.format(amount.subtract(money)));
        try {
            customerDao.saveOrUpdate(ra);
        } catch (Exception e) {
            map.put("code", "4");
            map.put("message", "支付失败！");
            return map;
        }

        // 更新订单状态，更新账户信息（余额扣款）
        order.setOrderState(2);
        order.setPayType(1);  //支付类型（1.余额 2.第三方 3.线下）
        order.setPayAmount(order.getAmount());
        order.setPayTime(new Date());
        orderDao.save(order);

        //保存交易记录
        TransactionDO trans = new TransactionDO();
        trans.setTransactionId(IDHelper.getTransactionId().toString());
        trans.setAcctId(userId.toString());
        trans.setCustId(custId);
        trans.setType(2);
        trans.setPayMode(1);
        trans.setOrderId(orderNo);
        trans.setCreateTime(new Timestamp(System.currentTimeMillis()));
        trans.setAmount(order.getAmount());
        orderDao.saveOrUpdate(trans);


//            String updateOrder = "update t_order set order_state=2,pay_type=1,pay_amount=?,pay_time=now()  where order_id =?";
//            jdbcTemplate.update(updateOrder, new Object[]{money1, dto.getOrderNo()});
        // t_transaction
        // dto.getTransaction_code() '交易类型（1.充值 2.扣减 3.消费）'
        // dto.getPay_type() 支付类型（1.余额 2.第三方 3.线下）'
//            Long transaction_id = IDHelper.getTransactionId();
//            Object[] objsTrans = {transaction_id, acct_id, dto.getCust_id(), dto.getThird_party_num(), money, dto.getOrderNo()};
//            String transactionSql = "insert into t_transaction set transaction_id=?,acct_id=?,cust_id=?,type=2,pay_mode=1,third_party_num=?,amount=?,order_id=?,create_time=now(),remark='客户群购买'";
//            marketResourceDao.insertOrder(transactionSql, objsTrans);
        // 更新客户群表
        if (cg != null) {
            cg.setStatus(3); //3=数据生成中  1=数据生成成功 0=数据生成失败
            cg.setUpdateTime(new Date());
            this.orderDao.saveOrUpdate(cg);
        }
//            String sql = "update customer_group set status=1,update_time=NOW()  where order_id =?";
//            jdbcTemplate.update(sql, new Object[]{dto.getOrderNo()});

        // 购买成功
        map.put("code", "0");
        map.put("message", "支付成功！");
        // 生成大数据平台自动提取数据文件
      /*  String content = generateCGroupDataConditionFile(String.valueOf(cg.getId()), cg.getCustId(), String.valueOf(cg.getMarketProjectId()), 7, cg.getQuantity(), cg.getGroupCondition());
        try {
            FileUtil.writeFile(ConstantsUtil.CGROUP_AUTO_FILE_PATH, cg.getId() + ".txt", content);
        } catch (Exception e) {
            log.error("生成客户群大数据平台提取数据文件异常,", e);
        }*/
        return map;
    }


    /**
     * 供应商客群分页
     *
     * @param userId
     * @param param
     * @return
     */
    public Page pageSupplierCustGroup(String userId, CustomerGrpOrdParam param) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t2.id AS groupId, ")
                .append(" t1.create_time createTime, FORMAT(IFNULL (t1.amount,0)/1000,2) AS amount, customer.enterprise_name enterpriseName, ")
                .append(" IFNULL(t2.extract_time,'') extractTime, t2.cust_id custId, t2.market_project_id marketProjectId, ")
                .append(" IFNULL(supplier.supplier_id,'') supplierId, IFNULL(supplier.`name`,'') supplierName, r.resource_id resourceId, rp.property_value resourceProperty")
                .append(" FROM t_order t1 ")
                .append(" JOIN customer_group t2 ON t1.order_id = t2.order_id ")
                .append(" JOIN t_customer customer ON customer.cust_id = t2.cust_id ")
                .append(" LEFT JOIN t_industry_pool pool ON pool.industry_pool_id = t2.industry_pool_id")
                .append(" LEFT JOIN t_market_resource r ON r.resource_id = pool.source_id")
                .append(" LEFT JOIN t_market_resource_property rp ON rp.resource_id = r.resource_id AND rp.property_name = 'price_config' ")
                .append(" LEFT JOIN t_supplier supplier ON supplier.supplier_id = r.supplier_id")
                .append(" where t1.order_type=1 ");
        if (StringUtil.isNotEmpty(userId)) {
            String supplierIds = userGroupService.getUserDataPermissonListByUserId(userId, 5);
            if (StringUtil.isNotEmpty(supplierIds)) {
                sql.append("and supplier.supplier_id in(" + supplierIds + ")");
            } else {
                return new Page();
            }
        }
        if (StringUtil.isNotEmpty(param.getGroupName())) {
            sql.append(" and t2.name like '%").append(StringEscapeUtils.escapeSql(param.getGroupName())).append("%'");
        }
        if (param.getOrderType() > 0) {
            sql.append(" and t1.order_state=").append(param.getOrderType());
        }
        if (StringUtil.isNotEmpty(param.getStatus())) {
            sql.append(" and t2.status=").append(param.getStatus());
        }
        if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
            sql.append(" and t2.id=").append(param.getGroupId());
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) && StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t2.extract_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                    .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        } else if (StringUtil.isNotEmpty(param.getStartTime())) {
            sql.append(" and t2.extract_time >= '").append(StringEscapeUtils.escapeSql(param.getStartTime())).append("'");
        } else if (StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t2.extract_time <= '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }
        // 企业名称模糊查询
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            sql.append(" and customer.enterprise_name LIKE '%" + param.getEnterpriseName() + "%'");
        }
        // 供应商名称模糊查询
        if (StringUtil.isNotEmpty(param.getSupplierName())) {
            sql.append(" and supplier.name LIKE '%" + param.getSupplierName() + "%'");
        }
        if (param.getChargingType() != null) {
            sql.append(" and rp.property_value LIKE '%type\":\"" + param.getChargingType() + "%'");
        }
        sql.append(" ORDER BY t2.extract_time desc, t2.create_time desc ");
        Page page = orderDao.sqlPageQuery0(sql.toString(), param.getPageNum(), param.getPageSize(), null);
        if (page != null && page.getData().size() > 0) {
            String orderSql = "SELECT IFNULL(SUM(order_sum), 0) order_sum from stat_c_g_u_d WHERE customer_group_id = ?";
            Map<String, Object> model;
            List<Map<String, Object>> list;
            String resourceProperty;
            JSONObject jsonObject;
            MarketProject marketProject;
            for (int i = 0; i < page.getData().size(); i++) {
                model = (Map<String, Object>) page.getData().get(i);
                // 项目名称
                model.put("projectName", "");
                if (StringUtil.isNotEmpty(String.valueOf(model.get("marketProjectId")))) {
                    marketProject = marketProjectDao.selectMarketProject(NumberConvertUtil.parseInt(model.get("marketProjectId")));
                    if (marketProject != null) {
                        model.put("projectName", marketProject.getName());
                    }
                }
                // 计费方式
                resourceProperty = String.valueOf(model.get("resourceProperty"));
                model.put("chargingType", "");
                if (StringUtil.isNotEmpty(resourceProperty)) {
                    jsonObject = JSON.parseObject(resourceProperty);
                    model.put("chargingType", jsonObject.getIntValue("type"));
                }
                // 实际提取量
                model.put("actualSum", countCustomGroupPhoneList(String.valueOf(model.get("groupId"))));
                // 标记成功量
                model.put("orderSum", 0);
                list = customerDao.sqlQuery(orderSql, model.get("groupId"));
                if (list != null && list.size() > 0) {
                    model.put("orderSum", list.get(0).get("order_sum"));
                }
                model.remove("resourceProperty");
            }
        }
        return page;
    }
}

