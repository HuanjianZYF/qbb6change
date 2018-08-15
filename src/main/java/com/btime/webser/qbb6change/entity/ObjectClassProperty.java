package com.btime.webser.qbb6change.entity;

/**
 * 用于递归解析对象的数据结构，直到找到qbb6Url
 * 通过一个对象，一个表示该对象的类，一个属性可以找到下一个对象，下一个对象的类
 * @author zyf
 * @date 2018/8/15 下午5:31
 */
public class ObjectClassProperty {
	
	private Object object; //对象
	private Class<?> clazz; //类
	private String property; //属性
	private ObjectClassProperty previous; //链表的上一个

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public ObjectClassProperty getPrevious() {
		return previous;
	}

	public void setPrevious(ObjectClassProperty previous) {
		this.previous = previous;
	}
}
