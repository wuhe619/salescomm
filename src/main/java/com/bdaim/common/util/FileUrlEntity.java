package com.bdaim.common.util;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * @date 2019/8/7
 * @description   读取配置文件实体类
 */
@Component
public class FileUrlEntity {
    @Value("${file.file_path}")
    private String fileUrl;

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public String toString() {
        return "FileUrlEntity{" +
                "fileUrl='" + fileUrl + '\'' +
                '}';
    }
}
