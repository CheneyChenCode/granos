package com.grace.granos.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grace.granos.dao.ShiftRepository;
import com.grace.granos.model.LeaveRequestModel;
import com.grace.granos.model.ShiftModel;

@Service
public class ShiftService {
	@Autowired
	private ShiftRepository shiftRepository;
	
	public Map<String, ShiftModel> getshiftModelMap(){
		List<ShiftModel> shifts = shiftRepository.findShift();
		Map<String, ShiftModel> shiftModelMap = shifts.stream()
				.collect(Collectors.toMap(ShiftModel::getName, shiftModel -> shiftModel));
		LeaveRequestModel lr=new LeaveRequestModel();
		return shiftModelMap;
	}
}
