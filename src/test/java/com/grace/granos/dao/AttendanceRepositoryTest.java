package com.grace.granos.dao;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.grace.granos.GranosApplication;
import com.grace.granos.model.AttendanceModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GranosApplication.class)
public class AttendanceRepositoryTest {
	@Autowired
	private AttendanceRepository attendanceRepository;
	@Test
	public void findLastPeriodWdByUserMon() throws Exception {
		AttendanceModel at = new AttendanceModel();
		at.setYear(2024);
		at.setMonth(9);
		at.setEmpId(1);
		AttendanceModel testAtt=null;
		try {
			testAtt=attendanceRepository.findLastAttByUserMon(at);
		}catch (EmptyResultDataAccessException e) {
	        // 查询结果为空时，返回一个默认值
			testAtt=null;  // 或者你希望的其他默认值
	    }
		Assert.assertNotNull(testAtt);
	}
}
