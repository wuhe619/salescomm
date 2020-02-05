package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.crm.entity.LkCrmAdminFieldSortEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldStyleEntity;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Record;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.erp.admin.entity.AdminFieldSort;
import com.bdaim.crm.erp.admin.entity.AdminFieldStyle;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.service.*;
import com.bdaim.crm.erp.oa.service.OaExamineCategoryService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author hmb
 */
@RestController
@RequestMapping("/field")
public class AdminFieldController extends Controller {

    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private CrmCustomerService crmCustomerService;

    @Resource
    private CrmBusinessService crmBusinessService;

    @Resource
    private CrmContactsService crmContactsService;

    @Resource
    private CrmContractService crmContractService;

    @Resource
    private CrmLeadsService crmLeadsService;

    @Resource
    private CrmProductService crmProductService;

    @Resource
    private CrmReceivablesService crmReceivablesService;

    @Resource
    private CrmReceivablesPlanService crmReceivablesPlanService;

    @Resource
    private OaExamineCategoryService oaExamineCategoryService;

    /**
     * @author zhangzhiwei
     * 保存自定义字段E
     */
    @Permissions("manage:crm")
    public void save() {
        String str = getRawData();
        JSONObject jsonObject = JSON.parseObject(str);
        renderJson(adminFieldService.save(jsonObject));
    }

    /**
     *
     */
    public void queryFields() {
        renderJson(adminFieldService.queryFields());
    }

    /**
     * @author zxy
     * 查询自定义字段列表
     */
    public void list() {
        JSONObject object = JSONObject.parseObject(getRawData());
        renderJson(R.ok().put("data", adminFieldService.list(object.getString("label"), object.getString("categoryId"))));
    }

    /**
     * @author wyq
     * 查询新增或编辑字段
     */
    public void queryField(@Para("label") String label, @Para("id") Integer id) {
        List<Record> recordList = new LinkedList<>();
        if (id != null) {
            if ("1".equals(label)) {
                recordList = crmLeadsService.queryField(id);
            }
            if ("2".equals(label)) {
                recordList = crmCustomerService.queryField(id);
            }
            if ("3".equals(label)) {
                recordList = crmContactsService.queryField(id);
            }
            if ("4".equals(label)) {
                recordList = crmProductService.queryField(id);
            }
            if ("5".equals(label)) {
                recordList = crmBusinessService.queryField(id);
            }
            if ("6".equals(label)) {
                recordList = crmContractService.queryField(id);
            }
            if ("7".equals(label)) {
                recordList = crmReceivablesService.queryField(id);
            }
            if ("8".equals(label)) {
                recordList = crmReceivablesPlanService.queryField(id);
            }
            if ("10".equals(label)) {
                recordList = oaExamineCategoryService.queryField(id);
            }
        } else {
            if ("8".equals(label)) {
                recordList = crmReceivablesPlanService.queryField();
            } else {
                recordList = adminFieldService.queryAddField(Integer.valueOf(label));
            }
        }
        renderJson(R.ok().put("data", recordList));
    }

    /**
     * @param types 模块类型
     * @param id    查询基本信息
     * @author wyq
     */
    public void information(@Para("types") Integer types, @Para("id") Integer id) {
        List<Record> recordList;
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.getSign(types)), id);
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        if (1 == types) {
            recordList = crmLeadsService.information(id);
        } else if (2 == types) {
            recordList = crmCustomerService.information(id);
        } else if (3 == types) {
            recordList = crmContactsService.information(id);
        } else if (4 == types) {
            recordList = crmProductService.information(id);
        } else if (5 == types) {
            recordList = crmBusinessService.information(id);
        } else if (6 == types) {
            recordList = crmContractService.information(id);
        } else if (7 == types) {
            recordList = crmReceivablesService.information(id);
        } else {
            recordList = new ArrayList<>();
        }
        renderJson(R.ok().put("data", recordList));
    }

    /**
     * @author zhangzhiwei
     * 设置字段样式
     */
    public void setFelidStyle() {
        renderJson(adminFieldService.setFelidStyle(getKv()));
    }

    /**
     * @author zhangzhiwei
     * 验证字段数据
     */
    @NotNullValidate(value = "val", message = "字段校验参数错误")
    @NotNullValidate(value = "types", message = "字段校验参数错误")
    @NotNullValidate(value = "fieldName", message = "字段校验参数错误")
    @NotNullValidate(value = "fieldType", message = "字段校验参数错误")
    public void verify() {
        renderJson(adminFieldService.verify(getKv()));
    }

    /**
     * @author wyq
     * 查询客户管理列表页字段
     */
    @NotNullValidate(value = "label", message = "label不能为空")
    @ResponseBody
    @RequestMapping(value = "/queryListHead", method = RequestMethod.POST)
    public ResponseInfo queryListHead(@Para("") LkCrmAdminFieldSortEntity adminFieldSort) {
        ResponseInfo resp = new ResponseInfo();
        List<Record> records;
        if (adminFieldSort.getLabel() == 10) {
            records = oaExamineCategoryService.queryFieldList();
        } else {
            records = adminFieldService.queryListHead(adminFieldSort);
        }
        List<LkCrmAdminFieldStyleEntity> fieldStyles = adminFieldService.queryFieldStyle(adminFieldSort.getLabel());
        records.forEach(record -> {
            for (LkCrmAdminFieldStyleEntity fieldStyle : fieldStyles) {
                if (record.get("fieldName") != null && fieldStyle.getFieldName().equals(record.get("fieldName"))) {
                    record.set("width", fieldStyle.getStyle());
                    break;
                }
            }
            if (!record.getColumns().containsKey("width")) {
                record.set("width", 100);
            }
        });
        resp.setData(JavaBeanUtil.recordToMap(records));
        //renderJson(R.ok().put("data",records));
        return resp;
    }

    /**
     * @author wyq
     * 查询字段排序隐藏设置
     */
    @NotNullValidate(value = "label", message = "label不能为空")
    public void queryFieldConfig(@Para("") AdminFieldSort adminFieldSort) {
        renderJson(adminFieldService.queryFieldConfig(adminFieldSort));
    }

    /**
     * @author wyq
     * 设置字段排序隐藏
     */
    @NotNullValidate(value = "label", message = "label不能为空")
    @NotNullValidate(value = "noHideIds", message = "显示列不能为空")
    public void fieldConfig(@Para("") AdminFieldSort adminFieldSort) {
        renderJson(adminFieldService.fieldConfig(adminFieldSort));
    }

    /**
     * @author wyq
     * 获取导入查重字段
     */
    public void getCheckingField(@Para("type") Integer type) {
        R data;
        switch (type) {
            case 1:
                data = crmLeadsService.getCheckingField();
                break;
            case 2:
                data = crmCustomerService.getCheckingField();
                break;
            case 3:
                data = crmContactsService.getCheckingField();
                break;
            case 4:
                data = crmProductService.getCheckingField();
                break;
            default:
                data = R.error("type不符合要求");
        }
        renderJson(data);
    }
}
