package com.bdaim.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdaim.image.controller.UploadAction;

public class StringUtil {
    private static Logger logger = LoggerFactory.getLogger(StringUtil.class);
    public static boolean isEmpty(String str) {
        if (str == null || str.equals("") || "null".equals(str)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNotEmpty(String str) {
        if (str == null || str.equals("") ||"null".equals(str)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 找出在str字符串中，toMatch字符串出现的次数
     */
    public static int countMatch(String str, String toMatch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.substring(i, str.length()).indexOf(toMatch) == 0) {
                count++;
            }
        }
        return count;
    }

    // 二进制转字符串
    public static String byte2hex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String tmp = "";
        for (int i = 0; i < b.length; i++) {
            tmp = Integer.toHexString(b[i] & 0XFF);
            if (tmp.length() == 1) {
                sb.append("0" + tmp);
            } else {
                sb.append(tmp);
            }

        }
        return sb.toString();
    }

    // 字符串转二进制
    public static byte[] hex2byte(String str) {
        if (str == null) {
            return null;
        }

        str = str.trim();
        int len = str.length();

        if (len == 0 || len % 2 == 1) {
            return null;
        }

        byte[] b = new byte[len / 2];
        try {
            for (int i = 0; i < str.length(); i += 2) {
                b[i / 2] = (byte) Integer.decode("0X" + str.substring(i, i + 2)).intValue();
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    //object convert to binary
    public static String objectToBinaryString(Object obj) throws IOException {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            NameValuePair onep = new NameValuePair();
            oos.writeObject(obj);
            byte[] abc = baos.toByteArray();
            return Base64.encodeBase64String(abc);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException("one value on error.");
        } finally {
            if (null != oos)
                oos.close();
        }
    }

    public static Object binaryStringToObject(String str) throws IOException {
        ObjectInputStream ois = null;
        try {
            byte[] bin = Base64.decodeBase64(str);
            ByteArrayInputStream bais = new ByteArrayInputStream(bin);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException("one value on error.");
        } finally {
            if (null != ois)
                ois.close();
        }
    }

    public static String toUtf8String(String s) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = Character.toString(c).getBytes("utf-8");
                } catch (Exception ex) {
                    logger.error("将文件名中的汉字转为UTF8编码的串时错误，输入的字符串为：" + s);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        if(isEmpty(str))return false;
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    public static String toCamelCase(String stringWithUnderline) {
        if (stringWithUnderline.indexOf(95) == -1) {
            return stringWithUnderline;
        } else {
            stringWithUnderline = stringWithUnderline.toLowerCase();
            char[] fromArray = stringWithUnderline.toCharArray();
            char[] toArray = new char[fromArray.length];
            int j = 0;

            for(int i = 0; i < fromArray.length; ++i) {
                if (fromArray[i] == '_') {
                    ++i;
                    if (i < fromArray.length) {
                        toArray[j++] = Character.toUpperCase(fromArray[i]);
                    }
                } else {
                    toArray[j++] = fromArray[i];
                }
            }

            return new String(toArray, 0, j);
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> lst = new ArrayList<String>();
        for (int i = 0; i < 1000; i++)
            lst.add(String.valueOf(i));
        String st = StringUtil.objectToBinaryString(lst);
        List aaa = (List) StringUtil.binaryStringToObject(st);
        System.out.println(aaa);

        String str2 = StringUtil.objectToBinaryString("123abcdfg");
        String str3 = (String) StringUtil.binaryStringToObject(str2);
        System.out.println(str3);

    }

}
