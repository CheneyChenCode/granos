package com.grace.granos.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.LeaveBalanceModel;

@Repository
public class LeaveBalanceRepository {
	private static final Logger logger = LoggerFactory.getLogger(LeaveBalanceRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public int[] addLeaveBalance(List<LeaveBalanceModel> models) {
		String sql = " INSERT INTO leave_balances ( " + "		emp_id, year, month, " + "		shift, used_hours,"
				+ "		remaining_hours, creater" + " ) " + " VALUES ( " + "		?, ?, ?, " + "		?, ?, ?, "
				+ "		?" + " ) ";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				ps.setInt(1, models.get(i).getEmpId());// emp_id
				ps.setInt(2, models.get(i).getYear());// year
				ps.setInt(3, models.get(i).getMonth());// month
				ps.setString(4, models.get(i).getShift());// shift
				ps.setFloat(5, models.get(i).getUsedHours());// used_hours
				ps.setFloat(6, models.get(i).getRemainingHours());// remaining_hours
				ps.setString(7, models.get(i).getCreater());// creater
			}

			@Override
			public int getBatchSize() {
				return models.size();
			}

		});
	}

	public int deleteLeaveBanlanceByUserMon(LeaveBalanceModel lr) {
		String sql = " DELETE " + " FROM " + " leave_balances" + " WHERE" + " year >= ? and month >= ? and emp_id = ?";
		int result = jdbcTemplate.update(sql, lr.getYear(), lr.getMonth(), lr.getEmpId());
		return result;
	}

	public List<LeaveBalanceModel> findLeaveBalanceByUserYear(LeaveBalanceModel lr) {
		String sql = " SELECT " + " l.emp_id, l.year, l.month, l.shift, l.used_hours, l.remaining_hours,s.description" + " FROM "
				+ " leave_balances l inner join shifts s on s.name=l.shift" + " WHERE " + " l.emp_id = ? "
				+" AND ((l.year = ? )|| (l.year = ? and l.month = ?))"
				+ " order by year,month,shift";

		List<LeaveBalanceModel> result = jdbcTemplate.query(sql,
				new BeanPropertyRowMapper<LeaveBalanceModel>(LeaveBalanceModel.class),
				new Object[] { lr.getEmpId(),lr.getYear(), lr.getYear()-1,12 });
		if (result != null && result.size() > 0) {
			return result;
		}
		return null;
	}

	public List<LeaveBalanceModel> findLeaveBalanceByUserYearMon(LeaveBalanceModel lb) {
		String sql = "SELECT " + " emp_id, year, month, shift, used_hours, remaining_hours" + " FROM"
				+ " leave_balances " + " WHERE " + " year = ? and month = ? and emp_id = ?"+" ORDER BY shift";

		List<LeaveBalanceModel> result = jdbcTemplate.query(sql,
				new BeanPropertyRowMapper<LeaveBalanceModel>(LeaveBalanceModel.class),
				new Object[] { lb.getYear(),lb.getMonth(), lb.getEmpId() });
		if (result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<LeaveBalanceModel> findLastLeaveBalance(int empId) {
		String sql = "SELECT " + " emp_id, year, month, shift, used_hours, remaining_hours" + " FROM"
				+ " leave_balances" + " WHERE " + " emp_id = ? "
				+ " AND year = (SELECT MAX(year) FROM leave_balances)"
				+ " AND month = (SELECT MAX(month) FROM leave_balances WHERE year = (SELECT MAX(year) FROM leave_balances))"
				+" ORDER BY shift";

		List<LeaveBalanceModel> result = jdbcTemplate.query(sql,
				new BeanPropertyRowMapper<LeaveBalanceModel>(LeaveBalanceModel.class),
				new Object[] { empId });
		if (result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
}
