package com.bdaim.batch.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.dto.ImportErr;
import com.bdaim.batch.service.BatchService;
import com.bdaim.batch.service.impl.BatchListServiceImpl;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.price.service.SalePriceService;
import com.bdaim.resource.service.impl.MarketResourceServiceImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Controller
@RequestMapping("/batch")
public class BatchAction extends BasicAction {
    private static Log logger = LogFactory.getLog(BatchAction.class);

    @Resource
    BatchService batchService;

    @Resource
    CustomerService customerService;
    @Resource
    BatchListServiceImpl batchListService;

    @Resource
    SalePriceService salePriceService;

    @Resource
    MarketResourceServiceImpl marketResourceServiceImpl;

    /**
     * 失联修复上传文件
     */
    @RequestMapping("/upload")
    @ResponseBody
    public String BatchuploadParse(@RequestParam(value = "file") MultipartFile file, String batchname, String repairMode, int certifyType, String channel) {
  /*      Map<String, Object> resultMap = new HashMap<>();
        String compId = opUser().getCustId();
        Map<String, String> salePricemap = salePriceService.SalePrice(compId, channel, certifyType);
        logger.info("企业客户设置销售定价判断:" + salePricemap.get("code") + "\tmessage:" + salePricemap.get("message"));
        if (salePricemap.get("code").equals("1")) {
            resultMap.put("code", "006");
            resultMap.put("_message", "未设置销售定价，请联系管理员！");
            return JSON.toJSONString(resultMap);
        }
        try {
            String propertyValue = null;
            List<String> channels = Arrays.asList(channel.split(","));
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
                        im.setErrCount("上传文件不能为空");
                        importErd.add(im);
                        returnError(JSONObject.toJSONString(importErd));
                    }
                    //本地
                    //String classPath = new BatchAction().getClass().getResource("/").getPath();
                    //服务器
                    String classPath = "/data/upload/";
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

                    LinkedList<String> certlist = new LinkedList<>();
                    LinkedList<String> custuserIdlist = new LinkedList<>();
                    Boolean repeatIdCardStatus = false;
                    Boolean repeateEntrpriseIdStatus = false;
                    int uploadNum = 0;//弃用lastRowNum 防止其他空白行点击后产生空字符串数据
                    for (int i = 1; i <= lastRowNum; i++) {
                        Row row = sheet.getRow(i);
                        String certifyMd5 = "", kehuId = "", label_one = "", label_two = "", label_three = "";
                        if (row != null) {
                            short lastCellNum = row.getLastCellNum();
                            for (int j = 0; j < lastCellNum; j++) {
                                Cell cell = row.getCell(j);
                                if (cell != null && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
                                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
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
                                batchListService.saveBatchDetail(certifyMd5, kehuId, batchId, label_one, label_two, label_three, channelall, certifyType);
                                batchListService.saveBatchLog(certifyMd5, kehuId, batchId, opUser().getId(), opUser().getName());
                            }
                        }
                    }
                    repeatIdCardStatus = batchService.repeatIdCardStatus(batchId);
                    repeateEntrpriseIdStatus = batchService.repeateEntrpriseIdStatus(batchId);

                    Double remainAmount = customerService.getRemainMoney(opUser().getCustId()) / 100;
                    if (channelall.equals("2")) {
                        propertyValue = "cuc";
                    }
                    if (channelall.equals("3")) {
                        propertyValue = "ctc";
                    }
                    if (channelall.equals("4")) {
                        propertyValue = "cmc";
                    }
                    CustomerProperty customerProperty = customerDao.getProperty(opUser().getCustId(), propertyValue + "_fix_price");
                    Double useAmount = null;
                    if (customerProperty != null) {
                        if (StringUtil.isNotEmpty(customerProperty.getPropertyValue())) {
                            //获取修复中的上传数量
                            int uploadOnFixNum = batchService.uploadNumGet(compId);
                            Double cucFixPrice = Double.valueOf(customerProperty.getPropertyValue()) / 100;
                            useAmount = (uploadNum + uploadOnFixNum) * 0.5 * cucFixPrice;
                            logger.info("联通修复扣费销售定价:" + cucFixPrice.toString() + "\t账户余额为：" + remainAmount.toString() + "\t本地修复所需费用：" + useAmount.toString() + "\t本次上传数量：" + uploadNum + "\t正在修复中的数量：" + uploadOnFixNum);
                        }
                    }

                    if (useAmount != null && (useAmount > remainAmount)) {
                        resultMap.put("code", "002");
                        resultMap.put("_message", "账户余额不足，上传失败！");
                        return JSON.toJSONString(resultMap);
                    } else if (uploadNum > 1000) {
                        resultMap.put("code", "003");
                        resultMap.put("_message", "上传数据超过1000条记录，上传失败！");
                        return JSON.toJSONString(resultMap);
                    } else if (repeatIdCardStatus) {
                        resultMap.put("code", "004");
                        resultMap.put("_message", "录入身份证加密数据不能重复，上传失败！");
                        return JSON.toJSONString(resultMap);
                    } else if (repeateEntrpriseIdStatus) {
                        resultMap.put("code", "005");
                        resultMap.put("_message", "录入企业自带id数据不能重复，上传失败！");
                        return JSON.toJSONString(resultMap);
                    } else {
                        batchListService.saveBatch(batchname, uploadNum, repairMode, compId, batchId, certifyType, channelall);
                *//*String errorCode = batchService.sendtofile(certlist,custuserIdlist,repairMode,batchId);
                if(errorCode.equals("00")){
                    batchListService.saveBatch(batchname,uploadNum,repairMode,compId,batchId);
                }else {
                    throw new RuntimeException("联通接口异常，请稍后再试！");
                }*//*
                        resultMap.put("code", "000");
                        resultMap.put("_message", "失联修复文件上传成功！");

                    }
                }
                return JSON.toJSONString(resultMap);
            }
        } catch (Exception e) {
            logger.error("失联修复文件上传失败！\t" + e.getMessage());
            resultMap.put("code", "001");
            resultMap.put("_message", "失联修复文件上传失败！");
            return JSON.toJSONString(resultMap);

        }
        return null;
    }*/
        String compId = opUser().getCustId();
        Map<String, Object> resultMap = null;
        try {
            resultMap = batchListService.uploadBatchFile(file, batchname, repairMode, certifyType, channel, compId, opUser().getId(), opUser().getName());
        } catch (Exception e) {
            logger.error("失联修复文件上传失败！\t" + e.getMessage());
            resultMap.put("code", "001");
            resultMap.put("_message", "失联修复文件上传失败！");
            return JSON.toJSONString(resultMap);
        }
        return JSON.toJSONString(resultMap);
    }

