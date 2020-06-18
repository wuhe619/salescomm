package com.bdaim.resource.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.api.service.ApiService;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ActionStates;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.page.PageList;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customs.services.ExportExcelService;
import com.bdaim.rbac.dto.AbstractTreeResource;
import com.bdaim.rbac.dto.CommonTreeResource;
import com.bdaim.rbac.service.ResourceService;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.resource.util.ResourceTypeHelper;
import com.bdaim.resource.util.TreeJsonFormat;
import com.bdaim.util.FileUtil;
import com.bdaim.util.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONFunction;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
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

    @Autowired
    ApiService apiService;

    @Autowired
    private ExportExcelService exportExcelService;

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
        //ModelAndView view = new ModelAndView("index.jsp");
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
                resp.setData(marketResourceService.updateMarketResource(info.getString("_c_"), info.getString("name"), info.getInteger("supplierId"), info.getString("salePrice"), info.getInteger("type"), resourceId));
            }
        } catch (Exception e) {
            resp.setMessage(e.getMessage());
            resp.setCode(-1);
        }
//    	OperlogAppender.operlog(request, user, this.pageName, -1);

        return resp;
    }

    @PostMapping(value = "/infos")
    public ResponseInfo listResource(@RequestBody com.alibaba.fastjson.JSONObject param) {
        ResponseInfo resp = new ResponseInfo();
        PageParam page = new PageParam();
        try {
            int pageNum = 0;
            int pageSize = 10;
            if (param.containsKey("pageNum"))
                pageNum = param.getInteger("pageNum");
            if (param.containsKey("pageSize"))
                pageSize = param.getInteger("pageSize");
            page.setPageNum(pageNum);
            page.setPageSize(pageSize);
            resp.setData(marketResourceService.listResource1(page, param));
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

    /**
     * 资源调用统计
     * @param params
     * @return
     */
    @PostMapping("/logs")
    public ResponseInfo subApiLogs(@RequestBody com.alibaba.fastjson.JSONObject params) {
        ResponseInfo resp = new ResponseInfo();
        PageParam page = new PageParam();
        if (StringUtil.isEmpty(params.getString("callMonth"))) {
            return new ResponseInfoAssemble().failure(-1, "查询时间不能为空");
        }
        page.setPageSize(params.containsKey("pageSize") ?   params.getIntValue("pageSize"):10);
        page.setPageNum(params.containsKey("pageNum") ?  params.getIntValue("pageNum"):0);
        if (params.containsKey("type") && "sub".equals(params.getString("type"))) {
            resp.setData(apiService.subApiLogs(params, page));
        } else {
            resp.setData(apiService.resApiLogs(params, page));
        }

        return resp;
    }

    /**
     * 资源调用记录详情
     * @param rsId
     * @param params
     * @return
     */
    @PostMapping("/logs/{rsId}")
    public ResponseInfo subApiLogDetail(@PathVariable("rsId")String rsId,@RequestBody com.alibaba.fastjson.JSONObject params) {
        ResponseInfo resp = new ResponseInfo();
        PageParam page = new PageParam();
        if (StringUtil.isEmpty(params.getString("callMonth"))) {
            return new ResponseInfoAssemble().failure(-1, "查询时间不能为空");
        }
        if(StringUtil.isEmpty(rsId)){
           return new ResponseInfoAssemble().failure(-1, "rsId 参数错误");
        }
        page.setPageSize(params.containsKey("pageSize") ?   params.getIntValue("pageSize"):10);
        page.setPageNum(params.containsKey("pageNum") ?  params.getIntValue("pageNum"):0);
        params.put("rsId",rsId);

        if (params.containsKey("type") && "sub".equals(params.getString("type"))) {
            resp.setData(apiService.subApiLogs(params, page));
        } else {
            resp.setData(apiService.resApiLogDetail(params, page));
        }
        return resp;
    }

//    public static void main(String[] args) {
//        doInfo("201912","sub,","_export_sub_api_logs");
//    }
    /*
    导出
     */
    @GetMapping(value = "/logs")
    public ResponseInfo doInfo(String callMonth, String type, String _rule_, HttpServletResponse response) {
        ResponseInfo resp = new ResponseInfo();
        com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
        PageParam page = new PageParam();
        if (StringUtil.isEmpty(callMonth)) {
            return new ResponseInfoAssemble().failure(-1, "查询时间不能为空");
        }
        page.setPageSize(0);
        page.setPageNum(10000000);
        params.put("type", type);
        params.put("callMonth", callMonth);
        params.put("_rule_", _rule_);
        try {
            PageList pageList = null;
            if (StringUtil.isNotEmpty(type) && "sub".equals(type)) {
                pageList = apiService.subApiLogs(params, page);
            } else {
               // pageList = apiService.resApiLogs(params, page);
            }
            if (pageList.getList().size() == 0) {
                exportExcelService.exportExcel(0, new ArrayList<>(), params, response);
            }
            exportExcelService.exportExcel(0, pageList.getList(), params, response);
            resp.setData(pageList);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }


    @PostMapping("/getPersentByApi")
    public ResponseInfo getPersentByApi(@RequestBody com.alibaba.fastjson.JSONObject jsonObject){
        logger.info("map"+jsonObject.toString());
       ResponseInfo responseInfo=new ResponseInfo();
       responseInfo.setCode(200);
        apiService.getPersentByApi(jsonObject);
        responseInfo.setData(jsonObject);
         return  responseInfo;
    }

    @PostMapping("/updatePercent")
    public ResponseInfo updatePercent(@RequestBody Map map){
        apiService.updatePercent(map);
        ResponseInfo responseInfo=new ResponseInfo();
        responseInfo.setCode(200);
        return responseInfo;
    }
}

