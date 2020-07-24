package com.bdaim.util;


import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
/**
 * 3DES算法。
 * java的api中仅仅提供了DES,DESede和PBE 3三种对称加密算法密钥材料实现类
 *
 * 使用CBC需要增加加密向量
 * - 第一段是加密算法的名称，如DESede实际上是3-DES。这一段还可以放其它的对称加密算法，如Blowfish等。
 * - 第二段是分组加密的模式，除了CBC和ECB之外，还可以是NONE/CFB/QFB等。最常用的就是CBC和ECB了。DES采用分组加密的方式，将明文按8字节（64位）分组分别加密。如果每个组独立处理，则是ECB。CBC的处理方式是先用初始向量IV对第一组加密，再用第一组的密文作为密钥对第二组加密，然后依次完成整个加密操作。如果明文中有两个分组的内容相同，ECB会得到完全一样的密文，但CBC则不会。
 * - 第三段是指最后一个分组的填充方式。大部分情况下，明文并非刚好64位的倍数。对于最后一个分组，如果长度小于64位，则需要用数据填充至64位。PKCS5Padding是常用的填充方式，如果没有指定，默认的方式就是它。
 * 补充一点，虽然DES的有效密钥长度是56位，但要求密钥长度是64位（8字节）。3DES则要求24字节。
 *
 *
 * CBC使用加密向量
 * ECB不使用加密向量
 *
 * 2020-03-07
 */
public class ThreeDES {

    public static final String ALGORITHM = "DESede";

    /**
     *
     * 加密
     *
     * @param secret_msg     密文
     * @param secret_key     秘钥
     * @param secret_model   加密模式
     * @param secret_vector  加密向量
     * @param secret_encryption_mode  CBC或者ECB
     * @return
     * @throws Exception
     */
    public static String encrypt(String secret_msg,String secret_key ,String secret_model ,String secret_vector,String secret_encryption_mode) throws Exception {
        String  encryptMsg=null;
        if("pkcs5".equals(secret_model.toLowerCase())) {
            //加密
            //加解密信息，秘钥，加密向量，加密模式(CBC或者ECB)
            encryptMsg=encryptDESCBC(secret_msg, secret_key, secret_vector,secret_encryption_mode);
        }
        if("pkcs7".equals(secret_model.toLowerCase())) {
            //加密
            //加解密信息，秘钥,加密向量.加密模式(CBC或者ECB)
            encryptMsg=encryptThreeDESECB(secret_msg, secret_key, secret_vector,secret_encryption_mode);
        }
        return encryptMsg;
    }


    /**
     *
     * 解密
     *
     * @param secret_msg     密文
     * @param secret_key     秘钥
     * @param secret_model   加密模式
     * @param secret_vector  加密向量
     * @param secret_encryption_mode  CBC或者ECB
     * @return
     * @throws Exception
     */
    public static String decrypt(String secret_msg,String secret_key ,String secret_model ,String secret_vector,String secret_encryption_mode) throws Exception {
        String  decryptMsg=null;
        if("pkcs5".equals(secret_model.toLowerCase())) {

            //解密
            //加解密信息，秘钥，加密向量
            decryptMsg=decryptDESCBC(secret_msg, secret_key, secret_vector,secret_encryption_mode);
        }
        if("pkcs7".equals(secret_model.toLowerCase())) {
            //解密
            //加解密信息，秘钥
            decryptMsg=decryptThreeDESECB(secret_msg, secret_key, secret_vector,secret_encryption_mode);
        }
        return decryptMsg;
    }


    /**
     * DESCBC加密
     *
     * @param src     加密信息
     * @param key     秘钥
     * @param encryptionVector  加密向量
     * @param algorithm  CBC或者ECB
     * @return 返回加密后的数据
     * @throws Exception
     */
    public static String encryptDESCBC(final String src, final String key,final String encryptionVector,final String algorithm) throws Exception {

        // --生成key,同时制定是des还是DESede,两者的key长度要求不同
        //final DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));

        SecretKey sKsecretKey = getKey(key);

        final Cipher  cipher = Cipher.getInstance("DESede/"+algorithm+"/PKCS5Padding");

        if("CBC".equals(algorithm.toUpperCase())) {
            // --加密向量
            final IvParameterSpec iv = new IvParameterSpec(encryptionVector.getBytes("UTF-8"));
            cipher.init(Cipher.ENCRYPT_MODE, sKsecretKey, iv);
        }else {
            cipher.init(Cipher.ENCRYPT_MODE, sKsecretKey);
        }

        final byte[] b = cipher.doFinal(src.getBytes("UTF-8"));

        // --通过base64,将加密数组转换成字符串
        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(b);
    }

