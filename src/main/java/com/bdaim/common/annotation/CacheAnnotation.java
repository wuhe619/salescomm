package com.bdaim.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAnnotation {
	String description() default "";

	// 增删改查
	public enum CacheType {
		INSERT, UPDATE, DELETE, QUERY
	}

	CacheType cacheType() default CacheType.QUERY;

	// eg:{"all","label:labelId:1000","cate"}
	String[] cacheKey() default "all";

}
