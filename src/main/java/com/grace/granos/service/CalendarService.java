package com.grace.granos.service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grace.granos.dao.HolidayRepository;
import com.grace.granos.model.CalendarEvent;
import com.grace.granos.model.HolidaysModel;
import com.grace.granos.model.JsonResponse;

@Service
public class CalendarService {
	private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private HolidayRepository holidayRepository;

	public JsonResponse importTwCalendar(String year) {
		JsonResponse jsr=new JsonResponse();
		JsonNode rootNode = getTwCalendarCdn(year);
		String result="total:"+rootNode.size();
		// 定义日期格式（根据输入的字符串格式）
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		List<HolidaysModel> holidaysModels = new ArrayList<>();
		int hCount=0;
		for (JsonNode node : rootNode) {
			String isHoliday = node.get("isHoliday").asText();
			String date = node.get("date").asText();
			String labor=year+"0501";
			if ("false".equals(isHoliday)) {
				if(labor.equals(date)) {
					HolidaysModel holidaysModel = createHolidayModel(formatter,date,"勞動節");
					holidaysModels.add(holidaysModel);
					hCount=hCount+1;
				}
				continue;
			}
			String title = node.get("description").asText();
			if (title.isEmpty()) {
				continue;
			}
			hCount=hCount+1;
			HolidaysModel holidaysModel = createHolidayModel(formatter,date,title);
			holidaysModels.add(holidaysModel);
		}
		int rCount = holidayRepository.addHolidays(holidaysModels).length;
		jsr.setData(result);
		if(rCount>0) {
			jsr.setStatus(0);
		}else {
			jsr.setStatus(1);
		}
		result=result+",holidays:"+hCount;
		result=result+",insert:"+rCount;
		jsr.setMessage(result);
		return jsr;
	}

	private HolidaysModel createHolidayModel(DateTimeFormatter formatter,String date,String title) {
		// 将字符串解析为 LocalDate
		LocalDate localDate = LocalDate.parse(date, formatter);
		// 将 LocalDate 转换为 java.sql.Date
		Date sqlDate = Date.valueOf(localDate);
		HolidaysModel holidaysModel = new HolidaysModel();
		holidaysModel.setDate(sqlDate);
		holidaysModel.setDayCode(2);
		holidaysModel.setDescription(title);
		return holidaysModel;
	}

	private JsonNode getTwCalendarCdn(String year) {
		logger.info("Service:getTwCalendarCdn[" + year + "]");
		String jsonUrl = "https://cdn.jsdelivr.net/gh/ruyut/TaiwanCalendar/data/%s.json";
		// 使用RestTemplate发送HTTP请求获取JSON数据
		ResponseEntity<String> response = restTemplate.getForEntity(String.format(jsonUrl, year), String.class);
		// 解析JSON数据并转换成所需的格式
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = objectMapper.readTree(response.getBody());
		} catch (JsonProcessingException e) {
			return rootNode;
		}
		return rootNode;
	}

	public List<CalendarEvent> getTwCalendar(String year) {
		logger.info("Service:getTwCalendar[" + year + "]");
		List<CalendarEvent> caledarEvents = new ArrayList<>();
		List<HolidaysModel> rootNode = holidayRepository.findHolidaysByYear(year);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (HolidaysModel node : rootNode) {
				CalendarEvent caledarEvent = new CalendarEvent();
				caledarEvent.setTitle(node.getDescription());
				caledarEvent.setStart(dateFormat.format(node.getDate()));
				caledarEvents.add(caledarEvent);
		}
		return caledarEvents;
	}

	public List<CalendarEvent> getTwCalendarByMonth(String year, String month) {
		logger.info("Service:getTwCalendar[" + year + "," + month + "]");
		String jsonUrl = "https://cdn.jsdelivr.net/gh/ruyut/TaiwanCalendar/data/%s.json";
		List<CalendarEvent> caledarEvents = new ArrayList<>();
		// 使用RestTemplate发送HTTP请求获取JSON数据
		ResponseEntity<String> response = restTemplate.getForEntity(String.format(jsonUrl, year), String.class);
		// 解析JSON数据并转换成所需的格式
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = objectMapper.readTree(response.getBody());
		} catch (JsonProcessingException e) {
			return caledarEvents;
		}
		for (JsonNode node : rootNode) {
			String title = node.get("description").asText();
			if (!title.isEmpty()) {
				String date = node.get("date").asText();
				String yearStr = date.substring(0, 4);
				String monthStr = date.substring(4, 6);
				if (year.equals(yearStr) && month.equals(monthStr)) {
					String startDate = yearStr + "-" + monthStr + "-" + date.substring(6, 8);
					CalendarEvent caledarEvent = new CalendarEvent();
					caledarEvent.setTitle(title);
					caledarEvent.setStart(startDate);
					caledarEvents.add(caledarEvent);
				}
			}
		}
		return caledarEvents;
	}
}
