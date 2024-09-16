package com.grace.granos.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.grace.granos.GranosApplication;
import com.grace.granos.model.DayCodeModel;
import com.grace.granos.model.StaffModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GranosApplication.class)
public class PayCodeRepositoryTest {
	@Autowired
	private PayCodeRepository payCodeRepository;
	@Test
	public void findPayCodeByNow() throws Exception {
		List<DayCodeModel> dayCodes = payCodeRepository.findPayCodeByNow();
		Assert.assertNotNull(dayCodes);
	}
}
