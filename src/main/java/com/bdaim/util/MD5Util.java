package com.bdaim.util;

import java.security.MessageDigest;

public class MD5Util {
	public static String encode32Bit(String str) {
		try {
			MessageDigest md=MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] b=md.digest();
			
			int i;
			StringBuffer buf=new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i=b[offset];
				if (i<0) {
					i+=256;
				}
				if (i<16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
		} catch (Exception e) {
			LogUtil.error("MD5加密失败!str:"+str);
		}
		return null;
	}
	
	public static String encode16Bit(String str) {
		return encode32Bit(str).substring(8,24);
	}
	
	public static void main(String[] args) {
		System.out.println(MD5Util.encode32Bit("label_all"));
	}
}
