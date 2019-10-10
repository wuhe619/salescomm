package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ResourceService;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * 申报单.税单
 */
@Service("busi_sbd_s")
@Transactional
public class SbdSService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(SbdFService.class);

//    @Autowired
//    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        Integer pid = info.getInteger("pid");
        String billNo = info.getString("bill_no");
        if (pid == null) {
            log.error("分单id不能为空");
            throw new TouchException("分单id不能为空");
        }
        if (StringUtil.isEmpty(billNo)) {
            log.error("分单号不能为空");
            throw new TouchException("分单号不能为空");
        }
        String code_ts = info.getString("code_ts");
        if (StringUtil.isEmpty(code_ts)) {
            log.error("商品编码不能为空");
            throw new TouchException("商品编码不能为空");
        }
        info.put("cust_id", cust_id);
        info.put("create_id", cust_user_id);
        info.put("create_date", new Date());

        info.put("ext_3", code_ts);
        info.put("ext_4", billNo);
        float duty_paid_price = 0;
        float estimated_tax = 0;
        float tax_rate = 0;
        int is_low_price = 0;

        if (StringUtil.isNotEmpty(code_ts)) {
            JSONObject params = new JSONObject();
            params.put("code", code_ts);
            Page page = resourceService.query("", "duty_paid_rate", params);
            if (page != null && page.getTotal() > 0) {
                List dataList = page.getData();
                Map<String, Object> d = (Map<String, Object>) dataList.get(0);
                JSONObject contentObj = JSON.parseObject(JSON.toJSONString(d));
                duty_paid_price = contentObj.getFloatValue("duty_price");
                if (StringUtil.isNotEmpty(info.getString("decl_price"))) {
                    if (Float.valueOf(info.getString("decl_price")) < duty_paid_price) {
                        is_low_price = 1;
                    }
                }
                tax_rate = contentObj.getFloatValue("tax_rate");
                estimated_tax = duty_paid_price * tax_rate;
            }
        }
        info.put("is_low_price", is_low_price);
        info.put("duty_paid_price", duty_paid_price);//完税价格
        info.put("estimated_tax", estimated_tax);//预估税金
        info.put("tax_rate", tax_rate);//税率
        info.put("total_price", 0);//价格合计

        serviceUtils.addDataToES(id.toString(), busiType, info);


        HBusiDataManager partH = serviceUtils.getObjectByIdAndType(pid.longValue(), BusiTypeEnum.SF.getType());

        String pcontent = partH.getContent();
        JSONObject jsonObject = JSON.parseObject(pcontent);
        // 税单存入主单号
        info.put("ext_2", jsonObject.getString("main_bill_no"));

        Float weight = jsonObject.getFloatValue("weight");
        Float pack_NO = jsonObject.getFloatValue("pack_no");
        if (weight == null) weight = 0f;
        if (info.containsKey("ggrosswt") && StringUtil.isNotEmpty(info.getString("ggrosswt"))) {
            weight += Float.valueOf(info.getString("ggrosswt"));
        }
        if (pack_NO == null) pack_NO = 0f;
        if (info.containsKey("g_qty") && StringUtil.isNotEmpty(info.getString("g_qty"))) {
            pack_NO += Float.valueOf(info.getString("g_qty"));
        }
        jsonObject.put("weight", weight);
        jsonObject.put("pack_no", pack_NO);
        Integer lowPricegoods = jsonObject.getInteger("low_price_goods");
        if (lowPricegoods == null) lowPricegoods = 0;
        jsonObject.put("low_price_goods", lowPricegoods + is_low_price);
        partH.setContent(jsonObject.toJSONString());


        String sql = "update " + HMetaDataDef.getTable(partH.getType(), "") + " set content='" + jsonObject.toJSONString() + "'" +
                " where id=" + partH.getId() + " and type='" + partH.getType() + "'";
        jdbcTemplate.update(sql);

