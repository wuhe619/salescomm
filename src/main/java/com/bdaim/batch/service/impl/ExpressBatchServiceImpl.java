package com.bdaim.batch.service.impl;

import com.bdaim.batch.dao.BatchInfoDao;
import com.bdaim.batch.dao.BatchInfoDetailDao;
import com.bdaim.batch.dao.BatchPropertyDao;
import com.bdaim.batch.entity.BatchDetailInfo;
import com.bdaim.batch.entity.BatchInfo;
import com.bdaim.batch.entity.BatchProperty;
import com.bdaim.batch.service.ExpressBatchService;
import com.bdaim.common.response.JsonResult;
import com.bdaim.common.response.ResponseBody;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.ExcelReaderUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.spring.DataConverter;
import com.bdaim.rbac.dto.Page;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


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

    @Override
    public void receiverInfoImport(MultipartFile multipartFile, String batchName, int expressContent, String custId) throws IOException {
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
    }

    @Override
    public List<Map<String, Object>> batchList(Map<String, Object> map) throws IllegalAccessException {
        int pageNum = Integer.valueOf(String.valueOf(map.get("page_num")));
        int pageSize = Integer.valueOf(String.valueOf(map.get("page_size")));
        int start = (pageNum - 1) * pageSize;
        StringBuffer hql = new StringBuffer("from nl_batch where 1=1");
        List<String> values = new ArrayList();
        //企业ID
        String custId = String.valueOf(map.get("cust_id"));
        String nullString = "null";
        if (!nullString.equals(custId) && StringUtil.isNotEmpty(custId)) {
            hql.append(" and comp_id = ?");
            values.add(custId);
        }
        //批次编号
        String batchId = String.valueOf(map.get("batch_id"));
        if (!nullString.equals(batchId) && StringUtil.isNotEmpty(batchId)) {
            hql.append(" and id = ?");
            values.add(batchId);
        }
        //批次名称
        String batchName = String.valueOf(map.get("batch_name"));
        if (!nullString.equals(batchName) && StringUtil.isNotEmpty(batchName)) {
            hql.append(" and batch_name = ?");
            values.add(batchName);
        }
        //状态
        String status = String.valueOf(map.get("status"));
        if (!nullString.equals(status) && StringUtil.isNotEmpty(status)) {
            hql.append(" and status = ?");
            values.add(status);
        }
        //上传时间开始
        String startTime = String.valueOf(map.get("start_time"));
        if (!nullString.equals(startTime) && StringUtil.isNotEmpty(startTime)) {
            hql.append(" and upload_time > ?");
            values.add(startTime);
        }
        //上传时间截止
        String endTime = String.valueOf(map.get("end_time"));
        if (!nullString.equals(endTime) && StringUtil.isNotEmpty(endTime)) {
            hql.append(" and upload_time < ?");
            values.add(endTime);
        }
        hql.append(" ORDER BY upload_time DESC ");
        Page page = batchInfoDao.page(hql.toString(), values, start, pageSize);
        List<Map<String, Object>> list = DataConverter.objectListToMap(page.getData());
        if (list != null && list.size() != 0) {
            for (Map<String,Object> tempMap:list) {
                tempMap.put("uploadTime",String.valueOf(tempMap.get("uploadTime")).substring(0,19));
                tempMap.put("expressContentType","电子版");
                String statusValue = String.valueOf(tempMap.get("status"));
                switch (statusValue) {
                    case "1":
                        tempMap.put("status", "校验中");
                    case "2":
                        tempMap.put("status", "校验失败");
                    case "3":
                        tempMap.put("status", "待上传");
                    case "4":
                        tempMap.put("status", "待发件");
                    case "5":
                        tempMap.put("status", "已取件");
                    case "6":
                        tempMap.put("status", "已发件");
                }
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> batchDetail(Map<String, Object> map) throws IllegalAccessException {
        int pageNum = Integer.valueOf(String.valueOf(map.get("page_num")));
        int pageSize = Integer.valueOf(String.valueOf(map.get("page_size")));
        int start = (pageNum - 1) * pageSize;
        StringBuffer hql = new StringBuffer("from nl_batch_detail where 1=1");
        List<String> values = new ArrayList();
        //批次编号
        String batchId = String.valueOf(map.get("batch_id"));
        String nullString = "null";
        if (!nullString.equals(batchId) && StringUtil.isNotEmpty(batchId)) {
            hql.append(" and batch_id = ?");
            values.add(batchId);
        }
        //收件人ID
        String id = String.valueOf(map.get("id"));
        if (!nullString.equals(id) && StringUtil.isNotEmpty(id)) {
            hql.append(" and id = ?");
            values.add(id);
        }
        //姓名
        String name = String.valueOf(map.get("name"));
        if (!nullString.equals(name) && StringUtil.isNotEmpty(name)) {
            hql.append(" and label_one = ?");
            values.add(name);
        }
        //文件编码
        String fileCode = String.valueOf(map.get("file_code"));
        if (!nullString.equals(fileCode) && StringUtil.isNotEmpty(fileCode)) {
            hql.append(" and label_six = ?");
            values.add(fileCode);
        }
        //校验结果
        String checkingResult = String.valueOf(map.get("checking_result"));
        if (!nullString.equals(checkingResult) && StringUtil.isNotEmpty(checkingResult)) {
            hql.append(" and label_seven = ?");
            values.add(checkingResult);
        }
        //快件状态
        String status = String.valueOf(map.get("status"));
        if (!nullString.equals(status) && StringUtil.isNotEmpty(status)) {
            hql.append(" and status = ?");
            values.add(status);
        }
        hql.append(" ORDER BY id DESC ");
        Page page = batchInfoDao.page(hql.toString(), values, start, pageSize);
        List<Map<String, Object>> list = DataConverter.objectListToMap(page.getData());
//        if (list != null && list.size() != 0) {
//            for (Map<String,Object> tempMap:list) {
//                //tempMap.put("checkingResult",String.valueOf(tempMap.get("")))
//            }
//        }
        return list;
    }
}
