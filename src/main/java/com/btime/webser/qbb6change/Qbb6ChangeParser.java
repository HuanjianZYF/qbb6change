package com.btime.webser.qbb6change;

import com.btime.webser.qbb6change.annotation.Qbb6Change;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class Qbb6ChangeParser {

	/**
	 * Qbb6Change的注解解析
	 */
	@Around("@annotation(qbb6Change)")
	public Object doParserQbb6Change(ProceedingJoinPoint pjp, Qbb6Change qbb6Change) throws Throwable {
		Object object = pjp.proceed();
		String prjName = qbb6Change.prjName();
		System.out.print(object + " " + prjName);
		return object;
	}

}
