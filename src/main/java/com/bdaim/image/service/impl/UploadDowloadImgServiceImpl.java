package com.bdaim.image.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.FileUrlEntity;
import com.bdaim.common.util.PictureRotateUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.image.service.UploadDowloadService;
import com.bdaim.resource.service.impl.MarketResourceServiceImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 上传下载图片service服务 2017/2/21
 *
 */
@Service("uploadDowloadImgService")
@Transactional
public class UploadDowloadImgServiceImpl implements UploadDowloadService {
    private static Log logger = LogFactory.getLog(UploadDowloadImgServiceImpl.class);
    protected static final Properties PROPERTIES = new Properties(System.getProperties());
    @Autowired
    private MarketResourceServiceImpl marketResourceServiceImpl;
    @Autowired
    private FileUrlEntity fileUrlEntity;

    @Override
    public Object uploadImgNew(HttpServletRequest request, HttpServletResponse response, String cust_id,
                               String pictureName) {
        String code = "";
        String message = "";
        // 返回结果编码： 1：成功 0：失败
        // 得到上传文件的保存目录
        // 获取用户名
        String userid = cust_id;
        logger.info("上传图片-->>>" + userid);
        pictureName = CipherUtil.generatePassword(pictureName);
        logger.info("上传图片名称---->>" + pictureName);
        //String savePath = PropertiesUtil.getStringValue("location") + userId;
        String classPath = "/data/upload/";
        String savePath = classPath + userid;

        File filePath = new File(savePath);
        // 判断上传文件的保存目录是否存在
        if (!filePath.exists() && !filePath.isDirectory()) {
            // 创建目录
            filePath.mkdirs();
        }
        String suffix = "";
        String sPath = savePath + "/" + pictureName;
        //sPath = sPath.substring(1,sPath.length());

        // 判断文件是否已经存在
		/*File file = new File(sPath);
		// 判断目录或文件是否存在
		if (file.exists()) { // 存在
			// 删除文件
			file.delete();
		}*/
        try {
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                    request.getSession().getServletContext());
            if (multipartResolver.isMultipart(request)) {
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext()) {
                    MultipartFile file1 = multiRequest.getFile(iter.next());
                    if (file1 != null) {
                        String fileName = file1.getOriginalFilename();
                        suffix = fileName.substring((fileName.indexOf(".") + 1), fileName.length());
                        sPath = sPath + "." + suffix;
                        File localFile = new File(sPath);
                        file1.transferTo(localFile);
                    }
                }
            }
            //处理图片的旋转问题
            File img = new File(sPath);
            int angle = PictureRotateUtil.getAngle(PictureRotateUtil.getExif(sPath));
            InputStream is;
            BufferedImage src;
            is = new FileInputStream(img);
            src = ImageIO.read(is);
            //旋转
            BufferedImage bf = PictureRotateUtil.getBufferedImg(src, sPath, PictureRotateUtil.getWidth(img), PictureRotateUtil.getHeight(img), angle);
            logger.info("上传图片成功！路径：" + sPath + "\t上传图片角度值：" + angle + "\t宽度：" + bf.getWidth() + "\t高度：" + bf.getHeight());
            File file = new File(sPath);
            if (file.exists()) { // 存在
                // 删除文件
                file.delete();
            }
            ImageIO.write(bf, suffix, new File(sPath));

