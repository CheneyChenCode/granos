package com.grace.granos.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.ShiftModel;

@Repository
public class ShiftRepository {
	private static final Logger logger = LoggerFactory.getLogger(ShiftRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	public List<ShiftModel> findShift(){
		String sql = " SELECT "
						   + " id,name,start_time,end_time,base_hours,rest_hours,rest_start_hour,description"
						   + " FROM "
						   + " shifts";

		List<ShiftModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<ShiftModel>(ShiftModel.class));
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public ShiftModel findShiftByNname(ShiftModel model){
		String sql = " SELECT "
						   + "		id,name,start_time,end_time,base_hours,rest_hours,rest_start_hour,description "
						   + " FROM "
						   + " shifts"
						   + " WHERE "
						   + " name = ? ";

		List<ShiftModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<ShiftModel>(ShiftModel.class), new Object[] { model.getName() });
		if(result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}
}
