package com.bdaim.batch.service.impl;

import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.service.ExpressBatchService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.*;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @description: 发件批次 信息管理 (失联修复_信函模块)
 * @auther: Chacker
 * @date: 2019/7/31 16:02
 */
@Service
public class ExpressBatchServiceImpl implements ExpressBatchService {

    protected static final Properties PROPERTIES = new Properties(System.getProperties());

    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private BatchDao batchDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ZipUtil zipUtil;
    @Autowired
    private FileUrlEntity fileUrlEntity;
    @Autowired
    private static Logger logger = LoggerFactory.getLogger(ExpressBatchServiceImpl.class);

    @Override
    public Map<String, Object> queryPathByBatchId(Map<String, Object> map) {
        String batchId = String.valueOf(map.get("batch_id"));
        String sql = "SELECT property_value AS zipPath FROM nl_batch_property WHERE batch_id='" + batchId + "' AND property_name='batchZipPath' LIMIT 1";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        logger.info("执行查询的SQL语句为"+ sql);
        return result;
    }

    /**
     * 查询快件记录接口
     * @param id
     */
    @Override
    public Map<String, Object> getExpressLog(String id) throws Exception {
        String sqlQuery = "SELECT * from t_touch_express_log WHERE touch_id = " + id;
        List<Map<String, Object>> list = batchDao.sqlQuery(sqlQuery);
        if (list!=null && list.size()>0){
            return  list.get(0);
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseInfo receiverInfoImport(MultipartFile multipartFile, String batchName, int expressContent, String custId) {
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
        String uploadPath = classPath + pathF + "receiver_info" + pathF + custId + pathF;
        // 文件路径的字符串拼接 目录 + 文件名 + 后缀
        uploadPath = uploadPath + generatedFileName + suffix;
        logger.info("uploadPath is:" + uploadPath);
        File file = new File(uploadPath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
//                multipartFile.transferTo(file);
                //为保证uploadPath定义的路径 在readExcel中和在创建file中使用的路径一样，所以使用FileUtils工具类
                FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
            } catch (IOException e) {
                logger.info("发生异常了哦");
                logger.info(e.getMessage());
            }
        }
        logger.info("excel文件上传成功");
        //2. 读取 & 解析excel文件(根据文件的后缀名进行区分)
        List<List<String>> contentList = ExcelReaderUtil.readExcel(uploadPath);
        if (contentList.size() > 1000) {
            return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "请保证一次上传数据不超过1000条记录");
        }
        //2.5 加上销售定价判断和余额的判断
        String priceSql = "SELECT t1.cust_id,t1.property_value,t2.enterprise_name FROM t_customer_property t1 LEFT JOIN t_customer t2 ON " +
                "t1.cust_id=t2.cust_id WHERE t1.cust_id='" + custId + "' AND t1.property_name='address_fix_price'";
        logger.info("执行SQL "+priceSql);
        List<Map<String, Object>> priceList = jdbcTemplate.queryForList(priceSql);
        if (priceList == null || priceList.size() == 0) {
            return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "请先设置销售定价");
        } else {
            try{
                // 判断余额是否足够 t_customer_property中的price的value是销售定价，单位 元，remain_amount是余额，单位分
                int num = contentList.size() - 1;
                String remainAmountSql = "SELECT cust_id,property_value FROM t_customer_property WHERE cust_id='" + custId + "' AND property_name='remain_amount'";
                Map<String, Object> amountMap = jdbcTemplate.queryForMap(remainAmountSql);
                logger.info("查询余额SQL为" + remainAmountSql);
                logger.info("查询余额结果为" + amountMap.toString());
                if (amountMap != null) {
                    BigDecimal price = new BigDecimal(String.valueOf(priceList.get(0).get("property_value")));
                    BigDecimal remainAmount = new BigDecimal(String.valueOf(amountMap.get("property_value")));
                    logger.info("price 定价为" + price);
                    logger.info("remainAmount 余额是" + remainAmount);
                    if (remainAmount.compareTo(price.multiply(new BigDecimal(num)).multiply(new BigDecimal("100"))) == -1) {
                        return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "余额不足，请先充值");
                    }
                }
            }catch (Exception e){
                logger.info("执行出错，出错地址为"+e.getMessage());
            }
        }
        //3. 把批次信息和批次详情列表存入数据库
        //3.1 设置并新增保存批次信息 (status 为"1",表示 【校验中】) (certify_type是修复方式 3.表示快递地址修复)
        String enterprise_name = String.valueOf(priceList.get(0).get("enterprise_name"));
        StringBuffer insertBatchSql = new StringBuffer("INSERT INTO nl_batch (id,batch_name,upload_time,comp_id,comp_name,certify_type,channel,status," +
                "upload_num) VALUES ('");
        String batchId = String.valueOf(System.currentTimeMillis());
        insertBatchSql.append(batchId + "','" + batchName + "',now(),'"
                + custId + "','"+enterprise_name+"','3','-1','1','" + (contentList.size() - 1) + "')");
        jdbcTemplate.update(insertBatchSql.toString());
        //把快递内容(1. 电子版 2. 打印版)存入批次属性表
        String insertBatchProperty = "INSERT INTO nl_batch_property (batch_id,property_name,property_value,create_time)" +
                " VALUES ('" + batchId + "','expressContentType','" + String.valueOf(expressContent) + "',NOW())";
        int row = jdbcTemplate.update(insertBatchProperty);
        logger.info("插入批次属性成功，条数为: " + row);
        //3.2 获取并保存批次详情信息 (因为第一行为标题，所以从第二行开始遍历)
        int checkingResult = 2;
        for (int i = 1; i < contentList.size(); i++) {
            /**
             * label_five 自带ID，对应收件人ID
             * label_one 姓名
             * label_two 电话
             * label_four 地址(收件人信息excel中的地址) 现已修改为 site 字段
             * label_three 身份证号码
             * batch_id 对应nl_batch主表中的id
             * status 是修复状态，这里表示校验状态 0.无效、1.有效、2.校验中(此时为【2】【校验中】)
             * label_seven 是快件状态 1、待上传内容2、待申请发件3、待取件4、已发件(此时为【1】【待上传内容】) 此时不赋值
             */
            String touchId = UUID.randomUUID().toString().replace("-","");
            StringBuffer batchDetailInsert = new StringBuffer("INSERT INTO nl_batch_detail (label_five,label_one,label_two," +
                    "site,label_three,batch_id," +
                    "status,touch_id,upload_time) VALUES ('");
            batchDetailInsert.append(contentList.get(i).get(0)).append("','").append(contentList.get(i).get(1)).append("','").append(contentList.get(i).get(2))
                    .append("','").append(contentList.get(i).get(3)).append("','").append(contentList.get(i).get(4)).append("','").append(batchId)
                    .append("','").append(checkingResult).append("','").append(touchId).append("',NOW())");
            int rowDetail = jdbcTemplate.update(batchDetailInsert.toString());
            logger.info("执行SQL" +batchDetailInsert.toString());
            logger.info("插入批次详情成功，条数为: " + rowDetail);
        }
        return new ResponseInfoAssemble().success(null);
    }

    @Override
    public Map<String, Object> batchList(Map<String, Object> map) throws IllegalAccessException {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(Integer.valueOf(String.valueOf(map.get("page_num"))));
        pageParam.setPageSize(Integer.valueOf(String.valueOf(map.get("page_size"))));

        StringBuffer sql = new StringBuffer("SELECT t1.id,t1.comp_id AS custId,t1.comp_name,t1.batch_name AS batchName," +
                "t1.certify_type AS certifyType,t1.channel,t1.repair_strategy,t1.status AS statusId,CASE t1.status WHEN '1' THEN '校验中'" +
                " WHEN '2' THEN '校验失败' WHEN '3' THEN '待上传' WHEN '4' THEN '待申请发件' WHEN '5' THEN '待取件'" +
                " WHEN '6' THEN '已发件' END AS status,t1.upload_num AS uploadNum,t1.success_num AS" +
                " successNum,DATE_FORMAT(t1.upload_time,'%Y-%m-%d %H:%i:%s') AS uploadTime,t2.property_name AS propertyName," +
                "t2.property_value AS propertyValue");
        sql.append(" FROM nl_batch t1 LEFT JOIN nl_batch_property t2 ON t1.id=t2.batch_id AND t2.property_name='expressContentType' WHERE 1=1");

        //loginType 封装员:1  打印员:2需要查询负责的企业
        String loginType = String.valueOf(map.get("loginType"));
        String nullString = "null";
        if (!nullString.equals(loginType) && StringUtil.isNotEmpty(loginType)) {
            String userId = String.valueOf(map.get("userId"));
            String propertyName = null;
            if (StringUtil.isNotEmpty(userId)) {
                //查询当前登录人所负责的企业
                if ("1".equals(loginType)) {
                    propertyName = "packager";
                } else if ("2".equals(loginType))
                    propertyName = "printer";
            }
            List<Map<String, Object>> custIdList = customerDao.getCustIdByPropertyValue(propertyName, userId);
            if (custIdList != null && custIdList.size() > 0) {
                String custIds = "";
                for (int i = 0; i < custIdList.size(); i++) {
                    custIds += custIdList.get(i).get("cust_id") + ",";
                }
                custIds = custIds.substring(0, custIds.length() - 1);
                map.put("cust_id", custIds);
            }
        }
        //企业ID
        String custId = String.valueOf(map.get("cust_id"));
        if (!nullString.equals(custId) && StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t1.comp_id in ( " + custId + ")");
        }
        //批次编号
        String batchId = String.valueOf(map.get("batch_id"));
        if (!nullString.equals(batchId) && StringUtil.isNotEmpty(batchId)) {
            sql.append(" AND t1.id like '%" + batchId + "%'");
        }
        //批次名称
        String batchName = String.valueOf(map.get("batch_name"));
        if (!nullString.equals(batchName) && StringUtil.isNotEmpty(batchName)) {
            sql.append(" AND t1.batch_name like '%" + batchName + "%'");
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
        //expressContentType 快件类型 1.电子版 2.打印版
        if (list != null && list.size() != 0) {
            list.stream().map(e -> "1".equals(String.valueOf(e.get("propertyValue"))) ? e.put("expressContentType", "电子版") :
                    e.put("expressContentType", "打印版")).collect(Collectors.toList());
        }
        Map<String, Object> resultMap = new HashMap<>(10);
        resultMap.put("rows", list);
        resultMap.put("total", page.getTotal());
        return resultMap;
    }

    @Override
    public Map<String, Object> batchDetail(Map<String, Object> map) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(NumberConvertUtil.parseInt(String.valueOf(map.get("page_num"))));
        pageParam.setPageSize(NumberConvertUtil.parseInt(String.valueOf(map.get("page_size"))));
        StringBuffer hql = new StringBuffer("SELECT t2.touch_id touchId,t2.id AS addressId,l.request_id,t2.label_five AS receiverId,t2.batch_id AS batchId,t2.label_one AS name,t2.label_two AS phone,t2.site AS address," +
                "t2.label_six AS fileCode,CASE t2.label_seven WHEN '1' THEN '待上传内容' WHEN '2' THEN '待申请发件'" +
                " WHEN '3' THEN '待取件' WHEN '4' THEN '已发件' END AS expressStatus," +
                "CASE t2.status WHEN '1' THEN '有效' WHEN '0' THEN '无效' ELSE '校验中' END AS status,t2.status AS statusId," +
                "t2.label_seven AS expressStatusId,t1.property_value AS expressContentType" +
                "  FROM  nl_batch_detail t2 LEFT JOIN nl_batch_property t1 ON t2.batch_id=t1.batch_id AND t1.property_name='expressContentType' LEFT JOIN t_touch_express_log l ON t2.touch_id = l.touch_id WHERE");
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
            hql.append(" AND t2.label_five LIKE '%" + id + "%'");
        }
        //地址ID
        String addressId = String.valueOf(map.get("address_id"));
        if (!nullString.equals(addressId) && StringUtil.isNotEmpty(addressId)) {
            hql.append(" AND t2.id LIKE '%" + addressId + "%'");
        }
        //姓名
        String name = String.valueOf(map.get("name"));
        if (!nullString.equals(name) && StringUtil.isNotEmpty(name)) {
            hql.append(" AND t2.label_one LIKE '%" + name + "%'");
            values.add(name);
        }
        //文件编码
        String fileCode = String.valueOf(map.get("file_code"));
        if (!nullString.equals(fileCode) && StringUtil.isNotEmpty(fileCode)) {
            hql.append(" AND t2.label_six LIKE '%" + fileCode + "%'");
        }
        //校验结果
        String status = String.valueOf(map.get("status"));
        if (!nullString.equals(status) && StringUtil.isNotEmpty(status)) {
            hql.append(" AND t2.status = '" + status + "'");
        }
        //快件状态
        String checkingResult = String.valueOf(map.get("express_status")).trim();
        if (!nullString.equals(checkingResult) && StringUtil.isNotEmpty(checkingResult)) {
            hql.append(" AND t2.label_seven = '" + checkingResult + "'");
            values.add(status);
        }
        hql.append(" ORDER BY t2.id DESC  ");
        logger.info("查询批次详情SQL为" + hql.toString());
        Map<String, Object> resultMap = new HashMap<>(10);
        try {
            Page page = new Pagination().getPageData(hql.toString(), null, pageParam, jdbcTemplate);
            List<Map<String, Object>> list = page.getList();
            logger.info("查询结果为" + list.toString());
            resultMap.put("total", page.getTotal());
            resultMap.put("rows", list);
            String countSql = "SELECT COUNT(*) AS count FROM nl_batch_detail WHERE status='1' AND batch_id='"+batchId+"'";
            Map<String,Object> count = jdbcTemplate.queryForMap(countSql);
            resultMap.put("valid",Integer.parseInt(String.valueOf(count.get("count"))));

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
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
        try{
            logger.info("进入上传快件内容方法"+batchId);
            //1. 对文件类型进行校验
            List<String> pdfFileNameList = new ArrayList<>();
            String fileUrl = fileUrlEntity.getFileUrl();
            String pathF = PROPERTIES.getProperty("file.separator");
            fileUrl = fileUrl.replace("/", pathF);
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
                    logger.info("解压ZIP");
                    //3.1 把zip文件存储路径存入nl_batch_property表
                    String zipPath = contentPath.replaceAll("\\\\", "\\\\\\\\");
                    //查询一下，没有则插入，有则修改
                    String sql = "SELECT COUNT(*) AS count FROM nl_batch_property WHERE batch_id='" + batchId + "' AND property_name='batchZipPath'";
                    Map<String, Object> countMap = jdbcTemplate.queryForMap(sql);
                    int count = Integer.parseInt(String.valueOf(countMap.get("count")));
                    if (count == 0) {
                        StringBuffer updateZipPath = new StringBuffer("INSERT INTO nl_batch_property VALUES ('");
                        updateZipPath.append(batchId).append("','batchZipPath','").append(zipPath).append("',NOW())");
                        jdbcTemplate.update(updateZipPath.toString());

                    } else {
                        StringBuffer updateZipPath = new StringBuffer("UPDATE nl_batch_property SET property_value='");
                        updateZipPath.append(zipPath).append("' WHERE batch_id='").append(batchId).append("' AND property_name='batchZipPath'");
                        jdbcTemplate.update(updateZipPath.toString());
                    }
                    pdfFileNameList = zipUtil.unZip(contentFile, destPath);
                }else if(Constant.PDF.equals(contentSuffix)){
                    //pdf文件，把文件路径更新到label_eight
                    String pdfPath = destPath+receiverId+Constant.PDF;
                    String updatePdfPath = "UPDATE nl_batch_detail SET label_eight='"+pdfPath+"' WHERE label_five='"+
                            receiverId+"' AND batch_id='"+batchId+"'";
                    updatePdfPath = updatePdfPath.replaceAll("\\\\", "\\\\\\\\");
                    logger.info("更新pdf文件路径 "+updatePdfPath);
                    jdbcTemplate.update(updatePdfPath);
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
                        expressPath = expressPath.replaceAll("\\\\", "\\\\\\\\");
                    }
                    //根据批次ID batchId 和收件人ID receiverID 更新 存储路径、文件编码
                    String sql = "UPDATE nl_batch_detail SET label_eight='" + expressPath + "',label_six='" + fileCode + "' " +
                            "WHERE batch_id='" + batchId + "' AND label_five='" + receiverID + "'";
                    logger.info("根据批次ID batchId 和收件人ID receiverID 更新 存储路径、文件编码 执行SQL为"+sql);
                    jdbcTemplate.update(sql);
                }
            }
            //5. 修改状态 根据收件人ID和 批次ID把 状态修改为 【2】【待申请发件】
            String sql = "UPDATE nl_batch_detail SET label_seven='2' WHERE batch_id='" + batchId + "'";
            if (StringUtil.isNotEmpty(receiverId)) {
                sql = sql + " AND label_five = '" + receiverId + "'";
            }
            jdbcTemplate.update(sql);
            logger.info("修改状态 根据收件人ID和 批次ID把 状态修改为 【2】【待申请发件】" + sql);
            //6. 如果此批次下没有待上传的 信息，则把该批次 状态修改为 【4】【待申请发件】
            String countSql = "SELECT COUNT(*) AS count FROM nl_batch_detail WHERE batch_id='" + batchId
                    + "' AND status='1' AND label_seven='1'";
            Map<String, Object> count = jdbcTemplate.queryForMap(countSql);
            int num = Integer.parseInt(String.valueOf(count.get("count")));
            if (num == 0) {
                String updateSql = "UPDATE nl_batch SET status='4' WHERE id='" + batchId + "'";
                logger.info("执行更新批次状态SQL " + updateSql);
                jdbcTemplate.update(updateSql);
            }
        }catch (Exception e){
            logger.info("发生异常了。。。。");
            logger.info(e.getMessage());
        }
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
    public List<Map<String, Object>> findDetailByBatchId(Map<String, Object> map) {
        String batchId = String.valueOf(map.get("batch_id"));
//        PageParam pageParam = new PageParam();
//        pageParam.setPageNum(NumberConvertUtil.parseInt(String.valueOf(map.get("page_num"))));
//        pageParam.setPageSize(NumberConvertUtil.parseInt(String.valueOf(map.get("page_size"))));
        StringBuffer hql = new StringBuffer("SELECT t2.id AS addressId,l.request_id,t2.label_five AS receiverId,t2.batch_id AS batchId,t2.label_one AS name,t2.label_two AS phone," +
                "t2.label_six AS fileCode,CASE t2.label_seven WHEN '1' THEN '待上传内容' WHEN '2' THEN '待申请发件'" +
                " WHEN '3' THEN '待取件' WHEN '4' THEN '已发件' END AS expressStatus,t2.site AS address,l.request_id AS expressCode," +
                "CASE t2.status WHEN '1' THEN '有效' WHEN '0' THEN '无效' ELSE '校验中' END AS status,t2.status AS statusId," +
                "t2.label_seven AS expressStatusId,t1.property_value AS expressContentType" +
                "  FROM  nl_batch_detail t2 LEFT JOIN nl_batch_property t1 ON t2.batch_id=t1.batch_id AND t1.property_name='expressContentType' LEFT JOIN t_touch_express_log l ON t2.touch_id = l.touch_id WHERE");
        List<String> values = new ArrayList();
        //批次编号
        String nullString = "null";
        if (!nullString.equals(batchId) && StringUtil.isNotEmpty(batchId)) {
            hql.append(" t2.batch_id = '" + batchId + "' ");
        }
        //收件人ID
        String id = String.valueOf(map.get("receiver_id"));
        if (!nullString.equals(id) && StringUtil.isNotEmpty(id)) {
            hql.append(" AND t2.label_five LIKE '%" + id + "%'");
        }
        //姓名
        String name = String.valueOf(map.get("name"));
        if (!nullString.equals(name) && StringUtil.isNotEmpty(name)) {
            hql.append(" AND t2.label_one LIKE '%" + name + "%'");
            values.add(name);
        }
        //文件编码
        String fileCode = String.valueOf(map.get("file_code"));
        if (!nullString.equals(fileCode) && StringUtil.isNotEmpty(fileCode)) {
            hql.append(" AND t2.label_six LIKE '%" + fileCode + "%'");
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
        List<Map<String, Object>> list = jdbcTemplate.queryForList(hql.toString());
        return list;
    }

    @Override
    public String findPdfPathByReceiverId(String batchId, String receiverId) {
        String pdfPath = "";
        try{
            String sql = "SELECT label_eight AS pdfPath FROM nl_batch_detail WHERE batch_id='" + batchId + "' AND label_five='" + receiverId + "' LIMIT 1";
            logger.info("执行SQL"+sql);
            Map<String, Object> map = jdbcTemplate.queryForMap(sql);
            pdfPath = String.valueOf(map.get("pdfPath"));
            return pdfPath;
        }catch (Exception e){
            logger.info("出现异常"+e.getMessage());
        }
        return pdfPath;
    }

    @Override
    public void updateBatchStatus(String batchId, int status) throws Exception {
        String updateSql = "UPDATE nl_batch SET `status` =? WHERE id = ?";
        batchDao.executeUpdateSQL(updateSql, status, batchId);
        String updateDetailSql = "UPDATE nl_batch_detail SET `label_seven` =4 WHERE batch_id = ? and status = 1";
        batchDao.executeUpdateSQL(updateDetailSql, batchId);
    }

    @Override
    public void updateFileCode(String addressId, String fileCode) {
        String sql = "UPDATE nl_batch_detail SET label_six='" + fileCode + "' WHERE id='" + addressId + "'";
        jdbcTemplate.update(sql);
    }
}
