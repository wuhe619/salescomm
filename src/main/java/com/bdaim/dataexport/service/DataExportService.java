package com.bdaim.dataexport.service;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.Table;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.bdaim.callcenter.common.CallUtil;
import com.bdaim.callcenter.common.PhoneAreaUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.common.service.PhoneService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.dataexport.dao.DataExportDao;
import com.bdaim.dataexport.entity.DataExport;
import com.bdaim.label.dao.LabelInfoDao;
import com.bdaim.label.dto.CategoryType;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.DataNode;
import com.bdaim.label.entity.LabelInfo;
import com.bdaim.rbac.entity.User;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.util.*;
import com.github.crab2died.ExcelUtils;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("dataExportService")
@Transactional
public class DataExportService {

    private static Logger log = LoggerFactory.getLogger(DataExportService.class);

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 数据最后导出时间
     */
    private static long dataExportTime = 0;

    private static long dataExportTimeV2 = 0;

    private static long customerGroupDataExportTime = 0;

    private static long customerDataExportTime = 0;

    @Resource
    private DataExportDao dataExportDao;
    @Resource
    private LabelInfoDao labelInfoDao;
    @Resource
    private DataPermissionService dataPermissionService;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private CustomGroupDao customGroupDao;
    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private PhoneService phoneService;

    public void addDataExport(DataExport dataExport) {
        dataExport.setCreateTime(new Date());
        //dataExport.setTaskNum(UUID.randomUUID().toString().replaceAll("-", ""));
        dataExportDao.save(dataExport);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDataExportList(DataExport dataExport, Page page, User user) {
        List args=new ArrayList();
        StringBuilder sql = new StringBuilder("select t.id as id, t.task_num as taskNum, t.export_type as exportType, t.data_type as dataType, t.start_time as startTime, t.end_time as endTime, case t.export_reason when 0 then '用户分析' when 1 then '营销应用' when 2 then '其它' end as exportReason, t.applicant as applicant,u.name as applicantStr,t.path as path from data_export t left join t_user u on t.applicant=u.id where t.applicant=?");
        args.add(user.getId());
        if (!StringUtils.isEmpty(dataExport.getTaskNum())) {
            sql.append(" and t.task_num=?");
            args.add(dataExport.getTaskNum());
        }
        if (!StringUtils.isEmpty(dataExport.getExportType())) {
            sql.append(" and t.export_type=?");
            args.add(dataExport.getExportType());
        }
        if (!StringUtils.isEmpty(dataExport.getDataType())) {
            sql.append(" and t.data_type=?");
            args.add(dataExport.getDataType());
        }
        sql.append(" order by t.create_time desc ");
        if (page == null) {
            return dataExportDao.sqlQuery(sql.toString(),args.toArray());
        } else {
            return dataExportDao.sqlPageQuery(sql.toString(),page.getStart(),page.getLimit(),args.toArray()).getData();
        }

    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDataExportById(Integer id) {
        String sql = "select t.id as id,t.task_num as taskNum,t.export_type as exportType,t.cid as cid,t.customer_group_id as customerGroupId,t.output_label as outputLabel,t.data_type as dataType,t.start_time as startTime,t.end_time as endTime,t.export_reason as exportReason,t.export_reason_detail as exportReasonDeail,t.applicant as applicant,t.create_time as createTime,t.filename as filename,t.path as path,u.name as applicantStr from data_export t left join t_user u on t.applicant=u.id where t.id=?";

        List<Map<String, Object>> list =  dataExportDao.sqlQuery(sql,id);
        Map<String,Object> map = list.get(0);
//		Integer exportType = (Integer)map.get("exportType");
        String outputLabel = (String) map.get("outputLabel");
//		String sql1 = "select t.type as type, t.code as code, t.value as value from data_export_optional t where type=" + exportType + " and code in ("+outputLabel+")";
//		List<Map<String, Object>> list = dataExportDao.getSqlQuery(sql1, new HashMap()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
        String sql1 = "select GROUP_CONCAT(label_name) from label_info where id in (" + outputLabel + ")";
        String outputLabelStr = (String) dataExportDao.getSQLQuery(sql1).uniqueResult();
        //map.put("outputLabelStr", outputLabelStr);
        map.put("outputLabel", outputLabelStr);
        return map;
    }

    public Long getDataExportListTotal(DataExport dataExport, User user) {
		/*Map<String, Object> map  = new HashMap<String, Object>();
		map.put("taskNum", dataExport.getTaskNum());
		map.put("exportType", dataExport.getExportType());
		map.put("dataType", dataExport.getDataType());
		Object count = dataExportDao.getHqlQuery("select count(1) from DataExport t where 1=1", map, new HashMap<String, Object>(), null).uniqueResult();*/
        List args=new ArrayList();
		StringBuilder sql = new StringBuilder("select count(1) from DataExport t where t.applicant = ?");
		args.add(user.getId());
		//sql.append(user.getId());
        if (!StringUtils.isEmpty(dataExport.getTaskNum())) {
            sql.append(" and taskNum = ?");
            args.add(dataExport.getTaskNum());
        }
        if (null != dataExport.getExportType() && 0 != dataExport.getExportType()) {
            sql.append(" and exportType = ?");
            args.add(dataExport.getExportType());
        }
        if (null != dataExport.getDataType() && 0 != dataExport.getDataType()) {
            sql.append(" and dataType = ?");
            args.add(dataExport.getDataType());
        }
        Object count = dataExportDao.createQuery(sql.toString(), args.toArray()).uniqueResult();
        return (Long) count;
    }

    public List<Map<String, Object>> getLabelTree(HttpServletRequest request, String outputLabel, Long userId) {
        List<DataNode> lst = new ArrayList<DataNode>();
        List<Map<String, Object>> lstMapRet = null;
        List<DataNode> lstMedia = dataPermissionService.getCategoryList(userId, 2, null, QueryType.PRIVILEGE, CategoryType.MEDIA);
        List<DataNode> lstProduct = dataPermissionService.getCategoryList(userId, 2, null, QueryType.PRIVILEGE, CategoryType.PRODUCT);
        if (null != lstProduct && lstProduct.size() > 0) {
            lst.addAll(lstProduct);

            for (DataNode dn : lstProduct) {
                lst.addAll(dn.getChildren());
            }
        }
        if (null != lstMedia && lstMedia.size() > 0) {
            lst.addAll(lstMedia);
            for (DataNode dn : lstMedia) {
                lst.addAll(dn.getChildren());
            }
        }
        List<Integer> ids = new ArrayList<Integer>();
        if (lst.size() > 0 && outputLabel != null && outputLabel.split(",").length > 0) {
            for (String id : outputLabel.split(",")) {
                ids.add(Integer.valueOf(id));
            }
			/*List<Map<String, Object>> lstMap = labelInfoDao.getSQLQuery("select id as id, parent_id as parentId,category_id as categoryId from label_info where parent_id in (:ids) order by parent_id")
					.setParameterList("ids", ids).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();*/
            List<Map<String, Object>> lstMap = labelInfoDao.getSQLQuery("select t.id as id, t.label_id as labelID, t.parent_id as parentId,t1.category_id as categoryId from label_info t force index (parent_id) left join category_class_rel t1 on t.id=t1.lid where t.parent_id in (:ids) order by t.parent_id")
                    .setParameterList("ids", ids).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            ids = new ArrayList<Integer>();
            for (Iterator it = lstMap.iterator(); it.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) it.next();
                Object categoryId = map.get("categoryId");
                boolean b = false;
                for (DataNode dn : lst) {
                    Object id = dn.getId();
                    if (id.equals(categoryId)) {
                        b = true;
                        break;
                    }
                    if (categoryId != null) {
                        //ids.add(new Integer(map.get("id").toString()));
                        ids.add(new Integer(map.get("id").toString()));
                    }
                }
                if (!b) {
                    it.remove();
                }
            }
            List<Map<String, Object>> lstMap2 = new ArrayList<Map<String, Object>>();
            if (ids.size() > 0) {
				/*lstMap2 = labelInfoDao.getSQLQuery("select id as id, parent_id as parentId,category_id as categoryId from label_info where parent_id in (:ids) order by parent_id")
						.setParameterList("ids", ids).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();*/
                lstMap2 = labelInfoDao.getSQLQuery("select t.id as id, t.label_id as labelID, t.parent_id as parentId,t1.category_id as categoryId from label_info t force index (parent_id) left join category_class_rel t1 on t.id=t1.lid where t.parent_id in (:ids) order by t.parent_id")
                        .setParameter("ids", ids).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            }

            for (Iterator it = lstMap2.iterator(); it.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) it.next();
                Object categoryId = map.get("categoryId");
                boolean b = false;
                for (DataNode dn : lst) {
                    Object id = dn.getId();
                    if (id.equals(categoryId)) {
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    it.remove();
                }
            }


            List<Map<String, Object>> lstRoot = new ArrayList<Map<String, Object>>();
            for (String id : outputLabel.split(",")) {
                Map<String, Object> map = new HashMap<String, Object>();
                //map.put("id", new Integer(id));
                LabelInfo li = labelInfoDao.get(new Integer(id));
                map.put("id", new Integer(id));
                map.put("labelID", li.getLabelId());
                lstRoot.add(map);
            }
            lstMapRet = getLabelTree(lstRoot, getLabelTree(lstMap, lstMap2));
        }
        return lstMapRet;
    }

    private List<Map<String, Object>> getLabelTree(List<Map<String, Object>> lstMap, List<Map<String, Object>> lstMap2) {
        List<Map<String, Object>> lstMapRet = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> l1 = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < lstMap.size(); i++) {
            Map<String, Object> map1 = lstMap.get(i);
            //Object id = map1.get("id");
            Object id = map1.get("id");
            for (Map<String, Object> map : lstMap2) {
                Object parentId = map.get("parentId");
                if (id.equals(parentId)) {
                    l1.add(map);
                }
            }
            if (l1.size() > 0)
                map1.put("children", l1);
            lstMapRet.add(map1);
            l1 = new ArrayList<Map<String, Object>>();
        }
        return lstMapRet;
    }

