package com.grace.granos.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.grace.granos.dao.LeaveBalanceRepository;
import com.grace.granos.dao.LeaveRequestRepository;
import com.grace.granos.dao.ShiftRepository;
import com.grace.granos.model.AttendanceModel;
import com.grace.granos.model.BalanceDataTableModel;
import com.grace.granos.model.LeaveBalanceModel;
import com.grace.granos.model.LeaveRequestModel;
import com.grace.granos.model.PayrollDataTableModel;
import com.grace.granos.model.PayrollModel;
import com.grace.granos.model.ShiftModel;
import com.grace.granos.model.User;



@Service
public class LeaveBalanceService {
	private static final Logger logger = LoggerFactory.getLogger(LeaveBalanceService.class);
	@Autowired
	private LeaveRequestRepository leaveRequestRepository;
	@Autowired
	private LeaveBalanceRepository leaveBalanceRepository;
	@Autowired
	private ShiftRepository shiftRepository;
	
	public List<BalanceDataTableModel> calculateBalances(int year, int month, User user) throws Exception {
		logger.info("Service:calculateBalances[" + year + "/" + month + "]");

		List<LeaveBalanceModel> lastBalance=leaveBalanceRepository.findLastLeaveBalance(user.getCharacter().getEmpId());
		if(CollectionUtils.isEmpty(lastBalance)) {
			throw new Exception("there are no Last Balances in this account");
		}
		List<ShiftModel> shifts = shiftRepository.findShift();
		Map<String, ShiftModel> shiftModelMap = shifts.stream()
				.collect(Collectors.toMap(ShiftModel::getName, shiftModel -> shiftModel));
		LeaveRequestModel lr=new LeaveRequestModel();
		lr.setEmpId(user.getCharacter().getEmpId());
		lr.setYear(lastBalance.get(0).getYear());
		lr.setMonth(lastBalance.get(0).getMonth());
		List<LeaveRequestModel> lrs=leaveRequestRepository.findLastLeaveRequest(lr);

		lastBalance = calculateBalancesPreMon(year, month, user, lastBalance, shiftModelMap, lrs);
		List<LeaveBalanceModel> newBalances=new ArrayList<LeaveBalanceModel>();
		for(LeaveBalanceModel lb:lastBalance) {
			LeaveBalanceModel newLb =new LeaveBalanceModel();
			newLb.setCreater(user.getUsername());
			newLb.setEmpId(user.getCharacter().getEmpId());
			newLb.setYear(year);
			newLb.setMonth(month);
			float shiftHours=lb.getUsedHours();
			float plusHours=lb.getRemainingHours();
			if(!CollectionUtils.isEmpty(lrs)) {
				float shiftHoursChange=(float)lrs.stream().filter(x->x.getShift().equals(lb.getShift())&&x.getHours()<0&&x.getYear()==lb.getYear()&&x.getMonth()>lb.getMonth()).mapToDouble(LeaveRequestModel::getHours).sum(); // 加總
				plusHours=plusHours+(float)lrs.stream().filter(x->x.getShift().equals(lb.getShift())&&x.getHours()>0&&x.getStatus()==1&&x.getYear()==lb.getYear()&&x.getMonth()>lb.getMonth()).mapToDouble(LeaveRequestModel::getHours).sum();
				newLb.setRemainingHours(plusHours+shiftHours);
				if(Math.abs(shiftHoursChange)>0) {
					shiftHours=shiftHours+shiftHoursChange;
					plusHours=plusHours+shiftHours;
				}
			}
			newLb.setUsedHours(shiftHours);
			newLb.setRemainingHours(plusHours);
			newLb.setShift(lb.getShift());

			ShiftModel shift=shiftModelMap.get(lb.getShift());
			if(shift!=null) {
				newLb.setDescription(shift.getDescription());
			}
			newBalances.add(newLb);
		}
		List<BalanceDataTableModel> BalanceTable=LeaveBalanceModelToDataTable(newBalances);
		return BalanceTable;
	}
	public List<LeaveRequestModel> findLastLeaveRequest(LeaveRequestModel lr) {
		List<LeaveRequestModel> lrs=leaveRequestRepository.findLastLeaveRequest(lr);
		return lrs;
	}
	public List<LeaveBalanceModel> findLastLeaveBalanceByMon(int empId,int year,int month) {
		List<LeaveBalanceModel> lastBalance=leaveBalanceRepository.findLastLeaveBalanceByMon(empId, year, month);
		return lastBalance;
	}

