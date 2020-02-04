package com.bdaim.crm.common.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface NotNullValidates {
    NotNullValidate[] value();
}
