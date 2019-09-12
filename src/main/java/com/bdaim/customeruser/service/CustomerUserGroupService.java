package com.bdaim.customeruser.service;

import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerUserGroupDTO;
import com.bdaim.customer.entity.CustomerUserGroup;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/27
 * @description
 */
@Service("customerUserGroupService")
@Transactional
public class CustomerUserGroupService {

    @Resource
    private CustomerUserDao customerUserDao;

    /**
     * 查询单个用户群组
     *
     * @param groupId
     * @return
     */
    public CustomerUserGroupDTO selectCustomerUserGroup(String groupId) {
        CustomerUserGroup customerUserGroup = customerUserDao.findCustomerUserGroup(groupId);
        CustomerUserGroupDTO dto = new CustomerUserGroupDTO(customerUserGroup);
        // 查询实际组员数量
        int userCount = customerUserDao.countSelectCustomerUserByUserGroupId(groupId, customerUserGroup.getCustId());
        dto.setUserCount(userCount);
        return dto;
    }
}
