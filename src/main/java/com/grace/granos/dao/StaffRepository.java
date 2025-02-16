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

	public int addStaff(StaffModel staffModel) {
		String sql = " INSERT INTO member_account ( " + "		USERNAME, PASSWORD, SALT, "
				+ "		CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME " + " ) " + " VALUE ( "
				+ "		:username, :password, :salt, " + "		:create_by, NOW(), :update_by, NOW() " + " ) ";
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(staffModel);
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(sql, paramSource, keyHolder);
		return keyHolder.getKey().intValue();
	}
	public StaffModel findStaffByUserName(String name) {
		String sql = " SELECT "
				+ " e.emp_id,e.name_en,e.name_cn,e.email,e.create_time,e.entry_date,e.status,e.leave_date,e.password,e.last_name_en,e.last_name_cn,e.username,e.job_id,e.org_id,e.gender,j.title,o.description organization"
				+ " FROM" + " employees e left join job j on j.id=e.job_id left join organization o on o.id=e.org_id" + " WHERE" + " e.username = ? ";

		List<StaffModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<StaffModel>(StaffModel.class),
				new Object[] { name });
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}
	public StaffModel findStaffById(int empId) {
		String sql = " SELECT "
				+ " e.emp_id,e.name_en,e.name_cn,e.email,e.create_time,e.entry_date,e.status,e.leave_date,e.password,e.last_name_en,e.last_name_cn,e.username,e.job_id,e.org_id,e.gender,j.title,o.description organization"
				+ " FROM" + " employees e left join job j on j.id=e.job_id left join organization o on o.id=e.org_id" + " WHERE" + " e.emp_id = ? ";

		List<StaffModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<StaffModel>(StaffModel.class),
				new Object[] { empId });
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	public List<StaffModel> findAll() {
		String sql = " SELECT "
				+ " e.emp_id,e.name_en,e.name_cn,e.email,e.create_time,e.entry_date,e.status,e.leave_date,e.password,e.last_name_en,e.last_name_cn,e.username,e.job_id,e.org_id,e.gender,j.title,o.description organization"
				+ " FROM" + " employees e left join job j on j.id=e.job_id left join organization o on o.id=e.org_id" + " WHERE" + " e.status = 1";
		List<StaffModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<StaffModel>(StaffModel.class));
		if (result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
}
