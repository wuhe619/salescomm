package com.bdaim.label.service;

import com.bdaim.label.dao.LabelTreeDao;
import com.bdaim.label.entity.LabelTree;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LabelTreeServiceImpl implements LabelTreeService {
	@Resource
	private LabelTreeDao labelTreeDao;
	@Resource
	private LabelCategoryService categoryTreeServiceImpl;

	@Override
	public List<LabelTree> loadAllLabelTree() {
		return labelTreeDao.createQuery("from LabelTree where parent=0").list();
	}

	@Override
	public List<LabelTree> loadChildrenById(int id) {
		return labelTreeDao.createQuery("from LabelTree where parent=?", id)
				.list();
	}

	@Override
	public LabelTree loadLabelTreeById(int id) {
		return labelTreeDao.get(id);
	}

	public List<Map<String, Object>> getTreeByLevel(int id, int level) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		LabelTree t = labelTreeDao.get(id);
		if (t.getLevel() >= level) {
			return new ArrayList<Map<String, Object>>();
		} else {
			List<LabelTree> tmp = t.getChildren();
			for (LabelTree l : tmp) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", l.getName());
//				if (l.getChildCategory() != null) {
//					map.put("childCategory()", categoryTreeServiceImpl
//							.getChildrenById(l.getChildCategory().getId()));
//				}
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
	public List<Map<String, Object>> getChildrenByIdAndLevel(int pid, int level) {
		return getTreeByLevel(pid, level);
	}

	/**
	 * 根据父级id和层级获取子集
	 * 
	 * @param id
	 * @param level
	 * @return
	 */
	@Override
	public Map<String, Object> getLabelTree(int id) {
		Map<String, Object> result = new HashMap<String, Object>();
		LabelTree label = labelTreeDao.get(id);
		result.put("name", label.getName());
		result.put("path", label.getPath());
//		if (label.getChildCategory() != null) {
//			result.put("childCategory()", categoryTreeServiceImpl
//					.getChildrenById(label.getChildCategory().getId()));
//		}
		return result;
	}
}
