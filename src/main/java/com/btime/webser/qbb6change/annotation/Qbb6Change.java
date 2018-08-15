package com.btime.webser.qbb6change.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于对返回的qbb6url做处理
 * 设想可以在config中加一个json，它是一个Map，map的key是project的name value是一个字符串
 * 字符串用";"分割，对应很多种的返回结果，把返回结果再用"."分割，对应返回结果的层次
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Qbb6Change {
	String prjName();
}
