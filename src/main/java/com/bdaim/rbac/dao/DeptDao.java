package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.entity.DeptEntity;
import com.bdaim.rbac.entity.RoleEntity;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author duanliying
 * @date 2019/3/13
 * @description
 */
@Component
public class DeptDao extends SimpleHibernateDao<DeptEntity, Serializable> {
    /**
     * 根据部门id查询部门对象
     */
    public DeptEntity getDeptEntityById(Long id) {
        DeptEntity cp = null;
        String hql = "from DeptEntity m where m.id=?";
        List<DeptEntity> list = this.find(hql, id);
        if (list.size() > 0)
            cp = (DeptEntity) list.get(0);
        return cp;
    }

    /**
     * 查询部门列表
     */
    public List<DeptEntity> getDeptEntityList() {
        String hql = "from DeptEntity";
        List<DeptEntity> list = this.find(hql);
        return list;
    }

    /**
     * 根据部门id查询职位集合
     */
    public List<RoleEntity> getRoleEntityList(Long deptId) {
        String hql = "from RoleEntity m where m.deptId=?";
        List<RoleEntity> list = new ArrayList<>();
        try {
            list = this.find(hql, deptId);
        } catch (Exception e) {
            logger.info("查询职位出错" + e);
        }
        return list;
    }

    /**
     * 根据部门id删除部门信息
     *
     * @throws SQLException
     */
    public int deleteByDeptId(Long deptId) {
        int i = 0;
        try {
            String sql = "DELETE FROM t_dept WHERE ID = '" + deptId + "'";
            i = jdbcTemplate.update(sql);

        } catch (Exception e) {
            logger.info("删除失败>>>>>>>" + e);
        }
        return i;
    }
}
