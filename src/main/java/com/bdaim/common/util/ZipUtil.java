package com.bdaim.common.util;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @description:
 * @auther: Chacker
 * @date: 2019/8/6 14:34
 */
@Component
public class ZipUtil {
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
                    File targetFile = new File(destDirPath + zipEntry.getName().substring(zipEntry.getName().lastIndexOf("/")));
                    result.add(zipEntry.getName().substring(0,zipEntry.getName().lastIndexOf(".")));
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
}
