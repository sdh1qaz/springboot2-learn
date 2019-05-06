package com.bee.sample.ch1.conf;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.bee.sample.ch1.annotation.Function;
/**
 * Spring Boot 的一个AOP实现，用于权限
 * @author 李家智
 *
 */
@Aspect
@Configuration
public class RoleAcessConfig {
	private static final Logger logger = LoggerFactory.getLogger(RoleAcessConfig.class);
	/**
	 * 所有使用Function的注解的方法，且在Controller注解标注的类里,通过权限校验允许调用，不通过不允许调用
	 * @param pjp
	 * @param function
	 * @return
	 * @throws Throwable
	 */
	@Around("within(@org.springframework.stereotype.Controller *) && @annotation(function)")
	//实现AOP的具体代码
	public Object functionAccessCheck(final ProceedingJoinPoint pjp, Function function) throws Throwable {
		if (function != null) {
			String functionName = function.value();
			if (!canAccess(functionName)) {
				MethodSignature ms = (MethodSignature) pjp.getSignature();
				throw new RuntimeException("Can not Access " + ms.getMethod());
			}
		}
		//通过了校验，可以继续调用。继续处理原有调用
		Object o = pjp.proceed();
		return o;

	}

	protected boolean canAccess(String functionName) {
		if (functionName.length() == 0) {
			return true;
		} else {
			if ("user.add".equals(functionName)) {
				return true;
			}
			// 取出当前用户对应的所有角色，从数据库查询角色是否有访问functionName的权限。
			return false;
		}
	}

	/**
	 * 所有Controller方法
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("@within(org.springframework.stereotype.Controller)")
	public Object simpleAop(final ProceedingJoinPoint pjp) throws Throwable {
		try {
			MethodSignature ms = (MethodSignature) pjp.getSignature();
			//获取方法名字
			String metName = ms.getName(); 
			Object[] args = pjp.getArgs();
			logger.info("aop开始,作用于" + metName + "方法上，入参" + "：args:" + Arrays.asList(args));
			Object o = pjp.proceed();
			logger.info("aop结束：作用于" + metName + "方法上，返回" + "return :" + o);
			return o;

		} catch (Throwable e) {
			throw e;
		}
	}
	
	/**
	 * 所有RestController方法
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("@within(org.springframework.web.bind.annotation.RestController)")
	public Object simpleAop1(final ProceedingJoinPoint pjp) throws Throwable {
		try {
			MethodSignature ms = (MethodSignature) pjp.getSignature();
			//获取方法名字
			String metName = ms.getName(); 
			Object[] args = pjp.getArgs();
			logger.info("aop开始,作用于" + metName + "方法上，入参" + "：args:" + Arrays.asList(args));
			Object o = pjp.proceed();
			logger.info("aop结束：作用于" + metName + "方法上，返回" + "return :" + o);
			return o;

		} catch (Throwable e) {
			throw e;
		}
	}

}
