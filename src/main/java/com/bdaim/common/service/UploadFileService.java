package com.bdaim.common.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.BusinessEnum;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class UploadFileService {
    private static Logger logger = Logger.getLogger(UploadFileService.class);

    @Value("${file.file_path}")
    private String filePath;

    /**
     * 上传文件
     * @param file
     * @param businessEnum 业务平台编码
     * @param subPath 自定义子目录
     * @return
     */
    public JSONObject uploadFile(MultipartFile file, BusinessEnum businessEnum,String subPath){
        JSONObject result = new JSONObject();
        if(file.isEmpty()){
            logger.info("文件内容为空");
            result.put("code","0");
            result.put("msg","文件内容为空");
            return result;
        }
        String fileName = file.getOriginalFilename();
        if(fileName.indexOf(".") == -1){
            result.put("code","0");
            result.put("msg","文件错误");
            return result;
        }
        if(subPath==null)subPath = "";
        try {
            String fileType = fileName.substring(fileName.lastIndexOf(".")+1);
            String subFilePath = buildSubFilePath(businessEnum,fileName);
            String _filePath = File.separator + businessEnum.getKey() + subPath + subFilePath;
            String savePath = filePath + _filePath;
            File destfile = new File(savePath + fileType);

            file.transferTo(destfile);
            result.put("code","1");
            result.put("msg","SUCCESS");
            result.put("data",_filePath+fileType);
        } catch (IOException e) {
            e.printStackTrace();
            result.put("code","0");
            result.put("msg","文件上传失败");
            return result;
        }
        return result;
    }


    private String buildSubFilePath(BusinessEnum businessEnum,String fileName){
        try {
            return sha1(businessEnum.getKey()+fileName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String sha1(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(data.getBytes());
        StringBuffer buf = new StringBuffer();
        byte[] bits = md.digest();
        for(int i=0;i<bits.length;i++){
            int a = bits[i];
            if(a<0) a+=256;
            if(a<16) buf.append("0");
            buf.append(Integer.toHexString(a));
        }
        return buf.toString();
    }

}
