package com.bdaim.crm.common.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
/**
 * 系统日志注解
 */
public @interface SysLog {
    public String value() default "";
}
