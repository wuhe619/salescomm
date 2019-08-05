package com.bdaim.batch.service.impl;

import com.bdaim.batch.dao.BatchInfoDao;
import com.bdaim.batch.dao.BatchInfoDetailDao;
import com.bdaim.batch.dao.BatchPropertyDao;
import com.bdaim.batch.entity.BatchDetailInfo;
import com.bdaim.batch.entity.BatchInfo;
import com.bdaim.batch.entity.BatchProperty;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
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
        //3. 把批次信息和批次详情列表存入数据库
        //3.1 设置并新增保存批次信息
        BatchInfo batchInfo = new BatchInfo();
        batchInfo.setId(String.valueOf(System.currentTimeMillis()));
        batchInfo.setBatchName(batchName);
//        batchInfo.setBatchType(String.valueOf(batchType));
        batchInfo.setUploadTime(DateUtil.fmtDateToStr(new Date(), DateUtil.YYYY_MM_DD_HH_mm_ss));
        batchInfo.setCustId(custId);
        //对非空且跟快递业务无关的字段进行赋值
        batchInfo.setCertifyType("-1");
        batchInfo.setChannel("-1");
        //把批次状态设置为校验中
        batchInfo.setStatus(Constant.CHECKING);
        //把快递内容(1. 电子版 2. 打印版)存入批次属性表
        BatchProperty batchProperty = new BatchProperty();
        batchProperty.setBatchId(batchInfo.getId());
        batchProperty.setPropertyName("express_content_type");
        batchProperty.setPropertyValue(String.valueOf(expressContent));
        //contentList的数量减去1，即为上传数量 (因为excel的第一行为列的标题)
        batchInfo.setUploadNum(contentList.size() - 1);
        batchInfoDao.saveBatchInfo(batchInfo);
        batchPropertyDao.saveBatchProperty(batchProperty);
        //3.2 获取并保存批次详情信息 (因为第一行为标题，所以从第二行开始遍历)
        for (int i = 1; i < contentList.size(); i++) {
            BatchDetailInfo batchDetailInfo = new BatchDetailInfo();
            batchDetailInfo.setId(String.valueOf(System.currentTimeMillis()));
            batchDetailInfo.setName(contentList.get(i).get(0));
            batchDetailInfo.setPhone(contentList.get(i).get(1));
            //地址填充到扩展字段 label_four
            batchDetailInfo.setLabelFour(contentList.get(i).get(2));
            //校验结果设置为校验中
            batchDetailInfo.setCheckingResult(Constant.CHECKING);
            batchDetailInfo.setBatchId(batchInfo.getId());
            batchInfoDetailDao.saveBatchInfoDetail(batchDetailInfo);
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
                "CASE t2.property_value WHEN '1' THEN '电子版' WHEN '2' THEN '打印版' END AS propertyValue");
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
                tempMap.put(String.valueOf(tempMap.get("propertyName")), tempMap.get("propertyValue"));
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
        StringBuffer hql = new StringBuffer("SELECT id,batch_id,label_one AS name,label_two AS phone,label_four AS address," +
                "label_six AS fileCode,CASE label_seven WHEN '1' THEN '有效' WHEN '2' THEN '无效' END AS checkingResult," +
                "CASE status WHEN '1' THEN '待上传内容' WHEN '2' THEN '待发件' WHEN '3' THEN '待取件' WHEN '4' THEN '已发件' END AS status" +
                "  FROM  nl_batch_detail WHERE");
        List<String> values = new ArrayList();
        //批次编号
        String batchId = String.valueOf(map.get("batch_id"));
        String nullString = "null";
        if (!nullString.equals(batchId) && StringUtil.isNotEmpty(batchId)) {
            hql.append(" batch_id = '" + batchId + "' ");
        }
        //收件人ID
        String id = String.valueOf(map.get("id"));
        if (!nullString.equals(id) && StringUtil.isNotEmpty(id)) {
            hql.append(" AND id = '" + id + "'");
        }
        //姓名
        String name = String.valueOf(map.get("name"));
        if (!nullString.equals(name) && StringUtil.isNotEmpty(name)) {
            hql.append(" AND label_one = '" + name + "'");
            values.add(name);
        }
        //文件编码
        String fileCode = String.valueOf(map.get("file_code"));
        if (!nullString.equals(fileCode) && StringUtil.isNotEmpty(fileCode)) {
            hql.append(" AND label_six = '" + fileCode + "'");
        }
        //校验结果
        String checkingResult = String.valueOf(map.get("checking_result"));
        if (!nullString.equals(checkingResult) && StringUtil.isNotEmpty(checkingResult)) {
            hql.append(" AND label_seven = '" + checkingResult + "'");
        }
        //快件状态
        String status = String.valueOf(map.get("status"));
        if (!nullString.equals(status) && StringUtil.isNotEmpty(status)) {
            hql.append(" AND status = '" + status + "'");
            values.add(status);
        }
        hql.append(" ORDER BY id DESC ");
        Page page = new Pagination().getPageData(hql.toString(), null, pageParam, jdbcTemplate);
        List<Map<String, Object>> list = page.getList();
        Map<String, Object> resultMap = new HashMap<>(10);
        resultMap.put("total", list.size());
        resultMap.put("rows", list);
        return resultMap;
    }
}
