package com.bdaim.label.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.InitService;
import com.bdaim.label.dao.CommonService;
import com.bdaim.label.dao.LabelCategoryDao;
import com.bdaim.label.entity.LabelCategory;
import com.bdaim.rbac.entity.User;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class LabelCategoryServiceImpl implements LabelCategoryService {
	@Resource
	private LabelCategoryDao labelCategoryDao;

	@Override
	public Integer addLabelCategory(LabelCategory cate) {
		return (Integer) labelCategoryDao.saveReturnPk(cate);
	}

	@Override
	public List<LabelCategory> getAllLabelCategory() {
		return labelCategoryDao.createQuery("from LabelCategory").list();
	}
	
	@Override
	public List<LabelCategory> loadAllLabelCategory() {
		return loadChildrenById(0);
	}

	@Override
	public List<LabelCategory> loadChildrenById(Integer id) {
		List<LabelCategory> result = new ArrayList<LabelCategory>();
		List<LabelCategory> list = labelCategoryDao.createQuery(
				"from LabelCategory where parent.id=?", id).list();
		if (list.size() == 0)
			return result;
		else {
			for (LabelCategory c : list) {
				LabelCategory LabelCategory = new LabelCategory();
				LabelCategory.setName(c.getName());
				LabelCategory.setPath(c.getPath());
				LabelCategory.setUri(c.getUri());
				LabelCategory.setChildren(loadChildrenById(c.getId()));
				result.add(LabelCategory);
			}
		}
		return result;
	}

	@Override
	public LabelCategory loadLabelCategoryById(Integer id) {
		if(InitService.CATEGORY_MAP.containsKey(id))
			return InitService.CATEGORY_MAP.get(id);
		return labelCategoryDao.get(id);
	}

	public List<Map<String, Object>> getTreeByLevel(int id, int level) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		LabelCategory t = labelCategoryDao.get(id);
		if (t.getLevel() >= level) {
			return new ArrayList<Map<String, Object>>();
		} else {
			List<LabelCategory> tmp = t.getChildren();
			for (LabelCategory l : tmp) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", l.getName());
				map.put("children", getTreeByLevel(l.getId(), level));
				result.add(map);
			}
		}
		return result;
	}

	/**
	 * 根据父级id和层级获取子集
	 * 
	 * @param id
	 * @param level
	 * @return
	 */
	@Override
	public List<Map<String, Object>> getChildrenByIdAndLevel(Integer pid,
			Integer level) {
		return getTreeByLevel(pid, level);
	}

	public List<Map<String, Object>> getTreeById(Integer id) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<LabelCategory> list = labelCategoryDao.createQuery(
				"from LabelCategory where parent.id=?", id).list();
		if (list.size() == 0)
			return result;
		else {
			for (LabelCategory c : list) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", c.getId());
				map.put("name", c.getName());
				map.put("children", getTreeById(c.getId()));
				result.add(map);
			}
		}
		return result;
	}

	/**
	 * 根据父级id和层级获取子集
	 * 
	 * @param id
	 * @param level
	 * @return
	 */
	@Override
	public List<Map<String, Object>> getChildrenById(Integer pid) {
		return getTreeById(pid);
	}

	@Override
	public List<Map<String, Object>> getAllLabelCategoryByClassId(
			Integer classId) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> list = labelCategoryDao
				.createQuery(
						"select new map(id as id, name as name) from LabelCategory where parentClass.id=?",
						classId).list();
		for (Map<String, Object> m : list) {
			if (m.get("id") != null)
				m.put("children", getChildrenById(Integer.parseInt(m.get("id")
						.toString())));
			result.add(m);
		}
		return result;
	}

	@Override
	public LabelCategory getFirstLeafLabelCategoryByClassId(Integer classId) {
		Criteria c = labelCategoryDao.createCriteria(Restrictions.eq(
				"parentClass.id", classId));
		c.setProjection(Projections.min("id"));
		if (c.list().size() == 0 || c.list().get(0) == null)
			return null;
		LabelCategory category = labelCategoryDao
				.get((Integer) c.list().get(0));
		while (category.getChildren().size() > 0) {
			category = category.getChildren().get(0);
		}
		return category;
	}

	/**
	 * 根據category的list生成完整树树
	 * 
	 * @param categorys
	 * @return
	 */
	public List<Map<String, Object>> getCategoryTree(
			List<LabelCategory> categorys) {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		if (null == categorys)
			return mapList;
		Map<Integer, Map<Integer, LabelCategory>> levelMap = new TreeMap<Integer, Map<Integer, LabelCategory>>();
		for (LabelCategory category : categorys) {
			Integer level = category.getLevel();
			if (null == level)
				continue;
			category.setChildren(new ArrayList<LabelCategory>());
			Integer cid = category.getId();
			if (levelMap.containsKey(level)) {
				Map<Integer, LabelCategory> categoryMap = levelMap.get(level);
				categoryMap.put(cid, category);
			} else {
				Map<Integer, LabelCategory> categoryMap = new TreeMap<Integer, LabelCategory>();
				categoryMap.put(cid, category);
				levelMap.put(level, categoryMap);
				String[] uriArray = category.getUri().split("/");
				for (String id : uriArray) {
					if (id.matches("[0-9]+")) {
						LabelCategory parentCategory = InitService.CATEGORY_MAP.get(Integer
								.valueOf(id));
						Map<Integer, LabelCategory> parentMap = new TreeMap<Integer, LabelCategory>();
						parentCategory
								.setChildren(new ArrayList<LabelCategory>());
						parentMap.put(parentCategory.getId(), parentCategory);
						levelMap.put(parentCategory.getLevel(), parentMap);
					}
				}
			}
		}
		if (!levelMap.isEmpty()) {
			Integer[] levels = new Integer[levelMap.size()];
			levelMap.keySet().toArray(levels);
			for (int i = 0; i < levels.length; i++) {
				if (i == levels.length - 1) {
					Map<Integer, LabelCategory> map = levelMap
							.get(levels[levels.length - i - 1]);
					for (Integer id : map.keySet()) {
						mapList.add(CommonService.getLabelCategoryMap(map
								.get(id)));
					}
				} else {
					Map<Integer, LabelCategory> map = levelMap
							.get(levels[levels.length - i - 1]);
					Map<Integer, LabelCategory> parentMap = levelMap
							.get(levels[levels.length - i - 2]);
					Map<Integer, List<LabelCategory>> childrenMap = new TreeMap<Integer, List<LabelCategory>>();
					for (LabelCategory category : map.values()) {
						LabelCategory parentCategory = category.getParent();
						if (null != parentCategory) {
							Integer pid = parentCategory.getId();
							if (childrenMap.containsKey(pid)) {
								List<LabelCategory> childrenList = childrenMap
										.get(pid);
								childrenList.add(category);
							} else {
								List<LabelCategory> childrenList = new ArrayList<LabelCategory>();
								childrenList.add(category);
								childrenMap.put(pid, childrenList);
							}
						}
					}
					for (Integer pid : childrenMap.keySet()) {
						if (parentMap.containsKey(pid)) {
							parentMap.get(pid)
									.setChildren(childrenMap.get(pid));
						} else {
							LabelCategory parentCategory = InitService.CATEGORY_MAP.get(pid);
							parentCategory.setChildren(childrenMap.get(pid));
							parentMap.put(pid, parentCategory);
						}
					}
				}
			}
		}
		// System.out.println(JSON.toJSONString(mapList));
		return mapList;
	}
	/**
	 * 根據category的list生成子品类树,不向上寻找根节点
	 * 
	 * @param categorys
	 * @return
	 */
	public List<Map<String, Object>> getSubCategoryTree(
			List<LabelCategory> categorys) {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		if (null == categorys)
			return mapList;
		Map<Integer, Map<Integer, LabelCategory>> levelMap = new TreeMap<Integer, Map<Integer, LabelCategory>>();
		for (LabelCategory category : categorys) {
			Integer level = category.getLevel();
			if (null == level)
				continue;
			category.setChildren(new ArrayList<LabelCategory>());
			Integer cid = category.getId();
			if (levelMap.containsKey(level)) {
				Map<Integer, LabelCategory> categoryMap = levelMap.get(level);
				categoryMap.put(cid, category);
			} else {
				Map<Integer, LabelCategory> categoryMap = new TreeMap<Integer, LabelCategory>();
				categoryMap.put(cid, category);
				levelMap.put(level, categoryMap);
			}
		}
		if (!levelMap.isEmpty()) {
			Integer[] levels = new Integer[levelMap.size()];
			levelMap.keySet().toArray(levels);
			for (int i = 0; i < levels.length; i++) {
				if (i == levels.length - 1) {
					Map<Integer, LabelCategory> map = levelMap
							.get(levels[levels.length - i - 1]);
					for (Integer id : map.keySet()) {
						mapList.add(CommonService.getLabelCategoryMap(map
								.get(id)));
					}
				} else {
					Map<Integer, LabelCategory> map = levelMap
							.get(levels[levels.length - i - 1]);
					Map<Integer, LabelCategory> parentMap = levelMap
							.get(levels[levels.length - i - 2]);
					Map<Integer, List<LabelCategory>> childrenMap = new TreeMap<Integer, List<LabelCategory>>();
					for (LabelCategory category : map.values()) {
						LabelCategory parentCategory = category.getParent();
						if (null != parentCategory) {
							Integer pid = parentCategory.getId();
							if (childrenMap.containsKey(pid)) {
								List<LabelCategory> childrenList = childrenMap
										.get(pid);
								childrenList.add(category);
							} else {
								List<LabelCategory> childrenList = new ArrayList<LabelCategory>();
								childrenList.add(category);
								childrenMap.put(pid, childrenList);
							}
						}
					}
					for (Integer pid : childrenMap.keySet()) {
						if (parentMap.containsKey(pid)) {
							parentMap.get(pid)
									.setChildren(childrenMap.get(pid));
						} else {
							LabelCategory parentCategory = InitService.CATEGORY_MAP.get(pid);
							parentCategory.setChildren(childrenMap.get(pid));
							parentMap.put(pid, parentCategory);
						}
					}
				}
			}
		}
		return mapList;
	}
	@Override
	public LabelCategory getCategoryByCategoryId(String categoryId) {
		return labelCategoryDao.findUniqueBy("categoryId", categoryId);
	}

	@Override
	public List<Map<String, Object>> getLeafLabelCategory(List<Integer> ids) {
		if(null!=ids&&ids.size()>0){
			String sql = "select lc.id as labelCategoryId, concat(lc.path,lc.name) as categoryName, ulc.is_default as isDefault from label_category lc left join user_label_category ulc on lc.id=ulc.label_category_id where lc.id in (:ids)";
			return labelCategoryDao.getSQLQuery(sql).setParameterList("ids", ids).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
		}else{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getCategoryTreeByMap(Map<String,Object> map) {
		JSONObject json = new JSONObject();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		json.put("stores", list);
		User user = (User) map.get("user");
		Integer lid = Integer.valueOf(map.get("id").toString());
		Integer cid = InitService.LID_CID_MAP.get(lid);
		if(null == cid)
			return json.toString();

		List<Integer> cateList = InitService.LID_CID_LIST_MAP.get(lid);
		
		String sql = "From LabelCategory where id in(:cateList) ";
		List<LabelCategory> categorys = labelCategoryDao.createQuery(sql).setParameterList("cateList", cateList).list();
		list = getCategoryTree(categorys);
		json.put("stores", list);
		return json.toString();
	}
}
