package com.bdaim.dataexport.service;

import com.bdaim.auth.LoginUser;
import com.bdaim.label.dao.LabelDao;
import com.bdaim.label.dto.CategoryType;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.DataNode;
import com.bdaim.rbac.dto.UserDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 数据权限实现类
 *
 */
@Service
public class DataPermissionService {

	@Resource
	private LabelDao labelDao;
	
	//标签分类
	public List<DataNode> getLabelList(UserDTO user, DataNode root, Integer deep, QueryType type) {
		Map<Object, List<DataNode>> map = new HashMap<Object, List<DataNode>>();
		List<DataNode> lst = new ArrayList<DataNode>();
		List rs = null;
		Object parentId = 0;
		try{
			if(user==null||(user!=null&&user.getId()==null)){
				rs = labelDao.sqlQuery("select t1.id,t1.label_id,ifnull(t1.parent_id,0) parent_id,t1.label_name from label_info t1 where t1.level<=?",deep);
				for(int i=0;i<rs.size();i++){
					Map r = (Map)rs.get(i);
					DataNode dn = new DataNode();
					dn.setId(r.get("id"));
					dn.setName(String.valueOf(r.get("label_name")));
					dn.setLabelId(String.valueOf(r.get("label_id")));
					dn.setChecked(false);
					
					if(parentId.equals(r.get("parent_id"))){
						lst.add(dn);
						if(i==rs.size()-1){
							map.put(parentId, lst);
						}
					}else{
						map.put(parentId, lst);
						parentId = r.get("parent_id");
						lst = new ArrayList<DataNode>();
						lst.add(dn);
						if(i==rs.size()-1){
							map.put(parentId, lst);
						}
					}
					
				}
				lst = getTree(map,root);
			}else{
				lst = labelDao.getLabelList(user, root, deep, type);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
		return lst;
	}
	
	//品类权限  包括电商与媒体
	public List<DataNode> getCategoryList(UserDTO user, DataNode root, Integer deep, QueryType type, CategoryType categoryType) {
		Map<Object, List<DataNode>> map = new HashMap<Object, List<DataNode>>();
		List<DataNode> lst = new ArrayList<DataNode>();
		List rs = null;
		Object parentId = 0;
		try{
			if(user==null||(user!=null&&user.getId()==null)){
				rs = labelDao.sqlQuery("select t1.id,ifnull(t1.parent_id,0) parent_id,t1.name from label_category t1 where t1.level<=? and t1.type = ?",deep, categoryType.ordinal());
				for(int i=0;i<rs.size();i++){
					Map r = (Map)rs.get(i);
					DataNode dn = new DataNode();
					dn.setId(r.get("id"));
					dn.setName(String.valueOf(r.get("name")));
					dn.setChecked(false);
					
					if(parentId.equals(r.get("parent_id"))){
						lst.add(dn);
						if(i==rs.size()-1){
							map.put(parentId, lst);
						}
					}else{
						map.put(parentId, lst);
						parentId = r.get("parent_id");
						lst = new ArrayList<DataNode>();
						lst.add(dn);
						if(i==rs.size()-1){
							map.put(parentId, lst);
						}
					}
					
				}
				lst = getTree(map,root);
			}else{
				lst = labelDao.getCategoryList(user, root, deep, type, categoryType);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
		return lst;
	}
	
	//对权限生成树形结构
	private List<DataNode> getTree(Map<Object, List<DataNode>> map, DataNode dataNode){
		for(Object obj : map.keySet()){
			List<DataNode> lst = map.get(obj);
			for(int i=0; i<lst.size(); i++){
				DataNode dn = lst.get(i);
				if(map.get(dn.getId())!=null){
					map.get(obj).get(i).getChildren().addAll(map.get(dn.getId()));
				}
			}
		}
		if(dataNode!=null&&dataNode.getId()!=null){
			return map.get(Integer.parseInt(dataNode.getId().toString()));
		}
		return map.get(0);
	}
	
	
	
	public List<DataNode> getLabelList(Long userId, Integer deep, DataNode root, QueryType type){
		UserDTO user = new UserDTO();
		user.setId(userId);

		List<DataNode> lst = labelDao.getLabelList(user, root, deep, type);
		return lst;
	}
	
	/**
	 * 
	 * @param userId
	 * @param deep
	 * @param root
	 * @param type  ALL  PRIVILEGE
	 * @param categoryType MEDIA PRODUCT
	 * @return
	 */
	public List<DataNode> getCategoryList(Long userId, Integer deep, DataNode root, QueryType type, CategoryType categoryType){
		UserDTO user = new UserDTO();
		user.setId(userId);

		List<DataNode> lst = labelDao.getCategoryList(user, root, deep, type, categoryType);
		return lst;
	}
	
	/**
	 * 
	 * 取用户品类权限叶子节点
	 * @param request
	 * @return
	 */
	public List<Integer> getLeafCategoryList(HttpServletRequest request){
		List<Integer> categoryIds = new ArrayList<Integer>();
		LoginUser u = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserDTO user = new UserDTO();
		user.setId(u.getId());
		user.setName(u.getName());
		
		
		List<DataNode> categoryList = new ArrayList<DataNode>();
		List<DataNode> perCategoryList = new ArrayList<DataNode>();
		List<DataNode> lstAllMedia = labelDao.getCategoryList(user, null, 5, QueryType.ALL, CategoryType.MEDIA);
		List<DataNode> lstAllProduct = labelDao.getCategoryList(user, null, 5, QueryType.ALL, CategoryType.PRODUCT);
		List<DataNode> lstPerMedia = labelDao.getCategoryList(user, null, 2, QueryType.PRIVILEGE, CategoryType.MEDIA);
		List<DataNode> lstPerProduct = labelDao.getCategoryList(user, null, 2, QueryType.PRIVILEGE, CategoryType.PRODUCT);
		if(null!=lstAllProduct&&lstAllProduct.size()>0)
			categoryList.addAll(lstAllProduct);
		if(null!=lstAllMedia&&lstAllMedia.size()>0)
			categoryList.addAll(lstAllMedia);
		if(null!=lstPerMedia&&lstPerMedia.size()>0)
			perCategoryList.addAll(lstPerMedia);
		if(null!=lstPerProduct&&lstPerProduct.size()>0)
			perCategoryList.addAll(lstPerProduct);
		getPerCategoryTree(categoryList, perCategoryList);
		getPerCategory(categoryList, categoryIds);
		return categoryIds;
	}
	
/*	//权限树
	public static List<DataNode> getCategoryTreeWithPermission(HttpServletRequest request) {
		UserManager userManager = new UserManagerImpl();
		User user = userManager.getManager(request);
		DataPrivilegeDao dataPrivilegeDao = new DataPrivilegeDao();
		List<DataNode> categoryList = new ArrayList<DataNode>();
		List<DataNode> perCategoryList = new ArrayList<DataNode>();
		//所有权限
		List<DataNode> lstAllMedia = dataPrivilegeDao.getCategoryList(user, null, 5, QueryType.ALL, CategoryType.MEDIA);
		List<DataNode> lstAllProduct = dataPrivilegeDao.getCategoryList(user, null, 5, QueryType.ALL, CategoryType.PRODUCT);
		//用户权限
		List<DataNode> lstPerMedia = dataPrivilegeDao.getCategoryList(user, null, 2, QueryType.PRIVILEGE, CategoryType.MEDIA);
		List<DataNode> lstPerProduct = dataPrivilegeDao.getCategoryList(user, null, 2, QueryType.PRIVILEGE, CategoryType.PRODUCT);
		if(null!=lstAllProduct&&lstAllProduct.size()>0)
			categoryList.addAll(lstAllProduct);
		if(null!=lstAllMedia&&lstAllMedia.size()>0)
			categoryList.addAll(lstAllMedia);
		if(null!=lstPerMedia&&lstPerMedia.size()>0)
			perCategoryList.addAll(lstPerMedia);
		if(null!=lstPerProduct&&lstPerProduct.size()>0)
			perCategoryList.addAll(lstPerProduct);
		getPerCategoryTree(categoryList, perCategoryList);
		freturn categoryList;
	}*/
	
	//权限树
	public List<DataNode> getCategoryTreeWithPermission2(Long userId, List<DataNode> lst, CategoryType categoryType) {
		if(null == lst)
			return new ArrayList<DataNode>();
		UserDTO user = new UserDTO();
		user.setId(userId);
		switch(categoryType){
			case MEDIA:
				List<DataNode> lstAllMedia = labelDao.getCategoryList(user, null, 5, QueryType.ALL, CategoryType.MEDIA);
				getPerCategoryTree(lstAllMedia, lst);
				return lstAllMedia;
			case PRODUCT:
				List<DataNode> lstAllProduct = labelDao.getCategoryList(user, null, 5, QueryType.ALL, CategoryType.PRODUCT);
				getPerCategoryTree(lstAllProduct, lst);
				return lstAllProduct;
		}
		return null;
	}
	
	private void getPerCategoryTree(List<DataNode> categoryList, List<DataNode> perCategoryList) {
		for(Iterator it = categoryList.iterator(); it.hasNext();){
			DataNode dn = (DataNode)it.next();
			boolean b = false;
			for(DataNode dn2 : perCategoryList){
				if(dn.getId().equals(dn2.getId())){
					b = true;
				}
			}
			if(!b){
				it.remove();
			}
		}
		List<DataNode> perCategoryLevel2List = new ArrayList<DataNode>();
		for(DataNode dn : perCategoryList){
			perCategoryLevel2List.addAll(dn.getChildren());
		}
		for(int i = 0; i < categoryList.size(); i++){
			DataNode dn1 = categoryList.get(i);
			List<DataNode> children = dn1.getChildren();
			for(Iterator it = children.iterator(); it.hasNext();){
				DataNode dn = (DataNode)it.next();
				boolean b = false;
				for(DataNode dn2 : perCategoryLevel2List){
					if(dn.getId().equals(dn2.getId())){
						b = true;
					}
				}
				if(!b){
					it.remove();
				}
			}
			categoryList.get(i).setChildren(children);
		}
	}
	
	private static void getPerCategory(List<DataNode> lst, List<Integer> categoryIds) {
		for(DataNode dn : lst){
			List<DataNode> children = dn.getChildren();
			if(children.size()>0){
				getPerCategory(children, categoryIds);
			}else{
				categoryIds.add(Integer.valueOf(dn.getId().toString()));
			}
		}
	}

}
