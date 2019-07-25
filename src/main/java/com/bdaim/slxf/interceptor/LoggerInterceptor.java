package com.bdaim.slxf.interceptor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggerInterceptor {
	@Pointcut("@within(com.bdaim.slxf.annotation.LoggerAnnotation)")
	private void pointCutMethod() {
	}

	@Before("pointCutMethod()")
	public void doBefore(JoinPoint jp) {
		String name = jp.getTarget().getClass().getName();
	}

	@AfterReturning(pointcut = "pointCutMethod()", returning = "result")
	public void doAfterReturn(String result) {
		System.out.println("---" + result + "---");
	}

	@AfterThrowing(pointcut = "pointCutMethod()", throwing = "e")
	public void doAfterException(Exception e) {
		System.out.println(e.getMessage());
	}

	@After("pointCutMethod()")
	public void doAfter() {
	}

	@Around("pointCutMethod()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		Object o = pjp.proceed();
		return o;
	}
}
