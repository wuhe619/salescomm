package com.bdaim.slxf.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.METHOD,
	java.lang.annotation.ElementType.TYPE  })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoggerAnnotation {
	String description() default "";
}
