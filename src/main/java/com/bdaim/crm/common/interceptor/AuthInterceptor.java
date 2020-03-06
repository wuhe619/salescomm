package com.bdaim.crm.common.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.erp.admin.service.LkAdminRoleService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation invocation) {
        //TODO 权限功能后台拦截
        Permissions permissions=invocation.getMethod().getAnnotation(Permissions.class);
        if(permissions!=null&&permissions.value().length>0){
            JSONObject jsonObject= Aop.get(LkAdminRoleService.class).auth(BaseUtil.getUserId());
            //组装应有权限列表
            List<String> arr=queryAuth(jsonObject, "");
            boolean isRelease=false;
            for (String key : permissions.value()) {
                if(!isRelease){
                    if(arr.contains(key)){
                        isRelease=true;
                    }
                }
            }
            if(!isRelease){
                invocation.getController().renderJson(R.error("无权访问"));
                return;
            }
        }
        invocation.invoke();
    }
    @SuppressWarnings("unchecked")
    private List<String> queryAuth(Map<String, Object> map, String key){
        List<String> permissions=new ArrayList<>();
        map.keySet().forEach(str->{
            if(map.get(str) instanceof Map){
                permissions.addAll(this.queryAuth((Map<String, Object>) map.get(str),key+str+":"));
            }else {
                permissions.add(key+str);
            }
        });
        return permissions;
    }
}
