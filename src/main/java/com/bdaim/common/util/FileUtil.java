package com.bdaim.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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

}
