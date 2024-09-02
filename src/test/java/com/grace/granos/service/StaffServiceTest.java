package com.grace.granos.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.grace.granos.GranosApplication;
import com.grace.granos.model.StaffModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GranosApplication.class)
public class StaffServiceTest {
	@Autowired
	private StaffService service;

	@Test
	public void Login() throws Exception {
		StaffModel user=new StaffModel();
		user.setUsername("0001");user.setPassword("12345");
		Assert.assertNotNull(service.Login(user));
	}
}
