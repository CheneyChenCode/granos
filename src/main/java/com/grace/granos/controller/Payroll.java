package com.grace.granos.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.grace.granos.model.AttendanceModel;
import com.grace.granos.model.JsonResponse;
import com.grace.granos.model.PayrollDataTableModel;
import com.grace.granos.model.PayrollModel;
import com.grace.granos.model.User;
import com.grace.granos.service.AttendanceService;
import com.grace.granos.service.PayrollService;
import com.grace.granos.service.StaffService;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Controller
public class Payroll {
	private static final Logger logger = LoggerFactory.getLogger(Payroll.class);
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private PayrollService payrollService;
	@Autowired
	private AttendanceService attendanceService;
    @Autowired
    private StaffService staffService;
	@RequestMapping("/payroll")
	public String payroll(Model model) {
		return "payroll";
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/getPayroll")
	public ResponseEntity<JsonResponse> getPayroll(@RequestParam("year") int year, @RequestParam("month") int month,HttpServletRequest request) {
		logger.info("Controller:getPayroll[" + year + "]");
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
        User user=staffService.getUser(request);
        List<PayrollModel> payRolls = payrollService.getPayroll(year, month, user.getCharacter().getEmpId());
        List<PayrollDataTableModel> pay=new ArrayList<PayrollDataTableModel>();
        JsonResponse rs = new JsonResponse(pay);
		if (!CollectionUtils.isEmpty(payRolls)) {
			pay=PayrollModelToDataTable(payRolls,locale);
			ResponseEntity.ok(new JsonResponse(pay));
		} else {
			List<AttendanceModel> atts;
			try {
				atts = attendanceService.checkAttendanceForPayByUserMon(year, month, user.getCharacter().getEmpId());
			} catch (Exception e) {
				logger.error(e.getMessage());
				rs.setStatus(3001);
				rs.setMessage(messageSource.getMessage("3001", null, locale));
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
			}
			if (!CollectionUtils.isEmpty(atts)) {
				rs.setStatus(3002);
				rs.setMessage(messageSource.getMessage("3002", null, locale));
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
			} else {
				payRolls = payrollService.calculatePayroll(year, month,user);
				pay=PayrollModelToDataTable(payRolls,locale);
			}
		}
		return ResponseEntity.ok(new JsonResponse(pay));
	}

	@RequestMapping("/downloadPayroll")
	public ResponseEntity<?> exportUsersToExcel(@RequestParam("year") int year,
			@RequestParam("month") int month, HttpServletRequest request) {
		User user=staffService.getUser(request);
		String finlename = user.getCharacter().getNameEn()+user.getCharacter().getLastNameEn()+"_PayRoll_"+String.valueOf(year) + StringUtils.leftPad(String.valueOf(month), 2, "0")
				+ StringUtils.leftPad(String.valueOf(user.getCharacter().getEmpId()), 4, "0");
		ByteArrayOutputStream out;
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		try {
			out = payrollService.exportUsersToExcel(year, month, user.getCharacter().getEmpId());
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

	public List<PayrollDataTableModel> PayrollModelToDataTable(List<PayrollModel> pls,Locale locale) {
		List<PayrollDataTableModel> payrollDataTable = new ArrayList<>();
//		Map<Integer, Double> payCodeMap = pls.stream().collect(
//				Collectors.groupingBy(PayrollModel::getPayCode, Collectors.summingDouble(PayrollModel::getHours)));
//		Map<Integer, Double> taxFreeMap = pls.stream().collect(Collectors.groupingBy(PayrollModel::getPayCode,
//				Collectors.summingDouble(PayrollModel::getTaxFreeHours)));
//		List<PayrollModel> distinctPayrolls = pls.stream().distinct().collect(Collectors.toList());
		
		String[] monStr = { "Jan", "Feb", "Wed", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		for (PayrollModel pl : pls) {
			PayrollDataTableModel pld = new PayrollDataTableModel();
			payrollDataTable.add(pld);
			pld.setYear(pl.getYear());
			pld.setEmpId(pl.getEmpId());
			pld.setMonth(monStr[pl.getMonth() - 1]);
			pld.setPayCode(pl.getPayCode());
			pld.setHours(pl.getHours());
			pld.setTaxFreeOverTime(pl.getTaxFreeHours());
			String payDescription=messageSource.getMessage("paycode."+pl.getPayCode(), null,locale);
			pld.setTitle(payDescription);
			pld.setWeighted(pld.getHours() * pl.getCoefficient());
			pld.setTaxFreeOverTimeWeighted(pld.getTaxFreeOverTime() * pl.getCoefficient());
		}
		return payrollDataTable;
	}
}