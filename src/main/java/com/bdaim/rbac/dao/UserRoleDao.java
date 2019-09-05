package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.DateUtil;
import com.bdaim.rbac.dto.RoleDTO;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.dto.UserRoles;
import com.bdaim.rbac.entity.User;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 */
@Component
public class UserRoleDao extends SimpleHibernateDao<User, Serializable> {

    public void insert(UserRoles userRoles)  {
    	UserDTO user = userRoles.getUser();
    	List<RoleDTO> roles = userRoles.getRoles();
    	String optUser = userRoles.getOptUser();
        if (roles != null && user != null && !roles.isEmpty() && user.getId()!=null) {
            for(RoleDTO role : roles){
            	this.executeUpdateSQL("insert into t_user_role_rel(ID,ROLE,OPTUSER,CREATE_TIME) VALUES("+user.getId()+","+role.getKey()+",'"+optUser+"',now())");
            }
        }
    }

    /**
     * 支持：按人员删除、按角色删除、按人员和角色删除
     * @param con
     * @param userRoles
     * @throws SQLException
     */
    public void delete(UserRoles userRoles)  {
        List<RoleDTO> roles = userRoles.getRoles();
        UserDTO user = userRoles.getUser();
        //如果角色为空，则表示删除当前人员的所有角色，否则删除指定角色
        if (roles != null && !roles.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (RoleDTO role : roles) {
                sb.append(role.getKey()).append(",");
            }
            //如果人员为空，则删除角色的相关信息
            if (user==null||user.getId()==null)
                this.executeUpdateSQL("delete from t_user_role_rel where ROLE in(" + sb.substring(0, sb.length() - 1) + ")");
            else
                this.executeUpdateSQL("delete from t_user_role_rel where ID=" + user.getId() + " and ROLE in(" + sb.substring(0, sb.length() - 1) + ")");
        } else {
            this.executeUpdateSQL("delete from t_user_role_rel where ID="+user.getId());
        }
    }

    public void update(Connection con, UserRoles userRoles) throws SQLException {
        if (userRoles.getUser().getKey() == null || userRoles.getUser().getKey().equals(""))
            throw new NullPointerException("用户信息不可为空");
        //更新用户角色就是先删除后增加的过程
        UserRoles delURoles=new UserRoles();
        delURoles.setUser(userRoles.getUser());
        delete(delURoles);
        insert(userRoles);

    }

    /**
     * 这个方法提供按用户查询他的权限信息
     *
     *
     * @param con
     * @param userRoles
     * @return
     */
    public UserRoles getObj(UserRoles userRoles) {
        try {
            List list = this.getSQLQuery("SELECT rel.ID,rel.ROLE,rel.LEVEL,rel.OPTUSER,rel.CREATE_TIME,r.NAME" +
                    " from t_role r,t_user_role_rel rel where rel.ROLE=r.ID and rel.ID="+userRoles.getUser().getKey()).list();
            
            UserRoles URoles = null;
            if (list.size()>0) {
            	List<RoleDTO> roles = new ArrayList<RoleDTO>();
            	List<Integer> levels = new ArrayList<Integer>();
                if (URoles==null)URoles=new UserRoles();
                for(int i=0;i<list.size();i++) {
                	Object[] obj = (Object[])list.get(i);
                	 RoleDTO role = new RoleDTO();
                     role.setKey(Long.parseLong(String.valueOf(obj[1])));
                     role.setName(String.valueOf(obj[5]));
                     roles.add(role);
                     levels.add(Integer.parseInt(String.valueOf(obj[2])));
                }
                URoles.setLevel(levels);
                URoles.setRoles(roles);
                Object[] d = (Object[])list.get(0);
                URoles.setOptUser(String.valueOf(d[3]));
                URoles.setCreateDate(DateUtil.fmtStrToDate(String.valueOf(d[4])));
            }
            return URoles;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

        }
        return null;
    }


	public void delete(Long operateUserId, Long userId) throws SQLException{
		StringBuilder builder = new StringBuilder();
		builder.append(" delete from t_user_role_rel where id = "+userId+" and role in (select temp.id from");
		builder.append(" (select r.id from t_role r inner join t_user_role_rel ur on ur.ROLE = r.ID and ur.id = "+operateUserId+")temp)");
		this.executeUpdateSQL(builder.toString());
	}


	public void deleteByUserId(Long userId) throws SQLException{
		String sql = "delete from t_user_role_rel where id = "+userId;
		this.executeUpdateSQL(sql);
	}
}
