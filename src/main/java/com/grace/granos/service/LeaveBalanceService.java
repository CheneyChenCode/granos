package com.grace.granos.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.grace.granos.model.CustomException;
import com.grace.granos.model.LeaveBalanceModel;
import com.grace.granos.model.LeaveRequestModel;
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
	private ShiftService shiftService;
	
	public List<LeaveBalanceModel> calculateBalances(int year, int month, User user) throws CustomException {
		logger.info("Service:calculateBalances[" + year + "/" + month + "]");
		List<LeaveBalanceModel> newBalances=new ArrayList<LeaveBalanceModel>();
		List<LeaveBalanceModel> lastBalance=leaveBalanceRepository.findLastLeaveBalance(user.getCharacter().getEmpId(),year);
		Map<String, ShiftModel> shiftModelMap = shiftService.getshiftModelMap();
		List<LeaveRequestModel> lrs=new ArrayList<LeaveRequestModel>();
		if(CollectionUtils.isEmpty(lastBalance)) {
			LeaveRequestModel lr=new LeaveRequestModel();
			lr.setEmpId(user.getCharacter().getEmpId());
			lr.setYear(lastBalance.get(0).getYear());
			lrs=leaveRequestRepository.findLeaveRequestByUserYear(lr);
			boolean exists = lrs.stream().anyMatch(x -> "balances".equals(x.getSource())&& "reset".equals(x.getNote())&&x.getHours()>0);
			if(!exists) {
				if("F".equals(user.getCharacter().getGender())) {
					LeaveRequestModel BRLE=new LeaveRequestModel();
					BRLE.setEmpId(user.getCharacter().getEmpId());
					BRLE.setYear(year);
					BRLE.setMonth(1);
					BRLE.setDay(1);
					BRLE.setNote("reset");
					BRLE.setSource("balances");
					BRLE.setShift("BRLE");
					BRLE.setStatus(1);
					BRLE.setHours(48);
					BRLE.setApprovedBy(user.getUsername());
					BRLE.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					BRLE.setRequester(user.getUsername());
					BRLE.setSeq(1);
					lrs.add(BRLE);
					LeaveRequestModel BRLG=new LeaveRequestModel();
					BRLG.setEmpId(user.getCharacter().getEmpId());
					BRLG.setYear(year);
					BRLG.setMonth(1);
					BRLG.setDay(1);
					BRLG.setNote("reset");
					BRLG.setSource("balances");
					BRLG.setShift("BRLG");
					BRLG.setStatus(1);
					BRLG.setHours(24);
					BRLG.setApprovedBy(user.getUsername());
					BRLG.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					BRLG.setRequester(user.getUsername());
					BRLG.setSeq(2);
					lrs.add(BRLG);
					LeaveRequestModel BRLI=new LeaveRequestModel();
					BRLI.setEmpId(user.getCharacter().getEmpId());
					BRLI.setYear(year);
					BRLI.setMonth(1);
					BRLI.setDay(1);
					BRLI.setNote("reset");
					BRLI.setSource("balances");
					BRLI.setShift("BRLI");
					BRLI.setStatus(1);
					BRLI.setHours(64);
					BRLI.setApprovedBy(user.getUsername());
					BRLI.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					BRLI.setRequester(user.getUsername());
					BRLI.setSeq(3);
					lrs.add(BRLI);
					LeaveRequestModel MRL=new LeaveRequestModel();
					MRL.setEmpId(user.getCharacter().getEmpId());
					MRL.setYear(year);
					MRL.setMonth(1);
					MRL.setDay(1);
					MRL.setNote("reset");
					MRL.setSource("balances");
					MRL.setShift("MRL");
					MRL.setStatus(1);
					MRL.setHours(64);
					MRL.setApprovedBy(user.getUsername());
					MRL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					MRL.setRequester(user.getUsername());
					MRL.setSeq(4);
					lrs.add(MRL);
					LeaveRequestModel PDL=new LeaveRequestModel();
					PDL.setEmpId(user.getCharacter().getEmpId());
					PDL.setYear(year);
					PDL.setMonth(1);
					PDL.setDay(1);
					PDL.setNote("reset");
					PDL.setSource("balances");
					PDL.setShift("PDL");
					PDL.setStatus(1);
					PDL.setHours(72);
					PDL.setApprovedBy(user.getUsername());
					PDL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					PDL.setRequester(user.getUsername());
					PDL.setSeq(5);
					lrs.add(PDL);
					LeaveRequestModel MTL=new LeaveRequestModel();
					MTL.setEmpId(user.getCharacter().getEmpId());
					MTL.setYear(year);
					MTL.setMonth(1);
					MTL.setDay(1);
					MTL.setNote("reset");
					MTL.setSource("balances");
					MTL.setShift("MTL");
					MTL.setStatus(1);
					MTL.setHours(448);
					MTL.setApprovedBy(user.getUsername());
					MTL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					MTL.setRequester(user.getUsername());
					MTL.setSeq(6);
					lrs.add(MTL);
					LeaveRequestModel PSL=new LeaveRequestModel();
					PSL.setEmpId(user.getCharacter().getEmpId());
					PSL.setYear(year);
					PSL.setMonth(1);
					PSL.setDay(1);
					PSL.setNote("reset");
					PSL.setSource("balances");
					PSL.setShift("PSL");
					PSL.setStatus(1);
					PSL.setHours(112);
					PSL.setApprovedBy(user.getUsername());
					PSL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					PSL.setRequester(user.getUsername());
					PSL.setSeq(7);
					lrs.add(PSL);
					LeaveRequestModel PTL=new LeaveRequestModel();
					PTL.setEmpId(user.getCharacter().getEmpId());
					PTL.setYear(year);
					PTL.setMonth(1);
					PTL.setDay(1);
					PTL.setNote("reset");
					PTL.setSource("balances");
					PTL.setShift("PTL");
					PTL.setStatus(1);
					PTL.setHours(56);
					PTL.setApprovedBy(user.getUsername());
					PTL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					PTL.setRequester(user.getUsername());
					PTL.setSeq(8);
					lrs.add(PTL);
					LeaveRequestModel SKL=new LeaveRequestModel();
					SKL.setEmpId(user.getCharacter().getEmpId());
					SKL.setYear(year);
					SKL.setMonth(1);
					SKL.setDay(1);
					SKL.setNote("reset");
					SKL.setSource("balances");
					SKL.setShift("SKL");
					SKL.setStatus(1);
					SKL.setHours(240);
					SKL.setApprovedBy(user.getUsername());
					SKL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					SKL.setRequester(user.getUsername());
					SKL.setSeq(9);
					lrs.add(SKL);
				}else {
					LeaveRequestModel BRLE=new LeaveRequestModel();
					BRLE.setEmpId(user.getCharacter().getEmpId());
					BRLE.setYear(year);
					BRLE.setMonth(1);
					BRLE.setDay(1);
					BRLE.setNote("reset");
					BRLE.setSource("balances");
					BRLE.setShift("BRLE");
					BRLE.setStatus(1);
					BRLE.setHours(48);
					BRLE.setApprovedBy(user.getUsername());
					BRLE.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					BRLE.setRequester(user.getUsername());
					BRLE.setSeq(1);
					lrs.add(BRLE);
					LeaveRequestModel BRLG=new LeaveRequestModel();
					BRLG.setEmpId(user.getCharacter().getEmpId());
					BRLG.setYear(year);
					BRLG.setMonth(1);
					BRLG.setDay(1);
					BRLG.setNote("reset");
					BRLG.setSource("balances");
					BRLG.setShift("BRLG");
					BRLG.setStatus(1);
					BRLG.setHours(24);
					BRLG.setApprovedBy(user.getUsername());
					BRLG.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					BRLG.setRequester(user.getUsername());
					BRLG.setSeq(2);
					lrs.add(BRLG);
					LeaveRequestModel BRLI=new LeaveRequestModel();
					BRLI.setEmpId(user.getCharacter().getEmpId());
					BRLI.setYear(year);
					BRLI.setMonth(1);
					BRLI.setDay(1);
					BRLI.setNote("reset");
					BRLI.setSource("balances");
					BRLI.setShift("BRLI");
					BRLI.setStatus(1);
					BRLI.setHours(64);
					BRLI.setApprovedBy(user.getUsername());
					BRLI.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					BRLI.setRequester(user.getUsername());
					BRLI.setSeq(3);
					lrs.add(BRLI);
					LeaveRequestModel MRL=new LeaveRequestModel();
					MRL.setEmpId(user.getCharacter().getEmpId());
					MRL.setYear(year);
					MRL.setMonth(1);
					MRL.setDay(1);
					MRL.setNote("reset");
					MRL.setSource("balances");
					MRL.setShift("MRL");
					MRL.setStatus(1);
					MRL.setHours(64);
					MRL.setApprovedBy(user.getUsername());
					MRL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					MRL.setRequester(user.getUsername());
					MRL.setSeq(4);
					lrs.add(MRL);
					LeaveRequestModel PDL=new LeaveRequestModel();
					PDL.setEmpId(user.getCharacter().getEmpId());
					PDL.setYear(year);
					PDL.setMonth(1);
					PDL.setDay(1);
					PDL.setNote("reset");
					PDL.setSource("balances");
					PDL.setShift("PDL");
					PDL.setStatus(1);
					PDL.setHours(72);
					PDL.setApprovedBy(user.getUsername());
					PDL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					PDL.setRequester(user.getUsername());
					PDL.setSeq(5);
					lrs.add(PDL);
					LeaveRequestModel PRL=new LeaveRequestModel();
					PRL.setEmpId(user.getCharacter().getEmpId());
					PRL.setYear(year);
					PRL.setMonth(1);
					PRL.setDay(1);
					PRL.setNote("reset");
					PRL.setSource("balances");
					PRL.setShift("PRL");
					PRL.setStatus(1);
					PRL.setHours(56);
					PRL.setApprovedBy(user.getUsername());
					PRL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					PRL.setRequester(user.getUsername());
					PRL.setSeq(6);
					lrs.add(PRL);
					LeaveRequestModel PSL=new LeaveRequestModel();
					PSL.setEmpId(user.getCharacter().getEmpId());
					PSL.setYear(year);
					PSL.setMonth(1);
					PSL.setDay(1);
					PSL.setNote("reset");
					PSL.setSource("balances");
					PSL.setShift("PSL");
					PSL.setStatus(1);
					PSL.setHours(112);
					PSL.setApprovedBy(user.getUsername());
					PSL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					PSL.setRequester(user.getUsername());
					PSL.setSeq(7);
					lrs.add(PSL);
					LeaveRequestModel PTL=new LeaveRequestModel();
					PTL.setEmpId(user.getCharacter().getEmpId());
					PTL.setYear(year);
					PTL.setMonth(1);
					PTL.setDay(1);
					PTL.setNote("reset");
					PTL.setSource("balances");
					PTL.setShift("PTL");
					PTL.setStatus(1);
					PTL.setHours(56);
					PTL.setApprovedBy(user.getUsername());
					PTL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					PTL.setRequester(user.getUsername());
					PTL.setSeq(8);
					lrs.add(PTL);
					LeaveRequestModel SKL=new LeaveRequestModel();
					SKL.setEmpId(user.getCharacter().getEmpId());
					SKL.setYear(year);
					SKL.setMonth(1);
					SKL.setDay(1);
					SKL.setNote("reset");
					SKL.setSource("balances");
					SKL.setShift("SKL");
					SKL.setStatus(1);
					SKL.setHours(240);
					SKL.setApprovedBy(user.getUsername());
					SKL.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
					SKL.setRequester(user.getUsername());
					SKL.setSeq(9);
					lrs.add(SKL);
				}
				//throw new CustomException("there are no Last Balances in this account",4002);
		        Map<String, List<LeaveRequestModel>> groupedByShift = lrs.stream()
		                .collect(Collectors.groupingBy(LeaveRequestModel::getShift));
		        groupedByShift.forEach((shift, requests) -> {
		        LeaveBalanceModel newLb =new LeaveBalanceModel();
				newLb.setCreater(user.getUsername());
				newLb.setEmpId(user.getCharacter().getEmpId());
				newLb.setYear(year);
				newLb.setMonth(month);
				newLb.setShift(shift);
				float shiftHours=(float)requests.stream().filter(x->x.getShift().equals(shift)&&x.getHours()<0&&(x.getYear()==year&&x.getMonth()>month||x.getYear()>year)).mapToDouble(LeaveRequestModel::getHours).sum(); // 加總
				newLb.setUsedHours(shiftHours);
				float plusHours=(float)requests.stream().filter(x->x.getShift().equals(shift)&&x.getHours()>0&&x.getStatus()==1&&(x.getYear()==year&&x.getMonth()>month||x.getYear()>year)).mapToDouble(LeaveRequestModel::getHours).sum();
				newLb.setRemainingHours(plusHours+shiftHours);
		        ShiftModel shiftModel=shiftModelMap.get(shift);
				if(shiftModel!=null) {
					newLb.setDescription(shiftModel.getDescription());
				}
				newBalances.add(newLb);
		        });
			}	
		
		}else {
			//lastBalance = calculateBalancesPreMon(year, month, user, lastBalance, shiftModelMap, lrs);
			LeaveRequestModel lr=new LeaveRequestModel();
			lr.setEmpId(user.getCharacter().getEmpId());
			lr.setYear(lastBalance.get(0).getYear());
			lr.setMonth(lastBalance.get(0).getMonth());
			lrs=leaveRequestRepository.findLastLeaveRequest(lr);
			for(LeaveBalanceModel lb:lastBalance) {
				LeaveBalanceModel newLb =new LeaveBalanceModel();
				newLb.setCreater(user.getUsername());
				newLb.setEmpId(user.getCharacter().getEmpId());
				newLb.setYear(year);
				newLb.setMonth(month);
				float shiftHours=lb.getUsedHours();
				float remainingHours=lb.getRemainingHours();
				newLb.setUsedHours(shiftHours);
				newLb.setRemainingHours(remainingHours);
				newLb.setShift(lb.getShift());
				ShiftModel shift=shiftModelMap.get(lb.getShift());
				if(shift!=null) {
					newLb.setDescription(shift.getDescription());
				}
				if(!CollectionUtils.isEmpty(lrs)) {
					float shiftHoursChange=(float)lrs.stream().filter(x->x.getShift().equals(lb.getShift())&&x.getHours()<0&&(x.getYear()==lb.getYear()&&x.getMonth()>lb.getMonth()||x.getYear()>lb.getYear())).mapToDouble(LeaveRequestModel::getHours).sum(); // 加總
					float plusHours=(float)lrs.stream().filter(x->x.getShift().equals(lb.getShift())&&x.getHours()>0&&x.getStatus()==1&&(x.getYear()==lb.getYear()&&x.getMonth()>lb.getMonth()||x.getYear()>lb.getYear())).mapToDouble(LeaveRequestModel::getHours).sum();
					newLb.setUsedHours(shiftHours+shiftHoursChange);
					newLb.setRemainingHours(remainingHours+plusHours+shiftHoursChange);
				}

				newBalances.add(newLb);
		}	
	}
		return newBalances;
	}
	public List<LeaveRequestModel> findLastLeaveRequest(LeaveRequestModel lr) {
		List<LeaveRequestModel> lrs=leaveRequestRepository.findLastLeaveRequest(lr);
		return lrs;
	}

	public List<LeaveBalanceModel> calculateBalancesPreMon(int year, int month, User user, List<LeaveBalanceModel> lastBalance,
			Map<String, ShiftModel> shiftModelMap, List<LeaveRequestModel> lrs) {
			List<LeaveBalanceModel> preBalances=new ArrayList<LeaveBalanceModel>();
			for(LeaveBalanceModel lb:lastBalance) {
				LeaveBalanceModel preLb =new LeaveBalanceModel();
				preLb.setCreater(user.getUsername());
				preLb.setEmpId(user.getCharacter().getEmpId());
				preLb.setYear(year);
				preLb.setMonth(month);
				float shiftHours=lb.getUsedHours();
				float remainingHours=lb.getRemainingHours();
				preLb.setShift(lb.getShift());
				preLb.setUsedHours(shiftHours);
				preLb.setRemainingHours(remainingHours);
				ShiftModel shift=shiftModelMap.get(lb.getShift());
				if(shift!=null) {
					preLb.setDescription(shift.getDescription());
				}
				if(!CollectionUtils.isEmpty(lrs)) {
					float shiftHoursChange=(float)lrs.stream().filter(x->x.getShift().equals(lb.getShift())&&x.getHours()<0&&x.getYear()==year&&x.getMonth()==month).mapToDouble(LeaveRequestModel::getHours).sum();
					float plusHours=(float)lrs.stream().filter(x->x.getShift().equals(lb.getShift())&&x.getHours()>0&&x.getStatus()==1&&x.getYear()==year&&x.getMonth()==month).mapToDouble(LeaveRequestModel::getHours).sum();
					preLb.setUsedHours(shiftHours+shiftHoursChange);
					preLb.setRemainingHours(remainingHours+plusHours+shiftHoursChange);
				}

				preBalances.add(preLb);
			}
			leaveBalanceRepository.addLeaveBalance(preBalances);
		return preBalances;
	}
	
	public List<LeaveBalanceModel> findLastLeaveBalances(int empId,int year) {
		return leaveBalanceRepository.findLastLeaveBalance(empId,year);
	}
	public List<LeaveBalanceModel> getLeaveBalances(int year, int month, int empId) {
		logger.info("Service:getLeaveBalances[" + year + "/" + month + "]");
		LeaveBalanceModel balance = new LeaveBalanceModel();
		balance.setYear(year);
		balance.setMonth(month);
		balance.setEmpId(empId);
		List<LeaveBalanceModel> balances = null;
		balances = leaveBalanceRepository.findLeaveBalanceByUserYearMon(balance);
		return balances;
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
