package com.grace.granos.controller;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
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
import com.grace.granos.service.AttendanceService;
import com.grace.granos.service.PayrollService;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;

@PropertySource("classpath:application.properties") // 指定属性文件的位置
@Controller
public class Payroll {
	private static final Logger logger = LoggerFactory.getLogger(Payroll.class);
    @Autowired
    private MessageSource messageSource;
    @Value("${temp.folder.attendance}") // 从属性文件中注入 temp 文件夹路径的值
    private String tempFolderPath;
	@Autowired
	private PayrollService payrollService;
	@Autowired
	private AttendanceService attendanceService;
	@RequestMapping("/payroll")
	public String payroll(Model model){
		return "payroll";
	}
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping("/getPayroll")
	public ResponseEntity<JsonResponse> getPayroll(@RequestParam("year") int year,@RequestParam("month") int month,@RequestParam("user") int empid,HttpServletRequest request){
    	logger.info("Controller:getPayroll[" + year + "]");
    	List<PayrollDataTableModel> pay = payrollService.getPayroll(year, month, empid);
    	Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
    	JsonResponse rs=new JsonResponse(pay);
    	if(pay.size()>0) {
    		ResponseEntity.ok(new JsonResponse(pay));
    	}else {
    		List<AttendanceModel> atts;
			try {
				atts = attendanceService.checkAttendanceForPayByUserMon(year, month, empid);
			} catch (Exception e) {
				logger.error(e.getMessage());
				rs.setStatus(3001);
				rs.setMessage(messageSource.getMessage("3001", null, locale));
            	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
			}
    		if(!CollectionUtils.isEmpty(atts)) {
    			rs.setStatus(3002);
    			rs.setMessage(messageSource.getMessage("3002", null, locale));
    			return ResponseEntity.ok(rs);
    		}else {
    	    	List<PayrollModel> pm = payrollService.calculatePayroll(year, month, empid);
    	    	pay=payrollService.PayrollModelToDataTable(pm);
    		}
    	}
		return ResponseEntity.ok(new JsonResponse(pay));
	}
    @RequestMapping("/getPayrollConfirmed")
	public ResponseEntity<JsonResponse> getPayrollConfirmed(@RequestParam("year") int year,@RequestParam("month") int month,@RequestParam("user") int empid){
    	List<PayrollModel> pm = payrollService.calculatePayroll(year, month, empid);
    	List<PayrollDataTableModel> pay=payrollService.PayrollModelToDataTable(pm);
		return ResponseEntity.ok(new JsonResponse(pay));
	}
}