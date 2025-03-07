package com.grace.granos.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.LeaveRequestModel;

@Repository
public class LeaveRequestRepository {
	private static final Logger logger = LoggerFactory.getLogger(LeaveRequestRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	public List<LeaveRequestModel> findAllLeaveRequestByUser(int empId){
		String sql = " SELECT "
						   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
						   + " FROM "
						   + " leave_requests"
						   + " WHERE status=1"
						   + " and emp_id = ?"
						   + " order by year, month,day,from_time,to_time";
		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { empId });
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveRequestModel> findMSLRequestByYear(LeaveRequestModel lr){
		String sql = " SELECT "
				   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
				   + " FROM "
				   + " leave_requests"
				   + " WHERE status=1"
				   + " and year = ? and emp_id = ?"
				   + " and shift='MSL'"
				   + " order by month,day,seq,from_time,to_time";
		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getYear(),lr.getEmpId()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveRequestModel> findAllMSLRequestByYear(LeaveRequestModel lr){
		String sql = " SELECT "
				   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
				   + " FROM "
				   + " leave_requests"
				   + " WHERE status=1"
				   + " and year = ?"
				   + " and shift='MSL'"
				   + " order by emp_id, month,day,seq,from_time,to_time";
		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getYear()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveRequestModel> findAllMSLRequestByMon(LeaveRequestModel lr){
		String sql = " SELECT "
				   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
				   + " FROM "
				   + " leave_requests"
				   + " WHERE status=1"
				   + " and year = ?"
				   + " and shift='MSL' and month = ?"
				   + " order by day,seq,from_time,to_time";
		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getYear(),lr.getMonth()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveRequestModel> findMSLRequestByMon(LeaveRequestModel lr){
		String sql = " SELECT "
				   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
				   + " FROM "
				   + " leave_requests"
				   + " WHERE status=1"
				   + " and emp_id = ? and year = ?"
				   + " and shift='MSL' and month = ?"
				   + " order by day,seq,from_time,to_time";
		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getEmpId(),lr.getYear(),lr.getMonth()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveRequestModel> findLastLeaveRequest(LeaveRequestModel lr){
		String sql = " SELECT "
						   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
						   + " FROM "
						   + " leave_requests"
						   + " WHERE "
						   + " status = 1 and emp_id = ? and (year > ? || (year =? and month >= ?))"
						   + " and from_time <= current_timestamp() and to_time <= current_timestamp()"
						   + " order by year, month,day,from_time,to_time";
		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getEmpId(),lr.getYear(),lr.getYear(),lr.getMonth()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}

	public List<LeaveRequestModel> findLastNewBalancesRequestByUser(LeaveRequestModel lr){
		String sql = " SELECT "
						   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
						   + " FROM "
						   + " leave_requests"
						   + " WHERE status=1"
						   + " and (year > ? || (year = ? and month >= ?))  and emp_id = ?"
						   + " and source = 'balances' and note = 'reset'"
						   + " order by year, month,day,seq,from_time,to_time";

		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getYear(),lr.getYear(),lr.getMonth(),lr.getEmpId()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveRequestModel> findAllNewBalancesRequestByYear(LeaveRequestModel lr){
		String sql = " SELECT "
						   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
						   + " FROM "
						   + " leave_requests"
						   + " WHERE status=1"
						   + " and year = ? "
						   + " and source = 'balances' and note = 'reset'"
						   + " order by emp_id, month,day,seq,from_time,to_time";

		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getYear()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveRequestModel> findLeaveRequestByUserYear(LeaveRequestModel lr){
		String sql = " SELECT "
						   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
						   + " FROM "
						   + " leave_requests"
						   + " WHERE status=1"
						   + " and year = ? and emp_id = ?"
						   + " and from_time <= current_timestamp() and to_time <= current_timestamp() "
						   + " order by month,day,seq,from_time,to_time";

		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getYear(),lr.getEmpId()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveRequestModel> findLeaveRequestByUserMon(LeaveRequestModel lr){
		String sql = " SELECT "
						   + " emp_id, year, month, day, seq, shift, from_time, to_time, hours, status, reason, note, source, requester, approved_by, approved_time"
						   + " FROM "
						   + " leave_requests"
						   + " WHERE status=1"
						   + " and year = ? and month = ? and emp_id = ?"
						   + " and from_time <= current_timestamp() and to_time <= current_timestamp()"
						   + " order by from_time,to_time";

		List<LeaveRequestModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LeaveRequestModel>(LeaveRequestModel.class), new Object[] { lr.getYear(),lr.getMonth(),lr.getEmpId()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public int deleteLeaveSumRequestByMon(LeaveRequestModel lr){
		String sql = " DELETE "
						   + " FROM "
						   + " leave_requests"
						   + " WHERE status=1"
						   + " and source = 'balances'"
						   + " and hours < 0"
						   + " and (year > ? || (year = ? and month >= ?)) and emp_id = ?";
		int result = jdbcTemplate.update(sql, lr.getYear(),lr.getYear(),lr.getMonth(),lr.getEmpId());
		return result;
	}
	
	public int deleteLeaveRequestByUserMon(LeaveRequestModel lr){
		String sql = " DELETE "
						   + " FROM "
						   + " leave_requests"
						   + " WHERE status=1"
						   + " and year = ? and month = ? and emp_id = ? and source=?";
		int result = jdbcTemplate.update(sql, lr.getYear(),lr.getMonth(),lr.getEmpId(),lr.getSource());
		return result;
	}

	public int[] addLeaveRequest(List<LeaveRequestModel> models){
		String sql = " INSERT INTO leave_requests ( "
			  	   + "		emp_id, year, month, "
			  	   + "		day, seq, shift, from_time,"
			  	   + "		to_time, hours, status,"
			  	   + "		reason, note, "
			  	   + "		source, requester, approved_by, approved_time"
			  	   + " ) "
			  	   + " VALUES ( "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?"
			  	   + " ) ";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				ps.setInt(1, models.get(i).getEmpId());//emp_id
				ps.setInt(2, models.get(i).getYear());//year
				ps.setInt(3,models.get(i).getMonth());//month
				ps.setInt(4,models.get(i).getDay());//day
				ps.setInt(5, models.get(i).getSeq());//seq
				ps.setString(6, models.get(i).getShift());//shift
				if(models.get(i).getFromTime()!=null) {
					ps.setString(7,dateFormat.format(models.get(i).getFromTime()));
				}else {
					ps.setTimestamp(7, null);//from_time
				}
				if(models.get(i).getToTime()!=null) {
					ps.setString(8,dateFormat.format(models.get(i).getToTime()));
				}else {
					ps.setTimestamp(8, null);//to_time
				}
				ps.setFloat(9, models.get(i).getHours());//hours
				ps.setInt(10, models.get(i).getStatus());//staus
				ps.setString(11, models.get(i).getReason());//reason
				ps.setString(12, models.get(i).getNote());//note
				ps.setString(13, models.get(i).getSource());//source
				ps.setString(14, models.get(i).getRequester());//requester
				ps.setString(15, models.get(i).getApprovedBy());//approved_by
				if(models.get(i).getApprovedTime()!=null) {
					ps.setString(16,dateFormat.format(models.get(i).getApprovedTime()));
				}else {
					ps.setTimestamp(16, null);//arrival_datetime
				}
			}

			@Override
			public int getBatchSize() {
				return models.size();
			}

        });
	}
}
