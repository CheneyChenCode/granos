package com.grace.granos.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.grace.granos.model.AttendanceDataTableModel;
import com.grace.granos.model.AttendanceModel;
import com.grace.granos.model.CustomException;
import com.grace.granos.model.JsonResponse;
import com.grace.granos.model.LeaveBalanceModel;
import com.grace.granos.model.LeaveRequestModel;
import com.grace.granos.model.ShiftModel;
import com.grace.granos.model.User;
import com.grace.granos.service.AttendanceService;
import com.grace.granos.service.FileStorageService;
import com.grace.granos.service.LeaveBalanceService;
import com.grace.granos.service.PayrollService;
import com.grace.granos.service.ShiftService;
import com.grace.granos.service.StaffService;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;

@PropertySource("classpath:application.properties") // 指定属性文件的位置
@Controller
public class Attendance {
	private static final Logger logger = LoggerFactory.getLogger(Attendance.class);

	@Autowired
	private MessageSource messageSource;
	@Autowired
	private AttendanceService attendanceService;
	@Autowired
	private StaffService staffService;
	@Autowired
	private FileStorageService fileStorageService;
	@Autowired
	private PayrollService payrollService;
	@Autowired
	private LeaveBalanceService leaveBanlanceService;
	@Autowired
	private ShiftService shiftService;
	@Value("${spring.time.zone}")
	private String zoneName;
	
	@RequestMapping("/attendance")
	public String attendance(Model model) {
		return "attendance";
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/getAttendance")
	public ResponseEntity<JsonResponse> getAttendance(@RequestParam("year") int year, @RequestParam("month") int month,HttpServletRequest request) {
		User user = staffService.getUser(request);
		List<AttendanceModel> attendances = attendanceService.findAttendanceByUserMon(year, month, user.getCharacter().getEmpId());
		List<AttendanceDataTableModel> caledarEvents = new ArrayList<>();
		if (attendances == null) {
			return ResponseEntity.ok(new JsonResponse(caledarEvents));
		}
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
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
			if(att.getAbnormalCode()!=null) {
				String[] errorCodes = att.getAbnormalCode().split(","); // Split by comma
				String[] errorMessage= new String[errorCodes.length];
		        for (int i = 0; i < errorCodes.length; i++) {
		            // 对原数组的每个元素进行加工，比如转换为大写
		        	errorMessage[i] = messageSource.getMessage(errorCodes[i], null, locale);
		        }
		        String result = String.join(", ", errorMessage);
		        if(StringUtils.isNoneBlank(caledarEvent.getReason())) {
		        	caledarEvent.setReason(caledarEvent.getReason()+","+result);
		        }else {
		        	caledarEvent.setReason(result);
		        }
			}
			caledarEvent.setNote(att.getNote());
			caledarEvent.setDayCode(att.getDayCode());
			caledarEvent.setPeriod(att.getPeriod());
			long taxFreeOverHours = 0;
			long taxFreeOverMinutes = 0;
			if (att.getRemainTaxFree() > 0) {
				if (att.getDayCode() != 1 && att.getDayCode() != 5) {
					taxFreeOverHours = (int) att.getWorkHours();
					taxFreeOverMinutes = Math.round((att.getWorkHours() - taxFreeOverHours) * 60);
				} else {
					taxFreeOverHours = (int) att.getOvertime();
					taxFreeOverMinutes = Math.round((att.getOvertime() - taxFreeOverHours) * 60);
				}
			} else {
				if (att.getDayCode() != 1 && att.getDayCode() != 5) {
					taxFreeOverHours = (int) (att.getRemainTaxFree() + att.getWorkHours());
					if (taxFreeOverHours > 0) {
						taxFreeOverMinutes = Math
								.round((att.getRemainTaxFree() + att.getWorkHours() - taxFreeOverHours) * 60);
					} else {
						taxFreeOverHours = 0;
					}
				} else {
					taxFreeOverHours = (int) (att.getRemainTaxFree() + att.getOvertime());
					if (taxFreeOverHours > 0) {
						taxFreeOverMinutes = Math
								.round((att.getRemainTaxFree() + att.getOvertime() - taxFreeOverHours) * 60);
					} else {
						taxFreeOverHours = 0;
					}
				}
			}
			caledarEvent.setTaxFreeOverTime(df.format(taxFreeOverHours) + ":" + df.format(taxFreeOverMinutes));
		}
		
		return ResponseEntity.ok(new JsonResponse(caledarEvents));
	}

