package com.bdaim.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.entity.ChartConfig;
import com.bdaim.common.service.ChartConfigService;
import com.bdaim.common.util.Constant;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.dataexport.dao.DataExportApplyDao;
import com.bdaim.dataexport.entity.DataExportApply;
import com.bdaim.label.dao.LabelInfoDao;
import com.bdaim.label.service.CommonService;
import com.bdaim.label.service.LabelAuditService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexAction extends BasicAction {
    @Resource
    private CustomGroupService customGroupService;
    @Resource
    private LabelAuditService labelAuditService;
    @Resource
    private LabelInfoDao labelInfoDao;
    @Resource
    private DataExportApplyDao dataExportApplyDao;
    @Resource
    private ChartConfigService chartConfigService;
    @Resource 
    private CommonService commonService;

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("nav", "my-index");
        model.addAttribute("dir", "html/my-index");
        return "my-index";
    }

    @RequestMapping("/backend")
    public String backend(Model model) {
		model.addAttribute("nav", "index");
		model.addAttribute("dir", "index");
		return "index";
    }
    
    @RequestMapping("/system")
    public String label(Model model) {
        model.addAttribute("nav", "system");
        model.addAttribute("dir", "system/index");
        return "system/index";
    }
    
    @RequestMapping("/system/system_front")
    public String labelFront(Model model) {
        model.addAttribute("nav", "system");
        model.addAttribute("dir", "system/system_front");
        return "system/system_front";
    }

    @RequestMapping("/system/group")
    public String labelGroup(Model model) {
        model.addAttribute("nav", "system");
        model.addAttribute("dir", "system/group");
        return "system/group";
    }
    
    /** 创建行业标签池页 */
    @RequestMapping("/userGroup/create_pool")
    public String poolCreate(Model model) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("dir", "user/create_pool");
        return "userGroup/create_pool";
    }

    @RequestMapping("/process")
    public String process(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("process")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
                if (processChild.size() > 0) {
                    JSONObject data = processChild.getJSONObject(0);
                    model.addAttribute("nav", "process");
                    model.addAttribute("dir",
                            "process/" + data.getString("uri"));
                    model.addAttribute("currentPage", data.getString("uri"));
                    return "process/" + data.getString("uri");
                }
            }
        }
        return "process/apply";
    }

    @RequestMapping("/process/apply")
    public String processApply(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("process")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
                break;
            }
        }
        model.addAttribute("nav", "process");
        model.addAttribute("dir", "process/apply");
        model.addAttribute("currentPage", "apply");
        return "process/apply";
    }

    @RequestMapping("/process/approve")
    public String processApprove(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("process")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
                break;
            }
        }
        model.addAttribute("nav", "process");
        model.addAttribute("dir", "process/approve");
        model.addAttribute("currentPage", "approve");
        return "process/approve";
    }

    @RequestMapping("/process/develop")
    public String processDevelop(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("process")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
                break;
            }
        }
        model.addAttribute("nav", "process");
        model.addAttribute("dir", "process/develop");
        model.addAttribute("currentPage", "develop");
        return "process/develop";
    }

    @RequestMapping("/process/develop/base")
    public String processDevelop(Model model, Integer id) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("process")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
                break;
            }
        }
        Map<String, Object> detail = labelAuditService.getAuditDetailById(id);
        model.addAttribute("nav", "process");
        model.addAttribute("dir", "process/develop/base");
        model.addAttribute("currentPage", "develop/base");
        model.addAttribute("data", detail);
        return "process/develop/base";
    }

    @RequestMapping("/process/publish")
    public String processPublish(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("process")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
                break;
            }
        }
        model.addAttribute("nav", "process");
        model.addAttribute("dir", "process/publish");
        model.addAttribute("currentPage", "publish");
        return "process/publish";
    }

    @RequestMapping("/task")
    public String task(Model model) {
        model.addAttribute("nav", "task");
        model.addAttribute("dir", "task");
        List<Map<String, Object>> mapList = labelInfoDao
                .createQuery(
                        "select new map(id as id,labelName as labelName) from LabelInfo where level =1 and labelName!='组合标签'")
                .list();
        model.addAttribute("labels", mapList);
        return "task";
    }

    @RequestMapping("/report")
    public String report(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("report")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
                if (processChild.size() > 0) {
                    JSONObject data = processChild.getJSONObject(0);
                    model.addAttribute("nav", "report");
                    model.addAttribute("baseUrl", "report");
                    model.addAttribute("dir", "report/" + data.getString("uri"));
                    model.addAttribute("currentPage", data.getString("uri"));
                    return "report/" + data.getString("uri");
                }
            }
        }
        return "/";
    }

    @RequestMapping("/report/bqfx")
    public String bqfx(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("report")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
            }
        }
        model.addAttribute("nav", "report");
        model.addAttribute("baseUrl", "report");
        model.addAttribute("dir", "report/bqfx");
        model.addAttribute("currentPage", "bqfx");
        return "report/bqfx";
    }

    @RequestMapping("/report/process")
    public String reportProcess(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("report")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
            }
        }
        model.addAttribute("nav", "report");
        model.addAttribute("baseUrl", "report");
        model.addAttribute("dir", "report/process");
        model.addAttribute("currentPage", "process");
        return "report/process";
    }

    @RequestMapping("/report/apply")
    public String reportApply(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("report")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
            }
        }
        model.addAttribute("nav", "report");
        model.addAttribute("baseUrl", "report");
        model.addAttribute("dir", "report/apply");
        model.addAttribute("currentPage", "apply");
        return "report/apply";
    }

    @RequestMapping("/report/cover")
    public String reportCover(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("report")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
            }
        }
        model.addAttribute("nav", "report");
        model.addAttribute("baseUrl", "report");
        model.addAttribute("dir", "report/cover");
        model.addAttribute("currentPage", "cover");
        return "report/cover";
    }

    @RequestMapping("/userGroup")
    public String group(Model model) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("dir", "user/index");
        return "userGroup/index";
    }

    @RequestMapping("/userGroup/detail")
    public String groupDetail(Model model, Integer id, String name) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("dir", "user/detail");
        model.addAttribute("groupId", id);
        model.addAttribute("groupName", name);
        CustomGroup userGroup = customGroupService.getCustomGroupById(Integer
                .parseInt(request.getParameter("id")));
        model.addAttribute("userGroup", commonService.getCustomGroupMap(userGroup));
        return "userGroup/detail";
    }

    @RequestMapping("/userGroup/graph")
    public String groupGraph(Model model) {
        JSONArray authArray = (JSONArray) request.getSession().getAttribute(
                Constant.USER_AUTH_KEY);
        for (int i = 0; i < authArray.size(); i++) {
            JSONObject obj = authArray.getJSONObject(i);
            if (obj.getString("uri").equals("graph")) {
                String child = obj.getString("child");
                JSONArray processChild = JSONArray.parseArray(child);
                model.addAttribute("pages", processChild);
            }
        }
        model.addAttribute("nav", "graph");
        model.addAttribute("dir", "graph");
        model.addAttribute("currentPage", "graph");
        model.addAttribute("baseUrl", "userGroup");
        return "graph";
    }

    @RequestMapping("/userGroup/create_group")
    public String userGroupCreate(Model model) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("dir", "user/add");
        return "userGroup/create_group";
    }
    
    @RequestMapping("/userGroup/add")
    public String userGroupAdd(Model model) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("dir", "user/add");
        return "userGroup/add";
    }

    @RequestMapping("/userGroup/gids")
    public String gids(Model model, Integer id, String name) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("id", id);
        model.addAttribute("groupId", id);
        model.addAttribute("groupName", name);
        CustomGroup userGroup = customGroupService.getCustomGroupById(id);
        JSONObject obj = new JSONObject();
        obj.put("groupName", userGroup.getName());
        obj.put("cover", userGroup.getUserCount());
        String percent = String
                .valueOf(((float) userGroup.getUserCount() / (float) userGroup
                        .getTotal()) * 100);
        obj.put("percent", String.format("%.2f", Double.parseDouble(percent))
                + "%");
        model.addAttribute("userGroup", obj);
        model.addAttribute("dir", "user/gids");
        return "userGroup/gids";
    }

    @RequestMapping("/userGroup/finding")
    public String finding(Model model, Integer id, String name, String orderId) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("dir", "user/finding");
        model.addAttribute("groupId", id);
        model.addAttribute("groupName", name);
        model.addAttribute("orderId", orderId);
        return "userGroup/finding";
    }

    @RequestMapping("/userGroup/portrait")
    public String getMicoPic(Model model) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("dir", "user/portrait");

        Map map = request.getParameterMap();
        for (Object obj : map.keySet()) {
            String[] strs = (String[]) map.get(obj);
            model.addAttribute(obj.toString(), strs.length == 0 ? "" : strs[0]);
        }
        return "userGroup/portrait";
    }

    //搜索微观画像
    @RequestMapping("/graph/portrait")
    public String getGraph(Model model) {
        model.addAttribute("nav", "graph");
        model.addAttribute("dir", "graph/portrait");
        Map map = request.getParameterMap();
        for (Object obj : map.keySet()) {
            String[] strs = (String[]) map.get(obj);
            model.addAttribute(obj.toString(), strs.length == 0 ? "" : strs[0]);
        }
        //operation log
//        super.operlog(request, 0, "微观画像");

        return "graph/portrait";
    }

    @RequestMapping("/output")
    public String output(Model model) {
        List<DataExportApply> applys = dataExportApplyDao
                .createQuery(
                        "From DataExportApply where status = 3 and now() between startTime and endTime and applyUser.id=?",
                        opUser().getId()).list();
        if (null != applys && applys.size() > 0) {
            model.addAttribute("dataExport", true);
        }
        model.addAttribute("nav", "output");
        model.addAttribute("dir", "output/index");
        return "output/index";
    }

    @RequestMapping("/output/export")
    public String export(Model model) {
        model.addAttribute("nav", "output");
        model.addAttribute("dir", "output/export");
        return "output/export";
    }

    @RequestMapping("/permissions")
    public String permissions(Model model) {
        model.addAttribute("nav", "permissions");
        model.addAttribute("dir", "permissions/permissions");
        model.addAttribute("currentPage", "permissions");
        model.addAttribute("baseUrl", "permissions");
        return "permissions/permissions";
    }

    @RequestMapping("/account")
    public String account(Model model) {
        model.addAttribute("nav", "account");
        model.addAttribute("dir", "account");
        model.addAttribute("currentPage", "account");
        model.addAttribute("baseUrl", "account");
        return "account";
    }

    @RequestMapping("/reportMaker")
    public String reportMaker(Model model) {
        List<ChartConfig> chartList = chartConfigService.getAllChartConfig();
        if (chartList.size() > 0) {
            List<Map<String, Object>> chartRsult = new ArrayList<Map<String, Object>>();
            for (ChartConfig config : chartList) {
                Map<String, Object> chart = new HashMap<String, Object>();
                chart.put("id", config.getId());
                chart.put("title", config.getTitle());
                chart.put("name", config.getTitle());
                chart.put("content", JSONArray.parse(config.getContent()));
                chartRsult.add(chart);
            }
            model.addAttribute("pages", chartRsult);
        }
        model.addAttribute("nav", "reportMaker");
        model.addAttribute("dir", "reportMaker/index");

        return "reportMaker/index";
    }

    @RequestMapping("/reportMaker/add")
    public String reportMakerAdd(Model model, Integer id) {
        model.addAttribute("nav", "reportMaker");
        model.addAttribute("dir", "reportMaker/add");
        Map<String, Object> chart = new HashMap<String, Object>();
        chart.put("title", "");
        model.addAttribute("page", chart);
        return "reportMaker/add";
    }

    @RequestMapping("/reportMaker/edit")
    public String reportMakerEdit(Model model, Integer id) {
        ChartConfig config = chartConfigService.getChartConfigById(id);
        Map<String, Object> chart = new HashMap<String, Object>();
        chart.put("id", config.getId());
        chart.put("title", config.getTitle().split(","));
        chart.put("content", JSONArray.parse(config.getContent()));
        chart.put("showChannel", config.getShowChannel());
        chart.put("showCycle", config.getShowCycle());
        model.addAttribute("nav", "reportMaker");
        model.addAttribute("dir", "reportMaker/add");
        model.addAttribute("page", chart);
        return "reportMaker/add";
    }

    @RequestMapping("/acount")
    // TODO请求路径和mrp中保证一致后删除即可
    public String accountOne(Model model) {
        model.addAttribute("nav", "account");
        model.addAttribute("dir", "account");
        model.addAttribute("currentPage", "account");
        model.addAttribute("baseUrl", "account");
        return "account";
    }

    @RequestMapping("/macroPicture")
    public String macroPicture(Model model, Integer id) {
        List<Map<String, Object>> chartList = chartConfigService.getAllChartConfigTree();
        List<Map<String, Object>> channels = chartConfigService.getLabelChannel(opUser().getId());
        model.addAttribute("channels1", JSON.toJSONString(channels));
        model.addAttribute("channels2", channels);
        model.addAttribute("pages1", JSON.toJSONString(chartList));
        model.addAttribute("pages2", chartList);
        model.addAttribute("nav", "macroPicture");
        model.addAttribute("dir", "macroPicture");
        return "macroPicture";
    }

    //微观搜索
    @RequestMapping("/graph")
    public String graph(Model model, Integer id) {
        model.addAttribute("nav", "graph");
        model.addAttribute("dir", "graph");
        model.addAttribute("currentPage", "graph");
        model.addAttribute("baseUrl", "graph");
        model.addAttribute("event", "$event");
        //operation log
//        super.operlog(request, 0, "微观画像");
        return "graph";
    }

    //微观搜索
