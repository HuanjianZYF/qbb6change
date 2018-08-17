package com.btime.webser.qbb6change;

import com.btime.webser.qbb6change.annotation.Qbb6Change;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class Qbb6ChangeParser {
	
	@Autowired
	private Qbb6ParserConfig parserConfig;
	
	/**
	 * Qbb6Change的注解解析
	 */
	@Around("@annotation(qbb6Change)")
	public Object doParserQbb6Change(ProceedingJoinPoint pjp, Qbb6Change qbb6Change) throws Throwable {
		
		//先执行原方法
		Object object = pjp.proceed();
		
		//拿到config
		try {
			String config = parserConfig.getParserConfig();
			if (StringUtils.isEmpty(config)) {
				return object;
			}
			
			String[] clazzAndProperties = config.split(Qbb6ParserConstants.CLASS_OUTER_SEPARATE); //得到类型-属性-属性的字符串
			for (String clazzAndPropertyStr : clazzAndProperties) {
				if (StringUtils.isEmpty(clazzAndPropertyStr)) { //被分号分隔的为空
					continue;
				}
				String[] properties = clazzAndPropertyStr.split(Qbb6ParserConstants.CLASS_INNER_SEPARATE); //得到属性列表，第一个是类名
				
				//解析传过来的第一个类名
				Class<?> clazz = null;
				try {
					clazz = object.getClass();
				} catch (Exception e) {
				}
				if (clazz == null) { //找不到这个类
					continue;
				}
				
				if (!clazz.getName().equals(properties[0])) { //需要先找到类名一样的
					continue;
				}
				if (properties.length < 2) { //正常不可能小于2
					continue;
				}

				//获取真正拥有qbb6url的那个对象，并替换掉它的那个属性
				getQbb6UrlOwnerAndResetUrl(properties, object, 1);
				
				continue; //可能同一个类会有qbb6Url
			}
		} catch (Exception e) {
			//保证不影响原方法
		}
				
		return object;
	}

	/**
	 * 当我们拿到一个对象，还拿到一连串的属性的时候，通过反射可以找到最后的那个属性，并改变它的值
	 * @param properties 一连串的属性
	 * @param object 一个对象
	 * @param level 由于属性可以为容器，用level表示第几层               
	 */
	private void getQbb6UrlOwnerAndResetUrl(String[] properties, Object object, int level) throws Exception {

		if (level == properties.length - 1) { //到了最后一层,递归出口
			Method getter = object.getClass().getDeclaredMethod(ReflectUtil.getGetterMethodName(properties[level]));
			Object value = getter.invoke(object);

			if (value == null || value.getClass() != String.class) { //正常情况下最后是String
				return;
			}

			//得到新的qbb6并设置
			String newQbb6 = doTransferQbb6((String) value);
			doSetterNewQbb6(object, properties[level], newQbb6);
			return;
		}
		
		//非出口，去下一层
		if (object instanceof List) { //如果是List,level不需要加1
			List<Object> list = (List<Object>) object;
			for (Iterator<Object> iterator = list.iterator(); iterator.hasNext();) {
				Object element = iterator.next();
				getQbb6UrlOwnerAndResetUrl(properties, element, level);
			}
		}

		if (object instanceof Map) { //如果是Map
			Map<Object, Object> map = (Map<Object, Object>) object;
			for (Map.Entry<Object, Object> entry : map.entrySet()) {
				Object element = entry.getValue();
				getQbb6UrlOwnerAndResetUrl(properties, element, level);
			}
		}
		
		Method getter = object.getClass().getDeclaredMethod(ReflectUtil.getGetterMethodName(properties[level]));
		Object value = getter.invoke(object);
		if (value == null) {
			return;
		}
		
		//正常的类
		getQbb6UrlOwnerAndResetUrl(properties, value, level + 1);
	}
	
	private void doSetterNewQbb6(Object object, String param, String url) {
		Class<?> owner = object.getClass();
		try {
			Method setter = owner.getDeclaredMethod(ReflectUtil.getSetterMethodName(param), String.class);
			setter.invoke(object, url);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 转换Qbb6Url,这里只能针对每一个case做转换
	 */
	private String doTransferQbb6(String qbb6Url) {
		String result = qbb6Url;
		
		if (StringUtils.isEmpty(qbb6Url) || !qbb6Url.startsWith(Qbb6ParserConstants.QBB6_PREFIX)) {
			return result;
		}
		int indexWenhao = qbb6Url.indexOf("?");
		if (indexWenhao < 0) { //没找到问号
			return result;
		}
		
		String parameterStr = qbb6Url.substring(indexWenhao + 1, qbb6Url.length());
		if (StringUtils.isEmpty(parameterStr)) { //没有参数
			return result;
		}
		
		//组装所有的参数
		Map<String, String> paramMap = getParamMap(parameterStr);
		if (!"mall".equals(paramMap.get("module")) || !"area".equals(paramMap.get("sub_module"))) { //不是商品集qbb6
			return result;
		}
		
		//得到rnUrl
		StringBuilder rnParams = new StringBuilder();
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			if (!"module".equals(entry.getKey()) && !"sub_module".equals(entry.getValue())) {
				rnParams.append("&");
				rnParams.append(entry.getKey());
				rnParams.append("=");
				rnParams.append(entry.getValue());
			}
		}
		String rnUrl =  Qbb6ParserConstants.RN_QBB6_PREFIX + rnParams.toString();
		
		result = qbb6Url + "&rn=" + URLEncoder.encode(rnUrl);
		return result;
	}
	
	private Map<String, String> getParamMap(String parameterStr) {
		Map<String, String> paramMap = new HashMap<>();

		String[] parameters = parameterStr.split("&"); //获取所有的参数
		for (String parameter : parameters) {
			String[] keyAndValue = parameter.split("=");
			if (keyAndValue.length != 2) {
				continue;
			}

			paramMap.put(keyAndValue[0].trim(), keyAndValue[1].trim());
		}
		
		return paramMap;
	}

}
