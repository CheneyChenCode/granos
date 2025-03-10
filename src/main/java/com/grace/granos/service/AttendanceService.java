package com.grace.granos.service;

import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.grace.granos.dao.AttendanceRepository;
import com.grace.granos.dao.HolidayRepository;
import com.grace.granos.dao.LeaveBalanceRepository;
import com.grace.granos.dao.LeaveRequestRepository;
import com.grace.granos.dao.PayCodeRepository;
import com.grace.granos.model.AttendanceModel;
import com.grace.granos.model.CustomException;
import com.grace.granos.model.HolidaysModel;
import com.grace.granos.model.LeaveBalanceModel;
import com.grace.granos.model.LeaveRequestModel;
import com.grace.granos.model.PayCodeModel;
import com.grace.granos.model.ShiftModel;
import com.grace.granos.model.User;

@Service
@PropertySource("classpath:application.properties") // 指定属性文件的位置
public class AttendanceService {
	private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
	@Autowired
	private AttendanceRepository attendanceRepository;
	@Autowired
	private ShiftService shiftService;
	@Autowired
	private HolidayRepository holidayRepository;
	@Autowired
	private LeaveBalanceRepository leaveBalanceRepository;
	@Autowired
	private LeaveRequestRepository leaveRequestRepository;
	@Autowired
	private PayrollService payrollService;

	@Value("${spring.time.zone}")
	private String zoneName;
	private ZoneId timeZone;

	@PostConstruct
	private void init() {
		// 确保在属性注入完成后进行初始化
		this.timeZone = ZoneId.of(zoneName);
	}

	public List<AttendanceModel> checkAttendanceForPayByUserMon(int year, int month, int empid) {
		AttendanceModel att = new AttendanceModel();
		att.setEmpId(empid);
		att.setYear(year);
		att.setMonth(month);
		return attendanceRepository.checkAttendanceForPayByUserMon(att);
	}

	public void addAttendances(List<AttendanceModel> models) {
		attendanceRepository.addAttendance(models);
	}

	public int deleteAttendances(AttendanceModel attendance) {
		return attendanceRepository.deleteAttendanceByUserMon(attendance);
	}

	public List<AttendanceModel> findAttendanceByUserMon(int year, int month, int empid) {
		AttendanceModel attendance = new AttendanceModel();
		attendance.setYear(year);
		attendance.setMonth(month);
		attendance.setEmpId(empid);
		return attendanceRepository.findAttendanceByUserMon(attendance);
	}

