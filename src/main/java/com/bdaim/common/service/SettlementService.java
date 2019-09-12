package com.bdaim.common.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dao.DicDao;
import com.bdaim.common.dao.SettlementDao;
import com.bdaim.common.dto.Page;
import com.bdaim.common.entity.Dic;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.fund.entity.Settlement;
import com.bdaim.fund.entity.SettlementProperty;
import com.bdaim.common.util.*;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.rbac.dao.UserDao;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author duanliying
 * @date 2019/6/21
 * @description
 */
@Service("settlementService")
@Transactional
public class SettlementService {
    private static Logger logger = Logger.getLogger(SettlementService.class);
    @Resource
    private SettlementDao settlementDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private DicDao dicDao;
    @Resource
    private UserDao userDao;

    /**
     * 保存结算记录信息
     *
     * @param json
     * @param loginUser
     * @return
     */
    public JSONObject saveSettlementLogs(JSONObject json, LoginUser loginUser) throws Exception {
        JSONObject result = new JSONObject();
        String code = "0";
        logger.info("传递参数是：" + json.toJSONString());
        //结算对象id
        String objId = json.getString("objId");
        //结算对象id
        String productId = json.getString("productId");
        //产品类型
        String productType = json.getString("productType");
        //结算周期
        String settlementTime = json.getString("settlementTime");
        //录入类型  1 首次录入  2 扣量录入
        String type = json.getString("type");
        //结算方  1：机构  2：推广活动
        String settlementObj = json.getString("settlementObj");

        //根据id和settlementCycle和type判断记录是否存在  存在不允许录入
        StringBuffer ifExistSql = new StringBuffer("SELECT d.id FROM t_settlement d LEFT JOIN t_settlement_property p ON d.id = p.settlement_id WHERE ");
        ifExistSql.append("d.id IN  ( SELECT settlement_id FROM t_settlement_property  WHERE property_name = 'productType' AND property_value = ? )");
        ifExistSql.append(" AND  d.id IN ( SELECT settlement_id FROM t_settlement_property  WHERE property_name = 'settlementObj' AND property_value = ? )");
        ifExistSql.append(" AND  d.id IN ( SELECT settlement_id FROM t_settlement_property  WHERE property_name = 'type' AND property_value = ? )");
        ifExistSql.append(" AND d.settlement_time = ? AND product_id = ? GROUP BY d.id");

        List<Map<String, Object>> list = settlementDao.sqlQuery(ifExistSql.toString(), productType, settlementObj, type, settlementTime, productId);
        if (list.size() == 0) {
            List<SettlementProperty> properties = new ArrayList<>();
            Settlement settlement = new Settlement();
            if (json != null && json.size() > 0) {
                Long id = IDHelper.getID();
                settlement.setId(id);
                settlement.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                settlement.setCreateUser(loginUser.getId().toString());
                for (Map.Entry<String, Object> entry : json.entrySet()) {
                    if ("productId".equals(entry.getKey())) {
                        settlement.setProductId(json.getString(entry.getKey()));
                    } else if ("remark".equals(entry.getKey())) {
                        settlement.setRemark(json.getString(entry.getKey()));
                    } else if ("settlementTime".equals(entry.getKey())) {
                        settlement.setSettlementTime(json.getString(entry.getKey()));
                    } else {
                        SettlementProperty property = new SettlementProperty();
                        property.setSettlementId(id);
                        property.setPropertyName(entry.getKey());
                        property.setPropertyValue(json.getString(entry.getKey()));
                        property.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        properties.add(property);
                    }
                }
            }
            //保存主信息
            settlementDao.saveOrUpdate(settlement);
            //保存属性信息
            settlementDao.batchSaveOrUpdate(properties);
            result.put("code", code);
            result.put("message", "success");
            return result;
        } else {
            //结算信息已经存在不可以重复录入
            result.put("code", -1);
            result.put("message", "结算信息已经存在不可重复添加");
            return result;
        }
    }


