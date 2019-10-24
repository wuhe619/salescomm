package com.bdaim.common.util.ftp;


import com.bdaim.util.PropertiesUtil;

public class SFTPConstants {
	public static final String SFTP_REQ_HOST = PropertiesUtil.getStringValue("host");
	public static final String SFTP_REQ_PORT = PropertiesUtil.getStringValue("port");
	public static final String SFTP_REQ_USERNAME = PropertiesUtil.getStringValue("username");
	public static final String SFTP_REQ_PASSWORD = PropertiesUtil.getStringValue("password");
	public static final int SFTP_DEFAULT_PORT = 22;
	public static final String SFTP_REQ_LOC = PropertiesUtil.getStringValue("location");
}
