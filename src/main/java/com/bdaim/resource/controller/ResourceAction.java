package com.bdaim.resource.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ActionStates;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.rbac.dto.AbstractTreeResource;
import com.bdaim.rbac.dto.CommonTreeResource;
import com.bdaim.rbac.service.ResourceService;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.resource.util.ResourceTypeHelper;
import com.bdaim.resource.util.TreeJsonFormat;
import com.bdaim.util.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/8/6
 * @description
 */
@RestController
@RequestMapping("/resource")
public class ResourceAction extends BasicAction {
    @Resource
    MarketResourceService marketResourceService;
    public static final Logger logger = LoggerFactory.getLogger(ResourceAction.class);

    /**
     * @Title:
     * @Description: 根据资源类型查询供应商信息和资源信息
     */
    @RequestMapping(value = "/getResource", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo getResourceInfoByType(String type, String supplierId) {
        try {
            List<Map<String, Object>> list = marketResourceService.getResourceInfoByType(type, supplierId);
            return new ResponseInfoAssemble().success(list);
        } catch (Exception e) {
            logger.error("查询资源信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询资源信息失败");
        }
    }

    @Resource
    private ResourceService resourceService;

    @RequestMapping(value = "/queryalltree.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryAllTree(HttpServletRequest request) {
        CommonTreeResource resource = new CommonTreeResource();
        resource.setID(0L);

        JSONArray json = TreeJsonFormat.format(resourceService.queryAllTree(resource, null));
        //OPERATION LOGS
//        OperlogAppender.operlog(request, pageName, -1);

        if (json == null) return "";
        else return json.toString();
    }

    @RequestMapping(value = "/queryotree.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryOTree(HttpServletRequest request) {
        CommonTreeResource resource = new CommonTreeResource();
        resource.setID(0L);
        JSONArray data = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("id", "0");
        obj.put("text", "根目录");
        AbstractTreeResource treeResource = resourceService.queryAllTree(resource, null);
        //OPERATION LOGS
//        OperlogAppender.operlog(pageName, -1);

        JSONArray json = null;
        if (treeResource != null && (json = TreeJsonFormat.format(treeResource)) != null) {
            obj.put("children", json.toString());
        }
        data.add(obj);
        return data.toString();
    }

    @RequestMapping(value = "/del.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String del(HttpServletRequest request, long id) {
        boolean ret = resourceService.del(id);
        //OPERATION LOGS
//        OperlogAppender.operlog(request, pageName, id);
        if (ret) {
            return ActionStates.SUCCESS_JSON.toString();
        } else {
            return ActionStates.FAIL_JSON.toString();
        }
    }

    @RequestMapping("/add.do")
    public ModelAndView add(HttpServletRequest request, @RequestParam long pid) {
        ModelAndView view = new ModelAndView("add.jsp");
        view.addObject("pid", pid);
        view.addObject("resourcelist", ResourceTypeHelper.getResourceList());
        return view;
    }

    @RequestMapping("/save.do")
    public ModelAndView save(@RequestParam String name, @RequestParam long pid, @RequestParam String type, @RequestParam String uri, @RequestParam(required = false) String comments, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("index.jsp");
//        UserManager manager= new UserManager();
        LoginUser user = opUser();
        CommonTreeResource resource = new CommonTreeResource();
        resource.setUser(user.getName());
        resource.setName(name);
        resource.setType(type);
        resource.setPid(pid);
        resource.setRemark(comments);
        resource.setUri(uri);
        CommonTreeResource rs = resourceService.save(resource);
        if (rs == null) {
            view.addObject("resultinfo", "保存失败");
        } else {
            view.addObject("data", rs);
            view.addObject("resultinfo", "保存成功");
        }

//    	OperlogAppender.operlog(request, user, this.pageName, -1);

        return view;
    }

    @RequestMapping("/modify.do")
    public ModelAndView modify(@RequestParam long id) {
        ModelAndView view = new ModelAndView("edit.jsp");
        if (id == 0) view.setViewName("index.jsp");
        else {
            view.addObject("data", resourceService.getResource(id));
            view.addObject("resourcelist", ResourceTypeHelper.getResourceList());
        }
        return view;
    }

    @RequestMapping("/update.do")
    public ModelAndView update(@RequestParam String name, @RequestParam long id, @RequestParam String type, @RequestParam String uri, @RequestParam(required = false) String comments, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("edit.jsp");

        LoginUser user = opUser();
        CommonTreeResource resource = new CommonTreeResource(id);
        resource.setName(name);
        resource.setRemark(comments);
        resource.setUser(user.getName());
        resource.setType(type);
        resource.setUri(uri);
        try {
            resourceService.update(resource);
            view.addObject("resultinfo", "保存失败");
        } catch (Exception e) {
            view.addObject("resultinfo", "保存成功");
            view.addObject("data", resource);
            view.addObject("resourcelist", ResourceTypeHelper.getResourceList());
        }

        //OPERATION LOGS
//        OperlogAppender.operlog(request, pageName, id);

        return view;
    }

    @PostMapping(value = "/all")
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String listResource1(@RequestBody com.alibaba.fastjson.JSONObject param) {
        List<MarketResourceDTO> marketResourceDTOList = null;
        try {
            marketResourceDTOList = marketResourceService.listResource(opUser().getCustId(), param);
        } catch (Exception e) {
            logger.error("查询资源列表失败,", e);
        }
        return returnJsonData(marketResourceDTOList);
    }

    @PostMapping("/info/{resourceId}")
    public ResponseInfo save1(@RequestBody(required = false) String RequestBody, @PathVariable(name = "resourceId") Integer resourceId) {
        ModelAndView view = new ModelAndView("index.jsp");
        ResponseInfo resp = new ResponseInfo();
        com.alibaba.fastjson.JSONObject info = null;
        try {
            if (RequestBody == null || "".equals(RequestBody))
                RequestBody = "{}";

            info = com.alibaba.fastjson.JSONObject.parseObject(RequestBody);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "记录解析异常:[" + RequestBody + "]");
        }
        if (StringUtil.isEmpty(info.getString("name"))) return new ResponseInfoAssemble().failure(-1, "资源名称不能为空");
        if (info.getInteger("supplierId") == null || info.getInteger("supplierId") == 0)
            return new ResponseInfoAssemble().failure(-1, "供应商不能为空");
        if (info.getInteger("type") == null || info.getInteger("type") == 0)
            return new ResponseInfoAssemble().failure(-1, "资源类型不能为空");
        try {
            if (resourceId == null || resourceId == 0) {
                resp.setData(marketResourceService.saveMarketResource(info.getString("name"), info.getInteger("supplierId"), info.getString("salePrice"), info.getInteger("type")));
            } else {
                resp.setData(marketResourceService.updateMarketResource(info.getString("name"), info.getInteger("supplierId"), info.getString("salePrice"), info.getInteger("type"), resourceId));
            }
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "资源更新失败");
        }
//    	OperlogAppender.operlog(request, user, this.pageName, -1);

        return resp;
    }

    @PostMapping(value = "/infos")
    public ResponseInfo listResource(@RequestBody com.alibaba.fastjson.JSONObject param) {
        ResponseInfo resp = new ResponseInfo();
        try {
            resp.setData(marketResourceService.listResource1(opUser().getCustId(), param));
        } catch (Exception e) {
            logger.error("查询资源列表失败,", e);
        }
        return resp;
    }

    @GetMapping(value = "/info/{id}")
    public ResponseInfo getResourceById(@PathVariable(name = "id") Integer id) {
        ResponseInfo resp = new ResponseInfo();
        if (id == null || id == 0) {
            return new ResponseInfoAssemble().failure(-1, "资源类型不能为空");
        }
        try {
            resp.setData(marketResourceService.getResourceById(id));
        } catch (Exception e) {
            logger.error("查询资源列表失败,", e);
            return new ResponseInfoAssemble().failure(-1, "查询资源列表失败");
        }
        return resp;
    }
}
