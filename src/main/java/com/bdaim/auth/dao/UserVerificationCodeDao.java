package com.bdaim.auth.dao;

import com.bdaim.auth.entity.UserVerificationCode;
import com.bdaim.common.dao.SimpleHibernateDao;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 用户手机验证码记录Dao
 *
 * @author chengning@salescomm.net
 * @date 2018/8/2
 * @description
 */
@Component
public class UserVerificationCodeDao extends SimpleHibernateDao<UserVerificationCode, Serializable> {

}