    /**
     * 失联修复模板下载
     */

    @RequestMapping("/downloadModel")
    @ResponseBody
    public String BatchModelfileDownload(HttpServletRequest request, HttpServletResponse response) {
        InputStream in = null;
        OutputStream bos = null;
        try {
            String classPath = new BatchAction().getClass().getResource("/").getPath();
            String fileName = "范式数联上传模板.xlsx";
            String pathF = PROPERTIES.getProperty("file.separator");
            String path = classPath.substring(0, (classPath.length() - 8)) + "/" + pathF + "model" + pathF + fileName;
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));   //保存的文件名,必须和页面编码一致,否则乱码
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            in = new FileInputStream(path);
            bos = response.getOutputStream();
            byte[] b = new byte[2048];
            int length;
            while ((length = in.read(b)) > 0) {
                bos.write(b, 0, length);
            }
            bos.flush();
            return "ok";
        } catch (Exception e) {
            logger.error("失联修复模板下载异常" + "\r\n" + e.getMessage());
            return "error";
        } finally {
            try {
                in.close();
                bos.close();
                logger.info("模板文件下载成功" + "\t" + DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                return "ok";
            } catch (Exception e) {
                logger.error("BatchModelfileDownload" + "\t" + "io资源释放异常" + "\r\n" + e.getMessage());
                return "error";
            }
        }
    }


    /**
     * 快递修复上传文件
     */
    @RequestMapping("/express/upload.do")
    @ResponseBody
    public String express(@RequestParam(value = "file") MultipartFile file, String batchname, String repairMode, String channel) {
        Map<String, Object> resultMap = new HashMap<>();
        String compId = opUser().getCustId();
        List<String> channels = null;
        if (StringUtil.isNotEmpty(channel)) {
            channels = Arrays.asList(channel.split(","));
        }
        for (int i = 0; i < channels.size(); i++) {
            //根据供应商和type查询resourceId
            String resourceId = marketResourceServiceImpl.queryResourceId(channels.get(i), ResourceEnum.ADDRESS.getType());
            String code = salePriceService.checkCustPrice(compId, resourceId);
            logger.info("查询企业客户是否设置销售定价:" + code);
            if ("0".equals(code)) {
                resultMap.put("_message", "未设置销售定价，请联系管理员！");
                return JSON.toJSONString(resultMap);
            }
        }
        try {
            String propertyValue = null;
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
                        im.setErrCount("上传文件不能为空");
                        importErd.add(im);
                        returnError(JSONObject.toJSONString(importErd));
                    }
                    //本地
                    //String classPath = new BatchAction().getClass().getResource("/").getPath();
                    //服务器
                    String classPath = "/data/upload/";
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
                    //查询企业账户余额
                    Double remainAmount = customerService.getRemainMoney(opUser().getCustId()) / 100;
                    //根据供应商和type查询resourceId
                    String resourceId = marketResourceServiceImpl.queryResourceId(channels.get(index), ResourceEnum.ADDRESS.getType());
                    JSONObject customerMarketResource = marketResourceServiceImpl.getCustomerMarketResource(compId, resourceId);
                    //查询出表格需要修复的数量判断余额是否充足
                    if (customerMarketResource != null) {
                        //double cucFixPrice = customerMarketResource.getDoubleValue(ResourceEnum.ADDRESS.getPrice());
                        double cucFixPrice = customerMarketResource.getDoubleValue("price");
                        //获取修复中的上传数量
                        int uploadOnFixNum = batchService.uploadNumGet(compId);
                        //useAmount = (uploadNum + uploadOnFixNum) * 0.5 * cucFixPrice;
                        useAmount = (lastRowNum + uploadOnFixNum) * cucFixPrice * channels.size();
                        logger.info("联通修复扣费销售定价:" + cucFixPrice + "\t账户余额为：" + remainAmount.toString() + "\t本地修复所需费用：" + useAmount.toString() + "\t本次上传数量：" + lastRowNum + "\t正在修复中的数量：" + uploadOnFixNum);
                    }
                    if (useAmount != null && (useAmount > remainAmount)) {
                        resultMap.put("code", "002");
                        resultMap.put("_message", "账户余额不足，上传失败！");
                        return JSON.toJSONString(resultMap);
                    } else if (lastRowNum > 1000) {
                        resultMap.put("code", "003");
                        resultMap.put("_message", "上传数据超过1000条记录，上传失败！");
                        return JSON.toJSONString(resultMap);
                    }
                    LinkedList<String> certlist = new LinkedList<>();
                    LinkedList<String> custuserIdlist = new LinkedList<>();
                    Boolean repeatIdCardStatus = false;
                    Boolean repeateEntrpriseIdStatus = false;
                    int uploadNum = 0;//弃用lastRowNum 防止其他空白行点击后产生空字符串数据
                    for (int i = 1; i <= lastRowNum; i++) {
                        Row row = sheet.getRow(i);
                        String name = "", phone = "", certifyMd5 = "";
                        if (row != null) {
                            short lastCellNum = row.getLastCellNum();
                            for (int j = 0; j < lastCellNum; j++) {
                                Cell cell = row.getCell(j);
                                if (cell != null && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
                                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                                    switch (j) {
                                        case 0:
                                            certlist.add(cell.getStringCellValue().trim());
                                            name = cell.getStringCellValue().trim();
                                            break;
                                        case 1:
                                            custuserIdlist.add(cell.getStringCellValue().trim());
                                            phone = cell.getStringCellValue().trim();
                                            break;
                                        case 2:
                                            certlist.add(cell.getStringCellValue().trim());
                                            certifyMd5 = cell.getStringCellValue().trim();
                                            break;

                                    }
                                }
                            }
                            if (StringUtil.isNotEmpty(certifyMd5)) {
                                uploadNum += 1;
                                batchListService.addressrepairupload(certifyMd5, name, batchId, phone, compId, channelall);
                            }
                        }
                    }
                    repeatIdCardStatus = batchService.repeatIdCardStatus(batchId);

                    if (repeatIdCardStatus) {
                        resultMap.put("code", "004");
                        resultMap.put("_message", "录入身份证加密数据不能重复，上传失败！");
                        return JSON.toJSONString(resultMap);
                    } else {
                        batchListService.Batch(batchname, uploadNum, repairMode, compId, batchId, channelall);
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
                return JSON.toJSONString(resultMap);
            }

        } catch (Exception e) {
            logger.error("失联修复文件上传失败！\t" + e.getMessage());
            resultMap.put("code", "001");
            resultMap.put("_message", "失联修复文件上传失败！");
            return JSON.toJSONString(resultMap);
        }
        return null;
    }


    /**
     * 地址修复模板下载
     */

    @RequestMapping("/express.do")
    @ResponseBody
    public String express(HttpServletRequest request, HttpServletResponse response) {
        InputStream in = null;
        OutputStream bos = null;
        try {
            String classPath = new BatchAction().getClass().getResource("/").getPath();
            String fileName = "地址修复模板.xlsx";
            String pathF = PROPERTIES.getProperty("file.separator");
            String path = classPath.substring(0, (classPath.length() - 8)) + "/" + pathF + "model" + pathF + fileName;
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));   //保存的文件名,必须和页面编码一致,否则乱码
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            in = new FileInputStream(path);
            bos = response.getOutputStream();
            byte[] b = new byte[2048];
            int length;
            while ((length = in.read(b)) > 0) {
                bos.write(b, 0, length);
            }
            bos.flush();
            return "ok";
        } catch (Exception e) {
            logger.error("地址修复模板下载异常" + "\r\n" + e.getMessage());
            return "error";
        } finally {
            try {
                in.close();
                bos.close();
                logger.info("模板文件下载成功" + "\t" + DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                return "ok";
            } catch (Exception e) {
                logger.error("BatchModelfileDownload" + "\t" + "io资源释放异常" + "\r\n" + e.getMessage());
                return "error";
            }
        }
    }


    /**
     * 失联修复文件下载
     */
    @RequestMapping(value = "/downloadBatchFixFile.do", method = RequestMethod.GET)
    @ResponseBody
    public String BatchFixfileDownload(String batchid, String suffxi, HttpServletResponse response) {
        InputStream in = null;
        OutputStream bos = null;
        try {
            //本地
            //String classPath = new BatchAction().getClass().getResource("/").getPath();
            //服务器
            String classPath = "/data/upload/";
            classPath += batchid + ".xlsx";
            String fileName = batchid + ".xlsx";
            //String pathF = PROPERTIES.getProperty("file.separator");
            //String path = classPath.substring(0,(classPath.length()-8)) + "/" +pathF+ "model"+pathF + fileName;
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));   //保存的文件名,必须和页面编码一致,否则乱码
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            in = new FileInputStream(classPath);
            bos = response.getOutputStream();
            byte[] b = new byte[2048];
            int length;
            while ((length = in.read(b)) > 0) {
                bos.write(b, 0, length);
            }
            bos.flush();
            return "ok";
        } catch (Exception e) {
            logger.error("修复批次文件下载异常" + "\r\n" + e.getMessage());
            return "error";
        } finally {
            try {
                in.close();
                bos.close();
                logger.info("修复批次文件下载成功" + "\t" + DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                return "ok";
            } catch (Exception e) {
                logger.error("downloadBatchUpFile" + "\t" + "io资源释放异常" + "\r\n" + e.getMessage());
                return "error";
            }
        }
    }


    /**
     * 修改批次表是否要传给联通修复
     */
    @RequestMapping(value = "/cucIsreceive", method = RequestMethod.GET)
    @ResponseBody
    public void CucIsreceive(String batchid, int cucIsReceived) {
        try {
            batchListService.cucIsreceive(batchid, cucIsReceived);
        } catch (Exception e) {
            logger.error("修改批次表是否要传给联通修复失败！\t" + e.getMessage());
        }

    }

    /**
     * 获取修复文件信息记录日志接口
     */
    @RequestMapping(value = "/batchOperlog.do", method = RequestMethod.GET)
    @ResponseBody
    public String BatchOperlog(String batchid) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = batchListService.batchOperlogLsit(batchid);
        map.put("data", list);
        return JSON.toJSONString(map);

    }


    //上传的类型渠道
    @RequestMapping(value = "/queryChannel", method = RequestMethod.GET)
    @ResponseBody
    public String QueryChannel() {
        String companyid = opUser().getCustId();
        List<Map<String, Object>> list = batchListService.ditchListv1(companyid);
        return JSON.toJSONString(list);

    }

    @RequestMapping(value = "/getTime.do", method = RequestMethod.GET)
    @ResponseBody
    public String getTime() {
        String companyid = opUser().getCustId();
        Object list = batchListService.getTime();
        return JSON.toJSONString(list);

    }


}
