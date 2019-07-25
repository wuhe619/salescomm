package com.bdaim.batch.controller;


import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.page.Page;
import com.bdaim.slxf.entity.*;
import com.bdaim.smscenter.service.SendmessageService;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wangxx@bdaim.com
 * @Description:
 * @date 2018/12/27 10:31
 */
@Controller
@RequestMapping(value = "/express")
public class SiteRepairAction extends BasicAction{
    private static Log logger = LogFactory.getLog(SiteRepairAction.class);
    @Resource
    private SendmessageService sendmessageService;

   //添加

    @RequestMapping(value = "/sendadd.do", method = RequestMethod.POST)
    @ResponseBody
    public String sendadd(@RequestBody SenderInfo senderInfo) {
        String compId = opUser().getCustId();
        logger.info("当前企业id是:" + compId);
        Map<Object, Object> list=sendmessageService.sendadd(senderInfo,compId);

        return JSON.toJSONString(list);
    }

    //修改
    @ResponseBody
    @RequestMapping(value = "/sendupdate.do", method = RequestMethod.POST)
    public String sendupdate(@RequestBody SenderInfo senderInfo) {
        Map<Object, Object> list=sendmessageService.sendupdate(senderInfo);
        return JSON.toJSONString(list);

    }

    //设置默认
    @ResponseBody
    @RequestMapping(value = "/defaultupdate.do", method = RequestMethod.GET)
    public String defaultupdate(String id) {
        String compId = opUser().getCustId();
        Map<Object, Object> list=sendmessageService.defaultupdate(id,compId);
        return JSON.toJSONString(list);
    }


   //发件信息查询
    @ResponseBody
    @RequestMapping(value = "/sendlist.do", method = RequestMethod.GET)
    public Object sendlist(@Valid PageParam page, BindingResult error) {
        if (error.hasFieldErrors()) {
            page.setPageNum(1);
            page.setPageSize(20);
        }
        if (page.getPageSize() > 100) {
            page.setPageSize(100);
        }

        String compId = opUser().getCustId();
        logger.info("当前企业id是:" + compId);
        Page list = null;
       list =sendmessageService.sendlist(page,compId);

        return JSON.toJSONString(list);
    }

    //发件信息删除
    @ResponseBody
    @RequestMapping(value = "/senddelete.do", method = RequestMethod.GET)
    public String senddelete(String id) {
        Map<Object, Object> list=sendmessageService.senddelete(id);

        return JSON.toJSONString(list);

    }





    //快递记录查询
    @RequestMapping(value = "/expressRecord.do", method = RequestMethod.GET)
    @ResponseBody
    public String searchPropertyList(@Valid PageParam page, BindingResult error,ExpressLog expressLog) {
        if (error.hasFieldErrors()) {
            page.setPageNum(1);
            page.setPageSize(20);
        }
        if (page.getPageSize() > 100) {
            page.setPageSize(100);
        }
        Page list = sendmessageService.pageList(page, expressLog);
        return JSON.toJSONString(list);
    }



    //快递记录导出
    @RequestMapping(value = "/exportRecords.do", method = RequestMethod.GET, produces = "application/vnd.ms-excel;charset=UTF-8")
    @ResponseBody
    public Object exportRecords(ExpressLog expressLog, HttpServletResponse response) {

        return sendmessageService.exportExportRecords(expressLog, response);

    }

    //修复详情导出
    @RequestMapping(value = "/repair/detailsderive.do", method = RequestMethod.GET, produces = "application/vnd.ms-excel;charset=UTF-8")
    @ResponseBody
    public Object repairDetailsderive(String batchid,String name,String phone,String touch_id,Integer status,Integer Status, HttpServletResponse response) {

        return sendmessageService.repairDetailsderive(batchid,name,phone,touch_id,status,Status,response);

    }


    //修复详情展示

