package com.bdaim.common.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/14
 * @description
 */
public class FileUtil {
    private static Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    private final static String AUDIO_FILE_PATH = "/data/audio/";

    /**
     * 根据url下载文件到指定目录
     *
     * @param httpUrl
     * @param filePath
     * @return boolean
     * @author chengning@salescomm.net
     * @date 2018/9/14 10:13
     */
    public static boolean saveFileByUrl(String httpUrl, String filePath) {
        //此方法只能用户HTTP协议
        DataInputStream in = null;
        DataOutputStream out = null;
        HttpURLConnection connection;
        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(new FileOutputStream(filePath));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 根据url获取文件进行保存,并且返回文件路径
     *
     * @param recordUrl
     * @param userId
     * @return
     * @throws RuntimeException
     */
    public static String savePhoneRecordFileReturnPath(String recordUrl, String userId) throws RuntimeException {
        LOG.info("保存通话记录录音文件参数:" + "recordUrl" + recordUrl + "userId" + userId);
        if (StringUtil.isNotEmpty(recordUrl) && !"NoTapes".equals(recordUrl)) {
            if (StringUtil.isNotEmpty(recordUrl) && StringUtil.isNotEmpty(userId)) {
                String fileName = recordUrl.substring(recordUrl.lastIndexOf("/"));
                String filePath = AUDIO_FILE_PATH + userId;
                LOG.info("保存通话记录录音文件路径:" + "filePath" + filePath);
                File file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                boolean flag = FileUtil.saveFileByUrl(recordUrl, filePath + File.separator + fileName);
                LOG.info("保存通话记录录音文件:" + flag);
                return filePath + File.separator + fileName;
            }
        }
        return "";
    }

    /**
     * wav转换成mp3
     */
    public static void wavToMp3(String inPath, String outFile) {
        // ffmpeg -i a.wav a.mp3
        try {
            Runtime.getRuntime().exec("ffmpeg -i " + inPath + " " + outFile);
        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public final static Set<String> EXCEL_FILE_TYPES = new HashSet() {{
        add(".xls");
        add(".xlsx");
    }};

    /**
     * 文件复制
     *
     * @param source
     * @param dest
     * @throws IOException
     */
    public static void copyFile(File source, File dest)
            throws IOException {
        FileUtils.copyFile(source, dest);
    }


    /**
     * 创建文件
     *
     * @param filePath
     * @param fileName
     */
    public static void createFile(String filePath, String fileName) {
        File folder = new File(filePath);
        //文件夹路径不存在
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        } else {
            LOG.warn("文件夹路径存在:" + filePath);
        }

        // 如果文件不存在就创建
        File file = new File(filePath + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LOG.error("文件创建失败:", e);
            }
        } else {
            LOG.warn("文件已存在,文件为:" + filePath + fileName);
        }
    }

    public static void writeFile(String filePath, String fileName, String content) throws Exception {
        File f = new File(filePath);
        if (!f.exists()) {
            LOG.warn(filePath + " 文件夹不存在,创建文件");
            f.mkdirs();
        } else {
            LOG.warn("文件夹路径存在:" + filePath);
        }
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            fos = new FileOutputStream(filePath + File.separator + fileName);
            osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(content);
            osw.flush();
        } catch (IOException e) {
            LOG.error("文件创建失败:", e);
            throw e;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                LOG.error("文件创建失败:", e);
                throw e;
            }
        }
    }

    /**
     * 写入文件到HttpServletResponse
     *
     * @param content
     * @param response
     * @throws IOException
     */
    public static void writeFileToResponse(String content, String fileName, HttpServletResponse response) throws IOException {
        if (StringUtil.isEmpty(content)) {
            return;
        }
        response.setHeader("content-disposition", "attachment;filename=" + fileName);
        response.getOutputStream().write(content.getBytes("UTF-8"));
    }

}
