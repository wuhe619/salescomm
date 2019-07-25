package com.bdaim.auth.service.impl;

import com.bdaim.auth.dao.UserVerificationCodeDao;
import com.bdaim.auth.entity.UserVerificationCode;
import com.bdaim.auth.service.UserVerificationCodeService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/8/2
 * @description
 */
@Service("UserVerificationCodeService")
@Transactional
public class UserVerificationCodeServiceImpl implements UserVerificationCodeService {

    @Resource
    private UserVerificationCodeDao userVerificationCodeDao;

    @Override
    public long addUserVerificationCode(UserVerificationCode userVerificationCode) {
        return (Long) userVerificationCodeDao.saveReturnPk(userVerificationCode);
    }

    @Override
    public void updateUserVerificationCode(UserVerificationCode userVerificationCode) {
        userVerificationCodeDao.update(userVerificationCode);
    }

    @Override
    public UserVerificationCode getUserVerificationCodeByCondition(Map<String, Object> map) {
        List<Object> values = new ArrayList<>();
        String hql = "From UserVerificationCode t WHERE 1=1 ";
        for (Map.Entry<String, Object> param : map.entrySet()) {
            hql += "AND " + param.getKey() + " = ? ";
            values.add(param.getValue());
        }
        return userVerificationCodeDao.findUnique(hql, values.toArray(new Object[values.size()]));
    }
}
