package com.bdaim.smscenter.util;

import com.alibaba.fastjson.JSON;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;


public class AES128CBC {

	private static final String AESType = "AES/CBC/NoPadding";
	private static final String CharType = "UTF-8";
	private static final String SecretType = "AES";

	public static Cipher getCipher(String key, String iv, int opmode) throws Exception {
		if (key == null || key.length() != 16) {
			throw new InvalidKeyException("key is wrong");
		}
		if (iv == null || iv.length() != 16) {
			throw new InvalidAlgorithmParameterException("iv  is wrong");
		}
		SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(CharType), SecretType);
		Cipher cipher = Cipher.getInstance(AESType);
		IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(CharType));
		cipher.init(opmode, sKeySpec, ivSpec);
		return cipher;
	}

	public static byte[] getSrcByte(String src, int blockSize) throws Exception {
		byte[] dataBytes = src.getBytes(CharType);
		int plaintextLength = dataBytes.length;
		if (plaintextLength % blockSize != 0) {
			plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
		}
		byte[] plaintext = new byte[plaintextLength];
		System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
		return plaintext;
	}

	public static String encrypt(String src, String key, String iv) throws Exception {
		Cipher cipher = getCipher(key, iv, Cipher.ENCRYPT_MODE);
		// return
		// Base64.encodeBase64String(cipher.doFinal(src.getBytes(CharType)));
		return TypeConversion.bytes2HexString(cipher.doFinal(getSrcByte(src, cipher.getBlockSize())));
	}

	public static String decrypt(String src, String key, String iv) throws Exception {
		Cipher cipher = getCipher(key, iv, Cipher.DECRYPT_MODE);
		// return new
		// String(cipher.doFinal(Base64.decodeBase64(src.getBytes(CharType))));
		return new String(cipher.doFinal(TypeConversion.hexString2Bytes(src)));
	}

	public static void main(String[] args) throws Exception {
		String key = "35150511B865466C";
		String iv = "B16FCC9B7C477F78";
		String str = "{\"areaId\":\"\", \"mobile\":\"+8618066799206\", \"content\":\"用jijj 232!@@ ¥, FMDDDEVMTDSL, Liyan, http://loccf%43op/  ,  \", \"msgId\":\"1471322916\", \"cbUrl\":\"\"}";
		String enStr = AES128CBC.encrypt(str, key, iv);
		System.out.println(enStr);
		// enStr =
		// "1596d4ce76a09e8a33ed40d426744be6603c935928096a13f9b6bf353f970d570756f6be35737066905b1dd2ef7f7d7d84d9d974a0bf9d67b5e47f0011a9612a4a4ecef7d0fe1ebd357d853220e1cf2eb2c3e92db88b1b672999228c52a54d3fdcd59ec2201cf6355b2158733030ea006c43d95bd4fd501aea23ad6edbf7c83632583d5ff620e24871bf99018134cb95ec2b3a32b78ce4a8172e9b5f136bf6a2";
		String s = AES128CBC.decrypt(enStr, key, iv);
		System.out.println(s);
		SmsSubmit smsSubmit = JSON.parseObject(s, SmsSubmit.class);
		System.out.println(smsSubmit.getContent());
	}
}
