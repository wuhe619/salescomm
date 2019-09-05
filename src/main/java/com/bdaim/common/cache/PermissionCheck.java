package com.bdaim.common.cache;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.helper.JDBCHelper;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 */
public class PermissionCheck {
    /**
     *
     * @param request
     * @param id
     * @return
     */
    public static boolean checkID(HttpServletRequest request, Long id){
//        UserManager manager= (UserManager) BeanCache.getBean(ConfigReader.USER_MANAGER);
    	LoginUser u = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DataSource ds= ((DataSourceManager) BeanCache.getBean(ConfigReader.DATA_SOURCE)).getDataSource();
        Connection con=null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con=ds.getConnection();
            st = con.prepareStatement("select ur.ID from t_user_role_rel ur,t_mrp_rel mr where ur.ID=? and mr.R_ID=? and ur.ROLE=mr.ROLE_ID and mr.type=0 ");
            st.setLong(1,u.getId());
            st.setLong(2,id);
            rs=st.executeQuery();
            if (rs.next())return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	JDBCHelper.closeRs(rs);
        	JDBCHelper.closeStmt(st);
            JDBCHelper.close(con);
        }
        return false;
    }

    public static boolean cehckURI(HttpServletRequest request, String uri){
//        UserManager manager= (UserManager) BeanCache.getBean(ConfigReader.USER_MANAGER);
    	LoginUser u = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DataSource ds= ((DataSourceManager) BeanCache.getBean(ConfigReader.DATA_SOURCE)).getDataSource();
        Connection con=null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = ds.getConnection().prepareStatement("select ur.ID FROM t_user_role_rel ur,t_mrp_rel mr,t_resource r where r.TYPE <> 4 and ur.ROLE=mr.ROLE_ID and mr.R_ID=r.ID and mr.type=0 " +
                    "and ur.ID=? and r.URI=?");
            st.setLong(1, u.getId());
            st.setString(2,uri);
            rs=st.executeQuery();
            if (rs.next())return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	JDBCHelper.closeRs(rs);
        	JDBCHelper.closeStmt(st);
            JDBCHelper.close(con);
        }
        return false;
    }

    public static boolean checkRegex(HttpServletRequest request, String uri){
//        UserManager manager= (UserManager) BeanCache.getBean(ConfigReader.USER_MANAGER);
    	LoginUser u = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DataSource ds= ((DataSourceManager) BeanCache.getBean(ConfigReader.DATA_SOURCE)).getDataSource();
        Connection con=null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = ds.getConnection().prepareStatement("select r.URI FROM t_user_role_rel ur,t_mrp_rel mr,t_resource r where r.TYPE = 4 and ur.ROLE=mr.ROLE_ID and mr.R_ID=r.ID and mr.type=0" +
                    "and ur.ID=? and r.URI=?");
            st.setLong(1, u.getId());
            st.setString(2,uri);
            rs=st.executeQuery();
            //循环所有的正则，判断当前URI是否合法
            Pattern uriRegex=null;
            while (rs.next()){
                uriRegex=Pattern.compile(rs.getString("URI"));
                if (uriRegex.matcher(uri).matches())return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	JDBCHelper.closeRs(rs);
        	JDBCHelper.closeStmt(st);
            JDBCHelper.close(con);
        }
        return false;
    }
    
    public static boolean cehckURI(Long userid,String uri){
        DataSource ds= ((DataSourceManager) BeanCache.getBean(ConfigReader.DATA_SOURCE)).getDataSource();
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            st = ds.getConnection().prepareStatement("select ur.ID FROM t_user_role_rel ur,t_mrp_rel mr,t_resource r where r.TYPE <> 4 and ur.ROLE=mr.ROLE_ID and mr.R_ID=r.ID and mr.type=0 " +
                    "and ur.ID=? and r.URI=?");
            st.setLong(1,userid);
            st.setString(2,uri);
            rs=st.executeQuery();
            if (rs.next()){
            	return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	JDBCHelper.closeRs(rs);
        	JDBCHelper.closeStmt(st);
            JDBCHelper.close(con);
        }
        return false;
    }
    
    public static boolean checkID(Long userid,Long id){
        DataSource ds= ((DataSourceManager) BeanCache.getBean(ConfigReader.DATA_SOURCE)).getDataSource();
        Connection con=null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con=ds.getConnection();
            st = con.prepareStatement("select ur.ID from t_user_role_rel ur,t_mrp_rel mr where ur.ID=? and mr.R_ID=? and ur.ROLE=mr.ROLE_ID and mr.type=0 ");
            st.setLong(1,userid);
            st.setLong(2,id);
            rs=st.executeQuery();
            if (rs.next())return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	JDBCHelper.closeRs(rs);
        	JDBCHelper.closeStmt(st);
            JDBCHelper.close(con);
        }
        return false;
    }
    

}
