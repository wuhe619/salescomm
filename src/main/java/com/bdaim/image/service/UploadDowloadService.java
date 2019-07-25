package com.bdaim.image.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

public interface UploadDowloadService {
	/*
	 * 新的上传企业营业执照副本
	 */
	Object uploadImgNew(HttpServletRequest request, HttpServletResponse response, String cust_id,
                               String pictureName);
	/*
	 * 新的上传企业营业执照副本
	 */
	Object downloadImgNew(HttpServletRequest request,
							  HttpServletResponse response, String userId, String picName);

	String zipFile(String zipBasePath, String zipName, String zipFilePath, List<String> filePaths, ZipOutputStream zos) throws IOException;

	String zipFileUrl(String zipBasePath, String zipName, String zipFilePath, List<String> filePaths, ZipOutputStream zos, String batchName, String realName, String userid) throws IOException;

	String toUtf8String(HttpServletRequest request, String fileName);
}
