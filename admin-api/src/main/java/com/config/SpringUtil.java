package com.config;

import org.springframework.context.ApplicationContext;

public class SpringUtil {
	
	public static ApplicationContext context ;

	public static void init(ApplicationContext ctx){
		context = ctx;
	}

	public static String getStringValue(String key) {
		return context.getEnvironment().getProperty(key);
	}
	
	public static<T> T getBean(Class<T> c){
		return (T)context.getBean(c) ;
	}
	
	public static<T> T getBean(String key) {
		return (T)context.getBean(key) ;
	}

}