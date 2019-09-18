package com.bdaim.common.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/10
 * @description
 */
@Service
public class MongoFileService {
    @Autowired
    private GridFsTemplate gridfsTemplate;
    @Autowired
    private MongoDbFactory mongoDbFactory;

    /**
     * 根据文件名称查询文件
     */
    public GridFSFile findFileById(String fileName) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where("filename").is(fileName));
        return gridfsTemplate.findOne(query);
    }

    /**
     * 保存文件
     *
     * @param file
     */
    public String saveFile(MultipartFile file, String fileName) throws IOException {
        String filename = file.getOriginalFilename();
        String type = filename.substring(filename.lastIndexOf("."), filename.length());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("_contentType", file.getContentType());
        metadata.put("resFileName", filename);
        metadata.put("resFileType", type);
        ObjectId id = gridfsTemplate.store(file.getInputStream(), fileName, metadata);
        return id.toString();
    }

    public String saveFile(InputStream file, String fileName) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        ObjectId id = gridfsTemplate.store(file, fileName, metadata);
        return id.toString();
    }

    /**
     * 根据文件名称删除文件
     */
    public void deleteFileById(String fileName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("filename").is(fileName));
        gridfsTemplate.delete(query);
    }

    /**
     * 下载文件
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public byte[] downloadFile(String fileName) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where("filename").is(fileName));
        GridFSFile gridFSFile = gridfsTemplate.findOne(query);
        if (gridFSFile == null) {
            return new byte[]{};
        }
        GridFSBucket bucket = GridFSBuckets.create(mongoDbFactory.getDb());
        GridFSDownloadStream in = bucket.openDownloadStream(gridFSFile.getObjectId());
        GridFsResource resource = new GridFsResource(gridFSFile, in);
        InputStream inputStream = resource.getInputStream();
        byte[] f = getBytes(inputStream);
        return f;
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int i = 0;
        while (-1 != (i = inputStream.read(b))) {
            bos.write(b, 0, i);
        }
        return bos.toByteArray();
    }
}
