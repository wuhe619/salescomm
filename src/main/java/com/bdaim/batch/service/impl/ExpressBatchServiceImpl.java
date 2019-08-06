package com.bdaim.batch.service.impl;

import com.bdaim.batch.dao.BatchInfoDao;
import com.bdaim.batch.dao.BatchInfoDetailDao;
import com.bdaim.batch.dao.BatchPropertyDao;
import com.bdaim.batch.service.ExpressBatchService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.*;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.common.util.spring.DataConverter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * @description: 发件批次 信息管理 (失联修复_信函模块)
 * @auther: Chacker
 * @date: 2019/7/31 16:02
 */
@Service
public class ExpressBatchServiceImpl implements ExpressBatchService {

    @Autowired
    private BatchInfoDao batchInfoDao;
    @Autowired
    private BatchInfoDetailDao batchInfoDetailDao;
    @Autowired
    private BatchPropertyDao batchPropertyDao;
    @Autowired
    private DataConverter dataConverter;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public ResponseInfo receiverInfoImport(MultipartFile multipartFile, String batchName, int expressContent, String custId) throws IOException {
        long time1 = System.currentTimeMillis();
        //1. 把excel文件上传到服务器中
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // (如果文件后缀名不对，则给与相应提示)
        if (!Constant.XLSX.equals(suffix) && !Constant.XLS.equals(suffix)) {
            return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "请保证文件格式为\".xls\"或\".xlsx\"");
        }
        String uploadPath = "/express/upload/";
        String batchId = String.valueOf(System.currentTimeMillis());
        // 文件路径的字符串拼接 目录 + 时间戳 + 毫秒
        uploadPath = uploadPath + batchId + suffix;
        File file = new File(uploadPath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        //为保证uploadPath定义的路径 在readExcel中和在创建file中使用的路径一样，所以使用FileUtils工具类
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        //2. 读取 & 解析excel文件(根据文件的后缀名进行区分)
        List<List<String>> contentList = ExcelReaderUtil.readExcel(uploadPath);
        if (contentList.size() > 1000) {
            return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "请保证一次上传数据不超过1000条记录");
        }
        //3. 把批次信息和批次详情列表存入数据库
        //3.1 设置并新增保存批次信息
        StringBuffer insertBatchSql = new StringBuffer("INSERT INTO nl_batch (id,batch_name,upload_time,comp_id,certify_type,channel,status," +
                "upload_num) VALUES ('");
        insertBatchSql.append(batchId + "','" + batchName + "',now(),'"
                + custId + "','-1','-1','1','" + (contentList.size() - 1) + "')");
        jdbcTemplate.update(insertBatchSql.toString());
        //把快递内容(1. 电子版 2. 打印版)存入批次属性表
        String insertBatchProperty = "INSERT INTO nl_batch_property (batch_id,property_name,property_value,create_time)" +
                " VALUES ('" + batchId + "','expressContentType','" + String.valueOf(expressContent) + "',NOW())";
        jdbcTemplate.update(insertBatchProperty);
        //3.2 获取并保存批次详情信息 (因为第一行为标题，所以从第二行开始遍历)
        for (int i = 1; i < contentList.size(); i++) {
            StringBuffer batchDetailSql = new StringBuffer("INSERT INTO nl_batch_detail (id,label_one,label_two,label_four,label_seven,batch_id) VALUES ('" +
                    String.valueOf(System.currentTimeMillis()) + "','" + contentList.get(i).get(0) + "','" + contentList.get(i).get(1) + "','" +
                    contentList.get(i).get(2) + "','1','" + batchId + "')");
            jdbcTemplate.update(batchDetailSql.toString());
        }
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);
        return new ResponseInfoAssemble().success(null);
    }

    @Override
    public Map<String, Object> batchList(Map<String, Object> map) throws IllegalAccessException {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(Integer.valueOf(String.valueOf(map.get("page_num"))));
        pageParam.setPageSize(Integer.valueOf(String.valueOf(map.get("page_size"))));

        StringBuffer sql = new StringBuffer("SELECT t1.id,t1.comp_id AS custId,t1.comp_name,t1.batch_name AS batchName," +
                "t1.certify_type AS certifyType,t1.channel,t1.repair_strategy,CASE t1.status WHEN '1' THEN '校验中'" +
                " WHEN '2' THEN '校验失败' WHEN '3' THEN '待上传' WHEN '4' THEN '待发件' WHEN '5' THEN '待发件'" +
                " WHEN '6' THEN '已发件' END AS status,t1.upload_num AS uploadNum,t1.success_num AS" +
                " successNum,DATE_FORMAT(t1.upload_time,'%Y-%m-%d %H:%i:%s') AS uploadTime,t2.property_name AS propertyName," +
                "t2.property_value AS propertyValue");
        sql.append(" FROM nl_batch t1 LEFT JOIN nl_batch_property t2 ON t1.id=t2.batch_id WHERE ");

        //企业ID
        String custId = String.valueOf(map.get("cust_id"));
        String nullString = "null";
        if (!nullString.equals(custId) && StringUtil.isNotEmpty(custId)) {
            sql.append(" t1.comp_id = '" + custId + "'");
        }
        //批次编号
        String batchId = String.valueOf(map.get("batch_id"));
        if (!nullString.equals(batchId) && StringUtil.isNotEmpty(batchId)) {
            sql.append(" AND t1.id = '" + batchId + "'");
        }
        //批次名称
        String batchName = String.valueOf(map.get("batch_name"));
        if (!nullString.equals(batchName) && StringUtil.isNotEmpty(batchName)) {
            sql.append(" AND t1.batch_name = '" + batchName + "'");
        }
        //状态
        String status = String.valueOf(map.get("status"));
        if (!nullString.equals(status) && StringUtil.isNotEmpty(status)) {
            sql.append(" AND t1.status = '" + status + "'");
        }
        //上传时间开始
        String startTime = String.valueOf(map.get("start_time"));
        if (!nullString.equals(startTime) && StringUtil.isNotEmpty(startTime)) {
            sql.append(" AND t1.upload_time >= '" + startTime + "'");
        }
        //上传时间截止
        String endTime = String.valueOf(map.get("end_time"));
        if (!nullString.equals(endTime) && StringUtil.isNotEmpty(endTime)) {
            sql.append(" AND t1.upload_time <='" + endTime + "'");
        }
        sql.append(" ORDER BY t1.upload_time DESC ");
        Page page = new Pagination().getPageData(sql.toString(), null, pageParam, jdbcTemplate);
        List<Map<String, Object>> list = page.getList();
        if (list != null && list.size() != 0) {
            for (Map<String, Object> tempMap : list) {
                String propertyValue = String.valueOf(tempMap.get("propertyValue"));
                if ("1".equals(propertyValue)) {
                    tempMap.put("expressContentType", "电子版");
                } else {
                    tempMap.put("expressContentType", "打印版");
                }
            }
        }
        Map<String, Object> resultMap = new HashMap<>(10);
        resultMap.put("rows", list);
        resultMap.put("total", list.size());
        return resultMap;
    }

    @Override
    public Map<String, Object> batchDetail(Map<String, Object> map) throws IllegalAccessException {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(NumberConvertUtil.parseInt(String.valueOf(map.get("page_num"))));
        pageParam.setPageSize(NumberConvertUtil.parseInt(String.valueOf(map.get("page_size"))));
        StringBuffer hql = new StringBuffer("SELECT t2.id,t2.batch_id AS batchId,t2.label_one AS name,t2.label_two AS phone,t2.label_four AS address," +
                "t2.label_six AS fileCode,t2.label_seven AS statusId,CASE t2.label_seven WHEN '1' THEN '有效' WHEN '2' THEN '无效' END AS checkingResult," +
                "CASE t2.status WHEN '1' THEN '待上传内容' WHEN '2' THEN '待发件' WHEN '3' THEN '待取件' WHEN '4' THEN '已发件' END AS status," +
                "t1.property_value AS expressContentType" +
                "  FROM  nl_batch_detail t2 LEFT JOIN nl_batch_property t1 ON t2.batch_id=t1.batch_id WHERE");
        List<String> values = new ArrayList();
        //批次编号
        String batchId = String.valueOf(map.get("batch_id"));
        String nullString = "null";
        if (!nullString.equals(batchId) && StringUtil.isNotEmpty(batchId)) {
            hql.append(" t2.batch_id = '" + batchId + "' ");
        }
        //收件人ID
        String id = String.valueOf(map.get("id"));
        if (!nullString.equals(id) && StringUtil.isNotEmpty(id)) {
            hql.append(" AND t2.id = '" + id + "'");
        }
        //姓名
        String name = String.valueOf(map.get("name"));
        if (!nullString.equals(name) && StringUtil.isNotEmpty(name)) {
            hql.append(" AND t2.label_one = '" + name + "'");
            values.add(name);
        }
        //文件编码
        String fileCode = String.valueOf(map.get("file_code"));
        if (!nullString.equals(fileCode) && StringUtil.isNotEmpty(fileCode)) {
            hql.append(" AND t2.label_six = '" + fileCode + "'");
        }
        //校验结果
        String checkingResult = String.valueOf(map.get("checking_result"));
        if (!nullString.equals(checkingResult) && StringUtil.isNotEmpty(checkingResult)) {
            hql.append(" AND t2.label_seven = '" + checkingResult + "'");
        }
        //快件状态
        String status = String.valueOf(map.get("status"));
        if (!nullString.equals(status) && StringUtil.isNotEmpty(status)) {
            hql.append(" AND t2.status = '" + status + "'");
            values.add(status);
        }
        hql.append(" ORDER BY t2.id DESC ");
        Page page = new Pagination().getPageData(hql.toString(), null, pageParam, jdbcTemplate);
        List<Map<String, Object>> list = page.getList();
        Map<String, Object> resultMap = new HashMap<>(10);
        resultMap.put("total", list.size());
        resultMap.put("rows", list);
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> checkStatistics(String cust_id) {
        String sql = "SELECT batch_name AS batchName,IFNULL(upload_num,0) AS uploadNum,IFNULL(success_num,0) AS successNum," +
                "IFNULL(upload_num/success_num,0) AS effectiveRate FROM nl_batch WHERE comp_id='" + cust_id + "' ORDER BY " +
                "upload_time DESC LIMIT 10";
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
        return resultList;
    }

    @Override
    public List<Map<String, Object>> effectiveStatistics() {
        String sql = "SELECT batch_name AS batchName,IFNULL(upload_num,0) AS uploadNum,IFNULL(success_num,0) AS successNum," +
                "IFNULL(upload_num/success_num,0) AS effectiveRate FROM nl_batch ORDER BY " +
                "upload_time DESC LIMIT 10";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }
}