    /**
     * 结算信息列表
     *
     * @param pageSize
     * @param pageNum
     * @param
     * @return
     */
    public JSONObject querySettlementList(Integer pageNum, Integer pageSize, String dicType, String
            settlementType, String institutionName) throws Exception {
        JSONObject json = new JSONObject();
        String querySql = "SELECT c.cust_id id ,enterprise_name institutionName ,p.property_value FROM t_customer c LEFT JOIN t_customer_property p ON c.cust_id = p.cust_id  WHERE source = 2 AND status !=3 AND p.property_name = 'settlementInfo'";
        if (StringUtil.isNotEmpty(institutionName)) {
            querySql += " and enterprise_name  = '" + institutionName + "'";
        }
        if (StringUtil.isNotEmpty(dicType)) {
            querySql += " AND property_value LIKE '%" + "\"type\":" + dicType + "%'";
        }
        if (StringUtil.isNotEmpty(settlementType)) {
            querySql += " AND property_value LIKE '%" + "\"settlementType\":" + settlementType + "%'";
        }

        Page page = customerDao.sqlPageQuery(querySql, pageNum, pageSize);
        List<Map<String, Object>> productInfo = null;
        if (page != null && page.getData().size() > 0) {
            List<Map<String, Object>> data = page.getData();
            for (int i = 0; i < data.size(); i++) {
                String id = String.valueOf(data.get(i).get("id"));
                //根据id查询属性信息
                logger.info("机构id是：" + id);
                String propertyValue = String.valueOf(data.get(i).get("property_value"));
                JSONObject jsonObject = JSONObject.parseObject(propertyValue);
                JSONArray infoList = jsonObject.getJSONArray("product");
                productInfo = new ArrayList<>();
                for (int j = 0; j < infoList.size(); j++) {
                    Map<String, Object> map = new HashMap<>();
                    //商品类型,根据检索条件添加
                    String type = infoList.getJSONObject(j).getString("type");
                    if (StringUtil.isNotEmpty(dicType)) {
                        //只返回跟检索条件中相同商品类型的数据
                        if (dicType.equals(type)) {
                            map.put("type", type);
                        } else {
                            continue;
                        }
                    } else {
                        map.put("type", type);
                    }
                    //结算方式
                    String settlement = infoList.getJSONObject(j).getString("settlementType");
                    if (StringUtil.isNotEmpty(settlementType)) {
                        //只返回跟检索条件中相同商品类型的数据
                        if (settlementType.equals(settlement)) {
                            map.put("settlementType", settlement);
                        } else {
                            continue;
                        }
                    } else {
                        map.put("settlementType", settlement);
                    }
                    //开始时间
                    String startTime = infoList.getJSONObject(j).getString("startTime");
                    map.put("startTime", startTime);
                    //结束时间
                    String stopTime = infoList.getJSONObject(j).getString("stopTime");
                    map.put("stopTime", stopTime);
                    //根据商品类型查询商品数量
                    String queryProperty = "SELECT COUNT(*) num FROM t_dic d LEFT JOIN t_dic_property p on d.id = p.dic_id WHERE dic_prop_key = 'institution' AND dic_prop_value = ? AND d.dic_type_id = ?";
                    List<Map<String, Object>> list = customerDao.sqlQuery(queryProperty, id, type);
                    if (list != null && list.size() > 0) {
                        map.put("count", list.get(0).get("num"));
                    }
                    productInfo.add(map);
                }
                // }
                data.get(i).put("productInfoList", productInfo);
            }
        }
        json.put("data", page.getData());
        json.put("total", page.getTotal());
        return json;
    }

