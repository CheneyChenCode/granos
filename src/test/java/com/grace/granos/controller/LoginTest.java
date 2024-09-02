package com.grace.granos.controller;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;

import com.grace.granos.GranosApplication;
import com.grace.granos.dao.StaffRepository;
import com.grace.granos.model.StaffModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GranosApplication.class)
public class LoginTest {
	@Autowired
	private StaffRepository staffDao;

	@Test
	public void md5(String src ,String salt) throws Exception {
        
		
	}
}
