package com.grace.granos.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.PayrollModel;

@Repository
public class PayrollRepository {
	private static final Logger logger = LoggerFactory.getLogger(PayrollRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public List<PayrollModel> findPayrollByUserMon(PayrollModel pay){
		String sql = " SELECT "
						   + " r.emp_id,r.year,r.month,r.day,r.pay_code,p.title,SUM(r.hours) hours,p.coefficient"
						   + " FROM "
						   + " payroll r inner join pay_code p on p.id=r.pay_code"
						   + " WHERE"
						   + " r.year = ? and r.month = ? and r.emp_id = ?"
						   + " GROUP BY r.emp_id,r.year,r.month,r.day,r.pay_code,p.title,p.coefficient"
						   + " order by r.pay_code";

		List<PayrollModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<PayrollModel>(PayrollModel.class), new Object[] { pay.getYear(),pay.getMonth(),pay.getEmpId()});
		if(result != null && result.size() > 0) {
			return result;
		}
		return null;
	}
}
