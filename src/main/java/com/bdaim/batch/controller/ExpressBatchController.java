package com.bdaim.batch.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.batch.service.ExpressBatchService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.FileUrlEntity;
import com.bdaim.common.util.StringUtil;
import com.github.crab2died.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @description: 发件批次 信息管理 (失联修复_信函模块)
 * @auther: Chacker
 * @date: 2019/7/31 10:09
 */
@RequestMapping("/express")
@Controller
public class ExpressBatchController extends BasicAction {

    private static Logger logger = LoggerFactory.getLogger(ExpressBatchController.class);
    protected static final Properties PROPERTIES = new Properties(System.getProperties());

    @Autowired
    private ExpressBatchService expressBatchService;
    @Autowired
    private FileUrlEntity fileUrlEntity;

    /**
     * 下载收件人信息模板
     *
     * @param response
     * @return java.lang.String
     * @auther Chacker
     * @date 2019/7/31 14:54
     */
    @RequestMapping(value = "receiverModel", method = RequestMethod.GET, produces = "application/vnd.ms-excel;charset=UTF-8")
    @ResponseBody
    public ResponseInfo downloadReceiverModel(@RequestParam String file_type, HttpServletResponse response) {
        InputStream in = null;
        OutputStream bos = null;
        try {
            //获取resources下的模板文件路径String classPath = this.getClass().getResourceAsStream("/").toString();
            String classPath = fileUrlEntity.getFileUrl();
            logger.error("hello classpath" + classPath);
            String fileName = null;
            if ("1".equals(file_type)) {
                fileName = "receiver_info.xlsx";
            } else if ("2".equals(file_type)) {
                fileName = "file_code_receipt_mapping.xlsx";
            }
            String pathF = PROPERTIES.getProperty("file.separator");
            classPath = classPath.replace("/", pathF);
            String path = classPath + pathF + "tp" + pathF + fileName;

            //下载的response属性设置
            response.setCharacterEncoding("utf-8");
//            response.setContentType("application/force-download");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));   //保存的文件名,必须和页面编码一致,否则乱码
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            in = new FileInputStream(new File(path));
            logger.error("hello chacker" + path);
            bos = response.getOutputStream();

            //以字节方式读取，放入输出流
            byte[] b = new byte[2048];
            int length;
            while ((length = in.read(b)) > 0) {
                bos.write(b, 0, length);
            }
            bos.flush();
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(500, "模板下载异常，请稍后重试或联系网站管理员");
        } finally {
            try {
                in.close();
                bos.close();
            } catch (Exception e) {
                logger.error("receiverModel" + "\t" + "io资源释放异常" + "\r\n" + e.getMessage());
            }
        }
        return new ResponseInfoAssemble().success(null);
    }

    /**
     * 上传发件信息(上传excel文件,对文件中的收件人信息进行批量导入)
     *
     * @param file               excel文件
     * @param batchName          批次名称
     * @param expressContentType 快件内容 1、电子版 2、打印版
     * @param custId             企业ID，用户登录后获取custId
     * @return
     * @auther Chacker
     * @date 2019/7/31 14:54
     */
    @RequestMapping(value = "/receiverInfoImport")
    @ResponseBody
    public ResponseInfo receiverInfoImport(@RequestParam(value = "file") MultipartFile file, String batchName, int expressContentType, String custId) {
        ResponseInfo result = expressBatchService.receiverInfoImport(file, batchName, expressContentType, custId);
        return result;
    }