    @RequestMapping(value = "/repairdetails", method = RequestMethod.GET)
    @ResponseBody
    public String repairDetails(String batchid,String name,String phone,String touch_id,Integer status,Integer Status, Integer pageNum, Integer pageSize) {
        JSONObject json = new JSONObject();
        List<Map<String, Object>> list = sendmessageService.repairDetails(pageNum,pageSize,batchid,name,phone,touch_id,status,Status);
        Map<String, Object> time= sendmessageService.time(batchid);

        json.put("total",list.size());
        json.put("atlatest",time.get("atlatest"));
        json.put("initialmortgage",time.get("initialmortgage"));
        json.put("list",list);
        return json.toJSONString();


    }


    //上传pdf文件

    @ResponseBody
    @RequestMapping(value = "/basicInfo/uploadFile", method = RequestMethod.POST)
    public String uploadFile(@RequestParam(value = "file", required = false) MultipartFile file,
                             HttpServletRequest request) throws IOException {
        Map<Object, Object> map = new HashMap<>();

        File targetFile = null;
        String msg = "";// 返回存储路径
        int code = 1;
        String fileName = file.getOriginalFilename();// 获取文件名加后缀
        if (fileName != null && fileName != "") {
            String returnUrl = "http://";// 访问路径
            String path = "/data/upload/"; // 文件存储位置


            //文件名加上时间戳
            String batchId = String.valueOf(System.currentTimeMillis());
            if (fileName.matches("^.+\\.(?i)(pdf)$")) {
                fileName = batchId + ".pdf";
                //得到目标文件对象
            }

           /* String fileF = fileName.substring(fileName.lastIndexOf("."), fileName.length());// 文件后缀
            fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + fileF;// 新的文件名*/
            // 先判断文件是否存在
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String fileAdd = sdf.format(new Date());
            File file1 = new File(path + File.separator + fileAdd);
            // 如果文件夹不存在则创建
            if (!file1.exists() && !file1.isDirectory()) {
                file1.mkdir();
            }
            targetFile = new File(file1, String.valueOf(fileName));
            try {
                file.transferTo(targetFile);
                msg = returnUrl + fileAdd + File.separator  + fileName;
                code = 0;

            } catch (Exception e) {
                e.printStackTrace();
            }
            String batch_id = request.getParameter("batch_id");
            String id_card = request.getParameter("id_card");
            sendmessageService.add(fileAdd + "/" + fileName, batch_id, id_card);


        }
        return JSON.toJSONString(msg);
    }


    /*@RequestMapping(value = "/show_attach", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public void show_attach(HttpServletRequest request, HttpServletResponse response) {
        FileInputStream bis = null;
        OutputStream os = null;
        try {
            String path = request.getParameter("filePath");//网络图片地址
            response.setContentType("text/html; charset=UTF-8");
            String type = request.getParameter("type");
            if ("pdf".equalsIgnoreCase(type)) {
                response.setContentType("application/pdf");
            } else {
                response.setContentType("image/" + type);
            }
            bis = new FileInputStream(path);
            os = response.getOutputStream();
            int count = 0;
            byte[] buffer = new byte[1024 * 1024];
            while ((count = bis.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/






























  /* //下载文件
   @RequestMapping("/devDoc")
    @ResponseBody
    public void devDoc(HttpServletRequest request, HttpServletResponse response, String storeName) throws Exception {
        request.setCharacterEncoding("UTF-8");
        String ctxPath = request.getSession().getServletContext().getRealPath("");
        String downLoadPath = ctxPath + storeName;
        response.setContentType("application/pdf");
        FileInputStream in = new FileInputStream(new File(downLoadPath));
        OutputStream out = response.getOutputStream();
        byte[] b = new byte[512];
        while ((in.read(b))!=-1) {
            out.write(b);
        }
        out.flush();
        in.close();
        out.close();
    }*/

/*
    @RequestMapping("/displayPDF.do")
    @ResponseBody
    public void displayPDF(HttpServletResponse response,String storeName) {
        try {
            File file = new File("/manager/images/"+storeName);
            FileInputStream fileInputStream = new FileInputStream(file);
            response.setHeader("Content-Disposition", "attachment;fileName=test.pdf");
            response.setContentType("multipart/form-data");
            OutputStream outputStream = response.getOutputStream();
            IOUtils.write(IOUtils.toByteArray(fileInputStream), outputStream);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }*/




