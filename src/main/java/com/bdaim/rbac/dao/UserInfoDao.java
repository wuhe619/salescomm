package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.entity.UserDO;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Component
public class UserInfoDao extends SimpleHibernateDao<UserDO,Serializable>{
}
