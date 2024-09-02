package com.grace.granos.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

import com.grace.granos.model.StaffModel;
import com.grace.granos.model.User;
import com.grace.granos.util.EncryptUtil;
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
    public Iterable<StaffModel> getStaff() {
        return staffRepository.findAll();
    }
    public StaffModel findStaffByUsername(StaffModel user) throws Exception {
    	if(StringUtils.isEmpty(user.getUsername())) {
    		throw new Exception("there is no username");
    	}
        return staffRepository.findStaffByusername(user);
    }
    public User Login(StaffModel user) {
    	logger.info("Service:Login["+user.getUsername()+"]");
    	StaffModel loger=staffRepository.findStaffByusername(user);
    	if(loger==null) {
    		return null;
    	}
    	User userF=new User();
    	userF.setEmpId(loger.getEmpId());
    	userF.setUsername(loger.getUsername());
    	userF.setNameCn(loger.getNameCn());
    	userF.setNameEn(loger.getNameEn());
    	userF.setLastNameCn(loger.getLastNameCn());
    	userF.setLastNameEn(loger.getLastNameEn());
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
}