package com.grace.granos.dao;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.grace.granos.GranosApplication;
import com.grace.granos.model.LeaveRequestModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GranosApplication.class)
public class LeaveRequestRepositoryTest {
	@Autowired
	private LeaveRequestRepository leaveRequestRepository;
	@Test
	public void deleteLeaveRequestByUserMon() throws Exception {
		LeaveRequestModel model=new LeaveRequestModel();
		model.setEmpId(1);
		model.setYear(2024);
		model.setMonth(5);
		model.setSource("attendance");
		int count = leaveRequestRepository.deleteLeaveRequestByUserMon(model);
		Assert.assertNotNull(count);
	}
	@Test
	public void addLeaveRequest() throws Exception {
		List<LeaveRequestModel> models=new ArrayList<LeaveRequestModel>();
		LeaveRequestModel model=new LeaveRequestModel();
		model.setEmpId(1);
		model.setYear(2024);
		model.setMonth(5);
		model.setDay(1);
		model.setFromTime(Timestamp.valueOf(LocalDateTime.of(2024, 5, 1, 23, 59)));
		model.setToTime(Timestamp.valueOf(LocalDateTime.of(2024, 5, 2, 0, 0)));
		model.setHours(1);
		model.setApprovedBy("0001");
		model.setApprovedTime(Timestamp.valueOf(LocalDateTime.now()));
		model.setNote("test");
		model.setReason("test2");
		model.setShift("NTL");
		model.setStatus(1);
		model.setSource("attendance");
		model.setRequester("0001");
		model.setSeq(1);
		models.add(model);
		int[] a=leaveRequestRepository.addLeaveRequest(models);
		Assert.assertNotNull(a);
	}
	@Test
	public void findLeaveRequestForPayByUserMon() {
		LeaveRequestModel model=new LeaveRequestModel();
		model.setEmpId(1);
		model.setYear(2024);
		model.setMonth(5);
		List<LeaveRequestModel> models=leaveRequestRepository.findLeaveRequestByUserMon(model);
		Assert.assertNotNull(models);
	}
}
