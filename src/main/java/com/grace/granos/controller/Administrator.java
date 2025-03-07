package com.grace.granos.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.grace.granos.model.User;
import com.grace.granos.service.PayrollService;
import com.grace.granos.service.StaffService;

import jakarta.servlet.http.HttpServletRequest;
@Controller
public class Administrator {
	@Autowired
	private StaffService staffService;
	private static final Logger logger = LoggerFactory.getLogger(Administrator.class);
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private PayrollService payrollService;
	@RequestMapping("/administrator")
	public String administrator(Model model,HttpServletRequest request) throws Exception {
		User user = staffService.getUser(request);
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		if(user.getJobId()!=1) {
			throw new Exception(messageSource.getMessage("4001", null, locale));
		}
		return "administrator";
	}
	@RequestMapping("/exportPayroll")
	public ResponseEntity<?> exportPayroll(@RequestParam("year") int year,
			@RequestParam("month") int month, HttpServletRequest request) {
		String finlename = "AllPayRoll_"+String.valueOf(year) + StringUtils.leftPad(String.valueOf(month), 2, "0");
		ByteArrayOutputStream out;
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		try {
			out = payrollService.exportAllUsersToExcel(year, month);
			if(out==null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messageSource.getMessage("3001", null, locale));
			}
			HttpHeaders header = new HttpHeaders();
			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + finlename + ".xlsx");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");
			ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

			return ResponseEntity.ok().headers(header).contentLength(out.size())
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(resource);
		} catch (IOException e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messageSource.getMessage("3001", null, locale));
		}
	}
}