    public String getTaskNum() {
        Object taskNum = dataExportDao.createQuery("select max(taskNum) from DataExport", new Object[]{}).uniqueResult();
        if (!StringUtils.isEmpty(taskNum)) {
            return taskNum.toString();
        }
        return null;
    }

    /**
     * 根据老版本客户群表t_customer_group_list_客户ID和Excel4J-2.1.4工具类导出excel
     *
     * @param response
     * @param customerId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     * @throws Exception
     */
    public void downloadCustomerGroupExcelData(HttpServletResponse response, String customerId, String invitationLabelId, String invitationLabelName, String invitationLabelValue, Date startTime, Date endTime) throws Exception {
        // 处理时间
        String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (startTime != null) {
            startTimeStr = LocalDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (endTime != null) {
            endTimeStr = LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        List<String> labelIdList = new ArrayList<>();
        // 处理表头
        List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
        List<String> headers = new ArrayList<>();
        for (Map<String, Object> map : labelNames) {
            if (map != null && map.get("label_name") != null) {
                headers.add(String.valueOf(map.get("label_name")));
                labelIdList.add(String.valueOf(map.get("label_id")));
                if (StringUtil.isEmpty(invitationLabelId)) {
                    if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(String.valueOf(map.get("label_name")))) {
                        invitationLabelId = String.valueOf(map.get("label_id"));
                    }
                }
            }
        }
        headers.add("身份ID");
        headers.add("操作人");
        headers.add("时间");
        headers.add("录音");
        if (StringUtil.isNotEmpty(invitationLabelId)) {
            // 获取邀约成功的用户
            List<String> superIds = new ArrayList<>();
            List<Map<String, Object>> invitationLabels = dataExportDao.sqlQuery("SELECT t1.option_value, t1.super_id superid FROM t_super_label t1  WHERE t1.super_id IN (SELECT id FROM t_customer_group_list_" + customerId + ") AND t1.cust_group_id IN (SELECT customer_group_id FROM t_customer_group_list_" + customerId + " )" +
                    " AND t1.label_id = ? ", new Object[]{invitationLabelId});
            for (Map<String, Object> map : invitationLabels) {
                if (map != null && map.get("option_value") != null && StringUtil.isNotEmpty(invitationLabelValue) && invitationLabelValue.equals(String.valueOf(map.get("option_value")))) {
                    superIds.add(String.valueOf(map.get("superid")));
                }
            }
            if (superIds.size() > 0) {
                // 邀约成功的用户所选自定义属性
                List<Map<String, Object>> invitationSuperLabels = dataExportDao.sqlQuery("SELECT t1.cust_group_id ,t1.option_value, t1.super_id superid, t2.label_id, t2.label_name FROM t_super_label t1 " +
                        " LEFT JOIN t_customer_label t2 ON t2.label_id = t1.label_id WHERE t1.super_id IN ( " + org.apache.commons.lang.StringUtils.join(superIds, ",") + " ) AND t1.cust_group_id IN (SELECT customer_group_id FROM t_customer_group_list_" + customerId + " )");
                // 组合拼装为map,方便通过label_id和super_id快速查找数据
                Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);
                for (Map<String, Object> map : invitationSuperLabels) {
                    invitationSuperLabelMap.put(map.get("cust_group_id") + "_" + map.get("label_id") + "_" + map.get("superid"), map.get("option_value"));
                }

                // 获取邀约成功,拨打电话成功用户的通话记录
                StringBuilder callLogSql = new StringBuilder();
                callLogSql.append(" SELECT voice.user_id, voice.customer_group_id, tuser.REALNAME, voice.superid, voice.create_time, substring_index(callback.recordurl,'/' , -1 ) recordurl  FROM t_touch_voice_log voice ");
                callLogSql.append(" LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid ");
                callLogSql.append(" LEFT JOIN t_customer_user tuser ON tuser.id = voice.user_id ");
                callLogSql.append(" WHERE voice.cust_id = ? ");
                callLogSql.append(" AND voice.type_code = 1 ");
                callLogSql.append(" AND voice.status = 1001 ");
                callLogSql.append(" AND voice.create_time > ? AND voice.create_time < ? ");
                if (superIds.size() > 0) {
                    callLogSql.append(" AND voice.superid IN  (  ");
                    for (String superId : superIds) {
                        callLogSql.append("'" + superId.trim() + "',");
                    }
                    callLogSql.deleteCharAt(callLogSql.length() - 1);
                    callLogSql.append(" )");
                }

                List<Map<String, Object>> callLogList = dataExportDao.sqlQuery(callLogSql.toString(), customerId, startTimeStr, endTimeStr);
                //构造excel返回数据
                String fileName;
                List<Map<String, Object>> customerList = dataExportDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                if (customerList.size() > 0) {
                    fileName = customerList.get(0).get("enterprise_name") + "";
                } else {
                    fileName = "客户";
                }
                fileName += "-营销数据-" + LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String fileType = ".xlsx";
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                List<List<Object>> data = new ArrayList<>();
                List<Object> columnList;
                String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                for (Map<String, Object> row : callLogList) {
                    columnList = new ArrayList<>();
                    for (String header : labelIdList) {
                        if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                            columnList.add(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")));
                        } else {
                            columnList.add("");
                        }
                    }
                    columnList.add(row.get("superid"));
                    columnList.add(row.get("REALNAME"));
                    columnList.add(row.get("create_time"));
                    if (row.get("recordurl") != null && !"null".equals(String.valueOf(row.get("recordurl")))
                            && !"NoTapes".equals(String.valueOf(row.get("recordurl")))) {
                        columnList.add(audioUrl + row.get("user_id") + "/" + row.get("recordurl"));
                    } else {
                        columnList.add("");
                    }
                    data.add(columnList);
                }
                OutputStream outputStream = response.getOutputStream();
                ExcelUtils.getInstance().exportObjects2Excel(data, headers, outputStream);
                outputStream.flush();
                response.flushBuffer();
                outputStream.close();
            }
        }
    }

    /**
     * 根据老版本客户群表t_customer_group_list_客户ID和easyexcel-1.0.4工具类导出excel
     *
     * @param response
     * @param customerId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     * @throws Exception
     */
    public void downloadCustomerGroupExcelData0(HttpServletResponse response, String customerId, String invitationLabelId, String invitationLabelName,
                                                String invitationLabelValue, String startTime, String endTime) throws Exception {
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
        List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
        // 处理表头
        List<List<String>> headers = new ArrayList<>();
        List<String> head;
        for (Map<String, Object> map : labelNames) {
            if (map != null && map.get("label_name") != null) {
                head = new ArrayList<>();
                head.add(String.valueOf(map.get("label_name")));
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
        head.add("操作人");
        headers.add(head);
        head = new ArrayList<>();
        head.add("时间");
        headers.add(head);
        head = new ArrayList<>();
        head.add("录音");
        headers.add(head);
        if (StringUtil.isNotEmpty(invitationLabelId)) {
            Set<String> customerGroupSets = new HashSet<>();
            // 查询客户下的所有客户群
            List<Map<String, Object>> customerGroupIds = dataExportDao.sqlQuery("SELECT id FROM customer_group WHERE cust_id = ? ", customerId);
            for (Map<String, Object> map : customerGroupIds) {
                if (map != null && map.get("id") != null) {
                    customerGroupSets.add(String.valueOf(map.get("id")));
                }
            }
            // 根据客户群ID查询用户数据superIds
            Set<String> superIdSets = new HashSet<>();
            List<Map<String, Object>> superIdList = dataExportDao.sqlQuery("SELECT id FROM t_customer_group_list_" + customerId +
                    " WHERE customer_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")");
            for (Map<String, Object> map : superIdList) {
                if (map != null && map.get("id") != null) {
                    superIdSets.add(String.valueOf(map.get("id")));
                }
            }
            // 获取邀约成功的用户
            Set<String> superIds = new HashSet<>();
            List<Map<String, Object>> invitationLabels = dataExportDao.sqlQuery("SELECT t1.option_value, t1.super_id superid FROM t_super_label t1  " +
                    " WHERE t1.super_id IN (" + SqlAppendUtil.sqlAppendWhereIn(superIdSets) + ") " +
                    " AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")" +
                    " AND t1.label_id = ? ", invitationLabelId);
            for (Map<String, Object> map : invitationLabels) {
                if (map != null && map.get("option_value") != null && StringUtil.isNotEmpty(invitationLabelValue) && invitationLabelValue.equals(String.valueOf(map.get("option_value")))) {
                    superIds.add(String.valueOf(map.get("superid")));
                }
            }
            if (superIds.size() > 0) {
                // 邀约成功的用户所选自定义属性
                List<Map<String, Object>> invitationSuperLabels = dataExportDao.sqlQuery("SELECT t1.cust_group_id ,t1.option_value, t1.super_id superid, t2.label_id, t2.label_name FROM t_super_label t1 " +
                        " LEFT JOIN t_customer_label t2 ON t2.label_id = t1.label_id WHERE t1.super_id IN (" + SqlAppendUtil.sqlAppendWhereIn(superIds) + ") " +
                        "AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")");
                // 组合拼装为map,方便通过label_id和super_id快速查找数据
                Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);
                for (Map<String, Object> map : invitationSuperLabels) {
                    invitationSuperLabelMap.put(map.get("cust_group_id") + "_" + map.get("label_id") + "_" + map.get("superid"), map.get("option_value"));
                }

                Set<String> userIdSets = new HashSet<>();
                Set<String> callSidSets = new HashSet<>();

                StringBuilder callLogSql = new StringBuilder();
                callLogSql.append(" SELECT voice.user_id, voice.customer_group_id, tuser.REALNAME, voice.superid, voice.create_time, substring_index(callback.recordurl,'/' , -1 ) recordurl  FROM t_touch_voice_log voice ");
                callLogSql.append(" LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid ");
                callLogSql.append(" LEFT JOIN t_customer_user tuser ON tuser.id = voice.user_id ");
                callLogSql.append(" WHERE voice.cust_id = ? ");
                callLogSql.append(" AND voice.type_code = 1 ");
                callLogSql.append(" AND voice.status = 1001 ");
                callLogSql.append(" AND voice.create_time > ? AND voice.create_time < ? ");
                if (superIds.size() > 0) {
                    callLogSql.append(" AND voice.superid IN  (" + SqlAppendUtil.sqlAppendWhereIn(superIds) + ") ");
                }
                // 获取邀约成功,拨打电话成功用户的通话记录
                List<Map<String, Object>> callLogList = dataExportDao.sqlQuery(callLogSql.toString(), customerId, startTimeStr, endTimeStr);
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("user_id") != null) {
                            userIdSets.add(String.valueOf(map.get("user_id")));
                        }
                        if (map.get("callSid") != null) {
                            callSidSets.add(String.valueOf(map.get("callSid")));
                        }
                    }
                }

