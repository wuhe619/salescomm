package com.bdaim.auth.service;

import java.util.Map;

import com.bdaim.auth.entity.UserVerificationCode;

/**
 * @author chengning@salescomm.net
 * @date 2018/8/2
 * @description
 */
public interface UserVerificationCodeService {

    /**
     * 根据条件查询1个用户验证码存储对象
     *
     * @author chengning@salescomm.net
     * @date 2018/8/2 10:02
     * @param map
     * @return com.bdaim.slxf.entity.UserVerificationCode
     */
    UserVerificationCode getUserVerificationCodeByCondition(Map<String, Object> map);

    /**
     * 保存用户验证码存储对象
     *
     * @author chengning@salescomm.net
     * @date 2018/8/2 14:36
     * @param userVerificationCode
     * @return long
     */
    long addUserVerificationCode(UserVerificationCode userVerificationCode);

    /**
     * 修改用户验证码存储对象
     *
     * @author chengning@salescomm.net
     * @date 2018/8/2 14:59
     * @param userVerificationCode
     * @return void
     */
    void updateUserVerificationCode(UserVerificationCode userVerificationCode);
}
