package com.bdaim.batch.service.impl;

import com.bdaim.batch.dao.BatchDao;
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

    protected static final Properties PROPERTIES = new Properties(System.getProperties());

    @Autowired
    private BatchInfoDao batchInfoDao;
    @Autowired
    private BatchInfoDetailDao batchInfoDetailDao;
    @Autowired
    private BatchPropertyDao batchPropertyDao;
    @Autowired
    private DataConverter dataConverter;
    @Autowired
    private BatchDao batchDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ZipUtil zipUtil;
    @Autowired
    private FileUrlEntity fileUrlEntity;

    @Override
    @Transactional
    public ResponseInfo receiverInfoImport(MultipartFile multipartFile, String batchName, int expressContent, String custId) throws IOException {
        //1. 把excel文件上传到服务器中
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // (如果文件后缀名不对，则给与相应提示)
        if (!Constant.XLSX.equals(suffix) && !Constant.XLS.equals(suffix)) {
            return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "请保证文件格式为\".xls\"或\".xlsx\"");
        }
        String generatedFileName = String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString().substring(0, 5);
        String classPath = fileUrlEntity.getFileUrl();
        String pathF = PROPERTIES.getProperty("file.separator");
        classPath = classPath.replace("/", pathF);
        String uploadPath = classPath + pathF + "receiver_info" + pathF+custId+pathF;
        // 文件路径的字符串拼接 目录 + 文件名 + 后缀
        uploadPath = uploadPath + generatedFileName + suffix;
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
        //3.1 设置并新增保存批次信息 (status 为"1",表示 【校验中】) (certify_type是修复方式 3.表示快递地址修复)
        StringBuffer insertBatchSql = new StringBuffer("INSERT INTO nl_batch (id,batch_name,upload_time,comp_id,certify_type,channel,status," +
                "upload_num) VALUES ('");
        String batchId = String.valueOf(System.currentTimeMillis());
        insertBatchSql.append(batchId + "','" + batchName + "',now(),'"
                + custId + "','3','-1','1','" + (contentList.size() - 1) + "')");
        jdbcTemplate.update(insertBatchSql.toString());
        //把快递内容(1. 电子版 2. 打印版)存入批次属性表
        String insertBatchProperty = "INSERT INTO nl_batch_property (batch_id,property_name,property_value,create_time)" +
                " VALUES ('" + batchId + "','expressContentType','" + String.valueOf(expressContent) + "',NOW())";
        jdbcTemplate.update(insertBatchProperty);
        //3.2 获取并保存批次详情信息 (因为第一行为标题，所以从第二行开始遍历)
        // nl_batch_detail中的id表示收件人ID label_seven是校验结果 1有效 2 无效
        for (int i = 1; i < contentList.size(); i++) {
            /**
             * label_five 自带ID，对应收件人ID
             * label_one 姓名
             * label_two 电话
             * label_four 地址(收件人信息excel中的地址)
             * label_three 身份证号码
             * batch_id 对应nl_batch主表中的id
             * status 是修复状态，这里表示校验状态 0.无效、1.有效、2.校验中(此时为【2】【校验中】)
             * label_seven 是快件状态 1、待上传内容2、待发件3、待取件4、已发件(此时为【1】【待上传内容】)
             */
            StringBuffer batchDetailInsert = new StringBuffer("INSERT INTO nl_batch_detail (label_five,label_one,label_two," +
                    "label_four,label_three,batch_id," +
                    "status,label_seven) VALUES ('");
            batchDetailInsert.append(contentList.get(i).get(0)).append("','").append(contentList.get(i).get(1)).append("','").append(contentList.get(i).get(2))
                    .append("','").append(contentList.get(i).get(3)).append("','").append(contentList.get(i).get(4)).append("','").append(batchId)
                    .append("','2','1')");
            jdbcTemplate.update(batchDetailInsert.toString());
        }
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
        sql.append(" FROM nl_batch t1 LEFT JOIN nl_batch_property t2 ON t1.id=t2.batch_id WHERE 1=1");

        //企业ID
        String custId = String.valueOf(map.get("cust_id"));
        String nullString = "null";
        if (!nullString.equals(custId) && StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t1.comp_id = '" + custId + "'");
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
        //快递格式
        String expressType = String.valueOf(map.get("express_type"));
        if (!nullString.equals(expressType) && StringUtil.isNotEmpty(expressType)) {
            sql.append(" AND t1.id in (SELECT batch_id FROM nl_batch_property WHERE property_name ='expressContentType' AND property_value =" + expressType + ") ");
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
        StringBuffer hql = new StringBuffer("SELECT t2.id AS addressId,t2.label_five AS receiverId,t2.batch_id AS batchId,t2.label_one AS name,t2.label_two AS phone,t2.label_four AS address," +
                "t2.label_six AS fileCode,CASE t2.label_seven WHEN '1' THEN '待上传内容' WHEN '2' THEN '待发件'" +
                " WHEN '3' THEN '待取件' WHEN '4' THEN '已发件' END AS expressStatus," +
                "CASE t2.status WHEN '1' THEN '有效' WHEN '0' THEN '无效' ELSE '校验中' END AS status,t2.status AS statusId," +
                "t2.label_seven AS expressStatusId,t1.property_value AS expressContentType" +
                "  FROM  nl_batch_detail t2 LEFT JOIN nl_batch_property t1 ON t2.batch_id=t1.batch_id WHERE");
        List<String> values = new ArrayList();
        //批次编号
        String batchId = String.valueOf(map.get("batch_id"));
        String nullString = "null";
        if (!nullString.equals(batchId) && StringUtil.isNotEmpty(batchId)) {
            hql.append(" t2.batch_id = '" + batchId + "' ");
        }
        //收件人ID
        String id = String.valueOf(map.get("receiver_id"));
        if (!nullString.equals(id) && StringUtil.isNotEmpty(id)) {
            hql.append(" AND t2.label_five = '" + id + "'");
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
        String status = String.valueOf(map.get("status"));
        if (!nullString.equals(status) && StringUtil.isNotEmpty(status)) {
            hql.append(" AND t2.status = '" + status + "'");
        }
        //快件状态
        String checkingResult = String.valueOf(map.get("express_status"));
        if (!nullString.equals(status) && StringUtil.isNotEmpty(status)) {
            hql.append(" AND t2.label_seven = '" + checkingResult + "'");
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

    /**
     * 上传/批量上传快件内容
     *
     * @param expressContent  PDF文件或PDF的压缩包（zip文件）
     * @param fileCodeMapping 文件编码与收件人ID映射文件
     * @param receiverId      收件人ID的字符串数组
     * @return
     * @auther Chacker
     * @date 2019/8/6 13:34
     */
    @Override
    public ResponseInfo sendMessageUpload(MultipartFile expressContent, MultipartFile fileCodeMapping, String receiverId, String batchId) throws IOException {
        //1. 对文件类型进行校验
        List<String> pdfFileNameList = new ArrayList<>();
        String fileUrl = fileUrlEntity.getFileUrl();
        String pathF = PROPERTIES.getProperty("file.separator");
        fileUrl = fileUrl.replace("/",pathF);
        StringBuffer stringBuffer = new StringBuffer(fileUrl);
        stringBuffer.append(pathF).append("pdf").append(pathF).append(batchId).append(pathF);
        String destPath = stringBuffer.toString();
        if (expressContent != null) {
            String contentFileName = expressContent.getOriginalFilename();
            pdfFileNameList.add(contentFileName.substring(0, contentFileName.lastIndexOf(".")));
            String contentSuffix = contentFileName.substring(contentFileName.lastIndexOf("."));
            if (!Constant.PDF.equals(contentSuffix) && !Constant.ZIP.equals(contentSuffix)) {
                return new ResponseInfoAssemble().failure(406, "操作失败。文件内容格式不正确(pdf/zip)");
            }
            String generatedZipName = String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString().substring(0, 5);
            String contentPath = destPath;
            if (Constant.ZIP.equals(contentSuffix)) {
                //zip文件，重新生成文件名
                contentPath = contentPath + generatedZipName + contentSuffix;
            } else if (Constant.PDF.equals(contentSuffix)) {
                //pdf文件，文件名不变(因为要和收件人ID做映射)
                contentPath = contentPath + contentFileName;
            }
            //2.2 把发件内容文件 (.PDF 或 .zip文件)上传到服务器

            File contentFile = new File(contentPath);
            if (!contentFile.exists()) {
                contentFile.getParentFile().mkdirs();
                contentFile.createNewFile();
            }
            FileUtils.copyInputStreamToFile(expressContent.getInputStream(), contentFile);
            //3. 如果是zip文件，则解压
            if (Constant.ZIP.equals(contentSuffix)) {
                pdfFileNameList = zipUtil.unZip(contentFile, destPath);
            }
        }
        if (fileCodeMapping != null) {
            String mappingFileName = fileCodeMapping.getOriginalFilename();
            String mappingSuffix = mappingFileName.substring(mappingFileName.lastIndexOf("."));
            if (!Constant.XLS.equals(mappingSuffix) && !Constant.XLSX.equals(mappingSuffix)) {
                return new ResponseInfoAssemble().failure(406, "操作失败。映射关系表文件文件格式不正确(xls/xlsx)");
            }
            //2. 将文件上传到服务器
            //2.1 把文件名与收件人ID映射文件上传到服务器
            String generatedFileName = String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString().substring(0, 5);
            String mappingPath = destPath;
            mappingPath = mappingPath + generatedFileName + mappingSuffix;
            File mappingFile = new File(mappingPath);
            if (!mappingFile.exists()) {
                mappingFile.getParentFile().mkdirs();
                mappingFile.createNewFile();
            }
            FileUtils.copyInputStreamToFile(fileCodeMapping.getInputStream(), mappingFile);

            //4. 根据excel中的映射关系，把pdf文件路径存入数据库
            //4.1 读取excel
            List<List<String>> contentStringList = ExcelReaderUtil.readExcel(mappingPath);
            //因为第一行是标题，所以从第二行开始遍历
            for (int i = 1; i < contentStringList.size(); i++) {
                //文件编码
                String fileCode = contentStringList.get(i).get(0);
                //收件人ID
                String receiverID = contentStringList.get(i).get(1);
                //PDF存储路径
                String expressPath = "";
                if (pdfFileNameList.contains(receiverID)) {
                    expressPath = destPath + receiverID + Constant.PDF;
                }
                //根据批次ID batchId 和收件人ID receiverID 更新 存储路径、文件编码
                String sql = "UPDATE nl_batch_detail SET label_eight='" + expressPath + "',label_six='" + fileCode + "' " +
                        "WHERE batch_id='" + batchId + "' AND label_five='" + receiverID + "'";
                jdbcTemplate.update(sql);
            }
        }
        //5. 修改状态 根据收件人ID和 批次ID把 状态修改为 【2】【待发件】
        String sql = "UPDATE nl_batch_detail SET label_seven='2' WHERE batch_id='" + batchId + "'";
        if (StringUtil.isNotEmpty(receiverId)) {
            sql = sql + " AND label_five = '" + receiverId + "'";
        }
        jdbcTemplate.update(sql);
        return new ResponseInfoAssemble().success(null);
    }

    @Override
    public void uploadModelFile(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        String uploadPath = "/express/model";
        String pathF = PROPERTIES.getProperty("file.separator");
        uploadPath = uploadPath + pathF + fileName;
        File file = new File(uploadPath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
    }

    @Override
    public List<Map<String, Object>> findDetailByBatchId(String batch_id) {
        StringBuffer sql = new StringBuffer("SELECT t1.label_six AS fileCode,t1.label_five AS receiverId,t1.label_one")
                .append(" AS name,t1.label_two AS phone,t1.label_four AS address,t1.id AS addressId,t2.request_id AS expressCode, ")
                .append("CASE t1.status WHEN '1' THEN '有效' WHEN '0' THEN '无效' ELSE '' END AS status,")
                .append("CASE t1.label_seven WHEN '1' THEN '待上传' WHEN '2' THEN '待发件' WHEN '3' THEN '待取件' WHEN '4' THEN '已发件'")
                .append(" ELSE '' END AS expressStatus ")
                .append("FROM nl_batch_detail t1 ")
                .append(" LEFT JOIN t_touch_express_log t2 ON t1.touch_id=t2.touch_id WHERE t1.batch_id='")
                .append(batch_id)
                .append("'");
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString());
        return result;
    }

    @Override
    public String findPdfPathByReceiverId(String batchId, String receiverId) {
        String sql = "SELECT label_eight AS pdfPath FROM nl_batch_detail WHERE batch_id='" + batchId + "' AND label_five='" + receiverId + "'";
        Map<String, Object> map = jdbcTemplate.queryForMap(sql);
        String pdfPath = String.valueOf(map.get("pdfPath"));
        return pdfPath;
    }

    @Override
    public void updateBatchStatus(String batchId, int status) throws Exception {
        String updateSql = "UPDATE nl_batch SET `status` =? WHERE id = ?";
        batchDao.executeUpdateSQL(updateSql, status,batchId);
    }

    @Override
    public void updateFileCode(String addressId, String fileCode) {
        String sql = "UPDATE nl_batch_detail SET label_six='" + fileCode + "' WHERE id='" + addressId + "'";
        jdbcTemplate.update(sql);
    }
}
