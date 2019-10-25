package com.bdaim.common.service.api;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.PhoneService;
import com.bdaim.util.LogUtil;
import com.bdaim.util.StringUtil;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

/**
 * @description 通过id获取手机号码
 */
@Service("hidephoneNumber")
@Transactional
public class HidePhoneNumberImpl {

    @Resource
    private PhoneService phoneService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public String getPhoneNumberById(HttpServletRequest request) {
        JSONObject json = new JSONObject();
        try {
            //请求id
            String actionId = request.getParameter("actionId");
            json.put("actionId", actionId);
            json.put("code", "200");
            json.put("param", "");
            json.put("number", "");
            //主键id
            String id = request.getParameter("id");
            LogUtil.info("HidePhoneNumberImpl.getPhoneNumberById: actionId=" + actionId + ";id=" + id);
            if (StringUtil.isEmpty(id)) {
                LogUtil.error("参数id为空");
                json.put("reason", "参数id不能为空");
                return json.toJSONString();
            }

            //String sql = " select * from u where id=" + id;
            String phone = phoneService.getPhoneBySuperId(id);
            //List<PhoneNumDTO> phoneNums = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PhoneNumDTO.class));
            if (StringUtil.isNotEmpty(phone)) {
                json.put("reason", "无数据");
            } else {
               /* PhoneNumDTO phoneNum = phoneNums.get(0);
                if (StringUtil.isNotEmpty(phoneNum.getPhone())) {*/
                json.put("number", phone);
                json.put("reason", "ok");
                /*} else {
                    json.put("reason", "无数据");
                }*/
            }
        } catch (Exception e) {
            json.put("reason", "查询error");
            json.put("code", "500");
            e.printStackTrace();
            LogUtil.info("查询异常:" + e);
        }
        return json.toJSONString();
    }

}




