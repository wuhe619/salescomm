package com.bdaim.util.ftp;


import com.bdaim.AppConfig;

public class SFTPConstants {
	public static final String SFTP_REQ_HOST = AppConfig.getHost();
	public static final String SFTP_REQ_PORT = AppConfig.getPort();
	public static final String SFTP_REQ_USERNAME = AppConfig.getUsername();
	public static final String SFTP_REQ_PASSWORD = AppConfig.getPassword();
	public static final int SFTP_DEFAULT_PORT = 22;
	public static final String SFTP_REQ_LOC = AppConfig.getLocation();
}
