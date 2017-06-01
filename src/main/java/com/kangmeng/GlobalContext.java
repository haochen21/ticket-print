package com.kangmeng;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class GlobalContext implements ApplicationContextAware {

	private static ApplicationContext applicationContext = null;

	private GlobalContext() {
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		if(GlobalContext.applicationContext == null){
			GlobalContext.applicationContext = context;
		}
	}
}
