package com.grace.granos.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.AttendanceModel;

@Repository
public class AttendanceRepository {
	private static final Logger logger = LoggerFactory.getLogger(AttendanceRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	public List<AttendanceModel> checkAttendanceForPayByUserMon(AttendanceModel att){
		String sql = " SELECT "
						   + " emp_id,year,month,day"
						   + " FROM "
						   + " attendance"
						   + " WHERE status <> 1"
						   + " AND year = ? and month = ? and emp_id = ?";

		List<AttendanceModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<AttendanceModel>(AttendanceModel.class), new Object[] { att.getYear(),att.getMonth(),att.getEmpId()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<AttendanceModel> findAttendanceForPayByUserMon(AttendanceModel att){
		String sql = " SELECT "
						   + " emp_id,year,month,day,seq,work_hours,overtime,shift,day_code,remain_tax_free"
						   + " FROM "
						   + " attendance"
						   + " WHERE "
						   + " year = ? and month = ? and emp_id = ? order by day,seq";

		List<AttendanceModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<AttendanceModel>(AttendanceModel.class), new Object[] { att.getYear(),att.getMonth(),att.getEmpId()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<AttendanceModel> findAttendanceByUserMon(AttendanceModel att){
		String sql = " SELECT "
						   + " emp_id,year,month,day,seq,start_datetime,week,arrival_datetime,end_datetime,leave_datetime,work_hours,overtime,approval,note,reason,shift,day_code,status"
						   + " FROM "
						   + " attendance"
						   + " WHERE"
						   + " year = ? and month = ? and emp_id = ? order by day,seq";

		List<AttendanceModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<AttendanceModel>(AttendanceModel.class), new Object[] { att.getYear(),att.getMonth(),att.getEmpId()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public int deleteAttendanceByUserMon(AttendanceModel att){
		String sql = " DELETE "
						   + " FROM "
						   + " attendance"
						   + " WHERE"
						   + " year = ? and month = ? and emp_id = ? ";
		int result = jdbcTemplate.update(sql, att.getYear(),att.getMonth(),att.getEmpId());
		return result;
	}
	public int findLastfixedDayOffInPeriod(AttendanceModel att){
		String sql = " SELECT "
						   + " COUNT(0)"
						   + " FROM "
						   + " attendance"
						   + " WHERE"
						   + " year = ? and month = ? and emp_id = ? and shift ='DXF'"
						   + " AND day >= (select MAX(day) from granos.attendance where year = ? and month = ? and emp_id = ? and period =1)";

		int result = jdbcTemplate.queryForObject(sql, Integer.class, att.getYear(),att.getMonth()-1,att.getEmpId(),att.getYear(),att.getMonth()-1,att.getEmpId());
		return result;
	}
	public int findLastflexibleDayOffInPeriod(AttendanceModel att){
		String sql = " SELECT "
						   + " COUNT(0)"
						   + " FROM "
						   + " attendance"
						   + " WHERE"
						   + " year = ? and month = ? and emp_id = ? and shift ='DLF'"
						   + " AND day >= (select MAX(day) from granos.attendance where year = ? and month = ? and emp_id = ? and period =1)";

		int result = jdbcTemplate.queryForObject(sql, Integer.class, att.getYear(),att.getMonth()-1,att.getEmpId(),att.getYear(),att.getMonth()-1,att.getEmpId());
		return result;
	}
	public int findLastAccumulateByUserMon(AttendanceModel att){
		String sql = " SELECT "
						   + " COUNT(0)"
						   + " FROM "
						   + " attendance"
						   + " WHERE"
						   + " year = ? and month = ? and emp_id = ? and work_hours  <> 0"
						   + " AND day >= (select MAX(day) from granos.attendance where year = ? and month = ? and emp_id = ? and work_hours > 0)";

		int result = jdbcTemplate.queryForObject(sql, Integer.class, att.getYear(),att.getMonth()-1,att.getEmpId(),att.getYear(),att.getMonth()-1,att.getEmpId());
		return result;
	}
	public int findLastPeriodWdByUserMon(AttendanceModel att){
		String sql = " SELECT "
						   + " period"
						   + " FROM "
						   + " attendance"
						   + " WHERE"
						   + " year = ? and month = ? and emp_id = ?"
						   + " AND day = (select MAX(day) from granos.attendance where year = ? and month = ? and emp_id = ?)";

		int result = jdbcTemplate.queryForObject(sql, Integer.class, att.getYear(),att.getMonth()-1,att.getEmpId(),att.getYear(),att.getMonth()-1,att.getEmpId());
		return result;
	}
	public int[] addAttendance(List<AttendanceModel> models){
		String sql = " INSERT INTO attendance ( "
			  	   + "		emp_id, year, month, "
			  	   + "		day, seq, arrival_datetime, leave_datetime,"
			  	   + "		work_hours, note, approval,"
			  	   + "		creater, day_code, "
			  	   + "		overtime, start_datetime, end_datetime, week,"
			  	   + "		reason, shift, status,comp_time,comp_reason,"
			  	   + "		period,paid_leave,remain_tax_free,over_start_datetime,"
			  	   + "		over_end_datetime"
			  	   + " ) "
			  	   + " VALUES ( "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ?, ?, "
			  	   + "		?, ? "
			  	   + " ) ";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, models.get(i).getEmpId());//emp_id
				ps.setInt(2, models.get(i).getYear());//year
				ps.setInt(3,models.get(i).getMonth());//month
				ps.setInt(4,models.get(i).getDay());//day
				ps.setInt(5, models.get(i).getSeq());//seq
				if(models.get(i).getArrivalDatetime()!=null) {
					ps.setTimestamp(6, Timestamp.valueOf(models.get(i).getArrivalDatetime().toLocalDateTime().plusHours(8)));//arrival_datetime
				}else {
					ps.setTimestamp(6, null);//arrival_datetime
				}
				if(models.get(i).getLeaveDatetime()!=null) {
					ps.setTimestamp(7, Timestamp.valueOf(models.get(i).getLeaveDatetime().toLocalDateTime().plusHours(8)));//leave_datetime
				}else {
					ps.setTimestamp(7, null);//leave_datetime
				}
				ps.setFloat(8, models.get(i).getWorkHours());//work_hours
				ps.setString(9, models.get(i).getNote());//note
				ps.setString(10, models.get(i).getApproval());//approval
				ps.setString(11, models.get(i).getCreater());//creater
				ps.setInt(12, models.get(i).getDayCode());//dayCode
				ps.setFloat(13, models.get(i).getOvertime());//overTime
				if(models.get(i).getStartDatetime()!=null) {
					ps.setTimestamp(14, Timestamp.valueOf(models.get(i).getStartDatetime().toLocalDateTime().plusHours(8)));//startDatetime
				}else {
					ps.setTimestamp(14, null);//startDatetime
				}
				if(models.get(i).getEndDatetime()!=null) {
					ps.setTimestamp(15, Timestamp.valueOf(models.get(i).getEndDatetime().toLocalDateTime().plusHours(8)));//endDatetime
				}else {
					ps.setTimestamp(15, null);//endDatetime
				}
				ps.setInt(16, models.get(i).getWeek());//week
				ps.setString(17, models.get(i).getReason());//reason
				ps.setString(18, models.get(i).getShift());//shift
				ps.setInt(19, models.get(i).getStatus());//status
				ps.setFloat(20, models.get(i).getCompTime());//comp_time
				ps.setString(21, models.get(i).getCompReason());//comp_reason
				ps.setInt(22, models.get(i).getPeriod());//period
				ps.setFloat(23, models.get(i).getPaidLeave());//paid_leave
				ps.setFloat(24, models.get(i).getRemainTaxFree());//remain_tax_free
				if(models.get(i).getOverStartDatetime()!=null) {
					ps.setTimestamp(25, Timestamp.valueOf(models.get(i).getOverStartDatetime().toLocalDateTime().plusHours(8)));//overStartDatetime
				}else {
					ps.setTimestamp(25, null);//overStartDatetime
				}
				if(models.get(i).getOverEndDatetime()!=null) {
					ps.setTimestamp(26, Timestamp.valueOf(models.get(i).getOverEndDatetime().toLocalDateTime().plusHours(8)));//overEndDatetime
				}else {
					ps.setTimestamp(26, null);//overEndDatetime
				}
			}

			@Override
			public int getBatchSize() {
				return models.size();
			}

        });

  }
}
