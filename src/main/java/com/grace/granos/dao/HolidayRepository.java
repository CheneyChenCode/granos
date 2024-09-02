package com.grace.granos.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.HolidaysModel;

@Repository
public class HolidayRepository {
	private static final Logger logger = LoggerFactory.getLogger(HolidayRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public int[] addHolidays(List<HolidaysModel> models){
		String sql = " INSERT INTO granos.holidays ( "
			  	   + "		date, description, day_code "
			  	   + " ) "
			  	   + " VALUES ( "
			  	   + "		?, ?, ? "
			  	   + " ) ";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setDate(1, models.get(i).getDate());//date
				ps.setString(2, models.get(i).getDescription());//decription
				ps.setInt(3,models.get(i).getDayCode());//day_code
			}

			@Override
			public int getBatchSize() {
				return models.size();
			}

        });

  }
	public List<HolidaysModel> findHolidaysByYear(String year){
		LocalDate date = LocalDate.of(Integer.parseInt(year), 1, 1);
		
		String sql = " SELECT "
						   + " date,description,day_code"
						   + " FROM "
						   + " granos.holidays"
						   + " WHERE"
						   + " date >= ? order by date";

		List<HolidaysModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<HolidaysModel>(HolidaysModel.class), new Object[] { Date.valueOf(date)});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<HolidaysModel> findHolidaysByYearMonth(int year,int month){
		LocalDate date = LocalDate.of(year, 1, month);
		
		String sql = " SELECT "
						   + " date,description,day_code"
						   + " FROM "
						   + " granos.holidays"
						   + " WHERE"
						   + " date >= ? order by date";

		List<HolidaysModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<HolidaysModel>(HolidaysModel.class), new Object[] { Date.valueOf(date)});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<HolidaysModel> findHolidaysByYearMonthWithOutWeekEnd(int year,int month){
		LocalDate date = LocalDate.of(year, 1, month);
		
		String sql = " SELECT "
						   + " date,description,day_code"
						   + " FROM "
						   + " granos.holidays"
						   + " WHERE"
						   + " date >= ? order by date";

		List<HolidaysModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<HolidaysModel>(HolidaysModel.class), new Object[] { Date.valueOf(date)});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
}
