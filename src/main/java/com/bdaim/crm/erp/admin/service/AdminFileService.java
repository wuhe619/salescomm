package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.bdaim.AppConfig;
import com.bdaim.common.service.UploadFileService;
import com.bdaim.crm.dao.LkCrmAdminFileDao;
import com.bdaim.crm.dto.LkCrmAdminFileDTO;
import com.bdaim.crm.entity.LkCrmAdminFileEntity;
import com.bdaim.crm.erp.admin.entity.AdminFile;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.BusinessEnum;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.config.Constants;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminFileService {

    private final static Logger LOG = LoggerFactory.getLogger(AdminFileService.class);

    @Resource
    private LkCrmAdminFileDao crmAdminFileDao;
    @Resource
    private UploadFileService uploadFileService;


    //public final static Prop prop = PropKit.use("config/crm9-config.txt");

    /**
     * @param file    文件
     * @param batchId 批次ID
     */
    @Deprecated
    public R upload(UploadFile file, String batchId, String fileType, String prefix) {
        if (batchId == null || "".equals(batchId)) {
            batchId = IdUtil.simpleUUID();
        }
        AdminFile adminFile = new AdminFile();
        adminFile.setBatchId(batchId);
        adminFile.setCreateTime(new Date());
        adminFile.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        adminFile.setPath(file.getFile().getAbsolutePath());
        if (ClassLoaderUtil.isPresent("com.jfinal.server.undertow.UndertowServer")) {
            adminFile.setFilePath(BaseUtil.getIpAddress() + prefix + "/" + file.getFileName());
        } else {
            adminFile.setFilePath(BaseUtil.getIpAddress() + new Constants().getBaseUploadPath() + "/" + prefix + "/" + file.getFileName());
        }
        adminFile.setName(file.getFileName());
        if (StrUtil.isNotBlank(fileType)) {
            adminFile.setFileType(fileType);
        }
        adminFile.setSize(file.getFile().length());
        return adminFile.save() ? R.ok().put("batchId", batchId).put("name", file.getFileName()).put("url", adminFile.getFilePath()).put("size", file.getFile().length() / 1000 + "KB").put("file_id", adminFile.getFileId()) : R.error();
    }

    public R upload0(HttpServletRequest request, String batchId, String fileType, String prefix) {
        if (batchId == null || "".equals(batchId)) {
            batchId = IdUtil.simpleUUID();
        }
        LkCrmAdminFileEntity adminFile = new LkCrmAdminFileEntity();
        adminFile.setBatchId(batchId);
        adminFile.setCreateTime(DateUtil.date().toTimestamp());
        adminFile.setCreateUserId(BaseUtil.getUser().getUserId());
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        String fileName = "", type = "";
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                MultipartFile multiRequestFile = multiRequest.getFile(iter.next());
                if (multiRequestFile != null) {
                    fileName = uploadFileService.uploadFile(multiRequestFile, BusinessEnum.CRM, true);
                    adminFile.setSize(multiRequestFile.getSize());
                    LOG.info("原始文件名1:{}", multiRequestFile.getName());
                    LOG.info("原始文件名2:{}", multiRequestFile.getOriginalFilename());
                    LOG.info("getContentType:{}", multiRequestFile.getContentType());
                    // 获取文件的扩展名
                    type = FilenameUtils.getExtension(multiRequestFile.getOriginalFilename());
                    adminFile.setName(multiRequestFile.getOriginalFilename());
                    break;
                }
            }
        }


        adminFile.setPath(AppConfig.getFile_path() + fileName);
        adminFile.setFilePath(fileName);
        if (StrUtil.isNotBlank(fileType)) {
            adminFile.setFileType(fileType);
        }
        adminFile.setCreateUserId(BaseUtil.getUser().getUserId());
        return (int) crmAdminFileDao.saveReturnPk(adminFile) > 0 ? R.ok().put("batchId", batchId).put("name", adminFile.getName()).put("url", adminFile.getFilePath()).put("size", adminFile.getSize() / 1000 + "KB").put("file_id", adminFile.getFileId()).put("fileId", adminFile.getFileId()) : R.error();
    }

    /**
     * 通过批次ID查询
     *
     * @param batchId 批次ID
     */
    public void queryByBatchId(String batchId, Record record) {
        if (batchId == null || "".equals(batchId)) {
            record.set("img", new ArrayList<>()).set("file", new ArrayList<>());
            return;
        }
        //List<AdminFile> adminFiles=AdminFile.dao.find(Db.getSql("admin.file.queryByBatchId"), batchId);
        List<LkCrmAdminFileDTO> adminFiles = crmAdminFileDao.queryByBatchId(batchId);
        Map<String, List<LkCrmAdminFileDTO>> collect = adminFiles.stream().collect(Collectors.groupingBy(LkCrmAdminFileDTO::getFileType));
        collect.forEach(record::set);
        if (!record.getColumns().containsKey("img") || record.get("img") == null) {
            record.set("img", new ArrayList<>());
        }
        if (!record.getColumns().containsKey("file") || record.get("file") == null) {
            record.set("file", new ArrayList<>());
        }

    }

    public List<LkCrmAdminFileDTO> queryByBatchId(String batchId) {
        if (batchId == null) {
            return new ArrayList<>();
        }
        return crmAdminFileDao.queryByBatchId(batchId);
    }

    /**
     * 通过ID查询
     *
     * @param id 文件ID
     */
    public R queryById(String id) {
        if (id == null) {
            return R.error("id参数为空");
        }
        return R.ok().put("data", crmAdminFileDao.get(NumberConvertUtil.parseInt(id)));
    }

    /**
     * 通过ID删除
     *
     * @param id 文件ID
     */
    @SuppressWarnings("all")
    public R removeById(String id) {
        if (id == null) {
            return R.error("id参数为空");
        }
        LkCrmAdminFileEntity adminFile = crmAdminFileDao.get(NumberConvertUtil.parseInt(id));
        if (adminFile != null) {
            crmAdminFileDao.delete(adminFile);
            /*File file = new File(adminFile.getPath());
            if (file.exists() && !file.isDirectory()) {
                file.delete();
            }*/
        }
        return R.ok();
    }

    /**
     * 通过批次ID删除
     *
     * @param batchId 批次ID
     */
    public void removeByBatchId(String batchId) {
        if (StrUtil.isEmpty(batchId)) {
            return;
        }
        List<String> paths = crmAdminFileDao.queryPathByBatchId(batchId);
        paths.stream().map(File::new).filter(file -> file.exists() && !file.isDirectory()).forEach(File::delete);
        crmAdminFileDao.executeUpdateSQL("delete from lkcrm_admin_file  WHERE batch_id = ?", batchId);
        //Db.deleteById("lkcrm_admin_file", "batch_id", batchId);
    }

    public boolean renameFileById(LkCrmAdminFileEntity file) {
        LkCrmAdminFileEntity adminFile = crmAdminFileDao.get(file.getFileId());
        BeanUtils.copyProperties(file, adminFile, JavaBeanUtil.getNullPropertyNames(file));
        crmAdminFileDao.update(adminFile);
        return true;
    }
}
