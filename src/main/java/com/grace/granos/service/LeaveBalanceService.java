package com.grace.granos.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.grace.granos.model.CustomException;
import com.grace.granos.model.LeaveBalanceModel;
import com.grace.granos.model.LeaveRequestModel;
import com.grace.granos.model.ShiftModel;
import com.grace.granos.model.StaffModel;
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
	@Autowired
	private StaffService staffService;

	public List<LeaveBalanceModel> calculateBalances(int year, int month, User user) throws CustomException {
		logger.info("Service:calculateBalances[" + year + "/" + month + "]");
		List<LeaveBalanceModel> newBalances = new ArrayList<LeaveBalanceModel>();
		LeaveBalanceModel lbl = new LeaveBalanceModel();
		lbl.setEmpId(user.getCharacter().getEmpId());
		lbl.setYear(year);
		lbl.setMonth(month);
		List<LeaveBalanceModel> lastBalance = leaveBalanceRepository.findLastLeaveBalance(lbl);
		Map<String, ShiftModel> shiftModelMap = shiftService.getshiftModelMap();
		List<LeaveRequestModel> lrs;
		if (!CollectionUtils.isEmpty(lastBalance)) {
			List<LeaveBalanceModel> currBalance=lastBalance.stream().filter(x->x.getYear()==year && x.getMonth()==month).toList();
			if(!CollectionUtils.isEmpty(currBalance)) {
				shiftModelMap.forEach((key, shiftModel) -> {
					if (key.length() > 2 && "L".equals(StringUtils.right(key, 1))) {
						if("M".equals(user.getCharacter().getGender())) {
							if("MTL".equals(key)||"MSL".equals(key)) {
								return;
							}
						}else {
							if("PTL".equals(key)) {
								return;
							}
						}
						LeaveBalanceModel newLb = new LeaveBalanceModel();
						newLb.setCreater(user.getUsername());
						newLb.setEmpId(user.getCharacter().getEmpId());
						newLb.setYear(year);
						newLb.setMonth(month);
						float shiftHours = 0;
						float remainingHours = 0;
						newLb.setShift(key);
						newLb.setDescription(shiftModel.getDescription());
						Optional<LeaveBalanceModel> lbop=currBalance.stream().filter(x->x.getShift().equals(key)).findFirst();
						if(lbop.isPresent()) {
							LeaveBalanceModel lb=lbop.get();
							shiftHours=lb.getUsedHours();
							remainingHours=lb.getRemainingHours();
						}
						newLb.setUsedHours(shiftHours);
						newLb.setRemainingHours(remainingHours);
						newBalances.add(newLb);
					}
				});
			}else {
		        Optional<LeaveBalanceModel> maxBalance = lastBalance.stream().filter(x -> x.getYear()<year || (x.getYear()==year && x.getMonth() < month))
		                .max(Comparator.comparingInt(LeaveBalanceModel::getYear)
		                    .thenComparingInt(LeaveBalanceModel::getMonth));
		        if (maxBalance.isPresent()) {
		            int maxYear = maxBalance.get().getYear();
		            int maxMonth = maxBalance.get().getMonth();

		            // 筛选所有 year == maxYear 且 month == maxMonth 的记录
		            lastBalance = lastBalance.stream()
		                .filter(x -> x.getYear() == maxYear && x.getMonth() == maxMonth)
		                .collect(Collectors.toList());
		        }
		        List<LeaveBalanceModel> maxBalances=lastBalance;
				LeaveRequestModel lr = new LeaveRequestModel();
				lr.setEmpId(user.getCharacter().getEmpId());
				lr.setYear(maxBalance.get().getYear());
				lr.setMonth(maxBalance.get().getMonth());
				lrs = leaveRequestRepository.findLastLeaveRequest(lr);
				shiftModelMap.forEach((key, shiftModel) -> {
					if (key.length() > 2 && "L".equals(StringUtils.right(key, 1))) {
						if("M".equals(user.getCharacter().getGender())) {
							if("MTL".equals(key)||"MSL".equals(key)) {
								return;
							}
						}else {
							if("PTL".equals(key)) {
								return;
							}
						}
						LeaveBalanceModel newLb = new LeaveBalanceModel();
						newLb.setCreater(user.getUsername());
						newLb.setEmpId(user.getCharacter().getEmpId());
						newLb.setYear(year);
						newLb.setMonth(month);
						float shiftHours = 0;
						float remainingHours = 0;
						newLb.setShift(key);
						newLb.setDescription(shiftModel.getDescription());
						Optional<LeaveBalanceModel> lbop=maxBalances.stream().filter(x->x.getShift().equals(key)).findFirst();
						if(lbop.isPresent()) {
							LeaveBalanceModel lb=lbop.get();
							if(lb.getYear()==year) {
								shiftHours=lb.getUsedHours();
							}
							remainingHours=lb.getRemainingHours();
						}
							
							if (!CollectionUtils.isEmpty(lrs)) {
								List<LeaveRequestModel> lrsn;
								if(lbop.isPresent()) {
									LeaveBalanceModel lb=lbop.get();
									lrsn = lrs.stream()
											.filter(x -> x.getShift().equals(key) && x.getStatus() == 1
													&& (x.getYear() > lb.getYear() ||(x.getYear() == lb.getYear() && x.getMonth() > lb.getMonth())) && (x.getYear()<year || (x.getYear()==year&& x.getMonth()<= month)))
											.toList();
								}else {
									lrsn = lrs.stream()
											.filter(x -> x.getShift().equals(key) && x.getStatus() == 1
													&& (x.getYear()<year || (x.getYear()==year&& x.getMonth()<= month)))
											.toList();
								}
								
								if (!CollectionUtils.isEmpty(lrsn)) {
									float shiftHoursChange = (float) lrsn.stream()
											.filter(x->x.getYear()==year && x.getHours() < 0 && !"balances".equals(x.getSource()))
											.mapToDouble(LeaveRequestModel::getHours).sum(); // 加總
									float cancelHoursChange = (float) lrsn.stream()
											.filter(x-> x.getHours() < 0 )
											.mapToDouble(LeaveRequestModel::getHours).sum(); // 加總
									float plusHours = (float) lrsn.stream()
											.filter(x ->  x.getHours() > 0 )
											.mapToDouble(LeaveRequestModel::getHours).sum();
									shiftHours=shiftHours + shiftHoursChange;
									remainingHours=remainingHours + plusHours +cancelHoursChange;
								}

							}
							

						newLb.setUsedHours(shiftHours);
						newLb.setRemainingHours(remainingHours);
						newBalances.add(newLb);
						
					}
				});
			}

		} else {
			LeaveRequestModel lr = new LeaveRequestModel();
			lr.setEmpId(user.getCharacter().getEmpId());
			lr.setYear(year);
			lr.setMonth(month);
			lrs = leaveRequestRepository.findAllLeaveRequestByUser(user.getCharacter().getEmpId());
			shiftModelMap.forEach((key, shiftModel) -> {
				if (key.length() > 2 && "L".equals(StringUtils.right(key, 1))) {
					if("M".equals(user.getCharacter().getGender())) {
						if("MTL".equals(key)||"MSL".equals(key)) {
							return;
						}
					}else {
						if("PTL".equals(key)) {
							return;
						}
					}
					LeaveBalanceModel newLb = new LeaveBalanceModel();
					newLb.setCreater(user.getUsername());
					newLb.setEmpId(user.getCharacter().getEmpId());
					newLb.setYear(year);
					newLb.setMonth(month);
					newLb.setShift(key);
					newLb.setDescription(shiftModel.getDescription());
					float shiftHours = 0;
					float remainingHours = 0;
					if (!CollectionUtils.isEmpty(lrs)) {
						float usedHours = (float) lrs.stream()
								.filter(x -> x.getYear()==year && x.getShift().equals(key) && x.getHours() < 0 && x.getStatus() == 1 && !"balances".equals(x.getSource()))
								.mapToDouble(LeaveRequestModel::getHours).sum(); // 加總
						float cancelHours = (float) lrs.stream()
								.filter(x -> (x.getYear()<year || (x.getYear()==year && x.getMonth()<=month)) && x.getShift().equals(key) && x.getHours() < 0 && x.getStatus() == 1)
								.mapToDouble(LeaveRequestModel::getHours).sum(); // 加總
						float plusHours = (float) lrs.stream()
								.filter(x -> (x.getYear()<year || (x.getYear()==year && x.getMonth()<=month)) && x.getShift().equals(key) && x.getHours() > 0 && x.getStatus() == 1)
								.mapToDouble(LeaveRequestModel::getHours).sum();
						shiftHours=usedHours;
						remainingHours=plusHours +cancelHours;
						
					}
					newLb.setUsedHours(shiftHours);
					newLb.setRemainingHours(remainingHours);
					newBalances.add(newLb);
				}
			});
		}

		return newBalances;
	}

	public void sumMSLMon(List<LeaveBalanceModel> lastBalance, LocalDateTime currentDate,List<LeaveRequestModel> lrs) {
		Optional<LeaveBalanceModel> lb = lastBalance.stream().filter(x -> "MSL".equals(x.getShift())).findFirst();
		LocalDateTime dateTime = LocalDateTime.of(lastBalance.get(0).getYear(), lastBalance.get(0).getMonth(), 1, 0, 0, 0).plusMonths(1);
		if (lb.isPresent()) {
			LeaveBalanceModel lbMsl = lb.get();
			if (lbMsl.getRemainingHours() > 0) {
				List<LeaveRequestModel> lrns = new ArrayList<LeaveRequestModel>();
				LeaveRequestModel lrn = new LeaveRequestModel();
				lrn.setEmpId(lbMsl.getEmpId());
				lrn.setYear(dateTime.getYear());
				lrn.setMonth(dateTime.getMonthValue());
				lrn.setShift(lbMsl.getShift());
				lrn.setDay(dateTime.getDayOfMonth());
				lrn.setFromTime(Timestamp.valueOf(dateTime));
				lrn.setToTime(lrn.getFromTime());
				lrn.setHours(0 - lbMsl.getRemainingHours());
				lrn.setApprovedBy(lbMsl.getCreater());
				lrn.setApprovedTime(Timestamp.valueOf(currentDate));
				lrn.setSource("balances");
				lrn.setNote(lastBalance.get(0).getYear() + "/" + lastBalance.get(0).getMonth() + " menstrual leave remaining has expired");
				lrn.setRequester(lbMsl.getCreater());
				lrn.setSeq(1);
				lrn.setStatus(1);
				lrns.add(lrn);
				leaveRequestRepository.addLeaveRequest(lrns);
				//lbMsl.setRemainingHours(0);
				//lbMsl.setUsedHours(lbMsl.getUsedHours() + lrn.getHours());
				lrs.addAll(lrns);
			}
		}
	}

	public void sumLeavesYear(List<LeaveBalanceModel> lastBalance, LocalDateTime currentDate,List<LeaveRequestModel> lrs) {
		LeaveBalanceModel lb = new LeaveBalanceModel();
		lb.setYear(lastBalance.get(0).getYear() - 1);
		lb.setMonth(12);
		lb.setEmpId(lastBalance.get(0).getEmpId());
		List<LeaveBalanceModel> lbs = leaveBalanceRepository.findLeaveBalanceByUserYearMon(lb);
		LocalDateTime dateTime = LocalDateTime.of(lastBalance.get(0).getYear(), 1, 1, 0, 0, 0).plusYears(1);
		if (!CollectionUtils.isEmpty(lbs)) {
			Optional<LeaveBalanceModel> lb12 = lastBalance.stream().filter(x -> "PDL".equals(x.getShift())).findFirst();// 2024/12/31
			Optional<LeaveBalanceModel> lb12last = lbs.stream().filter(x -> "PDL".equals(x.getShift())).findFirst();// 2023
																													// 12/31
			if (lb12last.isPresent()) {
				LeaveBalanceModel lb12lastPdl = lb12last.get();
				LeaveBalanceModel lb12Pdl = lb12.get();
				if (lb12lastPdl.getRemainingHours() > 0) {
					float lastRemaining = lb12lastPdl.getRemainingHours() + lb12Pdl.getUsedHours();// 2023 Remaining -
																									// 2024 used
					if (lastRemaining > 0) {
						List<LeaveRequestModel> lrns = new ArrayList<LeaveRequestModel>();
						LeaveRequestModel lrn = new LeaveRequestModel();
						lrn.setEmpId(lb12lastPdl.getEmpId());
						lrn.setYear(dateTime.getDayOfYear());
						lrn.setMonth(dateTime.getMonthValue());
						lrn.setShift("PDL");
						lrn.setDay(dateTime.getDayOfMonth());
						lrn.setFromTime(Timestamp.valueOf(dateTime));
						lrn.setToTime(lrn.getFromTime());
						lrn.setHours(0 - lastRemaining);
						lrn.setApprovedBy(lb12lastPdl.getCreater());
						lrn.setApprovedTime(Timestamp.valueOf(currentDate));
						lrn.setSource("balances");
						lrn.setNote(lb.getYear() + " paid leaves remaining has expired");
						lrn.setRequester(lb12lastPdl.getCreater());
						lrn.setSeq(1);
						lrn.setStatus(1);
						lrns.add(lrn);
						leaveRequestRepository.addLeaveRequest(lrns);
						lrs.addAll(lrns);
						//lb12Pdl.setUsedHours(lb12Pdl.getUsedHours() + lrn.getHours());
						//lb12Pdl.setRemainingHours(lb12Pdl.getRemainingHours() + lrn.getHours());
					}
				}
			}
		}
		List<LeaveBalanceModel> lb12ot = lastBalance.stream()
				.filter(x -> !"PDL".equals(x.getShift()) && !"MSL".equals(x.getShift())).toList();// 2024/12/31
		if (!CollectionUtils.isEmpty(lb12ot)) {
			for (LeaveBalanceModel l : lb12ot) {
				if (l.getRemainingHours() > 0) {
					List<LeaveRequestModel> lrnsw = new ArrayList<LeaveRequestModel>();
					LeaveRequestModel lrnw = new LeaveRequestModel();
					lrnw.setEmpId(l.getEmpId());
					lrnw.setYear(dateTime.getYear());
					lrnw.setMonth(dateTime.getMonthValue());
					lrnw.setShift(l.getShift());
					lrnw.setDay(dateTime.getDayOfMonth());
					lrnw.setFromTime(Timestamp.valueOf(dateTime));
					lrnw.setToTime(lrnw.getFromTime());
					lrnw.setHours(0 - l.getRemainingHours());
					lrnw.setApprovedBy(l.getCreater());
					lrnw.setApprovedTime(Timestamp.valueOf(currentDate));
					lrnw.setSource("balances");
					lrnw.setNote(lastBalance.get(0).getYear() + " leaves remaining has expired");
					lrnw.setRequester(l.getCreater());
					lrnw.setSeq(1);
					lrnw.setStatus(1);
					lrnsw.add(lrnw);
					leaveRequestRepository.addLeaveRequest(lrnsw);
					lrs.addAll(lrnsw);
					//l.setUsedHours(l.getUsedHours() + lrnw.getHours());
					//l.setRemainingHours(l.getRemainingHours() + lrnw.getHours());
				}
			}

		}
	}

	public List<LeaveRequestModel> resetBalancesByUser(int year, int month, LocalDateTime currentDate, String zoneName,
			User user) {
		StaffModel staff = staffService.findStaffById(user.getCharacter().getEmpId());
		LocalDateTime entryLocalDateTime = staff.getEntryDate().toLocalDate().atStartOfDay(ZoneId.of(zoneName))
				.toLocalDateTime();
		LeaveRequestModel lr = new LeaveRequestModel();
		lr.setYear(year);
		lr.setMonth(entryLocalDateTime.getMonthValue());
		lr.setEmpId(staff.getEmpId());
		List<LeaveRequestModel> lras = leaveRequestRepository.findLastNewBalancesRequestByUser(lr);
		List<LeaveRequestModel> rt = new ArrayList<LeaveRequestModel>();
		if (CollectionUtils.isEmpty(lras)) {
			for (int i = 0; i <= currentDate.getYear() - year; i++) {
				int nextYear = year + i;
				if (i == 0) {
					if (month <= lr.getMonth()) {
						rt.addAll(resetPDLnewBalances(nextYear, currentDate, staff, zoneName));
					}
				} else {
					rt.addAll(resetPDLnewBalances(nextYear, currentDate, staff, zoneName));
				}
			}
		} else {
			for (int i = 0; i <= currentDate.getYear() - year; i++) {
				int nextYear = year + i;
				boolean exits = lras.stream().anyMatch(x -> x.getYear() == nextYear);
				if (i == 0) {
					if (month <= lr.getMonth() && !exits) {
						rt.addAll(resetPDLnewBalances(nextYear, currentDate, staff, zoneName));
					}
				} else {
					if (!exits) {
						rt.addAll(resetPDLnewBalances(nextYear, currentDate, staff, zoneName));
					}
				}
			}
		}

		if ("F".equals(staff.getGender())) {
			int cMon= month;
			int cYear=year;
			int diffMon=0;
			if(currentDate.getYear()>year) {
				int diffYear=currentDate.getYear()-year;
				diffMon=(diffYear-1)*12+12-month+currentDate.getMonthValue();
			}else {
				diffMon=currentDate.getMonthValue()-month;
			}
			for (int i = 0; i <= diffMon; i++) {
				LeaveRequestModel lrMsl = new LeaveRequestModel();
				lrMsl.setYear(cYear);
				lrMsl.setMonth(cMon);
				lrMsl.setEmpId(staff.getEmpId());
				List<LeaveRequestModel> lrasf = leaveRequestRepository.findMSLRequestByMon(lr);
				if (!CollectionUtils.isEmpty(lrasf)) {
					boolean existc = lrasf.stream().anyMatch(x -> "balances".equals(x.getSource()) && x.getHours() > 0);
					if (!existc) {
						rt.addAll(addNewMSL(cYear, cMon, currentDate, staff, zoneName));
					}
				} else {
					rt.addAll(addNewMSL(cYear, cMon, currentDate, staff, zoneName));
				}
				cMon=cMon+1;
				if(cMon>12) {
					cYear=cYear+1;
					cMon=1;
				}
			}

		}
		return rt;
	}

	public List<LeaveRequestModel> resetBalances(LocalDateTime currentDate, String zoneName) {
		LeaveRequestModel lr = new LeaveRequestModel();
		lr.setYear(currentDate.getYear());
		lr.setMonth(currentDate.getMonthValue());
		List<LeaveRequestModel> lras = leaveRequestRepository.findAllNewBalancesRequestByYear(lr);
		List<LeaveRequestModel> lrasf = leaveRequestRepository.findAllMSLRequestByMon(lr);
		List<StaffModel> staffs = staffService.getStaff();
		List<LeaveRequestModel> rt = new ArrayList<LeaveRequestModel>();
		for (StaffModel s : staffs) {
			if(!CollectionUtils.isEmpty(lras)) {
				boolean exist = lras.stream().anyMatch(x -> x.getEmpId() == s.getEmpId());
				if (!exist) {
					rt.addAll(resetPDLnewBalances(currentDate.getYear(), currentDate, s, zoneName));
					rt.addAll(resetOTnewBalances(currentDate.getYear(), currentDate, s));
				}else {
					List<LeaveRequestModel> otNewBalance=lras.stream().filter(x->x.getStatus()==1 && "balances".equals(x.getSource())&&x.getYear()==currentDate.getYear()&&!"PDL".equals(x.getShift())&&!"MSL".equals(x.getShift())&&x.getHours()>0).toList();
					if(CollectionUtils.isEmpty(otNewBalance)) {
						rt.addAll(resetOTnewBalances(currentDate.getYear(), currentDate, s));
					}
					List<LeaveRequestModel> pdlNewBalance=lras.stream().filter(x->x.getStatus()==1 && "balances".equals(x.getSource())&&x.getYear()==currentDate.getYear()&&"PDL".equals(x.getShift())&&x.getHours()>0).toList();
					if(CollectionUtils.isEmpty(pdlNewBalance)) {
						rt.addAll(resetPDLnewBalances(currentDate.getYear(), currentDate, s, zoneName));
					}
				}
			}else {
				rt.addAll(resetPDLnewBalances(currentDate.getYear(), currentDate, s, zoneName));
				rt.addAll(resetOTnewBalances(currentDate.getYear(), currentDate, s));
			}
			if ("F".equals(s.getGender())) {
				if(!CollectionUtils.isEmpty(lrasf)) {
					boolean existc = lrasf.stream().anyMatch(
							x -> x.getEmpId() == s.getEmpId() && x.getHours() > 0 && "balances".equals(x.getSource()));
					if (!existc) {
						rt.addAll(addNewMSL(currentDate.getYear(), currentDate.getMonthValue(), currentDate, s, zoneName));
					}
				}else {
					rt.addAll(addNewMSL(currentDate.getYear(), currentDate.getMonthValue(), currentDate, s, zoneName));
				}
			}


		}
		return rt;
	}

	public List<LeaveRequestModel> addNewMSL(int year, int month, LocalDateTime currentDate, StaffModel staff,
			String zoneName) {
		List<LeaveRequestModel> lrs = new ArrayList<LeaveRequestModel>();
		LocalDateTime updatedLocalDateTime = currentDate.withYear(year).withMonth(month).withDayOfMonth(1);
		LeaveRequestModel MSL = new LeaveRequestModel();
		MSL.setEmpId(staff.getEmpId());
		MSL.setYear(year);
		MSL.setMonth(month);
		MSL.setDay(1);
		MSL.setNote("new MSL in the month");
		MSL.setSource("balances");
		MSL.setShift("MSL");
		MSL.setStatus(1);
		MSL.setHours(8);
		MSL.setFromTime(Timestamp.valueOf(updatedLocalDateTime));
		MSL.setToTime(MSL.getFromTime());
		MSL.setApprovedBy(staff.getUsername());
		MSL.setApprovedTime(Timestamp.valueOf(currentDate));
		MSL.setRequester(staff.getUsername());
		MSL.setSeq(1);
		lrs.add(MSL);
		leaveRequestRepository.addLeaveRequest(lrs);
		return lrs;
	}

	public List<LeaveRequestModel> resetOTnewBalances(int year, LocalDateTime currentDate, StaffModel staff) {
		List<LeaveRequestModel> lrs = new ArrayList<LeaveRequestModel>();
		LocalDateTime dateTime = LocalDateTime.of(year, 1, 1, 0, 0, 0);
		if ("F".equals(staff.getGender())) {
			LeaveRequestModel BREL = new LeaveRequestModel();
			BREL.setEmpId(staff.getEmpId());
			BREL.setYear(year);
			BREL.setMonth(dateTime.getMonthValue());
			BREL.setDay(dateTime.getDayOfMonth());
			BREL.setNote("reset");
			BREL.setSource("balances");
			BREL.setShift("BREL");
			BREL.setStatus(1);
			BREL.setHours(48);
			BREL.setFromTime(Timestamp.valueOf(dateTime));
			BREL.setToTime(BREL.getFromTime());
			BREL.setApprovedBy(staff.getUsername());
			BREL.setApprovedTime(Timestamp.valueOf(currentDate));
			BREL.setRequester(staff.getUsername());
			BREL.setSeq(1);
			lrs.add(BREL);
			LeaveRequestModel BRGL = new LeaveRequestModel();
			BRGL.setEmpId(staff.getEmpId());
			BRGL.setYear(year);
			BRGL.setMonth(dateTime.getMonthValue());
			BRGL.setDay(dateTime.getDayOfMonth());
			BRGL.setNote("reset");
			BRGL.setSource("balances");
			BRGL.setShift("BRGL");
			BRGL.setStatus(1);
			BRGL.setHours(24);
			BRGL.setFromTime(Timestamp.valueOf(dateTime));
			BRGL.setToTime(BRGL.getFromTime());
			BRGL.setApprovedBy(staff.getUsername());
			BRGL.setApprovedTime(Timestamp.valueOf(currentDate));
			BRGL.setRequester(staff.getUsername());
			BRGL.setSeq(1);
			lrs.add(BRGL);
			LeaveRequestModel BRIL = new LeaveRequestModel();
			BRIL.setEmpId(staff.getEmpId());
			BRIL.setYear(year);
			BRIL.setMonth(dateTime.getMonthValue());
			BRIL.setDay(dateTime.getDayOfMonth());
			BRIL.setNote("reset");
			BRIL.setSource("balances");
			BRIL.setShift("BRIL");
			BRIL.setStatus(1);
			BRIL.setHours(64);
			BRIL.setFromTime(Timestamp.valueOf(dateTime));
			BRIL.setToTime(BRIL.getFromTime());
			BRIL.setApprovedBy(staff.getUsername());
			BRIL.setApprovedTime(Timestamp.valueOf(currentDate));
			BRIL.setRequester(staff.getUsername());
			BRIL.setSeq(1);
			lrs.add(BRIL);
			LeaveRequestModel MRL = new LeaveRequestModel();
			MRL.setEmpId(staff.getEmpId());
			MRL.setYear(year);
			MRL.setMonth(dateTime.getMonthValue());
			MRL.setDay(dateTime.getDayOfMonth());
			MRL.setNote("reset");
			MRL.setSource("balances");
			MRL.setShift("MRL");
			MRL.setStatus(1);
			MRL.setHours(64);
			MRL.setFromTime(Timestamp.valueOf(dateTime));
			MRL.setToTime(MRL.getFromTime());
			MRL.setApprovedBy(staff.getUsername());
			MRL.setApprovedTime(Timestamp.valueOf(currentDate));
			MRL.setRequester(staff.getUsername());
			MRL.setSeq(1);
			lrs.add(MRL);
			LeaveRequestModel MTL = new LeaveRequestModel();
			MTL.setEmpId(staff.getEmpId());
			MTL.setYear(year);
			MTL.setMonth(dateTime.getMonthValue());
			MTL.setDay(dateTime.getDayOfMonth());
			MTL.setNote("reset");
			MTL.setSource("balances");
			MTL.setShift("MTL");
			MTL.setStatus(1);
			MTL.setHours(448);
			MTL.setFromTime(Timestamp.valueOf(dateTime));
			MTL.setToTime(MTL.getFromTime());
			MTL.setApprovedBy(staff.getUsername());
			MTL.setApprovedTime(Timestamp.valueOf(currentDate));
			MTL.setRequester(staff.getUsername());
			MTL.setSeq(1);
			lrs.add(MTL);
			LeaveRequestModel PSL = new LeaveRequestModel();
			PSL.setEmpId(staff.getEmpId());
			PSL.setYear(year);
			PSL.setMonth(dateTime.getMonthValue());
			PSL.setDay(dateTime.getDayOfMonth());
			PSL.setNote("reset");
			PSL.setSource("balances");
			PSL.setShift("PSL");
			PSL.setStatus(1);
			PSL.setHours(112);
			PSL.setFromTime(Timestamp.valueOf(dateTime));
			PSL.setToTime(PSL.getFromTime());
			PSL.setApprovedBy(staff.getUsername());
			PSL.setApprovedTime(Timestamp.valueOf(currentDate));
			PSL.setRequester(staff.getUsername());
			PSL.setSeq(1);
			lrs.add(PSL);
			LeaveRequestModel PRL = new LeaveRequestModel();
			PRL.setEmpId(staff.getEmpId());
			PRL.setYear(year);
			PRL.setMonth(dateTime.getMonthValue());
			PRL.setDay(dateTime.getDayOfMonth());
			PRL.setNote("reset");
			PRL.setSource("balances");
			PRL.setShift("PRL");
			PRL.setStatus(1);
			PRL.setHours(56);
			PRL.setFromTime(Timestamp.valueOf(dateTime));
			PRL.setToTime(PRL.getFromTime());
			PRL.setApprovedBy(staff.getUsername());
			PRL.setApprovedTime(Timestamp.valueOf(currentDate));
			PRL.setRequester(staff.getUsername());
			PRL.setSeq(1);
			lrs.add(PRL);
			LeaveRequestModel SKL = new LeaveRequestModel();
			SKL.setEmpId(staff.getEmpId());
			SKL.setYear(year);
			SKL.setMonth(dateTime.getMonthValue());
			SKL.setDay(dateTime.getDayOfMonth());
			SKL.setNote("reset");
			SKL.setSource("balances");
			SKL.setShift("SKL");
			SKL.setStatus(1);
			SKL.setHours(240);
			SKL.setFromTime(Timestamp.valueOf(dateTime));
			SKL.setToTime(SKL.getFromTime());
			SKL.setApprovedBy(staff.getUsername());
			SKL.setApprovedTime(Timestamp.valueOf(currentDate));
			SKL.setRequester(staff.getUsername());
			SKL.setSeq(1);
			lrs.add(SKL);
		} else {
			LeaveRequestModel BREL = new LeaveRequestModel();
			BREL.setEmpId(staff.getEmpId());
			BREL.setYear(year);
			BREL.setMonth(dateTime.getMonthValue());
			BREL.setDay(dateTime.getDayOfMonth());
			BREL.setNote("reset");
			BREL.setSource("balances");
			BREL.setShift("BREL");
			BREL.setStatus(1);
			BREL.setHours(48);
			BREL.setFromTime(Timestamp.valueOf(dateTime));
			BREL.setToTime(BREL.getFromTime());
			BREL.setApprovedBy(staff.getUsername());
			BREL.setApprovedTime(Timestamp.valueOf(currentDate));
			BREL.setRequester(staff.getUsername());
			BREL.setSeq(1);
			lrs.add(BREL);
			LeaveRequestModel BRGL = new LeaveRequestModel();
			BRGL.setEmpId(staff.getEmpId());
			BRGL.setYear(year);
			BRGL.setMonth(dateTime.getMonthValue());
			BRGL.setDay(dateTime.getDayOfMonth());
			BRGL.setNote("reset");
			BRGL.setSource("balances");
			BRGL.setShift("BRGL");
			BRGL.setStatus(1);
			BRGL.setHours(24);
			BRGL.setFromTime(Timestamp.valueOf(dateTime));
			BRGL.setToTime(BRGL.getFromTime());
			BRGL.setApprovedBy(staff.getUsername());
			BRGL.setApprovedTime(Timestamp.valueOf(currentDate));
			BRGL.setRequester(staff.getUsername());
			BRGL.setSeq(1);
			lrs.add(BRGL);
			LeaveRequestModel BRIL = new LeaveRequestModel();
			BRIL.setEmpId(staff.getEmpId());
			BRIL.setYear(year);
			BRIL.setMonth(dateTime.getMonthValue());
			BRIL.setDay(dateTime.getDayOfMonth());
			BRIL.setNote("reset");
			BRIL.setSource("balances");
			BRIL.setShift("BRIL");
			BRIL.setStatus(1);
			BRIL.setHours(64);
			BRIL.setFromTime(Timestamp.valueOf(dateTime));
			BRIL.setToTime(BRIL.getFromTime());
			BRIL.setApprovedBy(staff.getUsername());
			BRIL.setApprovedTime(Timestamp.valueOf(currentDate));
			BRIL.setRequester(staff.getUsername());
			BRIL.setSeq(1);
			lrs.add(BRIL);
			LeaveRequestModel MRL = new LeaveRequestModel();
			MRL.setEmpId(staff.getEmpId());
			MRL.setYear(year);
			MRL.setMonth(dateTime.getMonthValue());
			MRL.setDay(dateTime.getDayOfMonth());
			MRL.setNote("reset");
			MRL.setSource("balances");
			MRL.setShift("MRL");
			MRL.setStatus(1);
			MRL.setHours(64);
			MRL.setFromTime(Timestamp.valueOf(dateTime));
			MRL.setToTime(MRL.getFromTime());
			MRL.setApprovedBy(staff.getUsername());
			MRL.setApprovedTime(Timestamp.valueOf(currentDate));
			MRL.setRequester(staff.getUsername());
			MRL.setSeq(1);
			lrs.add(MRL);
			LeaveRequestModel PRL = new LeaveRequestModel();
			PRL.setEmpId(staff.getEmpId());
			PRL.setYear(year);
			PRL.setMonth(dateTime.getMonthValue());
			PRL.setDay(dateTime.getDayOfMonth());
			PRL.setNote("reset");
			PRL.setSource("balances");
			PRL.setShift("PRL");
			PRL.setStatus(1);
			PRL.setHours(56);
			PRL.setFromTime(Timestamp.valueOf(dateTime));
			PRL.setToTime(PRL.getFromTime());
			PRL.setApprovedBy(staff.getUsername());
			PRL.setApprovedTime(Timestamp.valueOf(currentDate));
			PRL.setRequester(staff.getUsername());
			PRL.setSeq(1);
			lrs.add(PRL);
			LeaveRequestModel PSL = new LeaveRequestModel();
			PSL.setEmpId(staff.getEmpId());
			PSL.setYear(year);
			PSL.setMonth(dateTime.getMonthValue());
			PSL.setDay(dateTime.getDayOfMonth());
			PSL.setNote("reset");
			PSL.setSource("balances");
			PSL.setShift("PSL");
			PSL.setStatus(1);
			PSL.setHours(112);
			PSL.setFromTime(Timestamp.valueOf(dateTime));
			PSL.setToTime(PSL.getFromTime());
			PSL.setApprovedBy(staff.getUsername());
			PSL.setApprovedTime(Timestamp.valueOf(currentDate));
			PSL.setRequester(staff.getUsername());
			PSL.setSeq(1);
			lrs.add(PSL);
			LeaveRequestModel PTL = new LeaveRequestModel();
			PTL.setEmpId(staff.getEmpId());
			PTL.setYear(year);
			PTL.setMonth(dateTime.getMonthValue());
			PTL.setDay(dateTime.getDayOfMonth());
			PTL.setNote("reset");
			PTL.setSource("balances");
			PTL.setShift("PTL");
			PTL.setStatus(1);
			PTL.setHours(56);
			PTL.setFromTime(Timestamp.valueOf(dateTime));
			PTL.setToTime(PTL.getFromTime());
			PTL.setApprovedBy(staff.getUsername());
			PTL.setApprovedTime(Timestamp.valueOf(currentDate));
			PTL.setRequester(staff.getUsername());
			PTL.setSeq(1);
			lrs.add(PTL);
			LeaveRequestModel SKL = new LeaveRequestModel();
			SKL.setEmpId(staff.getEmpId());
			SKL.setYear(year);
			SKL.setMonth(dateTime.getMonthValue());
			SKL.setDay(dateTime.getDayOfMonth());
			SKL.setNote("reset");
			SKL.setSource("balances");
			SKL.setShift("SKL");
			SKL.setStatus(1);
			SKL.setHours(240);
			SKL.setFromTime(Timestamp.valueOf(dateTime));
			SKL.setToTime(SKL.getFromTime());
			SKL.setApprovedBy(staff.getUsername());
			SKL.setApprovedTime(Timestamp.valueOf(currentDate));
			SKL.setRequester(staff.getUsername());
			SKL.setSeq(1);
			lrs.add(SKL);
		}
		leaveRequestRepository.addLeaveRequest(lrs);
		return lrs;
	}

	public List<LeaveRequestModel> resetPDLnewBalances(int year, LocalDateTime currentDate, StaffModel staff,
			String zoneName) {
		List<LeaveRequestModel> lrs = new ArrayList<LeaveRequestModel>();
		LocalDateTime localDateTime = staff.getEntryDate().toLocalDate().atStartOfDay(ZoneId.of(zoneName))
				.toLocalDateTime();
		LocalDateTime updatedLocalDateTime = localDateTime.withYear(year);
		LeaveRequestModel PDL = new LeaveRequestModel();
		PDL.setEmpId(staff.getEmpId());
		PDL.setYear(year);
		PDL.setMonth(staff.getEntryDate().toLocalDate().getMonthValue());
		PDL.setDay(staff.getEntryDate().toLocalDate().getDayOfMonth());
		PDL.setNote("reset");
		PDL.setSource("balances");
		PDL.setShift("PDL");
		PDL.setStatus(1);
		PDL.setHours(112);
		PDL.setFromTime(Timestamp.valueOf(updatedLocalDateTime));
		PDL.setToTime(PDL.getFromTime());
		PDL.setApprovedBy(staff.getUsername());
		PDL.setApprovedTime(Timestamp.valueOf(currentDate));
		PDL.setRequester(staff.getUsername());
		PDL.setSeq(1);
		lrs.add(PDL);
		leaveRequestRepository.addLeaveRequest(lrs);
		return lrs;
	}

	public List<LeaveRequestModel> findLastLeaveRequest(LeaveRequestModel lr) {
		List<LeaveRequestModel> lrs = leaveRequestRepository.findLastLeaveRequest(lr);
		return lrs;
	}

	public List<LeaveRequestModel> findAllLeaveRequestByUser(int empId) {
		List<LeaveRequestModel> lrs = leaveRequestRepository.findAllLeaveRequestByUser(empId);
		return lrs;
	}

	public List<LeaveBalanceModel> calculateBalancesPreMon(int year, int month, User user,
			List<LeaveBalanceModel> lastBalance, Map<String, ShiftModel> shiftModelMap, List<LeaveRequestModel> lrs,
			LocalDateTime currentDate) {
		if (month == 1) {
			sumLeavesYear(lastBalance, currentDate,lrs);
			StaffModel staff = new StaffModel();
			staff.setEmpId(user.getCharacter().getEmpId());
			staff.setUsername(user.getCharacter().getUsername());
			staff.setGender(user.getCharacter().getGender());
			List<LeaveRequestModel> otNewBalance=lrs.stream().filter(x->x.getStatus()==1 && "balances".equals(x.getSource())&&x.getYear()==year&&!"PDL".equals(x.getShift())&&!"MSL".equals(x.getShift())&&x.getHours()>0).toList();
			if(CollectionUtils.isEmpty(otNewBalance)) {
				lrs.addAll(resetOTnewBalances(year, currentDate, staff));
			}
		}
		List<LeaveBalanceModel> preBalances = new ArrayList<LeaveBalanceModel>();
		shiftModelMap.forEach((key, shiftModel) -> {
			if (key.length() > 2 && "L".equals(StringUtils.right(key, 1))) {
				if("F".equals(user.getCharacter().getGender())) {
					if("PTL".equals(key)) {
						return;
					}
				}else {
					if("MTL".equals(key)||"MSL".equals(key)) {
						return;
					}
				}
					LeaveBalanceModel preLb = new LeaveBalanceModel();
					preLb.setCreater(user.getUsername());
					preLb.setEmpId(user.getCharacter().getEmpId());
					preLb.setYear(year);
					preLb.setMonth(month);
					float shiftHours = 0;
					float remainingHours = 0;
					preLb.setShift(key);
					preLb.setDescription(shiftModel.getDescription());
					Optional<LeaveBalanceModel> lbop=lastBalance.stream().filter(x->x.getShift().equals(key)).findFirst();
					if(lbop.isPresent()) {
						LeaveBalanceModel lb=lbop.get();
						if(lb.getYear()==year) {
							shiftHours=lb.getUsedHours();
						}
						remainingHours=lb.getRemainingHours();
					}
						if (!CollectionUtils.isEmpty(lrs)) {
							float shiftHoursChange = (float) lrs.stream()
									.filter(x -> x.getShift().equals(key) && x.getHours() < 0 && x.getStatus() == 1
											&& x.getYear() == year && x.getMonth() == month && !"balances".equals(x.getSource()))
									.mapToDouble(LeaveRequestModel::getHours).sum();
							float cancelHours = (float) lrs.stream()
									.filter(x -> x.getShift().equals(key) && x.getHours() < 0 && x.getStatus() == 1
											&& x.getYear() == year && x.getMonth() == month && "balances".equals(x.getSource()))
									.mapToDouble(LeaveRequestModel::getHours).sum();
							float plusHours = (float) lrs.stream()
									.filter(x -> x.getShift().equals(key) && x.getHours() > 0 && x.getStatus() == 1
											&& x.getYear() == year && x.getMonth() == month)
									.mapToDouble(LeaveRequestModel::getHours).sum();
							shiftHours=shiftHours + shiftHoursChange;
							remainingHours=remainingHours + plusHours + shiftHoursChange+cancelHours;
						}
					
					preLb.setUsedHours(shiftHours);
					preLb.setRemainingHours(remainingHours);
					preBalances.add(preLb);
				
				
			}
		});
	
		if ("F".equals(user.getCharacter().getGender())) {
			sumMSLMon(preBalances, currentDate,lrs);
		}
		leaveBalanceRepository.addLeaveBalance(preBalances);
		return preBalances;
	}

	public List<LeaveBalanceModel> calculateBalancesPreMonByRequest(int year, int month, User user,
			List<LeaveBalanceModel> lastBalance, Map<String, ShiftModel> shiftModelMap, List<LeaveRequestModel> lrs,
			LocalDateTime currentDate) {
		List<LeaveBalanceModel> preBalances;
		if (CollectionUtils.isEmpty(lastBalance)) {//也就算一个月
			preBalances = new ArrayList<LeaveBalanceModel>();
			shiftModelMap.forEach((key, shiftModel) -> {
				if (key.length() > 2 && "L".equals(StringUtils.right(key, 1))) {
					if("M".equals(user.getCharacter().getGender())) {
						if("MTL".equals(key)||"MSL".equals(key)) {
							return;
						}
					}else {
						if("PTL".equals(key)) {
							return;
						}
					}
					LeaveBalanceModel preLb = new LeaveBalanceModel();
					preLb.setCreater(user.getUsername());
					preLb.setEmpId(user.getCharacter().getEmpId());
					preLb.setYear(year);
					preLb.setMonth(month);
					preLb.setShift(key);
					preLb.setDescription(shiftModel.getDescription());
					float shiftHours = 0;
					float cancelHours = 0;
					float plusHours = 0;

					List<LeaveRequestModel> lrsn = lrs.stream()
							.filter(x -> x.getYear() == year && x.getMonth() == month && x.getShift().equals(key))
							.toList();
					if (!CollectionUtils.isEmpty(lrsn)) {
						shiftHours = (float) lrsn.stream().filter(x -> x.getHours() < 0 && x.getStatus() == 1 && !"balances".equals(x.getSource()))
								.mapToDouble(LeaveRequestModel::getHours).sum();
						cancelHours=(float) lrsn.stream().filter(x -> x.getHours() < 0 && x.getStatus() == 1 && "balances".equals(x.getSource()))
								.mapToDouble(LeaveRequestModel::getHours).sum();
						plusHours = (float) lrsn.stream().filter(x -> x.getHours() > 0 && x.getStatus() == 1)
								.mapToDouble(LeaveRequestModel::getHours).sum();
					}

					preLb.setUsedHours(shiftHours);
					preLb.setRemainingHours(plusHours + shiftHours+cancelHours);

					preBalances.add(preLb);
				}
			});
			leaveBalanceRepository.addLeaveBalance(preBalances);
		} else {
			preBalances = calculateBalancesPreMon(year, month, user, lastBalance, shiftModelMap, lrs, currentDate);

		}
		return preBalances;
	}

	public List<LeaveBalanceModel> findLastLeaveBalanceByMon(LeaveBalanceModel lb) {
		return leaveBalanceRepository.findLastLeaveBalanceByMon(lb);
	}

	public List<LeaveBalanceModel> findLastLeaveBalances(LeaveBalanceModel lb) {
		return leaveBalanceRepository.findLastLeaveBalance(lb);
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
	public int deleteLeaveSumRequestByMon(LeaveRequestModel model) {
		return leaveRequestRepository.deleteLeaveSumRequestByMon(model);
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
		if (!CollectionUtils.isEmpty(leaveBalances)) {
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
		if (!CollectionUtils.isEmpty(leaveRequests)) {
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
					cellFromDatetime.setCellValue(
							Timestamp.valueOf(lr.getFromTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellFromDatetime.setCellValue(lr.getFromTime());
				}
				cellFromDatetime.setCellStyle(dateTimeCellStyle);
				Cell cellToDatetime = row2.createCell(7);
				if (lr.getToTime() != null) {
					cellToDatetime.setCellValue(
							Timestamp.valueOf(lr.getToTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
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