    /**
     * 分页查询批次列表
     *
     * @param map pageNum、pageSize、custId 包括分页参数和企业ID
     * @return
     * @auther Chacker
     * @date 2019/8/1 16:33
     */
    @RequestMapping(value = "/batchList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo batchList(@RequestParam Map<String, Object> map) throws IllegalAccessException {
        LoginUser lu = opUser();
        if (StringUtil.isEmpty(String.valueOf(map.get("page_num"))) || StringUtil.isEmpty(String.valueOf(map.get("page_size")))) {
            return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
        }
        Map<String, Object> resultMap = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            if (lu.getType() != 0) {
                //说明是封装员或者打印员登录
                map.put("loginType", lu.getType());
                map.put("userId", lu.getId());
            }
            resultMap = expressBatchService.batchList(map);
        } else {
            String custId = opUser().getCustId();
            map.put("cust_id", custId);
            resultMap = expressBatchService.batchList(map);
        }
        return new ResponseInfoAssemble().success(resultMap);
    }

    /**
     * 查询批次详情
     *
     * @param map batch_id
     * @return
     * @auther Chacker
     * @date 2019/8/2 14:38
     */
    @RequestMapping(value = "/batchDetail", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo batchDetail(@RequestParam Map<String, Object> map) throws IllegalAccessException {
        Map<String, Object> resultMap = expressBatchService.batchDetail(map);
        return new ResponseInfoAssemble().success(resultMap);
    }

    /**
     * 上传/批量上传发件内容
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/6 11:46
     */
    @RequestMapping(value = "/sendMessageUpload", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo sendMessageUpload(MultipartFile expressContent, MultipartFile fileCodeMapping, String receiverId, String batchId) throws IOException {
        ResponseInfo result = expressBatchService.sendMessageUpload(expressContent, fileCodeMapping, receiverId, batchId);
        return result;
    }

    /**
     * 上传模板文件，此接口不对前端提供，只是后端人员在(换环境)发布程序后使用
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/6 23:28
     */
    @RequestMapping(value = "/uploadModelFile", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo uploadModelFile(MultipartFile multipartFile) throws IOException {
        expressBatchService.uploadModelFile(multipartFile);
        return new ResponseInfoAssemble().success(null);
    }

    /**
     * 通过批次ID和收件人ID获取pdf
     *
     * @param batchId    批次ID
     * @param receiverId 收件人ID
     * @return
     * @auther Chacker
     * @date 2019/8/7 11:33
     */
    @RequestMapping(value = "/getPdfByReceiverId", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo readFileByCode(@RequestParam String batchId, String receiverId, HttpServletResponse response) throws IOException {
        logger.info("进入根据收件人ID预览PDF接口" +batchId +receiverId);
        //根据批次ID batchId 和 收件人ID receiverId 找到pdf所存储的路径
        String pdfPath = expressBatchService.findPdfPathByReceiverId(batchId, receiverId);
        pdfPath = pdfPath.substring(pdfPath.indexOf("pdf") - 1).replace("\\", "/");
        logger.info("pdf path is "+pdfPath);
        return new ResponseInfoAssemble().success(pdfPath);
    }


    /**
     * 导出批次详情
     *
     * @param map      export_type 1前端批次详情导出 2后台批次详情导出 3后台映射关系导出batch_id 批次ID
     * @param response response
     * @return
     * @auther Chacker
     * @date 2019/8/7 20:38
     */
    @RequestMapping(value = "/batchDetailExport", method = RequestMethod.GET)
    @ResponseBody
    public void batchDetailExport(@RequestParam Map<String, Object> map, HttpServletResponse response) throws IOException {
        //根据批次ID找到数据信息
        List<Map<String, Object>> dataList = expressBatchService.findDetailByBatchId(map);
        int exportType = Integer.parseInt(String.valueOf(map.get("export_type")));
        List<String> header = new ArrayList<>();
        List<List<Object>> data = new ArrayList<>();
        if (exportType == 1) {
            header.add("文件编码(不能重复)");
            header.add("收件ID(不能重复)");
            header.add("姓名");
            header.add("电话");
            header.add("地址");
            List<Object> rowList;
            for (Map<String, Object> column : dataList) {
                rowList = new ArrayList<>();
                rowList.add(column.get("fileCode") != null ? column.get("fileCode") : "");
                rowList.add(column.get("receiverId") != null ? column.get("receiverId") : "");
                rowList.add(column.get("name") != null ? column.get("name") : "");
                rowList.add(column.get("phone") != null ? column.get("phone") : "");
                rowList.add(column.get("address") != null ? column.get("address") : "");
                data.add(rowList);
            }
        } else if (exportType == 2) {
            header.add("地址ID");
            header.add("快递单号");
            header.add("收件ID");
            header.add("姓名");
            header.add("电话");
            header.add("校验结果");
            header.add("快件状态");
            header.add("文件编码");
            List<Object> rowList;
            for (Map<String, Object> column : dataList) {
                rowList = new ArrayList<>();
                rowList.add(column.get("addressId") != null ? column.get("addressId") : "");
                rowList.add(column.get("expressCode") != null ? column.get("expressCode") : "");
                rowList.add(column.get("receiverId") != null ? column.get("receiverId") : "");
                rowList.add(column.get("name") != null ? column.get("name") : "");
                rowList.add(column.get("phone") != null ? column.get("phone") : "");
                rowList.add(column.get("status") != null ? column.get("status") : "");
                rowList.add(column.get("expressStatus") != null ? column.get("expressStatus") : "");
                rowList.add(column.get("fileCode") != null ? column.get("fileCode") : "");
                data.add(rowList);
            }
        } else if (exportType == 3) {
            header.add("文件编码(不能重复)");
            header.add("快递单号(不能重复)");
            header.add("姓名");
            List<Object> rowList;
            for (Map<String, Object> column : dataList) {
                rowList = new ArrayList<>();
                rowList.add(column.get("fileCode") != null ? column.get("fileCode") : "");
                rowList.add(column.get("expressCode") != null ? column.get("expressCode") : "");
                rowList.add(column.get("name") != null ? column.get("name") : "");
                data.add(rowList);
            }
        }

        //下载的response属性设置
        response.setCharacterEncoding("utf-8");
//        response.setContentType("application/force-download");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "批次详情.xlsx";
        String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));   //保存的文件名,必须和页面编码一致,否则乱码
        response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
        OutputStream outputStream = response.getOutputStream();


        ExcelUtils.getInstance().exportObjects2Excel(data, header, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 更改批次状态
     *
     * @param
     * @retur
     * @date 2019/8/2 14:38
     */
    @RequestMapping(value = "/updateBatchStatus", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo updateBatchStatus(String batchId, int status) {
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                expressBatchService.updateBatchStatus(batchId, status);
            } else {
                return new ResponseInfoAssemble().failure(-2, "暂无权限");
            }
        } catch (Exception e) {
            logger.error("修改批次状态异常" + e);
            return new ResponseInfoAssemble().failure(-1, "修改批次状态异常");
        }
        return new ResponseInfoAssemble().success(null);
    }

    /**
     * 根据地址ID修改文件编码
     *
     * @param addressId 地址ID
     * @return
     * @auther Chacker
     * @date 2019/8/8 15:10
     */
    @RequestMapping(value = "/updateFileCode", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo updateFileCodeByReceiverId(@RequestParam String addressId, @RequestParam String fileCode) {
        expressBatchService.updateFileCode(addressId, fileCode);
        return new ResponseInfoAssemble().success(null);
    }

    /**
     * 后台导出信函文件(pdf的压缩包、zip文件)
     *
     * @param map batch_id 批次ID
     * @return
     * @auther Chacker
     * @date 2019/8/9 14:05
     */
    @RequestMapping(value = "/exportFile", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo exportExpressFile(@RequestParam Map<String, Object> map, HttpServletResponse response) {
        //根据批次ID查询出zip文件的存储路径
        Map<String, Object> zipPathMap = expressBatchService.queryPathByBatchId(map);
        String zipPath = String.valueOf(zipPathMap.get("zipPath"));
        String pathF = PROPERTIES.getProperty("file.separator");
        zipPath = zipPath.replace("\\", pathF).replace("/", pathF);
        //设置响应值
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/zip");
        String downloadZipFileName = zipPath.substring(zipPath.lastIndexOf(pathF) + 1);
        response.setHeader("Content-Disposition", "attachment;filename=" + downloadZipFileName);
        logger.info("导出的文件名称为"+downloadZipFileName);
        InputStream inputStream = null;
        OutputStream bos = null;
        try {
            inputStream = new FileInputStream(new File(zipPath));
            bos = response.getOutputStream();
            //以字节方式读取，放入输出流
            byte[] b = new byte[2048];
            int length;
            while ((length = inputStream.read(b)) > 0) {
                bos.write(b, 0, length);
            }
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("导出信函文件异常" + e.getMessage());
        } finally {
            try {
                inputStream.close();
                bos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ResponseInfoAssemble().success(null);
    }

}

