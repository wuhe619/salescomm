package com.bdaim.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * admin权限检查
 *
 * @author chengning@salescomm.net
 * @date 2018/12/13 15:14
 */
@Target({java.lang.annotation.ElementType.METHOD,
        java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidatePermission {
    String role();
}
