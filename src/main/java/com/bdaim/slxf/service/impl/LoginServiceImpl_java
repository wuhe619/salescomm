package com.bdaim.slxf.service.impl;

import javax.annotation.Resource;

import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.entity.User;
import com.bdaim.slxf.service.LoginService;
import com.bdaim.slxf.service.LoginService;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

	@Resource
	private UserDao userDao;

	@Override
	public User login(String userName, String password) {
		User user = userDao.findUnique(Restrictions.eq("userName", userName),
				Restrictions.eq("password", password));
		return user;
	}

	@Override
	public String loginOut(User user) {
		return null;
	}

	@Override
	public boolean update(User user) {
		User u = login(user.getName(), user.getPassword());
		if (u == null)
			return false;
		u.setPassword(user.getNewPassword());
		userDao.update(u);
		return true;
	}
}
