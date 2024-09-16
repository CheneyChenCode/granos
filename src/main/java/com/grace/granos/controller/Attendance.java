package com.grace.granos.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.grace.granos.model.AttendanceDataTableModel;
import com.grace.granos.model.AttendanceModel;
import com.grace.granos.model.JsonResponse;
import com.grace.granos.model.User;
import com.grace.granos.service.AttendanceService;
import com.grace.granos.service.FileStorageService;
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
	@RequestMapping("/attendance")
	public String attendance(Model model){
		return "attendance";
	}
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping("/getAttendance")
	public ResponseEntity<JsonResponse> getAttendance(@RequestParam("year") int year,@RequestParam("month") int month,@RequestParam("user") int empid){
    	List<AttendanceDataTableModel> attendances = attendanceService.getAttendancesEvent(year, month, empid);
		return ResponseEntity.ok(new JsonResponse(attendances));
	}
    @PostMapping("/uploadExcel")
    public ResponseEntity<JsonResponse> uploadExcelFile(@RequestParam("file") MultipartFile file,Model model,HttpServletRequest request){
    	JsonResponse rs=new JsonResponse();
        // 获取客户端发送的 Cookie
    	User user=staffService.getUser(request);
    	// 从 CookieLocaleResolver 中获取用户的语言环境
        Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
        String uploadMonth="";
            // 检查文件是否为空
            if (file.isEmpty()) {
            	//model.addAttribute("error","file");
            	rs.setStatus(2004);
            	rs.setMessage(messageSource.getMessage("2004", null, locale));
            	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
            }
            // 获取当前日期
            LocalDate currentDate = LocalDate.now();

            // 使用 DateTimeFormatter 格式化日期为 "yyyyMMdd" 格式的字符串
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String folderName = currentDate.format(formatter)+"/attendance";

    		// 创建文件夹
            String folderPath=fileStorageService.doesFolderExist(folderName);
    		if (folderPath==null) {
    		    	rs.setStatus(2001);
    		    	rs.setMessage(messageSource.getMessage("2001", null, locale));
    		    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
    		}
            
            // 获取上传文件的原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件扩展名
            String extension = FilenameUtils.getExtension(originalFilename);
            String filename=FilenameUtils.getName(originalFilename);
            
            // 验证文件类型是否为.xlsx或.xls
            if (!"xlsx".equalsIgnoreCase(extension) && !"xls".equalsIgnoreCase(extension)) {
                // 文件类型不符合要求，抛出异常或者做相应的处理
                //throw new IllegalArgumentException("Only .xlsx or .xls files are allowed.");
            	rs.setStatus(2002);
            	rs.setMessage(messageSource.getMessage("2002", null, locale));
            	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
            }
            // 将上传的文件保存到临时位置
            //Path tempFilePath = Files.createTempFile(filename+"_", extension);
            try {
                fileStorageService.createFile(folderPath+"/"+filename,file.getBytes());
	            // 解析Excel文件并处理数据
	            List<AttendanceModel> attendances = attendanceService.parseExcel(file.getInputStream(),user);
	            
	            attendanceService.addAttendances(attendances);
	            uploadMonth=attendances.get(0).getYear()+"-"+StringUtils.leftPad(String.valueOf(attendances.get(0).getMonth()),2,"0");
	            String abnormalMessage="";
	        	for(AttendanceModel att:attendances) {
	        		if(att.getStatus()!=1) {
	        			abnormalMessage=abnormalMessage+"\n"+StringUtils.leftPad(String.valueOf(att.getMonth()),2,"0")+"-"+StringUtils.leftPad(String.valueOf(att.getDay()),2,"0")+": "+att.getReason();
	        		}
	        	}
	        	if(StringUtil.isNotBlank(abnormalMessage)) {
	        		rs.setData("But there are some abnormal in attendance:"+abnormalMessage);
	        	}
            } catch (Exception e) {
				logger.error(e.getMessage());
            	rs.setStatus(2003);
            	rs.setMessage(messageSource.getMessage("2003", null, locale));
            	rs.setData(e.getMessage());
            	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
			}



            // 将数据存储到数据库或执行其他操作
            // attendanceService.saveAttendances(attendances);

            // 返回成功消息
            //return ResponseEntity.ok("Upload file successfully.");
        	rs.setStatus(2000);
        	rs.setMessage(uploadMonth+" "+messageSource.getMessage("2000", null, locale));
            return ResponseEntity.ok(rs);
    }

}