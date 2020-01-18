package com.bdaim.supplier.fund.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.entity.Dic;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.dic.service.DicService;
import com.bdaim.supplier.fund.dto.SearchPropertyDTO;
import com.bdaim.util.*;
import com.bdaim.common.dao.DicDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author duanliying
 * @date 2019/5/31
 * @description 机构service
 */
@Service("InstitutionService")
@Transactional
public class InstitutionService {
    private static Log log = LogFactory.getLog(InstitutionService.class);
    @Resource
    DicService dicService;
    @Resource
    CustomerDao customerDao;
    @Resource
    DicDao dicDao;

    /**
     * @description 新增机构信息
     * @author:duanliying
     * @method
     * @date: 2019/5/31 10:16
     */
    public void addInstitutionInfo(JSONObject json) throws Exception {
        //判断是编辑还是新增  0 编辑   1 新增
        String type = json.getString("type");
        String id = json.getString("id");
        log.info("机构id是：" + id + "操作类型是：" + type);
        //品牌id
        String brandId = json.getString("brandId");
        //机构名称
        String institutionName = json.getString("institutionName");
        //联系人
        String contactPerson = json.getString("contactPerson");
        //联系人电话
        String contactPhone = json.getString("contactPhone");
        //机构介绍
        String institutionInfo = json.getString("institutionInfo");
        //机构logo
        String institutionIogo = json.getString("institutionIogo");
        //结算信息
        String settlementInfo = json.getJSONObject("settlementInfo").toJSONString();
        if ("1".equals(type)) {
            //新增
            id = IDHelper.getID().toString();
            Customer customer = new Customer();
            customer.setCustId(id);
            customer.setBrandId(brandId);
            customer.setEnterpriseName(institutionName);
            customer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            customer.setSource("2");//2：金融超市   1：精准营销
            customerDao.save(customer);
            log.info("保存的机构id是：" + id);
        } else {
            Customer customer = customerDao.get(id);
            customer.setModifyTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            customer.setEnterpriseName(institutionName);
            customerDao.saveOrUpdate(customer);
        }
        //编辑机构信息
        if (StringUtil.isNotEmpty(contactPerson)) {
            CustomerProperty contactPersonInfo = customerDao.getProperty(id, "contactPerson");
            if (contactPersonInfo != null) {
                contactPersonInfo.setPropertyValue(contactPerson);
            } else {
                contactPersonInfo = new CustomerProperty(id, "contactPerson", contactPerson, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            }
            customerDao.saveOrUpdate(contactPersonInfo);
        }
        if (StringUtil.isNotEmpty(contactPhone)) {
            CustomerProperty contactPhoneInfo = customerDao.getProperty(id, "contactPhone");
            if (contactPhoneInfo != null) {
                contactPhoneInfo.setPropertyValue(contactPhone);
            } else {
                contactPhoneInfo = new CustomerProperty(id, "contactPhone", contactPhone, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            }
            customerDao.saveOrUpdate(contactPhoneInfo);
        }
        if (StringUtil.isNotEmpty(institutionInfo)) {
            CustomerProperty institutionInfoInfo = customerDao.getProperty(id, "institutionInfo");
            if (institutionInfoInfo != null) {
                institutionInfoInfo.setPropertyValue(institutionInfo);
            } else {
                institutionInfoInfo = new CustomerProperty(id, "institutionInfo", institutionInfo, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            }
            customerDao.saveOrUpdate(institutionInfoInfo);
        }
        if (StringUtil.isNotEmpty(institutionIogo)) {
            CustomerProperty institutionIogoInfo = customerDao.getProperty(id, "institutionIogo");
            if (institutionIogoInfo != null) {
                institutionIogoInfo.setPropertyValue(institutionIogo);
            } else {
                institutionIogoInfo = new CustomerProperty(id, "institutionIogo", institutionIogo, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            }
            customerDao.saveOrUpdate(institutionIogoInfo);
        }
        if (StringUtil.isNotEmpty(settlementInfo)) {
            CustomerProperty settlementInfoInfo = customerDao.getProperty(id, "settlementInfo");
            if (settlementInfoInfo != null) {
                settlementInfoInfo.setPropertyValue(settlementInfo);
            } else {
                settlementInfoInfo = new CustomerProperty(id, "settlementInfo", settlementInfo, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            }
            customerDao.saveOrUpdate(settlementInfoInfo);
        }
    }


    /**
     * @description 获取机构列表信息
     * @author:duanliying
     * @method
     * @date: 2019/5/31 13:25
     */
    public Page getInstitutionList(String dicType, String institutionName, String brandId, int pageNum, int pageSize) {
        Page page = null;
        List<Object> params = new ArrayList<>();
        try {
            StringBuffer querySql = new StringBuffer("SELECT c.enterprise_name institutionName,c.cust_id id,`status`,c.create_time ,brand_id brandId,");
            querySql.append("(SELECT COUNT(DISTINCT product_id) FROM t_fund_product_apply where product_id IN ( SELECT c.id FROM t_dic c LEFT JOIN t_dic_property p ON c.id = p.dic_id ");
            querySql.append("WHERE dic_prop_key = 'institution' AND dic_prop_value = c.cust_id ) AND match_status = 1 ");
            if (StringUtil.isNotEmpty(dicType)) {
                querySql.append("AND product_type = ?");
                params.add(dicType);
            }
            querySql.append(")successNum,( SELECT COUNT(DISTINCT dic_id) FROM t_dic c LEFT JOIN t_dic_property p ON c.id = p.dic_id WHERE p.dic_prop_key = 'institution' AND dic_prop_value = c.cust_id");
            if (StringUtil.isNotEmpty(dicType)) {
                querySql.append(" AND c.dic_type_id =?");
                params.add(dicType);
            }
            querySql.append(" ) productNum ");
            querySql.append(" FROM t_customer c LEFT JOIN t_customer_property p ON c.cust_id = p.cust_id WHERE 1 = 1");
            if (StringUtil.isNotEmpty(brandId)) {
                params.add(brandId);
                querySql.append(" and c.brand_id =? ");
            }
            if (StringUtil.isNotEmpty(institutionName)) {
                params.add("%"+institutionName+"%");
                querySql.append(" and c.enterprise_name LIKE ? ");
            }
            querySql.append(" AND c.source = 2 AND c. STATUS = 0 ");
            querySql.append(" GROUP BY c.cust_id ORDER BY successNum DESC,c.create_time DESC");
            page = customerDao.sqlPageQuery0(querySql.toString(), pageNum, pageSize, params.toArray());
            List<Map<String, Object>> data = page.getData();
            log.info("查询出信息是：" + data);
            if (data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    String id = String.valueOf(data.get(i).get("id"));
                    log.info("需要查询的机构id是：" + id);
                    //根据机构id查询商品信息
                    CustomerProperty imgUrl = customerDao.getProperty(id, "institutionIogo");
                    if (imgUrl != null) {
                        //logo
                        String imgValue = imgUrl.getPropertyValue();
                        if (imgValue.toLowerCase().endsWith(".jpg")
                                || imgValue.toLowerCase().endsWith(".png")
                                || imgValue.toLowerCase().endsWith(".gif")) {
                            data.get(i).put("imgUrl", ConfigUtil.getInstance().get("pic_server_url") + "/0/" + imgValue);
                        }
                    }
                    CustomerProperty settlementInfo = customerDao.getProperty(id, "settlementInfo");
                    Map<String, Object> map = new HashMap<>();
                    if (settlementInfo != null) {
                        String propertyValue = settlementInfo.getPropertyValue();
                        JSONObject jsonObject = JSONObject.parseObject(propertyValue);
                        JSONArray infoList = jsonObject.getJSONArray("product");
                        for (int j = 0; j < infoList.size(); j++) {
                            //商品类型
                            String type = infoList.getJSONObject(j).getString("type");
                            //结算类型
                            String settlementType = infoList.getJSONObject(j).getString("settlementType");
                            map.put(type, settlementType);
                        }
                    }
                    //结算信息
                    data.get(i).put("settlementInfo", map);
                }
            }
        } catch (Exception e) {
            log.error("查询机构列表异常" + e);
        }
        return page;
    }

    /**
     * @description 获取机构列表信息
     * @author:duanliying
     * @method
     * @date: 2019/5/31 13:25
     */
    public Page getUserRankingList(Integer pageNum, Integer pageSize, String dicType, Integer protectNum) throws Exception {
        Page page = null;
        try {
            StringBuffer querySql = new StringBuffer("SELECT c.cust_id institutionId, ( SELECT COUNT(DISTINCT product_id) FROM t_fund_product_apply WHERE product_id IN (");
            querySql.append(" SELECT c.id FROM t_dic c LEFT JOIN t_dic_property p ON c.id = p.dic_id ");
            querySql.append(" WHERE dic_prop_key = 'institution' AND dic_prop_value = c.cust_id ) ");
            querySql.append(" AND product_type = ? ) applyNum FROM t_customer c");
            querySql.append(" LEFT JOIN t_customer_property p ON c.cust_id = p.cust_id");
            querySql.append(" WHERE c.source = 2 AND c. STATUS = 0 GROUP BY c.cust_id ORDER BY applyNum DESC");
            List<Object> params = new ArrayList<>();
            params.add(dicType);
            page = customerDao.sqlPageQuery0(querySql.toString(), pageNum, pageSize, params.toArray());
            List<Map<String, Object>> data = page.getData();
            if (data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    String institutionId = String.valueOf(data.get(i).get("institutionId"));
                    Map<String, Object> institutionInfo = getInstitutionInfo(institutionId);
                    if (institutionInfo != null) {
                        data.get(i).put("brandLogo", institutionInfo.get("brandLogo"));
                        data.get(i).put("name", institutionInfo.get("name"));
                    }
                    log.info("需要查询的机构id是：" + institutionId);
                    SearchPropertyDTO searchPropertyDTO = new SearchPropertyDTO();
                    searchPropertyDTO.setDicType(dicType);
                    searchPropertyDTO.setType("1");
                    searchPropertyDTO.setInstitutionId(institutionId);
                    searchPropertyDTO.setApplicantsNum("1");
                    //调用商品列表接口（按照申请人数倒叙）
                    JSONObject json = dicService.page(protectNum, 0, searchPropertyDTO);
                    JSONArray list = json.getJSONArray("data");
                    data.get(i).put("productList", list);
                }
            }
        } catch (Exception e) {
            log.error("查询机构列表异常" + e);
        }
        return page;
    }

    /**
     * @description 删除机构信息逻辑删除
     * @author:duanliying
     * @method
     * @date: 2019/5/31 15:23
     */
    public Map<String, Object> deleteInstitution(String id) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<>();
        //查询机构下面是否有上架商品有无法删除
        int number = 0;
        if (number <= 0) {
            String updateSql = "UPDATE t_customer SET `status` = 3 WHERE cust_id = ? AND source = 2";
            int i = customerDao.executeUpdateSQL(updateSql, id);
            log.info("删除机构数量是：" + i);
            map.put("code", i);
            if (i == 1) {
                map.put("code", i);
                map.put("message", "成功");
            } else {
                map.put("message", "失败");
            }
        } else {
            map.put("code", 0);
            map.put("message", "该机构下有上架商品无法删除");
        }
        return map;
    }

    /**
     * @description 查看机构详情接口
     * @author:duanliying
     * @method
     * @date: 2019/5/31 16:48
     */
    public Map<String, Object> getInstitutionInfo(String id) {
        //根据机构id查询所属品牌名称
        Map<String, Object> map = null;
        try {
            Customer customer = customerDao.get(id);
            map = new HashMap<>();
            if (customer != null) {
                String name = "";
                String brandId = customer.getBrandId();
                log.info("机构id是：" + id + "机构所属品牌id是：" + brandId);
                //根据品牌id查询品牌名称
                Dic dic = dicDao.get(Long.parseLong(brandId));
                if (dic != null) {
                    log.info("查询品牌信息是" + String.valueOf(dic));
                    name = dic.getName();
                    map.put("name", name);
                }
                map.put("brandId", brandId);
                map.put("institutionName", customer.getEnterpriseName());
                //查询品牌log和品牌名称
                if (StringUtil.isNotEmpty(brandId)) {
                    DicProperty logo = dicDao.getProperty(NumberConvertUtil.parseLong(brandId), "logo");
                    String brandLogo = ConfigUtil.getInstance().get("pic_server_url") + "/0/" + logo.getDicPropValue();
                    map.put("brandLogo", brandLogo);
                    Dic dicEntity = dicDao.getDicEntity(NumberConvertUtil.parseLong(brandId));
                    if (dicEntity != null) {
                        map.put("brandName", dicEntity.getName());
                    }
                }
                //查询机构信息
                List<CustomerProperty> propertyAllList = customerDao.getPropertyAllList(id);
                log.info("查询机构信息集合是：" + String.valueOf(propertyAllList));
                if (propertyAllList != null && propertyAllList.size() > 0) {
                    for (int i = 0; i < propertyAllList.size(); i++) {
                        String propertyName = propertyAllList.get(i).getPropertyName();
                        String propertyValue = null;
                        if ("institutionIogo".equals(propertyName)) {
                            propertyValue = ConfigUtil.getInstance().get("pic_server_url") + "/0/" + propertyAllList.get(i).getPropertyValue();
                        } else {
                            propertyValue = propertyAllList.get(i).getPropertyValue();
                        }
                        map.put(propertyName, propertyValue);
                    }
                }
            }
        } catch (Exception e) {
            log.error("查询机构详情异常" + e);
        }
        return map;
    }

    /**
     * 检查商品名称是否存在
     *
     * @param name
     * @return
     */
    public boolean checkExistInstitutionName(String name) {
        Customer Customer = customerDao.getName(name);
        if (Customer != null) {
            return true;
        }
        return false;
    }
}