	public List<LeaveBalanceModel> calculateBalancesPreMon(int year, int month, User user, List<LeaveBalanceModel> lastBalance,
			Map<String, ShiftModel> shiftModelMap, List<LeaveRequestModel> lrs) {
		if(lastBalance.get(0).getYear()!=year||lastBalance.get(0).getMonth()!=month-1) {
			List<LeaveBalanceModel> preBalances=new ArrayList<LeaveBalanceModel>();
			for(LeaveBalanceModel lb:lastBalance) {
				LeaveBalanceModel preLb =new LeaveBalanceModel();
				preLb.setCreater(user.getUsername());
				preLb.setEmpId(user.getCharacter().getEmpId());
				preLb.setYear(year);
				preLb.setMonth(month-1);
				float shiftHours=lb.getUsedHours();
				float plusHours=lb.getRemainingHours();
				if(!CollectionUtils.isEmpty(lrs)) {
					float shiftHoursChange=(float)lrs.stream().filter(x->x.getShift().equals(lb.getShift())&&x.getHours()<0&&(x.getYear()>lb.getYear()||(x.getYear()==lb.getYear()&&x.getMonth()<month))).mapToDouble(LeaveRequestModel::getHours).sum();
					plusHours=plusHours+(float)lrs.stream().filter(x->x.getShift().equals(lb.getShift())&&x.getHours()>0&&x.getStatus()==1&&(x.getYear()>lb.getYear()||(x.getYear()==lb.getYear()&&x.getMonth()<month))).mapToDouble(LeaveRequestModel::getHours).sum();
					if(Math.abs(shiftHoursChange)>0) {
						shiftHours=shiftHours+shiftHoursChange;
						plusHours=plusHours+shiftHours;
					}
				}
				preLb.setShift(lb.getShift());
				preLb.setUsedHours(shiftHours);
				preLb.setRemainingHours(plusHours);
				
				ShiftModel shift=shiftModelMap.get(lb.getShift());
				if(shift!=null) {
					preLb.setDescription(shift.getDescription());
				}
				preBalances.add(preLb);
			}
			leaveBalanceRepository.addLeaveBalance(preBalances);
			lastBalance=preBalances;
		}
		return lastBalance;
	}
	
