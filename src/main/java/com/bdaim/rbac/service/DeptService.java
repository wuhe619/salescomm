package com.bdaim.rbac.service;

import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.*;
import com.bdaim.common.util.page.PageList;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.rbac.dao.DeptDao;
import com.bdaim.rbac.dto.DeptDTO;
import com.bdaim.rbac.entity.DeptEntity;
import com.bdaim.rbac.entity.RoleEntity;
import com.bdaim.rbac.vo.DeptInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigInteger;
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
    public Map<String, String> updateDeptMessage(DeptDTO deptDto, String optUserId) {
        Map<String, String> resultMap = new HashMap<>();
        String code = "1";
        try {
            String id = String.valueOf(deptDto.getId());
            logger.info("编辑部门id是" + id);
            //根据id查询是否存在,不存在新增操作\
            DeptEntity deptEntity = deptDao.getDeptEntityById(NumberConvertUtil.parseLong(id));
            if (deptEntity != null) {
                deptEntity.setName(deptDto.getName());
                deptEntity.setModifyTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                deptEntity.setOptuser(optUserId);
                deptDao.saveOrUpdate(deptEntity);
                resultMap.put("code", code);
                resultMap.put("message", "编辑成功");
            } else {
                //核验部门名字是否存在
                boolean flag = checkDeptName(deptDto.getName());
                if (!flag) {
                    deptEntity = new DeptEntity();
                    deptEntity.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                    deptEntity.setId(IDHelper.getTransactionId());
                    deptEntity.setType(1);
                    deptEntity.setOptuser(String.valueOf(optUserId));
                    deptEntity.setName(deptDto.getName());
                    deptDao.saveOrUpdate(deptEntity);
                    resultMap.put("code", code);
                    resultMap.put("message", "添加成功");
                } else {
                    resultMap.put("code", "0");
                    resultMap.put("message", "该部门已经存在");
                }
            }
        } catch (Exception e) {
            logger.error("编辑部门信息异常" + e);
            code = "0";
            resultMap.put("code", code);
            resultMap.put("message", "编辑失败");
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
        StringBuffer buffer = new StringBuffer("SELECT cast(t1.id as char) id,t1.create_time createTime,t1.modify_time,t1.name,COUNT(t2.id) roleNum ");
        buffer.append("FROM t_dept t1 LEFT JOIN t_role t2 ON t1.id=t2.deptid ")
                .append("GROUP BY t1.id,t1.create_time,t1.name,t1.modify_time ORDER BY t1.create_time DESC");
        logger.info("执行查询 " + buffer.toString());
        PageList page = new Pagination().getPageData(buffer.toString(), null, param, jdbcTemplate);
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
        try {
            if (StringUtil.isNotEmpty(deptId)) {
                List<RoleEntity> roleEntityList = deptDao.getRoleEntityList(Long.parseLong(deptId));
                if (roleEntityList.size() > 0) {
                    //有成员信息所以不能删除
                    resultMap.put("code", Constant.FAILURE_CODE);
                    resultMap.put("message", "该部门下有其他成员,暂时不能删除此部门");
                    return resultMap;
                }
            }
            int i = deptDao.deleteByDeptId(Long.parseLong(deptId));
            if (i > 0) {
                resultMap.put("code", Constant.SUCCESS_CODE);
                resultMap.put("message", "删除部门成功");
            } else {
                resultMap.put("code", Constant.FAILURE_CODE);
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

    public boolean add(DeptDTO dept) {
        try {
            dept.setId(IDHelper.getID());
            deptDao.insert(dept);
        } catch (Exception e) {
            logger.error("添加部门信息异常,",e);
            return false;
        } finally {

        }
        return true;
    }

    public boolean delete(DeptDTO dept) {
        try {
            if (!canDelete(dept.getId())) return false;
            deptDao.delete(dept);
        } catch (Exception e) {
            logger.error("删除部门信息异常,",e);
            return false;
        } finally {
        }
        return true;
    }

    private boolean canDelete(Long id) throws Exception {
        List list = deptDao.getSQLQuery("SELECT * FROM t_user where deptid=" + id).list();
        if (list.size() > 0) return false;
        else return true;
    }

    public boolean update(DeptDTO dept) {
        try {
            deptDao.update(dept);
        } catch (Exception e) {
            logger.error("更新部门信息异常,",e);
            return false;
        } finally {
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<DeptInfo> queryDept(Page page, String condition) {
        StringBuilder queryData = new StringBuilder();
        queryData.append(" SELECT d.ID , d.TYPE ,d.NAME , COUNT(distinct(u.id)) AS USERCOUNT, COUNT(distinct(r.id)) AS ROLECOUNT,d.MODIFY_TIME as MODIFY_TIME FROM t_dept d LEFT JOIN  t_user u ON d.ID = u.DEPTID and u.STATUS=0 LEFT JOIN  t_role r ON d.id = r.deptid");
        if (condition != null && !"".equals(condition)) {
            queryData.append(" where d.name like '%" + condition + "%' ");
        }
        queryData.append(" GROUP BY d.ID , d.TYPE , d.name , d.MODIFY_TIME");

        StringBuilder queryCount = new StringBuilder();
        queryCount.append("select count(*) as COUNT from (");

        queryCount.append(queryData);
        queryCount.append(") t1 ");
        String queryDataSql = queryData.toString();
        String queryCountSql = queryCount.toString();
        List<Map<String, Object>> list = deptDao.sqlQuery(queryDataSql, new Page(page.getPageIndex(), page.getCountPerPage()));
        List<DeptInfo> vos = null;
        if (list != null && !list.isEmpty()) {
            vos = new ArrayList<DeptInfo>();
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                //int type = (Integer)map.get("type");
                String name = (String) map.get("NAME");
                int usercount = NumberConvertUtil.everythingToInt(map.get("USERCOUNT"));
                int rolecount = NumberConvertUtil.everythingToInt(map.get("ROLECOUNT"));
                Date modifyTime = (Date) map.get("MODIFY_TIME");
                DeptInfo info = new DeptInfo();
                info.setId(id);
                info.setName(name);
                info.setRoleNum(rolecount);
                info.setUserNum(usercount);
                info.setModifyTime(DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", modifyTime));
                //info.setSource(DataFromEnum.getNameByValue(type));
                vos.add(info);
            }
        }
        List<Map<String, Object>> list1 = deptDao.sqlQuery(queryCountSql);
        if (list1 != null && !list.isEmpty()) {
            Map<String, Object> map = list1.get(0);
            int count = NumberConvertUtil.everythingToInt(map.get("COUNT"));
            page.setCount(count);
        }
        return vos;
    }

    public List<DeptInfo> queryDeptV1(Page page, String condition) {
        StringBuilder queryData = new StringBuilder();
        queryData.append(" SELECT d.ID , d.TYPE ,d.NAME , COUNT(distinct(u.id)) AS USERCOUNT, COUNT(distinct(r.id)) AS ROLECOUNT,d.MODIFY_TIME as MODIFY_TIME,d.CREATE_TIME AS CREATE_TIME FROM t_dept d LEFT JOIN  t_user u ON d.ID = u.DEPTID and u.STATUS=0 LEFT JOIN  t_role r ON d.id = r.deptid");
        if (condition != null && !"".equals(condition)) {
            queryData.append(" where d.name like '%" + condition + "%' ");
        }
        queryData.append(" GROUP BY d.ID , d.TYPE , d.name , d.MODIFY_TIME");
        queryData.append(" ORDER BY d.CREATE_TIME DESC ");
        String queryDataSql = queryData.toString();
        int countPerpage = page.getCountPerPage();
        int index = page.getPageIndex();
        int start = index * countPerpage;
        Page pageData = deptDao.sqlPageQuery(queryDataSql, start, countPerpage);
        List<DeptInfo> vos = null;
        if (pageData.getData() != null) {
            vos = new ArrayList<>();
            Map<String, Object> map;
            for (int i = 0; i < pageData.getData().size(); i++) {
                map = (Map<String, Object>) pageData.getData().get(i);
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                String name = (String) map.get("NAME");
                int usercount = NumberConvertUtil.everythingToInt(map.get("USERCOUNT"));
                int rolecount = NumberConvertUtil.everythingToInt(map.get("ROLECOUNT"));
                Date modifyTime = (Date) map.get("MODIFY_TIME");
                Date createTime = (Date) map.get("CREATE_TIME");
                DeptInfo info = new DeptInfo();
                info.setId(id);
                info.setName(name);
                info.setRoleNum(rolecount);
                info.setUserNum(usercount);
                if (createTime != null) {
                    info.setModifyTime(DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", createTime));
                } else if (modifyTime != null) {
                    info.setModifyTime(DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", modifyTime));
                }
                vos.add(info);
            }
        }
        page.setCount(pageData.getTotal());
        return vos;
    }

    @SuppressWarnings("unchecked")
    public List<DeptDTO> queryDept() {
        String sql = "select id as \"id\",name as \"name\" from t_dept ";
        List<Map<String, Object>> list = deptDao.sqlQuery(sql);
        List<DeptDTO> depts = null;
        if (list != null && !list.isEmpty()) {
            depts = new ArrayList<DeptDTO>();
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("id"));
                String name = (String) map.get("name");
                DeptDTO dept = new DeptDTO();
                dept.setId(id);
                dept.setName(name);
                depts.add(dept);
            }
        }
        return depts;
    }

    public List<DeptInfo> queryAll() {
        StringBuilder builder = new StringBuilder();
        builder.append(" select t.id,t.usercount,t.TYPE,t.name,count(r.id) as rolecount from");
        builder.append(" (select d.ID ,d.TYPE,d.name,count(u.id) as usercount from t_dept d left join t_user u on d.ID = u.DEPTID group by d.ID,d.TYPE,d.name) t");
        builder.append(" left join t_role r on t.id = r.deptid group by t.id,t.usercount,t.TYPE,t.name");
        String sql = builder.toString();
        List<Map<String, Object>> list = deptDao.sqlQuery(sql);
        List<DeptInfo> vos = null;
        if (list != null && !list.isEmpty()) {
            vos = new ArrayList<DeptInfo>();
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("id"));
                //int type = (Integer)map.get("type");
                String name = (String) map.get("name");
                int usercount = ((BigInteger) map.get("usercount")).intValue();
                int rolecount = ((BigInteger) map.get("rolecount")).intValue();
                DeptInfo info = new DeptInfo();
                info.setId(id);
                info.setName(name);
                info.setRoleNum(rolecount);
                info.setUserNum(usercount);
                //info.setSource(DataFromEnum.getNameByValue(type));
                vos.add(info);
            }
        }
        return vos;
    }

    @SuppressWarnings("unchecked")
    public DeptInfo queryDeptById(Long id) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT d.ID , d.TYPE ,d.NAME , COUNT(u.id) AS USERCOUNT, COUNT(r.id) AS ROLECOUNT ");
        builder.append("FROM t_dept d LEFT JOIN  t_user u ON d.ID = u.DEPTID LEFT JOIN  t_role r ON d.id = r.deptid ");
        builder.append("where d.id=").append(id).append(" GROUP BY d.ID , d.TYPE , d.name ");
        String sql = builder.toString();
        List<Map<String, Object>> list = deptDao.sqlQuery(sql);
        DeptInfo info = null;
        if (list != null && !list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            id = NumberConvertUtil.everythingToLong(map.get("ID"));
            String name = (String) map.get("NAME");
            int usercount = NumberConvertUtil.everythingToInt(map.get("USERCOUNT"));
            int rolecount = NumberConvertUtil.everythingToInt(map.get("ROLECOUNT"));
            info = new DeptInfo();
            info.setId(id);
            info.setName(name);
            info.setRoleNum(rolecount);
            info.setUserNum(usercount);
        }
        return info;
    }

    @SuppressWarnings("unchecked")
    public boolean checkDeptName(String deptName, Long id) {
        String sql = "select * from t_dept where name = '" + deptName + "'";
        if (id != null) {
            sql += " and id <> '" + id + "'";
        }
        List<Map<String, Object>> list = deptDao.sqlQuery(sql);
        if (list != null && !list.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
