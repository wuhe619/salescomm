package com.bdaim.common.service;

import com.bdaim.common.entity.DicProperty;
import com.bdaim.supplier.dao.DicDao;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;


/**
 */
@Service("dicService")
@Transactional
public class DicService {
    private static Logger logger = Logger.getLogger(DicService.class);

    @Resource
    private DicDao dicDao;

    public List<DicProperty> getDicProperty(Long id) throws Exception {
        List<DicProperty> propertyList = dicDao.getPropertyList(id);
        return propertyList;
    }
}