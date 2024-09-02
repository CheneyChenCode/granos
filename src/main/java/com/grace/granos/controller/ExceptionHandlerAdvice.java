package com.grace.granos.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ExceptionHandlerAdvice {
	@ExceptionHandler(value = Exception.class)
	public ModelAndView exception(Exception ex,WebRequest request){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("errorMessage",ex.getMessage());
		modelAndView.addObject("stackTrace",ex.getStackTrace());
		modelAndView.setViewName("error");
		return modelAndView;
	}
//	//如果我們要讓所有的@RequestMapping擁有此鍵值
//	@ModelAttribute
//	public void addAttribute(Model md){
//		md.addAttribute("message","你可以設定一些錯誤訊息");
//	}	
}