	public List<AttendanceModel> parseExcel(InputStream fileInputStream, User user) throws CustomException {
		List<AttendanceModel> attendanceDatas = new ArrayList<AttendanceModel>();
		int rowIn=0;
		try {
			Workbook workbook = WorkbookFactory.create(fileInputStream);

			Iterator<Sheet> sheets = workbook.sheetIterator();
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = null;
			while (sheets.hasNext()) {
				Sheet sheetTemp = sheets.next();
				// 判断当前 Sheet 的名称是否为“範本”
				if (!"範本".equals(sheetTemp.getSheetName())) {
					// 如果不是“範本”，将其添加到临时列表中
					sheet = sheetTemp;
				}
			}
			if (sheet == null) {
				throw new CustomException("Workbook doesn't contain any available sheets.", 2007);
			}
			Row titleRow = sheet.getRow(3);
			if (titleRow == null) {
				throw new CustomException("The third row is empty.", 2008);
			}
			// 按顺序存储关键字
			String[] keywords = { "Shifts", "Start Time", "Week", "Start Date", "Arrival Time", "End Date", "End Time",
					"Actual work hour", "Total", "Start time", "End time", "Actual Over Time work hour", "Total" };

			for (int i = 1; i <= 12; i++) {
				Cell cell = titleRow.getCell(i);
				String cellValue = getCellValue(formulaEvaluator, cell, String.class); // 假设单元格中是文本内容
				// 去除单元格中的换行符和空格
				cellValue = cellValue.replaceAll("\\s|\\r|\\n", "");
				// 检查单元格的文本内容是否包含当前关键字
				String keywordValue = keywords[i - 1].replaceAll("\\s|\\r|\\n", "");
				if (!cellValue.contains(keywordValue)) {
					// 如果不包含当前关键字，可以根据实际情况进行处理，比如抛出异常或者打印错误消息
					throw new CustomException("content does not match the expected keyword:" + keywords[i - 1], 2009);
				}
			}
			Calendar calendar = Calendar.getInstance();
			List<HolidaysModel> holidays = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			int seq = 0;
			Date datePre = null;
			AttendanceModel preMonWdAtt = null;
			AttendanceModel preMonWdAtt2 = null;
			int accumulateWorkDay = 0;
			int accumulateWorkDayInPeriod = 0;
			int fixedDayOffInPeriod = 0;
			int flexibleDayOffInPeriod = 0;
			int conversionDayOffCurrentMonth = 0;
			int shiftCurrentMonthIndex = 0;
			int period = 0;
			int countDay = 0;
			float remainTaxHours = 46;
			float countDayHours = 0;
			float countDayPaidHours = 0;
			String shiftCurrentMonth = null;
			String shiftNamePre = null;
			Map<String, ShiftModel> shiftModelMap = shiftService.getshiftModelMap();
			List<LeaveBalanceModel> balances = null;
			List<LeaveRequestModel> leacesRequest = null;
			Map<String, LeaveBalanceModel> leaveBalanceModelMap = null;
			Map<String, Map<Integer, PayCodeModel>> leavePayCodeMap = payrollService.getLeavePayCodeMap();
			boolean preOt=false;
			// 从第四行开始循环遍历每一行
			for (int rowIndex = 4; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				rowIn=rowIndex;
				Row row = sheet.getRow(rowIndex);
				if (row == null) {
					continue;
				}
				Date date = getCellValue(formulaEvaluator, row.getCell(4), Date.class);
				if (date == null) {
					if (isMergedCell(sheet, rowIndex, 4) && datePre != null) {
						date = datePre;
					} else {
						break;
					}
				}
				datePre = date;
				LeaveBalanceModel lb = new LeaveBalanceModel();
				LeaveRequestModel lr = new LeaveRequestModel();
				AttendanceModel at = new AttendanceModel();
				attendanceDatas.add(at);
				at.setStatus(1);
				at.setEmpId(user.getCharacter().getEmpId());
				at.setCreater(user.getUsername());
				// 使用 Calendar 类来获取日和月
				calendar.setTime(date);
				int year = calendar.get(Calendar.YEAR);
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				int month = calendar.get(Calendar.MONTH) + 1; // 月份是从 0 开始的，所以要加 1
				lb.setEmpId(user.getCharacter().getEmpId());
				lr.setEmpId(user.getCharacter().getEmpId());
				lr.setYear(year);
				lr.setMonth(month);
				if (month == 1) {
					lb.setYear(year - 1);
					lb.setMonth(12);
				} else {
					lb.setYear(year);
					lb.setMonth(month - 1);
				}
				at.setYear(year);
				at.setDay(day);
				at.setMonth(month);
				// 获取星期几（1 表示星期日，2 表示星期一，以此类推）
				int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
				at.setWeek(dayOfWeek);
				// Sequence of period
				Double excelPeriod = getCellValue(formulaEvaluator, row.getCell(17), Double.class);
				if (rowIndex == 4) {
					preMonWdAtt = attendanceRepository.findLastAttByUserMon(at);
					if (preMonWdAtt == null && excelPeriod == null) {
						throw new CustomException("There are no attendance records in the previous month.", 2010);
					}
					if (preMonWdAtt != null) {
						datePre = Date
								.from(LocalDate.of(preMonWdAtt.getYear(), preMonWdAtt.getMonth(), preMonWdAtt.getDay())
										.atStartOfDay(timeZone).toInstant());
						period = preMonWdAtt.getPeriod();
					}
					shiftCurrentMonth = attendanceRepository.findLastWdShiftByUserMon(at);
					accumulateWorkDay = attendanceRepository.findLastAccumulateWdByUserMon(at);
					if(period<7) {
						List<AttendanceModel> accumulaAtts = attendanceRepository.findLastAccumulateWdPeByUserMon(at);
						if(!CollectionUtils.isEmpty(accumulaAtts)) {
							int aDay=0;
							for(AttendanceModel a:accumulaAtts) {
								if(aDay!=a.getDay()) {
									if(a.getWorkHours()>0) {
										accumulateWorkDayInPeriod=accumulateWorkDayInPeriod+1;
										aDay=a.getDay();
									}else if(a.getShift().length()>2 && "L".equals(StringUtils.right(a.getShift(), 1)) && a.getPaidLeave() >0) {
										accumulateWorkDayInPeriod=accumulateWorkDayInPeriod+1;
										aDay=a.getDay();
									}else if(a.getShift().length()>2 && !"DCF".equals(a.getShift()) && a.getDayCode() != 3 && a.getDayCode() != 4) {
										accumulateWorkDayInPeriod = accumulateWorkDayInPeriod + 1;
										aDay=a.getDay();
									}else if(a.getShift().length()==2) {
										accumulateWorkDayInPeriod = accumulateWorkDayInPeriod + 1;
										aDay=a.getDay();
									}
								}
							}
						}
					}
					fixedDayOffInPeriod = attendanceRepository.findLastfixedDayOffInPeriod(at);
					flexibleDayOffInPeriod = attendanceRepository.findLastflexibleDayOffInPeriod(at);
					balances = leaveBalanceRepository.findLeaveBalanceByUserYearMon(lb);
					leacesRequest=leaveRequestRepository.findLeaveRequestByUserMon(lr);
					if (CollectionUtils.isEmpty(balances)) {
						//throw new CustomException("There are no leave balances in the previous month.", 2011);
					} else {
						leaveBalanceModelMap = balances.stream().collect(
								Collectors.toMap(LeaveBalanceModel::getShift, leaveBalanceModel -> leaveBalanceModel));
					}
				}

				seq = 1;
				// 将日期和时间字符串转换为 LocalDate 和 LocalTime 对象
				LocalDate startDate = date.toInstant().atZone(timeZone).toLocalDate();
				// arrival time
				Date arrivalTime = getCellValue(formulaEvaluator, row.getCell(5), Date.class);
				if (arrivalTime != null) {
					// 解析时间字符串为 LocalTime 对象
					LocalTime arrivalLocalTime = arrivalTime.toInstant().atZone(timeZone).toLocalTime();
					at.setArrivalDatetime(Timestamp.valueOf(LocalDateTime.of(startDate, arrivalLocalTime)));
					// 將 Timestamp 轉換為 LocalDateTime
					if (preMonWdAtt != null && preMonWdAtt.getLeaveDatetime() != null) {
						LocalDateTime dateTime1 = at.getArrivalDatetime().toLocalDateTime().withSecond(0).withNano(0);
						LocalDateTime dateTime2 = preMonWdAtt.getLeaveDatetime().toLocalDateTime().withSecond(0)
								.withNano(0);
						if (dateTime1.equals(dateTime2)||day==preMonWdAtt.getDay()) {
							seq = preMonWdAtt.getSeq() + 1;
						}
					}
				}

				if (seq > 1) {
					at.setYear(preMonWdAtt.getYear());
					at.setDay(preMonWdAtt.getDay());
					at.setMonth(preMonWdAtt.getMonth());
					at.setWeek(preMonWdAtt.getWeek());
				}

				at.setSeq(seq);

				if (excelPeriod != null && seq == 1) {
					period = (int) Math.floor(excelPeriod);
					if (period <= 1) {
						fixedDayOffInPeriod = 0;
						flexibleDayOffInPeriod = 0;
						accumulateWorkDayInPeriod = 0;
						conversionDayOffCurrentMonth=0;
					}
				} else if (seq == 1) {
					if (period == 7 || period ==0) {
						fixedDayOffInPeriod = 0;
						flexibleDayOffInPeriod = 0;
						accumulateWorkDayInPeriod = 0;
						conversionDayOffCurrentMonth=0;
						period = 1;
					} else {
						period = period + 1;
					}
				}
				at.setPeriod(period);
				ShiftModel shiftCurrentMonthShift = null;
				if (!StringUtil.isBlank(shiftCurrentMonth)) {
					shiftCurrentMonthShift = shiftModelMap.get(shiftCurrentMonth);
				} else {
					for (int rowInside = rowIndex + 1; rowInside <= sheet.getLastRowNum(); rowInside++) {
						Row rowInsider = sheet.getRow(rowInside);
						if (rowInsider == null) {
							break;
						}
						String shiftName = getCellValue(formulaEvaluator, rowInsider.getCell(1), String.class);
						if(StringUtils.isBlank(shiftName)) {
							continue;
						}
						if (shiftName.length()==2&&!"OT".equals(shiftName)) {
							shiftCurrentMonthShift = shiftModelMap.get(shiftName);
							if (shiftCurrentMonthShift == null) {
								logger.error(shiftName);
								throw new CustomException(shiftName + " shift doesn't exist",2022);
							}
							break;
						}
					}
				}
				at.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(date.toInstant().atZone(timeZone).toLocalDate(),
						shiftCurrentMonthShift.getStartTime().toInstant().atZone(timeZone).toLocalTime())));
				at.setEndDatetime(Timestamp.valueOf(at.getStartDatetime().toLocalDateTime().plusHours(
						(long) (shiftCurrentMonthShift.getBaseHours() + shiftCurrentMonthShift.getRestHours()))));

				// reason
				String reason = getCellValue(formulaEvaluator, row.getCell(14), String.class);
				at.setReason(reason);
				// approval
				String approval = getCellValue(formulaEvaluator, row.getCell(15), String.class);
				if(StringUtils.isNotEmpty(approval)){
					if(approval.contains("N") || approval.contains("No") || approval.contains("F")
							|| approval.contains("False")) {
						at.setApproval("N");
					}else if(approval.contains("Y") || approval.contains("Yes") || approval.contains("T")
							|| approval.contains("True")){
							at.setApproval("Y");
					}
				}			
				// note
				String note = getCellValue(formulaEvaluator, row.getCell(16), String.class);
				at.setNote(note);
				// date code
				int dayCode = 0;
				int endDayCode = 1;
				boolean inHoliday=false;
				boolean endInHoliday=false;
				String dayDescription = "";
				// holiday
				if (holidays == null) {
					holidays = holidayRepository.findHolidaysByYearMonthWithOutWeekEnd(at.getYear(), at.getMonth());
				}
				if (holidays != null) {
					String start = at.getYear() + "-" + StringUtils.leftPad(String.valueOf(at.getMonth()), 2, '0') + "-"
							+ StringUtils.leftPad(String.valueOf(at.getDay()), 2, '0');
					Optional<HolidaysModel> holiday = holidays.stream()
							.filter(x -> start.equals(dateFormat.format(x.getDate()))).findFirst();
					// 检查是否找到匹配的 CalendarEvent 对象
					if (holiday.isPresent()) {
						inHoliday=true;
						dayCode = holiday.get().getDayCode();
						dayDescription = holiday.get().getDescription();
					} else {
						dayCode = 1;
					}
				}
				// leave time
				Date leaveTime = getCellValue(formulaEvaluator, row.getCell(7), Date.class);
				if (leaveTime != null) {
					LocalTime leaveTimeJudge = leaveTime.toInstant().atZone(timeZone).toLocalTime();
					if(arrivalTime==null) {
						setAbnormalAttendance(at, 2020);
						continue;
					}
					if (leaveTimeJudge.isBefore(at.getArrivalDatetime().toLocalDateTime().toLocalTime())) {
						at.setLeaveDatetime(Timestamp.valueOf(LocalDateTime.of(startDate.plusDays(1), leaveTimeJudge)));
					} else {
						at.setLeaveDatetime(Timestamp.valueOf(LocalDateTime.of(startDate, leaveTimeJudge)));
					}
				}
				// shift
				boolean ot=false;
				String shiftName = getCellValue(formulaEvaluator, row.getCell(1), String.class);
				if (StringUtil.isBlank(shiftName) && isMergedCell(sheet, rowIndex, 1) && shiftNamePre != null) {
					shiftName = shiftNamePre;
				}
				if("OT".equals(shiftName)) {
					ot=true;
					if (seq > 1) {
						shiftName=shiftCurrentMonth;
					}else {
						for (int rowInside = rowIndex + 1; rowInside <= sheet.getLastRowNum(); rowInside++) {
							Row rowInsider = sheet.getRow(rowInside);
							if (rowInsider == null) {
								shiftName=shiftCurrentMonth;
								break;
							}
							shiftName = getCellValue(formulaEvaluator, rowInsider.getCell(1), String.class);
							if(StringUtils.isBlank(shiftName)) {
								if(rowInside==sheet.getLastRowNum()) {
									shiftName=shiftCurrentMonth;
									break;
								}else {
									continue;
								}
							}else if (shiftName.length()==2&&!"OT".equals(shiftName)) {
								if (shiftModelMap.get(shiftName) == null) {
									logger.error(shiftName);
									throw new CustomException(shiftName + " shift doesn't exist",2022);
								}
								if(!shiftName.equals(shiftCurrentMonth)) {
									ShiftModel shift1 = shiftModelMap.get(shiftCurrentMonth.trim());
									ShiftModel shift2 = shiftModelMap.get(shiftName.trim());
									long comparisonOt1 = ChronoUnit.SECONDS.between(shift1.getStartTime().toLocalDateTime().toLocalTime(), at
											.getArrivalDatetime().toLocalDateTime().toLocalTime());
									long comparisonOt2 = ChronoUnit.SECONDS.between(shift2.getStartTime().toLocalDateTime().toLocalTime(), at
											.getArrivalDatetime().toLocalDateTime().toLocalTime());
									// 比较哪个时间更接近 at
									if (Math.abs(comparisonOt1) <= Math.abs(comparisonOt2)) {
										shiftName=shiftCurrentMonth;
									} 
								}
								break;
							}
						}
					}
					if (arrivalTime != null) {
						LocalTime otTimeJudge = arrivalTime.toInstant().atZone(timeZone).toLocalTime();
						at.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(date.toInstant().atZone(timeZone).toLocalDate(),otTimeJudge)));
						at.setEndDatetime(at.getLeaveDatetime());
					}					
				}
				if (StringUtil.isBlank(shiftName)) {
					if(dayCode==2) {
						shiftName = "NTF";
					}else if(dayCode==5) {
						shiftName = "DSF";
					}else if(dayCode==1 && inHoliday) {
							shiftName = "TBF";
					}else if (fixedDayOffInPeriod == 0) {
//					if (dayCode == 2) {
//						//at.setCompTime(8);
//						//at.setCompReason(dayDescription + " VS " + "fixed day off");
//						//fixedDayOffInPeriod = 1;
//						at.setShift("DXF");
//					} else {
//						//dayCode = 4;
//						//fixedDayOffInPeriod = 1;
//						//at.setShift("DXF");
//					}
						shiftName = "DXF";

					} else if (flexibleDayOffInPeriod == 0) {
//					if (dayCode == 2) {
//						at.setCompTime(8);
//						at.setCompReason(dayDescription + " VS " + "flexible day off");
//						flexibleDayOffInPeriod = 1;
//						at.setShift("DLF");
//					} else {
//						dayCode = 3;
//						flexibleDayOffInPeriod = 1;
//						at.setShift("DLF");
//					}
						shiftName = "DLF";
						
					} else {
						shiftName = checkDayoffName(inHoliday, dayCode);
						if(StringUtils.isBlank(shiftName)) {
							setAbnormalAttendance(at, 2012);
							continue;
						}
						
					}
					// at.setDayCode(dayCode);
					// accumulateWorkDay = 0;
					// continue;
				}

				ShiftModel shift = shiftModelMap.get(shiftName.trim());
				if (shift == null) {
					logger.error(shiftName);
					throw new CustomException(date.toString() + " shift doesn't exist",2022);
				}
				at.setShift(shift.getName());
				if (shiftName.length() > 2) {
					switch (shiftName) {
					case "DXF":
						if (fixedDayOffInPeriod >= 1) {
							setAbnormalAttendance(at, 2013);
							continue;
						}
						if (dayCode == 2) {
							at.setShift("NTF");
							at.setCompTime(8);
							at.setCompReason(dayDescription + " VS " + "fixed day off");
						} else {
							dayCode = 4;
							fixedDayOffInPeriod = 1;
						}
						// at.setDayCode(dayCode);
						// accumulateWorkDay = 0;
						// continue;
						break;
					case "DLF":
						if (flexibleDayOffInPeriod >= 1) {
							setAbnormalAttendance(at, 2014);
							continue;
						}
						if (dayCode == 2) {
							at.setShift("NTF");
							at.setCompTime(8);
							at.setCompReason(dayDescription + " VS " + "flexible day off");
						} else {
							dayCode = 3;
							flexibleDayOffInPeriod = 1;
						}
						// at.setDayCode(dayCode);
						// accumulateWorkDay = 0;
						// continue;
						break;
					case "DCF":
						if (conversionDayOffCurrentMonth >= 2) {
							setAbnormalAttendance(at, 2015);
							continue;
						}
						conversionDayOffCurrentMonth =conversionDayOffCurrentMonth+1;
						if (dayCode == 2) {
							at.setShift("NTF");
						}

						// at.setDayCode(dayCode);
						// accumulateWorkDay = 0;
						// continue;
						break;
					case "DSF":
						if (dayCode == 2) {
							at.setShift("NTF");
						}
						break;
					case "TBF":
						if (dayCode == 2) {
							at.setShift("NTF");
						}else if(dayCode == 5){
							at.setShift("DSF");
						}
						break;
					default:
						// at.setDayCode(dayCode);

					}
				}

				if (StringUtil.isNotBlank(shiftCurrentMonth)) {
					if (shiftName.length() == 2 && !shiftCurrentMonth.substring(0, 1).equals(shiftName.substring(0, 1))
							&& conversionDayOffCurrentMonth < 2) {
						int attPeriod=at.getPeriod();
						for (int i = attendanceDatas.size() - 2; i > shiftCurrentMonthIndex; i--) {
							AttendanceModel temAt = attendanceDatas.get(i);
							attPeriod=attPeriod-1;
							if (StringUtil.isBlank(temAt.getShift())){
								if (temAt.getStatus() == 2
										&& conversionDayOffCurrentMonth < 2) {
									temAt.setShift("DCF");
									temAt.setStatus(1);
									temAt.setPaidLeave(shift.getBaseHours());
									temAt.setReason(null);
									temAt.setAbnormalCode(null);
									conversionDayOffCurrentMonth = conversionDayOffCurrentMonth + 1;
									}
							}else {
								if(temAt.getPeriod()==attPeriod && temAt.getPeriod()!=0) {
									if("DLF".equals(temAt.getShift())&& conversionDayOffCurrentMonth < 2) {
										temAt.setShift("DCF");
										flexibleDayOffInPeriod=flexibleDayOffInPeriod-1;
										temAt.setDayCode(1);
										conversionDayOffCurrentMonth = conversionDayOffCurrentMonth + 1;
									}else if("DXF".equals(temAt.getShift())&& conversionDayOffCurrentMonth < 2) {
										temAt.setShift("DCF");
										fixedDayOffInPeriod=fixedDayOffInPeriod-1;
										temAt.setDayCode(1);
										conversionDayOffCurrentMonth = conversionDayOffCurrentMonth + 1;
									}
								}else {
									attPeriod=0;
								}
							}
							if (i == attendanceDatas.size() - 2&&!StringUtil.isBlank(temAt.getShift())&&temAt.getShift().length()>2) {
								temAt.setStartDatetime(Timestamp.valueOf(
										LocalDateTime.of(temAt.getStartDatetime().toLocalDateTime().toLocalDate(),
												shift.getStartTime().toInstant().atZone(timeZone).toLocalTime())));
								temAt.setEndDatetime(Timestamp.valueOf(temAt.getStartDatetime().toLocalDateTime()
										.plusHours((long) (shift.getBaseHours() + shift.getRestHours()))));
							} 
						}
					}
				}
				if (shiftName.length() == 2) {
					shiftCurrentMonth = shiftName;
					shiftCurrentMonthShift=shiftModelMap.get(shiftCurrentMonth);
					shiftCurrentMonthIndex = rowIndex - 4;
				}

				if (shiftName.length() > 2) {
					at.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(startDate,
							shiftCurrentMonthShift.getStartTime().toInstant().atZone(timeZone).toLocalTime())));
					at.setEndDatetime(Timestamp.valueOf(at.getStartDatetime().toLocalDateTime().plusHours(
							(long) (shiftCurrentMonthShift.getBaseHours() + shiftCurrentMonthShift.getRestHours()))));
					// seq 2
					if (seq > 1) {
						Optional<AttendanceModel> at1 = attendanceDatas.stream()
								.filter(a -> a.getDay() == at.getDay() && a.getSeq() == at.getSeq() - 1).findFirst();
						if (at1.isPresent()) {
							at1.get().setEndDatetime(at.getArrivalDatetime());
							at.setStartDatetime(at1.get().getEndDatetime());
							LocalDateTime oStart = LocalDateTime.of(
									LocalDate.of(at.getYear(), at.getMonth(), at.getDay()),
									shiftCurrentMonthShift.getStartTime().toInstant().atZone(timeZone).toLocalTime());
							at.setEndDatetime(
									Timestamp.valueOf(oStart.plusHours((long) (shiftCurrentMonthShift.getBaseHours()
											+ shiftCurrentMonthShift.getRestHours()))));
						}
					}
				} else if(!ot) {
					// seq 2
					if (seq > 1 ) {
						Optional<AttendanceModel> atp = attendanceDatas.stream()
								.filter(a -> a.getDay() == at.getDay() && a.getSeq() == at.getSeq() - 1).findFirst();
						if (atp.isPresent()&&!preOt) {
							AttendanceModel at1 = null;
							if(at.getSeq() - 1==1) {
								at1=atp.get();
							}else {
								Optional<AttendanceModel> atpp = attendanceDatas.stream()
										.filter(a -> a.getDay() == at.getDay() && a.getSeq() == 1).findFirst();
								if(atpp.isPresent()) {
									at1=atpp.get();
								}
							}

							atp.get().setEndDatetime(at.getArrivalDatetime());
							at.setStartDatetime(atp.get().getEndDatetime());
							if(at1.getShift().length()==2) {
								at.setEndDatetime(Timestamp
										.valueOf(at1.getStartDatetime().toLocalDateTime().plusHours((long) (shift.getBaseHours() + shift.getRestHours()))));
							}
						}
					}else {
						at.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(startDate,
								shift.getStartTime().toInstant().atZone(timeZone).toLocalTime())));
						at.setEndDatetime(Timestamp.valueOf(at.getStartDatetime().toLocalDateTime()
								.plusHours((long) (shift.getBaseHours() + shift.getRestHours()))));
					}
					
				}

				long arriveLate = 0;
				if (at.getArrivalDatetime() != null) {
					// 比较两个时间字符串
					long comparisonResult = ChronoUnit.SECONDS.between(at.getStartDatetime().toLocalDateTime(), at
							.getArrivalDatetime().toLocalDateTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES));
					if (comparisonResult > 0) {
						at.setStartDatetime(at.getArrivalDatetime());
						arriveLate = comparisonResult;
						at.setEndDatetime(
								Timestamp.valueOf(at.getEndDatetime().toLocalDateTime().plusSeconds(arriveLate)));
						if (shiftName.length() == 2 && comparisonResult > 3600) {
							//setAbnormalAttendance(at, 2016);
						}
					}
				}
				AttendanceModel at2 = null;
				float totalWorkHours = 0;
				float paidLeaveHours = 0;
				if (at.getLeaveDatetime() != null) {
					// 轉換為 LocalDate 來忽略時間部分
					LocalDate arrDay = at.getStartDatetime().toLocalDateTime().toLocalDate();
					LocalDate endDay = at.getLeaveDatetime().toLocalDateTime().toLocalDate();

					// 計算相差的天數
					long daysBetween = ChronoUnit.DAYS.between(arrDay, endDay);
					if (daysBetween >= 1) {
						if (holidays != null) {
							String end = at.getEndDatetime().toLocalDateTime().getYear() + "-" + StringUtils.leftPad(
									String.valueOf(at.getEndDatetime().toLocalDateTime().getMonthValue()), 2, '0') + "-"
									+ StringUtils.leftPad(
											String.valueOf(at.getEndDatetime().toLocalDateTime().getDayOfMonth()), 2,
											'0');
							Optional<HolidaysModel> holiday = holidays.stream()
									.filter(x -> end.equals(dateFormat.format(x.getDate()))).findFirst();
							// 检查是否找到匹配的 CalendarEvent 对象
							if (holiday.isPresent()) {
								endDayCode = holiday.get().getDayCode();
								endInHoliday=true;
							}
						}
						if (dayCode != endDayCode && (dayCode == 5 || endDayCode == 5)) {
							if(endDayCode!=5) {
								endDayCode=1;
							}
							at2 = new AttendanceModel();
							attendanceDatas.add(at2);
							seq = seq + 1;
							at2.setSeq(seq);
							at2.setStatus(at.getStatus());
							at2.setEmpId(at.getEmpId());
							at2.setCreater(at.getCreater());
							at2.setYear(at.getYear());
							at2.setDay(at.getDay());
							at2.setMonth(at.getMonth());
							at2.setPeriod(at.getPeriod());
							at2.setWeek(at.getWeek());
							at2.setReason(at.getReason());
							at2.setApproval(at.getApproval());
							at2.setNote(at.getNote());
							// at2.setCompTime(at.getCompTime());
							// at2.setCompReason(at.getCompReason());
							at2.setDayCode(endDayCode);
							at2.setShift(at.getShift());

							at2.setEndDatetime(at.getEndDatetime());

							at2.setLeaveDatetime(at.getLeaveDatetime());

							at.setLeaveDatetime(Timestamp.valueOf(LocalDateTime
									.of(at.getLeaveDatetime().toLocalDateTime().toLocalDate(), LocalTime.of(00, 00, 00))
									.plusSeconds(arriveLate)));
							at2.setArrivalDatetime(at.getLeaveDatetime());
							at2.setStartDatetime(at.getLeaveDatetime());
							at.setEndDatetime(at.getLeaveDatetime());
							float workHours2 = calculateWork(formulaEvaluator, row, at2, shift,shiftCurrentMonthShift,dayCode);
							// paid leave
							if (at2.getShift().length() > 2) {
								paidLeaveHours = paidLeaveHours + workHours2;
								at2.setPaidLeave(workHours2);
								checkLeaveBalances(leaveBalanceModelMap, at2, workHours2,leacesRequest,endInHoliday,endDayCode,leavePayCodeMap);
							} else {
								if(workHours2>0) {
									at2.setWorkHours(workHours2);
									totalWorkHours = totalWorkHours + workHours2;
								}else {
									if(endDayCode==5) {
										at2.setShift("DSF");
										at2.setLeaveDatetime(at2.getEndDatetime());
										at2.setWorkHours(calculateWork(formulaEvaluator, row, at2, shift,shiftCurrentMonthShift,endDayCode));
										if(at2.getStatus()==2&&"2020".equals(at2.getAbnormalCode())) {
											at2.setStatus(1);
											at2.setAbnormalCode(null);
										}
									}
								}
							}
						}
					}
				}

				float workHours = calculateWork(formulaEvaluator, row, at, shift,shiftCurrentMonthShift,dayCode);
				// paid leave
				if (shiftName.length() > 2) {
					paidLeaveHours = paidLeaveHours + workHours;
					at.setPaidLeave(workHours);
					checkLeaveBalances(leaveBalanceModelMap, at, workHours,leacesRequest,inHoliday,dayCode,leavePayCodeMap);
				} else {
					at.setWorkHours(workHours);
					totalWorkHours = totalWorkHours + workHours;
				}
				// date code
				if (countDay != at.getDay()) {
					countDayHours=totalWorkHours+paidLeaveHours;
					countDayPaidHours=paidLeaveHours;
					countDay = at.getDay();
					preMonWdAtt = at;
					preMonWdAtt2=at2;
					preOt=ot;
					if(countDayHours<shiftCurrentMonthShift.getBaseHours()) {
						judgeEnoughtHours(countDayHours,at, shiftCurrentMonthShift, dayCode, endDayCode, inHoliday,
								endInHoliday, at2);
					}

					if (totalWorkHours > 0) {
						accumulateWorkDay = accumulateWorkDay + 1;
						accumulateWorkDayInPeriod = accumulateWorkDayInPeriod + 1;
					}else if(shiftName.length()>2 && "L".equals(StringUtils.right(shiftName, 1)) && paidLeaveHours >0) {
						//accumulateWorkDay = 0;
						accumulateWorkDayInPeriod = accumulateWorkDayInPeriod + 1;
//					}else if(shiftName.length()>2 && !"DCF".equals(shiftName)&& dayCode == 1) {
//						accumulateWorkDayInPeriod = accumulateWorkDayInPeriod + 1;
					}else if(shiftName.length()==2) {
						accumulateWorkDayInPeriod = accumulateWorkDayInPeriod + 1;
					}
					
				 	if(paidLeaveHours >= 8) {
						accumulateWorkDay = 0;
					}
					if (accumulateWorkDay > 7) {
						setAbnormalAttendance(at, 2017);
					}
					if (accumulateWorkDayInPeriod == 6 && flexibleDayOffInPeriod == 0) {
						flexibleDayOffInPeriod = 1;
						if(endDayCode != 5) {
							endDayCode = 3;
						}
						if (dayCode != 2 && dayCode != 5) {
							dayCode = 3;
						}
					}
					if (accumulateWorkDayInPeriod == 7 && fixedDayOffInPeriod == 0) {
						fixedDayOffInPeriod = 1;
						if(endDayCode != 5) {
							endDayCode = 4;
						}
						if (dayCode != 2 && dayCode != 5) {
							dayCode = 4;
						} else if (dayCode == 2) {
							at.setCompTime(8);
							at.setCompReason(dayDescription + " VS " + "fixed day off");
						}
					}
					
				}else {
					countDayHours=countDayHours+totalWorkHours+paidLeaveHours;
					countDayPaidHours=countDayPaidHours+paidLeaveHours;
					judgeEnoughtHours(countDayHours,preMonWdAtt, shiftCurrentMonthShift, dayCode, endDayCode, inHoliday,
							endInHoliday, preMonWdAtt2);
					judgeEnoughtHours(countDayHours,at, shiftCurrentMonthShift, dayCode, endDayCode, inHoliday,
							endInHoliday, at2);

				 	if(countDayPaidHours >= 8) {
						accumulateWorkDay = 0;
					}

				 	dayCode=preMonWdAtt.getDayCode();
					if(endDayCode != 5) {
						endDayCode = dayCode;
					}
				}
				at.setDayCode(dayCode);
				if (at.getWorkHours() > 0) {
					remainTaxHours = calculateOverTax(formulaEvaluator, remainTaxHours, row, at, shift);
				}
				if (at2 != null) {
					at2.setDayCode(endDayCode);
					if (at2.getWorkHours() > 0) {
						remainTaxHours = calculateOverTax(formulaEvaluator, remainTaxHours, row, at2, shift);
					}
				}

			}
			workbook.close();
		} catch (Exception e) {
			throw (new CustomException("row:"+rowIn+" ["+e.getMessage(), 2025, e));
		}
		return attendanceDatas;
	}

	private void judgeEnoughtHours(float countDayHours, AttendanceModel at, ShiftModel shiftCurrentMonthShift,
			int dayCode, int endDayCode, boolean inHoliday, boolean endInHoliday, AttendanceModel at2) {
			if(at.getShift().length()==2) {
				long maxComparisonResult= calculateMaxComparison(at,shiftCurrentMonthShift);
				float atHours = (float) maxComparisonResult / 60;
				if(at.getWorkHours()<atHours) {
					if(inHoliday||endInHoliday) {
						if(dayCode==1&&at.getWorkHours()>0) {
							at.setWorkHours(atHours);
						}
					}else {
						setAbnormalAttendance(at, 2023);
					}
					
				}else {
					if(at.getStatus()==2 && "2023".equals(at.getAbnormalCode())) {
						at.setStatus(1);
						at.setAbnormalCode(null);
					}
				}
				if(countDayHours>=shiftCurrentMonthShift.getBaseHours()) {
					if(at.getStatus()==2 && "2023".equals(at.getAbnormalCode())) {
						at.setStatus(1);
						at.setAbnormalCode(null);
					}
				}
				
			}
			if(at2!=null) {
				if(at2.getShift().length()==2) {
					long endMaxComparisonResult= calculateMaxComparison(at2,shiftCurrentMonthShift);
					float endHours = (float) endMaxComparisonResult / 60;
					if(at2.getWorkHours()<endHours) {
						if(endInHoliday) {
							if(endDayCode==1&&at2.getWorkHours()>0) {
								at2.setWorkHours(endHours);
							}
						}else{
							setAbnormalAttendance(at2, 2023);
						}
					}else {
						if(at2.getStatus()==2 && "2023".equals(at2.getAbnormalCode())) {
							at2.setStatus(1);
							at2.setAbnormalCode(null);
						}
					}
					if(countDayHours>=shiftCurrentMonthShift.getBaseHours()) {
						if(at2.getStatus()==2 && "2023".equals(at2.getAbnormalCode())) {
							at2.setStatus(1);
							at2.setAbnormalCode(null);
						}
					}
				}
			}

	}

	private String checkDayoffName(boolean inHoliday, int dayCode) {
		String df="";
		if (dayCode == 2) {
			df = "NTF";
		} else if (dayCode == 5) {
			df = "DSF";
		}else if (dayCode == 4) {
				df = "DXF";
		}else if (dayCode == 3) {
			df = "DLF";
		}else if (inHoliday) {
			df = "TBF";
		}
		return df;
	}

	private void setAbnormalAttendance(AttendanceModel at, int errorCode) {
		at.setStatus(2);
		if (StringUtil.isNotBlank(at.getAbnormalCode())) {
			at.setAbnormalCode(at.getAbnormalCode() + "," + errorCode);
		} else {
			at.setAbnormalCode(String.valueOf(errorCode));
		}
	}

	private void checkLeaveBalances(Map<String, LeaveBalanceModel> leaveBalanceModelMap, AttendanceModel at,
			float workHours,List<LeaveRequestModel> leacesRequest,boolean inHoliday,int dayCode,Map<String, Map<Integer, PayCodeModel>> leavePayCodeMap) {
		boolean check = true;
		if ("L".equals(StringUtils.right(at.getShift(), 1))) {
			String dayoffName=checkDayoffName(inHoliday,dayCode);
			if(StringUtils.isNoneBlank(dayoffName)) {
				Map<Integer, PayCodeModel> dayOffPayMap=leavePayCodeMap.get(dayoffName);
				Map<Integer, PayCodeModel> leavePayMap=leavePayCodeMap.get(at.getShift());
				float dCoefficient=dayOffPayMap!=null&&dayOffPayMap.get(dayCode)!=null?dayOffPayMap.get(dayCode).getCoefficient():0;
				float lCoefficient=leavePayMap!=null&&leavePayMap.get(dayCode)!=null?leavePayMap.get(dayCode).getCoefficient():0;
				if(dCoefficient>=lCoefficient) {
					at.setShift(dayoffName);
					return;
				}
			}
			float oldBlances=0;
			float newBlances=0;
			if(!CollectionUtils.isEmpty(leacesRequest)) {
				newBlances= (float) leacesRequest.stream().filter(x->"balances".equals(x.getSource())&&x.getShift().equals(at.getShift())&&x.getFromTime().before(at.getStartDatetime())).mapToDouble(LeaveRequestModel::getHours).sum();
			}
			if(leaveBalanceModelMap!=null) {
				LeaveBalanceModel lbm = leaveBalanceModelMap.get(at.getShift());
				if (lbm != null) {
					oldBlances=lbm.getRemainingHours();
				} 
			}
			if(oldBlances+newBlances<workHours) {
				check = false;
			}
		}
		if (check == false) {
			setAbnormalAttendance(at, 2018);
		}
	}

	private float calculateWork(FormulaEvaluator formulaEvaluator, Row row, AttendanceModel at, ShiftModel shift,ShiftModel shiftCount,int dayCode) {
		float hours = 0;
		if (at.getArrivalDatetime() == null || at.getLeaveDatetime() == null) {
			if (shift.getName().length() > 2 && at.getArrivalDatetime() == null && at.getLeaveDatetime() == null) {
				hours = shift.getBaseHours();
				return hours;
			}
			setAbnormalAttendance(at,2019);
		} else if (at.getArrivalDatetime() != null && at.getLeaveDatetime() != null) {
			if (at.getArrivalDatetime().compareTo(at.getLeaveDatetime()) >= 0) {
				setAbnormalAttendance(at, 2020);
				return hours;
			}
			long comparisonResult = 0;

			if (shift.getName().length() > 2 ) {
				comparisonResult = calculateComparison(at, shiftCount);
			}else {
				//hours = (float) comparisonResult / 60;
				comparisonResult = calculateComparison(at, shift);
			}
			if (((float) comparisonResult / 60) >= shift.getBaseHours()) {
				hours = shift.getBaseHours();
			} else {
				hours = (float) comparisonResult / 60;
			}
			if(dayCode==2&&hours<shift.getBaseHours()) {
				hours = shift.getBaseHours();
			}
		}
		if(hours<0) {
			setAbnormalAttendance(at, 2020);
		}
		return hours;
	}
	private long calculateMaxComparison(AttendanceModel at, ShiftModel shiftCount) {
		long comparisonResult=0;
		LocalDateTime start=at.getStartDatetime().toLocalDateTime();
		LocalDateTime end=at.getEndDatetime().toLocalDateTime();
		LocalDateTime restStartTime = LocalDateTime.of(LocalDate.of(at.getYear(), at.getMonth(), at.getDay()),
				shiftCount.getStartTime().toInstant().atZone(timeZone).toLocalTime()).plusHours((long) shiftCount.getRestStartHour());
		LocalDateTime endStartTime = restStartTime.plusHours((long) shiftCount.getRestHours());
		if(start.isAfter(restStartTime)){
			if(start.isAfter(endStartTime)) {
				comparisonResult = ChronoUnit.MINUTES.between(start,
						end);
			}else {
				if(end.isAfter(endStartTime)) {
					comparisonResult = ChronoUnit.MINUTES.between(endStartTime,
							end);
				}
			}

		}else {
			if(end.isAfter(restStartTime)) {
				if(end.isAfter(endStartTime)) {
					comparisonResult = ChronoUnit.MINUTES.between(start,
							restStartTime)+ChronoUnit.MINUTES.between(endStartTime,
									end);
				}else {
					comparisonResult = ChronoUnit.MINUTES.between(start,
							restStartTime);
				}
			}else {
				comparisonResult = ChronoUnit.MINUTES.between(start,
						end);
			}
		}
		return comparisonResult;
	}
	private long calculateComparison(AttendanceModel at, ShiftModel shiftCount) {
		long comparisonResult=0;
		LocalDateTime start=at.getStartDatetime().toLocalDateTime();
		LocalDateTime end=at.getLeaveDatetime().toLocalDateTime().isBefore(at.getEndDatetime().toLocalDateTime())?at.getLeaveDatetime().toLocalDateTime():at.getEndDatetime().toLocalDateTime();
		LocalDateTime restStartTime = LocalDateTime.of(LocalDate.of(at.getYear(), at.getMonth(), at.getDay()),
				shiftCount.getStartTime().toInstant().atZone(timeZone).toLocalTime()).plusHours((long) shiftCount.getRestStartHour());
		LocalDateTime endStartTime = restStartTime.plusHours((long) shiftCount.getRestHours());
		if(start.isAfter(restStartTime)){
			if(start.isAfter(endStartTime)) {
				comparisonResult = ChronoUnit.MINUTES.between(start,
						end);
			}else {
				if(end.isAfter(endStartTime)) {
					comparisonResult = ChronoUnit.MINUTES.between(endStartTime,
							end);
				}
			}

		}else {
			if(end.isAfter(restStartTime)) {
				if(end.isAfter(endStartTime)) {
					comparisonResult = ChronoUnit.MINUTES.between(start,
							restStartTime)+ChronoUnit.MINUTES.between(endStartTime,
									end);
				}else {
					comparisonResult = ChronoUnit.MINUTES.between(start,
							restStartTime);
				}
			}else {
				comparisonResult = ChronoUnit.MINUTES.between(start,
						end);
			}
		}
		return comparisonResult;
	}

	private float calculateOverTax(FormulaEvaluator formulaEvaluator, float remainTaxHours, Row row, AttendanceModel at,
			ShiftModel shift) {
		Date overStartTime = getCellValue(formulaEvaluator, row.getCell(10), Date.class);
		Date overEndTime = getCellValue(formulaEvaluator, row.getCell(11), Date.class);
		if (overStartTime != null && overEndTime != null) {
			LocalDateTime overStartDateTime = LocalDateTime.of(at.getEndDatetime().toLocalDateTime().toLocalDate(),
					overStartTime.toInstant().atZone(timeZone).toLocalTime());
			LocalDateTime overEndDateTime = LocalDateTime.of(at.getEndDatetime().toLocalDateTime().toLocalDate(),
					overEndTime.toInstant().atZone(timeZone).toLocalTime());
			if (at.getEndDatetime().compareTo(Timestamp.valueOf(overStartDateTime)) >= 0) {
				overStartDateTime = at.getEndDatetime().toLocalDateTime();
			}
			if (at.getLeaveDatetime().compareTo(Timestamp.valueOf(overStartDateTime)) <= 0) {
				return remainTaxHours;
			}
			if (at.getLeaveDatetime().compareTo(Timestamp.valueOf(overEndDateTime)) <= 0) {
				overEndDateTime = at.getLeaveDatetime().toLocalDateTime();
			}
			if (overStartDateTime.compareTo(overEndDateTime) > 0) {
				setAbnormalAttendance(at, 2021);
				return remainTaxHours;
			}
			Timestamp overStartTimeStamp = Timestamp.valueOf(overStartDateTime);
			Timestamp overEndTimeStamp = Timestamp.valueOf(overEndDateTime);
			long overtime = ChronoUnit.MINUTES.between(overStartDateTime, overEndDateTime);
			if(overtime>0) {
				at.setOvertime((float) overtime / 60);
				at.setOverStartDatetime(overStartTimeStamp);
				at.setOverEndDatetime(overEndTimeStamp);
				at.setWorkHours(at.getWorkHours() + at.getOvertime());
//				remainTaxHours = remainTaxHours - at.getOvertime();
//				if (at.getDayCode() != 1 && at.getDayCode() != 5) {
//					remainTaxHours = remainTaxHours - shift.getBaseHours();
//				}
//				at.setRemainTaxFree(remainTaxHours);
			}

		}
		if(at.getDayCode() == 1 || at.getDayCode() == 5) {
			if (at.getOvertime()>0) {
				if(remainTaxHours>0) {
					if(remainTaxHours -at.getOvertime()>0) {
						at.setTaxFree(at.getOvertime());
					}else {
						at.setTaxFree(remainTaxHours);
					}
				}
				remainTaxHours = remainTaxHours -at.getOvertime();
				at.setRemainTaxFree(remainTaxHours);
			}
		}else {
			if (at.getWorkHours()>0) {
				if(remainTaxHours>0) {
					if(remainTaxHours -at.getWorkHours()>0) {
						at.setTaxFree(at.getWorkHours());
					}else {
						at.setTaxFree(remainTaxHours);
					}
				}
				remainTaxHours = remainTaxHours -at.getWorkHours();
				at.setRemainTaxFree(remainTaxHours);
			}
		}
	
		return remainTaxHours;
	}

	// 检查单元格是否是合并单元格
	private static boolean isMergedCell(Sheet sheet, int rowIdx, int colIdx) {
		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);
			if (region.isInRange(rowIdx, colIdx)) {
				return true;
			}
		}
		return false;
	}

	// 评估单元格中的公式
	private <T> T getCellValue(FormulaEvaluator formulaEvaluator, Cell cell, Class<T> clazz) {

		if (cell == null) {
			return null;
		}

		switch (cell.getCellType()) {
		case STRING:
	        if (clazz == Date.class) {
	        	return null;
	        }
			return clazz.cast(cell.getStringCellValue());
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				if (clazz.equals(String.class)) {
					return clazz.cast(cell.getDateCellValue().toString());
				}
				return clazz.cast(cell.getDateCellValue());
			} else {
				if (clazz == String.class) {
					return (T) String.valueOf(cell.getNumericCellValue()); // 转换为 String
				} else {
					return clazz.cast(cell.getNumericCellValue()); // 其他类型按原逻辑转换
				}
			}
		case BOOLEAN:
			return clazz.cast(cell.getBooleanCellValue());
		case FORMULA:
			CellValue cellValue = formulaEvaluator.evaluate(cell);
			switch (cellValue.getCellType()) {
			case STRING:
				return clazz.cast(cellValue.getStringValue());
			case NUMERIC:
				if (DateUtil.isValidExcelDate(cellValue.getNumberValue())) {
					if (clazz.equals(String.class)) {
						return clazz.cast(cellValue.getStringValue());
					}
					return clazz.cast(DateUtil.getJavaDate(cellValue.getNumberValue()));
				}
				return clazz.cast(cellValue.getNumberValue());

			case BOOLEAN:
				return clazz.cast(cellValue.getBooleanValue());
			default:
				return null;
			}
		case BLANK:
			if (clazz.equals(String.class)) {
				return clazz.cast("");
			} else {
				return null;
			}
		default:
			return null;
		}
	}

}
