package com.bdaim.common;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 访问路径忽略到权限验证
 *
 * @description:
 * @auther: Chacker
 * @date: 2019/8/9 01:24
 */
@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 10)
public class IgnoreAuthConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/pdf/**");
        web.ignoring().antMatchers("/pic/**");

        web.ignoring().antMatchers("/open/getVoiceRecordFile/**", "/user/verify/code", "/customer/token",
                "/user/token", "/customer/m/login", "/user/otp/verify/**", "/user/identify/**",
                "/user/reset/password", "/mail/sendVerifyCode", "/sms/sendSms",
                "/returnUrl/**", "/retuNotifyUrl/**", "/marketResource/callBack/**",
                "/registerUser/validationPhone", "/registerUser/saveNew", "/registerUser/validationUserName",
                "/mail/sendEmailForAsk", "/customgroup/taskphone/**", "/customgroup/taskPhoneTest/**",
                "/customgroup/customPhoneList/**", "/customgroup/xfTaskPhones/**", "/customgroup/xzTaskGetPhone",
                "/sms/ytxCallbackSms/**", "/sms/getSmsStatus", "/marketResource/getVoice/**",
                "/api/sales/**", "/api/open/**", "/marketTask/xzTaskHandle",
                "/dic/get/**", "/dic/page/query", "/institution/getInstitutionInfo",
                "/institution/getInstitutionList", "/operlog/saveUserOperateLog", "/operlog/pageUserOperateLog",
                "/dic/getApplyList", "/dic/getApplyDetail", "/dic/queryType", "/institution/applyRank",
                "/dic/getLoanCost", "/dic/productApply", "/dic/listShowAdSpace", "/dic/getBrandList",
                "/customerSea/xzCustomerSeaGetPhone", "/upload/pic/**", "/marketResource/getVoice0/**",
                "/custuser/bindUserOpenId", "/open/customs/terminal/check/**", "/open/phone/xzGetTaskPhone", "/open/phone/xzGetTaskPhone0",
                "/open/unicom/callBack", "/open/unicom/recordCallBack", "/open/unicom/recordCallBack0", "/customs/pageDic",
                "/wechat/testAuto", "/transfer/**", "/open/sms/uploadinfo", "/customer/regist0", "/supplier/pageShowExtension",
                "/packages/getAliPayResult", "/auth/checkout", "/ent/**/search","/system/user/resetPasswordByPhone",
                "/crm/login","/open/entdata/*","/sec_auth/checkin/*","/sec_auth/sendSms/*");
    }
}
