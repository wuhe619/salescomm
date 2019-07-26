package com.bdaim.rbac.service;

import com.bdaim.common.exception.TouchException;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.entity.UserDO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
public interface UserInfoService {

    public UserDO getUserByUserName(String name) ;

    public UserDO getUserById(Long uid);

    public Map<String,Object> getUsersByCondition(UserQueryParam param);

    Map<String,Object> getUserByCondition(String type, String condition) throws TouchException;

    void resetPwd(int type, String condition, String password, int pwdLevel) throws Exception;

    void updatePwd(long uid, String oldPwd, String newPwd, int pwdLevel) throws Exception;
    void updateFrontPwd(long uid, String oldPwd, String newPwd, int pwdLevel) throws Exception;

    public List<Map<String, Object>> getSecurityCenterInfo(long id) throws Exception;

    public void verifyIdentifyValueUniqueness(int type,String value) throws Exception;

    void updateRegistInfo(String oldValue, String newValue)throws Exception;
    void updateFrontRegistInfo(String userId, String oldValue, String newValue)throws Exception;


    public void changeUserStatus(String userId,String action)throws Exception;

    public Map<String,Object> queryIndustryPoolByCondition(HttpServletRequest param)throws Exception;

    public Map<String,Object> listIndustryPoolByCustomerId(HttpServletRequest param) throws Exception;

    public Map<String,Object> showCustIndustryPoolStatus(HttpServletRequest param) throws Exception;
    
    public UserDO getUserByName(String name) ;
    public UserDO getUserBymobileNum(String mobileNum) ;
    public UserDO getUserByEmail(String email) ;
    public UserDO getUserByEnterpriseName(String enterpriseName) ;
    public Integer getUserByLoginPassWord(String loginPassWord,Long userId) ;

    public List<String> getAuthsByUserName(String userName)throws Exception;
}
