package com.bdaim.batch.controller;

import com.bdaim.batch.entity.BatchInfo;
import com.bdaim.batch.service.ExpressBatchService;
import com.bdaim.common.response.JsonResult;
import com.bdaim.common.response.ResponseBody;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.response.ResultCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @description: 发件批次 信息管理 (失联修复_信函模块)
 * @auther: Chacker
 * @date: 2019/7/31 10:09
 */
@RequestMapping("/express")
@RestController
public class ExpressBatchController {

    private static Log logger = LogFactory.getLog(ExpressBatchController.class);
    protected static final Properties PROPERTIES = new Properties(System.getProperties());

    @Autowired
    private ExpressBatchService expressBatchService;

    /**
     * 下载收件人信息模板
     *
     * @param response
     * @return java.lang.String
     * @auther Chacker
     * @date 2019/7/31 14:54
     */
    @RequestMapping("/receiverModel")
    public ResponseBody downloadReceiverModel(HttpServletResponse response) {
        InputStream in = null;
        OutputStream bos = null;
        try {
            //获取resources下的模板文件路径
            String classPath = new ExpressBatchController().getClass().getResource("/").getPath();
            String fileName = "收件人信息模板.xlsx";
            String pathF = PROPERTIES.getProperty("file.separator");
            String path = classPath + pathF + fileName;

            //下载的response属性设置
            response.setCharacterEncoding("utf-8");
            //application/vnd.ms-excel;charset=utf-8" xls格式，注释掉
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));   //保存的文件名,必须和页面编码一致,否则乱码
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            in = new FileInputStream(path);
            bos = response.getOutputStream();

            //以字节方式读取，放入输出流
            byte[] b = new byte[2048];
            int length;
            while ((length = in.read(b)) > 0) {
                bos.write(b, 0, length);
            }
            bos.flush();
            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(200, "500", "模板下载异常，请稍后重试或联系网站管理员");
        } finally {
            try {
                in.close();
                bos.close();
            } catch (Exception e) {
                logger.error("receiverModel" + "\t" + "io资源释放异常" + "\r\n" + e.getMessage());
            }
        }
    }

    /**
     * 上传发件信息(上传excel文件,对文件中的收件人信息进行批量导入)
     *
     * @param multipartFile excel文件
     * @param batchName     批次名称
     * @return
     * @auther Chacker
     * @date 2019/7/31 14:54
     */
    @RequestMapping(value = "/receiverInfoImport")
    public ResponseBody receiverInfoImport(@RequestParam(value = "file") MultipartFile multipartFile, String batchName, int expressContent, String custId) throws IOException {
        expressBatchService.receiverInfoImport(multipartFile, batchName, expressContent, custId);
        return new ResponseInfoAssemble().success("hello");
    }

    /**
     * 分页查询批次列表
     *
     * @param map pageNum、pageSize、custId 包括分页参数和企业ID
     * @return
     * @auther Chacker
     * @date 2019/8/1 16:33
     */
    @RequestMapping(value = "/batchList",method = RequestMethod.GET)
    public ResponseBody batchList(@RequestBody Map<String, Object> map) throws IllegalAccessException {
        List<Map<String,Object>> resultList = expressBatchService.batchList(map);
        return new ResponseInfoAssemble().success(resultList);
    }

}

