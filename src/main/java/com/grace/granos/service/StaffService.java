package com.grace.granos.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

import com.grace.granos.model.StaffModel;
import com.grace.granos.model.User;
import com.grace.granos.util.EncryptUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grace.granos.dao.StaffRepository;

@Service
public class StaffService {
	private static final Logger logger = LoggerFactory.getLogger(StaffService.class);
	@Autowired
	private StaffRepository staffRepository;
	@Autowired
	private EncryptUtil util;
	public void addStaff(StaffModel staffModel){
		staffRepository.addStaff(staffModel);
	}
    public List<StaffModel> getStaff() {
        return staffRepository.findAll();
    }
    public StaffModel findStaffByUserName(String username){
        return staffRepository.findStaffByUserName(username);
    }
    public StaffModel findStaffById(int empId){
        return staffRepository.findStaffById(empId);
    }
    public User findUserById(int empId){
    	StaffModel staff=staffRepository.findStaffById(empId);
    	User userF = staffToUser(staff);
        return userF;
    }
	private User staffToUser(StaffModel staff) {
    	User userF=null;
		if(staff!=null) {
    		userF=new User();
	    	userF.setEmpId(staff.getEmpId());
	    	userF.setUsername(staff.getUsername());
	    	userF.setNameCn(staff.getNameCn());
	    	userF.setNameEn(staff.getNameEn());
	    	userF.setLastNameCn(staff.getLastNameCn());
	    	userF.setLastNameEn(staff.getLastNameEn());
	    	userF.setJobId(staff.getJobId());
	    	userF.setPosition(staff.getTitle());
	    	userF.setOrganization(staff.getOrganization());
    	}
		return userF;
	}
    public List<User> findAllStaff(){
    	List<User> userN=new ArrayList<User>();
    	List<StaffModel> users=staffRepository.findAll();
		if(users!=null&&!users.isEmpty()) {
			for(StaffModel ss:users) {
				User user= new User();
				user.setEmpId(ss.getEmpId());
				user.setLastNameCn(ss.getLastNameCn());
				user.setNameEn(ss.getNameEn());
				user.setLastNameEn(ss.getLastNameEn());
				user.setNameCn(ss.getNameCn());
				user.setUsername(ss.getUsername());
				userN.add(user);
			}
		}
        return userN;
    }
    public User Login(StaffModel user) {
    	logger.info("Service:Login["+user.getUsername()+"]");
    	StaffModel loger=findStaffByUserName(user.getUsername());
    	if(loger==null) {
    		return null;
    	}
    	User userF= staffToUser(loger);
    	User userG=staffToUser(loger);
    	userF.setCharacter(userG);
    	EncryptUtil util=new EncryptUtil();
    	String password=loger.getPassword();
    	if(StringUtils.isNotEmpty(password)) {
    		if(password.equals(util.md5(user.getPassword(),1))){
    			logger.info("Service:Login["+user.getUsername()+"] got user");
    			return userF;
    		}else {
    			logger.info("Service:Login["+user.getUsername()+"] password is not vaild");
    			userF.setLoginMessage("password");
    		}
    	}else {
    		logger.info("Service:Login["+user.getUsername()+"] has no password");
    		userF.setLoginMessage("password");
    	}
    	
        return userF;
    }
    public String userToJson(User user) {
	   	 // 创建 MappingJackson2HttpMessageConverter 对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 将对象转换为 JSON 字符串
        String result=null;
		try {
			String json = converter.getObjectMapper().writeValueAsString(user);
		       // 加密 JSON 字符串
			result = util.encrypt(json);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        return result;
    }
    public User jsonToUser(String json) {
    	User user =null;
    	String decryptedValue = util.decrypt(json);
    	ObjectMapper objectMapper = new ObjectMapper();
    	try {
			user = objectMapper.readValue(decryptedValue, User.class);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return user;
    }
    public User getUser(HttpServletRequest request) {
    	Cookie[] cookies = request.getCookies();
        User user=null;
        if (cookies != null) {
            // 遍历所有 Cookie
            for (Cookie cookie : cookies) {
                if ("granosUser".equals(cookie.getName())) {
                    // 如果找到名为 "user" 的 Cookie，则获取其值
                    String userValue = cookie.getValue();
                    user = jsonToUser(userValue);
                    request.setAttribute("user", user);
                }
            }
        }
		return user;
    	
    }
}