	@PostMapping("/uploadExcel")
	public ResponseEntity<JsonResponse> uploadExcelFile(@RequestParam("file") MultipartFile file, Model model,
			HttpServletRequest request) {
		JsonResponse rs = new JsonResponse();
		// 获取客户端发送的 Cookie
		User user = staffService.getUser(request);
		// 从 CookieLocaleResolver 中获取用户的语言环境
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		String uploadMonth = "";
		// 检查文件是否为空
		if (file.isEmpty()) {
			// model.addAttribute("error","file");
			rs.setStatus(2004);
			rs.setMessage(messageSource.getMessage("2004", null, locale));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
		}
		// 获取当前日期
		LocalDateTime currentDate = LocalDateTime.now(ZoneId.of(zoneName));

		// 使用 DateTimeFormatter 格式化日期为 "yyyyMMdd" 格式的字符串
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String folderName = currentDate.format(formatter) + "/attendance";

		// 创建文件夹
		String folderPath = fileStorageService.doesFolderExist(folderName);
		if (folderPath == null) {
			rs.setStatus(2001);
			rs.setMessage(messageSource.getMessage("2001", null, locale));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
		}

		// 获取上传文件的原始文件名
		String originalFilename = file.getOriginalFilename();
		// 获取文件扩展名
		String extension = FilenameUtils.getExtension(originalFilename);
		String filename = FilenameUtils.getName(originalFilename);

		// 验证文件类型是否为.xlsx或.xls
		if (!"xlsx".equalsIgnoreCase(extension) && !"xls".equalsIgnoreCase(extension)) {
			// 文件类型不符合要求，抛出异常或者做相应的处理
			// throw new IllegalArgumentException("Only .xlsx or .xls files are allowed.");
			rs.setStatus(2002);
			rs.setMessage(messageSource.getMessage("2002", null, locale));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
		}
		// 将上传的文件保存到临时位置
		// Path tempFilePath = Files.createTempFile(filename+"_", extension);
		try {
			fileStorageService.createFile(folderPath + "/" + filename, file.getBytes());
			logger.info("Controller:uploadExcel["+folderPath + "/" + filename+"]"+"["+user.getCharacter().getUsername()+"]");
			List<AttendanceModel> attendances = attendanceService.parseExcel(file.getInputStream(), user);
			if (attendances != null && !attendances.isEmpty()) {
				int year = attendances.get(0).getYear();
				int month = attendances.get(0).getMonth();
				int empid = attendances.get(0).getEmpId();
				AttendanceModel attendance=new AttendanceModel();
				attendance.setYear(year);
				attendance.setMonth(month);
				attendance.setEmpId(empid);
				attendanceService.deleteAttendances(attendance);
				attendanceService.addAttendances(attendances);
				payrollService.deletePayroll(year,month,empid);
				uploadMonth = year + "-" + StringUtils.leftPad(String.valueOf(month), 2, "0")+" ";
				logger.info("Controller:uploadExcel["+uploadMonth +"]");
				String abnormalMessage = "";
				List<LeaveRequestModel> leaves =new ArrayList<LeaveRequestModel>();
				for (AttendanceModel att : attendances) {
					if (att.getStatus() != 1) {
						abnormalMessage = abnormalMessage + "\n"
								+ StringUtils.leftPad(String.valueOf(att.getMonth()), 2, "0") + "-"
								+ StringUtils.leftPad(String.valueOf(att.getDay()), 2, "0") + ": " + att.getReason();
						continue;
					}
					String shift=att.getShift();
					if(StringUtils.isNotEmpty(shift)&&shift.length() >2 && att.getPaidLeave()>0 && "L".equals(StringUtils.right(shift, 1))) {
						LeaveRequestModel leave=new LeaveRequestModel();
						leave.setEmpId(att.getEmpId());
						leave.setYear(att.getYear());
						leave.setMonth(att.getMonth());
						leave.setDay(att.getDay());
						leave.setShift(shift);
						leave.setSeq(att.getSeq());
						leave.setFromTime(att.getStartDatetime());
						leave.setToTime(att.getEndDatetime());
						leave.setHours(0-att.getPaidLeave());
						leave.setApprovedBy(user.getUsername());
						leave.setApprovedTime(Timestamp.valueOf(currentDate));
						leave.setNote(att.getNote());
						leave.setReason(att.getReason());
						leave.setRequester(user.getCharacter().getUsername());
						leave.setStatus(att.getStatus());
						leave.setSource("attendance");
						leaves.add(leave);
					}
				}
				LeaveRequestModel lr=new LeaveRequestModel();
				lr.setEmpId(user.getCharacter().getEmpId());
				lr.setYear(year);
				lr.setMonth(month);
				lr.setSource("attendance");
				leaveBanlanceService.deleteLeaveRequests(lr);
				LeaveBalanceModel lb=new LeaveBalanceModel();
				lb.setYear(year);
				lb.setMonth(month);
				lb.setEmpId(user.getCharacter().getEmpId());
				leaveBanlanceService.deleteLeaveBalance(lb);
				if(!CollectionUtils.isEmpty(leaves)) {
					leaveBanlanceService.addLeaveRequests(leaves);
				}
				Map<String, ShiftModel> shiftModelMap = shiftService.getshiftModelMap();
				List<LeaveBalanceModel> lastBalance=leaveBanlanceService.findLastLeaveBalances(user.getCharacter().getEmpId());
				if(CollectionUtils.isEmpty(lastBalance)) {
					rs.setStatus(4001);
					rs.setMessage(messageSource.getMessage("4001", null, locale));
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
				}
				int cMon= lastBalance.get(0).getMonth();
				int cYear=lastBalance.get(0).getYear();
				lr.setEmpId(user.getCharacter().getEmpId());
				lr.setYear(cYear);
				lr.setMonth(cMon);
				List<LeaveRequestModel> lrs=leaveBanlanceService.findLastLeaveRequest(lr);
				int diffMon=0;
				if(currentDate.getYear()>lastBalance.get(0).getYear()) {
					diffMon=12-lastBalance.get(0).getMonth()+currentDate.getMonthValue();
				}else {
					diffMon=currentDate.getMonthValue()-lastBalance.get(0).getMonth();
				}
				for(int i=1;i<=diffMon;i++) {
					cMon=cMon+1;
					if(cMon>12) {
						cYear=cYear+1;
						cMon=1;
					}
					lastBalance=leaveBanlanceService.calculateBalancesPreMon(cYear, cMon, user, lastBalance, shiftModelMap, lrs);
				}
				if (StringUtil.isNotBlank(abnormalMessage)) {
					rs.setData(messageSource.getMessage("2006", null, locale) + abnormalMessage);
				}
			}
		} catch (CustomException e) {
			if(e.getStatus()>0) {
				rs.setStatus(e.getStatus());
				rs.setMessage(uploadMonth+messageSource.getMessage(String.valueOf(e.getStatus()), null, locale));
			}else {
				rs.setStatus(2003);
				rs.setMessage(uploadMonth+messageSource.getMessage("2003", null, locale)+e.getCause().getMessage());
				rs.setData(e.getMessage());
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
		} catch (IOException e) {
			rs.setStatus(2003);
			rs.setMessage(uploadMonth+messageSource.getMessage("2003", null, locale)+e.getMessage());
			rs.setData(e.getMessage());
		}

		// 将数据存储到数据库或执行其他操作
		// attendanceService.saveAttendances(attendances);

		// 返回成功消息
		// return ResponseEntity.ok("Upload file successfully.");
		rs.setStatus(2000);
		rs.setMessage(uploadMonth+messageSource.getMessage("2000", null, locale));
		return ResponseEntity.ok(rs);
	}

}