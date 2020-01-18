package com.bdaim.dic.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.AppConfig;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dao.DicDao;
import com.bdaim.common.dao.DicTypeDao;
import com.bdaim.common.dto.DicTypeEnum;
import com.bdaim.common.dto.Page;
import com.bdaim.common.entity.Dic;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.common.entity.DicTypeProperty;
import com.bdaim.common.exception.ParamException;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.supplier.fund.dto.SearchPropertyDTO;
import com.bdaim.supplier.fund.entity.FeedBack;
import com.bdaim.supplier.fund.entity.FundProductApply;
import com.bdaim.template.dao.MarketTemplateDao;
import com.bdaim.template.entity.MarketTemplate;
import com.bdaim.util.*;
import com.github.crab2died.ExcelUtils;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


/**
 */
@Service("dicService")
@Transactional
public class DicService {
    private static Logger logger = LoggerFactory.getLogger(DicService.class);

    @Resource
    private DicDao dicDao;

    @Resource
    private DicTypeDao dicTypeDao;


    @Resource
    private CustomerDao customerDao;

    @Resource
    private CustomerUserDao customerUserDao;

    @Resource
    private MarketTemplateDao marketTemplateDao;

    public List<DicProperty> getDicProperty(Long id) throws Exception {
        List<DicProperty> propertyList = dicDao.getPropertyList(id);
        return propertyList;
    }

