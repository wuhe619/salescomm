package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.entity.UserDO;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Component
public class UserInfoDao extends SimpleHibernateDao<UserDO, Serializable> {

    /**
     * 根据登录用户名和授权来查询登录用户
     *
     * @param loginName
     * @param source
     * @return
     */
    public UserDO getUserByNameAuthorize(String loginName, int source) {
        String hql = "FROM UserDO m WHERE m.name=? AND FIND_IN_SET(?, m.authorize) >0 ";
        List<UserDO> list = this.find(hql, loginName, source);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
