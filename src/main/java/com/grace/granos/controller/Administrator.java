package com.grace.granos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.grace.granos.model.User;
import com.grace.granos.service.StaffService;

import jakarta.servlet.http.HttpServletRequest;
@Controller
public class Administrator {
	@Autowired
	private StaffService staffService;
	private static final Logger logger = LoggerFactory.getLogger(Administrator.class);
	@RequestMapping("/administrator")
	public String administrator(Model model,HttpServletRequest request) throws Exception {
		User user = staffService.getUser(request);
		if(user.getJobId()!=1) {
			throw new Exception("You are not authorized to access this page.");
		}
		return "administrator";
	}
}
