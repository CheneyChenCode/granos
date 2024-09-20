package com.grace.granos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.grace.granos.model.StaffModel;
import com.grace.granos.model.User;
import com.grace.granos.service.StaffService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class Login {
	private static final Logger logger = LoggerFactory.getLogger(Login.class);
	@Autowired
	StaffService staffService;
	@RequestMapping("/index")
	public String index(Model model,HttpServletRequest request) throws Exception {
		model.addAttribute("queryString", request.getQueryString());
        return "index";
	}
	@RequestMapping("/")
	public String hello(Model model,HttpServletRequest request) throws Exception {
		return "home";
	}
}