package com.bdaim.common.util;

import com.bdaim.common.exception.TouchException;
import com.bdaim.customs.dto.FileModel;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @description:
 * @auther: Chacker
 * @date: 2019/8/6 14:34
 */
@Component
public class ZipUtil {

    private static Logger log = LoggerFactory.getLogger(ZipUtil.class);

    public List<String> unZip(File srcFile, String destDirPath) {
        List<String> result = new ArrayList<String>();
        //开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcFile, Charset.forName("GBK"));
            Enumeration<?> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) e.nextElement();
                System.out.println("解压： " + zipEntry.getName());
                //如果是文件夹，则跳过
                if (zipEntry.isDirectory()) {
                    continue;
                } else {
                    //如果是文件，就先创建一个文件，然后用FileUtil把文件内容copy过去
                    String entryName = zipEntry.getName();
                    File targetFile;
                    if (entryName.contains("/")) {
                        targetFile = new File(destDirPath + zipEntry.getName().substring(zipEntry.getName().lastIndexOf("/")));
                    } else {
                        targetFile = new File((destDirPath + entryName));
                    }
                    result.add(zipEntry.getName().substring(entryName.lastIndexOf("/") + 1, entryName.lastIndexOf(".")));
                    if (!targetFile.exists()) {
                        targetFile.getParentFile().mkdirs();
                        targetFile.createNewFile();
                    }
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    FileUtils.copyInputStreamToFile(inputStream, targetFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 解压文件
     *
     * @param file
     * @return
     * @throws TouchException
     */
    public static List<FileModel> unZip(MultipartFile file) throws TouchException {
        // 判断文件是否为zip文件
        String filename = file.getOriginalFilename();
        if (!filename.endsWith("zip")) {
            log.info("传入文件格式不是zip文件" + filename);
            throw new TouchException("传入文件格式错误" + filename);
        }
        List<FileModel> fileModelList = new ArrayList<>();
        String zipFileName = null;
        // 对文件进行解析
        try {
            ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream(), Charset.forName("GBK"));
            BufferedInputStream bs = new BufferedInputStream(zipInputStream);
            ZipEntry zipEntry;
            byte[] bytes = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                zipFileName = zipEntry.getName();
                Assert.notNull(zipFileName, "压缩文件中子文件的名字格式不正确");
                FileModel fileModel = new FileModel();
                fileModel.setFileName(zipFileName);
                bytes = new byte[(int) zipEntry.getSize()];
                bs.read(bytes, 0, (int) zipEntry.getSize());
                InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                fileModel.setFileInputstream(byteArrayInputStream);
                fileModelList.add(fileModel);
            }
        } catch (Exception e) {
            log.error("读取部署包文件内容失败,请确认部署包格式正确:" + zipFileName, e);
            throw new TouchException("读取部署包文件内容失败,请确认部署包格式正确:" + zipFileName);
        }
        return fileModelList;
    }
}
