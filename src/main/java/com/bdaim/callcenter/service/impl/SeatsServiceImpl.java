package com.bdaim.callcenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.entity.BatchListEntity;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.callcenter.dto.SeatsInfo;
import com.bdaim.callcenter.dto.SeatsMessageParam;
import com.bdaim.callcenter.service.SeatsService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.ConstantsUtil;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserProperty;
import com.bdaim.price.dto.ResourcesPriceDto;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.supplier.dto.SupplierEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author duanliying
 * @date 2018/9/19
 * @description
 */
@Service("SeatsService")
@Transactional
public class SeatsServiceImpl implements SeatsService {
    private static Logger logger = LoggerFactory.getLogger(SeatsServiceImpl.class);

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private SupplierDao supplierDao;
    @Resource
    private BatchDao batchListDao;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private TransactionService transactionService;
    @Resource
    private BatchDao batchDao;
    @Resource
    private MarketResourceService marketResourceServiceImpl;

    /**
     * 联通包月分钟数配置key
     */
    private final static String SEAT_MONTH_PACKAGE_MINUTE_KEY = "cuc_minute";

    /**
     * @description 编辑企业坐席主信息
     * @author:duanliying
     * @method 渠道/供应商 0-联通 1-移动 2-电信
     * @date: 2018/9/19 17:58
     */
    @Override
    public void updateMainMessage(List<SeatsMessageParam> seatsList) throws Exception {
        String channelProperty = null;
        ArrayList<String> channelList = new ArrayList<>();
        String custId = seatsList.get(0).getCustId();
        String apparentNumber = null;
        if (seatsList.size() > 0) {
            for (int i = 0; i < seatsList.size(); i++) {
                String channel = seatsList.get(i).getChannel();
                channelList.add(channel);
                if (channel.equals(ConstantsUtil.SUPPLIERID__XZ)) {
                    channelProperty = "xz";
                }
                if (channel.equals(ConstantsUtil.SUPPLIERID__CUC)) {
                    channelProperty = "cuc";
                }
                if (channel.equals(ConstantsUtil.SUPPLIERID__CMC)) {
                    channelProperty = "cmc";
                }
                if (channel.equals(ConstantsUtil.SUPPLIERID__CTC)) {
                    channelProperty = "ctc";
                }
                if (channel.equals(ConstantsUtil.SUPPLIERID__JD)) {
                    channelProperty = "jd";
                }
                if (channel.equals(ConstantsUtil.SUPPLIERID__YD)) {
                    channelProperty = "yd";
                }
                //处理呼叫中心ID
                String callEnterpriseId = seatsList.get(i).getCallEnterpriseId();
                if (StringUtil.isNotEmpty(callEnterpriseId)) {
                    //去除多余空格和英文逗号
                    callEnterpriseId = callEnterpriseId.replaceAll(" ", "").replaceAll("，", ",");
                    logger.info(channel + "渠道的呼叫中心id是：" + callEnterpriseId);
                }
                //处理外显号码
                apparentNumber = seatsList.get(i).getApparentNumbera();
                if (StringUtil.isNotEmpty(apparentNumber)) {
                    //去除多余空格和英文逗号
                    apparentNumber = apparentNumber.replaceAll(" ", "").replaceAll("，", ",");
                    logger.info(channel + "外显号码是：" + apparentNumber);
                }
                //处理活动id
                String activityId = seatsList.get(i).getActivityId();
                if (StringUtil.isNotEmpty(activityId)) {
                    activityId = activityId.replaceAll(" ", "").replaceAll("，", ",");
                    logger.info(channel + "活动Id是：" + activityId);
                }
                if (ConstantsUtil.SUPPLIERID__CUC.equals(channel) || ConstantsUtil.SUPPLIERID__CTC.equals(channel) || ConstantsUtil.SUPPLIERID__CMC.equals(channel)) {
                    CustomerProperty callIdProperty = customerDao.getProperty(custId, channelProperty + "_call_id");
                    if (callIdProperty == null) {
                        CustomerProperty callIdCustomer = new CustomerProperty();
                        callIdCustomer.setCustId(custId);
                        callIdCustomer.setPropertyName(channelProperty + "_call_id");
                        callIdCustomer.setPropertyValue(callEnterpriseId);
                        callIdCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(callIdCustomer);
                    } else {
                        callIdProperty.setPropertyValue(callEnterpriseId);
                        customerDao.saveOrUpdate(callIdProperty);
                    }
                    //案件ID
                    CustomerProperty activityProperty = customerDao.getProperty(custId, channelProperty + "_activity_id");
                    if (activityProperty == null) {
                        CustomerProperty activityIdCustomer = new CustomerProperty();
                        activityIdCustomer.setCustId(custId);
                        activityIdCustomer.setPropertyName(channelProperty + "_activity_id");
                        activityIdCustomer.setPropertyValue(activityId);
                        activityIdCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(activityIdCustomer);
                    } else {
                        activityProperty.setPropertyValue(activityId);
                        customerDao.saveOrUpdate(activityProperty);
                    }
                    CustomerProperty apparentNumberCustomer = customerDao.getProperty(custId, channelProperty + "_apparent_number");
                    if (apparentNumberCustomer == null) {
                        CustomerProperty apparentCustomer = new CustomerProperty();
                        apparentCustomer.setCustId(custId);
                        apparentCustomer.setPropertyName(channelProperty + "_apparent_number");
                        apparentCustomer.setPropertyValue(apparentNumber);
                        apparentCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(apparentCustomer);
                    } else {
                        apparentNumberCustomer.setPropertyValue(apparentNumber);
                        customerDao.saveOrUpdate(apparentNumberCustomer);
                    }

                }
                if (ConstantsUtil.SUPPLIERID__JD.equals(channel)) {
                    String configId = seatsList.get(i).getConfigId();
                    CustomerProperty configCustomerProperty = customerDao.getProperty(custId, channelProperty + "_config_id");
                    if (configCustomerProperty == null) {
                        CustomerProperty configCustomer = new CustomerProperty();
                        configCustomer.setCustId(custId);
                        configCustomer.setPropertyName(channelProperty + "_config_id");
                        configCustomer.setPropertyValue(configId);
                        configCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(configCustomer);
                    } else {
                        configCustomerProperty.setPropertyValue(configId);
                        customerDao.saveOrUpdate(configCustomerProperty);
                    }
                }

                if (ConstantsUtil.SUPPLIERID__YD.equals(channel)) {
                    String configId = seatsList.get(i).getConfigId();
                    CustomerProperty configCustomerProperty = customerDao.getProperty(custId, channelProperty + "_config_id");
                    if (configCustomerProperty == null) {
                        CustomerProperty configCustomer = new CustomerProperty();
                        configCustomer.setCustId(custId);
                        configCustomer.setPropertyName(channelProperty + "_config_id");
                        configCustomer.setPropertyValue(configId);
                        configCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(configCustomer);
                    } else {
                        configCustomerProperty.setPropertyValue(configId);
                        customerDao.saveOrUpdate(configCustomerProperty);
                    }
                }
                if (ConstantsUtil.SUPPLIERID__XZ.equals(channel)) {
                    CustomerProperty configCustomerProperty = customerDao.getProperty(custId, channelProperty + "_apparent_number");
                    if (configCustomerProperty == null) {
                        CustomerProperty configCustomer = new CustomerProperty();
                        configCustomer.setCustId(custId);
                        configCustomer.setPropertyName(channelProperty + "_apparent_number");
                        configCustomer.setPropertyValue(apparentNumber);
                        configCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(configCustomer);
                    } else {
                        configCustomerProperty.setPropertyValue(apparentNumber);
                        customerDao.saveOrUpdate(configCustomerProperty);
                    }
                }

                //修改批次下外显号码需要根据外呼是讯众还是联通 根据根据certify_type  0是联通  其他是讯众
                int certifyType = -1;
                if ("2".equals(channel)) {
                    certifyType = 0;
                }
                //保存同时更新企业下批次配置的外显号码
                List<BatchListEntity> batchDetailList = batchDao.getBatchDetailList(custId, certifyType);
                if (batchDetailList.size() > 0) {
                    for (int j = 0; j < batchDetailList.size(); j++) {
                        String custApparentNumber = batchDetailList.get(j).getApparentNumber();
                        if (StringUtil.isNotEmpty(custApparentNumber)) {
                            StringBuffer stringBuffer = new StringBuffer();
                            String[] split = custApparentNumber.split(",");
                            if (split.length > 0) {
                                String[] channelApparent = apparentNumber.split(",");
                                logger.info("批次下原有外显号码" + custApparentNumber);
                                for (int k = 0; k < split.length; k++) {
                                    boolean contains = Arrays.asList(channelApparent).contains(split[k]);
                                    if (contains) {
                                        stringBuffer.append(split[k] + ",");
                                    }
                                }
                                if (stringBuffer != null && StringUtil.isNotEmpty(String.valueOf(stringBuffer))) {
                                    //删除最后一个，号同时把保存到批次下面的外显号码中
                                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                                }
                                logger.info("批次下更改后的外显号码" + String.valueOf(stringBuffer));
                                //保存到批次中
                                batchDetailList.get(j).setApparentNumber(String.valueOf(stringBuffer));
                                batchDao.saveOrUpdate(batchDetailList.get(j));
                            }
                        }
                    }
                }

            }
            //将channel进行拼接存储数据库以逗号隔开
            if (channelList.size() > 0) {
                StringBuilder channelStr = new StringBuilder();
                for (int j = 0; j < channelList.size(); j++) {
                    if (channelStr.length() > 0) {
                        channelStr.append(",");
                    }
                    channelStr.append(channelList.get(j));
                }
                //将渠道信息存储到数据库
                //查询数据库是否存在  存在更新  不存在增加
                CustomerProperty channelOldCustomer = customerDao.getProperty(custId, "channel");
                if (channelOldCustomer == null) {
                    CustomerProperty channelCustomer = new CustomerProperty();
                    channelCustomer.setCustId(custId);
                    channelCustomer.setPropertyName("channel");
                    channelCustomer.setPropertyValue(channelStr.toString());
                    channelCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                    customerDao.saveOrUpdate(channelCustomer);
                } else {
                    channelOldCustomer.setPropertyValue(channelStr.toString());
                    customerDao.saveOrUpdate(channelOldCustomer);
                }
            }

        }
    }

