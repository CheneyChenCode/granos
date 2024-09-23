package com.grace.granos.service;

import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
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
import com.grace.granos.dao.AttendanceRepository;
import com.grace.granos.dao.HolidayRepository;
import com.grace.granos.dao.ShiftRepository;
import com.grace.granos.model.AttendanceDataTableModel;
import com.grace.granos.model.AttendanceModel;
import com.grace.granos.model.HolidaysModel;
import com.grace.granos.model.PayrollModel;
import com.grace.granos.model.ShiftModel;
import com.grace.granos.model.User;

@Service
@PropertySource("classpath:application.properties") // 指定属性文件的位置
public class AttendanceService {
	private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
	@Autowired
	private AttendanceRepository attendanceRepository;
	@Autowired
	private ShiftRepository shiftRepository;
	@Autowired
	private HolidayRepository holidayRepository;
	@Value("${spring.time.zone}")
	private String zoneName;
	private ZoneId timeZone;

	@PostConstruct
	private void init() {
		// 确保在属性注入完成后进行初始化
		this.timeZone = ZoneId.of(zoneName);
	}

	public List<AttendanceModel> checkAttendanceForPayByUserMon(int year, int month, int empid) throws Exception {
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

	public List<AttendanceModel> findAttendanceByUserMon(AttendanceModel att) throws Exception {
		if (att.getYear() < 1) {
			throw new Exception("there is no year");
		}
		if (att.getMonth() < 1) {
			throw new Exception("there is no month");
		}
		if (att.getEmpId() < 1) {
			throw new Exception("there is no empoloyee id");
		}

		return attendanceRepository.findAttendanceByUserMon(att);
	}

	public List<AttendanceDataTableModel> getAttendancesEvent(int year, int month, int empid) {
		logger.info("Service:getAttendances[" + year + "]");
		AttendanceModel attendance = new AttendanceModel();
		attendance.setYear(year);
		attendance.setMonth(month);
		attendance.setEmpId(empid);
		List<AttendanceModel> attendances = null;
		List<AttendanceDataTableModel> caledarEvents = new ArrayList<>();
		try {
			attendances = findAttendanceByUserMon(attendance);
			if (attendances == null) {
				return caledarEvents;
			}
			// 創建SimpleDateFormat對象來定義日期格式
			// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			// dateFormat.setTimeZone(TimeZone.getTimeZone(zoneName)); // 设置时区为 UTC
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			timeFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置时区为 UTC
			String[] weekStr = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
			String[] monStr = { "Jan", "Feb", "Wed", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
			float totalWork = 0;
			float totalOverTime = 0;
			for (AttendanceModel att : attendances) {
				AttendanceDataTableModel caledarEvent = new AttendanceDataTableModel();
				caledarEvents.add(caledarEvent);
				caledarEvent.setEmpid(att.getEmpId());
				caledarEvent.setYear(att.getYear());
				caledarEvent.setMonth(monStr[att.getMonth() - 1]);
				caledarEvent.setWeek(weekStr[att.getWeek() - 1]);
				caledarEvent.setDay(att.getDay());
				caledarEvent.setSeq(att.getSeq());
				caledarEvent.setShift(att.getShift());
				caledarEvent.setStatus(att.getStatus());
				if (att.getStartDatetime() != null) {
					caledarEvent.setStartDate(timeFormat.format(att.getStartDatetime()));
				} else {
					caledarEvent.setStartDate("");
				}
				if (att.getEndDatetime() != null) {
					caledarEvent.setEndDate(timeFormat.format(att.getEndDatetime()));
				} else {
					caledarEvent.setEndDate("");
				}
				if (att.getArrivalDatetime() != null) {
					caledarEvent.setArrivalDate(timeFormat.format(att.getArrivalDatetime()));
				} else {
					caledarEvent.setArrivalDate("");
				}
				if (att.getLeaveDatetime() != null) {
					caledarEvent.setLeaveDate(timeFormat.format(att.getLeaveDatetime()));
				} else {
					caledarEvent.setLeaveDate("");
				}
				// 計算時間差並轉換為小時數
				// Timestamp actualStartTime=att.getStartDatetime();
				// Timestamp actualEndTime=att.getEndDatetime();
				// if(att.getArrivalDatetime().getTime()>att.getStartDatetime().getTime()) {
				// actualStartTime=att.getArrivalDatetime();
				// actualEndTime=new
				// Timestamp(att.getEndDatetime().getTime()+(att.getArrivalDatetime().getTime()-att.getStartDatetime().getTime()));
				// }
				// 格式化小时、分钟和秒
				DecimalFormat df = new DecimalFormat("00");
				// long diffLong=att.getLeaveDatetime().getTime() - actualStartTime.getTime();
				// long restHour=(long)att.getRestHours()*3600*1000;
				// if(diffLong>restHour) {
				// diffLong=diffLong-restHour;
				// }

				// 将时间差异转换为小时、分钟和秒
				// long hours = TimeUnit.MILLISECONDS.toHours(diffLong);
				// long minutes = TimeUnit.MILLISECONDS.toMinutes(diffLong) % 60;
				totalWork = totalWork + att.getWorkHours();
				int hours = (int) att.getWorkHours(); // 提取整数部分作为小时
				float minutesFloat = (att.getWorkHours() - hours) * 60; // 将小数部分转换为分钟
				int minutes = Math.round(minutesFloat); // 四舍五入取整
				caledarEvent.setHours(df.format(hours) + ":" + df.format(minutes));
				// long totalWorkHour = TimeUnit.MILLISECONDS.toHours(totalWorkLong);
				// long totalWorkMinutes = TimeUnit.MILLISECONDS.toMinutes(totalWorkLong) % 60;
				int totalHours = (int) totalWork; // 提取整数部分作为小时
				int totalMinutes = Math.round((totalWork - totalHours) * 60); // 四舍五入取整
				caledarEvent.setTotalHours(df.format(totalHours) + ":" + df.format(totalMinutes));

				// long diffOverLong=att.getLeaveDatetime().getTime() - actualEndTime.getTime();
				totalOverTime = totalOverTime + att.getOvertime();
				// 将时间差异转换为小时、分钟和秒
				long overHours = (int) att.getOvertime();
				long overMinutes = Math.round((att.getOvertime() - overHours) * 60);
				caledarEvent.setOvertime(df.format(overHours) + ":" + df.format(overMinutes));
				long totalOtHour = (int) totalOverTime;
				long totalOtMinutes = Math.round((totalOverTime - totalOtHour) * 60);
				caledarEvent.setTotalOverTime(df.format(totalOtHour) + ":" + df.format(totalOtMinutes));
				caledarEvent.setReason(att.getReason());
				caledarEvent.setNote(att.getNote());
				caledarEvent.setDayCode(att.getDayCode());
				caledarEvent.setPeriod(att.getPeriod());
				long taxFreeOverHours = 0;
				long taxFreeOverMinutes = 0;
				if(att.getRemainTaxFree()>0) {
					if (att.getDayCode() != 1 && att.getDayCode() != 5) {
						taxFreeOverHours=(int) att.getWorkHours();
						taxFreeOverMinutes=Math.round((att.getWorkHours()- taxFreeOverHours) * 60);
					}else {
						taxFreeOverHours=(int) att.getOvertime();
						taxFreeOverMinutes=Math.round((att.getOvertime()- taxFreeOverHours) * 60);
					}	
				}else {
					if (att.getDayCode() != 1 && att.getDayCode() != 5) {
						taxFreeOverHours=(int)(att.getRemainTaxFree()+att.getWorkHours());
						if(taxFreeOverHours>0) {
							taxFreeOverMinutes=Math.round((att.getRemainTaxFree()+att.getWorkHours()- taxFreeOverHours) * 60);
						}else {
							taxFreeOverHours=0;
						}
					}else {
						taxFreeOverHours=(int) (att.getRemainTaxFree()+att.getOvertime());
						if(taxFreeOverHours>0) {
							taxFreeOverMinutes=Math.round((att.getRemainTaxFree()+att.getOvertime()- taxFreeOverHours) * 60);
						}else {
							taxFreeOverHours=0;
						}
					}
				}
				caledarEvent.setTaxFreeOverTime(df.format(taxFreeOverHours) + ":" + df.format(taxFreeOverMinutes));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return caledarEvents;
	}

	public List<AttendanceModel> parseExcel(InputStream fileInputStream, User user) throws Exception {
		List<AttendanceModel> attendanceDatas = new ArrayList<AttendanceModel>();

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
			throw new RuntimeException("Workbook doesn't contain any available sheets.");
		}
		Row titleRow = sheet.getRow(3);
		if (titleRow == null) {
			throw new RuntimeException("The third row is empty.");
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
				throw new RuntimeException("Cell content does not match the expected keyword: " + keywords[i - 1]);
			}
		}
		Calendar calendar = Calendar.getInstance();
		List<HolidaysModel> holidays = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int seq = 0;
		Date datePre = null;
		AttendanceModel preMonWdAtt=null;
		int accumulateWorkDay = 0;
		int accumulateWorkDayInPeriod = 0;
		int fixedDayOffInPeriod = 0;
		int flexibleDayOffInPeriod = 0;
		int conversionDayOffCurrentMonth = 0;
		int shiftCurrentMonthIndex = 0;
		int period = 0;
		int countDay=0;
		float remainTaxHours = 46;
		String shiftCurrentMonth = null;
		String shiftNamePre = null;
		List<ShiftModel> shifts = shiftRepository.findShift();
		Map<String, ShiftModel> shiftModelMap = shifts.stream()
				.collect(Collectors.toMap(ShiftModel::getName, shiftModel -> shiftModel));
		// 从第四行开始循环遍历每一行
		for (int rowIndex = 4; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
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
			datePre=date;
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
			at.setYear(year);
			at.setDay(day);
			at.setMonth(month);
			// 获取星期几（1 表示星期日，2 表示星期一，以此类推）
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			at.setWeek(dayOfWeek);
			// Sequence of period
			Double excelPeriod = getCellValue(formulaEvaluator, row.getCell(17), Double.class);
			if (rowIndex == 4) {
				preMonWdAtt=attendanceRepository.findLastAttByUserMon(at);
				if (preMonWdAtt == null && excelPeriod == null) {
					throw new Exception("There are no attendance records in the previous month.");
				}
				datePre =  Date.from(LocalDate.of(preMonWdAtt.getYear(), preMonWdAtt.getMonth(), preMonWdAtt.getDay()).atStartOfDay(timeZone).toInstant());
				period = preMonWdAtt.getPeriod();
				shiftCurrentMonth=attendanceRepository.findLastWdShiftByUserMon(at);
				accumulateWorkDay = attendanceRepository.findLastAccumulateWdByUserMon(at);
				accumulateWorkDayInPeriod = attendanceRepository.findLastAccumulateWdPeByUserMon(at);
				fixedDayOffInPeriod = attendanceRepository.findLastfixedDayOffInPeriod(at);
				flexibleDayOffInPeriod = attendanceRepository.findLastflexibleDayOffInPeriod(at);
				//accumulateWorkDayInPeriod = period - fixedDayOffInPeriod - flexibleDayOffInPeriod;
			}
//			if(datePre.toInstant().atZone(timeZone).toLocalDate().equals(date.toInstant().atZone(timeZone).toLocalDate())) {
//				seq=preMonWdAtt.getSeq();
//			}else {
//				seq=1;
//			}
			seq=1;
			// 将日期和时间字符串转换为 LocalDate 和 LocalTime 对象
			LocalDate startDate = date.toInstant().atZone(timeZone).toLocalDate();
			// arrival time
			Date arrivalTime = getCellValue(formulaEvaluator, row.getCell(5), Date.class);
			if(arrivalTime!=null) {
				// 解析时间字符串为 LocalTime 对象
				LocalTime arrivalLocalTime = arrivalTime.toInstant().atZone(timeZone).toLocalTime();
				at.setArrivalDatetime(Timestamp.valueOf(LocalDateTime.of(startDate, arrivalLocalTime)));
				 // 將 Timestamp 轉換為 LocalDateTime
				if(preMonWdAtt.getLeaveDatetime()!=null) {
			        LocalDateTime dateTime1 = at.getArrivalDatetime().toLocalDateTime().withSecond(0).withNano(0);
			        LocalDateTime dateTime2 = preMonWdAtt.getLeaveDatetime().toLocalDateTime().withSecond(0).withNano(0);
					if(dateTime1.equals(dateTime2)) {
						seq=seq+1;
					}
				}
			}
			
			if(seq>1) {
				at.setYear(preMonWdAtt.getYear());
				at.setDay(preMonWdAtt.getDay());
				at.setMonth(preMonWdAtt.getMonth());
				at.setWeek(preMonWdAtt.getWeek());
			}
			preMonWdAtt=at;
			at.setSeq(seq);
			
			if (excelPeriod != null && seq == 1) {
				period = (int) Math.floor(excelPeriod);
				if (period <= 1) {
					fixedDayOffInPeriod = 0;
					flexibleDayOffInPeriod = 0;
					accumulateWorkDayInPeriod = 0;
				}
			} else if (seq == 1) {
				if (period == 7) {
					fixedDayOffInPeriod = 0;
					flexibleDayOffInPeriod = 0;
					accumulateWorkDayInPeriod = 0;
					period = 1;
				} else {
					period = period + 1;
				}
			}
			at.setPeriod(period);
			ShiftModel shiftCurrentMonthShift=shiftModelMap.get(shiftCurrentMonth);
			at.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(date.toInstant().atZone(timeZone).toLocalDate(),shiftCurrentMonthShift.getStartTime().toInstant().atZone(timeZone).toLocalTime())));
			at.setEndDatetime(Timestamp.valueOf(at.getStartDatetime().toLocalDateTime().plusHours((long) (shiftCurrentMonthShift.getBaseHours()+shiftCurrentMonthShift.getRestHours()))));

			// reason
			String reason = getCellValue(formulaEvaluator, row.getCell(14), String.class);
			at.setReason(reason);
			// approval
			String approval = getCellValue(formulaEvaluator, row.getCell(15), String.class);
			if (approval.contains("Y") || approval.contains("Yes") || approval.contains("T")
					|| approval.contains("True")) {
				at.setApproval("Y");
			} else if (approval.contains("N") || approval.contains("No") || approval.contains("F")
					|| approval.contains("False")) {
				at.setApproval("N");
			}
			// note
			String note = getCellValue(formulaEvaluator, row.getCell(16), String.class);
			at.setNote(note);
			// date code
			int dayCode = 0;
			int endDayCode = 1;
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
				if (leaveTimeJudge.isBefore(at.getArrivalDatetime().toLocalDateTime().toLocalTime())) {
					at.setLeaveDatetime(Timestamp.valueOf(LocalDateTime.of(startDate.plusDays(1), leaveTimeJudge)));
				}else {
					at.setLeaveDatetime(Timestamp.valueOf(LocalDateTime.of(startDate, leaveTimeJudge)));
				}
			}
			// shift
			String shiftName = getCellValue(formulaEvaluator, row.getCell(1), String.class);
			if (StringUtil.isBlank(shiftName) && isMergedCell(sheet, rowIndex, 1) && shiftNamePre != null) {
				shiftName = shiftNamePre;
			}
			if (StringUtil.isBlank(shiftName)) {
				if (fixedDayOffInPeriod == 0) {
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
					shiftName="DXF";
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
					shiftName="DLF";
				} else {
					if (dayCode == 2) {
						shiftName="NTL";
					}else if(dayCode == 5){
						shiftName="DSL";
					}else {
						at.setStatus(2);
						at.setReason("abnormal day off in a period");
						continue;
					}
					
				}
				//at.setDayCode(dayCode);
				//accumulateWorkDay = 0;
				//continue;
			}

			ShiftModel shift = shiftModelMap.get(shiftName.trim());
			if (shift == null) {
				logger.error(shiftName);
				throw new RuntimeException(date.toString() + " shift doesn't exist");
			}
			at.setShift(shift.getName());
			if (shiftName.length() > 2) {
				switch (shiftName) {
				case "DXF":
					if (fixedDayOffInPeriod >= 1) {
						at.setStatus(2);
						at.setReason("over one fixed day off a period");
						continue;
					}
					if (dayCode == 2) {
						at.setShift("NTL");
						at.setCompTime(8);
						at.setCompReason(dayDescription + " VS " + "fixed day off");
					} else {
						dayCode = 4;
						fixedDayOffInPeriod = 1;
					}
					//at.setDayCode(dayCode);
					//accumulateWorkDay = 0;
					//continue;
					break;
				case "DLF":
					if (flexibleDayOffInPeriod >= 1) {
						at.setStatus(2);
						at.setReason("over one flexible day off a period");
						continue;
					}
					if (dayCode == 2) {
						at.setShift("NTL");
						at.setCompTime(8);
						at.setCompReason(dayDescription + " VS " + "flexible day off");
					} else {
						dayCode = 3;
						flexibleDayOffInPeriod = 1;
					}
					//at.setDayCode(dayCode);
					//accumulateWorkDay = 0;
					//continue;
					break;
				case "DCF":
					if (conversionDayOffCurrentMonth >= 2) {
						at.setStatus(2);
						at.setReason("over two conversion day off a month");
						continue;
					}
					conversionDayOffCurrentMonth = 1;
					if (dayCode == 2) {
						at.setShift("NTL");
					}
					
					//at.setDayCode(dayCode);
					//accumulateWorkDay = 0;
					//continue;
					break;
				default:
					//at.setDayCode(dayCode);

				}
			}

			if (StringUtil.isNotBlank(shiftCurrentMonth)) {
				if (shiftName.length()==2&&!shiftCurrentMonth.substring(0, 1).equals(shiftName.substring(0, 1))
						&& conversionDayOffCurrentMonth < 2) {
					for (int i = attendanceDatas.size() - 2; i > shiftCurrentMonthIndex; i--) {
						AttendanceModel temAt = attendanceDatas.get(i);
						if (StringUtil.isBlank(temAt.getShift()) && temAt.getStatus() == 2
								&& conversionDayOffCurrentMonth < 2) {
							temAt.setShift("DCF");
							temAt.setStatus(1);
							temAt.setReason(null);
							conversionDayOffCurrentMonth = conversionDayOffCurrentMonth + 1;
						}else if(temAt.getShift().length()>2 && i==attendanceDatas.size() - 2) {
							if (temAt.getArrivalDatetime() != null) {
								long comparisonResult = ChronoUnit.SECONDS.between(LocalDateTime.of(temAt.getArrivalDatetime().toLocalDateTime().toLocalDate(), shift.getStartTime().toLocalDateTime().toLocalTime()),
										temAt.getArrivalDatetime().toLocalDateTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES));
								long comparisonResult2 = ChronoUnit.SECONDS.between(LocalDateTime.of(temAt.getArrivalDatetime().toLocalDateTime().toLocalDate(),shiftCurrentMonthShift.getStartTime().toLocalDateTime().toLocalTime()),
										temAt.getArrivalDatetime().toLocalDateTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES));
								ShiftModel switchShift=null;
								long switchArriveLate = 0;
								if(Math.abs(comparisonResult)>Math.abs(comparisonResult2)) {
									switchShift=shiftCurrentMonthShift;
									if(comparisonResult2>0) {
										switchArriveLate=comparisonResult2;
									}
								}else {
									switchShift=shift;
									if(comparisonResult>0) {
										switchArriveLate=comparisonResult;
									}
								}
								temAt.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(temAt.getArrivalDatetime().toLocalDateTime().toLocalDate(),switchShift.getStartTime().toInstant().atZone(timeZone).toLocalTime()).plusSeconds(switchArriveLate)));
								temAt.setEndDatetime(Timestamp.valueOf(temAt.getStartDatetime().toLocalDateTime().plusHours((long) (switchShift.getBaseHours()+switchShift.getRestHours()))));
								float temAtWorkHours=calculateWork(formulaEvaluator, row, temAt, switchShift);
								temAt.setPaidLeave(temAtWorkHours);
							}else {
								temAt.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(temAt.getStartDatetime().toLocalDateTime().toLocalDate(),shift.getStartTime().toInstant().atZone(timeZone).toLocalTime())));
								temAt.setEndDatetime(Timestamp.valueOf(temAt.getStartDatetime().toLocalDateTime().plusHours((long) (shift.getBaseHours()+shift.getRestHours()))));
							}
						}
					}
				}
			}
			if(shiftName.length() == 2) {
				shiftCurrentMonth = shiftName;
				shiftCurrentMonthIndex = rowIndex - 4;
			}

			if(shiftName.length() > 2) {
				at.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(startDate, shiftCurrentMonthShift.getStartTime().toInstant().atZone(timeZone).toLocalTime())));
				at.setEndDatetime(Timestamp.valueOf(at.getStartDatetime().toLocalDateTime().plusHours((long) (shiftCurrentMonthShift.getBaseHours()+shiftCurrentMonthShift.getRestHours()))));
				// seq 2
				if (seq > 1) {
					Optional<AttendanceModel> at1 = attendanceDatas.stream()
							.filter(a -> a.getDay() == at.getDay() && a.getSeq() == at.getSeq() - 1).findFirst();
					if (at1.isPresent()) {
						at1.get().setEndDatetime(at.getArrivalDatetime());
						at.setStartDatetime(at1.get().getEndDatetime());
						LocalDateTime oStart=LocalDateTime.of(LocalDate.of(at.getYear(), at.getMonth(), at.getDay()), shiftCurrentMonthShift.getStartTime().toInstant().atZone(timeZone).toLocalTime());
						at.setEndDatetime(Timestamp.valueOf(oStart.plusHours((long) (shiftCurrentMonthShift.getBaseHours()+shiftCurrentMonthShift.getRestHours()))));
					}
				}
			}else {
				at.setStartDatetime(Timestamp.valueOf(LocalDateTime.of(startDate, shift.getStartTime().toInstant().atZone(timeZone).toLocalTime())));
				at.setEndDatetime(Timestamp.valueOf(at.getStartDatetime().toLocalDateTime().plusHours((long) (shift.getBaseHours()+shift.getRestHours()))));
				// seq 2
				if (seq > 1) {
					Optional<AttendanceModel> at1 = attendanceDatas.stream()
							.filter(a -> a.getDay() == at.getDay() && a.getSeq() == at.getSeq() - 1).findFirst();
					if (at1.isPresent()) {
						at1.get().setEndDatetime(at.getArrivalDatetime());
						at.setStartDatetime(at1.get().getEndDatetime());
						LocalDateTime oStart=LocalDateTime.of(LocalDate.of(at.getYear(), at.getMonth(), at.getDay()), shift.getStartTime().toInstant().atZone(timeZone).toLocalTime());
						at.setEndDatetime(Timestamp.valueOf(oStart.plusHours((long) (shift.getBaseHours()+shift.getRestHours()))));
					}
				}
			}

			long arriveLate = 0;
			if (at.getArrivalDatetime() != null) {	
				// 比较两个时间字符串
				long comparisonResult = ChronoUnit.SECONDS.between(at.getStartDatetime().toLocalDateTime(),at.getArrivalDatetime().toLocalDateTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES));
				if (comparisonResult > 0) {
					at.setStartDatetime(at.getArrivalDatetime());
					arriveLate = comparisonResult;
					at.setEndDatetime(Timestamp.valueOf(at.getEndDatetime().toLocalDateTime().plusSeconds(arriveLate)));
				} 
			} 
			if (holidays != null) {
				String end = at.getEndDatetime().toLocalDateTime().getYear() + "-"
						+ StringUtils.leftPad(String.valueOf(at.getEndDatetime().toLocalDateTime().getMonthValue()), 2, '0') + "-"
						+ StringUtils.leftPad(String.valueOf(at.getEndDatetime().toLocalDateTime().getDayOfMonth()), 2, '0');
				Optional<HolidaysModel> holiday = holidays.stream()
						.filter(x -> end.equals(dateFormat.format(x.getDate()))).findFirst();
				// 检查是否找到匹配的 CalendarEvent 对象
				if (holiday.isPresent()) {
					endDayCode = holiday.get().getDayCode();
				}
			}
			AttendanceModel at2 = null;
			float totalWorkHours=0;
			float paidLeaveHours=0;
			if (at.getLeaveDatetime()!=null&&dayCode != endDayCode && (dayCode == 5 || endDayCode == 5)) {
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

				at.setLeaveDatetime(Timestamp.valueOf(
						LocalDateTime.of(at.getLeaveDatetime().toLocalDateTime().toLocalDate(), LocalTime.of(00, 00, 00)).plusSeconds(arriveLate)));
				at2.setArrivalDatetime(at.getLeaveDatetime());
				at2.setStartDatetime(at.getLeaveDatetime());
				at.setEndDatetime(at.getLeaveDatetime());
				float workHours2=calculateWork(formulaEvaluator, row, at2, shift);
				// paid leave
				if (at2.getShift().length() > 2) {
					paidLeaveHours=paidLeaveHours+workHours2;
					at.setPaidLeave(workHours2);
				}else {
					at2.setWorkHours(workHours2);
					totalWorkHours=totalWorkHours+workHours2;
				}
			}

			float workHours=calculateWork(formulaEvaluator, row, at, shift);
			// paid leave
			if (shiftName.length() > 2) {
				paidLeaveHours=paidLeaveHours+workHours;
				at.setPaidLeave(workHours);
			}else {
				at.setWorkHours(workHours);
				totalWorkHours=totalWorkHours+workHours;
			}
			//date code
			if(countDay!=at.getDay()) {
				if(totalWorkHours>0 || (dayCode !=3 && dayCode !=4)) {
					countDay=at.getDay();
					accumulateWorkDay=accumulateWorkDay+1;
					accumulateWorkDayInPeriod = accumulateWorkDayInPeriod + 1;
				}
				if (accumulateWorkDay > 7) {
					at.setStatus(2);
					if (StringUtil.isNotBlank(at.getReason())) {
						at.setReason(at.getReason() + ", work day over 7");
					} else {
						at.setReason("work day over 7");
					}
				}
				if (accumulateWorkDayInPeriod == 6 && flexibleDayOffInPeriod==0) {
					flexibleDayOffInPeriod=1;
					endDayCode = 3;
					if (dayCode != 2 && dayCode != 5) {
						dayCode = 3;
					}
				}
				if (accumulateWorkDayInPeriod == 7 && fixedDayOffInPeriod==0) {
					fixedDayOffInPeriod=1;
					endDayCode = 4;
					if (dayCode != 2 && dayCode != 5) {
						dayCode = 4;
					} else if (dayCode == 2) {
						at.setCompTime(8);
						at.setCompReason(dayDescription + " VS " + "fixed day off");
					}
				}
				if(paidLeaveHours>=8) {
					accumulateWorkDay=0;
				}
			}
			at.setDayCode(dayCode);
			if(at.getWorkHours()>0) {
				remainTaxHours = calculateOverTax(formulaEvaluator,remainTaxHours ,row, at, shift);
			}
			if(at2!=null) {
				at2.setDayCode(endDayCode);
				if(at2.getWorkHours()>0) {
					remainTaxHours = calculateOverTax(formulaEvaluator,remainTaxHours ,row, at2, shift);
				}
			}

		}
		workbook.close();

		return attendanceDatas;
	}

	private float calculateWork(FormulaEvaluator formulaEvaluator,  Row row, AttendanceModel at,ShiftModel shift) {
		float hours=0;
		if (at.getArrivalDatetime() == null || at.getLeaveDatetime() == null) {
			if(shift.getName().length()>2 && at.getArrivalDatetime() == null && at.getLeaveDatetime() == null) {
				hours=shift.getBaseHours();
				return hours;
			}
			at.setStatus(2);
			at.setReason("attendance time abnormal");
		} else if (at.getArrivalDatetime() != null && at.getLeaveDatetime() != null) {
			if (at.getArrivalDatetime().compareTo(at.getLeaveDatetime()) >= 0) {
				at.setStatus(2);
				at.setReason("leavetime and arrivaltime are abnormal");
				return hours;
			}
			long comparisonResult = 0;
			if (at.getLeaveDatetime().toLocalDateTime().compareTo(at.getEndDatetime().toLocalDateTime()) >= 0) {
				comparisonResult = ChronoUnit.MINUTES.between(at.getStartDatetime().toLocalDateTime(),
						at.getEndDatetime().toLocalDateTime());
			} else {
				comparisonResult = ChronoUnit.MINUTES.between(at.getStartDatetime().toLocalDateTime(),
						at.getLeaveDatetime().toLocalDateTime());
			}
			hours=(float) comparisonResult / 60;
			LocalDateTime restStartTime = at.getStartDatetime().toLocalDateTime()
					.plusSeconds(Math.round(shift.getRestStartHour() * 60 * 60));

			if (at.getLeaveDatetime().toLocalDateTime().compareTo(restStartTime) > 0
					&& at.getArrivalDatetime().toLocalDateTime().compareTo(restStartTime) <= 0) {
				comparisonResult = (long) (comparisonResult - shift.getRestHours() * 60);
			}
			if (((float) comparisonResult / 60) >= shift.getBaseHours()) {
				hours=shift.getBaseHours();
			} else {
				hours=(float) comparisonResult / 60;
			}
		}
		return hours;
	}

	private float calculateOverTax(FormulaEvaluator formulaEvaluator, float remainTaxHours,Row row, AttendanceModel at,
			ShiftModel shift) {
		// long overTime = (long) (comparisonResult - shift.getBaseHours() * 60);
		// at.setOverTime((float) overTime / 60);
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
			if (overStartDateTime.compareTo(overEndDateTime) >= 0) {
				at.setStatus(2);
				at.setReason("overtimes start and end are abnormal");
				return remainTaxHours;
			}
			Timestamp overStartTimeStamp = Timestamp.valueOf(overStartDateTime);
			Timestamp overEndTimeStamp = Timestamp.valueOf(overEndDateTime);
			at.setOverStartDatetime(overStartTimeStamp);
			at.setOverEndDatetime(overEndTimeStamp);
			long overtime = ChronoUnit.MINUTES.between(overStartDateTime, overEndDateTime);
			at.setOvertime((float) overtime / 60);
			at.setWorkHours(at.getWorkHours() + at.getOvertime());
			remainTaxHours=remainTaxHours-at.getOvertime();
			if (at.getDayCode() != 1 && at.getDayCode() != 5) {
				remainTaxHours = remainTaxHours - shift.getBaseHours();
			}
			at.setRemainTaxFree(remainTaxHours);
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
			return clazz.cast(cell.getStringCellValue());
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				if (clazz.equals(String.class)) {
					return clazz.cast(cell.getDateCellValue().toString());
				}
				return clazz.cast(cell.getDateCellValue());
			} else {
				return clazz.cast(cell.getNumericCellValue());
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
