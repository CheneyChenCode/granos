package com.grace.granos.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.grace.granos.model.DayCodeModel;
import com.grace.granos.model.PayCodeModel;
@Repository
public class PayCodeRepository {
	private static final Logger logger = LoggerFactory.getLogger(PayCodeRepository.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	public List<PayCodeModel> findLeavePayCode(){
		String sql = " SELECT "
						   + " p.id,p.day_code,p.shift,p.hour_part_greater,p.hour_part_less,p.coefficient,p.description,p.title,d.description day_code_desc"
						   + " FROM"
						   + " pay_code p inner join granos.day_code d on d.id =p.day_code"
						   + " WHERE"
						   + " now() >= p.active_time and (now() <= p.disable_time or p.disable_time is null)"
						   + " and char_length(p.shift) >2 and p.hour_part_greater=0"
						   + " ORDER BY p.day_code,p.id";

		List<PayCodeModel> result = jdbcTemplate.query(sql,
				new BeanPropertyRowMapper<PayCodeModel>(PayCodeModel.class));

		return result;
	}
	public List<DayCodeModel> findPayCodeByNow(){
		String sql = " SELECT "
						   + " p.id,p.day_code,p.shift,p.hour_part_greater,p.hour_part_less,p.coefficient,p.description,p.title,d.description day_code_desc"
						   + " FROM"
						   + " pay_code p inner join granos.day_code d on d.id =p.day_code"
						   + " WHERE"
						   + " now() >= p.active_time and (now() <= p.disable_time or p.disable_time is null)"
						   + " ORDER BY p.day_code,p.id";
		DayCodeRowMapper rowMapper = new DayCodeRowMapper();
		jdbcTemplate.query(sql, rowMapper);
		List<DayCodeModel> result = rowMapper.getDayCodeList();
//		List<PayCodeModel> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PayCodeModel.class));
//		if(result != null && result.size() > 0) {
//			List<DayCodeModel> dcms=new ArrayList<DayCodeModel>();
//			for(PayCodeModel pcm:result) {
//				Optional<DayCodeModel> dcm=dcms.stream().filter(x -> x.getDayCode() == pcm.getDayCode()).findFirst();
//				if(dcm.isPresent()) {
//					dcm.get().getPayCode().add(pcm);
//				}
//				else {
//					dcms.add(new DayCodeModel() {{
//						setDayCode(pcm.getDayCode());
//						List<PayCodeModel> pms=new ArrayList<PayCodeModel>();
//						pms.add(pcm);
//						setPayCode(pms);
//			        }});
//				}
//			}
//			return dcms;
//		}
		return result;
	}
	public class DayCodeRowMapper implements RowMapper<DayCodeModel> {
	    private Map<Integer, DayCodeModel> dayCodeMap = new HashMap<>();

	    @Override
	    public DayCodeModel mapRow(ResultSet rs, int rowNum) throws SQLException {
	        int dayCode = rs.getInt("day_code");
	        DayCodeModel dayCodeModel = dayCodeMap.get(dayCode);
	        if (dayCodeModel == null) {
		        String dayCodeDesc=rs.getString("day_code_desc");
	            dayCodeModel = new DayCodeModel();
	            dayCodeModel.setDayCode(dayCode);
	            dayCodeModel.setDescription(dayCodeDesc);
	            dayCodeModel.setPayCode(new ArrayList<PayCodeModel>());
	            dayCodeMap.put(dayCode, dayCodeModel);
	        }
	        PayCodeModel payCodeModel = new PayCodeModel();
	        payCodeModel.setId(rs.getInt("id"));
	        payCodeModel.setDayCode(rs.getInt("day_code"));
	        payCodeModel.setShift(rs.getString("shift"));
	        payCodeModel.setHourPartGreater(rs.getString("hour_part_greater"));
	        payCodeModel.setHourPartLess(rs.getString("hour_part_less"));
	        payCodeModel.setCoefficient(rs.getFloat("coefficient"));
	        payCodeModel.setDescription(rs.getString("description"));
	        payCodeModel.setTitle(rs.getString("title"));

	        dayCodeModel.getPayCode().add(payCodeModel);
	        return dayCodeModel;
	    }
	    public List<DayCodeModel> getDayCodeList() {
	        return new ArrayList<>(dayCodeMap.values());
	    }
	}

}
