package com.bdaim.common.dao;

import com.bdaim.common.entity.HFile;
import com.bdaim.util.BusinessEnum;
import com.bdaim.util.IDHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/18
 * @description
 */
@Component
public class FileDao extends SimpleHibernateDao<HFile, Integer> {

    private static Logger logger = LoggerFactory.getLogger(FileDao.class);

    public HFile selectByServiceId(String fileId) {
        //String hql = "FROM FileInfo WHERE serviceId = ?";
        HFile fileInfo = this.findUniqueBy("fileId", fileId);
        return fileInfo;
    }

    public void save(String serviceId, String objectId, BusinessEnum businessEnum, String fileName, String fileType) {
        HFile fileInfo = new HFile();
        fileInfo.setId(IDHelper.getID());
        fileInfo.setCreateDate(new Timestamp(System.currentTimeMillis()));
        fileInfo.setFileId(objectId);
        fileInfo.setType(businessEnum.getKey());
        fileInfo.setFileName(fileName);
        fileInfo.setExt1(serviceId);
        fileInfo.setFileType(fileType);
        logger.info("文件表插入数据:{}", fileInfo);
        this.save(fileInfo);
    }
}
