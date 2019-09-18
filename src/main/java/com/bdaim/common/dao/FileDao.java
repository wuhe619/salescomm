package com.bdaim.common.dao;

import com.bdaim.common.entity.FileInfo;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/18
 * @description
 */
@Component
public class FileDao extends SimpleHibernateDao<FileInfo, Integer> {

    public FileInfo selectByServiceId(String objectId) {
        //String hql = "FROM FileInfo WHERE serviceId = ?";
        FileInfo fileInfo = this.findUniqueBy("objectId", objectId);
        return fileInfo;
    }

    public void save(String serviceId, String objectId) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
        fileInfo.setObjectId(objectId);
        fileInfo.setServiceId(serviceId);
        this.save(fileInfo);
    }
}
