package com.grace.granos.dao;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.grace.granos.GranosApplication;
import com.grace.granos.dao.StaffRepository;
import com.grace.granos.model.StaffModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GranosApplication.class)
public class StaffRepositoryTest {
	@Autowired
	private StaffRepository staffDao;

	@Test
	public void findStaffByusername() throws Exception {
		StaffModel user =new StaffModel();
		user.setUsername("0001");
		user.setEmpId(1);;
	        //檢查結果
		user=staffDao.findStaffByUserName(user.getUsername());
		System.out.println(user.getEmail());
		System.out.println(user.getCreateTime());
		System.out.println(user.getEntryDate());
	        Assert.assertNotNull(user);
		
	}
	@Test
	public void findAll() throws Exception {
		Iterable<StaffModel> users =staffDao.findAll();
	        //檢查結果
		 for(StaffModel user : users) {
		        System.out.println(user.getCreateTime());
		        Assert.assertNotNull(user);
		    }
		
	}
}
