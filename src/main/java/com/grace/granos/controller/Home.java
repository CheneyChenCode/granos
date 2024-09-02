package com.grace.granos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.grace.granos.service.StaffService;

@Controller
public class Home {
	@Autowired
	StaffService staffService;
	@RequestMapping("/home")
	public String home(Model model){
		
		return "home";    
	}
}