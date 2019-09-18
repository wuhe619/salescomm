package com.bdaim.customs.dto;

import java.io.InputStream;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/18
 * @description
 */
public class FileModel {
    private static final long serialVersionUID = 13846812783412684L;
    String fileName;
    String fileType;
    InputStream fileInputstream;

    public FileModel() {
    }

    public FileModel(String fileName, String fileType, InputStream fileInputstream) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileInputstream = fileInputstream;
    }

    public String getFileName() {
        return this.fileName;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getFileType() {
        return this.fileType;
    }


    public void setFileType(String fileType) {
        this.fileType = fileType;
    }


    public InputStream getFileInputstream() {
        return this.fileInputstream;
    }


    public void setFileInputstream(InputStream fileInputstream) {
        this.fileInputstream = fileInputstream;
    }


    public String toString() {
        return "FileModel{fileName=\'" + this.fileName + '\'' + ", fileType=\'" + this.fileType + '\''
                + ", fileInputstream=" + this.fileInputstream + '}';
    }
}
