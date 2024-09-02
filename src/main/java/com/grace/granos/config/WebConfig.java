package com.grace.granos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	private UserInterceptor userInterceptor;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	/**
	 * 註冊locale解析器bean
	 */
	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver localeResolver = new CookieLocaleResolver();
		localeResolver.setDefaultLocale(Locale.US);
		return localeResolver;
	}

	/**
	 * 註冊locale攔截器bean
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang"); // use request param "lang" to change locale setting
		return localeChangeInterceptor;
	}

	/**
	 * 註冊locale截器
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
		registry.addInterceptor(userInterceptor).addPathPatterns("/calendar");
		registry.addInterceptor(userInterceptor).addPathPatterns("/home");
		registry.addInterceptor(userInterceptor).addPathPatterns("/attendance");
		registry.addInterceptor(userInterceptor).addPathPatterns("/payroll");
		registry.addInterceptor(userInterceptor).addPathPatterns("/getAttendance");
		registry.addInterceptor(userInterceptor).addPathPatterns("/getTwCalendar");
	}

}
