package com.grace.granos.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.PayrollModel;

@Repository
public class PayrollRepository {
	private static final Logger logger = LoggerFactory.getLogger(PayrollRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<PayrollModel> findPayrollByAllUserMon(PayrollModel pay) {
		String sql = " SELECT " + " r.emp_id,e.name_en,e.last_name_en,r.pay_code,p.title,SUM(r.hours) hours,p.coefficient,SUM(r.tax_free_hours) tax_free_hours"
				+ " FROM " + " payroll r inner join pay_code p on p.id=r.pay_code inner join employees e on e.emp_id=r.emp_id" + " WHERE"
				+ " r.year = ? and r.month = ?"
				+ " GROUP BY r.emp_id,e.name_en,e.last_name_cn,r.pay_code,p.title,p.coefficient" + " order by r.emp_id,r.pay_code";

		List<PayrollModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<PayrollModel>(PayrollModel.class),
				new Object[] { pay.getYear(), pay.getMonth()});
		if (result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<PayrollModel> findPayrollByUserMon(PayrollModel pay) {
		String sql = " SELECT " + " r.emp_id,r.year,r.month,r.day,r.pay_code,p.title,SUM(r.hours) hours,p.coefficient,SUM(r.tax_free_hours) tax_free_hours"
				+ " FROM " + " payroll r inner join pay_code p on p.id=r.pay_code" + " WHERE"
				+ " r.year = ? and r.month = ? and r.emp_id = ?"
				+ " GROUP BY r.emp_id,r.year,r.month,r.day,r.pay_code,p.title,p.coefficient" + " order by r.pay_code";

		List<PayrollModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<PayrollModel>(PayrollModel.class),
				new Object[] { pay.getYear(), pay.getMonth(), pay.getEmpId() });
		if (result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public List<PayrollModel> findPayrollDetailByUserMon(PayrollModel pay) {
		String sql = " SELECT " + " r.emp_id,r.year,r.month,r.day,r.pay_code,p.title,r.hours,p.coefficient,r.tax_free_hours,r.from_hour,r.to_hour"
				+ " FROM " + " payroll r inner join pay_code p on p.id=r.pay_code" + " WHERE"
				+ " r.year = ? and r.month = ? and r.emp_id = ?"
				+ " order by r.day,r.from_hour,r.to_hour,r.pay_code";

		List<PayrollModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<PayrollModel>(PayrollModel.class),
				new Object[] { pay.getYear(), pay.getMonth(), pay.getEmpId() });
		if (result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
	public int deletePayrollByUserMon(PayrollModel pay){
		String sql = " DELETE "
						   + " FROM "
						   + " payroll"
						   + " WHERE"
						   + " year = ? and month = ? and emp_id = ? ";
		int result = jdbcTemplate.update(sql, pay.getYear(),pay.getMonth(),pay.getEmpId());
		return result;
	}
	public int[] addPayroll(List<PayrollModel> models) {
		String sql = " INSERT INTO payroll ( " 
				+ "		emp_id, year, month," 
				+ "		day, pay_code, hours,"
				+ "		creater, tax_free_hours,from_hour,"
				+ "		to_hour"
				+ " ) " 
				+ " VALUES ( " 
				+ "		?, ?, ?, " 
				+ "		?, ?, ?, "
				+ "		?, ?, ?, " 
				+ "		? " 
				+ " ) ";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, models.get(i).getEmpId());// emp_id
				ps.setInt(2, models.get(i).getYear());// year
				ps.setInt(3, models.get(i).getMonth());// month
				ps.setInt(4, models.get(i).getDay());// day
				ps.setInt(5, models.get(i).getPayCode());// paycode
				ps.setFloat(6, models.get(i).getHours());// hours
				ps.setString(7, models.get(i).getCreater());// creater
				ps.setFloat(8, models.get(i).getTaxFreeHours());// taxFreeHours
				ps.setFloat(9, models.get(i).getFromHour());// fromHour
				ps.setFloat(10, models.get(i).getToHour());// toHour
			}

			@Override
			public int getBatchSize() {
				return models.size();
			}

		});
	}
}