    /**
     * 分页查询
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    public JSONObject page(Integer pageSize, Integer pageNum, SearchPropertyDTO searchPropertyDto) {
        JSONObject data = new JSONObject();
        Page page = null;
        logger.info("查询商品类型是：" + searchPropertyDto.getDicType());
        String type = searchPropertyDto.getType();
        if ("1".equals(type) || "0".equals(type)) {
            //商品列表
            page = getProductListInfo(pageSize, pageNum, searchPropertyDto);
        } else if ("2".equals(type)) {
            //同类推荐列表
            page = getSimilarListInfo(pageSize, pageNum, searchPropertyDto);
        } else if ("7".equals(type)) {
            //渠道列表
            page = pageExtensionChannel(pageSize, pageNum, searchPropertyDto);
        } else if ("8".equals(type)) {
            //活动页列表
            page = pageActivity(pageSize, pageNum, searchPropertyDto);
        } else if ("9".equals(type)) {
            //推广活动列表
            page = pageExtensionActivity(pageSize, pageNum, searchPropertyDto);
        } else if ("10".equals(type)) {
            //广告位列表
            page = pageAdSpace(pageSize, pageNum, searchPropertyDto);
        } else {
            //品牌列表
            page = getBrandListInfo(pageSize, pageNum, searchPropertyDto);
        }
        if (page != null && page.getData() != null) {
            Map<String, Object> m;
            List<DicProperty> propertyList;
            for (int i = 0; i < page.getData().size(); i++) {
                m = (Map<String, Object>) page.getData().get(i);
                m.put("id", String.valueOf(m.get("id")));
                propertyList = dicDao.getPropertyList(NumberConvertUtil.parseLong(m.get("id")));
                if (propertyList == null || propertyList.size() == 0) {
                    continue;
                }
                for (DicProperty c : propertyList) {
                    if (StringUtil.isEmpty(c.getDicPropKey())) {
                        continue;
                    }
                    if (DicTypeEnum.K.getId().equals(String.valueOf(m.get("dic_type_id")))
                            && "activityUrl".equals(c.getDicPropKey())) {
                        continue;
                    }
                    m.put(c.getDicPropKey(), c.getDicPropValue());
                    // 处理图片服务器地址
                    if (StringUtil.isNotEmpty(c.getDicPropValue()) && (c.getDicPropValue().toLowerCase().endsWith(".jpg")
                            || c.getDicPropValue().toLowerCase().endsWith(".png")
                            || c.getDicPropValue().toLowerCase().endsWith(".gif"))) {
                        m.put(c.getDicPropKey(), ConfigUtil.getInstance().get("pic_server_url") + "/0/" + c.getDicPropValue());
                    }
                }
            }
        }
        data.put("data", page.getData());
        data.put("total", page.getTotal());
        return data;
    }

    /**
     * 查询同类推荐列表
     *
     * @param searchPropertyDto
     */
    private Page getSimilarListInfo(Integer pageSize, Integer pageNum, SearchPropertyDTO searchPropertyDto) {
        Page page = null;
        List args = new ArrayList();
        try {
            if (StringUtil.isEmpty(searchPropertyDto.getDicType())) {
                throw new ParamException(" param dicType is required");
            }
            String sql = "select distinct a.* ,(SELECT  case when dic_prop_value ='' then '999'  when dic_prop_value is null then '999' ELSE dic_prop_value END  FROM t_dic_property WHERE dic_prop_key = 'productShowLevel' and dic_id=a.id) productShowLevel,";
            sql += "(SELECT COUNT(DISTINCT user_id) FROM t_fund_product_apply WHERE product_id = a.id ) applyNum from t_dic a where a.dic_type_id=? AND a.status = 1 ";
            args.add(searchPropertyDto.getDicType());
            //地域检索
            if (StringUtil.isNotEmpty(searchPropertyDto.getCity())) {
                String[] citys = searchPropertyDto.getCity().split(",");
                //威海市
                if (citys.length > 0) {
                    sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_key='area' AND  dic_prop_value LIKE '%";
                    logger.info("同类推荐的类型是：" + searchPropertyDto.getDicType() + "所属区域是：" + searchPropertyDto.getCity());
                    for (int i = 0; i < citys.length; i++) {
                        sql += citys[i] + "%";
                    }
                    sql += "' OR dic_prop_value LIKE '%全国%')";
                }
            }
            //职业身份
            if (StringUtil.isNotEmpty(searchPropertyDto.getProfessionalIdentity())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like ? and dic_prop_key='professionalIdentity')";
                args.add("%" + searchPropertyDto.getProfessionalIdentity() + "%");
            }
            //月收入
            if (StringUtil.isNotEmpty(searchPropertyDto.getRevenueConfigValue())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where  dic_prop_key='revenueConfigValue' and dic_prop_value<=?)";
                args.add(searchPropertyDto.getRevenueConfigValue());
            }
            //公积金
            if (StringUtil.isNotEmpty(searchPropertyDto.getAccumulationFund())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET(?,dic_prop_value) AND dic_prop_key='accumulationFund')";
                args.add(searchPropertyDto.getAccumulationFund());
            }
            //推荐设置（热门推荐和主题推荐）
            if (StringUtil.isNotEmpty(searchPropertyDto.getRecommendConfig())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like ? and dic_prop_key='recommendConfig')";
                args.add("%" + searchPropertyDto.getRecommendConfig() + "%");
            }
            //社保
            if (StringUtil.isNotEmpty(searchPropertyDto.getSocialSecurity())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET(?,dic_prop_value) AND dic_prop_key='socialSecurity')";
                args.add(searchPropertyDto.getSocialSecurity());
            }
            //本地社保连续缴存时间
            if (StringUtil.isNotEmpty(searchPropertyDto.getAccumulationFundTime())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where  dic_prop_key='accumulationFundTime' and dic_prop_value<=?)";
                args.add(searchPropertyDto.getAccumulationFundTime());
            }
            //本地社保连续缴存基数
            if (StringUtil.isNotEmpty(searchPropertyDto.getAccumulationFundValue())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where  dic_prop_key='accumulationFundValue' and dic_prop_value<=?)";
                args.add(searchPropertyDto.getAccumulationFundValue());
            }
            //名下房产
            if (StringUtil.isNotEmpty(searchPropertyDto.getHouseConfig())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like ? and dic_prop_key='houseConfig')";
                args.add("%" + searchPropertyDto.getHouseConfig() + "%");
            }
            //收入配置
            if (StringUtil.isNotEmpty(searchPropertyDto.getRevenueConfig())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'revenueConfig' AND " + searchPropertyDto.getRevenueConfig() + "=dic_prop_value)";
            }
            //经营流水
            if (StringUtil.isNotEmpty(searchPropertyDto.getManageAmount())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'manageAmount' AND " + searchPropertyDto.getManageAmount() + ">=dic_prop_value)";
            }
            //经营年限
            if (StringUtil.isNotEmpty(searchPropertyDto.getManageMonth())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'manageMonth' AND " + searchPropertyDto.getManageMonth() + ">=dic_prop_value)";
            }
            //经营执照  1 本地  2 外地
            if (StringUtil.isNotEmpty(searchPropertyDto.getManageArea())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'manageArea' AND " + searchPropertyDto.getManageArea() + "=dic_prop_value)";
            }
            //对公账户收入
            if (StringUtil.isNotEmpty(searchPropertyDto.getBusinessOwnerConfig())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'businessOwnerConfig' AND " + searchPropertyDto.getBusinessOwnerConfig() + ">=dic_prop_value)";
            }
            //贷款金额
            if (StringUtil.isNotEmpty(searchPropertyDto.getLoanAmountStart())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'loanAmountStart' AND " + searchPropertyDto.getLoanAmountStart() + ">=dic_prop_value  )";
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'loanAmountEnd' AND " + searchPropertyDto.getLoanAmountStart() + "<=dic_prop_value  )";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getLoanAmountEnd())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'loanAmountEnd' AND " + searchPropertyDto.getLoanAmountEnd() + "<=dic_prop_value  )";
            }
            //贷款期限
            if (StringUtil.isNotEmpty(searchPropertyDto.getTermStart())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'termStart' AND " + searchPropertyDto.getTermStart() + ">=dic_prop_value  )";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getTermEnd())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'termEnd' AND " + searchPropertyDto.getTermEnd() + "<=dic_prop_value  )";
            }
            //信用情况
            if (StringUtil.isNotEmpty(searchPropertyDto.getCreditSituation())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like ? and dic_prop_key='creditSituation')";
                args.add("%" + searchPropertyDto.getCreditSituation() + "%");
            }
            //行业类型检索
            if (StringUtil.isNotEmpty(searchPropertyDto.getIndustryType())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET(?,dic_prop_value) AND dic_prop_key='type')";
                args.add(searchPropertyDto.getIndustryType());
            }
            //名下是否有车
            if (StringUtil.isNotEmpty(searchPropertyDto.getCarType())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like ? and dic_prop_key='carType')";
                args.add("%" + searchPropertyDto.getCarType() + "%");
            }
            //特色标签（理财）
            if (StringUtil.isNotEmpty(searchPropertyDto.getFeaturedLabels())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like ? and dic_prop_key='featuredLabels')";
                args.add("%" + searchPropertyDto.getFeaturedLabels() + "%");
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getProductId())) {
                //过滤掉当前商品
                sql += " AND a.id !=?" ;
                args.add(searchPropertyDto.getProductId());
            }
            sql += " order by CAST(productShowLevel as SIGNED), applyNum DESC";
            page = dicDao.sqlPageQueryByPageSize(sql, pageNum, pageSize,args.toArray());
            List<Map<String, Object>> data = page.getData();
            //推荐商品不满足pageSize 是否需要填充数据 1 是 2 否
            if (!"2".equals(searchPropertyDto.getFillType())) {
                if (StringUtil.isNotEmpty(searchPropertyDto.getRecommendConfig()) && data.size() <= pageSize) {
                    StringBuffer querySql = new StringBuffer("SELECT d.* FROM t_dic d WHERE d.id NOT IN ( SELECT dic_id FROM t_dic_property WHERE dic_prop_value LIKE ? AND dic_prop_key = 'recommendConfig' )");
                    List params=new ArrayList();
                    params.add("%" + searchPropertyDto.getRecommendConfig() + "%");
                    if (StringUtil.isNotEmpty(searchPropertyDto.getIndustryType())) {
                        querySql.append(" AND d.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET(?,dic_prop_value) AND dic_prop_key='type')");
                        params.add(searchPropertyDto.getIndustryType());
                    }
                    querySql.append(" and d.status = 1 and d.dic_type_id = ? ORDER BY RAND() LIMIT " + (pageSize - data.size()));
                    params.add(searchPropertyDto.getDicType());
                    //随机查询n条相关数据
                    List<Map<String, Object>> list = dicDao.sqlQuery(querySql.toString(),params);
                    if (list != null && list.size() > 0) {
                        data.addAll(list);
                    }
                    page.setTotal(1000);
                }
            }
            for (Map<String, Object> map : data) {
                Long prodectId = NumberConvertUtil.parseLong(map.get("id"));
                logger.info("商品id是：" + prodectId);
                //所属机构
                DicProperty institutionConfig = dicDao.getProperty(prodectId, "institution");
                if (institutionConfig != null && StringUtil.isNotEmpty(institutionConfig.getDicPropValue())) {
                    //根据机构id查询机构名称
                    logger.info("所属机构是：" + institutionConfig.getDicPropValue());
                    String enterpriseName = customerDao.getEnterpriseName(institutionConfig.getDicPropValue());
                    map.put("institutionName", enterpriseName);
                }
            }

        } catch (ParamException e) {
            logger.error("查询推荐商品异常 ： " + e);
        }
        return page;
    }

    /**
     * 推广渠道分页
     *
     * @param pageSize
     * @param pageNum
     * @param searchPropertyDto
     * @return
     */
    private Page pageExtensionChannel(Integer pageSize, Integer pageNum, SearchPropertyDTO searchPropertyDto) {
        Page page = null;
        List args = new ArrayList();
        try {
            String sql = "select id,name,dic_type_id,create_time,last_update_time,status from t_dic a where a.dic_type_id=?";
            args.add(DicTypeEnum.I.getId());
            //渠道检索
            if (searchPropertyDto.getChannelType() != null) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET(?,dic_prop_value) AND dic_prop_key='type')";
                args.add(searchPropertyDto.getChannelType());
            }
            //名称检索
            if (StringUtil.isNotEmpty(searchPropertyDto.getName())) {
                sql += " AND a.name LIKE ?";
                args.add("%" + searchPropertyDto.getName() + "%");
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getStatus())) {
                sql += " AND a.status=?" ;
                args.add(searchPropertyDto.getStatus());
            }
            sql += " ORDER BY a.create_time DESC ";
            page = dicDao.sqlPageQuery0(sql, pageNum, pageSize,args.toArray());
        } catch (Exception e) {
            logger.error("查询渠道列表异常,", e);
        }
        return page;
    }

    /**
     * 活动页分页
     *
     * @param pageSize
     * @param pageNum
     * @param searchPropertyDto
     * @return
     */
    private Page pageActivity(Integer pageSize, Integer pageNum, SearchPropertyDTO searchPropertyDto) {
        Page page = null;
        List args = new ArrayList();
        try {
            String sql = "select id,name,dic_type_id,create_time,last_update_time,status from t_dic a where a.dic_type_id='" + DicTypeEnum.J.getId() + "'";
            //名称检索
            if (StringUtil.isNotEmpty(searchPropertyDto.getName())) {
                sql += " AND a.name LIKE ?";
                args.add("%"+searchPropertyDto.getName()+"%");
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getStatus())) {
                sql += " AND a.status=?";
                args.add(searchPropertyDto.getStatus());
            }
            sql += " ORDER BY a.create_time DESC ";
            page = dicDao.sqlPageQuery0(sql, pageNum, pageSize,args.toArray());
            if (page != null && page.getData() != null) {
                Map<String, Object> m;
                DicProperty property;
                String[] productIds;
                String productNames = "";
                Dic dic;
                for (int i = 0; i < page.getData().size(); i++) {
                    m = (Map<String, Object>) page.getData().get(i);
                    m.put("productSum", 0);
                    property = dicDao.getProperty(NumberConvertUtil.parseLong(m.get("id")), "products");
                    if (property == null) {
                        continue;
                    }
                    if (StringUtil.isEmpty(property.getDicPropValue())) {
                        continue;
                    }
                    productIds = property.getDicPropValue().split(",");
                    // 产品数量
                    m.put("productSum", productIds.length);
                    // 产品名称
                    for (String id : productIds) {
                        dic = dicDao.get(NumberConvertUtil.parseLong(id));
                        if (dic != null) {
                            productNames += dic.getName() + ",";
                        }
                    }
                    m.put("productNames", productNames);
                }

            }
        } catch (Exception e) {
            logger.error("查询活动页分页异常,", e);
        }
        return page;
    }

    /**
     * 推广活动分页
     *
     * @param pageSize
     * @param pageNum
     * @param dto
     * @return
     */
    private Page pageExtensionActivity(Integer pageSize, Integer pageNum, SearchPropertyDTO dto) {
        Page page = null;
        List args = new ArrayList();
        try {
            String sql = "select id,name,dic_type_id,create_time,last_update_time,status from t_dic a where a.dic_type_id='" + DicTypeEnum.K.getId() + "'";
            //名称检索
            if (StringUtil.isNotEmpty(dto.getName())) {
                sql += " AND a.name LIKE ?";
                args.add("%" + dto.getName() + "%");
            }
            //活动开始时间
            if (StringUtil.isNotEmpty(dto.getActivityStartTime())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value >= ? AND dic_prop_key='startTime')";
                args.add(dto.getActivityStartTime());
            }
            //活动结束时间
            if (StringUtil.isNotEmpty(dto.getActivityEndTime())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value <= ? AND dic_prop_key='endTime')";
                args.add(dto.getActivityEndTime());
            }
            //渠道检索
            if (StringUtil.isNotEmpty(dto.getExtensionChannel())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value = ? AND dic_prop_key='extensionChannel')";
                args.add(dto.getExtensionChannel());
            }
            //推广链接
            if (StringUtil.isNotEmpty(dto.getExtensionUrl())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value = ? AND dic_prop_key='extensionTypeValue')";
                args.add(dto.getExtensionUrl());
            }
            //触达方式
            if (StringUtil.isNotEmpty(dto.getTouchType())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value = ? AND dic_prop_key='touchType')";
                args.add(dto.getTouchType());
            }
            if (StringUtil.isNotEmpty(dto.getStatus())) {
                sql += " AND a.status=?";
                args.add(dto.getStatus());
            }
            sql += " ORDER BY a.create_time DESC ";
            page = dicDao.sqlPageQuery0(sql, pageNum, pageSize,args.toArray());
            if (page != null) {
                List<Map<String, Object>> data = null;
                String statSql = "SELECT IFNULL(SUM(regedit_num),0) regeditNum, IFNULL(SUM(firstget_num),0) firstgetNum, IFNULL(SUM(active_num),0) activeNum," +
                        "IFNULL(SUM(income)/1000,0) amount FROM stat_settlement WHERE id = ? AND type = 1";
                // 查询活动线索数量
                Map<String, Object> m;
                Dic dic;
                DicProperty dicProperty;
                LocalDateTime now = LocalDateTime.now(), startTime = null, endTime = null;
                MarketTemplate marketTemplate;
                for (int i = 0; i < page.getData().size(); i++) {
                    m = (Map<String, Object>) page.getData().get(i);
                    if (m != null) {
                        m.put("regeditNum", 0);
                        m.put("firstgetNum", 0);
                        m.put("activeNum", 0);
                        m.put("amount", 0.00);
                        try {
                            data = dicDao.sqlQuery(statSql, m.get("id"));
                            for (Map.Entry<String, Object> stat : data.get(0).entrySet()) {
                                m.put(stat.getKey(), stat.getValue());
                            }
                        } catch (SQLGrammarException e) {
                            logger.error("查询商品结算统计表异常,", e);
                        }
                        // 查询渠道名称
                        m.put("extensionChannelName", "");
                        String channelId = "";
                        dicProperty = dicDao.getProperty(NumberConvertUtil.parseLong(m.get("id")), "extensionChannel");
                        if (dicProperty != null) {
                            channelId = dicProperty.getDicPropValue();
                            dic = dicDao.get(NumberConvertUtil.parseLong(dicProperty.getDicPropValue()));
                            if (dic != null) {
                                m.put("extensionChannelName", dic.getName());
                            }
                        }

                        // 查询短信模板名称
                        m.put("smsTemplateName", "");
                        dicProperty = dicDao.getProperty(NumberConvertUtil.parseLong(m.get("id")), "smsTemplateId");
                        if (dicProperty != null && StringUtil.isNotEmpty(dicProperty.getDicPropValue())) {
                            marketTemplate = marketTemplateDao.get(NumberConvertUtil.parseInt(dicProperty.getDicPropValue()));
                            if (marketTemplate != null) {
                                m.put("smsTemplateName", marketTemplate.getTitle());
                            }
                        }
                        // 查询产品或活动页名称
                        m.put("extensionTypeName", "");
                        // 处理产品链接或活动页ID或链接
                        m.put("originalUrl", "");
                        dicProperty = dicDao.getProperty(NumberConvertUtil.parseLong(m.get("id")), "extensionType");
                        if (dicProperty != null) {
                            // 填写的为产品链接
                            if ("3".equals(dicProperty.getDicPropValue())) {
                                dicProperty = dicDao.getProperty(NumberConvertUtil.parseLong(m.get("id")), "extensionTypeValue");
                                if (dicProperty != null) {
                                    m.put("originalUrl", dicProperty.getDicPropValue());
                                }
                            } else {
                                dicProperty = dicDao.getProperty(NumberConvertUtil.parseLong(m.get("id")), "extensionTypeValue");
                                if (dicProperty != null) {
                                    dic = dicDao.get(NumberConvertUtil.parseLong(dicProperty.getDicPropValue()));
                                    if (dic != null) {
                                        m.put("extensionTypeName", dic.getName());
                                        m.put("originalUrl", DicTypeEnum.getUrl(dic.getDicTypeId()) + dic.getId());
                                    }
                                }
                            }
                        }
                        String originalUrl = String.valueOf(m.get("originalUrl"));
                        if (StringUtil.isNotEmpty(originalUrl)) {
                            if (originalUrl.indexOf("?") > 0) {
                                // 活动专属url处理
                                m.put("activityUrl", originalUrl + "&activityId=" + m.get("id") + "&channelId=" + channelId);
                            } else {
                                // 活动专属url处理
                                m.put("activityUrl", originalUrl + "?activityId=" + m.get("id") + "&channelId=" + channelId);
                            }
                        }
                        // 短链接处理
                        String sUrl = ShortUrlUtil.generateShortUrl(String.valueOf(m.get("activityUrl")));
                        m.put("shortUrl", sUrl);

                        // 处理活动状态
                        if (NumberConvertUtil.parseInt(m.get("status")) == 2) {
                            //已停止
                            m.put("activityStatus", 3);
                        } else {
                            try {
                                dicProperty = dicDao.getProperty(NumberConvertUtil.parseLong(m.get("id")), "startTime");
                                if (dicProperty != null && StringUtil.isNotEmpty(dicProperty.getDicPropValue())) {
                                    startTime = LocalDateTime.parse(dicProperty.getDicPropValue(), DatetimeUtils.DATE_TIME_FORMATTER);
                                }
                                dicProperty = dicDao.getProperty(NumberConvertUtil.parseLong(m.get("id")), "endTime");
                                if (dicProperty != null && StringUtil.isNotEmpty(dicProperty.getDicPropValue())) {
                                    endTime = LocalDateTime.parse(dicProperty.getDicPropValue(), DatetimeUtils.DATE_TIME_FORMATTER);
                                }
                                // 未到活动开始时间
                                if (now.isBefore(startTime)) {
                                    logger.warn("推广活动:" + m.get("id") + "未到活动开始时间");
                                    // 未开始
                                    m.put("activityStatus", 1);
                                    continue;
                                }
                                // 已过活动结束时间
                                if (now.isAfter(endTime)) {
                                    logger.warn("推广活动:" + m.get("id") + "已过活动结束时间");
                                    // 已结束
                                    m.put("activityStatus", 3);
                                    continue;
                                }
                                //进行中
                                m.put("activityStatus", 2);
                            } catch (DateTimeParseException e) {
                                logger.error("推广活动时间转换异常,", e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询推广活动分页异常,", e);
        }
        return page;
    }

    /**
     * 广告位分页
     *
     * @param pageSize
     * @param pageNum
     * @param dto
     * @return
     */
    private Page pageAdSpace(Integer pageSize, Integer pageNum, SearchPropertyDTO dto) {
        Page page = null;
        List args = new ArrayList();
        try {
            String sql = "select id,name,dic_type_id,create_time,last_update_time,status from t_dic a where a.dic_type_id='" + DicTypeEnum.E.getId() + "'";
            if (dto.getAdPlatform() != null) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value =  ? AND dic_prop_key='platform')";
                args.add(dto.getAdPlatform());
            }
            page = dicDao.sqlPageQuery0(sql, pageNum, pageSize,args.toArray());
        } catch (Exception e) {
            logger.error("查询广告位列表异常,", e);
        }
        return page;
    }

    /**
     * 查询商品列表信息
     *
     * @param searchPropertyDto
     */
    private Page getProductListInfo(Integer pageSize, Integer pageNum, SearchPropertyDTO searchPropertyDto) {
        Page page = null;
        List args = new ArrayList();
        try {
            String brandId = searchPropertyDto.getBrandId();
            // 查询申请人数
            String sql = "select distinct a.* ";
            if (StringUtil.isNotEmpty(searchPropertyDto.getApplicantsNum())) {
                sql += (",(SELECT COUNT(DISTINCT user_id) FROM t_fund_product_apply WHERE product_id = a.id ) num");
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getQuota())) {
                //额度
                sql += ", ( SELECT f.dic_prop_value FROM t_dic_property f WHERE f.dic_prop_key = \"loanAmountEnd\" AND f.dic_id = a.id ) loanAmount";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getDurationDay())) {
                //理财封闭期
                sql += ", ( SELECT f.dic_prop_value FROM t_dic_property f WHERE f.dic_prop_key = \"durationDay\" AND f.dic_id = a.id ) durationDay";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getRiskLevel())) {
                //理财风险等级
                sql += ", ( SELECT f.dic_prop_value FROM t_dic_property f WHERE f.dic_prop_key = \"riskLevel\" AND f.dic_id = a.id ) riskLevel";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getMinPurchaseAmount())) {
                //起购额度
                sql += ", ( SELECT f.dic_prop_value FROM t_dic_property f WHERE f.dic_prop_key = \"minPurchaseAmount\" AND f.dic_id = a.id ) minPurchaseAmount";
            }
            sql += " FROM t_dic a LEFT JOIN t_dic_property f ON a.id = f.dic_id where 1=1 and a.dic_type_id in('B','G','H')";
            //地域检索
            if (StringUtil.isNotEmpty(searchPropertyDto.getCity())) {
                String[] citys = searchPropertyDto.getCity().split(",");
                //山东省,威海市
                if (citys.length > 0) {
                    sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_key='area' AND  dic_prop_value LIKE '%";
                    logger.info("同类推荐的类型是：" + searchPropertyDto.getDicType() + "所属区域是：" + searchPropertyDto.getCity());
                    for (int i = 0; i < citys.length; i++) {
                        sql += citys[i] + "%";
                    }
                    sql += "' OR dic_prop_value LIKE '%全国%')";
                }
            }
            //商品名称
            if (StringUtil.isNotEmpty(searchPropertyDto.getName())) {
                sql += " and a.name like '%" + searchPropertyDto.getName() + "%'";
            }
            //商品类型  B贷款 G信用卡 H理财
            if (StringUtil.isNotEmpty(searchPropertyDto.getDicType())) {
                sql += " and a.dic_type_id  = '" + searchPropertyDto.getDicType() + "'";
            }
            //商品id
            if (StringUtil.isNotEmpty(searchPropertyDto.getProductId())) {
                sql += " and a.id  = '" + searchPropertyDto.getProductId() + "'";
            }
            //处理进行品牌id检索
            if (StringUtil.isNotEmpty(brandId) && StringUtil.isEmpty(searchPropertyDto.getInstitutionId())) {
                logger.info("品牌id是：" + brandId);
                //如果品牌id存在查询品牌下所有的机构id
                String getInstitution = "SELECT GROUP_CONCAT(cust_id) ids FROM t_customer WHERE brand_id = " + brandId + " AND source = 2 and `status`!=3";
                List<Map<String, Object>> list = dicDao.sqlQuery(getInstitution);
                if (list.size() > 0) {
                    String ids = String.valueOf(list.get(0).get("ids"));
                    sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET(dic_prop_value,'" + ids + "') AND dic_prop_key='institution')";
                }
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getInstitutionId())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET('" + searchPropertyDto.getInstitutionId() + "',dic_prop_value) AND dic_prop_key='institution')";
            }
            //商品状态
            if (StringUtil.isNotEmpty(searchPropertyDto.getStatus())) {
                // 检索时仓库中包含定时上架和仓库中的商品
                if ("3".equals(searchPropertyDto.getStatus())) {
                    sql += " and a.status IN (2, 3)";
                } else {
                    sql += " and a.status =" + searchPropertyDto.getStatus();
                }
            }
            //贷款相关检索
            //贷款期限
            if (StringUtil.isNotEmpty(searchPropertyDto.getTermStart())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'termStart' AND " + searchPropertyDto.getTermStart() + ">=dic_prop_value  )";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getTermEnd())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'termEnd' AND " + searchPropertyDto.getTermEnd() + "<=dic_prop_value  )";
            }
            //贷款金额
            if (StringUtil.isNotEmpty(searchPropertyDto.getLoanAmountStart())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'loanAmountStart' AND " + searchPropertyDto.getLoanAmountStart() + ">=dic_prop_value  )";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getLoanAmountEnd())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'loanAmountEnd' AND " + searchPropertyDto.getLoanAmountEnd() + "<=dic_prop_value  )";
            }
            //职业身份
            if (StringUtil.isNotEmpty(searchPropertyDto.getProfessionalIdentity())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getProfessionalIdentity() + "%' and dic_prop_key='professionalIdentity')";
            }
            //名下房产
            if (StringUtil.isNotEmpty(searchPropertyDto.getHouseConfig())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getHouseConfig() + "%' and dic_prop_key='houseConfig')";
            }
            //信用情况
            if (StringUtil.isNotEmpty(searchPropertyDto.getCreditSituation())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getCreditSituation() + "%' and dic_prop_key='creditSituation')";
            }
            //名下是否有车
            if (StringUtil.isNotEmpty(searchPropertyDto.getCarType())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getCarType() + "%' and dic_prop_key='carType')";
            }
            //收入配置
            if (StringUtil.isNotEmpty(searchPropertyDto.getRevenueConfig())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'revenueConfig' AND " + searchPropertyDto.getRevenueConfig() + "=dic_prop_value)";
            }
            //月收入
            if (StringUtil.isNotEmpty(searchPropertyDto.getRevenueConfigValue())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where  dic_prop_key='revenueConfigValue' and dic_prop_value<=" + searchPropertyDto.getRevenueConfigValue() + ")";
            }
            //经营流水
            if (StringUtil.isNotEmpty(searchPropertyDto.getManageAmount())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'manageAmount' AND " + searchPropertyDto.getManageAmount() + ">=dic_prop_value)";
            }
            //经营年限
            if (StringUtil.isNotEmpty(searchPropertyDto.getManageMonth())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'manageMonth' AND " + searchPropertyDto.getManageMonth() + ">=dic_prop_value)";
            }
            //经营执照  1 本地  2 外地
            if (StringUtil.isNotEmpty(searchPropertyDto.getManageArea())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'manageArea' AND " + searchPropertyDto.getManageArea() + "=dic_prop_value)";
            }
            //是否注册过营业执照  1:注册  我：未注册
            if (StringUtil.isNotEmpty(searchPropertyDto.getManageLicenseStatus())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'manageLicenseStatus' AND " + searchPropertyDto.getManageLicenseStatus() + "=dic_prop_value)";
            }
            //对公账户收入
            if (StringUtil.isNotEmpty(searchPropertyDto.getBusinessOwnerConfig())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'businessOwnerConfig' AND " + searchPropertyDto.getBusinessOwnerConfig() + ">=dic_prop_value)";
            }
            //特点检索字段
            String trait = searchPropertyDto.getTrait();
            if (StringUtil.isNotEmpty(trait)) {
                logger.info("检索的特点是：" + trait);
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_key='productFeatures' AND dic_prop_value like '%" + trait + "%')";
            }
            //信用卡相关检索
            //卡片用途
            if (StringUtil.isNotEmpty(searchPropertyDto.getUseClass())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getUseClass() + "%' and dic_prop_key='useClass')";
            }
            //币种
            if (StringUtil.isNotEmpty(searchPropertyDto.getCurrency())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getCurrency() + "%' and dic_prop_key='currency')";
            }
            //卡片等级
            if (StringUtil.isNotEmpty(searchPropertyDto.getCardLevel())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getCardLevel() + "%' and dic_prop_key='cardLevel')";
            }
            //卡片组织
            if (StringUtil.isNotEmpty(searchPropertyDto.getCardOrganization())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getCardOrganization() + "%' and dic_prop_key='cardOrganization')";
            }
            //机构（银行）
            if (StringUtil.isNotEmpty(searchPropertyDto.getInstitutionId())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET('" + searchPropertyDto.getInstitutionId() + "',dic_prop_value) AND dic_prop_key='institution')";
            }
            //投资类型 1:银行 2：保险 3：互金
            if (StringUtil.isNotEmpty(searchPropertyDto.getIndustryType())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET('" + searchPropertyDto.getIndustryType() + "',dic_prop_value) AND dic_prop_key='type')";
            }
            //安全等級
            if (StringUtil.isNotEmpty(searchPropertyDto.getSafeRank())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where FIND_IN_SET('" + searchPropertyDto.getSafeRank() + "',dic_prop_value) AND dic_prop_key='riskLevel')";
            }
            //产品特点
            if (StringUtil.isNotEmpty(searchPropertyDto.getProductSpecial())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getProductSpecial() + "%' and dic_prop_key='productSpecial')";
            }
            //特色标签（理财）
            if (StringUtil.isNotEmpty(searchPropertyDto.getFeaturedLabels())) {
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value like '%" + searchPropertyDto.getFeaturedLabels() + "%' and dic_prop_key='featuredLabels')";
            }
            //理财封闭期
            if (StringUtil.isNotEmpty(searchPropertyDto.getTimeType())) {
                //封闭期类型 1：1到3个月   2：3到6个月 3：6个月以上
                int startDay = 0, endDay = 0;
                if ("1".equals(searchPropertyDto.getTimeType())) {
                    startDay = 30;
                    endDay = 90;
                } else if ("2".equals(searchPropertyDto.getTimeType())) {
                    startDay = 90;
                    endDay = 180;
                } else if ("3".equals(searchPropertyDto.getTimeType())) {
                    startDay = 180;
                }
                //理财封闭期开始时间
                if (startDay > 0) {
                    sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'durationDay' AND " + startDay + "<=dic_prop_value ";
                }
                if (endDay > 0) {
                    sql += "AND " + endDay + ">=dic_prop_value";
                }
                sql += ")";
            }
            //投资期限
            if (StringUtil.isNotEmpty(searchPropertyDto.getInvestmentTerm())) {
                //封闭期类型 1：1个月内|2：1-3个月|3：半年至1年|4：1年及以上
                int startDay = 0, endDay = 0;
                if ("1".equals(searchPropertyDto.getInvestmentTerm())) {
                    startDay = 0;
                    endDay = 30;
                } else if ("2".equals(searchPropertyDto.getInvestmentTerm())) {
                    startDay = 30;
                    endDay = 90;
                } else if ("3".equals(searchPropertyDto.getInvestmentTerm())) {
                    startDay = 180;
                    endDay = 360;
                } else if ("4".equals(searchPropertyDto.getInvestmentTerm())) {
                    startDay = 360;
                }
                sql += " AND a.id in (SELECT dic_id FROM t_dic_property WHERE dic_prop_key = 'durationDay' AND " + startDay + "<=dic_prop_value ";
                if (endDay > 0) {
                    sql += "AND " + endDay + ">=dic_prop_value";
                }
                sql += ")";
            }
            sql += " order by ";
            String orderStr = "";
            //根据申请人数排序   0 正序 1 倒叙
            if (StringUtil.isNotEmpty(searchPropertyDto.getApplicantsNum()) && "0".equals(searchPropertyDto.getApplicantsNum())) {
                orderStr += " num ,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getApplicantsNum()) && "1".equals(searchPropertyDto.getApplicantsNum())) {
                orderStr += " num desc ,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getQuota()) && "0".equals(searchPropertyDto.getQuota())) {
                //额度排序 0 正序 1 倒叙
                orderStr += " loanAmount,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getQuota()) && "1".equals(searchPropertyDto.getQuota())) {
                orderStr += " loanAmount desc,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getComprehensive()) && "0".equals(searchPropertyDto.getComprehensive())) {
                //综合排序 0 正序 1 倒叙
                orderStr += " a.create_time ,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getComprehensive()) && "1".equals(searchPropertyDto.getComprehensive())) {
                orderStr += " a.create_time desc,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getDurationDay()) && "0".equals(searchPropertyDto.getDurationDay())) {
                //封闭区
                orderStr += " durationDay,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getDurationDay()) && "1".equals(searchPropertyDto.getDurationDay())) {
                orderStr += " durationDay desc,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getRiskLevel()) && "0".equals(searchPropertyDto.getRiskLevel())) {
                //风险等级
                orderStr += " riskLevel,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getRiskLevel()) && "1".equals(searchPropertyDto.getRiskLevel())) {
                orderStr += " riskLevel desc,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getMinPurchaseAmount()) && "0".equals(searchPropertyDto.getMinPurchaseAmount())) {
                //起购金额
                orderStr += " minPurchaseAmount,";
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getMinPurchaseAmount()) && "1".equals(searchPropertyDto.getMinPurchaseAmount())) {
                orderStr += "minPurchaseAmount desc,";
            }
            if (orderStr != null && StringUtil.isNotEmpty(orderStr)) {
                sql += orderStr;
                sql = sql.substring(0, sql.length() - 1);
            } else {
                sql += " a.create_time desc ";
            }
            page = dicDao.sqlPageQueryByPageSize(sql, pageNum, pageSize);
            List<Map<String, Object>> data = page.getData();
            if (data != null && data.size() > 0) {
                for (Map<String, Object> map : data) {
                    //类型
                    String dicTypeId = String.valueOf(map.get("dic_type_id"));
                    Long prodectId = NumberConvertUtil.parseLong(map.get("id"));
                    logger.info("产品id是：" + prodectId);
                    //根据产品id处理机构信息
                    //结算方式
                    DicProperty institution = dicDao.getProperty(prodectId, "institution");
                    if (institution != null && StringUtil.isNotEmpty(institution.getDicPropValue())) {
                        logger.info("机构id是：" + institution.getDicPropValue());
                        //根据机构id查询结算方式
                        CustomerProperty settlementInfo = customerDao.getProperty(institution.getDicPropValue(), "settlementInfo");
                        if (settlementInfo != null) {
                            String propertyValue = settlementInfo.getPropertyValue();
                            JSONObject jsonObject = JSONObject.parseObject(propertyValue);
                            JSONArray infoList = jsonObject.getJSONArray("product");
                            for (int j = 0; j < infoList.size(); j++) {
                                //商品类型
                                String type = infoList.getJSONObject(j).getString("type");
                                if (dicTypeId.equals(type)) {
                                    //结算类型
                                    String settlementType = infoList.getJSONObject(j).getString("settlementType");
                                    map.put("settlementType", settlementType);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询商品列表信息异常：" + e);
        }
        return page;
    }


    /**
     * 查询品牌列表
     *
     * @throws Exception
     */
    public Page getBrandListInfo(Integer pageSize, Integer pageNum, SearchPropertyDTO searchPropertyDto) {
        Page page = null;
        List args = new ArrayList();
        logger.info("查询品牌列表相关信息");
        try {
            String sql = "select distinct a.* from t_dic a left join t_dic_property b on a.id=b.dic_id where a.dic_type_id=? and a.status='1'";
            args.add(searchPropertyDto.getDicType());
            if (StringUtil.isNotEmpty(searchPropertyDto.getName())) {
                sql += " and a.name like ?";
                args.add("%"+searchPropertyDto.getName()+"%");
            }
            if (StringUtil.isNotEmpty(searchPropertyDto.getIsQualityBrand())) {
                sql += " and b.dic_prop_key='isQualityBrand' and b.dic_prop_value=?";
                args.add(searchPropertyDto.getIsQualityBrand());
            }
            sql += " order by a.create_time desc ";
            page = dicDao.sqlPageQueryByPageSize(sql, pageNum, pageSize,args.toArray());
            List<Map<String, Object>> data = page.getData();
            for (Map<String, Object> map : data) {
                Long brandId = NumberConvertUtil.parseLong(map.get("id"));
                logger.info("品牌id是：" + brandId);
                List<DicProperty> list = dicDao.getPropertyList(brandId);
                for (DicProperty property : list) {
                    if (property.getDicPropValue().toLowerCase().endsWith(".jpg")
                            || property.getDicPropValue().toLowerCase().endsWith(".png")
                            || property.getDicPropValue().toLowerCase().endsWith(".gif")) {
                        map.put(property.getDicPropKey(), ConfigUtil.getInstance().get("pic_server_url") + "/0/" + property.getDicPropValue());
                    } else {
                        map.put(property.getDicPropKey(), property.getDicPropValue());
                    }
                }
                int institutionNum = 0;
                institutionNum = customerDao.sqlQuery("select * from t_customer where status !=3 and source = 2 and brand_Id=?", brandId.toString()).size();
                map.put("institutionNum", institutionNum);
                map.put("idStr", brandId.toString());
            }
        } catch (Exception e) {
            logger.error("查询品牌列表异常：" + e);
        }
        return page;
    }

    /**
     * 检查商品上架参数
     *
     * @param dicId
     * @param dic
     */
    private void checkOverheadParam(long dicId, JSONObject dic) {
        //获取参数
        List<DicTypeProperty> dicTypeProperties = dicTypeDao.getPropertyList(dic.getString("dicTypeId"));
        List<DicProperty> propertyList = dicDao.getPropertyList(dicId);
        for (DicProperty d : propertyList) {
            dic.put(d.getDicPropKey(), d.getDicPropValue());
        }
        Set<String> set = dic.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String key = it.next();
            for (DicTypeProperty dicTypeProperty : dicTypeProperties) {
                if (key.equals(dicTypeProperty.getDicTypePropId())) {
                    if ("Y".equals(dicTypeProperty.getDicTypePropIsrequire())) {
                        if (dic.get(key) == null || "".equals(dic.getString(key))) {
                            throw new ParamException("参数:" + dicTypeProperty.getDicTypePropName() + "不允许为空");
                        }
                    }
                }
            }
        }
    }

    /**
     * 保存/编辑展示优先级
     *
     * @param dic
     * @return
     */
    private String updateShowLevel(JSONObject dic, Dic m) {
        String recommendConfig = "";
        if (DicTypeEnum.B.getId().equals(m.getDicTypeId())) {
            recommendConfig = "设为热门贷款";
        } else if (DicTypeEnum.G.getId().equals(m.getDicTypeId())) {
            recommendConfig = "设为热门卡片";
        } else if (DicTypeEnum.H.getId().equals(m.getDicTypeId())) {
            recommendConfig = "设为热门理财";
        }
        List<DicProperty> propertyList = dicDao.listDicNotInProperty(dic.getLong("id"), "productShowLevel", dic.getString("productShowLevel"), dic.getString("type"), m.getDicTypeId(), recommendConfig);
        if (propertyList != null && propertyList.size() > 0) {
            throw new ParamException("展示优先级已经存在");
        }
        DicProperty dicProperty = dicDao.getProperty(dic.getLong("id"), "productShowLevel");
        if (dicProperty == null) {
            dicProperty = new DicProperty(dic.getLong("id"), "productShowLevel", dic.getString("productShowLevel"));
        }
        dicProperty.setDicPropValue(dic.getString("productShowLevel"));
        dicDao.saveOrUpdate(dicProperty);
        return dic.getString("id");
    }

    /**
     * 保存/编辑
     *
     * @param dic
     * @param lu
     * @return id
     * @throws Exception
     */
    public String saveOrUpdate(JSONObject dic, LoginUser lu) throws Exception {
        if (!dic.containsKey("dicTypeId") || StringUtil.isEmpty(dic.getString("dicTypeId"))) {
            throw new ParamException("参数dicTypeId错误");
        }
        //获取参数
        List<DicTypeProperty> dicTypeProperties = dicTypeDao.getPropertyList(dic.getString("dicTypeId"));
        List<DicProperty> properties = new ArrayList<>();
        Long id = null;
        if (dic.containsKey("id")) {
            id = dic.getLong("id");
            Dic dbObj = dicDao.get(id);
            if (dbObj == null) {
                throw new ParamException("数据不存在");
            }
            // 更新展示优先级
            if (dic.containsKey("productShowLevel") && StringUtil.isNotEmpty(dic.getString("productShowLevel"))
                    && (dic.size() == 3 || dic.size() == 4)) {
                return updateShowLevel(dic, dbObj);
            }
            logger.info(dbObj.toString() + ";DIC.SIZE= " + dic.size());
            // 商品状态为上架中,不能编辑
            if (DicTypeEnum.isProductType(dbObj.getDicTypeId()) && (null != dbObj.getStatus() && 1 == dbObj.getStatus()) && dic.size() > 3) {
                throw new ParamException("商品上架中,不能编辑");
            }
            if (dic.containsKey("name") && StringUtil.isNotEmpty(dic.getString("name"))) {
                dbObj.setName(dic.getString("name"));
            }
            if (dic.containsKey("status") && dic.getInteger("status") != null) {
                dbObj.setStatus(dic.getInteger("status"));
            }
            dbObj.setLastUpdateUser(lu.getId().toString());
            dbObj.setLastUpdateTime(new Date());
            dicDao.saveOrUpdate(dbObj);
            boolean checkParam = true;
            // 商品参数校验
            if (DicTypeEnum.isProductType(dic.getString("dicTypeId")) && 1 != dic.getIntValue("status")) {
                checkParam = false;
            }
            if (dicTypeProperties != null && dicTypeProperties.size() > 0) {
                // 商品上架校验参数
                if (DicTypeEnum.isProductType(dbObj.getDicTypeId()) && 1 == dic.getIntValue("status") && dic.size() == 3) {
                    checkOverheadParam(dbObj.getId(), dic);
                    return "";
                }
                Set<String> set = dic.keySet();
                Iterator<String> it = set.iterator();
                DicProperty property;
                while (it.hasNext()) {
                    String key = it.next();
                    for (DicTypeProperty dicTypeProperty : dicTypeProperties) {
                        if (key.equals(dicTypeProperty.getDicTypePropId())) {
                            if (checkParam) {
                                if ("Y".equals(dicTypeProperty.getDicTypePropIsrequire())) {
                                    if (dic.get(key) == null || StringUtil.isEmpty(dic.getString(key))) {
                                        throw new ParamException("参数:" + dicTypeProperty.getDicTypePropName() + "不允许为空");
                                    }
                                }
                            }
                            property = new DicProperty(id, dicTypeProperty.getDicTypePropId(), dic.getString(dicTypeProperty.getDicTypePropId()));
                            properties.add(property);
                        }
                    }
                }
            }
        } else {
            boolean checkParam = true;
            // 商品参数校验
            if (DicTypeEnum.isProductType(dic.getString("dicTypeId")) && 1 != dic.getIntValue("status")) {
                checkParam = false;
            }
            if (dicTypeProperties != null && dicTypeProperties.size() > 0 && checkParam) {
                for (DicTypeProperty dicTypeProperty : dicTypeProperties) {
                    if ("Y".equals(dicTypeProperty.getDicTypePropIsrequire())) {
                        if (!dic.containsKey(dicTypeProperty.getDicTypePropId()) || dic.get(dicTypeProperty.getDicTypePropId()) == null) {
                            throw new ParamException("参数:" + dicTypeProperty.getDicTypePropName() + "不允许为空");
                        }
                    }
                }
            }

            //生成字典
            Dic dicObj = new Dic();
            dicObj.setName(dic.getString("name"));
            dicObj.setDicTypeId(dic.getString("dicTypeId"));
            if (dic.containsKey("status")) {
                dicObj.setStatus(dic.getInteger("status"));
            }
            dicObj.setCreateTime(new Date());
            dicObj.setCreateUser(lu.getId().toString());
            id = IDHelper.getID();
            dicObj.setId(id);
            dicDao.save(dicObj);
            //生成字典属性
            for (DicTypeProperty dicTypeProperty : dicTypeProperties) {
                String propId = dicTypeProperty.getDicTypePropId();
                if ("name".equals(propId) || "status".equals(propId) || "dicTypeId".equals(propId)) {
                    continue;
                }
                DicProperty property = new DicProperty(id, propId, dic.getString(propId));
                properties.add(property);
            }
        }
        if (properties.size() > 0) {
            dicDao.batchSaveOrUpdate(properties);
        }
        // 活动页
        if (DicTypeEnum.J.getId().equals(dic.getString("dicTypeId"))) {
            saveActivityConfig(id);
        }
        // 推广活动
        if (DicTypeEnum.K.getId().equals(dic.getString("dicTypeId"))) {
            saveExtensionActivityConfig(id, dic);
        }
        return String.valueOf(id);
    }

    /**
     * 保存活动地址和活动二维码
     *
     * @param id
     */
    private void saveActivityConfig(long id) {
        String url = DicTypeEnum.J.getUrl() + id;
        String fileType = "jpg";
        String fileName = CipherUtil.generatePassword(String.valueOf(id));
        String savePath = AppConfig.getLocation() + "0" + File.separator + fileName + "." + fileType;
        try {
            QRCodeUtil.writeQrCodeToFile(url, 300, 300, savePath, fileType);
            // 保存活动地址
            List<DicProperty> properties = new ArrayList<>();
            DicProperty activityUrl = dicDao.getProperty(id, "activityUrl");
            if (activityUrl == null) {
                activityUrl = new DicProperty(id, "activityUrl", url);
            }
            activityUrl.setDicPropValue(url);
            // 保存活动二维码
            DicProperty qrCode = dicDao.getProperty(id, "qrCode");
            if (qrCode == null) {
                qrCode = new DicProperty(id, "qrCode", fileName + "." + fileType);
            }
            qrCode.setDicPropValue(fileName + "." + fileType);
            properties.add(activityUrl);
            properties.add(qrCode);
            dicDao.batchSaveOrUpdate(properties);
        } catch (IOException e) {
            throw new RuntimeException("活动页:" + id + "保存二维码失败", e);
        }
    }

    /**
     * 处理推广活动地址
     *
     * @param id
     */
    private void saveExtensionActivityConfig(long id, JSONObject json) {
        String url = "";
        String extensionType = json.getString("extensionType");
        if ("1".equals(extensionType)) {
            long productId = json.getLongValue("extensionTypeValue");
            Dic dic = dicDao.get(productId);
            if (dic != null) {
                url = DicTypeEnum.getUrl(dic.getDicTypeId()) + productId;
            }
        } else if ("2".equals(extensionType)) {
            long activityId = json.getLongValue("extensionTypeValue");
            DicProperty activityUrl = dicDao.getProperty(activityId, "activityUrl");
            if (activityUrl != null) {
                url = activityUrl.getDicPropValue();
            }
        } else if ("3".equals(extensionType)) {
            url = json.getString("extensionTypeValue");
        }
        // 保存推广活动地址
        List<DicProperty> properties = new ArrayList<>();
        DicProperty activityUrl = dicDao.getProperty(id, "activityUrl");
        if (activityUrl == null) {
            activityUrl = new DicProperty(id, "activityUrl", url);
        }
        activityUrl.setDicPropValue(url);
        properties.add(activityUrl);
        // 短链接处理 originalUrl + "&activityId=" + m.get("id") + "&channelId=" + channelId
        String longUrl = "";
        if (StringUtil.isNotEmpty(url) && url.indexOf("?") > 0) {
            // 活动专属url处理
            longUrl = url + "&activityId=" + id + "&channelId=" + json.getString("extensionChannel");
        } else {
            // 活动专属url处理
            longUrl = url + "?activityId=" + id + "&channelId=" + json.getString("extensionChannel");
        }
        String sUrl = ShortUrlUtil.generateShortUrl(longUrl);
        DicProperty shortUrl = new DicProperty(id, "shortUrl", sUrl);
        properties.add(shortUrl);
        dicDao.batchSaveOrUpdate(properties);

    }

    /**
     * 返回详情
     *
     * @param id
     * @return
     */
    public JSONObject getDetailById(Long id) {
        JSONObject result = null;
        Dic dic = dicDao.get(id);
        if (dic == null) return result;
        String dicStr = JSON.toJSONString(dic);
        if (StringUtil.isEmpty(dicStr)) return result;
        result = JSON.parseObject(dicStr);
        List<DicProperty> list = dicDao.getPropertyList(id);
        JSONArray jsonArray;
        Dic product;
        List<Map<String, Object>> productList;
        Map<String, Object> data;
        List<DicProperty> productPropertys;
        for (DicProperty property : list) {
            if (StringUtil.isNotEmpty(property.getDicPropValue()) &&
                    (property.getDicPropValue().toLowerCase().endsWith(".jpg")
                            || property.getDicPropValue().toLowerCase().endsWith(".png")
                            || property.getDicPropValue().toLowerCase().endsWith(".gif"))) {
                result.put(property.getDicPropKey(), ConfigUtil.getInstance().get("pic_server_url") + "/0" + "/" + property.getDicPropValue());
            } else {
                result.put(property.getDicPropKey(), property.getDicPropValue());
            }
            // 处理广告位图片地址
            if (DicTypeEnum.E.getId().equals(dic.getDicTypeId()) && "config".equals(property.getDicPropKey())) {
                jsonArray = JSON.parseArray(property.getDicPropValue());
                if (jsonArray != null && jsonArray.size() > 0) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        jsonArray.getJSONObject(i).put("logo", ConfigUtil.getInstance().get("pic_server_url") + "/0/" + jsonArray.getJSONObject(i).get("logo"));
                    }
                    result.put(property.getDicPropKey(), jsonArray.toJSONString());
                }
            }
            // 查询机构名称
            if ("institution".equals(property.getDicPropKey())) {
                result.put("institutionName", "");
                Customer customer = customerDao.get(property.getDicPropValue());
                if (customer != null) {
                    result.put("institutionName", customer.getEnterpriseName());
                }
            }


            // 处理活动页产品列表
            if (DicTypeEnum.J.getId().equals(dic.getDicTypeId()) && "products".equals(property.getDicPropKey())) {
                productList = new ArrayList<>();
                if (StringUtil.isNotEmpty(property.getDicPropValue())) {
                    for (String productId : property.getDicPropValue().split(",")) {
                        data = new HashMap<>();
                        product = dicDao.get(NumberConvertUtil.parseLong(productId));
                        if (product != null) {
                            //根据商品id查询商品跳转信息
                            DicProperty activityProduct = dicDao.getProperty(id, "activityProduct");
                            if (activityProduct != null) {
                                JSONArray array = JSON.parseArray(activityProduct.getDicPropValue());
                                for (int i = 0; i < array.size(); i++) {
                                    if (productId.equals(array.getJSONObject(i).getString("productId"))) {
                                        String turnUrl = array.getJSONObject(i).getString("turnUrl");
                                        data.put("turnUrl", turnUrl);
                                        String turnConfig = array.getJSONObject(i).getString("turnConfig");
                                        data.put("turnConfig", turnConfig);
                                    }
                                }
                            }
                            data.put("id", product.getId());
                            data.put("name", product.getName());
                            data.put("createTime", product.getCreateTime());
                            data.put("status", product.getStatus());
                            data.put("dicTypeId", product.getDicTypeId());
                            productPropertys = dicDao.getPropertyList(product.getId());
                            for (DicProperty p : productPropertys) {
                                data.put(p.getDicPropKey(), p.getDicPropValue());
                                if (StringUtil.isNotEmpty(p.getDicPropValue()) &&
                                        (p.getDicPropValue().toLowerCase().endsWith(".jpg")
                                                || p.getDicPropValue().toLowerCase().endsWith(".png")
                                                || p.getDicPropValue().toLowerCase().endsWith(".gif"))) {
                                    data.put(p.getDicPropKey(), ConfigUtil.getInstance().get("pic_server_url") + "/0" + "/" + p.getDicPropValue());
                                }
                            }
                        }
                        productList.add(data);
                    }
                }
                result.put("pds", productList);
            }
        }
        result.put("institutionNum", 0);
        List<Customer> customers = customerDao.find("from Customer where status!=3 and brandId=?", id.toString());
        if (customers != null && customers.size() > 0) {
            result.put("institutionNum", customers.size());
            Map<String, Map<String, String>> map = new HashMap<>();
            List<Map<String, String>> institutionList = new ArrayList<>();
            for (Customer customer : customers) {
                Map<String, String> h = new HashMap();
                Map<String, String> h1 = new HashMap();
                h.put("institutionName", customer.getEnterpriseName());
                CustomerProperty settlementInfo = customerDao.getProperty(customer.getCustId(), "settlementInfo");
                if (settlementInfo != null && StringUtil.isNotEmpty(settlementInfo.getPropertyValue())) {
                    JSONObject obj = JSON.parseObject(settlementInfo.getPropertyValue());
                    JSONArray products = obj.getJSONArray("product");
                    String type = "";
                    if (products != null && products.size() > 0) {
                        for (int i = 0; i < products.size(); i++) {
                            JSONObject json = products.getJSONObject(i);
                            type += "," + json.getString("type");
                        }
                        if (type.length() > 0) type = type.substring(1);
                        h.put("productType", type);
                    }
                } else {
                    h.put("productType", "");
                }
                map.put(customer.getCustId(), h);
                for (Map.Entry<String, String> m : h.entrySet()) {
                    h1.put(m.getKey(), m.getValue());
                }
                institutionList.add(h1);
            }
            result.put("institutionList", institutionList);
            result.put("product", map);
        }
        return result;
    }

    public String getDicProdValues(String typeId, String typeProdId) {
        JSONObject jsonObject = new JSONObject();
        if (StringUtil.isNotEmpty(typeProdId)) {
            String[] s = typeProdId.split(",");
            for (String key : s) {
                if (StringUtil.isEmpty(key)) continue;
                DicTypeProperty property = dicTypeDao.getProperty(typeId, key);
                if (property != null) {
                    jsonObject.put(key, property.getDicTypePropValues());
                } else {
                    jsonObject.put(key, "");
                }
            }
        }
        return jsonObject.toJSONString();
    }

    /**
     * 保存产品申请记录
     *
     * @param fundOrder
     * @return
     */
    public boolean saveProductApply(FundProductApply fundOrder) {
        boolean status = false;
        List args=new ArrayList();
        fundOrder.setId(IDHelper.getID());
        fundOrder.setApplyTime(new Timestamp(System.currentTimeMillis()));
        // 判断是否满足商品申请条件
        if (StringUtil.isNotEmpty(fundOrder.getApplyValue())) {
            JSONObject condition = JSON.parseObject(fundOrder.getApplyValue());
            StringBuilder sql = new StringBuilder();
            sql.append(" SELECT id from t_dic where id = ? ");
            args.add(fundOrder.getProductId());
            String operator;
            for (Map.Entry k : condition.entrySet()) {
                if ("houseAge".equals(k.getKey()) || "houseValuation".equals(k.getKey()) ||
                        "name".equals(k.getKey()) || "idCard".equals(k.getKey()) ||
                        "bankId".equals(k.getKey()) || "bankCard".equals(k.getKey())
                        || "userName".equals(k.getKey()) || "phone".equals(k.getKey())
                        || "msg".equals(k.getKey()) || "verification".equals(k.getKey())) {
                    continue;
                }
                //处理年龄区间搜索
                if ("age".equals(k.getKey())) {
                    // 职业身份和有无车检索
                    sql.append(" AND id in (SELECT dic_id from t_dic_property where dic_prop_key='ageStart' AND ").append(k.getValue()).append(">=").append("dic_prop_value").append(" AND dic_id = ")
                            .append(fundOrder.getProductId()).append(")");
                    sql.append(" AND id in (SELECT dic_id from t_dic_property where dic_prop_key='ageEnd' AND ").append(k.getValue()).append("<=").append("dic_prop_value").append(" AND dic_id = ")
                            .append(fundOrder.getProductId()).append(")");
                    continue;
                }

                sql.append(" AND id in (SELECT dic_id from t_dic_property where dic_prop_key='")
                        .append(k.getKey()).append("'");
                if (StringUtil.isNotEmpty(String.valueOf(k.getValue())) && ("revenueConfigValue".equals(k.getKey()) || "manageAmount".equals(k.getKey())
                        || "businessOwnerConfig".equals(k.getKey())
                        || "accumulationFundTime".equals(k.getKey()) || "accumulationFundValue".equals(k.getKey()))) {
                    sql.append(" AND ").append(k.getValue()).append(">=").append("dic_prop_value");
                } else if ("loanAmountStart".equals(k.getKey())) {
                    // 贷款金额区间判断
                    sql.append(" AND ").append(k.getValue()).append(">=dic_prop_value").append(" AND dic_id = ").append(fundOrder.getProductId()).append(") ");
                    sql.append(" AND id in (SELECT dic_id from t_dic_property where dic_prop_key= 'loanAmountEnd' AND " + k.getValue() + " <= dic_prop_value ");
                } else if ("termStart".equals(k.getKey())) {
                    // 贷款期限区间判断
                    sql.append(" AND ").append(k.getValue()).append(">=dic_prop_value").append(" AND dic_id = ").append(fundOrder.getProductId()).append(")");
                    sql.append(" AND id in (SELECT dic_id from t_dic_property where dic_prop_key= 'termEnd' AND " + k.getValue() + " <= dic_prop_value ");
                } else if ("manageMonth".equals(k.getKey())) {
                    if ("1".equals(k.getValue())) {
                        sql.append(" AND dic_prop_value < 3");
                    } else if ("2".equals(k.getValue())) {
                        sql.append(" AND dic_prop_value >=3 AND dic_prop_value <= 6");
                    } else if ("3".equals(k.getValue())) {
                        sql.append(" AND dic_prop_value >6 AND dic_prop_value <= 12");
                    } else if ("4".equals(k.getValue())) {
                        sql.append(" AND dic_prop_value >12 AND dic_prop_value <= 24");
                    } else if ("5".equals(k.getValue())) {
                        sql.append(" AND dic_prop_value >24 AND dic_prop_value <= 36");
                    } else if ("6".equals(k.getValue())) {
                        sql.append(" AND dic_prop_value >36");
                    }
                } else if ("manageLicenseStatus".equals(k.getKey())) {
                    // 营业执照 1-有,本地 2-有,外地 3-无
                    if ("1".equals(k.getValue())) {
                        sql.append(" AND FIND_IN_SET('1',dic_prop_value)").append(" AND dic_id = ")
                                .append(fundOrder.getProductId())
                                .append(")");
                        sql.append(" AND id in (SELECT dic_id from t_dic_property where dic_prop_key='manageArea'").append(" AND FIND_IN_SET('1',dic_prop_value)");
                    } else if ("2".equals(k.getValue())) {
                        sql.append(" AND FIND_IN_SET('1',dic_prop_value))").append(" AND dic_id = ")
                                .append(fundOrder.getProductId()).append(")");
                        sql.append(" AND id in (SELECT dic_id from t_dic_property where dic_prop_key='manageArea'").append(" AND FIND_IN_SET('2',dic_prop_value)");
                    } else if ("3".equals(k.getValue())) {
                        sql.append(" AND FIND_IN_SET('2',dic_prop_value)");
                    }
                } else if ("carType".equals(k.getKey()) || "professionalIdentity".equals(k.getKey())) {
                    // 职业身份和有无车检索
                    sql.append(" AND dic_prop_value like ? ");
                    args.add("%"+k.getValue()+"%");
                } else {
                    sql.append(" AND dic_prop_value like ? ");
                    args.add("%"+k.getValue()+"%");
                }
                sql.append(" AND dic_id =? ").append(")");
                args.add(fundOrder.getProductId());
            }
            List<Map<String, Object>> list = dicDao.sqlQuery(sql.toString(), args.toArray());
            if (list != null && list.size() > 0) {
                status = true;
            }
        }
        fundOrder.setMatchStatus(status ? 1 : 2);
        // 产品类型
        Dic dic = dicDao.get(NumberConvertUtil.parseLong(fundOrder.getProductId()));
        if (dic != null) {
            fundOrder.setProductType(dic.getDicTypeId());
            // 处理H5贷款金额和期限
            if ("H5".equals(fundOrder.getFromClient())) {
                DicProperty dicProperty = dicDao.getProperty(dic.getId(), "loanAmountStart");
                if (dicProperty != null) {
                    fundOrder.setLoanAmount(dicProperty.getDicPropValue());
                }
                dicProperty = dicDao.getProperty(dic.getId(), "termEnd");
                if (dicProperty != null) {
                    fundOrder.setLoanTerm(dicProperty.getDicPropValue());
                }
            }
        }
        // 保存商品申请
        dicDao.saveOrUpdate(fundOrder);
        return status;
    }

    public void del(long dicId) {
        String sql = "delete from t_dic where id =?";
        dicTypeDao.executeUpdateSQL(sql, dicId);
    }

    public void updatePicStatus(String status, String dicId) {
        String sql = " update t_dic set status= ? where id=?";
        dicDao.executeUpdateSQL(sql,status,dicId);

    }


    /**
     * 商品申请列表
     *
     * @param pageNum
     * @param pageSize
     * @param productType
     * @param userId
     * @return
     * @throws Exception
     */
    public JSONObject getProductApplyList(Integer pageNum, Integer pageSize, String productName, String productId, String productType, String userId, String type) throws Exception {
        JSONObject JSONobject = new JSONObject();
        List args = new ArrayList();
        StringBuffer querySql = new StringBuffer("SELECT p.id,product_id,d.`name` ,p.apply_time,p.match_status applyStatus");
        if (!"1".equals(type)) {
            //查询的是商品申请统计列表
            querySql.append(",COUNT(user_id)count ,sum(if(match_status=1,1,0)) AS success,sum(if(match_status=1,0,1)) AS fail");
        }
        querySql.append(" FROM t_fund_product_apply p LEFT JOIN t_dic d ON p.product_id = d.id where 1=1");
        if (StringUtil.isNotEmpty(productName)) {
            querySql.append(" and d.name like ?");
            args.add("%"+productName+"%");
        }
        if (StringUtil.isNotEmpty(productId)) {
            querySql.append(" and p.product_id = ?");
            args.add(productId);
        }
        if (StringUtil.isNotEmpty(productType)) {
            querySql.append(" and p.product_type = ?");
            args.add(productType);
        }
        if (StringUtil.isNotEmpty(userId)) {
            //如果有userID查询的是申请记录
            querySql.append(" and p.user_id = ?");
            args.add(userId);
        }
        if ("1".equals(type)) {
            //查询的是申请记录
            querySql.append(" ORDER BY p.apply_time DESC");
        } else {
            querySql.append(" GROUP BY product_id ORDER BY count DESC");
        }
        Page page = dicDao.sqlPageQuery0(querySql.toString(), pageNum, pageSize,args.toArray());
        //查询的是用户足迹信息
        if (page != null && page.getData() != null) {
            Map<String, Object> m;
            List<DicProperty> propertyList;
            for (int i = 0; i < page.getData().size(); i++) {
                m = (Map<String, Object>) page.getData().get(i);
                propertyList = dicDao.getPropertyList(NumberConvertUtil.parseLong(m.get("product_id")));
                if (propertyList == null || propertyList.size() == 0) {
                    continue;
                }
                for (DicProperty c : propertyList) {
                    if (StringUtil.isEmpty(c.getDicPropKey())) {
                        continue;
                    }
                    m.put(c.getDicPropKey(), c.getDicPropValue());
                    if (StringUtil.isNotEmpty(c.getDicPropValue()) && (c.getDicPropValue().toLowerCase().endsWith(".jpg")
                            || c.getDicPropValue().toLowerCase().endsWith(".png")
                            || c.getDicPropValue().toLowerCase().endsWith(".gif"))) {
                        m.put(c.getDicPropKey(), ConfigUtil.getInstance().get("pic_server_url") + "/0/" + c.getDicPropValue());
                    }
                    if (StringUtil.isNotEmpty(c.getDicPropKey()) && "institution".equals(c.getDicPropKey())) {
                        //查询机构名称
                        String institutionName = customerDao.getEnterpriseName(String.valueOf(c.getDicPropValue()));
                        m.put("institutionName", institutionName);
                    }
                }
            }
        }

        if (page != null) {
            JSONobject.put("data", page.getData());
            JSONobject.put("total", page.getTotal());
        }
        return JSONobject;
    }

    /**
     * 商品申请详情
     */
    public JSONObject getProductApplyDetail(Integer pageNum, Integer pageSize, String id, String
            phone, String startTime, String stopTime, String channel, String status, String activityName, String
                                                    productType, String userId) throws Exception {
        logger.info("查询的商品id是：" + id);
        List args = new ArrayList();
        JSONObject JSONobject = new JSONObject();
        StringBuffer stringBuffer = new StringBuffer("SELECT d.name activityName,f.channel,f.apply_value,f.id,f.product_id,CAST(f.user_id AS CHAR) userId, f.mobile_phone, u.realname name, f.apply_time, f.activity_id, f.match_status, f.loan_amount loanAmount, f.loan_late loanLate, f.loan_term loanTerm ");
        stringBuffer.append(" FROM t_fund_product_apply f LEFT JOIN t_customer_user u ON f.user_id = u.id LEFT JOIN t_dic d ON d.id = f.product_id WHERE 1=1");
        if (StringUtil.isNotEmpty(id)) {
            stringBuffer.append(" and f.product_id = ?");
            args.add(id);
        }
        if (StringUtil.isNotEmpty(phone)) {
            stringBuffer.append(" and f.mobile_phone = ?");
            args.add(phone);
        }
        if (StringUtil.isNotEmpty(startTime)) {
            stringBuffer.append(" and f.apply_time >= ?");
            args.add(startTime);
        }
        if (StringUtil.isNotEmpty(stopTime)) {
            stringBuffer.append(" and f.apply_time <= ?");
            args.add(stopTime);
        }
        if (StringUtil.isNotEmpty(status)) {
            stringBuffer.append(" and f.match_status = ?");
            args.add(status);
        }
        if (StringUtil.isNotEmpty(channel)) {
            stringBuffer.append(" and f.channel = ?");
            args.add(channel);
        }
        if (StringUtil.isNotEmpty(activityName)) {
            stringBuffer.append(" and d.NAME = ?");
            args.add(activityName);
        }
        if (StringUtil.isNotEmpty(productType)) {
            stringBuffer.append(" and f.product_type =?");
            args.add(productType);
        }
        if (StringUtil.isNotEmpty(userId)) {
            stringBuffer.append(" and f.user_id = ?");
            args.add(userId);
        }
        stringBuffer.append(" ORDER BY apply_time DESC ");
        logger.info("检索sql是：" + stringBuffer.toString());
        Page page = dicDao.sqlPageQuery0(stringBuffer.toString(), pageNum, pageSize,args.toArray());
        if (page != null && page.getData().size() > 0) {
            List<Map<String, Object>> data = page.getData();
            List<DicProperty> propertyList;
            Dic dic;
            Map<String, Object> property;
            for (int i = 0; i < data.size(); i++) {
                String applyValue = String.valueOf(data.get(i).get("apply_value"));
                if (StringUtil.isNotEmpty(applyValue)) {
                    JSONObject jsonObject = JSONObject.parseObject(applyValue);
                    //职业
                    String professionalIdentity = jsonObject.getString("professionalIdentity");
                    data.get(i).put("position", professionalIdentity);
                    //信用情況
                    String creditSituation = jsonObject.getString("creditSituation");
                    data.get(i).put("creditSituation", creditSituation);
                    //称呼
                    String realname = jsonObject.getString("name");
                    data.get(i).put("realname", realname);
                }
                // 处理商品属性
                property = new HashMap<>();
                propertyList = dicDao.getPropertyList(NumberConvertUtil.parseLong(data.get(i).get("product_id")));
                if (propertyList != null && propertyList.size() > 0) {
                    for (DicProperty p : propertyList) {
                        property.put(p.getDicPropKey(), p.getDicPropValue());
                    }
                }
                data.get(i).put("property", property);
                // 处理商品名称
                dic = dicDao.get(NumberConvertUtil.parseLong(data.get(i).get("product_id")));
                data.get(i).put("productName", "");
                if (dic != null) {
                    data.get(i).put("productName", dic.getName());
                }
            }
        }
        JSONobject.put("data", page.getData());
        JSONobject.put("total", page.getTotal());
        return JSONobject;
    }

    /**
     * 申请用户详细信息
     *
     * @param userId
     * @return
     */
    public Map<String, Object> getUserDetail(String userId, String id) throws Exception {
        logger.info("用户id是：" + userId + "唯一标识id是：" + id);
        List args = new ArrayList();
        StringBuffer stringBuffer = new StringBuffer("SELECT CAST(d.user_id AS CHAR) userId,u.create_time registerTime,d.activity_id activityId, u.realname ,d.apply_value applyValue,c.name activityName");
        stringBuffer.append(" FROM t_fund_product_apply d LEFT JOIN t_customer_user u ON d.user_id = u.id LEFT JOIN t_dic c ON d.activity_id = c.id WHERE 1=1");
        if (StringUtil.isNotEmpty(userId)) {
            stringBuffer.append(" and d.user_id = ?");
            args.add(userId);
        }
        if (StringUtil.isNotEmpty(id)) {
            stringBuffer.append(" and d.id= ?");
            args.add(id);
        }
        List<Map<String, Object>> userInfoList = dicDao.sqlQuery(stringBuffer.toString(),args.toArray());
        logger.info("查询出用户信息是：" + JSONObject.toJSONString(userInfoList));
        Map<String, Object> userInfo = null;
        if (userInfoList.size() > 0) {
            userInfo = userInfoList.get(0);
            //根据userId查询基本信息
            List<CustomerUserPropertyDO> propertyAllList = customerUserDao.getPropertiesByUserId(userId);
            if (propertyAllList != null && propertyAllList.size() > 0) {
                for (int i = 0; i < propertyAllList.size(); i++) {
                    if ("channel".equals(propertyAllList.get(i).getPropertyName())) {
                        userInfo.put("channel", "自有渠道");
                        if (!"1".equals(propertyAllList.get(i).getPropertyValue())) {
                            Dic dicEntity = dicDao.getDicEntity(NumberConvertUtil.parseLong(propertyAllList.get(i).getPropertyValue()));
                            if (dicEntity != null) {
                                userInfo.put(propertyAllList.get(i).getPropertyName(), dicEntity.getName());
                            }
                        }
                    } else if ("registerSource".equals(propertyAllList.get(i).getPropertyName())) {
                        Dic dicEntity = dicDao.getDicEntity(NumberConvertUtil.parseLong(propertyAllList.get(i).getPropertyValue()));
                        if (dicEntity != null) {
                            userInfo.put(propertyAllList.get(i).getPropertyName(), dicEntity.getName());
                        }
                    } else {
                        userInfo.put(propertyAllList.get(i).getPropertyName(), propertyAllList.get(i).getPropertyValue());
                    }
                }
            }
        }
        return userInfo;
    }

    /**
     * 导出结算单
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/19 16:44
     */
    public String exportApplyInfo(HttpServletResponse response, String id, String phone, String
            startTime, String stopTime, String channel, String status) {
        Map<String, Object> resultMap = new HashMap<>();
        List args = new ArrayList();
        try {
            logger.info("查询的商品id是：" + id);
            StringBuffer stringBuffer = new StringBuffer("SELECT d.name activityName,f.channel,f.apply_value,f.id,f.product_id,u.id userId, f.mobile_phone, u.realname, f.apply_time, f.activity_id, f.match_status");
            stringBuffer.append(" FROM t_fund_product_apply f LEFT JOIN t_customer_user u ON f.user_id = u.id LEFT JOIN t_dic d ON d.id = f.activity_id WHERE 1=1");
            if (StringUtil.isNotEmpty(id)) {
                stringBuffer.append(" and f.product_id =?");
                args.add(id);
            }
            if (StringUtil.isNotEmpty(phone)) {
                stringBuffer.append(" and f.mobile_phone = ?");
                args.add(phone);
            }
            if (StringUtil.isNotEmpty(startTime)) {
                stringBuffer.append(" and f.apply_time >= ?");
                args.add(startTime);
            }
            if (StringUtil.isNotEmpty(stopTime)) {
                stringBuffer.append(" and f.apply_time <= ?");
                args.add(stopTime);
            }
            if (StringUtil.isNotEmpty(status)) {
                stringBuffer.append(" and f.match_status = ?");
                args.add(status);
            }
            if (StringUtil.isNotEmpty(channel)) {
                stringBuffer.append(" and f.channel = ? OR f.activity_id = ?");
                args.add(channel);
                args.add(channel);
            }
            stringBuffer.append(" ORDER BY apply_time");
            logger.info("检索sql是：" + stringBuffer.toString());
            List<Map<String, Object>> exportData = dicDao.sqlQuery(stringBuffer.toString(),args.toArray());
            if (exportData != null && exportData.size() > 0) {
                // 设置标题
                List<String> titles = new ArrayList<String>();
                titles.add("用户id");
                titles.add("用户手机号");
                titles.add("称呼");
                titles.add("申请时间");
                titles.add("渠道或活动");
                titles.add("系统匹配结果");
                titles.add("职业");
                titles.add("信用情况");
                titles.add("收入");
                titles.add("营业执照");
                titles.add("经营年限");
                titles.add("总经营流水");
                titles.add("社保");
                titles.add("公积金");
                titles.add("房");
                titles.add("车");
                titles.add("身份证号");
                titles.add("银行以及卡号");
                titles.add("年龄");
                titles.add("真实姓名");
                List<List<Object>> data = new ArrayList<>();
                if (exportData.size() > 0) {
                    for (int i = 0; i < exportData.size(); i++) {
                        //用户基本信息
                        String applValue = String.valueOf(exportData.get(i).get("apply_value"));
                        JSONObject jsonObject = JSON.parseObject(applValue);
                        List<Object> rowList = new ArrayList<>();
                        rowList.add(exportData.get(i).get("userId") != null ? exportData.get(i).get("userId") : "");
                        rowList.add(exportData.get(i).get("mobile_phone") != null ? exportData.get(i).get("mobile_phone") : "");
                        rowList.add(exportData.get(i).get("realname") != null ? exportData.get(i).get("realname") : "");
                        rowList.add(exportData.get(i).get("apply_time") != null ? exportData.get(i).get("apply_time") : "");
                        if (exportData.get(i).get("activityName") != null) {
                            rowList.add(exportData.get(i).get("activityName"));
                        } else {
                            rowList.add(exportData.get(i).get("channel") != null ? exportData.get(i).get("channel") : "");
                        }
                        //系统匹配结果
                        if ("1".equals(String.valueOf(exportData.get(i).get("match_status")))) {
                            rowList.add("成功");
                        } else if ("2".equals(String.valueOf(exportData.get(i).get("match_status")))) {
                            rowList.add("失败");
                        } else {
                            rowList.add(exportData.get(i).get("match_status") != null ? exportData.get(i).get("match_status") : "");
                        }

                        rowList.add(jsonObject.getString("professionalIdentity") != null ? jsonObject.getString("professionalIdentity") : "");
                        rowList.add(jsonObject.getString("creditSituation") != null ? jsonObject.getString("creditSituation") : "");
                        rowList.add(jsonObject.getString("revenueConfigValue") != null ? jsonObject.getString("revenueConfigValue") : "");
                        //是否注册过营业执照
                        if ("1".equals(jsonObject.getString("manageLicenseStatus"))) {
                            rowList.add("是");
                        } else if ("2".equals(jsonObject.getString("manageLicenseStatus"))) {
                            rowList.add("否");
                        } else {
                            rowList.add(jsonObject.getString("manageLicenseStatus") != null ? jsonObject.getString("manageLicenseStatus") : "");
                        }
                        rowList.add(jsonObject.getString("manageMonth") != null ? jsonObject.getString("manageMonth") : "");
                        rowList.add(jsonObject.getString("manageAmount") != null ? jsonObject.getString("manageAmount") : "");
                        //有本地社保|无本地社保
                        if ("1".equals(jsonObject.getString("socialSecurity"))) {
                            rowList.add("有");
                        } else if ("2".equals(jsonObject.getString("socialSecurity"))) {
                            rowList.add("无");
                        } else {
                            rowList.add(jsonObject.getString("socialSecurity") != null ? jsonObject.getString("socialSecurity") : "");
                        }
                        //有无公积金
                        if ("1".equals(jsonObject.getString("accumulationFund"))) {
                            rowList.add("有");
                        } else if ("2".equals(jsonObject.getString(("accumulationFund")))) {
                            rowList.add("无");
                        } else {
                            rowList.add(jsonObject.getString("accumulationFund") != null ? jsonObject.getString("accumulationFund") : "");
                        }
                        rowList.add(jsonObject.getString("houseConfig") != null ? jsonObject.getString("houseConfig") : "");

                        //有无车
                        if ("1".equals(jsonObject.getString("carType"))) {
                            rowList.add("有");
                        } else if ("2".equals(jsonObject.getString("carType"))) {
                            rowList.add("无");
                        } else {
                            rowList.add(jsonObject.getString("carType") != null ? jsonObject.getString("carType") : "");
                        }
                        rowList.add(jsonObject.getString("idCard") != null ? jsonObject.getString("idCard") : "");
                        //拼接银行+ 银行卡
                        if (StringUtil.isNotEmpty(jsonObject.getString("bankId")) || StringUtil.isNotEmpty(jsonObject.getString("bankCard"))) {
                            rowList.add(jsonObject.getString("bankId") + ":" + jsonObject.getString("idCard"));
                        } else {
                            rowList.add("");
                        }
                        rowList.add(jsonObject.getString("age") != null ? jsonObject.getString("age") : "");
                        rowList.add(jsonObject.getString("name") != null ? jsonObject.getString("name") : "");
                        data.add(rowList);
                    }
                }
                logger.info("导出数据是：" + JSON.toJSONString(data));
                if (data.size() > 0) {
                    String fileName = "产品用户申请记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
                    String fileType = ".xlsx";
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("application/vnd.ms-excel;charset=utf-8");
                    response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                    OutputStream outputStream = response.getOutputStream();
                    ExcelUtils.getInstance().exportObjects2Excel(data, titles, outputStream);
                    outputStream.flush();
                    response.flushBuffer();
                    outputStream.close();
                    logger.info("导出产品申请用户列表信息导出成功");
                    resultMap.put("code", "000");
                    resultMap.put("_message", "导出成功");
                }
            } else {
                resultMap.put("code", "001");
                resultMap.put("_message", "无数据导出！");
                return JSON.toJSONString(resultMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("导出产品申请用户列表信息导出失败\t" + e);
            resultMap.put("code", "002");
            resultMap.put("_message", "导出产品申请用户列表信息导出失败！");
        }

        return JSON.toJSONString(resultMap);
    }

    /**
     * 广告位展示列表
     *
     * @param dto
     * @return
     */
    public List<Map<String, Object>> listShowAdSpace(SearchPropertyDTO dto) {
        List<Map<String, Object>> list = null;
        List args = new ArrayList();
        if (StringUtil.isEmpty(dto.getDicType())) {
            throw new ParamException("参数dicType错误");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select id,name,dic_type_id,create_time,last_update_time,status from t_dic a where a.dic_type_id=?");
        args.add(dto.getDicType());
        // 所属平台检索
        if (StringUtil.isNotEmpty(dto.getAdPlatform())) {
            sql.append(" AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value = ? AND dic_prop_key='platform')");
            args.add(dto.getAdPlatform());
        }
        // 广告位唯一编码
        if (StringUtil.isNotEmpty(dto.getAdCode())) {
            sql.append(" AND a.id in (SELECT dic_id FROM t_dic_property where dic_prop_value = ? AND dic_prop_key='code')");
            args.add(dto.getAdCode());
        }
        list = dicDao.sqlQuery(sql.toString(), args.toArray());
        if (list.size() > 0) {
            JSONArray jsonArray, data;
            JSONObject jsonObject;
            List<DicProperty> propertyList;
            LocalDateTime putOnTime, pullOffTime, now = LocalDateTime.now();
            logger.warn("广告位列表所属平台:" + dto.getAdPlatform() + ",当前时间:" + now);
            for (Map<String, Object> m : list) {
                propertyList = dicDao.getPropertyList(NumberConvertUtil.parseLong(m.get("id")));
                if (propertyList == null || propertyList.size() == 0) {
                    continue;
                }
                for (DicProperty c : propertyList) {
                    if (StringUtil.isEmpty(c.getDicPropKey())) {
                        continue;
                    }
                    m.put(c.getDicPropKey(), c.getDicPropValue());
                    if ("config".equals(c.getDicPropKey())) {
                        data = new JSONArray();
                        jsonArray = JSON.parseArray(c.getDicPropValue());
                        if (jsonArray == null || jsonArray.size() == 0) {
                            continue;
                        }
                        for (int i = 0; i < jsonArray.size(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            jsonObject.put("logo", ConfigUtil.getInstance().get("pic_server_url") + "/0/" + jsonObject.get("logo"));
                            // 如果为自动上架则判断上下架时间
                            if ("1".equals(jsonObject.getString("autoPutOnStatus"))) {
                                if (StringUtil.isNotEmpty(jsonObject.getString("putOnTime")) && StringUtil.isNotEmpty(jsonObject.getString("pullOffTime"))) {
                                    putOnTime = LocalDateTime.parse(jsonObject.getString("putOnTime"), DatetimeUtils.DATE_TIME_FORMATTER);
                                    pullOffTime = LocalDateTime.parse(jsonObject.getString("pullOffTime"), DatetimeUtils.DATE_TIME_FORMATTER);
                                    // 未到上架时间
                                    if (now.isBefore(putOnTime)) {
                                        logger.warn("广告位code:" + m.get("name") + ",index:" + jsonObject.getString("index") + ",未到上架时间:" + putOnTime);
                                        continue;
                                    }
                                    // 已过下架时间
                                    if (now.isAfter(pullOffTime)) {
                                        logger.warn("广告位code:" + m.get("name") + ",index:" + jsonObject.getString("index") + ",已过下架时间:" + pullOffTime);
                                        continue;
                                    }
                                    data.add(jsonObject);
                                }
                            } else {
                                data.add(jsonObject);
                            }
                        }
                        m.put(c.getDicPropKey(), data.toJSONString());
                    }
                }
            }
        }
        return list;
    }

    /**
     * 保存已经反馈数据
     *
     * @param body
     * @return
     */
    public void saveFeedBack(JSONObject body) {
        //称呼
        String nickName = body.getString("nickName");
        //手机号码
        String mobilePhone = body.getString("mobilePhone");
        //反馈信息
        String feedback = body.getString("feedback");
        //1：pc 2:h5  3:其他
        String client = body.getString("client");
        //ip
        String ip = body.getString("ip");

        FeedBack feedBack = new FeedBack();
        feedBack.setNickName(nickName);
        feedBack.setMobilePhone(mobilePhone);
        feedBack.setFeedback(feedback);
        feedBack.setCreateTime((new Timestamp(System.currentTimeMillis())));
        feedBack.setClient(client);
        feedBack.setIp(ip);
        dicDao.saveOrUpdate(feedBack);
    }

    /**
     * 查询贷款商品相关费用
     *
     * @param loanAmount
     * @param loanTerm
     * @param productId
     */
    public Map<Object, Object> getLoanCost(int loanAmount, int loanTerm, String productId) {
        Map<Object, Object> map = new HashMap<>();
        //根据商品id查询利率信息
        DicProperty interestRateSett = dicDao.getProperty(NumberConvertUtil.parseLong(productId), "interestRateSett");
        double rate = 0, loanCost = 0, allInterest = 0, interestRate = 0;
        if (interestRateSett != null && StringUtil.isNotEmpty(interestRateSett.getDicPropValue())) {
            String rateStr = interestRateSett.getDicPropValue();
            logger.info("商品id是：" + productId + "\n设置的费率信息是：" + rateStr);
            JSONObject jsonObject = JSON.parseObject(rateStr);
            String type = null, interestRateType = null;
            if (jsonObject != null) {
                //1 固定费率   2 阶梯费率
                type = jsonObject.getString("type");
                interestRateType = jsonObject.getString("interestRateType");
                logger.info("利率类型是：" + interestRateType);
                if ("1".equals(type)) {
                    interestRate = jsonObject.getDoubleValue("value");
                    logger.info("设置的原始利率是：" + interestRate);
                    rate = LoanUtil.getRateByYear(interestRateType, interestRate);
                    logger.info("商品id是：" + productId + "\n年利率是：" + rate);
                } else if ("2".equals(type)) {
                    //阶梯计费
                    JSONArray array = jsonObject.getJSONArray("value");
                    if (array != null && array.size() > 0) {
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject json = JSON.parseObject(array.getString(i));
                            //根据贷款符合区间确定贷款利率
                            if (json != null) {
                                int start = json.getIntValue("start");
                                int end = json.getIntValue("end");
                                interestRate = json.getDoubleValue("value");
                                //判断贷款月份所在区间计算相关费用
                                if (start <= loanTerm && loanTerm <= end) {
                                    rate = LoanUtil.getRateByYear(interestRateType, interestRate);
                                    logger.info("利率是：" + rate);
                                    break;
                                }
                            }
                        }
                    }
                }
                if (rate > 0) {
                    DicProperty oneFeeCharge = dicDao.getProperty(NumberConvertUtil.parseLong(productId), "oneFeeCharge");
                    double oneFeeChargeCost = 0;
                    if (oneFeeCharge != null) {
                        logger.info("收取一次性费用的百分比：");
                        //查询一次费用
                        oneFeeChargeCost = loanAmount / 100 * NumberConvertUtil.parseDouble(oneFeeCharge.getDicPropValue());
                    }
                    //本息合计（月供）
                    loanCost = LoanUtil.getPerMonthPrincipalInterest(loanAmount, rate, loanTerm);
                    //总利息
                    allInterest = LoanUtil.getInterestCount(loanAmount, rate, loanTerm);
                    logger.info("月供是：" + loanCost + "利息是：" + allInterest);
                    //利率
                    map.put("rate", interestRate);
                    //利率类型
                    map.put("interestRateType", interestRateType);
                    //本息合计（月供）
                    map.put("monthlySupply", loanCost);
                    //总利息
                    map.put("allInterest", allInterest);
                    //一次性费用
                    map.put("oneFeeCharge", oneFeeChargeCost);
                } else {
                    logger.warn("利息为0不进行计算");
                    map.put("rate", 0);
                    map.put("interestRateType", interestRateType);
                    map.put("monthlySupply", loanCost);
                    map.put("allInterest", 0);
                    map.put("oneFeeCharge", 0);
                }
            }
        }
        return map;
    }

    /**
     * 检查商品名称是否存在
     *
     * @param id
     * @param name
     * @param dicTypeId
     * @return
     */
    public boolean checkExistProductName(Long id, String name, String dicTypeId, int status) {
        List<Dic> existDic = dicDao.listNameExist(id, name, dicTypeId, status);
        if (existDic.size() > 0) {
            return true;
        }
        return false;
    }

    public List<Map<String, Object>> getBrandList(String dicType) throws Exception {
        logger.info("查询类型是：" + dicType);
        List args = new ArrayList();
        StringBuffer querySql = new StringBuffer("SELECT DISTINCT brand_id brandId FROM t_customer c  WHERE c.cust_id IN ");
        querySql.append("(SELECT dic_prop_value FROM t_dic d LEFT JOIN t_dic_property p ON d.id = p.dic_id WHERE 1=1  ");
        if (StringUtil.isNotEmpty(dicType)) {
            querySql.append(" and d.dic_type_id =?");
            args.add(dicType);
        }
        querySql.append(" AND p.dic_prop_key = 'institution' AND d.`status` !=4) AND c.status = 0");
        List<Map<String, Object>> list = dicDao.sqlQuery(querySql.toString());
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                //根据品牌id查询品牌名称
                Dic dic = dicDao.getDicEntity(NumberConvertUtil.parseLong(list.get(i).get("brandId")));
                if (dic != null) {
                    logger.info("查询到品牌信息是：" + String.valueOf(dic));
                    list.get(i).put("brandName", dic.getName());
                }
            }
        }
        return list;
    }
}