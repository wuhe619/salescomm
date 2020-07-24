package com.bdaim.batch.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.controller.BatchAction;
import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.dao.BatchLogDao;
import com.bdaim.batch.dto.ImportErr;
import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.batch.entity.BatchListEntity;
import com.bdaim.batch.entity.BatchListParam;
import com.bdaim.batch.service.BatchListService;
import com.bdaim.batch.service.BatchService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.customer.dto.CustomerPropertyDTO;
import com.bdaim.util.*;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.price.dto.ResourcesPriceDto;

import com.bdaim.util.http.HttpUtil;
import net.sf.json.JSONString;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
@Service("batchListService")
@Transactional
public class BatchListServiceImpl implements BatchListService {

    private final static Logger LOG = LoggerFactory.getLogger(BatchListServiceImpl.class);

    @Resource
    private BatchDao batchListDao;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private BatchDetailDao batchDetailDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private CustomerService customerService;
    @Resource
    private BatchLogDao batchLogDao;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private BatchService batchService;
    @Resource
    private BatchListService batchListService;


    /**
     * @description 上传修复文件
     * @author:duanliying
     * @method
     * @date: 2019/4/8 10:39
     */
    public Map<String, Object> uploadBatchFile(MultipartFile file, String batchname, String repairStrategy, int certifyType, String channel, String compId, Long optUser, String optUserName,String province,String city,int extNumber) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> channels = null;
        int type = 0;

