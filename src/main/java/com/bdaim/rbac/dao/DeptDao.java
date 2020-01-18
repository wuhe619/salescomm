package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.dto.DeptDTO;
import com.bdaim.rbac.entity.DeptEntity;
import com.bdaim.rbac.entity.RoleEntity;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.Connection;
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
            String sql = "DELETE FROM t_dept WHERE ID = ? ";
            i = jdbcTemplate.update(sql, deptId);

        } catch (Exception e) {
            logger.info("删除失败>>>>>>>" + e);
        }
        return i;
    }

    public void insert(DeptDTO t) throws SQLException {
        this.executeUpdateSQL("insert into t_dept(ID,NAME,OPTUSER,CREATE_TIME,TYPE) values(?,?,?,now(),?)", t.getId(), t.getName(), t.getOptuser(), t.getType());
    }

    public void delete(DeptDTO t) {
        if (t.getId() == null) throw new NullPointerException("删除记录的ID不可为空");
        this.executeUpdateSQL("delete from t_dept where ID=?", t.getId());
    }

    public void update(DeptDTO t) {
        if (t.getId() == null) throw new NullPointerException("更新记录的ID不可为空");
        StringBuffer sb = new StringBuffer();
        sb.append("update t_dept set MODIFY_TIME=now(),");
        List<Object> p = new ArrayList<>();
        if (t.getOptuser() != null && !t.getOptuser().equals("")) {
            sb.append(" OPTUSER=?, ");
            p.add(t.getOptuser());
        }
        if (t.getName() != null && !t.getName().equals("")) {
            sb.append(" NAME=? ");
            p.add(t.getName());
        }
        //确认SQL，绑定参数
        this.executeUpdateSQL(sb.substring(0, sb.length() - 1) + " where ID=? ", t.getId(), p.toArray());
    }

    public DeptDTO getObj(Connection con, DeptDTO t) {

        return null;
    }

}