//        hBusiDataManagerDao.saveOrUpdate(partH);

        serviceUtils.updateDataToES(BusiTypeEnum.SF.getType(), pid.toString(), jsonObject);

        HBusiDataManager zh = serviceUtils.getObjectByIdAndType(jsonObject.getLong("pid"), BusiTypeEnum.SZ.getType());
        String zcontent = zh.getContent();
        JSONObject jsonz = JSON.parseObject(zcontent);
        Float weight_total = jsonz.getFloatValue("weight_total");
        Integer lowPricegoodsz = jsonObject.getInteger("low_price_goods");
        if (lowPricegoodsz == null) lowPricegoodsz = 0;
        jsonz.put("low_price_goods", lowPricegoodsz + is_low_price);

        if (weight_total == null) weight_total = 0f;
        if (StringUtil.isNotEmpty(info.getString("ggrosswt"))) {
            weight_total += Float.valueOf(info.getString("ggrosswt"));
        }
        jsonz.put("weight_total", weight_total);

        zh.setContent(jsonz.toJSONString());

        String sql2 = "update " + HMetaDataDef.getTable(zh.getType(), "") + " set content='" + jsonz.toJSONString() + "'" +
                " where id=" + zh.getId() + " and type='" + zh.getType() + "'";
        jdbcTemplate.update(sql2);
