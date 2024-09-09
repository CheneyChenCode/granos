package com.grace.granos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.grace.granos.service.StaffService;

@Controller
public class Home {
	private static final Logger logger = LoggerFactory.getLogger(Home.class);
	@Autowired
	StaffService staffService;
	@RequestMapping("/home")
	public String home(Model model){
		logger.info("welcome!!!");
		return "home";    
	}
}