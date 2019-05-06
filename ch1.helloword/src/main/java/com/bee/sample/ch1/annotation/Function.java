package com.bee.sample.ch1.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//标识该注解的生命周期在字节码阶段仍然有效
@Retention(RetentionPolicy.RUNTIME)
public @interface Function {
	public String value() default "";
}