   /* @RequestMapping(value="/downFileTwo")
    public void downloadtwo(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id, Model model)throws Exception {
        UpDownQuery query = new UpDownQuery();
        query.setId(id);
        List<UpDown> upDownList = upDownService.findUpDownAll(query);
        //前面三行代码是从数据库里查询文件路径
        if(upDownList.size()==1){
            //下载文件路径
            String path = upDownList.get(0).getUrl()+"/";
            //文件名称
            String allName = upDownList.get(0).getAllName();
            InputStream inputStream = new FileInputStream(path+allName);
            if(inputStream!=null){
                try {
                    BufferedImage image = ImageIO.read(inputStream);
                    //判断是否是图片 1.是预览  2.下载
                    if(image!=null){
                        //打开本地文件流
                        //激活下载操作
                        response.setContentType("image/jpeg");
                        ImageIO.write(image, "JPEG", response.getOutputStream());
                    }else {
                        response.setContentType("application/force-download");
                        String downloadFielName = new String(allName.getBytes("UTF-8"),"iso-8859-1");
                        response.addHeader("Content-Disposition","attachment;fileName=" + downloadFielName);// 设置文件名
                        OutputStream outputStream = response.getOutputStream();
                        byte[] bytes = new byte[2048];
                        int len = 0;
                        while ((len = inputStream.read(bytes))>0){
                            outputStream.write(bytes,0,len);
                        }
                        inputStream.close();
                        outputStream.close();
                    }

                } catch (Exception e){
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write("系统错误或文件已被删除！请联系管理员！");
                    throw e;
                }
            }else {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("系统错误或文件已被删除！请联系管理员！");
            }

        }*/








  /*  @RequestMapping(value = "/up_tx", method = RequestMethod.POST)
    public String uploadFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.request = request;
        String responseStr = "";
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 获取前台传值
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        // 获取上传文件存放的 目录 , 无则创建
        String configPath = request.getSession().getServletContext().getRealPath( "/images/usertx/" );

        // 创建文件夹
        File file = new File(configPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = null;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 上传文件名
            // System.out.println("key: " + entity.getKey());
            MultipartFile mf = entity.getValue();
            fileName = mf.getOriginalFilename();

            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String newFileName = df.format(new Date()) + "_" + new Random().nextInt(1000) + "." + fileExt;
            responseStr = "/images/usertx/" + newFileName ;
            File uploadFile = new File(configPath+"/" + newFileName);
            try {
                FileCopyUtils.copy(mf.getBytes(), uploadFile);
            } catch (IOException e) {
                responseStr = "上传失败";
                e.printStackTrace();
            }

        }
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        // 这句话的意思，是告诉servlet用UTF-8转码，而不是用默认的ISO8859
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseStr);
        return null;
    }
*/


  /*  }*/

 /*   @Override
    @ResponseBody
    @RequestMapping(value = "/basicInfo", method = RequestMethod.POST)
public void addResourceHandlers( @RequestParam ResourceHandlerRegistry registry) {
registry.addResourceHandler("/**")
.addResourceLocations("classpath:/META-INF/resources/")
        .addResourceLocations("classpath:/resources/")
.addResourceLocations("classpath:/static/")
.addResourceLocations("classpath:/public/");
 registry.addResourceHandler("/onlineFile/**").addResourceLocations("file:D:/data/upload/20190118/");
 super.addResourceHandlers(registry);
 }*/









 //提交快递

    @ResponseBody
    @RequestMapping(value = "/Submit/Courier", method = RequestMethod.GET)
    public String submitCourier(String siteid,String bachid) {
        Map<String,Object> map = new HashMap<>();
        String companyid = opUser().getCustId();
        Map<String, Object> list = sendmessageService.submitCourier(siteid, companyid,bachid);
        map.put("list",list);



        return JSON.toJSONString(map);
    }


    //快递记录

    @ResponseBody
    @RequestMapping(value = "/Submit/express", method = RequestMethod.GET)
    public String express(String touch_id) {
        Map<String,Object> map = new HashMap<>();
        Map<String, Object> list = sendmessageService.express(touch_id);
        map.put("markedWord",list);

        return JSON.toJSONString(map);
    }



}































