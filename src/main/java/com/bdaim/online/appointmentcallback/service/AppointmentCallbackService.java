package com.bdaim.online.appointmentcallback.service;

import com.bdaim.online.appointmentcallback.dao.AppointmentCallbackDao;
import com.bdaim.online.appointmentcallback.dto.AppointmentCallbackDTO;
import com.bdaim.online.appointmentcallback.dto.AppointmentCallbackQueryParam;
import com.bdaim.online.appointmentcallback.entity.AppointmentCallback;
import com.bdaim.online.appointmentcallback.vo.AppointmentCallbackVO;
import com.bdaim.common.dto.Page;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.util.ConstantsUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/12
 * @description
 */
@Service("appointmentCallbackService")
@Transactional
public class AppointmentCallbackService {

    private static Logger logger = Logger.getLogger(AppointmentCallbackService.class);

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AppointmentCallbackDao appointmentCallbackDao;
    @Resource
    private CustomGroupDao customGroupDao;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private MarketTaskDao marketTaskDao;

    /**
     * 更新预约回拨状态
     *
     * @param appointmentCallbackId
     * @param status
     * @return
     */
    public int updateCallbackState(String appointmentCallbackId, int status) {
        AppointmentCallback m = appointmentCallbackDao.get(NumberConvertUtil.parseInt(appointmentCallbackId));
        if (m != null) {
            m.setStatus(status);
            try {
                appointmentCallbackDao.saveOrUpdate(m);
                return 1;
            } catch (Exception e) {
                logger.error("更新预约回拨失败,appointmentCallbackId:" + appointmentCallbackId + ",status:" + status);
                return 0;
            }
        }
        return 0;
    }

    /**
     * 预约回拨保存
     *
     * @param model
     * @return int
     * @author chengning@salescomm.net
     * @date 2019/2/25 9:11
     */
    public int save(AppointmentCallbackVO model) {
        int code = 0;
        AppointmentCallback m = new AppointmentCallback();
        m.setCustId(model.getCustId());
        m.setSuperid(model.getSuperid());
        m.setCustomerGroupId(model.getCustomerGroupId());
        m.setBackupPhone(model.getBackupPhone());
        m.setRemark(model.getRemark());
        m.setCreateTime(new Timestamp(System.currentTimeMillis()));
        if (StringUtil.isNotEmpty(model.getAppointmentTime())) {
            m.setAppointmentTime(Timestamp.valueOf(LocalDateTime.parse(model.getAppointmentTime(), DATE_TIME_FORMATTER)));
        } else {
            m.setAppointmentTime(new Timestamp(System.currentTimeMillis()));
        }
        m.setOperator(model.getOperator());
        m.setStatus(model.getStatus());
        m.setMarketTaskId(model.getMarketTaskId());
        try {
            appointmentCallbackDao.saveOrUpdate(m);
            code = 1;
        } catch (Exception e) {
            logger.error("保存预约回拨失败,", e);
            throw e;
        }
        return code;
    }

    /**
     * 预约回拨分页列表
     *
     * @param model
     * @return com.bdaim.sale.dto.Page
     * @author chengning@salescomm.net
     * @date 2019/2/25 9:12
     */
    public Page pageList(AppointmentCallbackQueryParam model) {
        Page page;
        try {
            String userGroupId = "";
            // 组长
            if (model.getUserType().equals("2") && "1".equals(model.getUserGroupRole())) {
                userGroupId = model.getUserGroupId();
            } else if (model.getUserType().equals("2")) {
                // 普通员工
                model.setOperator(model.getUserId());
            }
            //如果登陆人是项目管理员，只能查看自己负责的项目
            List<String> cgIds = null;
            if ("3".equals(model.getUserType())) {
                cgIds = customerUserDao.listCustGroupByUserId(NumberConvertUtil.parseLong(model.getUserId()));
                if (cgIds == null || cgIds.size() == 0) {
                    return new Page();
                }
            }
            page = appointmentCallbackDao.pageSearch(model, userGroupId, cgIds);
            if (page.getData() != null) {
                AppointmentCallback m;
                AppointmentCallbackDTO dto;
                CustomGroup customGroup;
                MarketTask marketTask;
                List<AppointmentCallbackDTO> list = new ArrayList<>();

                StringBuilder sb = new StringBuilder();
                List<Map<String, Object>> superList;
                for (int i = 0; i < page.getData().size(); i++) {
                    m = (AppointmentCallback) page.getData().get(i);
                    dto = new AppointmentCallbackDTO(m);
                    // 处理客户群名称
                    if (dto != null) {
                        if (dto.getCustomerGroupId() != null) {
                            customGroup = customGroupDao.get(NumberConvertUtil.parseInt(dto.getCustomerGroupId()));
                            dto.setCustomerGroupName(customGroup != null ? customGroup.getName() : "");
                        }
                        // 处理操作人姓名
                        dto.setOperatorName(customerUserDao.getLoginName(dto.getOperator()));
                        // 处理营销任务名称
                        if (dto.getMarketTaskId() != null) {
                            marketTask = marketTaskDao.get(dto.getMarketTaskId());
                            dto.setMarketTaskName(marketTask != null ? marketTask.getName() : "");
                        }
                    }

                    // 查询客户群用户信息
                    sb.setLength(0);
                    sb.append(" SELECT custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street");
                    sb.append(" FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCustomerGroupId() + " custG ");
                    sb.append(" where custG.id = ?");
                    try {
                        superList = appointmentCallbackDao.sqlQuery(sb.toString(), dto.getSuperid());
                        if (superList.size() > 0) {
                            dto.setSuper_name(String.valueOf(superList.get(0).get("super_name")));
                            dto.setSuper_age(String.valueOf(superList.get(0).get("super_age")));
                            dto.setSuper_sex(String.valueOf(superList.get(0).get("super_sex")));
                            dto.setSuper_telphone(String.valueOf(superList.get(0).get("super_telphone")));
                            dto.setSuper_phone(String.valueOf(superList.get(0).get("super_phone")));
                            dto.setSuper_address_province_city(String.valueOf(superList.get(0).get("super_address_province_city")));
                            dto.setSuper_address_street(String.valueOf(superList.get(0).get("super_address_street")));
                        }
                    } catch (Exception e) {
                        logger.error("预约回拨查询客户群用户信息失败:", e);
                    }

                    list.add(dto);
                }
                page.setData(list);
            }
        } catch (Exception e) {
            logger.error("查询预约回拨分页列表失败,", e);
            throw e;
        }
        return page;
    }
}