            code = "1";
            message = "上传图片成功";
        } catch (Exception e) {
            code = "0";
            logger.error("上传图片失败！路径：" + sPath, e);
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", code);
        resultMap.put("_message", message);
        resultMap.put("url", pictureName + "." + suffix);
        return JSONObject.toJSON(resultMap);
    }

    @Override
    public Object uploadImg(MultipartFile file) {
        //1. 把图片文件上传到服务器中 /data/upload/0/pictureName.suffix
        String originalFileName = file.getOriginalFilename();
        String suffixName = originalFileName.substring(originalFileName.lastIndexOf("."));
        String randomFileName = System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
        randomFileName = CipherUtil.generatePassword(randomFileName);
        String pathF = PROPERTIES.getProperty("file.separator");
        StringBuffer uploadPathBuffer = new StringBuffer(pathF);
        uploadPathBuffer.append("data").append(pathF).append("upload").append(pathF).append("0").append(pathF).append(randomFileName).append(suffixName);
        logger.info("upload path is" + uploadPathBuffer.toString());
        File file1 = new File(uploadPathBuffer.toString());
        if (!file1.exists()) {
            try {
                file1.getParentFile().mkdirs();
                file1.createNewFile();
                FileUtils.copyInputStreamToFile(file.getInputStream(), file1);
            } catch (Exception e) {
                logger.info("发生异常，异常信息如下");
                logger.info(e.getMessage());
            }
        }
        Map<String, Object> resultMap = new HashMap<>(10);
        resultMap.put("code", "1");
        resultMap.put("_message", "上传图片成功");
        resultMap.put("url", randomFileName + suffixName);
        return JSONObject.toJSON(resultMap);
    }

    /*
     * 下载企业营业执照副本
     */
    @Override
    public Object downloadImgNew(HttpServletRequest request, HttpServletResponse response, String userid, String pictureName) {
        String code = "";
        String message = "";
        // 返回结果编码： 1：成功 0：失败
        logger.info("加载图片---->>" + userid);
        logger.info("加载图片名称---->>" + pictureName);
        //String savePath = path + userId;

        String classPath = "images/";
        String savePath = classPath + userid;

        File picFile = new File(savePath);
        // 判断userid
        // 找到当前用户上传过的营业执照
        File[] tempFile = picFile.listFiles();
        for (int i = 0; i < tempFile.length; i++) {
            if (tempFile[i].getName().startsWith(pictureName)) {
                savePath = savePath + File.separator + tempFile[i].getName();
            }
        }
        logger.info("最终加载图片路径---->>" + savePath);
        picFile = new File(savePath);
        response.setContentType("image/jpeg; charset=GBK");
        ServletOutputStream outputStream;
        try {
            outputStream = response.getOutputStream();
            FileInputStream inputStream = new FileInputStream(savePath);
            byte[] buffer = new byte[1024];
            int i = -1;
            while ((i = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, i);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            outputStream = null;
            code = "1";
            message = "加载营业执照成功";
        } catch (IOException e) {
            logger.error("加载营业执照失败！", e);
            code = "0";
            message = "加载营业执照失败";
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", code);
        resultMap.put("_message", message);
        resultMap.put("data", "");
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 压缩文件
     *
     * @param zipBasePath 临时压缩文件基础路径
     * @param zipName     临时压缩文件名称
     * @param zipFilePath 临时压缩文件完整路径
     * @param filePaths   需要压缩的文件路径集合
     * @throws IOException
     */
    @Override
    public String zipFile(String zipBasePath, String zipName, String zipFilePath, List<String> filePaths, ZipOutputStream zos) throws IOException {
        //循环读取文件路径集合，获取每一个文件的路径
        for (String filePath : filePaths) {
            File inputFile = new File(filePath);
            if (inputFile.exists()) {
                if (inputFile.isFile()) {
                    logger.info("循环读取文件路径集合，获取每一个文件的路径:" + filePath);
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
                    //将文件写入zip内，即将文件进行打包
                    zos.putNextEntry(new ZipEntry(inputFile.getName()));
                    int size = 0;
                    byte[] buffer = new byte[1024];
                    while ((size = bis.read(buffer)) > 0) {
                        zos.write(buffer, 0, size);
                    }
                    zos.closeEntry();
                    bis.close();
                } else { //如果是文件夹，则使用穷举的方法获取文件，写入zip
                    try {
                        File[] files = inputFile.listFiles();
                        List<String> filePathsTem = new ArrayList<>();
                        for (File fileTem : files) {
                            filePathsTem.add(fileTem.toString());
                        }
                        zipFile(zipBasePath, zipName, zipFilePath, filePathsTem, zos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                logger.info("循环文件路径集合，获取的不是一个文件:" + filePath);
            }
        }
        return null;
    }

    /**
     * 压缩文件
     *
     * @param zipBasePath 临时压缩文件基础路径
     * @param zipName     临时压缩文件名称
     * @param zipFilePath 临时压缩文件完整路径
     * @param filePaths   需要压缩的文件路径集合
     * @param batchName
     * @param realName
     * @param userid
     * @throws IOException
     */
    @Override
    public String zipFileUrl(String zipBasePath, String zipName, String zipFilePath, List<String> filePaths, ZipOutputStream zos, String batchName, String realName, String userid) throws IOException {
        //循环读取文件路径集合，获取每一个文件的路径
        for (String filePathUrl : filePaths) {
            logger.info("filePathUrl:" + filePathUrl);
            String fileUrl = filePathUrl.substring(0, filePathUrl.lastIndexOf("#"));
            String creatTime = filePathUrl.substring((filePathUrl.lastIndexOf("#") + 1), filePathUrl.length());

            String tyeStr = fileUrl.substring(fileUrl.lastIndexOf("."), fileUrl.length());
            URL url = new URL(fileUrl);
            String fileName = batchName + "-" + realName + "-" + userid + "-" + creatTime + tyeStr;
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int state = conn.getResponseCode();
            if (state == 200) {
                //设置超时间为3秒
                conn.setConnectTimeout(3 * 1000);
                //防止屏蔽程序抓取而返回403错误
                //conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                //得到输入流
                InputStream inputStream = conn.getInputStream();
                logger.info("循环读取文件路径集合，获取每一个文件的路径:" + fileUrl);
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                //将文件写入zip内，即将文件进行打包
                zos.putNextEntry(new ZipEntry(fileName));
                int size = 0;
                byte[] buffer = new byte[1024];
                while ((size = bis.read(buffer)) > 0) {
                    zos.write(buffer, 0, size);
                }
                zos.closeEntry();
                bis.close();
                inputStream.close();
            } else {
                logger.info("文件集合中的文件路径不可达：" + fileUrl);
            }
        }
        return null;
    }

    public String zipFileUrlV1(List<String> fileNames, ZipOutputStream zos, String batchName, String realName, String userId) throws IOException {
        //循环读取文件路径集合，获取每一个文件的路径
        String tyeStr, fileName, createTime;
        logger.info("zipFileUrlV1：录音文件名" + fileNames);
        String[] names;
        for (String tmpName : fileNames) {
            if (StringUtil.isEmpty(tmpName)) {
                logger.warn("tmpName:" + tmpName + "为空!");
                continue;
            }
            names = tmpName.split("#");
            fileName = names[0];
            createTime = names[1];
            logger.info("fileName:" + fileName + ",createTime:" + createTime);
            tyeStr = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            //得到输入流
            InputStream inputStream = marketResourceServiceImpl.getVoiceFileInputStream(userId, fileName);
            if (inputStream != null) {
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                //将文件写入zip内，即将文件进行打包
                zos.putNextEntry(new ZipEntry(batchName + "-" + realName + "-" + userId + "-" + createTime + tyeStr));
                int size = 0;
                byte[] buffer = new byte[1024];
                while ((size = bis.read(buffer)) > 0) {
                    zos.write(buffer, 0, size);
                }
                zos.closeEntry();
                bis.close();
                inputStream.close();
            }
        }
        return null;
    }

    /**
     * 根据不同浏览器将文件名中的汉字转为UTF8编码的串,以便下载时能正确显示另存的文件名.
     *
     * @param s 原文件名
     * @return 重新编码后的文件名
     */
    @Override
    public String toUtf8String(HttpServletRequest request, String s) {
        String agent = request.getHeader("User-Agent");
        try {
            boolean isFireFox = (agent != null && agent.toLowerCase().indexOf("firefox") != -1);
            if (isFireFox) {
                s = new String(s.getBytes("UTF-8"), "ISO8859-1");
            } else {
                s = StringUtil.toUtf8String(s);
                if ((agent != null && agent.indexOf("MSIE") != -1)) {
                    // see http://support.microsoft.com/default.aspx?kbid=816868
                    if (s.length() > 150) {
                        // 根据request的locale 得出可能的编码
                        s = new String(s.getBytes("UTF-8"), "ISO8859-1");
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }


}
