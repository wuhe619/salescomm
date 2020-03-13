package com.bdaim.crm.erp.admin.service;

import com.bdaim.common.service.UploadFileService;
import com.bdaim.crm.dao.LkCrmAdminConfigDao;
import com.bdaim.crm.entity.LkCrmAdminConfigEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class AdminConfigService {

    private final static Logger LOG = LoggerFactory.getLogger(AdminConfigService.class);

    @Resource
    private LkCrmAdminConfigDao crmAdminConfigDao;
    @Resource
    private UploadFileService uploadFileService;


    public R saveOrUpdate(LkCrmAdminConfigEntity entity) {
        entity.setCustId(BaseUtil.getCustId());
        if (entity.getSettingId() == null && StringUtil.isNotEmpty(entity.getName())) {
            LkCrmAdminConfigEntity unique = crmAdminConfigDao.findUnique(" FROM LkCrmAdminConfigEntity WHERE cust_id = ? AND name = ? ", BaseUtil.getCustId(), entity.getName());
            if (unique != null) {
                entity.setSettingId(unique.getSettingId());
            }
        }
        if (entity.getSettingId() == null) {
            entity.setIsSystem(2);
            entity.setStatus(1);
            int id = (int) crmAdminConfigDao.saveReturnPk(entity);
            return id > 0 ? R.ok() : R.error();
        } else {
            LkCrmAdminConfigEntity dbEntity = crmAdminConfigDao.get(entity.getSettingId());
            BeanUtils.copyProperties(entity, dbEntity, JavaBeanUtil.getNullPropertyNames(entity));
            crmAdminConfigDao.update(dbEntity);
            return R.ok();
        }
    }

    public R queryByName(String name) {
        if (StringUtil.isEmpty(name)) {
            return R.error("name必填");
        }
        List<LkCrmAdminConfigEntity> list = crmAdminConfigDao.find(" FROM LkCrmAdminConfigEntity WHERE cust_id = ? AND name = ? ", BaseUtil.getCustId(), name);
        return R.ok().put("data", list != null && list.size() > 0 ? list.get(0) : null);
    }
}
