package com.bdaim.common.dao;

import com.bdaim.common.entity.HFile;
import com.bdaim.common.util.BusinessEnum;
import com.bdaim.common.util.IDHelper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/18
 * @description
 */
@Component
public class FileDao extends SimpleHibernateDao<HFile, Integer> {

    public HFile selectByServiceId(String fileId) {
        //String hql = "FROM FileInfo WHERE serviceId = ?";
        HFile fileInfo = this.findUniqueBy("fileId", fileId);
        return fileInfo;
    }

    public void save(String serviceId, String objectId, BusinessEnum businessEnum,String fileName) {
        HFile fileInfo = new HFile();
        fileInfo.setId(IDHelper.getID());
        fileInfo.setCreateDate(new Timestamp(System.currentTimeMillis()));
        fileInfo.setFileId(objectId);
        fileInfo.setType(businessEnum.getKey());
        fileInfo.setFileName(fileName);
        fileInfo.setExt1(serviceId);
        this.save(fileInfo);
    }
}
