package com.grace.granos.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.grace.granos.model.User;
import com.grace.granos.service.StaffService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private StaffService staffService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI().replaceAll("/", "");
        String queryString = request.getQueryString();
        User user=staffService.getUser(request);
        if (user == null) {
            // 用户未登录，重定向到登录页面
        	String url=requestURI;
        	if(StringUtils.isNotEmpty(queryString)) {
        		url=url+"&"+queryString;
        	}
            response.sendRedirect("/index?go="+url);
            return false; // 返回 false 表示拦截请求，不继续执行后续操作
        }

        return true; // 返回 true 表示放行请求，继续执行后续操作
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        // 在处理请求后执行的操作，可以对 modelAndView 进行处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 在请求完成后执行的操作
    }
}