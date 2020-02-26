package com.bdaim.crm.erp.admin.controller;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.exception.TouchException;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.annotation.RequestBody;
import com.bdaim.crm.entity.LkCrmAdminFieldSortEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldStyleEntity;
import com.bdaim.crm.erp.admin.entity.AdminFieldSort;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.service.*;
import com.bdaim.crm.erp.oa.service.OaExamineCategoryService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.customer.dto.CustomerLabelDTO;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.core.paragetter.Para;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author hmb
 */
@RestController
@RequestMapping("/field")
public class AdminFieldController extends BasicAction {

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
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public R save(@RequestBody JSONObject jsonObject) {
        //String str = getRawData();
        // JSONObject jsonObject = JSON.parseObject(str);
        return (adminFieldService.save(jsonObject));
    }

    /**
     *
     */
    @RequestMapping(value = "/queryFields", method = RequestMethod.POST)
    public R queryFields() {
        return (adminFieldService.queryFields());
    }

    /**
     * @author zxy
     * 查询自定义字段列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public R list(@RequestBody JSONObject object) {
        //JSONObject object = JSONObject.parseObject(getRawData());
        return (R.ok().put("data", adminFieldService.list(object.getString("label"), object.getString("categoryId"))));
    }

    /**
     * @author wyq
     * 查询新增或编辑字段
     */
    @RequestMapping(value = "/queryField", method = RequestMethod.POST)
    public R queryField(@Para("label") String label, @Para("id") Integer id) {
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
        //Json json = ErpJsonFactory.me().getJson();
        return renderCrmJson(recordList);
    }

    /**
     * @param types 模块类型
     * @param id    查询基本信息
     * @author wyq
     */
    @RequestMapping(value = "/information", method = RequestMethod.POST)
    public R information(@Para("types") Integer types, @Para("id") String id, Long seaId) {
        List<Record> recordList;
        if (8 != types) {
            boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.getSign(types)), id);
            if (auth) {
                return (R.noAuth());
                //return;
            }
        }
        if (1 == types) {
            recordList = crmLeadsService.information(NumberUtil.parseInt(id));
        } else if (2 == types) {
            recordList = crmCustomerService.information(NumberUtil.parseInt(id));
        } else if (3 == types) {
            recordList = crmContactsService.information(NumberUtil.parseInt(id));
        } else if (4 == types) {
            recordList = crmProductService.information(NumberUtil.parseInt(id));
        } else if (5 == types) {
            recordList = crmBusinessService.information(NumberUtil.parseInt(id));
        } else if (6 == types) {
            recordList = crmContractService.information(NumberUtil.parseInt(id));
        } else if (7 == types) {
            recordList = crmReceivablesService.information(NumberUtil.parseInt(id));
        } else if (8 == types) {
            // 线索公海
            try {
                recordList = crmLeadsService.information(seaId, BaseUtil.getUser().getCustId(), id);
            } catch (TouchException e) {
                recordList = null;
                return (R.error(e.getErrMsg()));
            }
        } else {
            recordList = new ArrayList<>();
        }
        return renderCrmJson(recordList);
    }

    /**
     * @author zhangzhiwei
     * 设置字段样式
     */
    @RequestMapping(value = "/setFelidStyle", method = RequestMethod.POST)
    public R setFelidStyle(@RequestParam Map param) {
        return (adminFieldService.setFelidStyle((Kv) param));
    }

    /**
     * @author zhangzhiwei
     * 验证字段数据
     */
    @NotNullValidate(value = "val", message = "字段校验参数错误")
    @NotNullValidate(value = "types", message = "字段校验参数错误")
    @NotNullValidate(value = "fieldName", message = "字段校验参数错误")
    @NotNullValidate(value = "fieldType", message = "字段校验参数错误")
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public R verify(@RequestParam HashMap map) {
        return adminFieldService.verify(map);
    }

    /**
     * @author wyq
     * 查询客户管理列表页字段
     */
    @NotNullValidate(value = "label", message = "label不能为空")
    @RequestMapping(value = "/queryListHead", method = RequestMethod.POST)
    public R queryListHead(@Para("") LkCrmAdminFieldSortEntity adminFieldSort) {
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
        ///resp.setData(JavaBeanUtil.recordToMap(records));
        return (R.ok().put("data", JavaBeanUtil.recordToMap(records)));
    }

    /**
     * @author wyq
     * 查询字段排序隐藏设置
     */
    @NotNullValidate(value = "label", message = "label不能为空")
    @RequestMapping(value = "/queryFieldConfig", method = RequestMethod.POST)
    public R queryFieldConfig(@Para("") AdminFieldSort adminFieldSort) {
        return (adminFieldService.queryFieldConfig(adminFieldSort));
    }

    /**
     * @author wyq
     * 设置字段排序隐藏
     */
    @NotNullValidate(value = "label", message = "label不能为空")
    @NotNullValidate(value = "noHideIds", message = "显示列不能为空")
    @RequestMapping(value = "/fieldConfig", method = RequestMethod.POST)
    public R fieldConfig(@Para("") AdminFieldSort adminFieldSort) {
        return (adminFieldService.fieldConfig(adminFieldSort));
    }

    /**
     * @author wyq
     * 获取导入查重字段
     */
    @RequestMapping(value = "/getCheckingField", method = RequestMethod.POST)
    public R getCheckingField(@Para("type") Integer type) {
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
        return (data);
    }

    /**
     * @description 获取自定义属性
     * @method
     * @date: 2019/7/1 10:29
     */
    @RequestMapping(value = "/list/label", method = RequestMethod.POST)
    public String getLabelInfo(@org.springframework.web.bind.annotation.RequestBody CustomerLabelDTO customerLabelDTO) {
        ResponseJson responseJson = new ResponseJson();
        try {
            Map<String, Object> map = adminFieldService.getLabelInfoById(customerLabelDTO, opUser());
            responseJson.setData(map);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }
}
