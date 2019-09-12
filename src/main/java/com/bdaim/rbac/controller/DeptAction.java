package com.bdaim.rbac.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.util.StringUtil;
import com.bdaim.rbac.DataFromEnum;
import com.bdaim.rbac.dto.DeptDTO;
import com.bdaim.rbac.service.DeptService;
import com.bdaim.rbac.vo.DeptInfo;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/3/15
 * @description
 */
@Controller
@RequestMapping("/dept")
public class DeptAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(DeptAction.class);
    @Resource
    private DeptService deptService;

    /**
     * 编辑部门信息(部门名称)
     *
     * @param deptDto id,name
     * @return
     * @auther Chacker
     * @date
     */
    @RequestMapping(value = "/editDeptMessage", method = RequestMethod.POST)
    @ResponseBody
    public Object editDeptMessage(@RequestBody DeptDTO deptDto) {
        Map<String, String> resultMap = new HashMap<>();
        //获取当前操作人
        LoginUser lu = opUser();
        String optUser = lu.getUsername();
        resultMap = deptService.updateDeptMessage(deptDto, optUser);
        return JSONObject.toJSON(resultMap);
    }


    /**
     * 删除部门信息
     */
    @RequestMapping(value = "/delDept", method = RequestMethod.GET)
    @ResponseBody
    public Object delDeptMessage(String deptId) {
        Map<String, String> resultMap = deptService.delDeptMessage(deptId);
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 部门列表信息查询
     *
     * @param map page_num、page_size
     * @return
     * @auther Chacker
     * @date
     */
    @RequestMapping(value = "/queryDeptList", method = RequestMethod.GET)
    @ResponseBody
    public String queryDeptList(@RequestParam Map<String, Object> map) {
        String pageNum = String.valueOf(map.get("page_num"));
        if (StringUtil.isEmpty(pageNum)) {
            return "请传入分页参数";
        }
        Map<String, Object> resultMap = null;
        try {
            resultMap = deptService.queryDeptList(map);
        } catch (Exception e) {
            logger.error("查询部门列表信息异常" + e);
        }
        return JSON.toJSONString(resultMap);
    }
    /**
     * 获取部门信息以及部门下职位信息
     */
    @RequestMapping(value = "/getDeptAndRoles", method = RequestMethod.GET)
    @ResponseBody
    public Object getDeptAndRoles( String deptId) {
        List<Map<String, Object>> deptAndRoles = null;
        try {
            deptAndRoles = deptService.getDeptAndRoles(deptId);
        } catch (Exception e) {
            logger.error("查询部门信息以及职位信息异常" + e);
        }
        return JSONObject.toJSON(deptAndRoles);
    }

    private String pageName = "部门管理";

    @RequestMapping(value = "/query.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String index() {
        List<DeptInfo> vos = deptService.queryAll();
        JSONArray array = JSONArray.fromObject(vos);
        return array.toString();
    }

    @RequestMapping(value = "/del.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String del(HttpServletRequest request, long id) {
        DeptDTO dept = new DeptDTO();
        dept.setId(id);
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        if (deptService.delete(dept)) {
            result.put("result", true);
        } else {
            result.put("result", false);
            result.put("msg", "现在的部门正在被使用中，无法被删除");
        }
//        if (null != dept.getId())
//        	OperlogAppender.operlog(request, this.pageName, dept.getId());

        return result.toString();
    }

    /**
     * 保存部门信息，用于新增和更新保存
     *
     * @param name
     * @param id
     * @return
     */
    @RequestMapping("/save.do")
    @ResponseBody
    public String save(HttpServletRequest request, @RequestParam String name, @RequestParam(required = false) Long id) {
        /*try {
			name = new String(name.getBytes("ISO-8859-1"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        //校验用户名是否唯一
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        boolean unique = checkDeptName(name, id);
        if (!unique) {
            result.put("result", false);
            result.put("code", 1);
            return returnJsonData(result);
        }
        DeptDTO dept = new DeptDTO();
        dept.setName(name);
        LoginUser user= opUser();
        dept.setOptuser(user.getName());
        dept.setType(DataFromEnum.SYSTEM.getValue());
        boolean flag = false;
        if (id == null) {
            dept.setCreateTime(new Date());
            flag = deptService.add(dept);
        } else {
            dept.setId(id);
            dept.setModifyTime(new Date());
            flag = deptService.update(dept);
        }

        if (flag) {
            id = dept.getId();
            result.put("result", true);
            DeptInfo info = deptService.queryDeptById(id);
            result.put("data", net.sf.json.JSONObject.fromObject(info));
        } else {
            result.put("result", false);
            result.put("code", 2);
        }
//        if (null != dept.getId())
//        	OperlogAppender.operlog(request, this.pageName, dept.getId());

        return returnJsonData(result);
    }

    /**
     * 分页查询部门信息
     *
     * @param pageIndex
     * @param countPerPage
     * @return
     */
    @RequestMapping(value = "/queryDept.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryDept(HttpServletRequest request, @RequestParam(required = false) Integer pageIndex, @RequestParam int countPerPage, @RequestParam(required = false) String condition) {
        Page page = new Page();
        if (pageIndex == null) {
            page.setPageIndex(0);
        } else {
            page.setPageIndex(pageIndex);
        }
        page.setCountPerPage(countPerPage);
        JSONArray array = null;
        List<DeptInfo> vos = deptService.queryDeptV1(page, condition);
        if (vos != null) array = JSONArray.fromObject(vos);
        net.sf.json.JSONObject object = new net.sf.json.JSONObject();
        object.put("count", page.getCount());
        if (array == null || array.size() == 0) {
            object.put("data", "");
        } else {
            object.put("data", array);
        }

//        UserDTO user = OperlogAppender.getUser(request);
//    	OperlogAppender.operlog(request, user, this.pageName, -1);

        return returnJsonData(object);
    }

    /**
     * 检查部门名称是否唯一
     *
     * @param deptName
     * @param id
     * @return
     */
    public boolean checkDeptName(String deptName, Long id) {
        boolean flag = deptService.checkDeptName(deptName, id);
        return flag;
    }

}
