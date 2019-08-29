package com.bdaim.rbac.service;

import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.*;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.rbac.dao.DeptDao;
import com.bdaim.rbac.dto.DeptDto;
import com.bdaim.rbac.entity.DeptEntity;
import com.bdaim.rbac.entity.RoleEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.util.*;

/**
 * @author duanliying
 * @date 2019/3/15
 * @description
 */
@Service("deptService")
@Transactional
public class DeptService {
    private static Logger logger = LoggerFactory.getLogger(DeptService.class);
    @Resource
    private DeptDao deptDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 新增或编辑修改 部门信息
     *
     * @param deptDto   部门信息
     * @param optUserId 操作人
     * @return
     * @auther Chacker
     * @date
     */
    public Map<String, String> updateDeptMessage(DeptDto deptDto, String optUserId) {
        logger.info("新增或编辑修改 部门信息 ======");
        Map<String, String> resultMap = new HashMap<>();
        String id = deptDto.getId();
        try {
            if (StringUtil.isEmpty(id)) {
                /**
                 * 入参中不存在部门id，则执行新增操作
                 * 先查验该部门名称是否已存在，如果已存在则不允许新增
                 */
                boolean isExisted = checkDeptName(deptDto.getName());
                logger.info("该部门是否已存在 " + isExisted);
                if (!isExisted) {
                    DeptEntity deptEntity = new DeptEntity();
                    deptEntity.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                    deptEntity.setId(IDHelper.getTransactionId());
                    deptEntity.setType(1);
                    deptEntity.setOptuser(String.valueOf(optUserId));
                    deptEntity.setName(deptDto.getName());
                    deptDao.saveOrUpdate(deptEntity);
                    resultMap.put("code", Constant.SUCCESS_CODE);
                    resultMap.put("message", "操作成功");
                } else {
                    resultMap.put("code", Constant.FAILURE_CODE);
                    resultMap.put("message", "该部门已存在");
                }
            } else {
                //入参中存在部门id，则执行修改操作
                DeptEntity deptEntity = deptDao.getDeptEntityById(NumberConvertUtil.parseLong(id));
                logger.info("执行修改操作" + deptEntity.toString());
                if (deptEntity != null) {
                    deptEntity.setName(deptDto.getName());
                    deptEntity.setModifyTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                    deptEntity.setOptuser(optUserId);
                    deptDao.saveOrUpdate(deptEntity);
                    resultMap.put("code", Constant.SUCCESS_CODE);
                    resultMap.put("message", "编辑成功");
                }
            }
        } catch (Exception e) {
            logger.error("编辑部门信息异常" + e);
            resultMap.put("code", Constant.FAILURE_CODE);
            resultMap.put("message", "操作失败");
        }
        return resultMap;
    }

    /**
     * 部门列表信息查询
     *
     * @param map page_num、page_size
     * @return
     * @auther Chacker
     * @date
     */
    public Map<String, Object> queryDeptList(Map<String, Object> map) throws Exception {
        //设置分页参数
        PageParam param = new PageParam();
        param.setPageNum(Integer.parseInt(String.valueOf(map.get("page_num"))));
        param.setPageSize(Integer.parseInt(String.valueOf(map.get("page_size"))));

        //查询SQL语句
        StringBuffer buffer = new StringBuffer("SELECT t1.id,t1.create_time createTime,t1.name,COUNT(t2.id) roleNum ");
        buffer.append("FROM t_dept t1 LEFT JOIN t_role t2 ON t1.id=t2.deptid ")
                .append("GROUP BY t1.id,t1.create_time,t1.name");
        logger.info("执行查询 " + buffer.toString());
        Page page = new Pagination().getPageData(buffer.toString(), null, param, jdbcTemplate);
        List<Map<String, Object>> data = page.getList();

        //组装返回值
        Map<String, Object> result = new HashMap<>(16);
        result.put("data", data);
        result.put("total", page.getTotal());
        return result;
    }


    /**
     * 删除部门信息
     *
     * @date: 2019/3/13 18:09
     */
    public Map<String, String> delDeptMessage(String deptId) {
        Map<String, String> resultMap = new HashMap<>();
        logger.info("需要删除的部门id是：" + deptId);
        //根据部门id查询是否存在成员,存在不能删除
        String code = "0";
        try {
            if (StringUtil.isNotEmpty(deptId)) {
                List<RoleEntity> roleEntityList = deptDao.getRoleEntityList(Long.parseLong(deptId));
                if (roleEntityList.size() > 0) {
                    //有成员信息所以不能删除
                    resultMap.put("code", code);
                    resultMap.put("message", "该部门下有其他成员,暂时不能删除此部门");
                    return resultMap;
                }
            }
            int i = deptDao.deleteBydeptId(Long.parseLong(deptId));
            if (i > 0) {
                resultMap.put("code", "1");
                resultMap.put("message", "删除部门成功");
            } else {
                resultMap.put("code", code);
                resultMap.put("message", "删除部门失败");
            }

        } catch (Exception e) {
            logger.error("删除部门信息异常" + e);
        }
        return resultMap;
    }

    /**
     * 核验部门名称是否唯一
     *
     * @author:duanliying
     * @method
     * @date: 2019/3/13 19:24
     */
    public boolean checkDeptName(String deptName) {
        String sql = "SELECT id FROM t_dept WHERE name = '" + deptName + "'";
        List<Map<String, Object>> list = deptDao.sqlQuery(sql);
        if (list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @description 获取部门信息以及部门下职位信息
     * @author:duanliying
     * @method
     * @date: 2019/4/23 17:16
     */
    public List<Map<String, Object>> getDeptAndRoles(String deptId) throws Exception {
        logger.info("传递的部门id是：" + deptId);
        StringBuffer queryDeptSql = new StringBuffer("SELECT  cast(ID as char) deptID,`NAME` deptName FROM t_dept where 1=1");
        if (StringUtil.isNotEmpty(deptId)) {
            queryDeptSql.append(" and ID ='" + deptId + "'");
        }
        List<Map<String, Object>> deptList = deptDao.sqlQuery(queryDeptSql.toString());
        if (deptList.size() > 0) {
            List<Map<String, Object>> roleList = null;
            String roleSql = "SELECT ID roleId,`NAME` roleName  FROM t_role WHERE DEPTID =?";
            for (int i = 0; i < deptList.size(); i++) {
                String deptID = String.valueOf(deptList.get(i).get("deptID"));
                //根据部门id查询职位信息
                roleList = deptDao.sqlQuery(roleSql, deptID);
                if (roleList != null && roleList.size() > 0) {
                    deptList.get(i).put("roles", roleList);
                }
            }
        }
        return deptList;
    }
}
