
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * HMACSHA1加密算法
 * HMACSHA1加密算法
 */
public class HMACSHA1
{

    private static final String MAC_NAME = "HmacSHA1";

    private static final String ENCODING = "utf-8";

    /**
     * 全局数组
     */
    private final static String[] HEXDIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f"};


    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    public static String hmacSHA1Encrypt(String encryptText, String encryptKey)
    {
        try
        {
//            byte[] data = encryptKey.getBytes(ENCODING);
            BASE64Decoder de = new BASE64Decoder();
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKey secretKey = new SecretKeySpec(de.decodeBuffer(encryptKey), MAC_NAME);
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(MAC_NAME);
            //用给定密钥初始化 Mac 对象
            mac.init(secretKey);
            //完成 Mac 操作
            byte[] text = encryptText.getBytes(ENCODING);
            byte[] tmp = mac.doFinal(text);
            //String result = new BASE64Encoder().encode(mac.doFinal(text));
            BASE64Encoder en = new BASE64Encoder();
            //String result = byteArrayToHexString(tmp);
            String result = en.encode(tmp); //result.getBytes(ENCODING)
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     * @param b 字节数组
     * @return 字符串
     */
    private static String byteToHexString(byte b)
    {
        int ret = b;
        if (ret < 0)
        {
            ret += 256;
        }
        int m = ret / 16;
        int n = ret % 16;
        return HEXDIGITS[m] + HEXDIGITS[n];
    }

    /**
     * 转换字节数组为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] bytes)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++)
        {
            sb.append(byteToHexString(bytes[i]));
        }
        return sb.toString();
    }

}
