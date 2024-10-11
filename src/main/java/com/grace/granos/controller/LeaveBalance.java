package com.grace.granos.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.grace.granos.model.BalanceDataTableModel;
import com.grace.granos.model.CustomException;
import com.grace.granos.model.JsonResponse;
import com.grace.granos.model.LeaveBalanceModel;
import com.grace.granos.model.User;
import com.grace.granos.service.LeaveBalanceService;
import com.grace.granos.service.StaffService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LeaveBalance {
	private static final Logger logger = LoggerFactory.getLogger(LeaveBalance.class);
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private StaffService staffService;
	@Autowired
	private LeaveBalanceService leaveBalanceService;
	
	@RequestMapping("/balances")
	public String balances(Model model) {
		return "balances";
	}
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/getBalances")
	public ResponseEntity<JsonResponse> getBalnaces(@RequestParam("year") int year, @RequestParam("month") int month,
			HttpServletRequest request) {
		logger.info("Controller:getBalances[" + year + "]" + "[" + month + "]");
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		User user = staffService.getUser(request);
		List<LeaveBalanceModel> lbtms = leaveBalanceService.getLeaveBalances(year, month,
				user.getCharacter().getEmpId());
		List<BalanceDataTableModel> lbts=new ArrayList<BalanceDataTableModel>();
		JsonResponse rs = new JsonResponse(lbts);
		if (!CollectionUtils.isEmpty(lbtms)) {
			lbts=LeaveBalanceModelToDataTable(lbtms,locale);
			ResponseEntity.ok(new JsonResponse(lbts));
		} else {
			try {
				lbtms = leaveBalanceService.calculateBalances(year, month, user);
				lbts=LeaveBalanceModelToDataTable(lbtms,locale);
			} catch (CustomException e) {
				rs.setStatus(e.getStatus());
				rs.setMessage(messageSource.getMessage(String.valueOf(e.getMessage()), null, locale));
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
			}
		}
		return ResponseEntity.ok(new JsonResponse(lbts));
	}

	@RequestMapping("/downloadBalances")
	public ResponseEntity<?> exportRequestsExcel(@RequestParam("year") int year, @RequestParam("month") int month,HttpServletRequest request) {
		User user = staffService.getUser(request);
		String finlename = String.valueOf(year) + StringUtils.leftPad(String.valueOf(month), 2, "0")
				+ StringUtils.leftPad(String.valueOf(user.getCharacter().getEmpId()), 4, "0");
		ByteArrayOutputStream out;
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		try {
			out = leaveBalanceService.exportRequestToExcel(year, month, user.getCharacter().getEmpId());
			if (out == null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(messageSource.getMessage("3001", null, locale));
			}
			HttpHeaders header = new HttpHeaders();
			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balances_" + finlename + ".xlsx");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");
			ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

			return ResponseEntity.ok().headers(header).contentLength(out.size())
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(resource);
		} catch (IOException e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(messageSource.getMessage("3001", null, locale));
		}
	}
	public List<BalanceDataTableModel> LeaveBalanceModelToDataTable(List<LeaveBalanceModel> lbs,Locale locale) {
		List<BalanceDataTableModel> leaveBalanceDataTable = new ArrayList<BalanceDataTableModel>();
		String[] monStr = { "Jan", "Feb", "Wed", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		for (LeaveBalanceModel lb : lbs) {
			BalanceDataTableModel lbt = new BalanceDataTableModel();
			leaveBalanceDataTable.add(lbt);
			lbt.setYear(lb.getYear());
			lbt.setEmpId(lb.getEmpId());
			lbt.setMonth(monStr[lb.getMonth() - 1]);
			lbt.setShift(lb.getShift());
			lbt.setUsedHours(lb.getUsedHours());
			lbt.setRemainingHours(lb.getRemainingHours());
			String lbDescription=messageSource.getMessage("leave."+lbt.getShift(), null,locale);
			if(StringUtils.isNotBlank(lbDescription)) {
				lbt.setTitle(lbDescription);
			}else {
				lbt.setTitle(lb.getDescription());
			}
		}
		return leaveBalanceDataTable;
	}
}