//	@RequestMapping("/operLog")
    @RequestMapping("/operateLog")
    public String operLog(Model model, Integer id) {
        model.addAttribute("nav", "operateLog");
        model.addAttribute("dir", "operateLog");
        model.addAttribute("currentPage", "operateLog");
        model.addAttribute("baseUrl", "operateLog");
        model.addAttribute("event", "$event");
        return "operateLog";
    }

    /**
     * 7
     * 根据洋河定制需求，修改本方法
     *
     * @param model:返回页面的request请求域对象
     * @param id:手动创建的用户群id
     * @param idList:广告id列表或者生命周期用户群(如导入期人群)标签值
     * @param idName:ad_places或lifeCycle
     * @param cycle:周期
     * @return
     */
    @RequestMapping("/userGroup/groupportrait")
    public String groupPortrait(Model model, Integer id, Integer cycle, String name, Integer groupStatus, String orderId) {
        model.addAttribute("nav", "userGroup");
        model.addAttribute("groupId", id);
        model.addAttribute("groupName", name);
        model.addAttribute("orderId", orderId);
        model.addAttribute("groupStatus", groupStatus);
        model.addAttribute("dir", "user/groupportrait");
        JSONObject userGroupObject = this.getCustomerGroupCovers(model, id, cycle);
        model.addAttribute("userGroup", userGroupObject);
        return "userGroup/groupportrait";
    }


    /**
     * 获取关于用户群的覆盖数
     *
     * @param model:返回页面的request请求域对象
     * @param id:手动创建的用户群id
     * @param idList:广告id列表或者生命周期用户群(如导入期人群)标签值
     * @param idName:ad_places或lifeCycle
     * @param cycle:周期
     * @return
     */
    private JSONObject getCustomerGroupCovers(Model model, Integer id, Integer cycle) {
        JSONObject userGroupObject = new JSONObject();

        // 若用户群id不为空，则直接查询MySQL获取已存在用户群的覆盖数，并计算覆盖百分比
        if (id != null) {
            model.addAttribute("id", id);
            CustomGroup userGroup = customGroupService.getCustomGroupById(id);
            userGroupObject.put("groupName", userGroup.getName());
            userGroupObject.put("cover", userGroup.getUserCount());

            String percent = String.valueOf(((float) userGroup.getUserCount() / (float) userGroup.getTotal()) * 100);
            userGroupObject.put("percent", String.format("%.2f", Double.parseDouble(percent)) + "%");
        }

        return userGroupObject;
    }


    public static void main(String args[]) {

    }
}
