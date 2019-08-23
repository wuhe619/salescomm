package com.bdaim.common.service;

import com.bdaim.label.entity.LabelCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service("initService")
@Transactional
public class InitService {
	public static final Map<Integer,Integer> LID_CID_MAP = new TreeMap<Integer,Integer>();
	public static final Map<Integer,List<Integer>> LID_CID_LIST_MAP = new TreeMap<Integer,List<Integer>>();
	public static final Map<Integer,LabelCategory> CATEGORY_MAP = new TreeMap<Integer,LabelCategory>();
	public static final Map<String,Object> PICTURE_MAP = new HashMap<String,Object>();

}
