package com.grace.granos.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.StaffModel;

@Repository
public class StaffRepository {
	private static final Logger logger = LoggerFactory.getLogger(StaffRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public int addStaff(StaffModel staffModel){
		String sql = " INSERT INTO member_account ( "
			  	   + "		USERNAME, PASSWORD, SALT, "
			  	   + "		CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME "
			  	   + " ) "
			  	   + " VALUE ( "
			  	   + "		:username, :password, :salt, "
			  	   + "		:create_by, NOW(), :update_by, NOW() "
			  	   + " ) ";
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(staffModel);
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(sql, paramSource, keyHolder);
		return keyHolder.getKey().intValue();
  }
	public StaffModel findStaffByusername(StaffModel staffModel){
		String sql = " SELECT "
						   + "		emp_id,name_en,name_cn,email,create_time,entry_date,status,leave_date,password,last_name_en,last_name_cn,username "
						   + " FROM "
						   + "		employees"
						   + " WHERE "
						   + "		username = ? ";

		List<StaffModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<StaffModel>(StaffModel.class), new Object[] { staffModel.getUsername() });
		if(result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}
	public Iterable<StaffModel> findAll(){
		String sql = " SELECT "
						   + "		emp_id,name_en,name_cn,email,create_time,entry_date,status,leave_date,password,last_name_en,last_name_cn,username "
						   + " FROM "
						   + "		employees";

		List<StaffModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<StaffModel>(StaffModel.class));
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
}
