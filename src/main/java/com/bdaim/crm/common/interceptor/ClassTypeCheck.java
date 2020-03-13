package com.bdaim.crm.common.interceptor;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClassTypeCheck {
    @AliasFor("value")
    public Class classType();
}
