package com.bdaim.image.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.service.BatchService;
import com.bdaim.callcenter.dto.RecordVoiceQueryParam;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.util.*;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.image.service.impl.UploadDowloadImgServiceImpl;
import com.bdaim.resource.service.MarketResourceService;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipOutputStream;

/**
 * 上传下载action
 * 2017/2/21
 */
@Controller
@RequestMapping("/upload")
public class UploadAction extends BasicAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(UploadAction.class);

    @Resource
    private UploadDowloadImgServiceImpl uploadDowloadService;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private BatchService batchService;
    @Resource
    private CustomerUserDao customerUserDao;

    /*
     * 上传企业营业执照副本
     */
    @RequestMapping(value = "/uploadImgBak", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public Object uploadImg(HttpServletRequest request,
                            HttpServletResponse response) {
        //path="/home/business/";
        String cust_id = "0";
        String pictureName = IDHelper.getTransactionId() + "";
        return uploadDowloadService.uploadImgNew(request, response, cust_id, pictureName);
    }
    @RequestMapping(value = "/uploadImg", method = RequestMethod.POST)
    @ResponseBody
    public Object uploadImg(@RequestParam MultipartFile file) {
        return uploadDowloadService.uploadImg(file);
    }


    /*
     * 下载企业营业执照副本
     */
    @RequestMapping(value = "/downloadImg", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public Object downloadDate(HttpServletRequest request,
                               HttpServletResponse response, String picName) {
        String custId = opUser().getCustId();
        String userId = custId;
        return uploadDowloadService.downloadImgNew(request, response, userId, picName);
    }


    /**
     * 凭证图片下载
     */
    @RequestMapping(value = "/getImg", method = RequestMethod.GET)
    @ResponseBody
    public String BatchModelfileDownload(String fileName, HttpServletResponse response) {
        FileInputStream fis = null;
        try {
            logger.info("图片名字是：" + fileName);
            File file = new File("/data/upload/0/" + fileName);
            //file = new File("C:\\Users\\Administrator\\Desktop\\" + fileName);
            fis = new FileInputStream(file);
            //IOUtils.copy(fis, response.getOutputStream());
            //BASE64Encoder encoder = new BASE64Encoder();
            byte[] byt = new byte[fis.available()];
            fis.read(byt);
            String base64 = Base64.encodeBase64String(byt);
            return JSON.toJSONString("data:image/jpg;base64,"+base64);
           /* response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));    //保存的文件名,必须和页面编码一致,否则乱码
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);*/
        } catch (Exception e) {
            logger.error("图片下载异常" + "\r\n" + e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                logger.info("图片下载成功" + "\t" + DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            } catch (Exception e) {
                logger.error("BatchModelfileDownload" + "\t" + "io资源释放异常" + "\r\n" + e.getMessage());
            }
        }
        return null;
    }


    /**
     * 前台--录音文件下载
     */
    @AuthPassport
    @RequestMapping(value = "/open/downloadSound.do", method = RequestMethod.POST)
    @ResponseBody
    public String downloadSound(@RequestBody JSONObject param, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();
        String basePath = ConfigUtil.getInstance().get("audio_server_url") + "/";
        RecordVoiceQueryParam recordVoiceQueryParam = new RecordVoiceQueryParam();
        String touchId = param.getString("touchId");
        recordVoiceQueryParam.setTouchId(touchId);
        List<Map<String, Object>> resultList = marketResourceService.soundUrl(recordVoiceQueryParam);
        String userId = "";
        String fileName = "";
        if (resultList != null && resultList.size() > 0) {
            if (resultList.get(0).get("recordurl") != null) {
                fileName = resultList.get(0).get("recordurl").toString();
                if (StringUtil.isNotEmpty(fileName)) {
                    fileName = fileName.substring((fileName.lastIndexOf("/") + 1), (fileName.length()));
                }
            }
            if (resultList.get(0).get("user_id") != null) {
                userId = resultList.get(0).get("user_id").toString();

            }
        }
        String targetPath = basePath + userId + File.separator + fileName;

        //本地
		/*String targetPath = "http://online.datau.top/audio/1810090141300001/TEL-18630016545_BJHKK_zx1_20181010192854.wav";
		String fileName = targetPath.substring((targetPath.lastIndexOf("/")+1),(targetPath.length()));*/
        logger.info("文件名：" + fileName + "文件路径：" + targetPath);

        try {
            URL url = new URL(targetPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            InputStream inputStream = conn.getInputStream();
            //获取自己数组
            byte[] bs = readInputStream(inputStream);
            response.setContentType("application/octet-stream;charset=ISO8859-1");
            BufferedOutputStream output = null;
            BufferedInputStream input = null;
            try {
                output = new BufferedOutputStream(response.getOutputStream());
                // 中文文件名必须转码为 ISO8859-1,否则为乱码
                String fileNameDown = new String(fileName.getBytes(), "ISO8859-1");
                // 作为附件下载
                response.setHeader("Content-Disposition", "attachment;filename=" + fileNameDown);
                output.write(bs);
                response.flushBuffer();
                resultMap.put("code", "000");
                resultMap.put("msg", "录音文件下载成功！");
                json.put("data", resultMap);
                return json.toJSONString();
            } catch (Exception e) {
                logger.error("Download log file error", e);
                resultMap.put("code", "001");
                resultMap.put("msg", "录音文件下载异常！");
                json.put("data", resultMap);
                return json.toJSONString();
            } // 用户可能取消了下载
            finally {
                if (input != null)
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (output != null)
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            resultMap.put("code", "001");
            resultMap.put("msg", "录音文件下载异常！");
            json.put("data", resultMap);
            return json.toJSONString();
        }
    }

    /**
     * 前台--录音文件批量下载
     */
    @RequestMapping(value = "/downloadSounds", method = RequestMethod.GET)
    @ResponseBody
    public String downloadSounds(HttpServletResponse res, RecordVoiceQueryParam recordVoiceQueryParam) throws IOException {
        String fileName = "";
        String userid = "";
        List<String> filePaths = new ArrayList<>();

        //本地
		/*String zipFilePath = "F:/huoke-workplace/nolose/out/artifacts/nolose_war_exploded/WEB-INF/classes/0/";
		String zipName = "Sounds.zip";
		filePaths.add("http://online.datau.top/audio/1810090141300001/TEL-18630016545_BJHKK_zx1_20181010192854.wav");
		filePaths.add("http://online.datau.top/audio/1810260511230004/TEL-18630016545_BJHKK_zx1_20181031194621.wav");
		filePaths.add("http://online.datau.top/audio/1810260511230004/TEL-18630016545_BJHKK_zx1_20181031162352.wav");*/
        //服务器
        String basePath = ConfigUtil.getInstance().get("audio_server_url") + "/";
        recordVoiceQueryParam.setCustId(opUser().getCustId());
        recordVoiceQueryParam.setUserType(opUser().getUserType());
        recordVoiceQueryParam.setUserId(opUser().getId());
        List<Map<String, Object>> resultList = marketResourceService.soundUrllist(recordVoiceQueryParam);
        String batchId = "";
        String batchName = "";
        String realName = "";
        String createTime = "";
        for (Map map : resultList) {
            if (map != null) {
                //失联修复录音文件命名：批次名称+坐席昵称+用户ID+致电时间戳
                //压缩包名：失联修复录音文件+时间戳
                if (map.get("batch_id") != null) {
                    batchId = map.get("batch_id").toString();
                    batchName = batchService.batchNameGet(batchId);
                }
                if (map.get("user_id") != null) {
                    userid = map.get("user_id").toString();
                    realName = customerUserDao.getName(userid);
                }
                if (map.get("create_time") != null) {
                    logger.info("致电时间：" + createTime);
                    createTime = map.get("create_time").toString();
                    createTime = createTime.replaceAll(" ", "_");
                }
                if (map.get("recordurl") != null) {
                    fileName = map.get("recordurl").toString();
                    if (StringUtil.isEmpty(fileName)) {
                        logger.warn("打包录音文件fileName:" + fileName + ",为空");
                        continue;
                    }
                    if (fileName.indexOf(".") == -1) {
                        logger.warn("打包录音文件fileName:" + fileName + ",路径异常");
                        continue;
                    }
                    fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
                    filePaths.add(fileName + "#" + createTime);
                }
            }
        }
        String nowtime = DateUtil.fmtDateToStr(new Date(), "yyyy-MM-dd_HH:mm:ss");
        //String zipName = "失联修复录音文件-";
        //zipName = URLEncoder.encode(zipName,"UTF-8");
        String zipName = "失联修复录音文件-" + nowtime + ".zip";
        ;
        zipName = uploadDowloadService.toUtf8String(request, zipName);
        String zipFilePath = "/tmp/" + userid + "/";


        res.setContentType("text/html; charset=UTF-8");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-disposition", "attachment;filename=" + fileName);//设置下载的文件名称
        OutputStream out = res.getOutputStream();   //创建页面返回方式为输出流，会自动弹出下载框


        creatFile(zipFilePath, zipName);
        logger.info("创建空的zip包文件路径：" + zipFilePath + "\tzip文件名：" + zipName);
        File zip = new File(zipFilePath + zipName);
        logger.info("zip包文件路径：" + zip.getPath());

        //创建zip文件输出流
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
        uploadDowloadService.zipFileUrlV1(filePaths, zos, batchName, realName, userid);
        zos.close();
        res.setHeader("Content-disposition", "attachment;filename=" + zipName);

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFilePath + zipName));
        byte[] buff = new byte[bis.available()];
        bis.read(buff);
        bis.close();
        out.write(buff);
        out.flush();
        out.close();
        return null;
    }

    public static void creatFile(String filePath, String fileName) {
        File folder = new File(filePath);
        //文件夹路径不存在
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        } else {
            logger.info("文件夹路径存在:" + filePath);
        }

        // 如果文件不存在就创建
        File file = new File(filePath + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("文件已存在，文件为:" + filePath + fileName);
        }
    }


    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static void main(String[] args) {
        String tmpName = "http://online.datau.top/audio/1811060826120000/fb792854-5eb5-11e9-a59c-00163e0e3936_00163e0e3936a59c11e95eb5d629e49e.mp3#2019-04-14_21:04:28.0";
        System.out.println(File.separator);
        String[] names = tmpName.split("#");
        String fileName = tmpName.substring(names[0].lastIndexOf("/") + 1, names[0].length());
        String createTime = names[1];
        System.out.println(fileName);
        System.out.println(createTime);
    }
}
