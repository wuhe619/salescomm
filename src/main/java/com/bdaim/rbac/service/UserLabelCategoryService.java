package com.bdaim.rbac.service;

import com.bdaim.label.entity.LabelCategory;
import com.bdaim.label.entity.UserLabelCategory;
import com.bdaim.rbac.dao.UserLabelCategoryDao;
import com.bdaim.rbac.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Transactional
@Service("userLabelCategoryService")
public class UserLabelCategoryService {

	@Resource
	private UserLabelCategoryDao userLabelCategoryDao;

	
	public void updateIsDefault(Long userId, Integer labelCategoryId) {
		UserLabelCategory ulc1 = userLabelCategoryDao.findUnique("from UserLabelCategory ulc where ulc.labelCategoryUser.id=? and ulc.isDefault=?", new Object[]{userId, 1});
		UserLabelCategory ulc2 = userLabelCategoryDao.findUnique("from UserLabelCategory ulc where ulc.labelCategoryUser.id=? and ulc.labelCategory.id=?", new Object[]{userId, labelCategoryId});
		if(null!=ulc1){
			if(null!=ulc2&&(!ulc1.getLabelCategory().getId().equals(ulc2.getLabelCategory().getId()))){
				ulc1.setIsDefault(0);
				userLabelCategoryDao.update(ulc1);
				ulc2.setIsDefault(1);
				userLabelCategoryDao.update(ulc2);
			}else{
				ulc1.setIsDefault(0);
				userLabelCategoryDao.update(ulc1);
				saveLabelCategory(userId, labelCategoryId);
			}
		}else{
			if(null!=ulc2){
				ulc2.setIsDefault(1);
				userLabelCategoryDao.update(ulc2);
			}else{
				saveLabelCategory(userId, labelCategoryId);
			}
		}
	}
	
	private void saveLabelCategory(Long userId, Integer labelCategoryId){
		UserLabelCategory uc = new UserLabelCategory();
		User user = new User();
		user.setId(userId);
		uc.setLabelCategoryUser(user);
		LabelCategory lc = new LabelCategory();
		lc.setId(labelCategoryId);
		uc.setLabelCategory(lc);
		uc.setIsDefault(1);
		userLabelCategoryDao.save(uc);
	}
}
