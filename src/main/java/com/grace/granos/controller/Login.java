package com.grace.granos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.grace.granos.model.StaffModel;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class Login {
	private static final Logger logger = LoggerFactory.getLogger(Login.class);

	@RequestMapping("/index")
	public String index(Model model,HttpServletRequest request) throws Exception {
		model.addAttribute("queryString", request.getQueryString());
		return "index";
	}
	
	public StaffModel login() {
		return null;
		
	}
}