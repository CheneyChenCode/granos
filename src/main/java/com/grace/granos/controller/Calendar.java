package com.grace.granos.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.grace.granos.model.CalendarEvent;
import com.grace.granos.model.JsonResponse;
import com.grace.granos.service.CalendarService;

@PropertySource("classpath:application.properties") // 指定属性文件的位置
@Controller
public class Calendar {
	private static final Logger logger = LoggerFactory.getLogger(Calendar.class);
	@Value("${spring.time.zone}")
	private String zoneName;
    @Autowired
	CalendarService calendarService;
    
    @GetMapping("/importTwCalendar")
	public ResponseEntity<JsonResponse> importTwCalendar(){
		// 获取当前日期
		LocalDateTime currentDate = LocalDateTime.now(ZoneId.of(zoneName));
    	JsonResponse result=calendarService.importTwCalendar(String.valueOf(currentDate.getYear()));
		if(result.getStatus()>0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}else {
			return ResponseEntity.ok(new JsonResponse(result));
		}
	}
    @CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/getTwCalendar")
	public ResponseEntity<List<CalendarEvent>> getTwCalendar(@RequestParam("start") String start,@RequestParam("end") String end){
        String year=start.substring(0, 4);
		List<CalendarEvent> caledarEvents = calendarService.getTwCalendar(year);
		return ResponseEntity.ok(caledarEvents);
	}
    
	@RequestMapping("/calendar")
	public String calendar(Model model){
		return "calendar";
	}
}