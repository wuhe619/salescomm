package com.bdaim.dataexport.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.dataexport.entity.DataExport;
import com.bdaim.dataexport.service.DataExportService;
import com.bdaim.label.service.LabelInterfaceService;
import com.bdaim.util.Constant;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/dataExport")
public class DataExportAction extends BasicAction {

    private static Logger log = LoggerFactory.getLogger(DataExportAction.class);

    @Resource
    private DataExportService dataExportService;
    @Resource
    private LabelInterfaceService labelInterfaceService;
    @Resource
    private CustomGroupService customGroupService;

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/addDataExport")
    public synchronized String addDataExport(DataExport dataExport, @RequestParam(required = false) MultipartFile gid) {
        Map<String, Object> rstMap = new HashMap<String, Object>();
        String fileName = "";
        try {
            Map<String, File> is = new HashMap<String, File>();
            Map<String, String> texts = new HashMap<String, String>();
            dataExport.setTaskNum(getTaskNum());
            if (dataExport.getExportType().equals(Constant.DATA_EXPORT_GROUP_TYPE)) {
                //Map<String, Object> map1 = new HashMap<String, Object>();
                JSONObject map1 = new JSONObject();
                if (gid != null)
                    map1.put("filename", gid.isEmpty() ? "" : gid.getOriginalFilename());
                map1.put("cid", dataExport.getCid() == null ? "" : dataExport.getCid());
                map1.put("outputLabelID", getLabelTree(dataExport.getOutputLabel()));
                map1.put("starttime", DateUtils.formatDate(dataExport.getStartTime(), "yyyy-MM-dd"));
                map1.put("endtime", DateUtils.formatDate(dataExport.getEndTime(), "yyyy-MM-dd"));
                //TreeMap<String, Object> map2 = new TreeMap<String, Object>();
                JSONObject map2 = new JSONObject();
                map2.put("jobID", dataExport.getTaskNum());
                map2.put("user", opUser().getId().toString());
                map2.put("aim", Constant.DATA_EXPORT_AIMS.get(dataExport.getExportReason()));
                map2.put("filters", map1);
                texts.put("data", JSON.toJSONString(map2));

                if (gid != null && !gid.isEmpty()) {
                    String path = request.getSession().getServletContext().getRealPath("/uploadGID");
                    File fileDir = new File(path);
                    if (!fileDir.exists()) {
                        fileDir.mkdirs();
                    }
                    fileName = path + "/gid" + new Date().getTime() + ".txt";
                    gid.transferTo(new File(fileName));
                    is.put("file", new File(fileName));
                }
                String ret = "";
                if (dataExport.getExportType().equals(Constant.DATA_EXPORT_CID_TYPE)) {
                    ret = labelInterfaceService.downloadByCidJobSubmission(JSONObject.fromObject(map2));
                } else {
                    ret = labelInterfaceService.downloadByGidJobSubmission("file", is, texts);
                }
                JSONObject jsonObj = JSONObject.fromObject(ret);
                if (!jsonObj.isEmpty())
                    dataExport.setPath(jsonObj.getString("path"));
            } else {
                Integer customerGroupId = dataExport.getCustomGroupId();
                List<JSONObject> terms = getTerms(customerGroupId);
                if (1 == dataExport.getDataType()) {//数据类型  用户画像数据

                    Map<String, Object> mapFilter = new HashMap<String, Object>();
                    mapFilter.put("outputLabelID", getLabelTree(dataExport.getOutputLabel()));
                    Map<String, Object> map1 = new HashMap<String, Object>();
                    map1.put("terms", terms);
                    map1.put("filters", mapFilter);
                    map1.put("jobID", dataExport.getTaskNum());
                    //map1.put("user", user.get().getId());

                    String ret = labelInterfaceService.downloadByGroupJobSubmission(JSONObject.fromObject(map1));
                    JSONObject jsonObj = JSONObject.fromObject(ret);
                    if (!jsonObj.isEmpty())
                        dataExport.setPath(jsonObj.getString("path"));
                }
                if (0 == dataExport.getDataType()) {  // 数据类型   gid
                    JSONObject map1 = new JSONObject();
                    map1.put("jobID", dataExport.getTaskNum());
                    map1.put("terms", terms);
                    String ret = labelInterfaceService.downloadByGroupGidSubmission(JSONObject.fromObject(map1));
                    JSONObject jsonObj = JSONObject.fromObject(ret);
                    if (!jsonObj.isEmpty())
                        dataExport.setPath(jsonObj.getString("path"));
                }

            }

            dataExport.setApplicant(opUser().getId());
            dataExportService.addDataExport(dataExport);
            rstMap.put("_message", "导数成功！");
            rstMap.put("isSuccess", 1);
        } catch (Exception e) {
            e.printStackTrace();
            rstMap.put("isSuccess", 0);
            if (e instanceof ResourceAccessException) {
                rstMap.put("_message", "远程服务器不可用");
            } else {
                rstMap.put("_message", "导数失败！");
            }
        } finally {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        }
        return JSON.toJSONString(rstMap);
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getDataExportList")
    public String getDataExportList(DataExport dataExport, Page page) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> lst = dataExportService.getDataExportList(dataExport, page, opUser().getUser());
        for (int i = 0; i < lst.size(); i++) {
            Map<String, Object> map = lst.get(i);
            Object taskNum = map.get("taskNum");
            String status = "--";
            try {
                status = labelInterfaceService.getDownloadStatusById(taskNum.toString());
                lst.get(i).put("status", JSONObject.fromObject(status).get("status"));
            } catch (Exception e) {
                //e.printStackTrace();
                lst.get(i).put("status", "--");
            }
            lst.get(i).put("path", lst.get(i).get("path") + "&authKey=" + generAuthKey(taskNum.toString()));
        }
        Long total = dataExportService.getDataExportListTotal(dataExport, opUser().getUser());
        resultMap.put("stores", lst);
        resultMap.put("total", total);
        return JSON.toJSONString(resultMap);
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getDataExportById")
    public String getDataExportById(Integer id) {
        Map<String, Object> map = dataExportService.getDataExportById(id);
        return JSON.toJSONString(map);
    }

    /**
     * TODO 不再使用
     */
    @RequestMapping(value = "/salesCommCustomerGroupExport", method = RequestMethod.GET)
    public void testCustomerGroupExport(HttpServletResponse response, String startTime, String endTime) {
        try {
            dataExportService.downloadCustomerGroupExcelData(response, opUser().getCustId(), null, "邀约状态", "成功",
                    null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO 不再使用
     */
    @RequestMapping(value = "/salesCommCustomerGroupExport0", method = RequestMethod.GET)
    public void testCustomerGroupExport0(HttpServletResponse response, String startTime, String endTime) {
        try {
            dataExportService.downloadCustomerGroupExcelData0(response, opUser().getCustId(), null, "邀约状态", "成功",
                    startTime, endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO 不再使用
     */
    @RequestMapping(value = "/salesCommCustomerGroupExport1", method = RequestMethod.GET)
    public void testCustomerGroupExport1(HttpServletResponse response, String startTime, String endTime) {
        try {
            dataExportService.downloadCustomerGroupExcelData1(response, opUser().getCustId(), null, "邀约状态", "成功",
                    startTime, endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/salesCommCustomerGroupExport_V1", method = RequestMethod.GET)
    @CacheAnnotation
    public void salesCommCustomerGroupExport_V1(HttpServletResponse response, String startTime, String endTime) {
        response.setContentType("application/json;charset=utf-8");
        boolean status = opUser().getUsername().indexOf("xzcc") != -1 || opUser().getUsername().indexOf("yunxun") != -1;
        if (status && "1".equals(opUser().getUserType())) {
            dataExportService.exportCustomerMarketDataToExcelV3(response, opUser().getCustId(), null, "邀约状态", "成功",
                    startTime, endTime);
        } else if (dataExportService.checkCustExportPermission(opUser().getCustId()) && "1".equals(opUser().getUserType())) {
            dataExportService.exportCustomerMarketDataToExcelV3(response, opUser().getCustId(), null, "邀约状态", "成功",
                    startTime, endTime);
        } else {
            log.warn(opUser().getUsername() + "非法请求接口");
            try {
                response.getOutputStream().write("{\"msg\":\"非法请求接口\"}".getBytes("UTF-8"));
            } catch (IOException e) {
                log.error(opUser().getUsername() + "非法请求接口异常,", e);
            }
        }

    }

    @PostMapping(value = "/exportCustData")
    public void exportCustData(HttpServletResponse response, String startTime, String endTime, String value) {
        response.setContentType("application/json;charset=utf-8");
        dataExportService.exportCustomerMarketDataToExcelV4(response, opUser().getCustId(), value, startTime, endTime);
    }

    @RequestMapping(value = "/downloadCustomerGroupExcelData_V1_NoPhoneArea", method = RequestMethod.GET)
    @CacheAnnotation
    public void downloadCustomerGroupExcelData_V1_1_NoPhoneArea(HttpServletResponse response, String startTime, String endTime) {
        response.setContentType("application/json;charset=utf-8");
        if (!"".equals(opUser().getUsername())) {
            dataExportService.downloadCustomerGroupExcelData_V1_1_NoPhoneArea(response, opUser().getCustId(), null, "邀约状态", "成功",
                    startTime, endTime);
        } else {
            log.warn(opUser().getUsername() + "非法请求接口");
            try {
                response.getOutputStream().write("{\"msg\":\"非法请求接口\"}".getBytes("UTF-8"));
            } catch (IOException e) {
                log.error(opUser().getUsername() + "非法请求接口异常,", e);
            }
        }

    }

    @RequestMapping(value = "/exportCustomerMarketDataToExcelV3", method = RequestMethod.GET)
    @CacheAnnotation
    public void exportCustomerMarketDataToExcelV3(HttpServletResponse response, String startTime, String endTime) {
        response.setContentType("application/json;charset=utf-8");
        if (!"".equals(opUser().getUsername())) {
            dataExportService.exportCustomerMarketDataToExcelV3(response, opUser().getCustId(), null, "邀约状态", "成功",
                    startTime, endTime);
        } else {
            log.warn(opUser().getUsername() + "非法请求接口");
            try {
                response.getOutputStream().write("{\"msg\":\"非法请求接口\"}".getBytes("UTF-8"));
            } catch (IOException e) {
                log.error(opUser().getUsername() + "非法请求接口异常,", e);
            }
        }

    }

    /**
     * TODO 不再使用
     */
    @RequestMapping(value = "/salesCommCustomerGroupExport_V2", method = RequestMethod.GET)
    @CacheAnnotation
    public void salesCommCustomerGroupExport_V2(HttpServletResponse response, String startTime, String endTime) {
        response.setContentType("application/json;charset=utf-8");
        if (!"".equals(opUser().getUsername())) {
            dataExportService.downloadCustomerGroupExcelData_V2(response, opUser().getCustId(), null, "邀约状态", "成功",
                    startTime, endTime);
        } else {
            log.warn(opUser().getUsername() + "非法请求接口");
            try {
                response.getOutputStream().write("{\"msg\":\"非法请求接口\"}".getBytes("UTF-8"));
            } catch (IOException e) {
                log.error(opUser().getUsername() + "非法请求接口异常,", e);
            }
        }

    }

    /**
     * TODO 不再使用
     */
    @RequestMapping(value = "/salesCommCustomerGroupExport_V3", method = RequestMethod.GET)
    @CacheAnnotation
    public void salesCommCustomerGroupExport_V3(HttpServletResponse response, String startTime, String endTime) {
        response.setContentType("application/json;charset=utf-8");
        if (!"".equals(opUser().getUsername())) {
            dataExportService.downloadCustomerGroupExcelData_V1(response, opUser().getCustId(), null, "邀约状态", "成功",
                    startTime, endTime);
        } else {
            log.warn(opUser().getUsername() + "非法请求接口");
            try {
                response.getOutputStream().write("{\"msg\":\"非法请求接口\"}".getBytes("UTF-8"));
            } catch (IOException e) {
                log.error(opUser().getUsername() + "非法请求接口异常,", e);
            }
        }

    }

    @RequestMapping(value = "/potentialCustomerDataExport", method = RequestMethod.GET)
    public void potentialCustomerDataExport(HttpServletResponse response) {
        try {
            dataExportService.downloadCustomerGroupExcelData(response, opUser().getCustId(), "1", null, "是",
                    null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//	@ResponseBody
//	@RequestMapping("/generAuthKey")
//	public String generAuthKey(String taskNum) {
//		String dateStr = DateFormatUtils.format(new Date(), "yyyyddMM");
//		String key = dateStr+taskNum;
//		String authKey = DigestUtils.md5Hex(key);
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("authKey", authKey);
//		return jsonObject.toString();
//	}

    private String getLabelTree(String outputLabel) {
        List<Map<String, Object>> ret = dataExportService.getLabelTree(request, outputLabel, opUser().getId());
        List<JSONObject> lst = new ArrayList<JSONObject>();
        for (Map<String, Object> m : ret) {
            List<Map<String, Object>> mapLst = (List<Map<String, Object>>) m.get("children");
            if (null != mapLst && mapLst.size() > 0) {
                for (Map<String, Object> map : mapLst) {
                    JSONObject jo = new JSONObject();
                    jo.put("labelID", m.get("labelID"));
                    JSONObject jo2 = new JSONObject();
                    jo2.put("labelID", map.get("labelID"));
                    jo.put("children", jo2);
                    lst.add(jo);
                }
            } else {
                JSONObject jo = new JSONObject();
                jo.put("labelID", m.get("labelID"));
                jo.put("children", "{}");
                lst.add(jo);
            }
        }
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setIgnoreDefaultExcludes(false);
        jsonConfig.setExcludes(new String[]{"categoryId", "parentId"});
        JSONArray jsonArr = JSONArray.fromObject(lst, jsonConfig);
        return jsonArr.toString();
    }

    private String getTaskNum() {
        String taskNum = dataExportService.getTaskNum();
        if (StringUtils.isEmpty(taskNum)) {
            return "110001";
        } else {
            return String.valueOf(Integer.parseInt(taskNum) + 1);
        }
    }

    private String generAuthKey(String taskNum) {
        String dateStr = DateFormatUtils.format(new Date(), "yyyyddMM");
        String key = dateStr + taskNum;
        String authKey = DigestUtils.md5Hex(key);
        return authKey;
    }

    private List<JSONObject> getTerms(Integer customerGroupId) {
        List<JSONObject> term = new ArrayList<JSONObject>();
        CustomGroup customGroup = customGroupService.getCustomGroupById(customerGroupId);
        if (null == customGroup) return null;
        JSONArray jsonArray = JSONArray.fromObject(customGroup.getGroupCondition());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject map = new JSONObject();
            List<String> lst = new ArrayList<String>();
            JSONObject obj = (JSONObject) jsonArray.get(i);
            String labelId = obj.get("labelId") == null ? "" : obj.get("labelId").toString();
            String categoryId = "";
            if (obj.has("categoryId"))
                categoryId = obj.get("categoryId") == null ? "" : obj.get("categoryId").toString();
            String startTime = obj.get("startTime") == null ? "" : obj.get("startTime").toString();
            String endTime = obj.get("endTime") == null ? "" : obj.get("endTime").toString();
            String symbol = obj.get("symbol") == null ? "" : obj.get("symbol").toString();
            String type = obj.get("type") == null ? "" : obj.get("type").toString();
            String leafs = obj.getString("leafs");
            JSONArray arr1 = JSONArray.fromObject(leafs);
            for (int j = 0; j < arr1.size(); j++) {
                JSONObject ob = (JSONObject) arr1.get(j);
                String labelName = ob.getString("name") == null ? "" : ob.getString("name");
                lst.add(labelName);
            }
            map.put("labelID", labelId);
            map.put("categoryId", categoryId);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("symbol", symbol);
            map.put("type", type);
            map.put("values", lst);
            term.add(map);
        }

        return term;
    }
}
