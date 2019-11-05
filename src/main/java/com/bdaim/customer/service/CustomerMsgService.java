package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.bdaim.customer.dao.CustomerMsgRepository;
import com.bdaim.customer.dto.*;
import com.bdaim.customer.entity.CustomerMsg;
import com.bdaim.express.dto.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerMsgService {
    @Autowired
    private CustomerMsgRepository customerMsgRepository;
    @Resource
    private JdbcTemplate jdbcTemplate;

    public ContentPage getCustomerMsgList(Integer pageNum, Integer pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Pageable pageable = new PageRequest(pageNum - 1, pageSize, sort);
        String sql = "select m.id as id ,m.cust_id as custId,m.cust_user_id as custUserId,m.create_time as createTime,m.msg_type as msgType," +
                "t.enterprise_name as custName from h_customer_msg m left join t_customer t on m.cust_id=t.cust_id order by m.create_time asc limit "+(pageNum-1)*pageSize+","+pageSize;
        List<ContentData> list = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(ContentData.class));
        Map<String,Object> map=new HashMap<>();
        long count = customerMsgRepository.count();
        ContentPage contentPage=new ContentPage();
        contentPage.setCount(count);
        contentPage.setCountList(list.stream().map(entity -> new ContentDTO(entity)).collect(Collectors.toList()));
        contentPage.setPageNum(pageNum);
        contentPage.setPageSize(pageSize);
        return contentPage;
    }

    public Content getCustomerMsgById(int id) {
        CustomerMsg customerMsg = customerMsgRepository.findById(id);
        String contentStr = customerMsg.getContent();
        Content content = JSON.parseObject(contentStr, Content.class);
        return content;
    }

}