        //根据certifyType
        if (certifyType == 0) {
            type = ResourceEnum.IDCARD.getType();
        } else if (certifyType == 1) {
            type = ResourceEnum.IMEI.getType();
        } else if (certifyType == 2) {
            type = ResourceEnum.MAC.getType();
        }
        LOG.info("修复类型是：" + certifyType);
        if (StringUtil.isNotEmpty(channel)) {
            channels = Arrays.asList(channel.split(","));
        }
        String resourceId = null;
        for (int k = 0; k < channels.size(); k++) {
            String fixPrice = null;
            //根据供应商和type查询resourceId
            MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(channels.get(k), type);
            if (marketResourceEntity != null && marketResourceEntity.getResourceId() >= 0) {
                resourceId = String.valueOf(marketResourceEntity.getResourceId());
                LOG.info("查询企业客户资源类型是:" + type + "渠道是：" + channels.get(k) + "资源id是" + resourceId);
            }
            //核验企业是否设置销售定价
            ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, compId);
            if (resourcesPriceDto != null) {
                if (certifyType == 0) {
                    fixPrice = resourcesPriceDto.getIdCardPrice();
                } else if (certifyType == 1) {
                    fixPrice = resourcesPriceDto.getImeiPrice();
                } else if (certifyType == 2) {
                    fixPrice = resourcesPriceDto.getMacPrice();
                }
            }
            if (StringUtil.isEmpty(fixPrice)) {
                resultMap.put("_message", "未设置销售定价，请联系管理员！");
                return resultMap;
            }
            if (StringUtil.isEmpty(resourcesPriceDto.getActivityId())) {
                resultMap.put("_message", "未配置资源信息，请联系管理员！");
                return resultMap;
            }
        }
        if (channels.size() > 0) {
            for (int index = 0; index < channels.size(); index++) {
                String channelall = channels.get(index);
                //得到上传文件的输入流
                InputStream is = null;
                Workbook xs = null;
                OutputStream os = null;
                ImportErr im = new ImportErr();
                List<ImportErr> importErd = new ArrayList<>(0);
                if (file == null) {
                       /* im.setErrCount("上传文件不能为空");
                        importErd.add(im);*/
                    resultMap.put("_message", "上传文件不能为空");
                    return resultMap;
                }
                //本地
                //String classPath = new BatchAction().getClass().getResource("/").getPath();
                //服务器
                String classPath = "/data/upload/";
//                String classPath = "E:\\";

                String fileName = file.getOriginalFilename();
                File localFile = null;
                //文件名加上时间戳
                String batchId = String.valueOf(System.currentTimeMillis());
                if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
                    classPath += batchId + ".xlsx";
                    //得到目标文件对象
                    localFile = new File(classPath);
                    file.transferTo(localFile);
                } else {
                    classPath += batchId + ".xlx";
                    //得到目标文件对象
                    localFile = new File(classPath);
                    file.transferTo(localFile);
                }

                xs = new XSSFWorkbook(localFile);
                Sheet sheet = xs.getSheetAt(0);
                int lastRowNum = sheet.getLastRowNum();
                Double useAmount = null;
                LinkedList<String> certlist = new LinkedList<>();
                LinkedList<String> custuserIdlist = new LinkedList<>();
                List<BatchDetail> batchDetailList = new ArrayList<>();
                Boolean repeatIdCardStatus = false;
                Boolean repeateEntrpriseIdStatus = false;
                int uploadNum = 0;//弃用lastRowNum 防止其他空白行点击后产生空字符串数据
               if(resourceId.equals("15")){
                   for (int i = 1; i <= lastRowNum; i++) {
                       Row row = sheet.getRow(i);
                       String certifyMd5 = "", kehuId = "", label_one = "", label_two = "", label_three = "",invaMobList = "";
                       if (row != null) {
                           short lastCellNum = row.getLastCellNum();
                           for (int j = 0; j < lastCellNum; j++) {
                               Cell cell = row.getCell(j);
                               if (cell != null && cell.getCellType() != CellType.BLANK) {
                                   cell.setCellType(CellType.STRING);
                                   switch (j) {
                                       case 0:
                                           certlist.add(cell.getStringCellValue().trim());
                                           certifyMd5 = cell.getStringCellValue().trim();
                                           break;
                                       case 1:
                                           custuserIdlist.add(cell.getStringCellValue().trim());
                                           invaMobList = cell.getStringCellValue().trim();
                                           break;
                                       case 2:
                                           custuserIdlist.add(cell.getStringCellValue().trim());
                                           kehuId = cell.getStringCellValue().trim();
                                           break;
                                       case 3:
                                           certlist.add(cell.getStringCellValue().trim());
                                           label_one = cell.getStringCellValue().trim();
                                           break;
                                       case 4:
                                           custuserIdlist.add(cell.getStringCellValue().trim());
                                           label_two = cell.getStringCellValue().trim();
                                           break;
                                       case 5:
                                           custuserIdlist.add(cell.getStringCellValue().trim());
                                           label_three = cell.getStringCellValue().trim();
                                           break;
                                   }
                               }
                           }

                           if (StringUtil.isNotEmpty(certifyMd5) && StringUtil.isNotEmpty(kehuId)) {
                               uploadNum += 1;
                               BatchDetail batchDetail = new BatchDetail();
                               batchDetail.setIdCard(certifyMd5);
                               batchDetail.setEnterpriseId(kehuId);
                               batchDetail.setLabelOne(label_one);
                               batchDetail.setLabelTwo(label_two);
                               batchDetail.setLabelThree(label_three);
                               batchDetail.setLabelFour(invaMobList);
                               batchDetailList.add(batchDetail);
                           }
                       }
                   }
               }else {
                   for (int i = 1; i <= lastRowNum; i++) {
                       Row row = sheet.getRow(i);
                       String certifyMd5 = "", kehuId = "", label_one = "", label_two = "", label_three = "";
                       if (row != null) {
                           short lastCellNum = row.getLastCellNum();
                           for (int j = 0; j < lastCellNum; j++) {
                               Cell cell = row.getCell(j);
                               if (cell != null && cell.getCellType() != CellType.BLANK) {
                                   cell.setCellType(CellType.STRING);
                                   switch (j) {
                                       case 0:
                                           certlist.add(cell.getStringCellValue().trim());
                                           certifyMd5 = cell.getStringCellValue().trim();
                                           break;
                                       case 1:
                                           custuserIdlist.add(cell.getStringCellValue().trim());
                                           kehuId = cell.getStringCellValue().trim();
                                           break;
                                       case 2:
                                           certlist.add(cell.getStringCellValue().trim());
                                           label_one = cell.getStringCellValue().trim();
                                           break;
                                       case 3:
                                           custuserIdlist.add(cell.getStringCellValue().trim());
                                           label_two = cell.getStringCellValue().trim();
                                           break;
                                       case 4:
                                           custuserIdlist.add(cell.getStringCellValue().trim());
                                           label_three = cell.getStringCellValue().trim();
                                           break;
                                   }
                               }
                           }

                           if (StringUtil.isNotEmpty(certifyMd5) && StringUtil.isNotEmpty(kehuId)) {
                               uploadNum += 1;
                               BatchDetail batchDetail = new BatchDetail();
                               batchDetail.setIdCard(certifyMd5);
                               batchDetail.setEnterpriseId(kehuId);
                               batchDetail.setLabelOne(label_one);
                               batchDetail.setLabelTwo(label_two);
                               batchDetail.setLabelThree(label_three);
                               batchDetailList.add(batchDetail);
                           }
                       }
                   }
               }
                String custFixPrice = null;
                //查询企业账户余额
                Double remainAmount = customerService.getRemainMoney(compId) / 100;
                //根据供应商和type查询resourceId
                MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(channels.get(index), type);
                if (marketResourceEntity != null) {
                    resourceId = String.valueOf(marketResourceEntity.getResourceId());
                }
                //查询修复单价
                ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, compId);
                //查询出表格需要修复的数量判断余额是否充足
                if (resourcesPriceDto != null) {
                    if (resourcesPriceDto != null) {
                        if (certifyType == 0) {
                            custFixPrice = resourcesPriceDto.getIdCardPrice();
                        } else if (certifyType == 1) {
                            custFixPrice = resourcesPriceDto.getImeiPrice();
                        } else if (certifyType == 2) {
                            custFixPrice = resourcesPriceDto.getMacPrice();
                        }
                    }
                    //获取修复中的上传数量
                    int uploadOnFixNum = batchService.uploadNumGet(compId);
                    double douFixPrice = 0;
                    if (StringUtil.isNotEmpty(custFixPrice)) {
                        douFixPrice = Double.parseDouble(custFixPrice);
                    }
                    useAmount = (uploadNum + uploadOnFixNum) * douFixPrice * channels.size();
                    LOG.info("修复扣费销售定价:" + douFixPrice + "\t账户余额为：" + remainAmount.toString() + "\t本地修复所需费用：" + useAmount.toString() + "\t本次上传数量：" + lastRowNum + "\t正在修复中的数量：" + uploadOnFixNum);
                }
                if (useAmount != null && (useAmount > remainAmount)) {
                    resultMap.put("code", "002");
                    resultMap.put("_message", "账户余额不足，上传失败！");
                    return resultMap;
                } else if (uploadNum > 1000) {
                    resultMap.put("code", "003");
                    resultMap.put("_message", "上传数据超过1000条记录，上传失败！");
                    return resultMap;
                }
                //保存批次详情数据
                if (batchDetailList.size() > 0) {
                    batchListService.saveBatchDetailList(batchDetailList, channelall, resourceId, certifyType, batchId, optUser, optUserName);
                    repeatIdCardStatus = batchService.repeatIdCardStatus(batchId);
                    repeateEntrpriseIdStatus = batchService.repeateEntrpriseIdStatus(batchId);
                    //保存批次信息
                    if (repeatIdCardStatus) {
                        resultMap.put("code", "004");
                        resultMap.put("_message", "录入身份证加密数据不能重复，上传失败！");
                        return resultMap;
                    } else if (repeateEntrpriseIdStatus) {
                        resultMap.put("code", "005");
                        resultMap.put("_message", "录入企业自带id数据不能重复，上传失败！");
                        return resultMap;
                    } else {
                        batchListService.saveBatch(batchname, uploadNum, repairStrategy, compId, batchId, certifyType, channelall,province,city,extNumber);
                    }
                        /*String errorCode = batchService.sendtofile(certlist,custuserIdlist,repairMode,batchId);
                if(errorCode.equals("00")){
                    batchListService.saveBatch(batchname,uploadNum,repairMode,compId,batchId);
                }else {
                    throw new RuntimeException("联通接口异常，请稍后再试！");
                }*/
                    resultMap.put("code", "000");
                    resultMap.put("_message", "失联修复文件上传成功！");

                }
            }
        }
        return resultMap;
    }

    @Override
    public PageList pageList(PageParam page, BatchListParam batchListParam, String role) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, comp_id, comp_name, repair_mode repairMode,batch_name batchName, certify_type certifyType, status, upload_num uploadNum, success_num successNum, upload_time uploadTime, repair_time repairTime, channel, repair_strategy repairStrategy FROM nl_batch WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(batchListParam.getComp_name())) {
            sqlBuilder.append(" AND comp_name LIKE ?");
            p.add("%" + batchListParam.getComp_name() + "%");
        }
        if (StringUtil.isNotEmpty(batchListParam.getCompId())) {
            sqlBuilder.append(" AND comp_id = ? ");
            p.add(batchListParam.getCompId());
        }
        if (StringUtil.isNotEmpty(batchListParam.getId())) {
            sqlBuilder.append(" AND id = ? ");
            p.add(batchListParam.getId());
        }
        if (StringUtil.isNotEmpty(batchListParam.getBatchName())) {
            sqlBuilder.append(" AND batch_name LIKE ?");
            p.add("%"+batchListParam.getCompId()+"%");
        }
        if (StringUtil.isNotEmpty(batchListParam.getUploadStartTime())) {
            sqlBuilder.append(" AND upload_time >= ? ");
            p.add(batchListParam.getUploadStartTime());
        }
        if (StringUtil.isNotEmpty(batchListParam.getUploadEndTime())) {
            sqlBuilder.append(" AND upload_time <= ? ");
            p.add(batchListParam.getUploadEndTime());
        }
        if (batchListParam.getCertifyType() != null) {
            sqlBuilder.append(" AND certify_type = ?");
            p.add(batchListParam.getCertifyType());
        }
        if (batchListParam.getStatus() != null) {
            if (batchListParam.getStatus() == 2) {
                sqlBuilder.append(" AND status IN (2,4,5)");
            } else {
                sqlBuilder.append(" AND status =?");
                p.add(batchListParam.getStatus());
            }
        }
        //只查询用户自己负责的批次必须是前台用户才可以
        if ("2".equals(batchListParam.getUserType()) && "ROLE_CUSTOMER".equals(role)) {
            sqlBuilder.append(" AND id in (SELECT DISTINCT custG.batch_id ")
                    .append(" FROM nl_batch_detail custG ")
                    .append(" WHERE custG.allocation = 1 ")
                    .append(" AND custG.user_id=? )");
            p.add(batchListParam.getUserId());
        }
        sqlBuilder.append(" AND certify_type !=3 ORDER BY upload_time DESC");
        LOG.info("批次列表查询sql:\t" + sqlBuilder.toString());
        PageList result = new Pagination().getPageData(sqlBuilder.toString(), p.toArray(), page, jdbcTemplate);
        if (result != null && result.getList() != null && result.getList().size() > 0) {
            Map map;
            // 处理修复状态为 4数据待发送给联通或者5发送联通成功都为2-修复中
            for (int i = 0; i < result.getList().size(); i++) {
                map = (Map) result.getList().get(i);
                if (map != null && map.get("status") != null && (Integer.parseInt(String.valueOf(map.get("status"))) == 4 ||
                        Integer.parseInt(String.valueOf(map.get("status"))) == 5)) {
                    map.put("status", 2);
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> list(PageParam page, BatchListParam batchListParam) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, batch_name batchName, certify_type certifyType, status, upload_num uploadNum, success_num successNum, upload_time uploadTime, repair_time repairTime FROM nl_batch WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (batchListParam.getCompId() != null) {
            sqlBuilder.append(" AND comp_id = ?");
            p.add(batchListParam.getCompId());
        }
        if (batchListParam.getId() != null) {
            sqlBuilder.append(" AND id = ?");
            p.add(batchListParam.getId());
        }
        if (StringUtil.isNotEmpty(batchListParam.getBatchName())) {
            sqlBuilder.append(" AND batch_name = ?");
            p.add(batchListParam.getBatchName());
        }
        if (StringUtil.isNotEmpty(batchListParam.getUploadStartTime())) {
            sqlBuilder.append(" AND upload_time >= ?");
            p.add(batchListParam.getUploadStartTime());
        }
        if (StringUtil.isNotEmpty(batchListParam.getUploadEndTime())) {
            sqlBuilder.append(" AND upload_time <= ?");
            p.add(batchListParam.getUploadEndTime());
        }
        if (batchListParam.getCertifyType() != null) {
            sqlBuilder.append(" AND certify_type = ?");
            p.add(batchListParam.getCertifyType());
        }
        if (batchListParam.getStatus() != null) {
            sqlBuilder.append(" AND status = ?");
            p.add(batchListParam.getStatus());
        }
        sqlBuilder.append(" ORDER BY upload_time DESC");
        return jdbcTemplate.queryForList(sqlBuilder.toString(), p.toArray());
    }

    @Override
    public Map<String, Object> countCallProgressByCondition(String userId, String userType, String batchId, String
            customId) {
        List<Map<String, Object>> dataList;
        // 判断是否为企业用户
        if (String.valueOf(Constant.ADMIN_USER_TYPE).equals(userType)) {
            userId = "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT custG.id, custG.user_id, custG.batch_id, t_user.REALNAME `name` " +
                "   FROM  nl_batch_detail custG ");
        sb.append(" LEFT JOIN t_customer_user t_user ON t_user.ID = custG.user_id ");
        sb.append(" WHERE custG.status = 1 ");
        List<Object> p = new ArrayList<>();
        p.add(customId);
        p.add(batchId);
        if (StringUtil.isNotEmpty(batchId)) {
            sb.append(" AND custG.batch_id=?");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(userId)) {
            sb.append(" AND custG.user_id=?");
            p.add(userId);
        }
        Set<String> xAxisNames = new HashSet<>();
        Map<String, String> names = new HashMap<>(16);
        Map<String, Long> callCountData = new HashMap<>(16);
        Map<String, Set<String>> userGroupData = new HashMap<>(16);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString(), p.toArray());
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
            p = new ArrayList<>();
            p.add(customId);
            p.add(batchId);
            sb.append("SELECT cust_id, user_id, create_time FROM t_touch_voice_log WHERE cust_id = ? AND batch_id = ? ");
            if (StringUtil.isNotEmpty(userId)) {
                sb.append(" AND user_id = ? ");
                p.add(userId);
            }
            sb.append(" AND superid IN (");

            for (String superId : map.getValue()) {
                sb.append("?,");
                p.add(superId);
            }
            // 去除逗号
            sb.deleteCharAt(sb.length() - 1);
            sb.append(" ) GROUP BY superid, batch_id ");
            list = jdbcTemplate.queryForList(sb.toString(), p.toArray());
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
        //result.put("xAxis", xAxisNames);
        return result;
    }


    @Override
    public void saveBatchDetail(String certifyMd5, String kehuId, String batchId, String lalel_one, String
            label_two, String label_three, String channel, int certifyType) throws Exception {
        int channels = Integer.parseInt(channel.toString());
                   /* BatchDetail batchDetail = new BatchDetail();
                    batchDetail.setBatchId(batchId);
                    batchDetail.setEnterpriseId(kehuId);
                    batchDetail.setIdCard(certifyMd5);
                    batchDetail.setChannel(channels);
                    batchDetail.setStatus(2);//修复中
                    batchDetail.setUploadTime(new Timestamp(new Date().getTime()));
                    //batchDetail.setUserId(userId);
                    batchDetail.setLabelOne(lalel_one);
                    batchDetail.setLabelTwo(label_two);
                    batchDetail.setLabelThree(label_three);*/
        String touchId = Long.toString(IDHelper.getTransactionId());
        int type_code = 0;
        if (certifyType == 0) {
            type_code = 6;
        } else if (certifyType == 1) {
            type_code = 11;
        } else if (certifyType == 2) {
            type_code = 10;
        }
        StringBuilder sqlBuilderl = new StringBuilder("SELECT resource_id FROM t_market_resource WHERE supplier_id=? AND type_code= ?");
        List<Map<String, Object>> resourceIdList = batchDetailDao.sqlQuery(sqlBuilderl.toString(), channels, type_code);
        String resourceId = null;
        if (resourceIdList.size() > 0) {
            resourceId = String.valueOf(resourceIdList.get(0).get("resource_id"));
        }
        LOG.info("resourceId是" + resourceId);
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO nl_batch_detail(touch_id,batch_id,enterprise_id,id_card,channel,status,upload_time,label_one,label_two,label_three,resource_id) values(?,?,?,?,?,?,?,?,?,?,?)");
        batchDetailDao.executeUpdateSQL(sqlBuilder.toString(), touchId, batchId, kehuId, certifyMd5, channels, 2, new Timestamp(new Date().getTime()), lalel_one, label_two, label_three, resourceId);
        LOG.info("修复文件上传，插入批次明细表： 批次ID:" + batchId + "\t身份证加密：" + certifyMd5 + "\t客户侧用户id:" + kehuId);


    }

    /**
     * 保存批次详情信息和记录
     */
    @Override
    public void saveBatchDetailList(List<BatchDetail> batchDetailList, String channel, String resourceId,
                                    int certifyType, String batchId, Long operUserId, String operName) {

        String kehuId = null, certifyMd5 = null, lalel_one = null, label_two = null, label_three = null,label_four=null;
        if (batchDetailList.size() > 0) {
            for (int i = 0; i < batchDetailList.size(); i++) {
                String touchId = String.valueOf(UUID.randomUUID()).replaceAll("-", "");
                BatchDetail batchDetail = batchDetailList.get(i);
                if (batchDetail != null) {
                    kehuId = batchDetail.getEnterpriseId();
                    certifyMd5 = batchDetail.getIdCard();
                    lalel_one = batchDetail.getLabelOne();
                    label_two = batchDetail.getLabelTwo();
                    label_three = batchDetail.getLabelThree();
                    label_four = batchDetail.getLabelFour();
                }
                LOG.info("resourceId是" + resourceId);
                StringBuilder sqlBuilder = new StringBuilder("INSERT INTO nl_batch_detail(id,touch_id,batch_id,enterprise_id,id_card,channel,status,upload_time,label_one,label_two,label_three,resource_id,label_four) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
                batchDetailDao.executeUpdateSQL(sqlBuilder.toString(), IDHelper.getID()+"",touchId, batchId, kehuId, certifyMd5, channel, 2, new Timestamp(new Date().getTime()), lalel_one, label_two, label_three, resourceId,label_four);
                LOG.info(IDHelper.getID()+""+"修复文件上传，插入批次明细表： 批次ID:" + batchId + "\t身份证加密：" + certifyMd5 + "\t客户侧用户id:" + kehuId);
                //保存修复记录信息
                if (operUserId != null) {
                    if (StringUtil.isEmpty(operName)) {
                        operName = customerUserDao.getName(String.valueOf(operUserId));
                        StringBuilder sqlBuilderLog = new StringBuilder("INSERT INTO nl_batch_log(batch_id,enterprise_id,id_card,upload_time,oper_name,oper_user_id) values(?,?,?,?,?,?)");
                        batchDetailDao.executeUpdateSQL(sqlBuilderLog.toString(), batchId, kehuId, certifyMd5, new Timestamp(new Date().getTime()), operName, operUserId);
                    }
                }
                LOG.info("修复操作记录： " + batchDetailDao.toString());
            }
        }

    }


    @Override
    public void saveBatch(String batchname, int uploadNum, String repairStrategy, String compId, String batchId,
                          int certifyType, String channel,String province,String city,int extNumber) throws Exception {
        int channels = Integer.parseInt(channel);
        String compName = "";
        Customer customer = customerDao.findUniqueBy("custId", compId);
        if (customer != null) {
            compName = customer.getEnterpriseName();
        }
        long extNumberSecond=0;
        if(extNumber>0){
            extNumberSecond=extNumber*60*60;
            if(extNumberSecond>4294967296l){
                extNumberSecond=4294967296l;
            }
        }
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO nl_batch(batch_name,certify_type,channel,id,comp_id,comp_name,upload_time,status,upload_num,repair_strategy,cuc_received,midle_number_province,midle_number_city,midle_number_expiry_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        batchDetailDao.executeUpdateSQL(sqlBuilder.toString(), batchname, certifyType, channels, batchId, compId, compName, (new Timestamp(new Date().getTime())), 2, uploadNum, repairStrategy, 0,province,city,extNumberSecond+"");
        LOG.info("修复文件上传成功，插入批次表： 企业ID:" + compId + "\t批次ID:" + batchId + "\t批次名称：" + batchname + "\t上传数量:" + uploadNum + "\t修复模式：" + repairStrategy);

    }

    @Override
    public void cucIsreceive(String batchId, int cucIsReceived) throws Exception {
        BatchListEntity batchListEntity = batchListDao.findUniqueBy("id", batchId);
        if (batchListEntity != null) {
            batchListEntity.setCucReceived(cucIsReceived);
        }
        batchListDao.update(batchListEntity);
        LOG.info("修改批次表是否要传给联通修复" + "\t批次ID:" + batchId + "\t是否被修复标志：" + cucIsReceived);

    }

    @Override
    public void saveBatchLog(String certifyMd5, String kehuId, String batchId, Long operUserId, String operName) throws
            Exception {
       /* BatchLogEntity batchLogEntity = new BatchLogEntity();
        batchLogEntity.setBatchId(batchId);
        batchLogEntity.setEnterpriseId(kehuId);
        batchLogEntity.setIdCard(certifyMd5);
        batchLogEntity.setUploadTime(new Timestamp(new Date().getTime()));*/
        if (operUserId != null) {
            if (StringUtil.isEmpty(operName)) {
                operName = customerUserDao.getName(String.valueOf(operUserId));
                StringBuilder sqlBuilder = new StringBuilder("INSERT INTO nl_batch_log(batch_id,enterprise_id,id_card,upload_time,oper_name,oper_user_id) values(?,?,?,?,?,?)");
                batchDetailDao.executeUpdateSQL(sqlBuilder.toString(), batchId, kehuId, certifyMd5, new Timestamp(new Date().getTime()), operName, operUserId);
            }
        }
      /*  batchLogEntity.setOperName(operName);
        batchLogDao.save(batchLogEntity);*/
        LOG.info("修复操作记录： " + batchDetailDao.toString());
    }

    @Override
    public List<Map<String, Object>> batchOperlogLsit(String batchid) {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT batch_id,enterprise_id,id_card,oper_user_id,oper_name,upload_time,remark from nl_batch_log ");
        sb.append(" WHERE 1 = 1 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(batchid)) {
            sb.append(" AND batch_id=?");
            p.add(batchid);
        }

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString(), p.toArray());
        return list;
    }

    /**
     * @description 批次列表增加统计分析功能
     */
    @Override
    public Map<String, Object> queryAnalysis(PageParam page, String batchId, String custId) {
        List<Map<String, Object>> list = new ArrayList<>();
        //查詢当前企业所有员工
        String queryUserSql = "SELECT id,realname,account FROM t_customer_user WHERE user_type = '2' AND cust_id=? ";
        List<Map<String, Object>> userDoMap = batchLogDao.sqlQuery(queryUserSql, custId);
        Map<String, Object> allMessageMap = new HashMap<>();
        try {
            if (userDoMap.size() > 0) {
                for (int i = 0; i < userDoMap.size(); i++) {
                    Map<String, Object> userMap = new HashMap<>();
                    String userId = String.valueOf(userDoMap.get(i).get("id"));
                    String realname = String.valueOf(userDoMap.get(i).get("realname"));
                    String account = String.valueOf(userDoMap.get(i).get("account"));
                    userMap = getTouchLog(userId, batchId);
                    userMap.put("realname", realname);
                    userMap.put("account", account);
                    userMap.put("userId", userId);
                    //员工列表信息
                    list.add(userMap);
                }
            }

            //查询批次信息
            //批次信息sql
            StringBuffer batchSql = new StringBuffer("SELECT n.id batchId, batch_name batchName, n.upload_time uploadTime, upload_num uploadNum, ");
            batchSql.append("COUNT(DISTINCT nl.id_card,nl.status = 1 or null) successNum ");
            batchSql.append("FROM nl_batch n LEFT JOIN nl_batch_detail nl ON n.id = nl.batch_id ");
            batchSql.append("WHERE n.id = ?");
            List<Map<String, Object>> batchMap = batchLogDao.sqlQuery(batchSql.toString(), batchId);
            //修复率
            String repairRate = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(String.valueOf(batchMap.get(0).get("successNum"))), NumberConvertUtil.parseLong(String.valueOf(batchMap.get(0).get("uploadNum"))));
            Map<String, Object> batchMessageMap = getTouchLog(null, batchId);
            if (batchMap != null && batchMap.size() > 0) {
                batchMessageMap.put("batchId", batchMap.get(0).get("batchId"));
                batchMessageMap.put("uploadTime", batchMap.get(0).get("uploadTime"));
                batchMessageMap.put("batchName", batchMap.get(0).get("batchName"));
                batchMessageMap.put("uploadNum", batchMap.get(0).get("uploadNum"));
                batchMessageMap.put("successNum", batchMap.get(0).get("successNum"));
                batchMessageMap.put("repairRate", repairRate);
            }
            //添加汇总信息
            allMessageMap.put("userList", list);
            allMessageMap.put("batchMessageMap", batchMessageMap);
            allMessageMap.put("total", list.size());
        } catch (Exception e) {
            LOG.error("批次列表统计分析异常" + e);
        }
        return allMessageMap;
    }

    public Map<String, Object> getTouchLog(String userId, String batchId) throws Exception {
        Map<String, Object> userMap = new HashMap<>();
        //分配数量sql
        List<Object> p = new ArrayList<>();
        p.add(batchId);
        StringBuffer allocationSql = new StringBuffer("SELECT COUNT(DISTINCT id_card) allocationCount from nl_batch_detail WHERE batch_id=?  AND allocation='1'");
        if (StringUtil.isNotEmpty(userId)) {
            allocationSql.append(" AND user_id = ? ");
            p.add(userId);
        }
        //查询总通话时长和拨打人数
        StringBuffer seccussCallStr = new StringBuffer();
        seccussCallStr.append("SELECT COUNT(DISTINCT n.id_card) callNum , IFNULL(SUM(c.Callerduration), 0) sumCallTime FROM t_touch_voice_log l");
        seccussCallStr.append(" LEFT JOIN nl_batch_detail n ON l.batch_id = n.batch_id AND n.id = l.superid\n");
        seccussCallStr.append("LEFT JOIN t_callback_info c ON l.callSid = c.callSid\n");
        seccussCallStr.append("WHERE n.batch_id =?");
        List<Object> sp = new ArrayList<>();
        sp.add(batchId);
        if (StringUtil.isNotEmpty(userId)) {
            seccussCallStr.append(" AND l.user_id = ? ");
            sp.add(userId);
        }
        //查询接听人数
        StringBuffer seccussCallSql = new StringBuffer();
        seccussCallSql.append("SELECT COUNT(DISTINCT n.id_card) callSuccessNum ");
        seccussCallSql.append("FROM t_touch_voice_log t ");
        seccussCallSql.append("LEFT JOIN t_callback_info c ON t.callSid = c.callSid ");
        seccussCallSql.append("LEFT JOIN nl_batch_detail n ON t.batch_id = n.batch_id AND n.id = t.superid\n");
        seccussCallSql.append("WHERE t.batch_id =? AND t.`status` = '1001' AND c.Callerduration > 0");
        List<Object> seccussCall = new ArrayList<>();
        seccussCall.add(batchId);
        if (StringUtil.isNotEmpty(userId)) {
            seccussCallSql.append(" AND t.user_id = ? ");
            sp.add(userId);
        }
        //查询短信信息
        StringBuffer smsSql = new StringBuffer();
        smsSql.append("SELECT COUNT(DISTINCT n.id_card) sendSmsSum, COUNT(DISTINCT n.id_card,s.status = 1001 or null) sendSuccessSum ");
        smsSql.append("FROM t_touch_sms_log s\n");
        smsSql.append("LEFT JOIN nl_batch_detail n ON s.batch_id = n.batch_id AND s.superid = n.id\n");
        smsSql.append("WHERE s.batch_id = ?\n");
        List<Object> smsP = new ArrayList<>();
        smsP.add(batchId);
        if (StringUtil.isNotEmpty(userId)) {
            smsSql.append(" AND s.user_id = ? ");
            smsP.add(userId);
        }

        List<Map<String, Object>> allocationList = batchLogDao.sqlQuery(allocationSql.toString(), p.toArray());
        if (allocationList.size() > 0) {
            int allocationCount = NumberConvertUtil.everythingToInt(allocationList.get(0).get("allocationCount"));
            //分配数量
            userMap.put("allocationCount", allocationCount);
            LOG.info("分配人数是" + allocationCount);
            //拨打人数统计
            List<Map<String, Object>> callSuccessMap = batchLogDao.sqlQuery(seccussCallStr.toString(), sp.toArray());
            int callNumber = 0, callSuccessNum = 0, sumCallTime = 0;
            if (callSuccessMap != null && callSuccessMap.size() > 0) {
                callNumber = NumberConvertUtil.everythingToInt(callSuccessMap.get(0).get("callNum"));
                sumCallTime = NumberConvertUtil.transformtionInt(String.valueOf(callSuccessMap.get(0).get("sumCallTime")));
                userMap.put("callNumber", callNumber);
                userMap.put("sumCallTime", sumCallTime);
                LOG.info("拨打成功的人数是：" + callNumber + "通话总时长是" + sumCallTime);
                if (allocationCount - callNumber < 0) {
                    userMap.put("surplusNum", 0);
                } else {
                    userMap.put("surplusNum", allocationCount - callNumber);
                }
                LOG.info("剩余数量是" + (allocationCount - callNumber));
            }
            //接听人数callSuccessNum
            List<Map<String, Object>> callSuccessNumList = batchLogDao.sqlQuery(seccussCallSql.toString(), seccussCall.toArray());
            if (callSuccessNumList != null && callSuccessNumList.size() > 0) {
                callSuccessNum = NumberConvertUtil.everythingToInt(callSuccessNumList.get(0).get("callSuccessNum"));
                userMap.put("callSuccessNum", callSuccessNum);
                LOG.info("接听人数是：" + callSuccessNum);
            }
            //接听占比
            String successRate = NumberConvertUtil.getPercent(callSuccessNum, callNumber);
            userMap.put("successRate", successRate);
            LOG.info("接听占比是：" + successRate);
            //平均通话时长
            String averageCallTime = NumberConvertUtil.getAverage(sumCallTime, callSuccessNum);
            userMap.put("averageCallTime", averageCallTime);
            LOG.info("平均通话时长是：" + averageCallTime);
        }
        //发送短信数量
        List<Map<String, Object>> smsMap = batchLogDao.sqlQuery(smsSql.toString(), smsP.toArray());
        int sendSmsSum = NumberConvertUtil.everythingToInt(smsMap.get(0).get("sendSmsSum"));
        int sendSuccessSum = NumberConvertUtil.transformtionInt(String.valueOf(smsMap.get(0).get("sendSuccessSum")));
        userMap.put("sendSmsSum", sendSmsSum);
        userMap.put("sendSuccessSum", sendSuccessSum);
        LOG.info("短信发送数量是：" + sendSmsSum + "短信发送成功数量是：" + sendSuccessSum);
        return userMap;
    }


    @Override
    public PageList sitelist(PageParam page, BatchListParam batchListParam) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT n.id, n.comp_id, n.comp_name, n.repair_mode repairMode,n.batch_name \n" +
                "batchName,n.status, n.upload_num uploadNum, n.success_num successNum, n.upload_time uploadTime, n.repair_time repairTime, n.channel, n.repair_strategy repairStrategy ,MAX(t.create_time) AS Latest_time,MIN(t.create_time) AS Earliest_time\n" +
                "FROM nl_batch n LEFT JOIN t_touch_express_log t ON n.id=t.batch_id WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(batchListParam.getComp_name())) {
            sqlBuilder.append(" AND comp_name LIKE ?");
            p.add("%" + batchListParam.getComp_name() + "%");
        }
        if (StringUtil.isNotEmpty(batchListParam.getCompId())) {
            sqlBuilder.append(" AND comp_id = ?");
            p.add(batchListParam.getCompId());
        }
        if (StringUtil.isNotEmpty(batchListParam.getId())) {
            sqlBuilder.append(" AND id = ?");
            p.add(batchListParam.getId());
        }
        if (StringUtil.isNotEmpty(batchListParam.getBatchName())) {
            sqlBuilder.append(" AND batch_name LIKE ?");
            p.add("%" + batchListParam.getBatchName() + "%");
        }
        if (StringUtil.isNotEmpty(batchListParam.getUploadStartTime())) {
            sqlBuilder.append(" AND upload_time >= ?");
            p.add(batchListParam.getUploadStartTime());
        }
        if (StringUtil.isNotEmpty(batchListParam.getUploadEndTime())) {
            sqlBuilder.append(" AND upload_time <= ? ");
            p.add(batchListParam.getUploadEndTime());
        }
        if (batchListParam.getCertifyType() != null) {
            sqlBuilder.append(" AND certify_type = ?");
            p.add(batchListParam.getCertifyType());
        }
        if (batchListParam.getStatus() != null) {
            if (batchListParam.getStatus() == 2) {
                sqlBuilder.append(" AND status IN (2,4,5)");
            } else {
                sqlBuilder.append(" AND status = ?");
                p.add(batchListParam.getStatus());
            }
        }
        //只查询用户自己负责的批次
        if ("2".equals(batchListParam.getUserType())) {
            sqlBuilder.append(" AND id in (SELECT DISTINCT custG.batch_id ")
                    .append(" FROM nl_batch_detail custG ")
                    .append(" WHERE custG.allocation = 1 ")
                    .append(" AND custG.user_id=? )");
            p.add(batchListParam.getUserId());
        }
        sqlBuilder.append(" AND n.certify_type=3 GROUP BY n.id ORDER BY n.upload_time DESC");
        LOG.info("批次列表查询sql:\t" + sqlBuilder.toString());
        PageList result = new Pagination().getPageData(sqlBuilder.toString(), p.toArray(), page, jdbcTemplate);
        if (result != null && result.getList() != null && result.getList().size() > 0) {
            Map map;
            // 处理修复状态为 4数据待发送给联通或者5发送联通成功都为2-修复中
            for (int i = 0; i < result.getList().size(); i++) {
                map = (Map) result.getList().get(i);
                if (map != null && map.get("status") != null && (Integer.parseInt(String.valueOf(map.get("status"))) == 4 ||
                        Integer.parseInt(String.valueOf(map.get("status"))) == 5)) {
                    map.put("status", 2);
                }
                if (map.get("Latest_time") == null && map.get("Earliest_time") == null) {

                    map.put("Latest_time", "");
                    map.put("Earliest_time", "");
                }
            }
        }

        return result;
    }


    @Override
    public void addressrepairupload(String certifyMd5, String name, String batchId, String phone, String
            compId, String channelall) {
        int channels = Integer.parseInt(channelall);
        String touchId = Long.toString(IDHelper.getTransactionId());
        String phoneId = CipherUtil.encodeByMD5(phone);
        StringBuilder sqlBuilderl = new StringBuilder("SELECT resource_id FROM t_market_resource WHERE supplier_id=? AND type_code=?");
        List<Map<String, Object>> resource_id = batchDetailDao.sqlQuery(sqlBuilderl.toString(), channels, 12);
        String resourceId = null;
        if (resource_id.size() > 0) {
            resourceId = String.valueOf(resource_id.get(0).get("resource_id"));
        }
        LOG.info("resourceId是" + resourceId);
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO nl_batch_detail(touch_id,batch_id,id_card,enterprise_id,channel,status,phoneId,upload_time,name,resource_id) values(?,?,?,?,?,?,?,?,?,?)");
        batchDetailDao.executeUpdateSQL(sqlBuilder.toString(), touchId, batchId, certifyMd5, compId, channels, 2, phoneId, new Timestamp(new Date().getTime()), name, resourceId);
        LOG.info("修复文件上传，插入批次明细表： 批次ID:" + batchId + "\t身份证加密：" + certifyMd5);
        StringBuilder sql = new StringBuilder("SELECT * FROM  u WHERE id = ? ");
        List<Map<String, Object>> list = batchDetailDao.sqlQuery(sql.toString(), phoneId);
        if (list.size() == 0) {
            StringBuilder sqlBuilder2 = new StringBuilder("INSERT INTO u(id,phone) values(?,?)");
            batchDetailDao.executeUpdateSQL(sqlBuilder2.toString(), phoneId, phone);
        }
    }


    public Object ditchList(String companyid, int certify_type) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtil.isNotEmpty(companyid)) {
            CustomerProperty channelProperty = customerDao.getProperty(companyid, "channel");
            String channel = channelProperty.getPropertyValue();

            if (StringUtil.isNotEmpty(channel)) {
                String[] channels = channel.split(",");
                String supplier = null;
                if (channels.length > 0) {
                    for (int i = 0; i < channels.length; i++) {

                        //根据供应商查询供应商
                        String ditch = null;
                        if (channels[i].equals("2")) {
                            ditch = "cuc";
                            if (certify_type == 0) {
                                CustomerProperty salefixPrice = customerDao.getProperty(companyid, ditch + "_fix_price");
                                if (salefixPrice != null) {
                                    if (!salefixPrice.getPropertyValue().equals("")) {
                                        supplier = "2,";
                                    }
                                }

                            }
                        }
                        if (channels[i].equals("4")) {
                            ditch = "cmc";
                            if (certify_type == 0) {
                                CustomerProperty salefixPrice = customerDao.getProperty(companyid, ditch + "_fix_price");
                                if (salefixPrice != null) {
                                    if (!salefixPrice.getPropertyValue().equals("")) {
                                        if (supplier == null) {
                                            supplier = "4,";
                                        } else {
                                            supplier += "4,";
                                        }

                                    }
                                }

                            }
                        }
                        if (channels[i].equals("3")) {
                            ditch = "ctc";
                            if (certify_type == 0) {
                                CustomerProperty salefixPrice = customerDao.getProperty(companyid, ditch + "_fix_price");
                                if (salefixPrice != null) {
                                    if (!salefixPrice.getPropertyValue().equals("")) {
                                        if (supplier == null) {
                                            supplier = "3,";
                                        } else {
                                            supplier += "3,";
                                        }
                                    }
                                }
                            }

                        }
                        if (channels[i].equals("2")) {
                            ditch = "cuc";
                            if (certify_type == 1) {
                                CustomerProperty saleImeiPrice = customerDao.getProperty(companyid, ditch + "_imei_price");
                                if (saleImeiPrice != null) {
                                    if (!saleImeiPrice.getPropertyValue().equals("")) {
                                        supplier = "2,";
                                    }
                                }

                            }
                        }
                        if (channels[i].equals("4")) {
                            ditch = "cmc";
                            if (certify_type == 1) {
                                CustomerProperty saleImeiPrice = customerDao.getProperty(companyid, ditch + "_imei_price");
                                if (saleImeiPrice != null) {
                                    if (!saleImeiPrice.getPropertyValue().equals("")) {
                                        if (supplier == null) {
                                            supplier = "4,";
                                        } else {
                                            supplier += "4,";
                                        }
                                    }
                                }

                            }
                        }
                        if (channels[i].equals("3")) {
                            ditch = "ctc";
                            if (certify_type == 1) {
                                CustomerProperty saleImeiPrice = customerDao.getProperty(companyid, ditch + "_imei_price");
                                if (saleImeiPrice != null) {
                                    if (!saleImeiPrice.getPropertyValue().equals("")) {
                                        if (supplier == null) {
                                            supplier = "3,";
                                        } else {
                                            supplier += "3,";
                                        }
                                    }
                                }
                            }

                        }
                        if (channels[i].equals("2")) {
                            ditch = "cuc";
                            if (certify_type == 2) {
                                CustomerProperty saleMacPrice = customerDao.getProperty(companyid, ditch + "_mac_price");
                                if (saleMacPrice != null) {
                                    if (!saleMacPrice.getPropertyValue().equals("")) {
                                        supplier = "2,";
                                    }
                                }

                            }
                        }
                        if (channels[i].equals("4")) {
                            ditch = "cmc";
                            if (certify_type == 2) {
                                CustomerProperty saleMacPrice = customerDao.getProperty(companyid, ditch + "_mac_price");
                                if (saleMacPrice != null) {
                                    if (!saleMacPrice.getPropertyValue().equals("")) {
                                        if (supplier == null) {
                                            supplier = "4,";
                                        } else {
                                            supplier += "4,";
                                        }
                                    }
                                }

                            }
                        }
                        if (channels[i].equals("3")) {
                            ditch = "ctc";
                            if (certify_type == 2) {
                                CustomerProperty saleMacPrice = customerDao.getProperty(companyid, ditch + "_mac_price");
                                if (saleMacPrice != null) {
                                    if (!saleMacPrice.getPropertyValue().equals("")) {
                                        if (supplier == null) {
                                            supplier = "3,";
                                        } else {
                                            supplier += "3,";
                                        }
                                    }
                                }
                            }
                        }

                    }

                    if (supplier != null) {
                        int indx = supplier.lastIndexOf(",");
                        if (indx != -1) {
                            supplier = supplier.substring(0, indx) + supplier.substring(indx + 1, supplier.length());
                        }
                    }
                    map.put("supplier", supplier);


                }
            }
        }
        return map;
    }

    public List<Map<String, Object>> ditchListv1(String companyid) {
        List<Map<String, Object>> list = new ArrayList<>();
        //查询身份证资源，根据type查询所有资源id
        List<ResourcesPriceDto> custIdcardResourcesList = customerDao.getCustResourcesPriceDtoByIdAndType(ResourceEnum.IDCARD.getType(), companyid);
        if (custIdcardResourcesList.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", ResourceEnum.IDCARD.getType());
            StringBuffer idCardStr = new StringBuffer();
            for (int i = 0; i < custIdcardResourcesList.size(); i++) {
                String supplierId = custIdcardResourcesList.get(i).getSupplierId();
                if (!String.valueOf(idCardStr).contains(supplierId)) {
                    idCardStr.append(supplierId).append(",");
                }
            }
            LOG.info("拥有身份证修复资源供应商是：" + idCardStr);
            map.put("supplierId", String.valueOf(idCardStr));
            list.add(map);
        }

        //查询Imei资源，根据type查询所有资源id
        List<ResourcesPriceDto> custImeiResourcesList = customerDao.getCustResourcesPriceDtoByIdAndType(ResourceEnum.IMEI.getType(), companyid);
        if (custImeiResourcesList.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", ResourceEnum.IMEI.getType());
            StringBuffer imeiStr = new StringBuffer();
            for (int i = 0; i < custImeiResourcesList.size(); i++) {
                String supplierId = custImeiResourcesList.get(i).getSupplierId();
                if (!String.valueOf(imeiStr).contains(supplierId)) {
                    imeiStr.append(supplierId).append(",");
                }
            }
            map.put("supplierId", String.valueOf(imeiStr));
            LOG.info("拥有imei资源供应商是：" + imeiStr);
            list.add(map);
        }

        //查询Mac资源，根据type查询所有资源id
        List<ResourcesPriceDto> custMacResourcesList = customerDao.getCustResourcesPriceDtoByIdAndType(ResourceEnum.MAC.getType(), companyid);
        if (custMacResourcesList.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", ResourceEnum.MAC.getType());
            StringBuffer macStr = new StringBuffer();
            for (int i = 0; i < custMacResourcesList.size(); i++) {
                String supplierId = custMacResourcesList.get(i).getSupplierId();
                if (!String.valueOf(macStr).contains(supplierId)) {
                    macStr.append(supplierId).append(",");
                }
            }
            LOG.info("拥有mac修复资源供应商是：" + macStr);
            map.put("supplierId", String.valueOf(macStr));
            list.add(map);
        }

        return list;
    }

    @Override
    public void Batch(String batchname, int uploadNum, String repairMode, String compId, String batchId, String
            channelall) {
        int channels = Integer.parseInt(channelall.toString());
        String compName = "";
        Customer customer = customerDao.findUniqueBy("custId", compId);
        if (customer != null) {
            compName = customer.getEnterpriseName();
        }
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO nl_batch(batch_name,certify_type,channel,id,comp_id,comp_name,upload_time,status,upload_num,repair_strategy,repair_mode,cuc_received) values(?,?,?,?,?,?,?,?,?,?,?,?)");
        batchDetailDao.executeUpdateSQL(sqlBuilder.toString(), batchname, 3, channels, batchId, compId, compName, (new Timestamp(new Date().getTime())), 2, uploadNum, "2", repairMode, 0);


        LOG.info("修复文件上传成功，插入批次表： 企业ID:" + compId + "\t批次ID:" + batchId + "\t批次名称：" + batchname + "\t上传数量:" + uploadNum + "\t修复模式：" + repairMode);
    }

    public List<Map<String, Object>> getTime() {
        List<Map<String, Object>> list = batchDetailDao.sqlQuery("select now()");
        return list;
    }

    @Override
    public List<Map<String, Object>> getArea(String parentId) {


        List<Map<String, Object>> mapList = batchDetailDao.queryMapsListBySql(" select area_id,area_name,level  from area where parent_id=? ", parentId);


        return mapList;
    }

    @Override
    public ResponseInfo unBind(String bindId, String coolDown,String custId) {

      ResponseInfo responseInfo=new ResponseInfo();

        JSONObject prams=new JSONObject();
        prams.put("bindId",bindId);
        prams.put("coolDown",coolDown);
        String s = prams.toJSONString();
        CustomerProperty customerProperty=new CustomerProperty();
        customerProperty.setCustId(custId);
        customerProperty.setPropertyName("15_config");
        CustomerPropertyDTO customerProperty1 = customerService.getCustomerProperty(customerProperty);
        String propertyValue = customerProperty1.getPropertyValue();
        JSONObject jsonObject = JSONObject.parseObject(propertyValue);
        try {
            String secretId = jsonObject.getString("secretId");
            String secretKeyd = jsonObject.getString("secretKey");
            String requestRefId=IDHelper.getID()+"";
            JSONObject headObj=new JSONObject();
            headObj.put("requestRefId",requestRefId);
            String idKey="requestRefId="+requestRefId+"&secretId="+secretId;
            String s2 = HMACSHA1.hmacSHA1Encrypt(idKey, secretKeyd);
            headObj.put("requestRefId",requestRefId);
            headObj.put("secretId",secretId);
            headObj.put("signature",s2);

            String secretKey = ThreeDES.encryptDESCBC(s, jsonObject.getString("secretKey"), "", "");
          JSONObject request=new JSONObject();
            request.put("request",secretKey);
            request.put("head",headObj);

            String s1 = HttpUtil.httpsPost("", request.toJSONString());
            LogUtil.info("unbind id"+bindId+"return"+s1);
            JSONObject returnObj = JSONObject.parseObject(s1);
            String status =returnObj.get("status").toString();
            responseInfo.setCode(200);
            if(status.equals("0")){
                responseInfo.setMsg("解绑成功");
            }else{
                responseInfo.setMsg("解绑失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            responseInfo.setCode(500);
        }


        return responseInfo;
    }

    @Override
    public ResponseInfo delta(String bindId, int  delta, String custId) {
        ResponseInfo responseInfo=new ResponseInfo();

        JSONObject prams=new JSONObject();
        prams.put("bindId",bindId);
        delta=delta;
        prams.put("delta",delta);
        String s = prams.toJSONString();
        CustomerProperty customerProperty=new CustomerProperty();
        customerProperty.setCustId(custId);
        customerProperty.setPropertyName("15_config");
        CustomerPropertyDTO customerProperty1 = customerService.getCustomerProperty(customerProperty);
        String propertyValue = customerProperty1.getPropertyValue();
        JSONObject jsonObject = JSONObject.parseObject(propertyValue);
        try {
            String secretId = jsonObject.getString("secretId");
            String secretKeyd = jsonObject.getString("secretKey");
            String requestRefId=IDHelper.getID()+"";
            JSONObject headObj=new JSONObject();
            headObj.put("requestRefId",requestRefId);
            String idKey="requestRefId="+requestRefId+"&secretId="+secretId;
            String s2 = HMACSHA1.hmacSHA1Encrypt(idKey, secretKeyd);
            headObj.put("requestRefId",requestRefId);
            headObj.put("secretId",secretId);
            headObj.put("signature",s2);
            String secretKey = ThreeDES.encryptDESCBC(s, jsonObject.getString("secretKey"), "", "");
            JSONObject request=new JSONObject();
            request.put("request",secretKey);
            request.put("head",headObj);

            String s1 = HttpUtil.httpsPost("", request.toJSONString());
            LogUtil.info("unbind id"+bindId+"return"+s1);
            JSONObject returnObj = JSONObject.parseObject(s1);
            String status =returnObj.get("status").toString();
            responseInfo.setCode(200);
            if(status.equals("0")){
                responseInfo.setMsg("成功");
            }else{
                responseInfo.setMsg("延期失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            responseInfo.setCode(500);
        }


        return responseInfo;
    }


}