                // 查询用户姓名
                Map<String, Object> realNameMap = new HashMap<>();
                if (userIdSets.size() > 0) {
                    List<Map<String, Object>> userList = dataExportDao.sqlQuery("SELECT id, REALNAME FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ") ");
                    for (Map<String, Object> map : userList) {
                        realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                    }
                }

                // 查询录音文件
                Map<String, Object> recordUrlMap = new HashMap<>();
                if (callSidSets.size() > 0) {
                    StringBuilder recordUrlSql = new StringBuilder();
                    recordUrlSql.append("SELECT callSid, recordurl FROM t_callback_info WHERE callSid IN ( " + SqlAppendUtil.sqlAppendWhereIn(callSidSets) + ")");
                    List<Map<String, Object>> recordUrlList = dataExportDao.sqlQuery(recordUrlSql.toString());
                    String recordUrl;
                    for (Map<String, Object> map : recordUrlList) {
                        recordUrl = String.valueOf(map.get("recordurl"));
                        if (StringUtil.isNotEmpty(recordUrl) && recordUrl.lastIndexOf("/") > 0) {
                            recordUrl = recordUrl.substring(recordUrl.lastIndexOf("/"), recordUrl.length());
                        }
                        recordUrlMap.put(String.valueOf(map.get("callSid")), recordUrl);
                    }
                }

