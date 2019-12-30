package com.bdaim.customer.account.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.service.CustomerExtensionService;
import com.bdaim.customs.services.ExportExcelService;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/customer/extension")
public class CustomerExtensionController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(CustomerExtensionController.class);
    @Autowired
    private CustomerExtensionService customerExtensionService;
    @Autowired
    private ExportExcelService exportExcelService;

    /**
     * 添加及修改推广线索
     *
     * @param id
     * @return
     */
    @PostMapping("/{id}")
    public ResponseInfo saveExtension(@PathVariable(name = "id") Long id, @RequestBody(required = false) String body) {

        ResponseInfo resp = new ResponseInfo();
        JSONObject info = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";

            info = JSONObject.parseObject(body);
            if (id == 0l) return new ResponseInfoAssemble().failure(-1, "备注修改失败");
            resp.setData(customerExtensionService.updateExtension(id, info));
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "备注修改失败");
        }

        return resp;
    }

    @PostMapping("/all")
    public ResponseInfo query(@RequestBody(required = false) String body, HttpServletResponse response) {
        ResponseInfo resp = new ResponseInfo();
        PageParam page = new PageParam();
        JSONObject info = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";

            info = JSONObject.parseObject(body);
            if (StringUtil.isNotEmpty(info.getString("id"))) {
                page.setPageSize(10000000);
                page.setPageNum(1);
            } else {
                page.setPageSize(!info.containsKey("pageSize") ? 10 : info.getIntValue("pageSize"));
                page.setPageNum(!info.containsKey("pageNum") ? 1 : info.getIntValue("pageNum"));
            }
            PageList query = customerExtensionService.query(info, page);
            if ("extension".equals(info.getString("type"))) {
                int size = query.getList().size();
                if (size > 200)
                    return new ResponseInfoAssemble().failure(-1, "已经超出导出上限（" + (size - 200) + "条），请调整检索条件后重试");
                exportExcelService.exportExcel(0, query.getList(), info, response);
            }
            resp.setData(query);
        } catch (Exception e) {

        }
        return resp;
    }

//    @PostMapping("/export")
//    public ResponseInfo doInfo(@RequestBody(required = false) String body, HttpServletResponse response) {
//        ResponseInfo resp = new ResponseInfo();
//        PageParam page = new PageParam();
//        JSONObject info = null;
//        try {
//            if (body == null || "".equals(body))
//                body = "{}";
//            info = JSONObject.parseObject(body);
//
//            if (StringUtil.isEmpty(info.getString("id"))) {
//                page.setPageSize(0);
//                page.setPageNum(10000000);
//            } else {
//                page.setPageSize(info.containsKey("pageSize") ? 0 : info.getIntValue("pageSize"));
//                page.setPageNum(info.containsKey("pageNum") ? 10 : info.getIntValue("pageNum"));
//            }
//            PageList query = customerExtensionService.query(info, page);
//            int size = query.getList().size();
//            if (size > 200) return new ResponseInfoAssemble().failure(-1, "已经超出导出上限（" + (size - 200) + "条），请调整检索条件后重试");
//            exportExcelService.exportExcel(0, query.getList(), info, response);
//        } catch (Exception e) {
//            return new ResponseInfoAssemble().failure(-1, "导出失败");
//        }
//        return resp;
//    }


}
