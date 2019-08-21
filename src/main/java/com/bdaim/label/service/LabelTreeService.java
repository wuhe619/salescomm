package com.bdaim.label.service;

import com.bdaim.label.entity.LabelTree;

import java.util.List;
import java.util.Map;

public interface LabelTreeService {
	public List<LabelTree> loadAllLabelTree();

	public List<LabelTree> loadChildrenById(int id);

	public LabelTree loadLabelTreeById(int id);

	public List<Map<String, Object>> getChildrenByIdAndLevel(int i, int j);

	public Map<String, Object> getLabelTree(int id);

}