                //构造excel返回数据
                String fileName;
                List<Map<String, Object>> customerList = dataExportDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                if (customerList.size() > 0) {
                    fileName = customerList.get(0).get("enterprise_name") + "";
                } else {
                    fileName = "客户";
                }
                fileName += "-营销数据-" + LocalDateTime.now().format(DATE_TIME_FORMATTER);
                final String fileType = ".xlsx";
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                List<List<String>> data = new ArrayList<>();
                List<String> columnList;
                final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                for (Map<String, Object> row : callLogList) {
                    columnList = new ArrayList<>();
                    for (String header : labelIdList) {
                        if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                            columnList.add(String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid"))));
                        } else {
                            columnList.add("");
                        }
                    }
                    columnList.add(String.valueOf(row.get("superid")));
                    columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                    columnList.add(String.valueOf(row.get("create_time")));
                    if (recordUrlMap.get(row.get("callSid")) != null && !"null".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))
                            && !"NoTapes".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))) {
                        columnList.add(audioUrl + row.get("user_id") + "/" + recordUrlMap.get(row.get("callSid")));
                    } else {
                        columnList.add("");
                    }
                    data.add(columnList);
                }
                OutputStream outputStream = response.getOutputStream();
                ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                Table table = new Table(3);
                table.setHead(headers);
                Sheet sheet1 = new Sheet(1, 0);
                sheet1.setSheetName("营销数据");
                writer.write0(data, sheet1, table);
                writer.finish();
                outputStream.flush();
                response.flushBuffer();
                outputStream.close();
            }
        }
    }

    /**
     * 根据新版本客户群表t_customer_group_list_客户群ID和easyexcel-1.0.4工具类导出excel
     *
     * @param response
     * @param customerId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     * @throws Exception
     */
    public void downloadCustomerGroupExcelData1(HttpServletResponse response, String customerId, String invitationLabelId, String invitationLabelName,
                                                String invitationLabelValue, String startTime, String endTime) throws Exception {
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
        List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
        // 处理表头
        List<List<String>> headers = new ArrayList<>();
        List<String> head;
        for (Map<String, Object> map : labelNames) {
            if (map != null && map.get("label_name") != null) {
                head = new ArrayList<>();
                head.add(String.valueOf(map.get("label_name")));
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
        head.add("操作人");
        headers.add(head);
        head = new ArrayList<>();
        head.add("时间");
        headers.add(head);
        head = new ArrayList<>();
        head.add("录音");
        headers.add(head);
        if (StringUtil.isNotEmpty(invitationLabelId)) {
            Set<String> customerGroupSets = new HashSet<>();
            // 根据客户群ID查询用户数据superIds
            Set<String> superIdSets = new HashSet<>();
            // 获取邀约成功,拨打电话成功用户的通话记录
            List<Map<String, Object>> callLogList = dataExportDao.sqlQuery("SELECT voice.user_id, voice.customer_group_id, voice.superid," +
                            "voice.create_time, voice.callSid FROM t_touch_voice_log voice " +
                            " LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid " +
                            " WHERE voice.cust_id = ? AND voice.type_code = 1 AND voice.status = 1001 " +
                            " AND voice.create_time >= ? AND voice.create_time <= ? ",
                    customerId, startTimeStr, endTimeStr);

            for (Map<String, Object> map : callLogList) {
                if (map != null) {
                    if (map.get("customer_group_id") != null) {
                        customerGroupSets.add(String.valueOf(map.get("customer_group_id")));
                    }
                    if (map.get("superid") != null) {
                        superIdSets.add(String.valueOf(map.get("superid")));
                    }
                }
            }
            // 客户群ID和身份ID大于0
            if (superIdSets.size() > 0 && customerGroupSets.size() > 0) {
                // 获取邀约成功的用户
                Set<String> invitationSupers = new HashSet<>();
                List<Map<String, Object>> invitationLabels = dataExportDao.sqlQuery("SELECT t1.option_value, t1.super_id superid FROM t_super_label t1  " +
                        " WHERE t1.super_id IN (" + SqlAppendUtil.sqlAppendWhereIn(superIdSets) + ") " +
                        " AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")" +
                        " AND t1.label_id = ? ", invitationLabelId);
                for (Map<String, Object> map : invitationLabels) {
                    if (map != null && map.get("option_value") != null && StringUtil.isNotEmpty(invitationLabelValue) && invitationLabelValue.equals(String.valueOf(map.get("option_value")))) {
                        invitationSupers.add(String.valueOf(map.get("superid")));
                    }
                }
                if (invitationSupers.size() > 0) {
                    // 邀约成功的用户所选自定义属性
                    List<Map<String, Object>> invitationSuperLabels = dataExportDao.sqlQuery("SELECT t1.cust_group_id ,t1.option_value, t1.super_id superid, t2.label_id, t2.label_name FROM t_super_label t1 " +
                            " LEFT JOIN t_customer_label t2 ON t2.label_id = t1.label_id WHERE t1.super_id IN (" + SqlAppendUtil.sqlAppendWhereIn(invitationSupers) + ") " +
                            " AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")");
                    // 组合拼装为map,方便通过label_id和super_id快速查找数据
                    Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                    Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);
                    for (Map<String, Object> map : invitationSuperLabels) {
                        invitationCustGroupSuperMap.put(map.get("cust_group_id") + "_" + map.get("superid"), map.get("option_value"));
                        invitationSuperLabelMap.put(map.get("cust_group_id") + "_" + map.get("label_id") + "_" + map.get("superid"), map.get("option_value"));
                    }

                    Set<String> userIdSets = new HashSet<>();
                    Set<String> callSidSets = new HashSet<>();

                    for (Map<String, Object> map : callLogList) {
                        if (map != null) {
                            if (map.get("user_id") != null) {
                                userIdSets.add(String.valueOf(map.get("user_id")));
                            }
                            if (map.get("callSid") != null) {
                                callSidSets.add(String.valueOf(map.get("callSid")));
                            }
                        }
                    }

                    // 查询用户姓名
                    Map<String, Object> realNameMap = new HashMap<>();
                    List<Map<String, Object>> userList = dataExportDao.sqlQuery("SELECT id, REALNAME FROM t_customer_user WHERE id IN (" + org.apache.commons.lang.StringUtils.join(userIdSets, ",") + ") ");
                    for (Map<String, Object> map : userList) {
                        realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                    }

                    // 查询录音文件
                    Map<String, Object> recordUrlMap = new HashMap<>();
                    StringBuilder recordUrlSql = new StringBuilder();
                    recordUrlSql.append("SELECT callSid, recordurl FROM t_callback_info WHERE callSid IN ( ");
                    for (String callSid : callSidSets) {
                        recordUrlSql.append("'")
                                .append(callSid)
                                .append("',");
                    }
                    recordUrlSql.deleteCharAt(recordUrlSql.length() - 1);
                    recordUrlSql.append(" )");
                    List<Map<String, Object>> recordUrlList = dataExportDao.sqlQuery(recordUrlSql.toString());
                    String recordUrl;
                    for (Map<String, Object> map : recordUrlList) {
                        recordUrl = String.valueOf(map.get("recordurl"));
                        if (StringUtil.isNotEmpty(recordUrl) && recordUrl.lastIndexOf("/") > 0) {
                            recordUrl = recordUrl.substring(recordUrl.lastIndexOf("/"), recordUrl.length());
                        }
                        recordUrlMap.put(String.valueOf(map.get("callSid")), recordUrl);
                    }

                    //构造excel返回数据
                    String fileName;
                    List<Map<String, Object>> customerList = dataExportDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                    if (customerList.size() > 0) {
                        fileName = customerList.get(0).get("enterprise_name") + "";
                    } else {
                        fileName = "客户";
                    }
                    fileName += "-营销数据-" + LocalDateTime.now().format(DATE_TIME_FORMATTER);
                    final String fileType = ".xlsx";
                    response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                    response.setContentType("application/vnd.ms-excel;charset=utf-8");
                    List<List<String>> data = new ArrayList<>();
                    List<String> columnList;
                    final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                    for (Map<String, Object> row : callLogList) {
                        if (invitationCustGroupSuperMap.get(row.get("customer_group_id") + "_" + row.get("superid")) != null) {
                            columnList = new ArrayList<>();
                            for (String header : labelIdList) {
                                if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                                    columnList.add(String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid"))));
                                } else {
                                    columnList.add("");
                                }
                            }
                            columnList.add(String.valueOf(row.get("superid")));
                            columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                            columnList.add(String.valueOf(row.get("create_time")));
                            if (recordUrlMap.get(row.get("callSid")) != null && !"null".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))
                                    && !"NoTapes".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))) {
                                columnList.add(audioUrl + row.get("user_id") + "/" + recordUrlMap.get(row.get("callSid")));
                            } else {
                                columnList.add("");
                            }
                            data.add(columnList);
                        }
                    }
                    OutputStream outputStream = response.getOutputStream();
                    ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                    Table table = new Table(3);
                    table.setHead(headers);
                    Sheet sheet1 = new Sheet(1, 0);
                    sheet1.setSheetName("营销数据");
                    writer.write0(data, sheet1, table);
                    writer.finish();
                    outputStream.flush();
                    response.flushBuffer();
                    outputStream.close();
                }
            }
        }
    }

    /**
     * 根据新版本客户群表t_customer_group_list_客户群ID和easyexcel-1.0.4工具类导出excel
     *
     * @param response
     * @param customerId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     * @throws Exception
     */
    public void downloadCustomerGroupExcelData_V1(HttpServletResponse response, String customerId, String invitationLabelId, String invitationLabelName,
                                                  String invitationLabelValue, String startTime, String endTime) {
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            // 处理时间
            if (dataExportTime >= 0 && (System.currentTimeMillis() - dataExportTime) < 10 * 60 * 1000) {
                outputStream.write("{\"msg\":\"请稍后重试\"}".getBytes("UTF-8"));
                return;
            }
            dataExportTime = System.currentTimeMillis();
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
            List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
            // 处理表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
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
                Set<String> customerGroupSets = new HashSet<>();
                // 根据客户群ID查询用户数据superIds
                Set<String> superIdSets = new HashSet<>();
                // 获取邀约成功,拨打电话成功用户的通话记录
                List<Map<String, Object>> callLogList = dataExportDao.sqlQuery("SELECT voice.user_id, voice.customer_group_id, voice.superid," +
                                "voice.create_time, voice.callSid FROM t_touch_voice_log voice " +
                                " LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid " +
                                " WHERE voice.cust_id = ? AND voice.type_code = 1 AND voice.status = 1001 " +
                                " AND voice.create_time >= ? AND voice.create_time <= ? ",
                        customerId, startTimeStr, endTimeStr);
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("customer_group_id") != null) {
                            customerGroupSets.add(String.valueOf(map.get("customer_group_id")));
                        }
                        if (map.get("superid") != null) {
                            superIdSets.add(String.valueOf(map.get("superid")));
                        }
                    }
                }

                // 客户群ID和身份ID大于0
                if (superIdSets.size() > 0 && customerGroupSets.size() > 0) {
                    // 获取邀约成功的用户
                    Set<String> invitationSupers = new HashSet<>();
                    List<Map<String, Object>> invitationLabels = dataExportDao.sqlQuery("SELECT t1.option_value, t1.super_id superid FROM t_super_label t1  " +
                            " WHERE t1.super_id IN (" + SqlAppendUtil.sqlAppendWhereIn(superIdSets) + ") " +
                            " AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")" +
                            " AND t1.label_id = ? ", invitationLabelId);
                    for (Map<String, Object> map : invitationLabels) {
                        if (map != null && map.get("option_value") != null && StringUtil.isNotEmpty(invitationLabelValue)
                                && invitationLabelValue.equals(String.valueOf(map.get("option_value")))) {
                            invitationSupers.add(String.valueOf(map.get("superid")));
                        }
                    }
                    if (invitationSupers.size() > 0) {
                        // 邀约成功的用户所选自定义属性
                        List<Map<String, Object>> invitationSuperLabels = dataExportDao.sqlQuery("SELECT t1.cust_group_id ,t1.option_value, t1.super_id superid, t2.label_id, t2.label_name FROM t_super_label t1 " +
                                " LEFT JOIN t_customer_label t2 ON t2.label_id = t1.label_id WHERE t1.super_id IN (" + SqlAppendUtil.sqlAppendWhereIn(invitationSupers) + " )" +
                                " AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")");
                        // 组合拼装为map,方便通过label_id和super_id快速查找数据
                        Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                        Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);
                        for (Map<String, Object> map : invitationSuperLabels) {
                            invitationCustGroupSuperMap.put(map.get("cust_group_id") + "_" + map.get("superid"), map.get("option_value"));
                            invitationSuperLabelMap.put(map.get("cust_group_id") + "_" + map.get("label_id") + "_" + map.get("superid"), map.get("option_value"));
                        }

                        Set<String> userIdSets = new HashSet<>();
                        Set<String> callSidSets = new HashSet<>();
                        for (Map<String, Object> map : callLogList) {
                            if (map != null) {
                                if (map.get("user_id") != null) {
                                    userIdSets.add(String.valueOf(map.get("user_id")));
                                }
                                if (map.get("callSid") != null) {
                                    callSidSets.add(String.valueOf(map.get("callSid")));
                                }
                            }
                        }
                        // 查询用户姓名
                        Map<String, Object> realNameMap = new HashMap<>();
                        if (userIdSets.size() > 0) {
                            List<Map<String, Object>> userList = dataExportDao.sqlQuery("SELECT id, REALNAME FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                            for (Map<String, Object> map : userList) {
                                realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                            }
                        }

                        // 查询录音文件
                        Map<String, Object> recordUrlMap = new HashMap<>();
                        if (callSidSets.size() > 0) {
                            StringBuilder recordUrlSql = new StringBuilder();
                            recordUrlSql.append("SELECT callSid, recordurl FROM t_callback_info WHERE callSid IN ( " + SqlAppendUtil.sqlAppendWhereIn(callSidSets) + ")");
                            List<Map<String, Object>> recordUrlList = dataExportDao.sqlQuery(recordUrlSql.toString());
                            String recordUrl;
                            for (Map<String, Object> map : recordUrlList) {
                                recordUrl = String.valueOf(map.get("recordurl"));
                                if (StringUtil.isNotEmpty(recordUrl) && recordUrl.lastIndexOf("/") > 0) {
                                    recordUrl = recordUrl.substring(recordUrl.lastIndexOf("/"), recordUrl.length());
                                }
                                recordUrlMap.put(String.valueOf(map.get("callSid")), recordUrl);
                            }
                        }

                        // 查询手机号
                        Map<String, Object> phoneMap = phoneService.getPhoneMap(invitationSupers);
                       /* if (invitationSupers.size() > 0) {
                            StringBuilder phoneSql = new StringBuilder();
                            phoneSql.append("SELECT id, phone FROM u WHERE id IN ( ");
                            for (String superId : invitationSupers) {
                                phoneSql.append("'")
                                        .append(superId)
                                        .append("',");
                            }
                            phoneSql.deleteCharAt(phoneSql.length() - 1);
                            phoneSql.append(" )");
                            List<Map<String, Object>> phoneList = dataExportDao.sqlQuery(phoneSql.toString());
                            for (Map<String, Object> map : phoneList) {
                                phoneMap.put(String.valueOf(map.get("id")), map.get("phone"));
                            }
                        }*/

                        //构造excel返回数据
                        String fileName;
                        List<Map<String, Object>> customerList = dataExportDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                        if (customerList.size() > 0) {
                            fileName = customerList.get(0).get("enterprise_name") + "";
                        } else {
                            fileName = "客户";
                        }

                        List<List<String>> data = new ArrayList<>();
                        List<String> columnList;
                        final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                        for (Map<String, Object> row : callLogList) {
                            if (invitationCustGroupSuperMap.get(row.get("customer_group_id") + "_" + row.get("superid")) != null) {
                                columnList = new ArrayList<>();
                                for (String header : labelIdList) {
                                    if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                                        columnList.add(String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid"))));
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
                                columnList.add(marketResourceService.getPhoneAttributionAreaV1(1, String.valueOf(phoneMap.get(String.valueOf(row.get("superid")))), String.valueOf(row.get("customer_group_id"))));
                                //姓名
                                columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                                columnList.add(String.valueOf(row.get("create_time")));

                                if (recordUrlMap.get(row.get("callSid")) != null && !"null".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))
                                        && !"NoTapes".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))) {
                                    columnList.add(audioUrl + row.get("user_id") + "/" + recordUrlMap.get(row.get("callSid")));
                                } else {
                                    columnList.add("");
                                }
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
                            dataExportTime = 0;
                        } else {
                            outputStream.write("{\"msg\":\"无数据\"}".getBytes("UTF-8"));
                            dataExportTime = 0;
                        }

                    } else {
                        outputStream.write("{\"msg\":\"无满足的用户\"}".getBytes("UTF-8"));
                        dataExportTime = 0;
                    }
                } else {
                    outputStream.write("{\"msg\":\"客户下无客户群\"}".getBytes("UTF-8"));
                    dataExportTime = 0;
                }
            } else {
                outputStream.write("{\"msg\":\"无满足条件的自建属性\"}".getBytes("UTF-8"));
                dataExportTime = 0;
            }
        } catch (Exception e) {
            log.error("导出营销数据异常,", e);
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

    public void downloadCustomerGroupExcelData_V1_1(HttpServletResponse response, String customerId, String invitationLabelId, String invitationLabelName,
                                                    String invitationLabelValue, String startTime, String endTime) {
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            // 处理时间
            if (dataExportTime >= 0 && (System.currentTimeMillis() - dataExportTime) < 5 * 60 * 1000) {
                outputStream.write("{\"msg\":\"请稍后重试\"}".getBytes("UTF-8"));
                return;
            }
            dataExportTime = System.currentTimeMillis();
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
            List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
            // 处理表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
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
                Set<String> customerGroupSets = new HashSet<>();
                // 根据客户群ID查询用户数据superIds
                Set<String> superIdSets = new HashSet<>();
                // 获取邀约成功,拨打电话成功用户的通话记录
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT voice.user_id, voice.customer_group_id, voice.superid,")
                        .append(" voice.create_time, voice.callSid, t_super_label.option_value FROM t_touch_voice_log voice ")
                        .append(" LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid ")
                        .append(" JOIN t_super_label ON t_super_label.super_id = voice.superid AND t_super_label.label_id = ? AND t_super_label.cust_group_id = voice.customer_group_id ")
                        .append(" WHERE voice.cust_id = ? AND voice.type_code = 1 AND voice.status = 1001 ")
                        .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                        .append(" AND FIND_IN_SET(t_super_label.option_value, ?)");

                List<Map<String, Object>> callLogList = dataExportDao.sqlQuery(sql.toString(), invitationLabelId, customerId, startTimeStr, endTimeStr, invitationLabelValue);
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("superid") != null) {
                            superIdSets.add(String.valueOf(map.get("superid")));
                        }
                        if (map.get("customer_group_id") != null) {
                            customerGroupSets.add(String.valueOf(map.get("customer_group_id")));
                        }
                    }
                }

                // 客户群ID和身份ID大于0
                if (superIdSets.size() > 0) {
                    // 获取邀约成功的用户
                    Set<String> invitationSupers = new HashSet<>();
                    for (Map<String, Object> map : callLogList) {
                        if (map != null && map.get("option_value") != null && StringUtil.isNotEmpty(invitationLabelValue)
                                && invitationLabelValue.equals(String.valueOf(map.get("option_value")))) {
                            invitationSupers.add(String.valueOf(map.get("superid")));
                        }

                    }
                    if (invitationSupers.size() > 0) {
                        // 邀约成功的用户所选自定义属性
                        List<Map<String, Object>> invitationSuperLabels = dataExportDao.sqlQuery("SELECT t1.cust_group_id ,t1.option_value, t1.super_id superid, t2.label_id, t2.label_name FROM t_super_label t1 " +
                                " LEFT JOIN t_customer_label t2 ON t2.label_id = t1.label_id WHERE t1.super_id IN (" + SqlAppendUtil.sqlAppendWhereIn(invitationSupers) + " )" +
                                " AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")");
                        // 组合拼装为map,方便通过label_id和super_id快速查找数据
                        Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                        Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);
                        for (Map<String, Object> map : invitationSuperLabels) {
                            invitationCustGroupSuperMap.put(map.get("cust_group_id") + "_" + map.get("superid"), map.get("option_value"));
                            invitationSuperLabelMap.put(map.get("cust_group_id") + "_" + map.get("label_id") + "_" + map.get("superid"), map.get("option_value"));
                        }

                        Set<String> userIdSets = new HashSet<>();
                        Set<String> callSidSets = new HashSet<>();
                        for (Map<String, Object> map : callLogList) {
                            if (map != null) {
                                if (map.get("user_id") != null) {
                                    userIdSets.add(String.valueOf(map.get("user_id")));
                                }
                                if (map.get("callSid") != null) {
                                    callSidSets.add(String.valueOf(map.get("callSid")));
                                }
                            }
                        }
                        // 查询用户姓名
                        Map<String, Object> realNameMap = new HashMap<>();
                        if (userIdSets.size() > 0) {
                            List<Map<String, Object>> userList = dataExportDao.sqlQuery("SELECT id, REALNAME FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                            for (Map<String, Object> map : userList) {
                                realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                            }
                        }

                        // 查询录音文件
                        Map<String, Object> recordUrlMap = new HashMap<>();
                        if (callSidSets.size() > 0) {
                            StringBuilder recordUrlSql = new StringBuilder();
                            recordUrlSql.append("SELECT callSid, recordurl FROM t_callback_info WHERE callSid IN ( " + SqlAppendUtil.sqlAppendWhereIn(callSidSets) + ")");
                            List<Map<String, Object>> recordUrlList = dataExportDao.sqlQuery(recordUrlSql.toString());
                            String recordUrl;
                            for (Map<String, Object> map : recordUrlList) {
                                recordUrl = String.valueOf(map.get("recordurl"));
                                if (StringUtil.isNotEmpty(recordUrl) && recordUrl.lastIndexOf("/") > 0) {
                                    recordUrl = recordUrl.substring(recordUrl.lastIndexOf("/"), recordUrl.length());
                                }
                                recordUrlMap.put(String.valueOf(map.get("callSid")), recordUrl);
                            }
                        }

                        // 查询手机号
                        Map<String, Object> phoneMap = phoneService.getPhoneMap(invitationSupers);
                       /* if (invitationSupers.size() > 0) {
                            StringBuilder phoneSql = new StringBuilder();
                            phoneSql.append("SELECT id, phone FROM u WHERE id IN ( ");
                            for (String superId : invitationSupers) {
                                phoneSql.append("'")
                                        .append(superId)
                                        .append("',");
                            }
                            phoneSql.deleteCharAt(phoneSql.length() - 1);
                            phoneSql.append(" )");
                            List<Map<String, Object>> phoneList = dataExportDao.sqlQuery(phoneSql.toString());
                            for (Map<String, Object> map : phoneList) {
                                phoneMap.put(String.valueOf(map.get("id")), map.get("phone"));
                            }
                        }*/

                        //构造excel返回数据
                        String fileName;
                        List<Map<String, Object>> customerList = dataExportDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                        if (customerList.size() > 0) {
                            fileName = customerList.get(0).get("enterprise_name") + "";
                        } else {
                            fileName = "客户";
                        }

                        List<List<String>> data = new ArrayList<>();
                        List<String> columnList;
                        final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                        for (Map<String, Object> row : callLogList) {
                            if (invitationCustGroupSuperMap.get(row.get("customer_group_id") + "_" + row.get("superid")) != null) {
                                columnList = new ArrayList<>();
                                for (String header : labelIdList) {
                                    if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                                        columnList.add(String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid"))));
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
                                columnList.add(marketResourceService.getPhoneAttributionAreaV1(1, String.valueOf(phoneMap.get(String.valueOf(row.get("superid")))), String.valueOf(row.get("customer_group_id"))));
                                //姓名
                                columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                                columnList.add(String.valueOf(row.get("create_time")));

                                if (recordUrlMap.get(row.get("callSid")) != null && !"null".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))
                                        && !"NoTapes".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))) {
                                    columnList.add(audioUrl + row.get("user_id") + "/" + recordUrlMap.get(row.get("callSid")));
                                } else {
                                    columnList.add("");
                                }
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
                            dataExportTime = 0;
                        } else {
                            outputStream.write("{\"msg\":\"无数据\"}".getBytes("UTF-8"));
                            dataExportTime = 0;
                        }

                    } else {
                        outputStream.write("{\"msg\":\"无满足的用户\"}".getBytes("UTF-8"));
                        dataExportTime = 0;
                    }
                } else {
                    outputStream.write("{\"msg\":\"客户下无客户群\"}".getBytes("UTF-8"));
                    dataExportTime = 0;
                }
            } else {
                outputStream.write("{\"msg\":\"无满足条件的自建属性\"}".getBytes("UTF-8"));
                dataExportTime = 0;
            }
        } catch (Exception e) {
            log.error("导出营销数据异常,", e);
            dataExportTime = 0;
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

    public void downloadCustomerGroupExcelData_V1_1_NoPhoneArea(HttpServletResponse response, String customerId, String invitationLabelId, String invitationLabelName,
                                                                String invitationLabelValue, String startTime, String endTime) {
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            // 处理时间
            if (dataExportTime >= 0 && (System.currentTimeMillis() - dataExportTime) < 5 * 60 * 1000) {
                outputStream.write("{\"msg\":\"请稍后重试\"}".getBytes("UTF-8"));
                return;
            }
            dataExportTime = System.currentTimeMillis();
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
            List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
            // 处理表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
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
                Set<String> customerGroupSets = new HashSet<>();
                // 根据客户群ID查询用户数据superIds
                Set<String> superIdSets = new HashSet<>();
                // 获取邀约成功,拨打电话成功用户的通话记录
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT voice.user_id, voice.customer_group_id, voice.superid,")
                        .append(" voice.create_time, voice.callSid, t_super_label.option_value FROM t_touch_voice_log voice ")
                        .append(" LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid ")
                        .append(" JOIN t_super_label ON t_super_label.super_id = voice.superid AND t_super_label.label_id = ? AND t_super_label.cust_group_id = voice.customer_group_id ")
                        .append(" WHERE voice.cust_id = ? AND voice.type_code = 1 AND voice.status = 1001 ")
                        .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                        .append(" AND FIND_IN_SET(t_super_label.option_value, ?)");

                List<Map<String, Object>> callLogList = dataExportDao.sqlQuery(sql.toString(), invitationLabelId, customerId, startTimeStr, endTimeStr, invitationLabelValue);
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("superid") != null) {
                            superIdSets.add(String.valueOf(map.get("superid")));
                        }
                        if (map.get("customer_group_id") != null) {
                            customerGroupSets.add(String.valueOf(map.get("customer_group_id")));
                        }
                    }
                }

                // 客户群ID和身份ID大于0
                if (superIdSets.size() > 0) {
                    // 获取邀约成功的用户
                    Set<String> invitationSupers = new HashSet<>();
                    for (Map<String, Object> map : callLogList) {
                        if (map != null && map.get("option_value") != null && StringUtil.isNotEmpty(invitationLabelValue)
                                && invitationLabelValue.equals(String.valueOf(map.get("option_value")))) {
                            invitationSupers.add(String.valueOf(map.get("superid")));

                        }
                    }
                    if (invitationSupers.size() > 0) {
                        // 邀约成功的用户所选自定义属性
                        List<Map<String, Object>> invitationSuperLabels = dataExportDao.sqlQuery("SELECT t1.cust_group_id ,t1.option_value, t1.super_id superid, t2.label_id, t2.label_name FROM t_super_label t1 " +
                                " LEFT JOIN t_customer_label t2 ON t2.label_id = t1.label_id WHERE t1.super_id IN (" + SqlAppendUtil.sqlAppendWhereIn(invitationSupers) + " )" +
                                " AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")");
                        // 组合拼装为map,方便通过label_id和super_id快速查找数据
                        Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                        Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);
                        for (Map<String, Object> map : invitationSuperLabels) {
                            invitationCustGroupSuperMap.put(map.get("cust_group_id") + "_" + map.get("superid"), map.get("option_value"));
                            invitationSuperLabelMap.put(map.get("cust_group_id") + "_" + map.get("label_id") + "_" + map.get("superid"), map.get("option_value"));
                        }

                        Set<String> userIdSets = new HashSet<>();
                        Set<String> callSidSets = new HashSet<>();
                        for (Map<String, Object> map : callLogList) {
                            if (map != null) {
                                if (map.get("user_id") != null) {
                                    userIdSets.add(String.valueOf(map.get("user_id")));
                                }
                                if (map.get("callSid") != null) {
                                    callSidSets.add(String.valueOf(map.get("callSid")));
                                }
                            }
                        }
                        // 查询用户姓名
                        Map<String, Object> realNameMap = new HashMap<>();
                        if (userIdSets.size() > 0) {
                            List<Map<String, Object>> userList = dataExportDao.sqlQuery("SELECT id, REALNAME FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                            for (Map<String, Object> map : userList) {
                                realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                            }
                        }

                        // 查询录音文件
                        Map<String, Object> recordUrlMap = new HashMap<>();
                        if (callSidSets.size() > 0) {
                            StringBuilder recordUrlSql = new StringBuilder();
                            recordUrlSql.append("SELECT callSid, recordurl FROM t_callback_info WHERE callSid IN ( " + SqlAppendUtil.sqlAppendWhereIn(callSidSets) + ")");
                            List<Map<String, Object>> recordUrlList = dataExportDao.sqlQuery(recordUrlSql.toString());
                            String recordUrl;
                            for (Map<String, Object> map : recordUrlList) {
                                recordUrl = String.valueOf(map.get("recordurl"));
                                if (StringUtil.isNotEmpty(recordUrl) && recordUrl.lastIndexOf("/") > 0) {
                                    recordUrl = recordUrl.substring(recordUrl.lastIndexOf("/"), recordUrl.length());
                                }
                                recordUrlMap.put(String.valueOf(map.get("callSid")), recordUrl);
                            }
                        }

                        // 查询手机号
                        Map<String, Object> phoneMap = phoneService.getPhoneMap(invitationSupers);
                       /* if (invitationSupers.size() > 0) {
                            StringBuilder phoneSql = new StringBuilder();
                            phoneSql.append("SELECT id, phone FROM u WHERE id IN ( ");
                            for (String superId : invitationSupers) {
                                phoneSql.append("'")
                                        .append(superId)
                                        .append("',");
                            }
                            phoneSql.deleteCharAt(phoneSql.length() - 1);
                            phoneSql.append(" )");
                            List<Map<String, Object>> phoneList = dataExportDao.sqlQuery(phoneSql.toString());
                            for (Map<String, Object> map : phoneList) {
                                phoneMap.put(String.valueOf(map.get("id")), map.get("phone"));
                            }
                        }*/

                        //构造excel返回数据
                        String fileName;
                        List<Map<String, Object>> customerList = dataExportDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                        if (customerList.size() > 0) {
                            fileName = customerList.get(0).get("enterprise_name") + "";
                        } else {
                            fileName = "客户";
                        }

                        List<List<String>> data = new ArrayList<>();
                        List<String> columnList;
                        final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                        for (Map<String, Object> row : callLogList) {
                            if (invitationCustGroupSuperMap.get(row.get("customer_group_id") + "_" + row.get("superid")) != null) {
                                columnList = new ArrayList<>();
                                for (String header : labelIdList) {
                                    if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                                        columnList.add(String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid"))));
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
                                columnList.add("");
                                //姓名
                                columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                                columnList.add(String.valueOf(row.get("create_time")));

                                if (recordUrlMap.get(row.get("callSid")) != null && !"null".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))
                                        && !"NoTapes".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))) {
                                    columnList.add(audioUrl + row.get("user_id") + "/" + recordUrlMap.get(row.get("callSid")));
                                } else {
                                    columnList.add("");
                                }
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
                            dataExportTime = 0;
                        } else {
                            outputStream.write("{\"msg\":\"无数据\"}".getBytes("UTF-8"));
                            dataExportTime = 0;
                        }

                    } else {
                        outputStream.write("{\"msg\":\"无满足的用户\"}".getBytes("UTF-8"));
                        dataExportTime = 0;
                    }
                } else {
                    outputStream.write("{\"msg\":\"客户下无客户群\"}".getBytes("UTF-8"));
                    dataExportTime = 0;
                }
            } else {
                outputStream.write("{\"msg\":\"无满足条件的自建属性\"}".getBytes("UTF-8"));
                dataExportTime = 0;
            }
        } catch (Exception e) {
            log.error("导出营销数据异常,", e);
            dataExportTime = 0;
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

    public void downloadCustomerGroupExcelData_V2(HttpServletResponse response, String customerId, String invitationLabelId, String invitationLabelName,
                                                  String invitationLabelValue, String startTime, String endTime) {
        OutputStream outputStream = null;
        Map<String, String> msg = new HashMap<>();
        try {
            outputStream = response.getOutputStream();
            // 处理时间
            if (dataExportTimeV2 >= 0 && (System.currentTimeMillis() - dataExportTimeV2) < 10 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", String.valueOf(dataExportTimeV2));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            dataExportTimeV2 = System.currentTimeMillis();

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
            List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
            // 处理表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
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
                Set<String> customerGroupSets = new HashSet<>();
                // 获取邀约成功,拨打电话成功用户的通话记录
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT voice.user_id, voice.customer_group_id, voice.superid," +
                        "voice.create_time, voice.callSid FROM t_touch_voice_log voice " +
                        " LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid " +
                        " WHERE voice.cust_id = ? AND voice.type_code = 1 AND voice.status = 1001 " +
                        " AND voice.create_time >= ? AND voice.create_time <= ? ");
                List<Map<String, Object>> callLogList = dataExportDao.sqlQuery(sql.toString(), customerId, startTimeStr, endTimeStr);
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("customer_group_id") != null) {
                            customerGroupSets.add(String.valueOf(map.get("customer_group_id")));
                        }
                    }
                }

                // 客户群ID大于0
                if (customerGroupSets.size() > 0) {
                    // 邀约成功的用户所选自定义属性
                    List<Map<String, Object>> invitationSuperLabels = dataExportDao.sqlQuery("SELECT t1.cust_group_id ,t1.option_value, t1.super_id superid, t2.label_id, t2.label_name FROM t_super_label t1 " +
                                    " LEFT JOIN t_customer_label t2 ON t2.label_id = t1.label_id WHERE exists " +
                                    "(SELECT 1 FROM t_touch_voice_log voice LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid " +
                                    " WHERE voice.superid = t1.super_id AND voice.cust_id = ? AND voice.type_code = 1 AND voice.status = 1001  AND voice.create_time >= ? AND voice.create_time <= ? )" +
                                    " AND t1.cust_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(customerGroupSets) + ")",
                            customerId, startTimeStr, endTimeStr);

                    // 组合拼装为map,方便通过label_id和super_id快速查找数据
                    Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                    Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);
                    // 获取邀约成功的用户
                    Set<String> invitationSupers = new HashSet<>();
                    for (Map<String, Object> map : invitationSuperLabels) {
                        invitationCustGroupSuperMap.put(map.get("cust_group_id") + "_" + map.get("superid"), map.get("option_value"));
                        invitationSuperLabelMap.put(map.get("cust_group_id") + "_" + map.get("label_id") + "_" + map.get("superid"), map.get("option_value"));
                        invitationSupers.add(String.valueOf(map.get("superid")));
                    }

                    Set<String> userIdSets = new HashSet<>();
                    Set<String> callSidSets = new HashSet<>();
                    for (Map<String, Object> map : callLogList) {
                        if (map != null) {
                            if (map.get("user_id") != null) {
                                userIdSets.add(String.valueOf(map.get("user_id")));
                            }
                            if (map.get("callSid") != null) {
                                callSidSets.add(String.valueOf(map.get("callSid")));
                            }
                        }
                    }
                    // 查询用户姓名
                    Map<String, Object> realNameMap = new HashMap<>();
                    if (userIdSets.size() > 0) {
                        List<Map<String, Object>> userList = dataExportDao.sqlQuery("SELECT id, REALNAME FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                        for (Map<String, Object> map : userList) {
                            realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                        }
                    }

                    // 查询录音文件
                    Map<String, Object> recordUrlMap = new HashMap<>();
                    if (callSidSets.size() > 0) {
                        StringBuilder recordUrlSql = new StringBuilder();
                        recordUrlSql.append("SELECT callSid, recordurl FROM t_callback_info WHERE callSid IN ( " + SqlAppendUtil.sqlAppendWhereIn(callSidSets) + ")");
                        List<Map<String, Object>> recordUrlList = dataExportDao.sqlQuery(recordUrlSql.toString());
                        String recordUrl;
                        for (Map<String, Object> map : recordUrlList) {
                            recordUrl = String.valueOf(map.get("recordurl"));
                            if (StringUtil.isNotEmpty(recordUrl) && recordUrl.lastIndexOf("/") > 0) {
                                recordUrl = recordUrl.substring(recordUrl.lastIndexOf("/"), recordUrl.length());
                            }
                            recordUrlMap.put(String.valueOf(map.get("callSid")), recordUrl);
                        }
                    }

                    // 查询手机号
                    Map<String, Object> phoneMap = phoneService.getPhoneMap(invitationSupers);
                    /*if (invitationSupers.size() > 0) {
                        StringBuilder phoneSql = new StringBuilder();
                        phoneSql.append("SELECT id, phone FROM u WHERE id IN ( ");
                        for (String superId : invitationSupers) {
                            phoneSql.append("'")
                                    .append(superId)
                                    .append("',");
                        }
                        phoneSql.deleteCharAt(phoneSql.length() - 1);
                        phoneSql.append(" )");
                        List<Map<String, Object>> phoneList = dataExportDao.sqlQuery(phoneSql.toString());
                        for (Map<String, Object> map : phoneList) {
                            phoneMap.put(String.valueOf(map.get("id")), map.get("phone"));
                        }
                    }*/

                    //构造excel返回数据
                    String fileName;
                    List<Map<String, Object>> customerList = dataExportDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                    if (customerList.size() > 0) {
                        fileName = customerList.get(0).get("enterprise_name") + "";
                    } else {
                        fileName = "客户";
                    }

                    List<List<String>> data = new ArrayList<>();
                    List<String> columnList;
                    final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                    String status;
                    for (Map<String, Object> row : callLogList) {
                        if (invitationCustGroupSuperMap.get(row.get("customer_group_id") + "_" + row.get("superid")) != null &&
                                invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + invitationLabelId + "_" + row.get("superid")) != null) {
                            status = String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + invitationLabelId + "_" + row.get("superid")));
                            if (StringUtil.isNotEmpty(status) && status.equals(invitationLabelValue)) {
                                columnList = new ArrayList<>();
                                for (String header : labelIdList) {
                                    if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                                        columnList.add(String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid"))));
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
                                columnList.add(marketResourceService.getPhoneAttributionAreaV1(1, String.valueOf(phoneMap.get(String.valueOf(row.get("superid")))), String.valueOf(row.get("customer_group_id"))));
                                //姓名
                                columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                                columnList.add(String.valueOf(row.get("create_time")));

                                if (recordUrlMap.get(row.get("callSid")) != null && !"null".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))
                                        && !"NoTapes".equals(String.valueOf(recordUrlMap.get(row.get("callSid"))))) {
                                    columnList.add(audioUrl + row.get("user_id") + "/" + recordUrlMap.get(row.get("callSid")));
                                } else {
                                    columnList.add("");
                                }
                                data.add(columnList);
                            }
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
                        //ExcelUtils.getInstance().exportObjects2Excel(data, headers, outputStream);
                        outputStream.flush();
                        response.flushBuffer();
                        outputStream.close();
                        dataExportTimeV2 = 0;
                    } else {
                        outputStream.write("{\"msg\":\"无数据\"}".getBytes("UTF-8"));
                        dataExportTimeV2 = 0;
                    }
                } else {
                    outputStream.write("{\"msg\":\"客户下无客户群\"}".getBytes("UTF-8"));
                    dataExportTimeV2 = 0;
                }
            } else {
                msg.put("msg", "无满足条件的自建属性");
                msg.put("data", String.valueOf(dataExportTimeV2));
                msg.put("invitationLabelName", invitationLabelName);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
            }
        } catch (Exception e) {
            log.error("导出营销数据异常,", e);
            msg.put("msg", "导出营销数据异常");
            msg.put("data", String.valueOf(dataExportTimeV2));
            msg.put("invitationLabelName", invitationLabelName);
            try {
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
            } catch (IOException e1) {
                log.error("导出营销数据异常,", e);
            }
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
     * 导出满足自建属性的单个客户群的营销数据
     *
     * @param response
     * @param customerId
     * @param customerGroupId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     */
    public void exportCustomerGroupMarketDataToExcelV3(HttpServletResponse response, String customerId, String customerGroupId, String invitationLabelId, String invitationLabelName,
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
            List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
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
                // 检查通话记录月表是否存在
                marketResourceDao.createVoiceLogTableNotExist(nowMonth);

                String labelDataLikeValue = "\"" + invitationLabelId + "\":\"" + invitationLabelValue + "\"";
                StringBuffer sql = new StringBuffer();
                // 获取邀约成功,拨打电话成功用户的通话记录
                sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.superid, voice.recordurl, ")
                        .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                        .append(" t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street ")
                        .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowMonth + " voice ")
                        .append(" JOIN t_customer_group_list_" + customerGroupId + " t ON t.id = voice.superid ")
                        .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? ")
                        .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                        .append(" AND voice.type_code = 1 AND voice.status = 1001 ")
                        .append(" AND t.super_data LIKE ? ");

                List<Map<String, Object>> callLogList = dataExportDao.sqlQuery(sql.toString(), invitationLabelId, customerId, startTimeStr, endTimeStr, invitationLabelValue,"%" + labelDataLikeValue + "%");
                // 有满足条件的营销记录
                if (callLogList.size() > 0) {
                    // 组合拼装为map,方便通过label_id和super_id快速查找数据
                    Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                    Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);

                    // 当前客户群下满足条件的身份ID集合
                    Set<String> superIdSets = new HashSet<>();
                    Set<String> userIdSets = new HashSet<>();
                    Map<String, Object> labelData;
                    String monthYear ="";
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
                        List<Map<String, Object>> userList = dataExportDao.sqlQuery("SELECT id, REALNAME FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                        for (Map<String, Object> map : userList) {
                            realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                        }
                    }

                    // 根据superId查询手机号
                    Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);

                    //构造excel返回数据
                    String fileName;
                    List<Map<String, Object>> customerList = dataExportDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
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
                            columnList.add(PhoneAreaUtil.replacePhone(phoneMap.get(String.valueOf(row.get("superid")))));
                            //归属地
                            columnList.add(marketResourceService.getPhoneAttributionAreaV1(1, String.valueOf(phoneMap.get(String.valueOf(row.get("superid")))), String.valueOf(row.get("customer_group_id"))));
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
     * 导出满足自建属性的单个客户群的营销数据
     *
     * @param response
     * @param customerId
     * @param
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     */
    public void exportCustomerMarketDataToExcelV3(HttpServletResponse response, String customerId, String invitationLabelId, String invitationLabelName,
                                                  String invitationLabelValue, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        msg.put("invitationLabelId", invitationLabelId);
        msg.put("invitationLabelName", invitationLabelName);
        msg.put("invitationLabelValue", invitationLabelValue);

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            // 处理时间
            if (customerDataExportTime >= 0 && (System.currentTimeMillis() - customerDataExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", String.valueOf(customerDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            customerDataExportTime = System.currentTimeMillis();
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
            List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
            headNames.add("身份ID");
            headNames.add("客户群ID");
            headNames.add("手机号");
            headNames.add("归属地");
            headNames.add("操作人");
            headNames.add("登录账号");
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
            head.add("登录账号");
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
                List<Map<String, Object>> list = null;
                List<Map<String, Object>> callLogList = new ArrayList<>();
                String labelDataLikeValue = "\"" + invitationLabelId + "\":\"" + invitationLabelValue + "\"";
                StringBuffer sql = new StringBuffer();
                // 查询客户下所有客户群
                List<Map<String, Object>> customerGroupIdList = dataExportDao.sqlQuery("SELECT id FROM customer_group WHERE status = 1 AND cust_id = ?", customerId);
                for (Map<String, Object> m : customerGroupIdList) {
                    // 检查通话记录月表是否存在
                    marketResourceDao.createVoiceLogTableNotExist(nowMonth);

                    // 获取邀约成功,拨打电话成功用户的通话记录
                    sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.superid, voice.recordurl, ")
                            .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                            .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street ")
                            .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowMonth + " voice ")
                            .append(" JOIN t_customer_group_list_" + m.get("id") + " t ON t.id = voice.superid ")
                            .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? ")
                            .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                            .append(" AND voice.status = 1001 ")
                            .append(" AND t.super_data LIKE ? ");
                    try {
                        list = dataExportDao.sqlQuery(sql.toString(), customerId, m.get("id"), startTimeStr, endTimeStr,"%" + labelDataLikeValue + "%");
                    } catch (SQLGrammarException e) {
                        log.warn("导出客户下全部营销数据失败:", e);
                    }
                    if (list != null && list.size() > 0) {
                        callLogList.addAll(list);
                    }
                    sql.setLength(0);
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
                    String monthYear ="";
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
                                        invitationCustGroupSuperMap.put(map.get("customer_group_id") + "_" + map.get("superid"), key.getValue());
                                        invitationSuperLabelMap.put(map.get("customer_group_id") + "_" + key.getKey() + "_" + map.get("superid"), key.getValue());
                                    }
                                }
                            }
                        }
                    }
                    // 查询用户姓名
                    Map<String, Object> realNameMap = new HashMap<>();
                    Map<String, Object> accountMap = new HashMap<>();
                    if (userIdSets.size() > 0) {
                        List<Map<String, Object>> userList = customGroupDao.sqlQuery("SELECT id, REALNAME, account FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                        for (Map<String, Object> map : userList) {
                            realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                            accountMap.put(String.valueOf(map.get("id")), map.get("account"));
                        }
                    }

                    // 根据superId查询手机号
                    Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);

                    //构造excel返回数据
                    String fileName;
                    List<Map<String, Object>> customerList = customGroupDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                    if (customerList.size() > 0) {
                        fileName = customerList.get(0).get("enterprise_name") + "";
                    } else {
                        fileName = "客户";
                    }

                    List<List<String>> data = new ArrayList<>();
                    List<String> columnList;

                    for (Map<String, Object> row : callLogList) {
                        if (invitationCustGroupSuperMap.get(row.get("customer_group_id") + "_" + row.get("superid")) != null) {
                            columnList = new ArrayList<>();
                            for (String header : labelIdList) {
                                if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                                    columnList.add(String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid"))));
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
                            //登录账号
                            columnList.add(String.valueOf(accountMap.get(String.valueOf(row.get("user_id")))));
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
                        customerDataExportTime = 0;
                    } else {
                        msg.put("msg", "客户群下无满足条件的客户数据");
                        msg.put("data", String.valueOf(customerDataExportTime));
                        outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                        customerDataExportTime = 0;
                        return;
                    }
                } else {
                    msg.put("msg", "客户群下无满足条件的客户数据");
                    msg.put("data", String.valueOf(customerDataExportTime));
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    customerDataExportTime = 0;
                    return;
                }
            } else {
                msg.put("msg", "无满足条件的自建属性");
                msg.put("data", String.valueOf(customerDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                customerDataExportTime = 0;
                return;
            }
        } catch (Exception e) {
            log.error("导出客户下的营销数据异常,", e);
            customerDataExportTime = 0;
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

    public void exportCustomerMarketDataToExcelV4(HttpServletResponse response, String customerId, String value, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        msg.put("value", value);

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            // 处理时间
            if (customerDataExportTime >= 0 && (System.currentTimeMillis() - customerDataExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", String.valueOf(customerDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            customerDataExportTime = System.currentTimeMillis();
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
            List<Map<String, Object>> labelNames = dataExportDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            Set<String> headNames = new HashSet<>();
            headNames.add("身份ID");
            headNames.add("客户群ID");
            headNames.add("手机号");
            headNames.add("归属地");
            headNames.add("操作人");
            headNames.add("登录账号");
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
            head.add("登录账号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("录音");
            headers.add(head);

            if (StringUtil.isNotEmpty(value)) {
                String nowMonth = DateUtil.getNowMonthToYYYYMM();
                if (StringUtil.isNotEmpty(startTimeStr) && StringUtil.isNotEmpty(endTimeStr)) {
                    nowMonth = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyyMM"));
                }
                List<Map<String, Object>> list = null;
                List<Map<String, Object>> callLogList = new ArrayList<>();
                String labelDataLikeValue = value;
                StringBuffer sql = new StringBuffer();
                // 查询客户下所有客户群
                List<Map<String, Object>> customerGroupIdList = dataExportDao.sqlQuery("SELECT id FROM customer_group WHERE status = 1 AND cust_id = ?", customerId);
                for (Map<String, Object> m : customerGroupIdList) {
                    // 检查通话记录月表是否存在
                    marketResourceDao.createVoiceLogTableNotExist(nowMonth);

                    // 获取邀约成功,拨打电话成功用户的通话记录
                    sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.superid, voice.recordurl, ")
                            .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                            .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street ")
                            .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowMonth + " voice ")
                            .append(" JOIN t_customer_group_list_" + m.get("id") + " t ON t.id = voice.superid ")
                            .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? ")
                            .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                            .append(" AND t.super_data LIKE ? ");
                    try {
                        list = dataExportDao.sqlQuery(sql.toString(), customerId, m.get("id"), startTimeStr, endTimeStr,"%" + labelDataLikeValue + "%");
                    } catch (SQLGrammarException e) {
                        log.warn("导出客户下全部营销数据失败:", e);
                    }
                    if (list != null && list.size() > 0) {
                        callLogList.addAll(list);
                    }
                    sql.setLength(0);
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
                    String monthYear ="";
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
                                        invitationCustGroupSuperMap.put(map.get("customer_group_id") + "_" + map.get("superid"), key.getValue());
                                        invitationSuperLabelMap.put(map.get("customer_group_id") + "_" + key.getKey() + "_" + map.get("superid"), key.getValue());
                                    }
                                }
                            }
                        }
                    }
                    // 查询用户姓名
                    Map<String, Object> realNameMap = new HashMap<>();
                    Map<String, Object> accountMap = new HashMap<>();
                    if (userIdSets.size() > 0) {
                        List<Map<String, Object>> userList = customGroupDao.sqlQuery("SELECT id, REALNAME, account FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                        for (Map<String, Object> map : userList) {
                            realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                            accountMap.put(String.valueOf(map.get("id")), map.get("account"));
                        }
                    }

                    // 根据superId查询手机号
                    Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);

                    //构造excel返回数据
                    String fileName;
                    List<Map<String, Object>> customerList = customGroupDao.sqlQuery("SELECT enterprise_name FROM t_customer WHERE cust_id = ?", new Object[]{customerId});
                    if (customerList.size() > 0) {
                        fileName = customerList.get(0).get("enterprise_name") + "";
                    } else {
                        fileName = "客户";
                    }

                    List<List<String>> data = new ArrayList<>();
                    List<String> columnList;

                    for (Map<String, Object> row : callLogList) {
                        if (invitationCustGroupSuperMap.get(row.get("customer_group_id") + "_" + row.get("superid")) != null) {
                            columnList = new ArrayList<>();
                            for (String header : labelIdList) {
                                if (invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid")) != null) {
                                    columnList.add(String.valueOf(invitationSuperLabelMap.get(row.get("customer_group_id") + "_" + header + "_" + row.get("superid"))));
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
                            //登录账号
                            columnList.add(String.valueOf(accountMap.get(String.valueOf(row.get("user_id")))));
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
                        customerDataExportTime = 0;
                    } else {
                        msg.put("msg", "客户群下无满足条件的客户数据");
                        msg.put("data", String.valueOf(customerDataExportTime));
                        outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                        customerDataExportTime = 0;
                        return;
                    }
                } else {
                    msg.put("msg", "客户群下无满足条件的客户数据");
                    msg.put("data", String.valueOf(customerDataExportTime));
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    customerDataExportTime = 0;
                    return;
                }
            } else {
                msg.put("msg", "无满足条件的自建属性");
                msg.put("data", String.valueOf(customerDataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                customerDataExportTime = 0;
                return;
            }
        } catch (Exception e) {
            log.error("导出客户下的营销数据异常,", e);
            customerDataExportTime = 0;
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
     * 判断用户是否有导出成功单权限
     *
     * @param custId
     * @return
     */
    public boolean checkCustExportPermission(String custId) {
        CustomerProperty cp = customerDao.getProperty(custId, "export_success_order");
        if (cp != null && "1".equals(cp.getPropertyValue())) {
            return true;
        }
        return false;
    }

}