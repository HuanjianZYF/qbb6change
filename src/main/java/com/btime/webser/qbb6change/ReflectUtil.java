package com.btime.webser.qbb6change;

/**
 * @author zyf
 * @date 2018/8/17 下午1:27
 */
public class ReflectUtil {
	
	public static String getGetterMethodName(String property) {
		String s = property.substring(0, 1);
		s = s.toUpperCase();
		return "get" + s + property.substring(1, property.length());
	}

	public static String getSetterMethodName(String property) {
		String s = property.substring(0, 1);
		s = s.toUpperCase();
		return "set" + s + property.substring(1, property.length());
	}

}