//        hBusiDataManagerDao.saveOrUpdate(zh);
        serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), zh.getId().toString(), jsonz);

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(id, busiType);
        String content = dbManager.getContent();
        JSONObject json = JSONObject.parseObject(content);
        Iterator keys = info.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            json.put(key, info.get(key));
        }
        serviceUtils.updateDataToES(busiType, id.toString(), json);

        HBusiDataManager fmanager = serviceUtils.getObjectByIdAndType(json.getLong("pid"), BusiTypeEnum.SF.getType());
        String fcontent = fmanager.getContent();

        JSONObject fjson = JSONObject.parseObject(fcontent);

        List<HBusiDataManager> goodsList = serviceUtils.getDataList(BusiTypeEnum.SS.getType(), fmanager.getId().longValue());
        float weight = 0;  //重量
        float pack_NO = 0; //数量
        int lowPricegoods = 0; //低价商品数
        int is_low_price = 0;
        float festimated_tax = 0;//预估税金
        for (HBusiDataManager m : goodsList) {
            JSONObject params = new JSONObject();
            params.put("code", m.getExt_1());
            float tax_rate = 0;
            float estimated_tax = 0;
            float duty_paid_price = 0;
            Page page = resourceService.query("", "duty_paid_rate", params);
            if (page != null && page.getTotal() > 0) {
                List dataList = page.getData();
                Map<String, Object> d = (Map<String, Object>) dataList.get(0);
                JSONObject contentObj = JSON.parseObject(JSON.toJSONString(d));
                duty_paid_price = contentObj.getFloatValue("duty_price");

                tax_rate = contentObj.getFloatValue("tax_rate");
                estimated_tax = duty_paid_price * tax_rate;
                festimated_tax += estimated_tax;
            }

            JSONObject goods = JSONObject.parseObject(m.getContent());
            if (m.getId() == id.intValue()) {
                if (StringUtil.isNotEmpty(info.getString("decl_price"))) {
                    if (Float.valueOf(info.getString("decl_price")) < duty_paid_price) {
                        is_low_price = 1;
                    }
                }
                info.put("is_low_price", is_low_price);
                info.put("duty_paid_price", duty_paid_price);//完税价格
                info.put("estimated_tax", estimated_tax);//预估税金
                info.put("tax_rate", tax_rate);//税率
                info.put("total_price", 0);//价格合计
            } else {
                if (goods.containsKey("ggrosswt") && StringUtil.isNotEmpty(goods.getString("ggrosswt"))) {
                    weight += goods.getFloatValue("ggrosswt");
                }
                if (goods.containsKey("g_qty") && StringUtil.isNotEmpty(goods.getString("g_qty"))) {
                    pack_NO += goods.getFloatValue("g_qty");
                }
                if (StringUtil.isNotEmpty(goods.getString("decl_price"))) {
                    if (Float.valueOf(goods.getString("decl_price")) < duty_paid_price) {
                        is_low_price = 1;
                    }
                }
            }
            if (is_low_price == 1) {
                lowPricegoods++;
            }
        }
        fjson.put("weight_total", weight);
        fjson.put("lowPricegoods", lowPricegoods);
        fjson.put("pack_no", pack_NO);
        fjson.put("estimated_tax", festimated_tax);
        serviceUtils.updateDataToES(BusiTypeEnum.SF.getType(), fmanager.getId().toString(), fjson);
        //serviceUtils.updateDataToES(BusiTypeEnum.SS.getType(),id.toString(),info);
    }

    @Override
    public void getInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) {
        // TODO Auto-generated method stub

        HBusiDataManager ph = serviceUtils.getObjectByIdAndType(id, busiType);
        serviceUtils.deleteDatafromES(BusiTypeEnum.SS.getType(), id.toString());
        serviceUtils.delDataListByIdAndType(id, busiType);
        String pcontent = ph.getContent();
        JSONObject pjson = JSON.parseObject(pcontent);

        //获取分单信息，从分单中减去商品的重量等
        HBusiDataManager parth = serviceUtils.getObjectByIdAndType(pjson.getLong("pid"), BusiTypeEnum.SF.getType());
        String partcontent = parth.getContent();
        JSONObject partcontentJson = JSON.parseObject(partcontent);

        Float weight = partcontentJson.getFloatValue("weight");
        Float pack_NO = partcontentJson.getFloatValue("pack_no");
        if (weight == null) weight = 0f;
        if (StringUtil.isNotEmpty(pjson.getString("ggrosswt"))) {
            weight -= Float.valueOf(pjson.getString("ggrosswt"));
        }
        if (pack_NO == null) pack_NO = 0f;
        if (StringUtil.isNotEmpty(pjson.getString("g_qty"))) {
            pack_NO -= Float.valueOf(pjson.getString("g_qty"));
        }
        partcontentJson.put("weight", weight);
        partcontentJson.put("pack_no", pack_NO);
        parth.setContent(partcontentJson.toJSONString());

        String sql = "update " + HMetaDataDef.getTable(parth.getType(), "") + " set content='" + partcontentJson.toJSONString() + "'" +
                " where id=" + parth.getId() + " and type='" + parth.getType() + "'";
        jdbcTemplate.update(sql);

//        hBusiDataManagerDao.saveOrUpdate(parth);
        serviceUtils.updateDataToES(BusiTypeEnum.SF.getType(), parth.getId().toString(), partcontentJson);

        //处理主单
        HBusiDataManager zh = serviceUtils.getObjectByIdAndType(partcontentJson.getLong("pid"), BusiTypeEnum.SZ.getType());
        String zcontent = zh.getContent();
        JSONObject jsonz = JSON.parseObject(zcontent);
        Float weight_total = jsonz.getFloatValue("weight_total");
        if (weight_total == null) weight_total = 0f;

        weight_total -= Float.valueOf(pjson.getString("ggrossWt") == null ? "0" : pjson.getString("ggrossWt"));
        jsonz.put("weight_total", weight_total);
        zh.setContent(jsonz.toJSONString());

        String sql2 = "update " + HMetaDataDef.getTable(zh.getType(), "") + " set content='" + jsonz.toJSONString() + "'" +
                " where id=" + zh.getId() + " and type='" + zh.getType() + "'";
        jdbcTemplate.update(sql2);

//        hBusiDataManagerDao.saveOrUpdate(zh);
        serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), zh.getId().toString(), jsonz);

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
       /* String sql = null;
        //查询主列表
        if ("main".equals(params.getString("rule.do"))) {
            sqlParams.clear();
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from "+HMetaDataDef.getTable()+" where type=?");
            if (!"all".equals(cust_id))
                sqlstr.append(" and cust_id='").append(cust_id).append("'");

            sqlParams.add(busiType);

            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if ("pageNum".equals(key) || "pageSize".equals(key)|| "pid1".equals(key)|| "pid2".equals(key)) continue;
                if ("cust_id".equals(key)) {
                    sqlstr.append(" and cust_id=?");
                } else if (key.endsWith(".c")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 2) + "') like '%?%'");
                } else if (key.endsWith(".start")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 6) + "') >= ?");
                } else if (key.endsWith(".end")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 6) + "') <= ?");
                } else {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key + "')=?");
                }

                sqlParams.add(params.get(key));
            }
            String pidO = params.getString("pidO");
            String pidS = params.getString("pidO");
            if(StringUtil.isNotEmpty(pidO) && StringUtil.isNotEmpty(pidS)) {
                sqlstr.append(" and JSON_EXTRACT(content, '$.pid')= SELECT id FROM "+HMetaDataDef.getTable()+" WHERE type =? AND JSON_EXTRACT(content, '$.pid')=?");
            }
            sql = sqlstr.toString();
        }*/
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }


}
