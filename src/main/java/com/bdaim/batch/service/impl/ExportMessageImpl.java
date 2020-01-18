package com.bdaim.batch.service.impl;

import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.service.ExportMessageService;
import com.bdaim.util.ExcelUtil;
import com.bdaim.util.StringUtil;
import com.github.crab2died.ExcelUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author duanliying
 * @date 2018/9/11
 * @description
 */
@Service("exportMessageService")
@Transactional
public class ExportMessageImpl implements ExportMessageService {
    private static Logger logger = LoggerFactory.getLogger(ExportMessageImpl.class);
    @Resource
    private BatchDetailDao batchDetailDao;

    /**
     * @description 失联人员信息导出功能
     * @author:duanliying
     * @method
     * @date: 2018/9/11 9:25
     */
    @Override
    public void exportLostContactMessage(String labelListStr, String custId, String batchId, String realname, Long userId, String userType, String enterpriseId, String id, String idCard, String status, HttpServletResponse response, String role) throws IOException {
        //处理自建属性添加集合信息
        List<String> optionValues = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (labelListStr != null && !labelListStr.equals("")) {
            List<String> labelAndValue = Arrays.asList(labelListStr.split(";"));

            for (int i = 0; i < labelAndValue.size(); i++) {
                List<String> stringList = Arrays.asList(labelAndValue.get(i).split(","));
                labels.add(stringList.get(0));
                optionValues.add(stringList.get(1));
            }
        }
        // 处理表头
        List<String> labelIdList = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        headers.add("用户ID");
        headers.add("企业自带ID");
        headers.add("唯一标识");
        headers.add("标签一");
        headers.add("标签二");
        headers.add("标签三");
        headers.add("姓名");
        headers.add("年龄");
        headers.add("性别");
        headers.add("手机");
        headers.add("电话");
        headers.add("地址");
        headers.add("渠道");
        headers.add("负责人");
        headers.add("通话次数");
        headers.add("最后通话时间");
        headers.add("通话备注");
        //获取所有自定义属性的labelName  作为表头信息
        List<Map<String, Object>> labelNames = batchDetailDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{custId});
        Set<String> headNames = new HashSet<>();
        for (Map<String, Object> map : labelNames) {
            if (map != null && map.get("label_name") != null) {
                if (headNames.contains(String.valueOf(map.get("label_name")))) {
                    headers.add(String.valueOf(map.get("label_name")) + map.get("label_id"));
                } else {
                    headers.add(String.valueOf(map.get("label_name")));
                    headNames.add(String.valueOf(map.get("label_name")));
                }
                labelIdList.add(String.valueOf(map.get("label_id")));
            }
        }
        //获取勾选的失联人员信息根据唯一标识
        List<Map<String, Object>> propertyList = null;
        List<String> superIds = new ArrayList<>();
        List<List<Object>> data = new ArrayList<>();
        List<Map<String, Object>> messageList = new ArrayList<>();
        StringBuffer sqlBuilder = new StringBuffer();
        sqlBuilder.append("SELECT t.* FROM ( select custG.id,custG.batch_id,custG.user_id,custG.enterprise_id,custG.id_card,custG.label_one,custG.label_two,custG.label_three,custG.`status`,custG.channel, t.realname,t4.super_name,t4.super_age,t4.super_sex,t4.super_telphone,t4.super_phone,t4.super_address_street,GROUP_CONCAT(t2.label_id) AS labelId,\n" +
                "\tGROUP_CONCAT(t3.label_name) AS labelName,GROUP_CONCAT(t2.option_value) AS optionValue,");
        sqlBuilder.append(" (SELECT create_time FROM t_touch_voice_log where  t_touch_voice_log.superid = custG.id AND t_touch_voice_log.batch_id = custG.batch_id ORDER BY create_time DESC LIMIT 1) lastCallTime, ");
        sqlBuilder.append(" (SELECT COUNT(0) FROM t_touch_voice_log where t_touch_voice_log.superid = custG.id  AND t_touch_voice_log.batch_id = custG.batch_id ORDER BY create_time DESC LIMIT 1) callCount ");
        sqlBuilder.append("  from nl_batch_detail custG ");
        sqlBuilder.append("  LEFT JOIN t_customer_user t  ON custG.user_id = t.id");
        sqlBuilder.append("  LEFT JOIN t_touch_voice_info t4 ON custG.id= t4.super_id and custG.batch_id = t4.batch_id");
        sqlBuilder.append("  LEFT JOIN t_super_label t2 ON custG.id = t2.super_id AND t2.batch_id = '" + batchId + "'");
        sqlBuilder.append("  LEFT JOIN t_customer_label t3 ON t2.label_id = t3.label_id AND custG.batch_id = t4.batch_id");
        sqlBuilder.append(" where 1 = 1 ");
        List<Object> p = new ArrayList<>();
        if (userType.equals("2") && "ROLE_CUSTOMER".equals(role)) {
            sqlBuilder.append("AND custG.user_id = ? ");
            p.add(userId);
        }
        if (StringUtil.isNotEmpty(id)) {
            sqlBuilder.append(" and custG.id= ? ");
            p.add(id);
        }
        if (StringUtil.isNotEmpty(idCard)) {
            sqlBuilder.append(" and custG.id_card= ? ");
            p.add(idCard);
        }
        if (StringUtil.isNotEmpty(enterpriseId)) {
            sqlBuilder.append(" and custG.enterprise_id= ?");
            p.add(enterpriseId);
        }

