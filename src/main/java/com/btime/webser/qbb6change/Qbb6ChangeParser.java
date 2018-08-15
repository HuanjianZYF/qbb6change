package com.btime.webser.qbb6change;

import com.btime.webser.qbb6change.annotation.Qbb6Change;
import com.btime.webser.qbb6change.entity.ObjectClassProperty;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

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
				getQbb6UrlOwnerAndResetUrl(clazz, properties, object);
				
				break; //找到一个类名就可以break
			}
		} catch (Exception e) {
			//保证不影响原方法
		}
				
		return object;
	}
	
	private void getQbb6UrlOwnerAndResetUrl(Class<?> clazz, String[] properties, Object object) {
		
		//填充第一层对象
		ObjectClassProperty objectClassProperty = new ObjectClassProperty();
		objectClassProperty.setObject(object);
		objectClassProperty.setClazz(clazz);

		//i>length表示config里面的属性序列已经穷尽了，class是String表示已经找到Qbb6url
		int i = 1;
		while (objectClassProperty.getClazz() != null && objectClassProperty.getClazz() != String.class && i < properties.length) {
			objectClassProperty.setProperty(properties[i]);
			objectClassProperty = parseObject(objectClassProperty);
			i++;
		}
		
		if (objectClassProperty.getClazz() != String.class) { //没有找到
			return;
		}
		
		String qbb6Url = (String) objectClassProperty.getObject();
		String url = doTransferQbb6(qbb6Url); //转换后的qbb6url
		
		//拿到上一个对象，调用它的set方法
		ObjectClassProperty previous = objectClassProperty.getPrevious();
		doSetterNewQbb6(previous, url);
	}
	
	private void doSetterNewQbb6(ObjectClassProperty previous, String url) {
		Class<?> owner = previous.getClazz();
		try {
			Method setter = owner.getDeclaredMethod(getSetterMethodName(previous.getProperty()), String.class);
			setter.invoke(previous.getObject(), url);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 该方法参数是一个对象，该对象的类，该对象的一个属性名
	 * 返回那个对象的属性的对象，那个对象的属性的对象的类
	 * 如果找不到那个属性 不给结果的clazz赋值，就可以退出了 *在该方法里面无需对里面属性判空*
	 */
	private ObjectClassProperty parseObject(ObjectClassProperty param) {
		ObjectClassProperty result = new ObjectClassProperty();
		try {
			Class<?> clazz = param.getClazz();
			Object obj = param.getObject();
			String property = param.getProperty();
			
			//调用传进来对象的getter方法
			Method getter = clazz.getDeclaredMethod(getGetterMethodName(property));
			Object resultObject = getter.invoke(obj);
			
			result.setPrevious(param);
			result.setObject(resultObject);
			result.setClazz(resultObject.getClass());
		} catch (Exception e) {
		}
		
		return result;
	}
	
	private String getGetterMethodName(String property) {
		String s = property.substring(0, 1);
		s = s.toUpperCase();
		return "get" + s + property.substring(1, property.length());
	}
	
	private String getSetterMethodName(String property) {
		String s = property.substring(0, 1);
		s = s.toUpperCase();
		return "set" + s + property.substring(1, property.length());
	}

	/**
	 * 转换Qbb6Url
	 */
	private String doTransferQbb6(String qbb6Url) {
		String result = qbb6Url;

		result = "************zyyyyyyyyyyffffffffff";
		return result;
	}

}