	public List<LeaveBalanceModel> findLastLeaveBalances(int empId) {
		return leaveBalanceRepository.findLastLeaveBalance(empId);
	}
	public List<BalanceDataTableModel> getLeaveBalances(int year, int month, int empId) {
		logger.info("Service:getLeaveBalances[" + year + "/" + month + "]");
		LeaveBalanceModel balance = new LeaveBalanceModel();
		balance.setYear(year);
		balance.setMonth(month);
		balance.setEmpId(empId);
		List<BalanceDataTableModel> balancesTable= new ArrayList<BalanceDataTableModel>();
		List<LeaveBalanceModel> balances = null;
		balances = leaveBalanceRepository.findLeaveBalanceByUserYearMon(balance);
		if (CollectionUtils.isEmpty(balances)) {
			return balancesTable;
		}
		balancesTable = LeaveBalanceModelToDataTable(balances);
		return balancesTable;
	}
	public List<BalanceDataTableModel> LeaveBalanceModelToDataTable(List<LeaveBalanceModel> lbs) {
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
			lbt.setTitle(lb.getDescription());
		}
		return leaveBalanceDataTable;
	}
	public int deleteLeaveBalance(LeaveBalanceModel model) {
		return leaveBalanceRepository.deleteLeaveBanlanceByUserMon(model);
	}
	public void addLeaveRequests(List<LeaveRequestModel> models) {
		leaveRequestRepository.addLeaveRequest(models);
	}
	public int deleteLeaveRequests(LeaveRequestModel model) {
		return leaveRequestRepository.deleteLeaveRequestByUserMon(model);
	}
	public ByteArrayOutputStream exportRequestToExcel(int year, int month, int empid) throws IOException {
		LeaveBalanceModel lbModel = new LeaveBalanceModel();
		lbModel.setEmpId(empid);
		lbModel.setYear(year);
		List<LeaveBalanceModel> leaveBalances = leaveBalanceRepository.findLeaveBalanceByUserYear(lbModel);
		LeaveRequestModel lrModel = new LeaveRequestModel();
		lrModel.setYear(year);
		lrModel.setEmpId(empid);
		List<LeaveRequestModel> leaveRequests = leaveRequestRepository.findLeaveRequestByUserYear(lrModel);

		// 创建 Excel 工作簿
		Workbook workbook = new XSSFWorkbook();

		CellStyle dateTimeCellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		dateTimeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

		Sheet sheet1 = workbook.createSheet("Leave Balances");
		// 创建表头
		Row header1 = sheet1.createRow(0);
		header1.createCell(0).setCellValue("EMP_ID");
		header1.createCell(1).setCellValue("YEAR");
		header1.createCell(2).setCellValue("MONTH");
		header1.createCell(3).setCellValue("SHIFT");
		header1.createCell(4).setCellValue("USED_HOURS");
		header1.createCell(5).setCellValue("REMAINING_HOURS");

		// 填充数据
		int rowNum = 1;
		if(!CollectionUtils.isEmpty(leaveBalances)) {
			for (LeaveBalanceModel lb : leaveBalances) {
				Row row1 = sheet1.createRow(rowNum++);
				row1.createCell(0).setCellValue(lb.getEmpId());
				row1.createCell(1).setCellValue(lb.getYear());
				row1.createCell(2).setCellValue(lb.getMonth());
				row1.createCell(3).setCellValue(lb.getShift());
				row1.createCell(4).setCellValue(lb.getUsedHours());
				row1.createCell(5).setCellValue(lb.getRemainingHours());
			}
		}

		Sheet sheet2 = workbook.createSheet("Leave Requests");
		Row header2 = sheet2.createRow(0);
		header2.createCell(0).setCellValue("EMP_ID");
		header2.createCell(1).setCellValue("YEAR");
		header2.createCell(2).setCellValue("MONTH");
		header2.createCell(3).setCellValue("DAY");
		header2.createCell(4).setCellValue("SEQ");
		header2.createCell(5).setCellValue("Shift");
		header2.createCell(6).setCellValue("FROM_TIME");
		header2.createCell(7).setCellValue("TO_TIME");
		header2.createCell(8).setCellValue("HOURS");
		header2.createCell(9).setCellValue("STATUS");
		header2.createCell(10).setCellValue("Reason");
		header2.createCell(11).setCellValue("Note");
		header2.createCell(12).setCellValue("SOURCE");
		header2.createCell(13).setCellValue("REQUESTER");
		header2.createCell(14).setCellValue("APPROVED_BY");
		header2.createCell(15).setCellValue("APPROVED_TIME");
		rowNum = 1;
		if(!CollectionUtils.isEmpty(leaveRequests)) {
			for (LeaveRequestModel lr : leaveRequests) {
				Row row2 = sheet2.createRow(rowNum++);
				row2.createCell(0).setCellValue(lr.getEmpId());
				row2.createCell(1).setCellValue(lr.getYear());
				row2.createCell(2).setCellValue(lr.getMonth());
				row2.createCell(3).setCellValue(lr.getDay());
				row2.createCell(4).setCellValue(lr.getSeq());
				row2.createCell(5).setCellValue(lr.getShift());
				Cell cellFromDatetime = row2.createCell(6);
				if (lr.getFromTime() != null) {
					cellFromDatetime.setCellValue(Timestamp
							.valueOf(lr.getFromTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellFromDatetime.setCellValue(lr.getFromTime());
				}
				cellFromDatetime.setCellStyle(dateTimeCellStyle);
				Cell cellToDatetime = row2.createCell(7);
				if (lr.getToTime() != null) {
					cellToDatetime.setCellValue(Timestamp
							.valueOf(lr.getToTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellToDatetime.setCellValue(lr.getToTime());
				}
				cellToDatetime.setCellStyle(dateTimeCellStyle);
				row2.createCell(8).setCellValue(lr.getHours());
				row2.createCell(9).setCellValue(lr.getStatus());
				row2.createCell(10).setCellValue(lr.getReason());
				row2.createCell(11).setCellValue(lr.getNote());
				row2.createCell(12).setCellValue(lr.getSource());
				row2.createCell(13).setCellValue(lr.getRequester());
				row2.createCell(14).setCellValue(lr.getApprovedBy());
				Cell cellApprovedDatetime = row2.createCell(15);
				if (lr.getApprovedTime() != null) {
					cellApprovedDatetime.setCellValue(Timestamp
							.valueOf(lr.getApprovedTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellApprovedDatetime.setCellValue(lr.getApprovedTime());
				}
				cellApprovedDatetime.setCellStyle(dateTimeCellStyle);
			}
		}
		
		// 2. 将 Excel 写入 ByteArrayOutputStream
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();
		return out;
	}
}