        if (StringUtil.isNotEmpty(realname)) {
            sqlBuilder.append(" and t.realname= ?");
            p.add(realname);
        }
        if (StringUtil.isNotEmpty(status)) {
            sqlBuilder.append(" and custG.status= ?");
            p.add(status);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and custG.batch_id= ?");
            p.add(batchId);
        }

        sqlBuilder.append(" GROUP BY custG.id ,id_card");
        sqlBuilder.append(" ORDER BY custG.id DESC )t");
        if (labels.size() > 0) {
            sqlBuilder.append(" WHERE 1=1 ");
            for (String optionValue : optionValues) {
                sqlBuilder.append(" AND FIND_IN_SET(? , t.optionValue) ");
                p.add(optionValue);
            }
        }
        List<Map<String, Object>> list = batchDetailDao.sqlQuery(sqlBuilder.toString(), p.toArray());

        //处理渠道信息对应转换  2--联通  3--电信 4---移动
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {

                if (list.get(i).get("channel") != null) {
                    if (String.valueOf(list.get(i).get("channel")).equals("2")) {
                        list.get(i).put("channel", "联通");
                    }
                    if (String.valueOf(list.get(i).get("channel")).equals("3")) {
                        list.get(i).put("channel", "电信");
                    }
                    if (String.valueOf(list.get(i).get("channel")).equals("4")) {
                        list.get(i).put("channel", "移动");
                    }
                }
                //查询备注信息
                if (list.get(i).get("id") != null) {
                    //根据userid和左后通话时间查询出备注信息
                    String remarkSql = "select remark from t_touch_voice_log  where batch_id=? and superid = ? ORDER BY create_time DESC LIMIT 1";
                    List<Map<String, Object>> remarkList = batchDetailDao.sqlQuery(remarkSql.toString(), list.get(i).get("batch_id"), list.get(i).get("id"));
                    if (remarkList.size() > 0) {
                        String remark = String.valueOf(remarkList.get(0).get("remark"));
                        //备注{}企业{}批次   对备注信息进行截取
                        if (remark.contains("{}")) {
                            //截取字符串
                            List<String> remarks = Arrays.asList(remark.split("\\{}"));
                            list.get(i).put("remark", remarks.get(0));
                        } else {
                            list.get(i).put("remark", remarkList.get(0).get("remark"));
                        }
                    } else {
                        list.get(i).put("remark", "");
                    }
                }
            }
        }
        logger.info("导出失联人员信息sql" + sqlBuilder.toString());
        messageList.addAll(list);

        for (Map<String, Object> map : messageList) {
            if (map.get("id") != null) {
                superIds.add(String.valueOf(map.get("id")));
            }
        }
        if (superIds.size() > 0) {
            propertyList = getSelectedLabelsBySuperId(superIds, batchId);
        }
        //创建map用于存放自建属性值
        Map<String, Object> superLabelMap = new HashMap<>(16);
        if (propertyList != null) {
            for (Map<String, Object> map : propertyList) {
                superLabelMap.put(map.get("batch_id") + "_" + map.get("label_id") + "_" + map.get("super_id"), map.get("option_value"));
            }
        }
        //处理文件名字 根据时间段保证唯一
        String fileName = "-失联信息-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
        String fileType = ".xlsx";
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
        List<Object> rowList;
        for (Map<String, Object> column : messageList) {
            rowList = new ArrayList<>();
            rowList.add(column.get("id") != null ? column.get("id") : "");
            rowList.add(column.get("enterprise_id") != null ? column.get("enterprise_id") : "");
            rowList.add(column.get("idCard") != null ? column.get("idCard") : "");
            rowList.add(column.get("label_one") != null ? column.get("label_one") : "");
            rowList.add(column.get("label_two") != null ? column.get("label_two") : "");
            rowList.add(column.get("label_three") != null ? column.get("label_three") : "");
            rowList.add(column.get("super_name") != null ? column.get("super_name") : "");
            rowList.add(column.get("super_age") != null ? column.get("super_age") : "");
            /*rowList.add(column.get("super_sex") != null ? column.get("super_sex") : "");*/
            if ("0".equals(String.valueOf(column.get("super_sex")))) {
                rowList.add("女");
            } else if ("1".equals(String.valueOf(column.get("super_sex")))) {
                rowList.add("男");
            } else {
                rowList.add("");
            }
            rowList.add(column.get("super_telphone") != null ? column.get("super_telphone") : "");
            rowList.add(column.get("super_phone") != null ? column.get("super_phone") : "");
            rowList.add(column.get("super_address_street") != null ? column.get("super_address_street") : "");
            rowList.add(column.get("channel") != null ? column.get("channel") : "");
            rowList.add(column.get("realname") != null ? column.get("realname") : "");
            rowList.add(column.get("callCount") != null ? column.get("callCount") : "");
            rowList.add(column.get("lastCallTime") != null ? column.get("lastCallTime") : "");
            rowList.add(column.get("remark") != null ? column.get("remark") : "");
            for (String header : labelIdList) {
                if (superLabelMap.get(column.get("batch_id") + "_" + header + "_" + column.get("id")) != null) {
                    rowList.add(superLabelMap.get(column.get("batch_id") + "_" + header + "_" + column.get("id")));
                } else {
                    rowList.add("");
                }
            }

            data.add(rowList);
        }
        if (data.size() > 0) {
            //将获取的结果写入表格中
            OutputStream outputStream = response.getOutputStream();
            ExcelUtils.getInstance().exportObjects2Excel(data, headers, outputStream);
            outputStream.flush();
            response.flushBuffer();
            outputStream.close();
        }
    }

    /**
     * 批次详情导出
     *
     * @param batchId
     * @param status
     * @param response
     */
    @Override
    public void exportDetailInfo(String batchId, String detailId, Integer status, HttpServletResponse response, String exportType, String custId) throws IOException, IllegalAccessException {
        StringBuffer sb = new StringBuffer("SELECT content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3 detailId, ext_4 batchId, ext_5 scoure from h_data_manager_hy_pic_x WHERE ext_4 = ? ");
        List<Object> p = new ArrayList<>();
        p.add(batchId);
        if (status != null) {
            sb.append(" and ext_2 = ? ");
            p.add(status);
        }
        if (StringUtil.isNotEmpty(detailId)) {
            sb.append(" and ext_3 = ? ");
            p.add(detailId);
        }
        if (StringUtil.isNotEmpty(custId)) {
            sb.append(" and cust_id =? ");
            p.add(custId);
        }
        List<Map<String, Object>> list = batchDetailDao.sqlQuery(sb.toString(), p.toArray());
        ExcelUtil.exportExcelByList(list, exportType, response);
    }

    /**
     * @description 获取自定义属性值
     * @author:duanliying
     * @method
     * @date: 2018/9/11 13:44
     */
    public List<Map<String, Object>> getSelectedLabelsBySuperId(List<String> superIds, String batchId) {
        StringBuffer sb = new StringBuffer();
        sb.append("  SELECT t1.super_id,t1.batch_id,t1.option_value,t1.label_id  from  t_super_label t1")
                .append("  LEFT JOIN t_customer_label t2")
                .append("  ON t1.label_id = t2.label_id")
                .append("  WHERE 1=1  AND t2.status =1")
                .append("  and  t1.super_id  IN (" + com.bdaim.util.SqlAppendUtil.sqlAppendWhereIn(superIds) + " )")
                .append("  AND  t1.batch_id = ?");
        List<Map<String, Object>> list = batchDetailDao.sqlQuery(sb.toString(), batchId);
        return list;
    }

}