    /**
     * 获取结算信息
     *
     * @param
     */
    public JSONObject getSettlementInfo(Integer pageNum, Integer pageSize, String settlementTime, String objId, String dicType, String type, String settlementObj) throws Exception {
        JSONObject jsonObject = new JSONObject();
       /* //结算对象  1：机构  2：推广活动
        if ("1".equals(settlementObj)) {
            String querySql = "SELECT dic_id ,d.name FROM t_dic d LEFT JOIN t_dic_property p ON d.id = p.dic_id  WHERE dic_prop_key = 'institution' AND dic_prop_value = ? AND d.dic_type_id =?";
            List<Map<String, Object>> list = dicDao.sqlQuery(querySql, objId, dicType);
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    Long id = NumberConvertUtil.parseLong(list.get(i).get("dic_id"));
                    //根据id查询结算信息
                    //线索
                    DicProperty cluePriceConfig = dicDao.getProperty(id, "cluePriceConfig");
                    //佣金
                    DicProperty commissionConfig = dicDao.getProperty(id, "commissionConfig");
                    list.get(i).put("cluePriceConfig", cluePriceConfig.getDicPropValue());
                    list.get(i).put("commissionConfig", commissionConfig.getDicPropValue());
                }
            }
            jsonObject.put("list", list);
        }*/
        //添加列表信息   type  1:首次录入   2  扣减录入
        StringBuffer querySql = new StringBuffer("SELECT GROUP_CONCAT(DISTINCT d.id)ids,d.id id,d.remark,d.create_user,settlement_time settlementTime,d.create_time creatTime");
      /*  querySql.append("sum(( SELECT property_value FROM t_settlement_property WHERE property_name = 'commission' AND settlement_id = d.id )) commission,");
        querySql.append("sum(( SELECT property_value FROM t_settlement_property WHERE property_name = 'firstgetNum' AND settlement_id = d.id )) firstgetNum,");
        querySql.append("sum(( SELECT  property_value  FROM t_settlement_property WHERE property_name = 'activeNum' AND settlement_id = d.id )) activeNum");*/
        querySql.append(" FROM t_settlement d LEFT JOIN t_settlement_property p ON d.id = p.settlement_id WHERE d.id IN ");
        //querySql.append(" ( SELECT settlement_id FROM t_settlement_property WHERE property_name = 'productType' AND property_value = ? )");
        querySql.append(" ( SELECT settlement_id FROM t_settlement_property WHERE property_name = 'settlementObj' AND property_value = ? )");
        querySql.append(" and d.id IN( SELECT settlement_id FROM t_settlement_property WHERE property_name = 'type' AND property_value = ? )");
        querySql.append(" and d.id IN( SELECT settlement_id FROM t_settlement_property WHERE property_name = 'objId' AND property_value = ? )");
        if (StringUtil.isNotEmpty(settlementTime)) {
            querySql.append(" AND d.settlement_time = '" + settlementTime + "'");
        }
        querySql.append(" GROUP BY d.settlement_time");
        Page page = dicDao.sqlPageQuery(querySql.toString(), pageNum, pageSize, settlementObj, type, objId);
        List<Map<String, Object>> data = page.getData();
        if (data != null && data.size() > 0) {
            for (int j = 0; j < data.size(); j++) {
                //处理文件将文件名字添加路径
                SettlementProperty fileNameProperty = settlementDao.getProperty(NumberConvertUtil.parseLong(data.get(j).get("id")), "attachmentPath");
                if (fileNameProperty != null && StringUtil.isNotEmpty(fileNameProperty.getPropertyValue()) && (String.valueOf(fileNameProperty.getPropertyValue()).toLowerCase().endsWith(".xlsx")
                        || String.valueOf(fileNameProperty.getPropertyValue()).toLowerCase().endsWith(".xls"))) {
                    data.get(j).put("attachmentPath", ConfigUtil.getInstance().get("pic_server_url") + "/0/" + String.valueOf(fileNameProperty.getPropertyValue()));
                }
                //查询用户名字
                String name = userDao.getName(String.valueOf(data.get(j).get("create_user")));
                data.get(j).put("name", name);
                //计算相关数量
                String ids = String.valueOf(data.get(j).get("ids"));
                if (StringUtil.isNotEmpty(ids)) {
                    String[] split = ids.split(",");
                    int firstgetNumSum = 0, regeditNumSum = 0, activeNum = 0;
                    double commission = 0.0;
                    for (int i = 0; i < split.length; i++) {
                        SettlementProperty firstgetNum = settlementDao.getProperty(NumberConvertUtil.parseLong(split[i]), "firstgetNum");
                        if (firstgetNum != null) {
                            firstgetNumSum += NumberConvertUtil.parseInt(firstgetNum.getPropertyValue());
                        }
                        data.get(j).put("firstgetSum", firstgetNumSum);
                        SettlementProperty regeditProperty = settlementDao.getProperty(NumberConvertUtil.parseLong(split[i]), "regeditNum");
                        if (regeditProperty != null) {
                            regeditNumSum += NumberConvertUtil.parseInt(regeditProperty.getPropertyValue());
                        }
                        data.get(j).put("regeditSum", regeditNumSum);
                        SettlementProperty activeProperty = settlementDao.getProperty(NumberConvertUtil.parseLong(split[i]), "activeNum");
                        if (activeProperty != null) {
                            activeNum += NumberConvertUtil.parseInt(activeProperty.getPropertyValue());
                        }
                        data.get(j).put("activeSum", activeNum);
                        SettlementProperty commissionProperty = settlementDao.getProperty(NumberConvertUtil.parseLong(split[i]), "commission");
                        if (commissionProperty != null) {
                            commission += NumberConvertUtil.parseDouble(commissionProperty.getPropertyValue());
                        }
                        data.get(j).put("commissionSum", commission);
                    }
                }
            }
        }
        jsonObject.put("total", page.getTotal());
        jsonObject.put("logs", data);
        return jsonObject;
    }

