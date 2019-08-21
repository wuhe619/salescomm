package com.bdaim.label.service;


import com.bdaim.label.entity.LabelCategory;

import java.util.List;
import java.util.Map;

public interface LabelCategoryService {
    /**
     * 获取所有品类
     *
     * @return
     */
    public List<LabelCategory> loadAllLabelCategory();

    /**
     * 根据父id获取子品类
     *
     * @param id
     * @return
     */
    public List<LabelCategory> loadChildrenById(Integer id);

    /**
     * 根据id获取品类
     *
     * @param id
     * @return
     */
    public LabelCategory loadLabelCategoryById(Integer id);

    /**
     * 根据父id和层级获取子品类
     *
     * @param pid
     * @param level
     * @return
     */
    public List<Map<String, Object>> getChildrenByIdAndLevel(Integer pid,
                                                             Integer level);

    /**
     * 根据父id获取子品类
     *
     * @param pid
     * @param level
     * @return
     */
    public List<Map<String, Object>> getChildrenById(Integer pid);

    /**
     * 根据分类获取品类
     *
     * @param classId
     * @return
     */
    public List<Map<String, Object>> getAllLabelCategoryByClassId(
            Integer classId);

    /**
     * 根据分类获取末级第一个品类
     *
     * @param classId
     * @return
     */
    public LabelCategory getFirstLeafLabelCategoryByClassId(Integer classId);

    /**
     * @param cate
     * @return
     */
    public Integer addLabelCategory(LabelCategory cate);

    /**
     * 根据category列表生成树形结构
     *
     * @param categorys
     * @return
     */
    public List<Map<String, Object>> getCategoryTree(List<LabelCategory> categorys);

    /**
     * 根据父id获取子品类
     *
     * @param id
     * @return
     */
    public LabelCategory getCategoryByCategoryId(String categoryId);

    /**
     * 获取带权限的品类叶子节点
     *
     * @param ids
     * @return
     */
    public List<Map<String, Object>> getLeafLabelCategory(List<Integer> ids);

    /**
     * 根据条件获取品类树
     *
     * @param map
     * @return
     */
    public String getCategoryTreeByMap(Map<String, Object> map);

    /**
     * 获取所有品类
     *
     * @return
     */
    public List<LabelCategory> getAllLabelCategory();

    /**
     * 根据category列表生成树形结构
     *
     * @param categorys
     * @return
     */
    public List<Map<String, Object>> getSubCategoryTree(List<LabelCategory> categorys);
}
