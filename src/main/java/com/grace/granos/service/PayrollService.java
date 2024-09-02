package com.grace.granos.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grace.granos.dao.AttendanceRepository;
import com.grace.granos.dao.PayCodeRepository;
import com.grace.granos.dao.PayrollRepository;
import com.grace.granos.dao.ShiftRepository;
import com.grace.granos.model.AttendanceModel;
import com.grace.granos.model.DayCodeModel;
import com.grace.granos.model.PayCodeModel;
import com.grace.granos.model.PayrollDataTableModel;
import com.grace.granos.model.PayrollModel;
import com.grace.granos.model.ShiftModel;

import io.micrometer.common.util.StringUtils;

@Service
public class PayrollService {
	private static final Logger logger = LoggerFactory.getLogger(PayrollService.class);
	@Autowired
	private PayCodeRepository payCodeRepository;
	@Autowired
	private PayrollRepository payrollRepository;
	@Autowired
	private AttendanceRepository attendanceRepository;
	public List<PayrollModel> calculatePayroll(int year, int month, int empid) {
		logger.info("Service:calculatePayroll[" + year + "/"+month+"]");
		List<PayrollModel> payrolls = new ArrayList<>();
		AttendanceModel attendanceModel=new AttendanceModel();
		attendanceModel.setEmpId(empid);
		attendanceModel.setYear(year);
		attendanceModel.setMonth(month);
		List<AttendanceModel> atts=attendanceRepository.findAttendanceForPayByUserMon(attendanceModel);
		if(CollectionUtils.isEmpty(atts)) {
			return payrolls;
		}
		List<DayCodeModel> dayCodes= payCodeRepository.findPayCodeByNow();
		for(DayCodeModel dc:dayCodes) {
			List<AttendanceModel> filAttsByDc=atts.stream().filter(x->dc.getDayCode()==x.getDayCode()).collect(Collectors.toList());
			for(PayCodeModel pc:dc.getPayCode()){
				List<AttendanceModel> filAttsByShift=filAttsByDc.stream().filter(x->pc.getShift().equals(x.getShift())).collect(Collectors.toList());
				for(AttendanceModel att:filAttsByShift) {
					PayrollModel pl=new PayrollModel();
					pl.setEmpId(att.getEmpId());
					pl.setDay(att.getDay());
					pl.setMonth(att.getMonth());
					pl.setPayCode(pc.getId());
					pl.setYear(att.getYear());
					pl.setTitle(pc.getTitle());
					pl.setCoefficient(pc.getCoefficient());
					float s=Float.parseFloat(pc.getHourPartGreater());
					float workHours=att.getWorkHours();
					if(att.getPaidLeave()>0) {
						workHours=att.getPaidLeave();
					}
					if(workHours>s) {
						float e=workHours;
						if(StringUtils.isNotEmpty(pc.getHourPartLess())&&workHours>Float.parseFloat(pc.getHourPartLess())) {
								e=Float.parseFloat(pc.getHourPartLess());
						}
						pl.setHours(e-s);
						float taxFreeOverTime=e-s;
						if(pc.getCoefficient()==1 && (pc.getDayCode()==1 || pc.getDayCode()==5)) {
							taxFreeOverTime=0;
						}else {
							if(att.getRemainTaxFree()<0) {
						        // Sum the salaries where the condition is met
						        float totalRemainTaxFree = (float) payrolls.stream()
						            .filter(x->x.getDay()==att.getDay()&&x.getYear()==att.getYear()&&x.getMonth()==att.getMonth() ) // condition: only include if overtime is true
						            .mapToDouble(PayrollModel::getTaxFreeHours)
						            .sum();
								if(taxFreeOverTime+(att.getRemainTaxFree()+totalRemainTaxFree)>=0) {
									taxFreeOverTime=taxFreeOverTime+(att.getRemainTaxFree()+totalRemainTaxFree);
								}else {
									taxFreeOverTime=0;
								}
							}
						}
						pl.setTaxFreeHours(taxFreeOverTime);
						payrolls.add(pl);
					}
				}
			}
		}
		
		return payrolls;
	}
	public List<PayrollDataTableModel> PayrollModelToDataTable(List<PayrollModel> pls){
		List<PayrollDataTableModel> payrollDataTable = new ArrayList<>();
		Map<Integer, Double> payCodeMap = pls.stream()
				  .collect(Collectors.groupingBy(PayrollModel::getPayCode, Collectors.summingDouble(PayrollModel::getHours)));
		Map<Integer, Double> taxFreeMap = pls.stream()
				  .collect(Collectors.groupingBy(PayrollModel::getPayCode, Collectors.summingDouble(PayrollModel::getTaxFreeHours)));
		List<PayrollModel> distinctPayrolls = pls.stream()
                .distinct()
                .collect(Collectors.toList());
		String[] monStr = { "Jan", "Feb", "Wed", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		for(PayrollModel pl:distinctPayrolls){
			PayrollDataTableModel pld=new PayrollDataTableModel();
			payrollDataTable.add(pld);
			pld.setYear(pl.getYear());
			pld.setEmpId(pl.getEmpId());
			pld.setMonth(monStr[pl.getMonth()-1]);
			pld.setPayCode(pl.getPayCode());
			pld.setHours(payCodeMap.get(pl.getPayCode()).floatValue());
			pld.setTaxFreeOverTime(taxFreeMap.get(pl.getPayCode()).floatValue());
			pld.setTitle(pl.getTitle());
			pld.setWeighted(pld.getHours()*pl.getCoefficient());
			pld.setTaxFreeOverTimeWeighted(pld.getTaxFreeOverTime()*pl.getCoefficient());
		}
		return payrollDataTable;
	}
	public List<PayrollDataTableModel> getPayroll(int year, int month, int empid) {
		logger.info("Service:getPayroll[" + year +  "/"+month+"]");
		PayrollModel payroll = new PayrollModel();
		payroll.setYear(year);
		payroll.setMonth(month);
		payroll.setEmpId(empid);
		List<PayrollModel> payrolls = null;
		List<PayrollDataTableModel> payrollDataTable = new ArrayList<>();
		try {
			payrolls = payrollRepository.findPayrollByUserMon(payroll);
			if (payrolls == null) {
				return payrollDataTable;
			}
			// 創建SimpleDateFormat對象來定義日期格式
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置时区为 UTC
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
			timeFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置时区为 UTC
			String[] monStr = { "Jan", "Feb", "Wed", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
			for (PayrollModel pay : payrolls) {
				PayrollDataTableModel payTable = new PayrollDataTableModel();
				payrollDataTable.add(payTable);
				payTable.setEmpId(pay.getEmpId());
				payTable.setYear(pay.getYear());
				payTable.setMonth(monStr[pay.getMonth() - 1]);
				payTable.setHours(pay.getHours());
				payTable.setTitle(pay.getTitle());
				payTable.setWeighted(pay.getHours()*pay.getCoefficient());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return payrollDataTable;
	}
}