    /**
     * 查看结算信息详情
     *
     * @param
     */
    public List<Map<String, Object>> getSettlementDetails(String settlementIds) throws Exception {
        logger.info("结算记录id是：" + settlementIds);
        List<Map<String, Object>> list = new ArrayList<>();
        String[] split = settlementIds.split(",");
        if (split.length > 0) {
            for (int i = 0; i < split.length; i++) {
                long settlementId = NumberConvertUtil.parseLong(split[i]);
                List<SettlementProperty> propertyList = settlementDao.getPropertyList(settlementId);
                HashMap<String, Object> map = new HashMap<>();
                for (SettlementProperty c : propertyList) {
                    if (StringUtil.isNotEmpty(c.getPropertyName())) {
                        map.put(c.getPropertyName(), c.getPropertyValue());
                    }
                }
                Settlement entity = settlementDao.getEntity(settlementId);
                Dic dicEntity = null;
                if (entity != null) {
                    String productId = entity.getProductId();
                    dicEntity = dicDao.getDicEntity(NumberConvertUtil.parseLong(productId));
                }
                if (dicEntity != null) {
                    map.put("productName", dicEntity.getName());
                }
                if (map.size() > 0) {
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * 结算商品信息查询
     *
     * @return
     */
    public JSONObject getInputInfo(String objId, String settlementObj, String dicType) throws Exception {
        JSONObject jsonObject = new JSONObject();
        //判断查询的是类型是机构还是活动信息
        List<Map<String, Object>> productList = null;
        if ("1".equals(settlementObj)) {
            //查询的是机构商品信息
            String querySql = "SELECT dic_id ,d.name FROM t_dic d LEFT JOIN t_dic_property p ON d.id = p.dic_id  WHERE dic_prop_key = 'institution' AND dic_prop_value = ? AND d.dic_type_id =?";
            productList = dicDao.sqlQuery(querySql, objId, dicType);
        } else if ("2".equals(settlementObj)) {
            Long activityId = NumberConvertUtil.parseLong(objId);
            //查询活动信息
            DicProperty commissionTypeProperty = dicDao.getProperty(activityId, "extensionType");
            //推广类型
            String commissionType = commissionTypeProperty.getDicPropValue();
            if (commissionType != null) {
                logger.info("推广类型是：" + commissionType);
                //活动类型
                jsonObject.put("extensionType", commissionType);
                //活动名称
                Dic activityIdName = dicDao.getDicEntity(activityId);
                if (activityIdName != null) {
                    jsonObject.put("activityIdName", activityIdName.getName());
                    productList = new ArrayList<>();
                    //活动页推广
                    DicProperty extensionTypeValue = dicDao.getProperty(activityId, "extensionTypeValue");
                    if (extensionTypeValue != null) {
                        //默认是单品
                        String productId = extensionTypeValue.getDicPropValue();
                        Dic dicEntityInfo = dicDao.getDicEntity(NumberConvertUtil.parseLong(productId));
                        if (dicEntityInfo != null) {
                            //产品类型
                            jsonObject.put("productType", dicEntityInfo.getDicTypeId());
                        }
                        //处理是活动页的情况
                        if ("2".equals(commissionType)) {
                            DicProperty products = dicDao.getProperty(NumberConvertUtil.parseLong(productId), "products");
                            if (products != null && StringUtil.isNotEmpty(products.getDicPropValue())) {
                                productId = products.getDicPropValue();
                            }
                        }
                        logger.info("查询出活动页商品的id是:" + productId);
                        if (StringUtil.isNotEmpty(productId)) {
                            String[] productIds = productId.split(",");
                            //根据商品id查询商品名称
                            if (productIds != null && productIds.length > 0) {
                                for (int i = 0; i < productIds.length; i++) {
                                    //根据id查询商品名字
                                    Dic dicEntity = dicDao.getDicEntity(NumberConvertUtil.parseLong(productIds[i]));
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("id", productIds[i]);
                                    map.put("dicType", dicEntity.getDicTypeId());
                                    map.put("name", dicEntity.getName());
                                    productList.add(map);
                                }
                            }
                        }
                    }
                }
            }
        }
        jsonObject.put("productList", productList);
        return jsonObject;
    }


    /**
     * 统计监控运营数据列表展示
     */
    public JSONObject getOperateDataList(Integer pageNum, Integer pageSize, String time, String queryType, String settlementObj, String type, String product_id) {
        JSONObject jsonObject = new JSONObject();
        Page page = null;
        StringBuffer stringBuffer = new StringBuffer("SELECT COUNT(DISTINCT product_id) productNum,COUNT(DISTINCT id) activityNum,GROUP_CONCAT(DISTINCT id) ids,product_id,product_type,SUM(regedit_num) regeditNum ,SUM(firstget_num) firstgetNum,SUM(active_num) activeNum,CONVERT(SUM(income)/1000,DECIMAL(10,2)) income,id, ");
        stringBuffer.append("(SELECT dic_prop_value FROM t_dic_property p  WHERE dic_prop_key = 'extensionChannel' AND s.id = p.dic_id)  channel");
        stringBuffer.append(" FROM stat_settlement s WHERE stat_time LIKE '" + time + "%'");
        //录入类型  1 首次录入   2.扣减录入
        if (StringUtil.isNotEmpty(type)) {
            stringBuffer.append(" AND type ='" + type + "'");
        }
        //settlementObj结算方  1 ：机构  2：活动
        if (StringUtil.isNotEmpty(settlementObj)) {
            stringBuffer.append(" AND settlement_obj ='" + settlementObj + "'");
        }
        //settlementObj结算方  1 ：机构  2：活动
        if (StringUtil.isNotEmpty("productId")) {
            stringBuffer.append(" AND product_id ='" + product_id + "'");
        }
        //根据type区分是1：商品收入排行还:2：机构收入 3：活动效果列表
        if ("1".equals(queryType)) {
            stringBuffer.append(" GROUP BY product_id ORDER BY income DESC");
        } else if ("2".equals(queryType)) {
            stringBuffer.append(" GROUP BY id ORDER BY income DESC");
        } else if ("3".equals(queryType)) {
            stringBuffer.append(" GROUP BY id,product_id ORDER BY income DESC");
        } else if ("4".equals(queryType)) {
            stringBuffer.append(" GROUP BY channel ORDER BY income DESC");
        }

        page = dicDao.sqlPageQuery(stringBuffer.toString(), pageNum, pageSize);
        if (page != null && page.getData().size() > 0) {
            List<Map<String, Object>> data = page.getData();
            for (int i = 0; i < data.size(); i++) {
                Long productId = NumberConvertUtil.parseLong(data.get(i).get("product_id"));
                Long id = NumberConvertUtil.parseLong(data.get(i).get("id"));
                logger.info("获取的产品id是：" + productId);
                //根据产品id查询产品名称
                Dic dicEntity = dicDao.getDicEntity(productId);
                if (dicEntity != null) {
                    String name = dicEntity.getName();
                    logger.info("产品名称是： " + name);
                    data.get(i).put("productName", name);
                }
                if ("2".equals(queryType)) {
                    //机构收入排行
                    //查询机构名称
                    String enterpriseName = customerDao.getEnterpriseName(String.valueOf(id));
                    data.get(i).put("name", enterpriseName);
                }
                if ("3".equals(queryType) || "4".equals(queryType)) {
                    //活动名称
                    Dic dicEntityName = dicDao.getDicEntity(id);
                    if (dicEntityName != null) {
                        data.get(i).put("name", dicEntityName.getName());
                    }
                    //活动链接
                    DicProperty activityUrl = dicDao.getProperty(id, "activityUrl");
                    if (activityUrl != null) {
                        data.get(i).put("activityUrl", activityUrl.getDicPropValue());
                    }
                    //渠道id
                    DicProperty extensionChannel = dicDao.getProperty(id, "extensionChannel");
                    if (extensionChannel != null) {
                        if (extensionChannel.getDicPropValue() != null) {
                            Dic channelName = dicDao.getDicEntity(NumberConvertUtil.parseLong(extensionChannel.getDicPropValue()));
                            data.get(i).put("channelId", extensionChannel.getDicPropValue());
                            data.get(i).put("channelName", channelName.getName());
                        }
                    }
                    //查询扣量结算金额
                    String querySql = "SELECT CONVERT ( SUM(income) / 1000, DECIMAL (10, 2) ) deductAmount, id FROM stat_settlement WHERE stat_time LIKE '" + time + "%' AND type = '2' AND settlement_obj = '2'";
                    if ("3".equals(queryType)) {
                        querySql += " and product_id = '" + productId + "' and id = '" + id + "'";
                    } else if ("4".equals(queryType)) {
                        querySql += " and id in (" + data.get(i).get("ids") + ")";
                    }
                    List<Map<String, Object>> list = dicDao.sqlQuery(querySql);
                    if (list.size() > 0) {
                        //扣量金额
                        data.get(i).put("deductAmount", String.valueOf(list.get(0).get("deductAmount")));
                        logger.info("扣量金额是：" + String.valueOf(list.get(0).get("deductAmount")));
                        //添加收益  首次录入 - 扣量录入
                        if (list.get(0).get("deductAmount") != null || data.get(i).get("income") != null) {
                            logger.info("首次录入金额是：" + String.valueOf(data.get(i).get("income")));
                            double profit = NumberConvertUtil.parseDouble(String.valueOf(data.get(i).get("income"))) - NumberConvertUtil.parseDouble(String.valueOf(list.get(0).get("deductAmount")));
                            DecimalFormat df = new DecimalFormat("#.00");
                            data.get(i).put("profit", df.format(profit));
                            logger.info("收益是：" + df.format(profit));
                        }
                    }
                }
            }
        }
        jsonObject.put("data", page.getData());
        jsonObject.put("total", page.getTotal());
        return jsonObject;
    }

}
