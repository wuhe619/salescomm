package com.bdaim.customer.dao;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.SeatInfoDto;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.entity.CustomerUserProperty;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author duanliying
 * @date 2019/4/9
 * @description
 */
@Component
public class CustomerUserPropertyDao extends SimpleHibernateDao<CustomerUserProperty, Integer> {
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
        List<CustomerUserProperty> list = this.find(hql, userId, propertyName + "_seat");
        if (list.size() > 0) {
            CustomerUserProperty cp = (CustomerUserProperty) list.get(0);
            if (StringUtil.isNotEmpty(cp.getPropertyValue())) {
                seatInfoDto = JSONObject.parseObject(cp.getPropertyValue(), SeatInfoDto.class);
            }
            //获取坐席分钟数(供应商)
            List<CustomerUserProperty> listSupMinute = this.find(hql, userId, resourceId + "_minute");
            if (listSupMinute.size() > 0 && StringUtil.isNotEmpty(listSupMinute.get(0).getPropertyValue())) {
                int propertyValue = Integer.parseInt(listSupMinute.get(0).getPropertyValue());
                seatInfoDto.setSeatSupMinute(propertyValue);
            }
            //获取坐席分钟数(企业)
            List<CustomerUserProperty> listCustMinute = this.find(hql, userId, propertyName + "_minute");
            if (listCustMinute.size() > 0 && StringUtil.isNotEmpty(listCustMinute.get(0).getPropertyValue())) {
                int propertyValue = Integer.parseInt(listCustMinute.get(0).getPropertyValue());
                seatInfoDto.setSeatCustMinute(propertyValue);
            }
        }
        logger.info("获取坐席信息" + seatInfoDto.toString() + "userid是：" + userId);
        return seatInfoDto;
    }
}