    /**
     * DESCBC解密
     *
     * @param src     密文信息
     * @param key     秘钥
     * @param encryptionVector  加密向量
     * @param algorithm  CBC或者ECB
     * @return 返回解密后的原始数据
     * @throws Exception
     */
    public static String decryptDESCBC(final String src, final String key,final String encryptionVector,final String algorithm) throws Exception {
        // --通过base64,将字符串转成byte数组
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte[] bytesrc = decoder.decodeBuffer(src);

        SecretKey sKsecretKey = getKey(key);

        final Cipher  cipher = Cipher.getInstance("DESede/"+algorithm+"/PKCS5Padding");

        if("CBC".equals(algorithm.toUpperCase())) {
            // --加密向量
            final IvParameterSpec iv = new IvParameterSpec(encryptionVector.getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, sKsecretKey, iv);
        }else {
            cipher.init(Cipher.DECRYPT_MODE, sKsecretKey);
        }

        final byte[] retByte = cipher.doFinal(bytesrc);

        return new String(retByte);

    }

    /**
     *
     * @param src     密文信息
     * @param key     秘钥
     * @param encryptionVector  加密向量
     * @param algorithm  CBC或者ECB
     * @return
     * @throws Exception
     */
    public static String encryptThreeDESECB(final String src, final String key,final String encryptionVector,final String algorithm ) throws Exception {
        /**
         * 这个地方调用BouncyCastleProvider 让java支持PKCS7Padding
         */
        Security.addProvider(new BouncyCastleProvider());

        SecretKey sKsecretKey = getKey(key);

        final Cipher cipher = Cipher.getInstance("DESede/"+algorithm+"/PKCS7Padding");

        if("CBC".equals(algorithm.toUpperCase())) {
            final IvParameterSpec iv = new IvParameterSpec(encryptionVector.getBytes("UTF-8"));
            cipher.init(Cipher.ENCRYPT_MODE, sKsecretKey,iv);
        }else {
            cipher.init(Cipher.ENCRYPT_MODE, sKsecretKey);
        }

        final byte[] b = cipher.doFinal(src.getBytes());

        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(b).replaceAll("\r", "").replaceAll("\n", "");

    }

    // 3DESECB解密,key必须是长度大于等于 3*8 = 24 位哈 //加解密信息，秘钥,加密向量.加密模式(CBC或者ECB)
    public static String decryptThreeDESECB(final String src, final String key ,final String encryptionVector,final String algorithm) throws Exception {
        /**
         * 这个地方调用BouncyCastleProvider 让java支持PKCS7Padding
         */
        Security.addProvider(new BouncyCastleProvider());
        // --通过base64,将字符串转成byte数组
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte[] bytesrc = decoder.decodeBuffer(src);
        // --解密的key
        SecretKey sKsecretKey = getKey(key);

        // --Chipher对象解密
        final Cipher cipher= Cipher.getInstance("DESede/"+algorithm+"/PKCS7Padding");

        if("CBC".equals(algorithm.toUpperCase())) {
            final IvParameterSpec iv = new IvParameterSpec(encryptionVector.getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, sKsecretKey,iv);
        }else {
            cipher.init(Cipher.DECRYPT_MODE, sKsecretKey);
        }

        final byte[] retByte = cipher.doFinal(bytesrc);

        return new String(retByte);
    }

    /**
     * 使用DESede对secretKey进行加密
     * @param secretKey 密钥
     * @throws Exception
     */
    public static SecretKey getKey(String secretKey) throws Exception {
        byte[] keyBytes = new BASE64Decoder().decodeBuffer(secretKey);
        DESedeKeySpec dks = new DESedeKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey sKsecretKey = keyFactory.generateSecret(dks);
        return sKsecretKey;
    }




}
