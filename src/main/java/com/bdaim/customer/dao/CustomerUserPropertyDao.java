package com.bdaim.customer.dao;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.SeatInfoDto;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUserPropertyDO;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @author duanliying
 * @date 2019/4/9
 * @description
 */
@Component
public class CustomerUserPropertyDao extends SimpleHibernateDao<CustomerUserPropertyDO, Integer> {
    /**
     * 查询坐席信息
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/9 11:17
     */
    public SeatInfoDto getSeatMessageById(String resourceId, String userId, String supplierId) throws Exception {
        SeatInfoDto seatInfoDto = null;
        String propertyName = null;
        if ("1".equals(supplierId)) {
            propertyName = "xz";
        } else if ("2".equals(supplierId)) {
            propertyName = "cuc";
        } else if ("3".equals(supplierId)) {
            propertyName = "cmc";
        } else if ("4".equals(supplierId)) {
            propertyName = "ctc";
        }
        String hql = "from CustomerUserPropertyDO m where m.userId=? AND m.propertyName = ?";
        //查询坐席基本信息
        List<CustomerUserPropertyDO> list = this.find(hql, userId, propertyName + "_seat");
        if (list.size() > 0) {
            CustomerUserPropertyDO cp = (CustomerUserPropertyDO) list.get(0);
            if (StringUtil.isNotEmpty(cp.getPropertyValue())) {
                seatInfoDto = JSONObject.parseObject(cp.getPropertyValue(), SeatInfoDto.class);
            }
            //获取坐席分钟数(供应商)
            List<CustomerUserPropertyDO> listSupMinute = this.find(hql, userId, resourceId + "_minute");
            if (listSupMinute.size() > 0 && StringUtil.isNotEmpty(listSupMinute.get(0).getPropertyValue())) {
                int propertyValue = Integer.parseInt(listSupMinute.get(0).getPropertyValue());
                seatInfoDto.setSeatSupMinute(propertyValue);
            }
            //获取坐席分钟数(企业)
            List<CustomerUserPropertyDO> listCustMinute = this.find(hql, userId, propertyName + "_minute");
            if (listCustMinute.size() > 0 && StringUtil.isNotEmpty(listCustMinute.get(0).getPropertyValue())) {
                int propertyValue = Integer.parseInt(listCustMinute.get(0).getPropertyValue());
                seatInfoDto.setSeatCustMinute(propertyValue);
            }
        }
        logger.info("获取坐席信息" + seatInfoDto.toString() + "userid是：" + userId);
        return seatInfoDto;
    }
    public CustomerUserPropertyDO getProperty(String userId, String propertyName) {
        CustomerUserPropertyDO cp = null;
        String hql = "from CustomerUserPropertyDO m where m.userId=? and m.propertyName=?";
        List<CustomerUserPropertyDO> list = this.find(hql, userId, propertyName);
        if (list.size() > 0)
            cp = (CustomerUserPropertyDO) list.get(0);
        return cp;
    }

    /**
     * 企业属性编辑与新增
     */
    public boolean dealUserPropertyInfo(String userId, String propertyName, String propertyValue) {
        boolean success = false;
        CustomerUserPropertyDO propertyInfo = this.getProperty(userId, propertyName);
        if (propertyInfo == null) {
            propertyInfo = new CustomerUserPropertyDO();
            propertyInfo.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
            propertyInfo.setUserId(userId);
            propertyInfo.setPropertyValue(propertyValue);
            logger.info(userId + " 属性不存在，新建该属性" + "\tpropertyName:" + propertyName + "\tpropertyValue:" + propertyValue);
            propertyInfo.setPropertyName(propertyName);
            success = true;
        } else {
            if (StringUtil.isNotEmpty(propertyInfo.getPropertyValue())) {
                propertyInfo.setPropertyValue(propertyValue);
                success = true;
            }
        }
        this.saveOrUpdate(propertyInfo);
        return success;
    }
}
