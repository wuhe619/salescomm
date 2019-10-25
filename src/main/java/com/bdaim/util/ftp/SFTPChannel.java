package com.bdaim.util.ftp;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SFTPChannel {
	Session session = null;

	Channel channel = null;

	public static final Logger LOG = LoggerFactory.getLogger(SFTPChannel.class);

	public ChannelSftp getChannel(String dpath, int timeout) throws JSchException {
	

		String ftpHost = SFTPConstants.SFTP_REQ_HOST;

		String port = SFTPConstants.SFTP_REQ_PORT;

		String ftpUserName = SFTPConstants.SFTP_REQ_USERNAME;

		String ftpPassword = SFTPConstants.SFTP_REQ_PASSWORD;

		int ftpPort = SFTPConstants.SFTP_DEFAULT_PORT;

		if (port != null && !port.equals("")) {

			ftpPort = Integer.valueOf(port);

		}

		JSch jsch = new JSch(); // 创建JSch对象

		session = jsch.getSession(ftpUserName, ftpHost, ftpPort); // 根据用户名，主机ip，端口获取一个Session对象

		LOG.debug("Session created.");

		if (ftpPassword != null) {

			session.setPassword(ftpPassword); // 设置密码

		}

		Properties config = new Properties();

		config.put("StrictHostKeyChecking", "no");

		session.setConfig(config); // 为Session对象设置properties

		session.setTimeout(timeout); // 设置timeout时间

		session.connect(); // 通过Session建立链接

		LOG.debug("Session connected.");

		LOG.debug("Opening Channel.");

		channel = session.openChannel("sftp"); // 打开SFTP通道

		channel.connect(); // 建立SFTP通道的连接

		LOG.debug("Connected successfully to ftpHost = " + ftpHost + ",as ftpUserName = " + ftpUserName

		+ ", returning: " + channel);
		createDir(dpath, (ChannelSftp)channel);

		return (ChannelSftp) channel;

	}
    /** 
     * 判断目录是否存在 
     */  
    public boolean isDirExist(String directory,ChannelSftp sftp) {  
     boolean isDirExistFlag = false;  
     try {  
      SftpATTRS sftpATTRS = sftp.lstat(directory);
      isDirExistFlag = true;  
      return sftpATTRS.isDir();  
     } catch (Exception e) {  
      if (e.getMessage().toLowerCase().equals("no such file")) {  
       isDirExistFlag = false;  
      }  
     }  
     return isDirExistFlag;  
    }  
    /** 
     * 创建一个文件目录 
     */  
    public void createDir(String createpath, ChannelSftp sftp) {  
     try {  
      if (isDirExist(createpath,sftp)) {  
           sftp.cd(createpath);  
      }  
      String pathArry[] = createpath.split("/");  
      StringBuffer filePath = new StringBuffer("/");  
      for (String path : pathArry) {  
       if (path.equals("")) {  
        continue;  
       }  
       filePath.append(path + "/");  
       if (isDirExist(filePath.toString(), sftp)) {  
        sftp.cd(filePath.toString());  
       } else {  
        // 建立目录  
        sftp.mkdir(filePath.toString());  
        // 进入并设置为当前目录  
        sftp.cd(filePath.toString());  
       }  
      }  
      sftp.cd(createpath);  
     } catch (SftpException e) {
    	 LOG.debug("创建路径错误：" + createpath);  
     }  
    }  
	public void closeChannel() throws Exception {

		if (channel != null) {

			channel.disconnect();

		}

		if (session != null) {

			session.disconnect();

		}

	}
}
