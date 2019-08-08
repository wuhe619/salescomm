package com.bdaim.batch.controller;

import com.bdaim.batch.service.ExpressBatchService;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.FileUrlEntity;
import com.github.crab2died.ExcelUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
public class ExpressBatchController {

    private static Log logger = LogFactory.getLog(ExpressBatchController.class);
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
            String path = classPath + pathF + fileName;

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
    public ResponseInfo receiverInfoImport(@RequestParam(value = "file") MultipartFile file, String batchName, int expressContentType, String custId) throws IOException {
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
        Map<String, Object> resultMap = expressBatchService.batchList(map);
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
    public void readFileByCode(@RequestParam String batchId, String receiverId, HttpServletResponse response) throws IOException {
        //根据批次ID batchId 和 收件人ID receiverId 找到pdf所存储的路径
        String pdfPath = expressBatchService.findPdfPathByReceiverId(batchId, receiverId);
        //设置response属性
//        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/pdf");

        //通过输出流输出pdf文件
        FileInputStream inputStream = new FileInputStream(new File(pdfPath));
        OutputStream outputStream = response.getOutputStream();
        byte[] b = new byte[1024];
        int length;
        while ((length = inputStream.read(b)) > 0) {
            outputStream.write(b, 0, length);
        }
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }


    /**
     * 导出批次详情
     *
     * @param batch_id 批次ID
     * @param response response
     * @return
     * @auther Chacker
     * @date 2019/8/7 20:38
     */
    @RequestMapping(value = "/batchDetailExport", method = RequestMethod.GET)
    @ResponseBody
    public void batchDetailExport(@RequestParam String batch_id, HttpServletResponse response) throws IOException {
        //根据批次ID找到数据信息
        List<Map<String, Object>> dataList = expressBatchService.findDetailByBatchId(batch_id);
        List<String> header = new ArrayList<>();
        header.add("文件编码(不能重复)");
        header.add("收件ID(不能重复)");
        header.add("姓名");
        header.add("电话");
        header.add("地址");
        //下载的response属性设置
        response.setCharacterEncoding("utf-8");
//        response.setContentType("application/force-download");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = batch_id + "批次详情.xlsx";
        String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));   //保存的文件名,必须和页面编码一致,否则乱码
        response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
        OutputStream outputStream = response.getOutputStream();

        List<List<Object>> data = new ArrayList<>();
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


        ExcelUtils.getInstance().exportObjects2Excel(data, header, outputStream);
        outputStream.flush();
        outputStream.close();
    }

}