    /**
     * @description 批量添加坐席信息
     * @author:duanliying
     * @method
     * @date: 2018/9/19 19:01
     */
    public Map<String, String> addSeatsList(SeatsMessageParam seatsMessageParam, Long accountUserId) throws Exception {
        List<SeatsInfo> seatsInfoList = seatsMessageParam.getSeatsInfoList();
        logger.info("保存坐席信息传递参数是" + String.valueOf(seatsInfoList));
        String resourceId = null;
        //获取企业id
        String custId = seatsMessageParam.getCustId();
        //添加坐席前判断余额是否充足和是否设置销售定价
        Map<String, String> resultMap = checkSeatsBalance(custId, seatsInfoList);
        logger.info("查询坐席是否设置销售定价返回结果" + resultMap.toString());
        if (resultMap.size() > 0 && "1".equals(resultMap.get("result"))) {
            //添加坐席前判断坐席账号是否重复
            boolean seatsFlag = checkSeatsRepeat(seatsMessageParam);
            if (seatsFlag == false) {
                String seatResult = null;
                for (int i = 0; i < seatsInfoList.size(); i++) {
                    SeatsInfo seatsInfo = seatsInfoList.get(i);
                    Long userId = IDHelper.getID();
                    //添加坐席的登陆账号密码
                    CustomerUser customerUserDO = new CustomerUser();
                    customerUserDO.setId(userId);
                    customerUserDO.setAccount(seatsInfoList.get(i).getAccount());
                    customerUserDO.setPassword(CipherUtil.encodeByMD5(seatsInfoList.get(i).getPassword()));
                    customerUserDO.setCust_id(custId);
                    customerUserDO.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
                    customerUserDO.setStatus(0);
                    customerUserDO.setUserType(2);
                    customerUserDao.save(customerUserDO);
                    //保存坐席手机号码
                    CustomerUserProperty customerPropertyDo = new CustomerUserProperty();
                    customerPropertyDo.setUserId(String.valueOf(userId));
                    customerPropertyDo.setPropertyName("mobile_num");
                    customerPropertyDo.setPropertyValue(seatsInfoList.get(i).getPhoneNum());
                    customerPropertyDo.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
                    customerUserDao.saveOrUpdate(customerPropertyDo);
                    //保存坐席的账号+密码
                    List<Map<String, Object>> userPropertyInfoList = seatsInfo.getUserPropertyInfoList();
                    String channel = null, seatId = null, seatMinute = null, seatName = null;
                    //供应商坐席分钟数
                    boolean flag = false;
                    for (int j = 0; j < userPropertyInfoList.size(); j++) {
                        seatId = String.valueOf(userPropertyInfoList.get(j).get("seatId"));
                        channel = String.valueOf(userPropertyInfoList.get(j).get("channel"));
                        if (StringUtil.isNotEmpty(seatId) && StringUtil.isNotEmpty(channel)) {
                            logger.info("坐席id是" + seatId + "渠道是：" + channel);
                            //渠道  2-联通 3-电信 4-移动
                            SupplierEnum callIdPropertyName = SupplierEnum.getCallIdPropertyName(channel);
                            if (callIdPropertyName != null) {
                                seatName = callIdPropertyName.getSeatName();
                                seatMinute = callIdPropertyName.getSeatMinute();
                            }

                            //将坐席账号密码坐席Id处理成json存入数据库
                            HashMap<String, String> map = new HashMap<>();
                            map.put("seatId", String.valueOf(userPropertyInfoList.get(j).get("seatId")));
                            map.put("seatPassword", String.valueOf(userPropertyInfoList.get(j).get("seatPassword")));
                            if (StringUtil.isNotEmpty(String.valueOf(userPropertyInfoList.get(j).get("seatName")))) {
                                map.put("seatName", String.valueOf(userPropertyInfoList.get(j).get("seatName")));
                            }
                      /*  //通过userId,查询企业呼叫中心id
                        String sql= "SELECT u.cust_id,t.property_value FROM t_customer_user u LEFT JOIN t_customer_property t ON u.cust_id = t.cust_id AND t.property_name = 'cuc_call_id' WHERE u.id =?";
                        List<Map<String, Object>> maps = customerUserDao.sqlQuery(sql,seatsInfo.getUserId());
                        if (maps.size()>0){
                            UNICOM_ENT_ID=String.valueOf(maps.get(0).get("property_value"));
                        }
                    //调用联通注册坐席接口
                    Map<String, Object> registerSeatsMessage = new CallCenterServiceImpl().unicomAddSeatAccount(UNICOM_ENT_ID, String.valueOf(userPropertyInfoList.get(j).get("seatId")), String.valueOf(userPropertyInfoList.get(j).get("seatName")), String.valueOf(userPropertyInfoList.get(j).get("seatPassword")));
                    logger.info("坐席注册结果" + ":" + registerSeatsMessage);
                    if (registerSeatsMessage.get("code").equals("000")) {*/
                            //根据supplierId和type查询resourceId
                            String queryResourceIdSql = "SELECT resource_id from t_market_resource WHERE supplier_id = " + channel + " AND type_code = " + ResourceEnum.CALL.getType();
                            List<Map<String, Object>> list = sourceDao.sqlQuery(queryResourceIdSql, new Object[]{});
                            if (list.size() > 0) {
                                resourceId = String.valueOf(list.get(0).get("resource_id"));
                            }
                            //根据供应商id查询坐席对象
                            CustomerUserProperty customerUser = new CustomerUserProperty();
                            customerUser.setUserId(String.valueOf(userId));
                            customerUser.setPropertyName(seatName);
                            customerUser.setPropertyValue(JSON.toJSONString(map));
                            customerUser.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
                            customerUserDao.saveOrUpdate(customerUser);
                            //判断当前日期是否在15号之后，是扣除一半坐席费用
                            String time = "15 00:00:00";
                            //现在时间
                            Date dateNow = null;
                            Date date = null;
                            String custSeatsPriceStr = null;
                            int supSeatMinute = 0, custSeatMinute = 0;
                            BigDecimal seatPrice = new BigDecimal("0");
                            SimpleDateFormat formatter = new SimpleDateFormat("dd HH:mm:ss");
                            try {
                                date = formatter.parse(time);
                                String format = new SimpleDateFormat("dd HH:mm:ss").format(new Date());
                                dateNow = formatter.parse(format);
                                flag = date.before(dateNow);
                                //查询企业坐席销售定价
                                ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, custId);
                                if (resourcesPriceDto != null) {
                                    custSeatsPriceStr = resourcesPriceDto.getSeatPrice();
                                    custSeatMinute = resourcesPriceDto.getSeatMinute();
                                }
                                if (flag) {
                                    //15号之前扣除一半金额
                                    custSeatsPriceStr = new BigDecimal(custSeatsPriceStr).divide(new BigDecimal(2)).toString();
                                    logger.info("坐席注册扣费客户:" + custId + "坐席注册单价:" + custSeatsPriceStr);
                                    if (custSeatMinute > 0) {
                                        custSeatMinute = new BigDecimal(custSeatMinute).divide(new BigDecimal(2)).setScale(0, BigDecimal.ROUND_DOWN).intValue();
                                        logger.info("坐席注册扣费客户:" + custId + "坐席分钟数:" + custSeatMinute);
                                    }
                                }
                                // 企业账户余额扣款
                                logger.info("坐席注册扣费客户:" + custId + "坐席注册单价:" + custSeatsPriceStr);
                                seatPrice = new BigDecimal(custSeatsPriceStr).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN);
                                logger.info("坐席注册扣费客户:" + custId + ",开始扣费,金额(分):" + seatPrice.intValue());
                                boolean seatAccountStatus = customerDao.accountDeductions(custId, seatPrice);
                                logger.info("坐席注册扣费客户:" + custId + ",扣费状态:" + seatAccountStatus);
                                //根据渠道和企业id查询成本价
                                ResourcesPriceDto supResourcesPriceDto = null;
                                if (StringUtil.isNotEmpty(resourceId)) {
                                    supResourcesPriceDto = supplierDao.getSupResourceMessageById(Integer.parseInt(resourceId), null);
                                }

                                //成本价坐席
                                BigDecimal seatSpperPrice = new BigDecimal("0");
                                if (supResourcesPriceDto != null) {
                                    supSeatMinute = supResourcesPriceDto.getSeatMinute();
                                    String supSeatPriceStr = supResourcesPriceDto.getSeatPrice();
                                    if (StringUtil.isNotEmpty(supSeatPriceStr)) {
                                        seatSpperPrice = new BigDecimal(supSeatPriceStr).multiply(new BigDecimal(100));
                                    }

                                    if (flag) {
                                        //15号之前扣除一半金额  供应商扣费(定价是元扣费时需要变成分)
                                        seatSpperPrice = seatSpperPrice.divide(new BigDecimal("2"));
                                        logger.info("坐席注册扣费供应商:" + custId + "开始保存交易记录,金额:" + seatSpperPrice.intValue());
                                        //分钟数剩余一半
                                        if (supSeatMinute > 0) {
                                            supSeatMinute = (new BigDecimal(supSeatMinute).divide(new BigDecimal(2)).setScale(0, BigDecimal.ROUND_DOWN)).intValue();
                                            logger.info("坐席注册扣费客户:" + custId + "坐席注册分钟数是:" + supSeatMinute);
                                        }
                                    }
                                    boolean seatPriceStatus = sourceDao.supplierAccountDuctions(String.valueOf(channel), seatSpperPrice);
                                    logger.info("开通坐席扣费供应商:" + custId + "扣费状态是" + seatPriceStatus);
                                }
                                //保存交易记录（企业 + 供应商）
                                boolean seatAmountStatus = transactionService.saveTransactionLog(custId, TransactionEnum.SEAT_DEDUCTION.getType(), seatPrice.intValue(), 1, String.valueOf(channel),
                                        "坐席扣费", accountUserId, "", "", seatSpperPrice.intValue(), resourceId);
                                logger.info("坐席扣费客户:" + custId + "保存交易记录状态:" + seatAmountStatus);
                                //添加坐席套餐分钟数在user属性表中
                                CustomerUserProperty customerUserProperty = new CustomerUserProperty();
                                customerUserProperty.setUserId(String.valueOf(userId));
                                customerUserProperty.setPropertyName(seatMinute);
                                customerUserProperty.setPropertyValue(String.valueOf(custSeatMinute));
                                customerUserProperty.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
                                customerUserDao.saveOrUpdate(customerUserProperty);

                                //保存坐席的供应商分钟数
                                CustomerUserProperty customerSupplierMinute = new CustomerUserProperty();
                                customerSupplierMinute.setUserId(String.valueOf(userId));
                                customerSupplierMinute.setPropertyName(resourceId + "_minute");
                                customerSupplierMinute.setPropertyValue(String.valueOf(supSeatMinute));
                                customerSupplierMinute.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
                                customerUserDao.saveOrUpdate(customerSupplierMinute);
                                //}
                                // }
                            } catch (Exception e) {
                                logger.error("创建坐席异常" + e);
                            }
                        }
                    }
                }
                resultMap.put("result", "1");
                resultMap.put("_message", "坐席添加成功");
                return resultMap;
            } else {
                resultMap.put("result", "0");
                resultMap.put("_message", "坐席添加失败,账号重复,请重新添加");
                return resultMap;
            }

        } else {
            return resultMap;
        }
    }

    /**
     * @description 单独编辑坐席信息（添加修改）
     * @author:duanliying
     * @method
     * @date: 2018/9/27 13:40
     */
    @Override
    public int updateSeatMessage(SeatsInfo seatsInfo) {
        //1-讯众 2-联通 3-电信 4-移动
        String channel = seatsInfo.getChannel();
        String custId = seatsInfo.getCustId();
        HashMap<String, String> map = new HashMap<>();
        int update = 0;
        SupplierEnum callIdPropertyName = SupplierEnum.getCallIdPropertyName(channel);
        //根据渠道查询坐席名字
        String propertyName = callIdPropertyName.getSeatName();
        String selectManageSql = "select * from t_customer_user_property where user_id = ? and property_name= ?";
        List<Map<String, Object>> seatList = jdbcTemplate.queryForList(selectManageSql, seatsInfo.getUserId(), propertyName);
        if (seatList.size() > 0) {
            com.alibaba.fastjson.JSONObject json = JSON.parseObject(String.valueOf(seatList.get(0).get("property_value")));
            //区分前台修改主叫号码还是后台
            if (seatsInfo.getType().equals("1")) {
                map.put("seatId", json.getString("seatId"));
                map.put("seatPassword", json.getString("seatPassword"));
                map.put("seatName", json.getString("seatName"));
                map.put("mainNumber", seatsInfo.getMainNumber());
            } else {
                map.put("seatId", seatsInfo.getSeatId());
                map.put("seatPassword", seatsInfo.getSeatPassword());
                map.put("seatName", seatsInfo.getSeatName());
                map.put("mainNumber", seatsInfo.getMainNumber());
            }
            Map<String, Object> result = null;
            Map<String, Object> seatsPasswordResult = null;
            //通过企业id和资源id查询企业呼叫中心id
            MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
            String callCenterId = null;
            if (marketResourceEntity != null && marketResourceEntity.getResourceId() > 0) {
                logger.info("查询资源信息是" + marketResourceEntity.getResourceId() + "企业id是：" + custId);
                ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(String.valueOf(marketResourceEntity.getResourceId()), custId);
                if (resourcesPriceDto != null) {
                    callCenterId = resourcesPriceDto.getCallCenterId();
                    logger.info("查询到的企业呼叫中心id是" + callCenterId);
                }
            }
            //如果是后台修改密码  前台只能修改主叫号码
            if (seatsInfo.getType().equals("2")) {
                //修改坐席密码 同时调用联通接口修改坐席密码
                if (!seatsInfo.getSeatPassword().equals(json.getString("seatPassword"))) {
                    seatsPasswordResult = new CallCenterServiceImpl().unicomUpdateSeatPasswd(callCenterId, seatsInfo.getSeatId(), seatsInfo.getSeatPassword());
                    logger.info("坐席密码修改" + ":" + seatsPasswordResult);
                }
            }
            //调用修改主叫号码接口
            if (seatsInfo != null && seatsInfo.getMainNumber() != null && !seatsInfo.getMainNumber().equals(json.getString("mainNumber")) && "2".equals(channel)) {
              /*  if (json.getString("mainNumber") != null) {
                    //先删除联通注册上的主叫号码（分机号码）
                    Map<String, Object> extensionDeleteResult = new CallCenterServiceImpl().unicomExtensionDelete(UNICOM_ENT_ID, json.getString("mainNumber"));
                    logger.info("坐席主叫号码删除" + ":" + extensionDeleteResult);
                }*/
                //调用联通接口进行增加主叫号码
                logger.info("修改主叫号码使用的企业外呼id是：" + ":" + callCenterId);
                result = new CallCenterServiceImpl().unicomExtensionRegister(callCenterId, seatsInfo.getMainNumber(), 1);
                logger.info("坐席主叫号增加" + ":" + result);

                if (result.get("result") != null && result.get("result").equals("0") || result.get("code").equals("211") || result.get("code").equals("213")) {
                    //查询坐席是否存在 不存在做插入
                    if (seatList.size() == 0) {
                        String insertSql = "insert into t_customer_user_property(user_id,property_name,property_value,create_time) values(?,?,?,now())";
                        update = jdbcTemplate.update(insertSql, new Object[]{seatsInfo.getUserId(), propertyName, JSON.toJSONString(map),});
                    } else {
                        String updateSql = "update t_customer_user_property SET property_value= ? where property_name=? AND user_id= ? ";
                        update = jdbcTemplate.update(updateSql, new Object[]{JSON.toJSONString(map), propertyName, seatsInfo.getUserId()});
                    }
                }
            } else {
                //修改讯众主叫号码
                if (seatList.size() == 0) {
                    String insertSql = "insert into t_customer_user_property(user_id,property_name,property_value,create_time) values(?,?,?,now())";
                    update = jdbcTemplate.update(insertSql, new Object[]{seatsInfo.getUserId(), propertyName, JSON.toJSONString(map),});
                } else {
                    String updateSql = "update t_customer_user_property SET property_value= ? where property_name=? AND user_id= ? ";
                    update = jdbcTemplate.update(updateSql, new Object[]{JSON.toJSONString(map), propertyName, seatsInfo.getUserId()});
                }
            }
        }
        return update;
    }

    @Override
    public String updatePlatformMessage(SeatsInfo seatsInfo) {
        String phoneNum = seatsInfo.getPhoneNum();
        String userId = seatsInfo.getUserId();
        String password = seatsInfo.getPassword();
        String realName = seatsInfo.getRealName();
        int update = 0;
        String code = "1";
        //判断新旧密码是否相同  不同就无法修改
        try {
            //更改平台的账户密码和手机号码
            //修改密码
            if (StringUtil.isNotEmpty(password)) {
                String updateSql = "update t_customer_user SET password= ? where id= ? ";
                update = jdbcTemplate.update(updateSql, new Object[]{CipherUtil.encodeByMD5(password), userId});
            }
            if (StringUtil.isNotEmpty(realName)) {
                String updateSql = "update t_customer_user SET realname= ? where id= ? ";
                update = jdbcTemplate.update(updateSql, new Object[]{realName, userId});
            }
            String selectPhoneSql = "select * from t_customer_user_property where user_id = ? and property_name= ?";
            List<Map<String, Object>> phoneList = jdbcTemplate.queryForList(selectPhoneSql, seatsInfo.getUserId(), "mobile_num");
            if (phoneList.size() > 0) {
                if (StringUtil.isNotEmpty(phoneNum)) {
                    //修改手机号码
                    String updateSql = "update t_customer_user_property SET property_value= ? where  user_id= ? AND property_name =?";
                    update = jdbcTemplate.update(updateSql, new Object[]{phoneNum, seatsInfo.getUserId(), "mobile_num"});
                }
            } else {
                String insertSql = "insert into t_customer_user_property(user_id,property_name,property_value,create_time) values(?,?,?,now())";
                update = jdbcTemplate.update(insertSql, new Object[]{seatsInfo.getUserId(), "mobile_num", phoneNum});
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            logger.error("更改平台信息错误" + e);
            code = "3";
        }

        return code;
    }

    /**
     * 获取企业坐席列表
     *
     * @param page
     * @param customerRegistDTO
     * @return
     */
    @Override
    public Page getCustomerInfo(PageParam page, CustomerRegistDTO customerRegistDTO) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT\n" +
                "\tt1.cust_id AS custId,\n" +
                "\tt1.enterprise_name AS enterpriseName,t1.create_time,\n" +
                "\tcast(t2.id AS CHAR) AS userId,\n" +
                "\tt3.account AS adminAccount,\n" +
                "(\n" +
                "\t\tSELECT\n" +
                "\t\t\tproperty_value\n" +
                "\t\tFROM\n" +
                "\t\t\tt_customer_property \n" +
                "\t\tWHERE\n" +
                "\t\t\tproperty_name = 'resource_type' AND t_customer_property.cust_id = t1.cust_id\n" +
                "\t) resourceType ,\n" +
                "(\n" +
                "\t\tSELECT\n" +
                "\t\t\tCOUNT(0)\n" +
                "\t\tFROM\n" +
                "\t\t\tt_customer_user \n" +
                "\t\tWHERE\n" +
                "\t\t\tt_customer_user.cust_id = t1.cust_id AND t_customer_user.user_type = '2'\n" +
                "\t) seatCount\n" +
                "FROM\n" +
                "\tt_customer t1\n" +
                "LEFT JOIN t_user t2 ON t1.cust_id = t2.cust_id\n" +
                "LEFT JOIN t_customer_user t3 ON t1.cust_id = t3.cust_id\n" +
                "WHERE\n" +
                "\t1 = 1 AND t3.user_type=1 ");
        if (StringUtil.isNotEmpty(customerRegistDTO.getCustId())) {
            sqlBuilder.append(" AND t1.cust_id = " + customerRegistDTO.getCustId());
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getEnterpriseName())) {
            sqlBuilder.append(" AND t1.enterprise_name = '" + customerRegistDTO.getEnterpriseName() + "'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getName())) {
            sqlBuilder.append(" AND t3.account LIKE '%" + customerRegistDTO.getName() + "%'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getStartTime())) {
            sqlBuilder.append(" AND t1.create_time >= '" + customerRegistDTO.getStartTime() + "'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getEndTime())) {
            sqlBuilder.append(" AND t1.create_time <= '" + customerRegistDTO.getEndTime() + "'");
        }
        sqlBuilder.append(" GROUP BY t1.create_time DESC");
        return new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
    }

    /**
     * 获取坐席列表信息
     *
     * @param
     * @return
     */
    @Override
    public Map<String, Object> getSeatsMessage(String custId, Integer pageNum, Integer pageSize) {
        //前台传的是页数 需要转化成起始值
        Integer start = (pageNum - 1) * pageSize;
        List<SeatsMessageParam> seatsParamsList = new ArrayList<SeatsMessageParam>();
        String channelProperty = null;
        CustomerProperty channelList = null;
        HashMap<String, Object> map = new HashMap<>();
        //获取平台信息集合
        StringBuffer hql = new StringBuffer(" from CustomerUserDO m where 1=1");
        List<String> values = new ArrayList();
        if (custId != null && !custId.equals("")) {
            hql.append(" and m.cust_id = ?");
            values.add(custId);
        }
        hql.append(" and userType='2' ");
        hql.append(" ORDER BY m.createTime desc ");
        com.bdaim.common.dto.Page page = customerUserDao.page(hql.toString(), values, start, pageSize);
        if (page.getData() != null && page.getData().size() > 0) {
            List<CustomerUser> customerUserList = page.getData();
            channelList = customerDao.getProperty(custId, "channel");
            logger.info("企业id是：" + custId + "现在已经添加的渠道是" + String.valueOf(channelList));
            for (int i = 0; i < customerUserList.size(); i++) {
                SeatsMessageParam seatsMessageParam = new SeatsMessageParam();
                ArrayList<SeatsInfo> seatsList = new ArrayList<>();
                //获取平台的userId
                Long id = customerUserList.get(i).getId();
                String getUserIdSql = "SELECT * FROM t_customer_user_property WHERE user_id= ? AND property_name= ?  ";
                List<Map<String, Object>> mobileNum = jdbcTemplate.queryForList(getUserIdSql, String.valueOf(id), "mobile_num");
                //遍历渠道信息 拼接key
                if (channelList != null) {
                    List<String> channels = Arrays.asList(String.valueOf(channelList.getPropertyValue()).split(","));
                    if (channels.size() > 0) {
                        for (int j = 0; j < channels.size(); j++) {
                            SupplierEnum callIdPropertyName = SupplierEnum.getCallIdPropertyName(channels.get(j));
                            if (callIdPropertyName!=null){
                                channelProperty = callIdPropertyName.getSeatName();
                                //根据userid获取坐席信息
                                CustomerUserProperty userProperty = customerUserDao.getProperty(String.valueOf(id), channelProperty);
                                //將json串存入对象
                                if (userProperty != null) {
                                    com.alibaba.fastjson.JSONObject json = JSON.parseObject(userProperty.getPropertyValue());
                                    SeatsInfo seatsInfo = new SeatsInfo();
                                    //获取主叫号码
                                    seatsInfo.setMainNumber(json.getString("mainNumber"));
                                    //坐席账号（id）
                                    seatsInfo.setSeatId(json.getString("seatId"));
                                    seatsInfo.setSeatName(json.getString("seatName"));
                                    seatsInfo.setSeatPassword(json.getString("seatPassword"));
                                    seatsInfo.setChannel(channels.get(j));
                                    seatsList.add(seatsInfo);
                                }
                            }
                        }
                        seatsMessageParam.setSeatsInfoList(seatsList);
                        seatsMessageParam.setAccount(customerUserList.get(i).getAccount());
                        seatsMessageParam.setUserId(String.valueOf(id));
                        seatsMessageParam.setPassword(customerUserList.get(i).getPassword());
                        seatsMessageParam.setRealName(customerUserList.get(i).getRealname());
                        seatsMessageParam.setCreateTime(customerUserList.get(i).getCreateTime());
                        seatsMessageParam.setStatus(customerUserList.get(i).getStatus());
                        if (mobileNum.size() > 0) {
                            seatsMessageParam.setMobileNum(String.valueOf(mobileNum.get(0).get("property_value")));
                        }
                        seatsParamsList.add(seatsMessageParam);
                    }
                }
            }
        }
        //查询企业信息
        Customer customerDO = customerDao.findUniqueBy("custId", custId);
        List<CustomerUser> customerUser = customerUserDao.getPropertyByType(1, custId);
        CustomerRegistDTO customerRegistDTO = new CustomerRegistDTO();
        customerRegistDTO.setEnterpriseName(customerDO.getEnterpriseName());
        customerRegistDTO.setName(customerUser.get(0).getAccount());
        logger.info("查询供应商是：" + channelList.getPropertyValue());
        customerRegistDTO.setChannel(String.valueOf(channelList.getPropertyValue()));
        map.put("seatsParamsList", seatsParamsList);
        map.put("total", page.getTotal());
        map.put("CustomerRegistDTO", customerRegistDTO);
        return map;
    }

    /**
     * @description 修改坐席账号有效状态(根0 - 有效 1 - 无效)
     * @author:duanliying
     * @method
     * @date: 2018/9/21 10:51
     */
    @Override
    public void updateSeatsStatus(String id, int status, String channel, String custId) throws Exception {
        //根据id和属性名查询seats_status
        List<CustomerUser> customerUserDO = customerUserDao.findBy("id", Long.valueOf(id));
        if (customerUserDO.size() > 0) {
            if (customerUserDO.get(0) != null) {
                customerUserDO.get(0).setStatus(status);
                customerUserDao.saveOrUpdate(customerUserDO.get(0));
            }
            if (status == 0) {
                if (StringUtil.isNotEmpty(channel)) {
                    //修改坐席状态为有效后查询更新坐席分钟数
                    String[] split = channel.split(",");
                    if (split.length > 0) {
                        //根据供应商查询坐席分钟数
                        for (int i = 0; i < split.length; i++) {
                            //根据供应商
                            MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(split[i], ResourceEnum.CALL.getType());
                            Integer resourceId = marketResourceEntity.getResourceId();
                            logger.info("资源id是：" + resourceId);
                            JSONObject customerMarketResource = null, supplierMarketResource = null;
                            String custSeatMinute = null, supplierSeatMinute = null;
                            if (resourceId != null) {
                                //根据rensourceId查询坐席分钟数
                                customerMarketResource = marketResourceServiceImpl.getCustomerMarketResource(custId, String.valueOf(resourceId));
                                supplierMarketResource = marketResourceServiceImpl.getSupplierMarketResource((resourceId));
                            }
                            if (customerMarketResource != null || supplierMarketResource != null) {
                                //                custSeatMinute = customerMarketResource.getString(ResourceEnum.CALL.getSeatMinute());
                                //                supplierSeatMinute = supplierMarketResource.getString(ResourceEnum.CALL.getSeatMinute());
                                logger.info("企业坐席分钟数是：" + custSeatMinute + " 供应商坐席分钟数是：" + supplierSeatMinute);
                            }
                            SupplierEnum callIdPropertyName = SupplierEnum.getCallIdPropertyName(split[i]);
                            String seatMinute = callIdPropertyName.getSeatMinute();
                            //查询当前坐席下企业分钟数是否存在
                            CustomerUserProperty seatMiuteInfo = customerUserDao.getProperty(id, seatMinute);
                            if (seatMiuteInfo != null) {
                                seatMiuteInfo.setPropertyValue(custSeatMinute);
                                customerUserDao.saveOrUpdate(seatMiuteInfo);
                            } else {
                                CustomerUserProperty customerUserProperty = new CustomerUserProperty();
                                customerUserProperty.setUserId(id);
                                customerUserProperty.setPropertyName(seatMinute);
                                customerUserProperty.setPropertyValue(custSeatMinute);
                                customerUserProperty.setCreateTime(String.valueOf(new Timestamp(System.currentTimeMillis())));
                                customerUserDao.saveOrUpdate(customerUserProperty);
                            }

                            //查询当前坐席下供应商分钟数是否存在
                            CustomerUserProperty supSeatInfo = customerUserDao.getProperty(id, resourceId + "_minute");
                            if (supSeatInfo != null) {
                                supSeatInfo.setPropertyValue(supplierSeatMinute);
                                customerUserDao.saveOrUpdate(supSeatInfo);
                            } else {
                                CustomerUserProperty customerUserProperty = new CustomerUserProperty();
                                customerUserProperty.setUserId(id);
                                customerUserProperty.setPropertyName(resourceId + "_minute");
                                customerUserProperty.setPropertyValue(supplierSeatMinute);
                                customerUserProperty.setCreateTime(String.valueOf(new Timestamp(System.currentTimeMillis())));
                                customerUserDao.saveOrUpdate(customerUserProperty);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * @description 获取拨打电话所需参数
     * @author:duanliying
     * @method
     * @date: 2018/9/21 11:12
     */
    public SeatsInfo getCallProperty(String userId, String properName) {
        SeatsInfo seatsInfo = new SeatsInfo();
        String getUserIdSql = "SELECT * FROM t_customer_user_property WHERE user_id= ? AND property_name= ?  ";
        List<Map<String, Object>> seatsMessages = jdbcTemplate.queryForList(getUserIdSql, userId, properName);
        if (seatsMessages.size() > 0) {
            com.alibaba.fastjson.JSONObject json = JSON.parseObject(seatsMessages.get(0).get("property_value").toString());
            //获取主叫号码
            seatsInfo.setMainNumber(json.getString("mainNumber"));
            //坐席账号（id）
            seatsInfo.setSeatId(json.getString("seatId"));
            seatsInfo.setSeatName(json.getString("seatName"));
            seatsInfo.setSeatPassword(json.getString("seatPassword"));
        }
        return seatsInfo;
    }

    /**
     * @description
     * @author:duanliying
     * @method cmc_apparent_number 移动外显号码 cmc_enterprise_id企业外呼id   cuc_enterprise_password企业密码
     * channel   cmc 移动   cuc-联通  ctc- 电信
     * @date: 2018/9/25 16:20
     */
    public Map<String, String> getEnterpriseMessage(String custId, String channel) {
        HashMap<String, String> map = new HashMap<>();
        //根据cust_id查询所有信息
        String getIdSql = "SELECT * FROM t_customer_property WHERE cust_id= ?";
        List<Map<String, Object>> seatsMessages = customerDao.sqlQuery(getIdSql, custId);
        for (int i = 0; i < seatsMessages.size(); i++) {
            // cuc_seat
            if (String.valueOf(seatsMessages.get(i).get("property_name")).contains(channel)) {
                map.put(String.valueOf(seatsMessages.get(i).get("property_name")), String.valueOf(seatsMessages.get(i).get("property_value")));
            }
        }
        return map;
    }

    /**
     * @description 获取渠道配置信息
     * @author:duanliying
     * @method
     * @date: 2018/10/22 18:04
     */
    @Override
    public Object getChannelList(String custId) {
        HashMap<String, Object> map = new HashMap<>();
        //查询枚举类查找所有渠道信息
        for (SupplierEnum supper : SupplierEnum.values()) {
            SeatsMessageParam seatsAllocation = new SeatsMessageParam();
            //根据key查询配置value
            CustomerProperty callId = customerDao.getProperty(custId, supper.getCallId());
            CustomerProperty apparentNumber = customerDao.getProperty(custId, supper.getApparentNumber());
            CustomerProperty activityId = customerDao.getProperty(custId, supper.getActivityId());
            //地址渠道 暂时没有确定
            CustomerProperty configId = customerDao.getProperty(custId, supper.getConfig());
            if (callId != null) {
                seatsAllocation.setCallEnterpriseId(callId.getPropertyValue());
            }
            if (apparentNumber != null) {
                seatsAllocation.setApparentNumbera(apparentNumber.getPropertyValue());
            }
            if (activityId != null) {
                seatsAllocation.setActivityId(activityId.getPropertyValue());
            }
            if (configId != null) {
                seatsAllocation.setConfigId(configId.getPropertyValue());
            }
            seatsAllocation.setChannel(String.valueOf(supper.getSupplierId()));
            map.put(supper.getSupplierName() + "SeatsMessage", seatsAllocation);
        }

        //查询企业信息
        String querySql = "select t1.cust_id,t1.account,t2.enterprise_name FROM t_customer_user t1 LEFT JOIN t_customer t2 ON t1.cust_id=t2.cust_id where t1.cust_id=?";
        List<Map<String, Object>> cutomerMessage = customerDao.sqlQuery(querySql, custId);
        if (cutomerMessage.size() > 0) {
            map.put("enterpriseNme", cutomerMessage.get(0).get("enterprise_name"));
            map.put("customerAccount", cutomerMessage.get(0).get("account"));
            map.put("custId", cutomerMessage.get(0).get("cust_id"));
        }
        return map;
    }

    /**
     * @description 查询外显号 和 批次下已经选择的外显号码
     * @author:duanliying
     * @method
     * @date: 2018/10/24 13:58
     */
    @Override
    public Map<String, Object> getApparentNum(String batchId, String cust_id) {
        HashMap<String, Object> map = new HashMap<>();
        List<String> apparentbycustId = null;
        List<String> apparentByBatchId = null;
        String suplierId = null;
        BatchListEntity batchListEntity = batchDao.getBatchMessage(batchId);
        if (batchListEntity != null) {
            Integer certifyType = batchListEntity.getCertifyType();
            if (certifyType == 0) {
                suplierId = SupplierEnum.CUC.getSupplierId();
            } else {
                suplierId = SupplierEnum.XZ.getSupplierId();
            }
        }
        MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(suplierId, ResourceEnum.CALL.getType());
        ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(String.valueOf(marketResourceEntity.getResourceId()), cust_id);
        logger.info("查询出企业配置的外显号码是：" + resourcesPriceDto);
        //对查询回来的外显号进行处理
        if (resourcesPriceDto != null) {
            if (resourcesPriceDto.getApparentNumber() != null) {
                apparentbycustId = Arrays.asList(resourcesPriceDto.getApparentNumber().split(","));
            }
        }
        //查詢当前批次下面的外显号
        if (!"".equals(batchListEntity.getApparentNumber()) && batchListEntity.getApparentNumber() != null) {
            apparentByBatchId = Arrays.asList(batchListEntity.getApparentNumber().split(","));
            logger.info("查询批次下以选择的外显号码是：" + apparentByBatchId);
            if (apparentByBatchId != null) {
                map.put("ApparentByBatchId", apparentByBatchId);
            }
        }
        map.put("ApparentbycustId", apparentbycustId);
        map.put("BatchListEntity", batchListEntity);
        return map;
    }

    /**
     * @description 为批次设置外显号
     * @author:duanliying
     * @date: 2018/10/24 14:54
     */
    @Override
    public void updateApparentNum(String batchId, String apparentNums) throws Exception {
        //根据批次查询对象
        BatchListEntity batchListEntity = batchListDao.findUniqueBy("id", batchId);
        batchListEntity.setApparentNumber(apparentNums);
        batchListDao.saveOrUpdate(batchListEntity);
    }

    /**
     * @description 前台验证平台和坐席账号不可重复
     * @author:duanliying
     * @method
     * @date: 2018/11/2 14:03
     */
    @Override
    public int checkAccount(String account, String seatId) {
        int code = 0;
        if (account != null && !account.equals("")) {
            //查询此登陆账户是否存在
            List<CustomerUser> userAccount = customerUserDao.findBy("account", account);
            if (userAccount.size() > 0) {
                //平台账户已经存在不可以重复添加
                code = 1;
            }
            if (seatId != null && !seatId.equals("")) {
                if (seatId.equals(account)) {
                    code = 2;
                }
            }
        }
        return code;
    }

    @Override
    public List<CustomerUserProperty> getUserAllProperty(String userId) {
        return customerUserDao.getAllProperty(userId);
    }


    /**
     * 获取客户注册坐席单价（销售定价）
     *
     * @param custId
     * @return
     */
    public double getCustSeatsPrice(String custId, String channel) {
        CustomerProperty seatPrice = customerDao.getProperty(custId, channel + "_seat_price");
        if (seatPrice != null && StringUtil.isNotEmpty(seatPrice.getPropertyValue())) {
            return Integer.parseInt(seatPrice.getPropertyValue());
        }
        return 0;
    }


    /**
     * @description 添加坐席后台验证坐席登陆账号是否重复
     * @author:duanliying
     * @method
     * @date: 2018/11/12 13:54
     */
    public boolean checkSeatsRepeat(SeatsMessageParam seatsMessageParam) {
        boolean flag = true;
        List<SeatsInfo> seatsInfoList = seatsMessageParam.getSeatsInfoList();
        for (int i = 0; i < seatsInfoList.size(); i++) {
            if (seatsInfoList.get(i).getAccount() != null && !seatsInfoList.get(i).getAccount().equals("")) {
                //查询此登陆账户是否存在
                List<CustomerUser> userAccount = customerUserDao.findBy("account", seatsInfoList.get(i).getAccount());
                if (userAccount.size() > 0) {
                    //账号已经存在
                    flag = true;
                } else
                    flag = false;
            }
        }
        return flag;
    }

    /**
     * @description 添加坐席前进行余额判断
     * @author:duanliying
     * @method
     * @date: 2018/11/12 14:08
     */
    public Map<String, String> checkSeatsBalance(String custId, List<SeatsInfo> seatsInfoList) throws Exception {
        Map<String, String> resultMap = new HashMap<String, String>();
        int cucCount = 0, cmcCount = 0, ctcCount = 0, xzCount = 0;
        for (int i = 0; i < seatsInfoList.size(); i++) {
            List<Map<String, Object>> userPropertyInfoList = seatsInfoList.get(i).getUserPropertyInfoList();
            for (int j = 0; j < userPropertyInfoList.size(); j++) {
                String seatId = String.valueOf(userPropertyInfoList.get(j).get("seatId"));
                if (StringUtil.isNotEmpty(seatId)) {
                    int channel = Integer.valueOf(String.valueOf(userPropertyInfoList.get(j).get("channel")));
                    //计算三大运营商添加坐席数量
                    if (channel == 1) {
                        xzCount++;
                    }
                    if (channel == 2) {
                        cucCount++;
                    }
                    if (channel == 4) {
                        cmcCount++;
                    }
                    if (channel == 3) {
                        ctcCount++;
                    }
                }
            }
        }
        BigDecimal xzAmount = null, cucAmount = null, cmcAmount = null, ctcAmount = null;
        //查询三大运营商销售定价
        if (xzCount > 0) {
            ResourcesPriceDto xzResourcesPriceDto = customerDao.getCustResourceMessageByIdAndType(SupplierEnum.XZ.getSupplierId(), ResourceEnum.CALL.getType(), custId);
            if (xzResourcesPriceDto != null && StringUtil.isNotEmpty(xzResourcesPriceDto.getSeatPrice()) && Integer.parseInt(xzResourcesPriceDto.getSeatPrice()) > 0) {
                String custSeatsPrice = xzResourcesPriceDto.getSeatPrice();
                xzAmount = new BigDecimal(custSeatsPrice).multiply(new BigDecimal(xzCount)).multiply(new BigDecimal(100));
            } else {
                resultMap.put("result", "0");
                resultMap.put("_message", "坐席添加失败，讯众坐席未设置销售定价");
                return resultMap;
            }
        }
        if (cucCount > 0) {
            ResourcesPriceDto cucResourcesPriceDto = customerDao.getCustResourceMessageByIdAndType(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType(), custId);
            if (cucResourcesPriceDto != null && StringUtil.isNotEmpty(cucResourcesPriceDto.getSeatPrice()) && Integer.parseInt(cucResourcesPriceDto.getSeatPrice()) > 0) {
                String custSeatsPrice = cucResourcesPriceDto.getSeatPrice();
                cucAmount = new BigDecimal(custSeatsPrice).multiply(new BigDecimal(cucCount)).multiply(new BigDecimal(100));
            } else {
                resultMap.put("result", "0");
                resultMap.put("_message", "坐席添加失败，联通坐席未设置销售定价");
                return resultMap;
            }
        }
        if (cmcCount > 0) {
            ResourcesPriceDto cmcResourcesPriceDto = customerDao.getCustResourceMessageByIdAndType(SupplierEnum.CMC.getSupplierId(), ResourceEnum.CALL.getType(), custId);
            if (cmcResourcesPriceDto != null && StringUtil.isNotEmpty(cmcResourcesPriceDto.getSeatPrice()) && Integer.parseInt(cmcResourcesPriceDto.getSeatPrice()) > 0) {
                String custSeatsPrice = cmcResourcesPriceDto.getSeatPrice();
                cmcAmount = new BigDecimal(custSeatsPrice).multiply(new BigDecimal(cmcCount)).multiply(new BigDecimal(100));
            } else {
                resultMap.put("result", "0");
                resultMap.put("_message", "坐席添加失败，移动坐席未设置销售定价");
                return resultMap;
            }
        }
        if (ctcCount > 0) {
            ResourcesPriceDto ctcResourcesPriceDto = customerDao.getCustResourceMessageByIdAndType(SupplierEnum.CTC.getSupplierId(), ResourceEnum.CALL.getType(), custId);
            if (ctcResourcesPriceDto != null && StringUtil.isNotEmpty(ctcResourcesPriceDto.getSeatPrice()) && Integer.parseInt(ctcResourcesPriceDto.getSeatPrice()) > 0) {
                String custSeatsPrice = ctcResourcesPriceDto.getSeatPrice();
                ctcAmount = new BigDecimal(custSeatsPrice).multiply(new BigDecimal(ctcCount)).multiply(new BigDecimal(100));
            } else {
                resultMap.put("result", "0");
                resultMap.put("_message", "坐席添加失败，电信坐席未设置销售定价");
                return resultMap;
            }
        }
        //查询企业余额
        CustomerProperty customerBalance = customerDao.getProperty(custId, "remain_amount");
        if (customerBalance != null && !customerBalance.equals("")) {
            if (new BigDecimal(customerBalance.getPropertyValue()).compareTo((cucAmount == null ? new BigDecimal("0") : cucAmount).add(xzAmount == null ? new BigDecimal("0") : xzAmount).add(cmcAmount == null ? new BigDecimal("0") : cmcAmount).add(ctcAmount == null ? new BigDecimal("0") : ctcAmount)) >= 0) {
                resultMap.put("result", "1");
            } else {
                resultMap.put("result", "0");
                resultMap.put("_message", "账户余额不足请充值,坐席添加失败");
            }
        }
        return resultMap;
    }

    /**
     * @description 获取主叫号码（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/20 16:02
     */
    @Override
    public Map<String, String> getExtensionNum(String seatAccount, String cust_id, String channel) {
        Map<String, String> resultmap = new HashMap<>();
        String channelProperty = null;
        if ("2".equals(channel)) {
            channelProperty = "cuc";
        }
        if ("4".equals(channel)) {
            channelProperty = "cmc";
        }
        if ("3".equals(channel)) {
            channelProperty = "ctc";
        }
        String code = null;
        String message = null;
        String mainNumber = null;
        //根据custId和seatAccount查询userId
        CustomerUser customer = customerUserDao.getCustomer(seatAccount, cust_id);
        if (customer != null) {
            Long userId = customer.getId();
            //根据user查询坐席配置信息
            CustomerUserProperty customerUser = customerUserDao.getProperty(String.valueOf(userId), channelProperty + "_seat");
            if (customerUser != null) {
                String propertyValue = customerUser.getPropertyValue();
                if (!"".equals(propertyValue)) {
                    com.alibaba.fastjson.JSONObject json = JSON.parseObject(propertyValue);
                    mainNumber = json.getString("mainNumber");
                }
                if (StringUtil.isNotEmpty(mainNumber) || "".equals(mainNumber)) {
                    message = "查询成功";
                    code = "000";
                } else {
                    message = "分机号码未配置";
                    code = "001";
                }
            } else {
                //坐席未配置
                message = "坐席未配置";
                code = "002";
            }
        } else {
            //账号不存在
            message = seatAccount + "账号不存在";
            code = "003";
        }
        resultmap.put("msg", message);
        resultmap.put("code", code);
        resultmap.put("mainNumber", mainNumber);
        return resultmap;
    }

    @Override
    public int updateCallerID(SeatsMessageParam seatsMessageParam, String custId) {
        List<SeatsInfo> list = seatsMessageParam.getSeatsInfoList();
        String UserId = list.get(0).getUserId();
        HashMap<String, String> map = new HashMap<>();
        ArrayList<String> channelList = new ArrayList<>();
        String propertyName = null;
        int update = 0;

        //2-联通 3-电信 4-移动
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                String channel = list.get(i).getChannel();
                channelList.add(channel);
                if (channel.equals(ConstantsUtil.SUPPLIERID__CUC)) {
                    propertyName = "cuc_seat";
                }
                if (channel.equals(ConstantsUtil.SUPPLIERID__CMC)) {
                    propertyName = "cmc_seat";
                }
                if (channel.equals(ConstantsUtil.SUPPLIERID__CTC)) {
                    propertyName = "ctc_seat";
                }
                if (channel.equals(ConstantsUtil.SUPPLIERID__XZ)) {
                    propertyName = "xz_seat";
                }


                String selectManageSql = "select * from t_customer_user_property where user_id = ? and property_name= ?";
                List<Map<String, Object>> seatList = jdbcTemplate.queryForList(selectManageSql, UserId, propertyName);
                if (seatList.size() > 0) {
                    com.alibaba.fastjson.JSONObject json = JSON.parseObject(String.valueOf(seatList.get(0).get("property_value")));
                    //区分前台修改主叫号码还是后台
                    if (list.get(0).getType().equals("1")) {
                        if (propertyName.equals("cuc_seat")) {
                            map.put("seatId", json.getString("seatId"));
                            map.put("seatPassword", json.getString("seatPassword"));
                            map.put("seatName", json.getString("seatName"));
                            map.put("mainNumber", list.get(i).getMainNumber());
                        }
                        if (propertyName.equals("xz_seat")) {
                            map.put("seatId", json.getString("seatId"));
                            map.put("seatPassword", json.getString("seatPassword"));
                            map.put("seatName", json.getString("seatName"));
                            map.put("mainNumber", list.get(i).getMainNumber());
                        }

                    }
                    MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
                    String callCenterId = null;
                    if (marketResourceEntity != null && marketResourceEntity.getResourceId() > 0) {
                        logger.info("查询资源信息是" + marketResourceEntity.getResourceId() + "企业id是：" + custId);
                        ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(String.valueOf(marketResourceEntity.getResourceId()), custId);
                        if (resourcesPriceDto != null) {
                            callCenterId = resourcesPriceDto.getCallCenterId();
                            logger.info("查询到的企业呼叫中心id是" + callCenterId);
                        }
                    }
                    Map<String, Object> resultl = null;
                    Map<String, Object> seatsPasswordResult = null;
                    //如果是后台修改密码  前台只能修改主叫号码
                    if (list.get(i).getType().equals("2")) {
                        //修改坐席密码 同时调用联通接口修改坐席密码
                        if (!list.get(i).getSeatPassword().equals(json.getString("seatPassword"))) {
                            seatsPasswordResult = new CallCenterServiceImpl().unicomUpdateSeatPasswd(callCenterId, list.get(i).getSeatId(), list.get(i).getSeatPassword());
                            logger.info("坐席密码修改" + ":" + seatsPasswordResult);
                        }
                    }
                    //调用修改主叫号码接口
                    if (!list.get(i).getMainNumber().equals(json.getString("mainNumber")) && channel.equals(ConstantsUtil.SUPPLIERID__CUC)) {
                 /*  if (json.getString("mainNumber") != null) {
                        //先删除联通注册上的主叫号码（分机号码）
                        Map<String, Object> extensionDeleteResult = new CallCenterServiceImpl().unicomExtensionDelete(UNICOM_ENT_ID, json.getString("mainNumber"));
                        logger.info("坐席主叫号码删除" + ":" + extensionDeleteResult);
                    }*/
                        //判断是不是联通的 是修改
                        //调用联通接口进行增加主叫号码
                        resultl = new CallCenterServiceImpl().unicomExtensionRegister(callCenterId, list.get(i).getMainNumber(), 1);
                        logger.info("坐席主叫号增加" + ":" + resultl);
                    }
                    if (resultl != null) {
                        if (resultl.get("result") != null && resultl.get("result").equals("0") || resultl.get("code").equals("211") || resultl.get("code").equals("213") || !channel.equals(ConstantsUtil.SUPPLIERID__CUC)) {
                            //查询坐席是否存在 不存在做插入
                            if (seatList.size() == 0) {
                                String insertSql = "insert into t_customer_user_property(user_id,property_name,property_value,create_time) values(?,?,?,now())";
                                update = jdbcTemplate.update(insertSql, new Object[]{list.get(i).getUserId(), propertyName, JSON.toJSONString(map),});
                            } else {
                                String updateSql = "update t_customer_user_property SET property_value= ? where property_name=? AND user_id= ? ";
                                update = jdbcTemplate.update(updateSql, new Object[]{JSON.toJSONString(map), propertyName, list.get(i).getUserId()});
                            }
                        }
                    } else {
                        if (seatList.size() == 0) {
                            String insertSql = "insert into t_customer_user_property(user_id,property_name,property_value,create_time) values(?,?,?,now())";
                            update = jdbcTemplate.update(insertSql, new Object[]{list.get(i).getUserId(), propertyName, JSON.toJSONString(map),});
                        } else {
                            String updateSql = "update t_customer_user_property SET property_value= ? where property_name=? AND user_id= ? ";
                            update = jdbcTemplate.update(updateSql, new Object[]{JSON.toJSONString(map), propertyName, list.get(i).getUserId()});
                        }
                    }
                }
            }


        }

        return update;
    }

    /**
     * 单独添加坐席接口
     *
     * @param seatsInfo
     * @return
     */
    public Map<String, String> addSeatMessage(SeatsInfo seatsInfo, Long accountUserId) throws Exception {
        String custId = seatsInfo.getCustId();
        String channel = seatsInfo.getChannel();
        String userId = seatsInfo.getUserId();
        HashMap<String, String> map = new HashMap<>();
        String seatId = seatsInfo.getSeatId();
        String seatPsaaword = seatsInfo.getSeatPassword();
        String seatName = seatsInfo.getSeatName();
        Map<String, String> resultMap = new HashMap<>();
        //判断是否设置销售定价和余额是否充足
        String resourceId = null;
        String custSeatPrice = null;
        int custSeatMinute = 0, supSeatMinute = 0;
        String queryResourceIdSql = "SELECT resource_id from t_market_resource WHERE supplier_id = " + channel + " AND type_code = " + ResourceEnum.CALL.getType();
        List<Map<String, Object>> list = sourceDao.sqlQuery(queryResourceIdSql, new Object[]{});
        if (list.size() > 0) {
            resourceId = String.valueOf(list.get(0).get("resource_id"));
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            ResourcesPriceDto custResourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, custId);
            if (custResourcesPriceDto == null || (custResourcesPriceDto.getSeatPrice()!=null && NumberConvertUtil.parseInt(custResourcesPriceDto.getSeatPrice())<0)) {
                resultMap.put("result", "0");
                resultMap.put("_message", "坐席添加失败，坐席未设置销售定价");
                return resultMap;
            } else {
                custSeatPrice = custResourcesPriceDto.getSeatPrice();
                custSeatMinute = custResourcesPriceDto.getSeatMinute();
                logger.info("企业id是：" + custId + " 坐席价格:" + custSeatPrice + " 坐席分钟:" + custSeatMinute);
            }
        }
        logger.info("seatId：" + seatId + " seatPsaaword:" + seatPsaaword + " seatName:" + seatName);
        map.put("seatId", seatId);
        map.put("seatPassword", seatPsaaword);
        if (StringUtil.isNotEmpty(String.valueOf(seatsInfo.getSeatName()))) {
            map.put("seatName", seatName);
        }
        String seatMinute = null, cusSeatName = null;
        SupplierEnum callIdPropertyName = SupplierEnum.getCallIdPropertyName(channel);
        if (callIdPropertyName != null) {
            cusSeatName = callIdPropertyName.getSeatName();
            seatMinute = callIdPropertyName.getSeatMinute();
        }
        //添加坐席json信息
        CustomerUserProperty customerUser = new CustomerUserProperty();
        customerUser.setUserId(String.valueOf(userId));
        customerUser.setPropertyName(cusSeatName);
        customerUser.setPropertyValue(JSON.toJSONString(map));
        customerUser.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
        customerUserDao.saveOrUpdate(customerUser);
        Boolean flag = false;
        logger.info("supplierId是：" + channel);
        //判断当前日期是否在15号之后，是扣除一半坐席费用
        String time = "15 00:00:00";
        //现在时间
        Date dateNow = null;
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd HH:mm:ss");
        try {
            date = formatter.parse(time);
            String format = new SimpleDateFormat("dd HH:mm:ss").format(new Date());
            dateNow = formatter.parse(format);
            flag = date.before(dateNow);
            //查询企业坐席销售定价

            if (flag) {
                custSeatPrice = new BigDecimal(custSeatPrice).divide(new BigDecimal(2)).toString();
                logger.info("坐席注册扣费客户:" + custId + "坐席注册单价:" + custSeatPrice);
                if (custSeatMinute > 0) {
                    custSeatMinute = new BigDecimal(custSeatMinute).divide(new BigDecimal(2)).setScale(0, BigDecimal.ROUND_DOWN).intValue();
                    logger.info("坐席注册扣费客户:" + custId + "坐席分钟数:" + custSeatMinute);
                }
            }
            // 企业账户余额扣款
            logger.info("坐席注册扣费客户:" + custId + "坐席注册单价:" + custSeatPrice);
            BigDecimal seatPrice = new BigDecimal(custSeatPrice).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN);
            boolean seatAccountStatus = customerDao.accountDeductions(custId, seatPrice);
            logger.info("坐席注册扣费客户:" + custId + ",扣费状态:" + seatAccountStatus);
            //成本价坐席
            //根据渠道和企业id查询成本价
            ResourcesPriceDto supResourcesPriceDto = null;
            if (StringUtil.isNotEmpty(resourceId)) {
                supResourcesPriceDto = supplierDao.getSupResourceMessageById(Integer.parseInt(resourceId), null);
            }
            BigDecimal seatSpperPrice = new BigDecimal(0);
            if (supResourcesPriceDto != null) {
                supSeatMinute = supResourcesPriceDto.getSeatMinute();
                String supSeatPriceStr = supResourcesPriceDto.getSeatPrice();
                if (StringUtil.isNotEmpty(supSeatPriceStr)) {
                    seatSpperPrice = new BigDecimal(supSeatPriceStr).multiply(new BigDecimal(100));
                }
                if (flag) {
                    //15号之前扣除一半金额  供应商扣费(定价是元扣费时需要变成分)
                    seatSpperPrice = seatSpperPrice.divide(new BigDecimal("2"));
                    logger.info("坐席注册扣费供应商:" + custId + "开始保存交易记录,金额:" + seatSpperPrice.intValue());
                    //分钟数剩余一半
                    if (supSeatMinute > 0) {
                        supSeatMinute = (new BigDecimal(supSeatMinute).divide(new BigDecimal(2)).setScale(0, BigDecimal.ROUND_DOWN)).intValue();
                        logger.info("坐席注册扣费客户:" + custId + "坐席注册分钟数是:" + supSeatMinute);
                    }
                }
                logger.info("开通坐席扣费供应商:" + custId + "开始注册坐席扣费,金额" + seatSpperPrice);
                boolean seatPriceStatus = sourceDao.supplierAccountDuctions(String.valueOf(channel), seatSpperPrice);
                logger.info("开通坐席扣费供应商:" + custId + "扣费状态是" + seatPriceStatus);
            }
            //保存交易记录（企业 + 供应商）
            logger.info("坐席注册扣费供应商:" + custId + "开始保存交易记录,金额:" + seatSpperPrice.intValue());
            boolean seatAmountStatus = transactionService.saveTransactionLog(custId, TransactionEnum.SEAT_DEDUCTION.getType(), seatPrice.intValue(), 1, String.valueOf(channel),
                    "坐席扣费", accountUserId, "", "", seatSpperPrice.intValue(), resourceId);
            logger.info("坐席扣费客户:" + custId + "保存交易记录状态:" + seatAmountStatus);
            //添加坐席套餐分钟数在user属性表中
            //查询企业属性表的销售定价中坐席套餐分钟数
            CustomerUserProperty customerUserProperty = new CustomerUserProperty();
            customerUserProperty.setUserId(String.valueOf(userId));
            customerUserProperty.setPropertyName(seatMinute);
            customerUserProperty.setPropertyValue(String.valueOf(custSeatMinute));
            customerUserProperty.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
            customerUserDao.saveOrUpdate(customerUserProperty);
            //保存坐席的供应商分钟
            CustomerUserProperty customerSupUserProperty = new CustomerUserProperty();
            customerSupUserProperty.setUserId(String.valueOf(userId));
            customerSupUserProperty.setPropertyName(resourceId + "_minute");
            customerSupUserProperty.setPropertyValue(String.valueOf(supSeatMinute));
            customerSupUserProperty.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
            customerUserDao.saveOrUpdate(customerSupUserProperty);

        } catch (Exception e) {
            logger.error("单独添加坐席异常" + e);
        }
        resultMap.put("result", "1");
        resultMap.put("_message", "坐席添加成功");
        return resultMap;
    }


    /**
     * 获取坐席剩余包月分钟数
     *
     * @param userId
     * @return
     * @throws Exception
     */
    public int getSeatSurplusMinute(String userId) {
        String sql = "SELECT property_value FROM t_customer_user_property WHERE user_id = ? AND property_name = ?";
        List<Map<String, Object>> seatSurplusMinuteList = customerDao.sqlQuery(sql, userId, SupplierEnum.CUC.getSeatMinute());
        if (seatSurplusMinuteList != null && seatSurplusMinuteList.size() > 0 && StringUtil.isNotEmpty(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")))) {
            return Integer.parseInt(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")));
        }
        return 0;
    }


    public double getSeatPrice(String supplierId, String custId) throws Exception {
        MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(supplierId, ResourceEnum.CALL.getType());
        double custSeatsPrice = 0.0;
        if (marketResourceEntity != null) {
            String resourceId = String.valueOf(marketResourceEntity.getResourceId());
            logger.info("查询坐席价格的资源id是：" + resourceId);
            //根据资源id和供应商id查询出销售定价
            JSONObject customerMarketResource = marketResourceServiceImpl.getCustomerMarketResource(custId, resourceId);
            if (customerMarketResource != null) {
                //custSeatsPrice = customerMarketResource.getDoubleValue(ResourceEnum.CALL.getSeatPrice());
                logger.warn("未配置坐席价格");
            }
        }
        return custSeatsPrice;
    }
}
