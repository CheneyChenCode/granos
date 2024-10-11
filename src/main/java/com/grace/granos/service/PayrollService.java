package com.grace.granos.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisCrossBetween;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.BarGrouping;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grace.granos.dao.AttendanceRepository;
import com.grace.granos.dao.PayCodeRepository;
import com.grace.granos.dao.PayrollRepository;
import com.grace.granos.model.AttendanceModel;
import com.grace.granos.model.DayCodeModel;
import com.grace.granos.model.PayCodeModel;
import com.grace.granos.model.PayrollModel;
import com.grace.granos.model.User;

@Service
public class PayrollService {
	private static final Logger logger = LoggerFactory.getLogger(PayrollService.class);
	@Autowired
	private PayCodeRepository payCodeRepository;
	@Autowired
	private PayrollRepository payrollRepository;
	@Autowired
	private AttendanceRepository attendanceRepository;

	public List<PayrollModel> calculatePayroll(int year, int month, User user) {
		logger.info("Service:calculatePayroll[" + year + "/" + month + "]");
		List<PayrollModel> payrolls = new ArrayList<PayrollModel>();
		AttendanceModel attendanceModel = new AttendanceModel();
		attendanceModel.setEmpId(user.getCharacter().getEmpId());
		attendanceModel.setYear(year);
		attendanceModel.setMonth(month);
		List<AttendanceModel> atts = attendanceRepository.findAttendanceForPayByUserMon(attendanceModel);
		if (CollectionUtils.isEmpty(atts)) {
			return payrolls;
		}
		List<DayCodeModel> dayCodes = payCodeRepository.findPayCodeByNow();
		float totalWorkhours = 0;
		float totalPaidLeave = 0;
		int day = 0;
		for (AttendanceModel att : atts) {
			float workHours = att.getWorkHours();
			float paidLeave = att.getPaidLeave();
			if (day != att.getDay()) {
				day = att.getDay();
				totalWorkhours = workHours;
				totalPaidLeave = paidLeave;
			} else {
				totalWorkhours = totalWorkhours + workHours;
				totalPaidLeave = totalPaidLeave + paidLeave;
			}
			List<DayCodeModel> filAttsByDc = dayCodes.stream().filter(x -> att.getDayCode() == x.getDayCode())
					.collect(Collectors.toList());
			for (DayCodeModel dc : filAttsByDc) {
				List<PayCodeModel> filAttsByShift = dc.getPayCode().stream()
						.filter(x -> att.getDayCode() == x.getDayCode() && att.getShift().equals(x.getShift()))
						.collect(Collectors.toList());
				for (PayCodeModel pc : filAttsByShift) {
					// float hours=0;
					float currentHourPartGreater = StringUtils.isNotEmpty(pc.getHourPartGreater())
							? Float.parseFloat(pc.getHourPartGreater())
							: 0;
					float currentHourPartLess = StringUtils.isNotEmpty(pc.getHourPartLess())
							? Float.parseFloat(pc.getHourPartLess())
							: Float.MAX_VALUE;
					if (paidLeave > 0 && totalPaidLeave > currentHourPartGreater) {
						PayrollModel pl = createPayrollModel(payrolls, totalPaidLeave, att, pc, currentHourPartGreater,
								currentHourPartLess, user);
						if (pl == null) {
							continue;
						}
						logger.info("month:" + pl.getMonth() + ",day:" + pl.getDay() + ",seq" + att.getSeq() + ",shift:"
								+ pc.getShift() + ",payCode:" + pl.getPayCode());
						logger.info("workHours:" + pl.getHours() + ",TaxFreeHours:" + pl.getTaxFreeHours());
						continue;
					}

					if (totalWorkhours > currentHourPartGreater) {
						PayrollModel pl = createPayrollModel(payrolls, totalWorkhours, att, pc, currentHourPartGreater,
								currentHourPartLess, user);
						if (pl == null) {
							continue;
						}
						float taxFreeOverTime = pl.getHours();
						if (currentHourPartGreater == 0 && currentHourPartLess == 8
								&& (pc.getDayCode() == 1 || pc.getDayCode() == 5)) {
							taxFreeOverTime = 0;
						} else {
							if (att.getRemainTaxFree() < 0) {
								// Sum the salaries where the condition is met
								float totalRemainTaxFree = (float) payrolls.stream()
										.filter(x -> x.getDay() == att.getDay() && x.getYear() == att.getYear()
												&& x.getMonth() == att.getMonth()) // condition: only include if
																					// overtime is true
										.mapToDouble(PayrollModel::getTaxFreeHours).sum();
								if (taxFreeOverTime + (att.getRemainTaxFree() + totalRemainTaxFree) >= 0) {
									taxFreeOverTime = taxFreeOverTime + (att.getRemainTaxFree() + totalRemainTaxFree);
								} else {
									taxFreeOverTime = 0;
								}
							}
						}
						pl.setTaxFreeHours(taxFreeOverTime);
						logger.info("month:" + pl.getMonth() + ",day:" + pl.getDay() + ",seq" + att.getSeq() + ",shift:"
								+ pc.getShift() + ",payCode:" + pl.getPayCode());
						logger.info("workHours:" + pl.getHours() + ",TaxFreeHours:" + pl.getTaxFreeHours());
					}

				}
			}
		}
		if (payrolls.isEmpty()) {
			return payrolls;
		}
		deletePayroll(year, month, user.getCharacter().getEmpId());
		addPayroll(payrolls);
		return payrolls;
	}

	private PayrollModel createPayrollModel(List<PayrollModel> payrolls, float totalHours, AttendanceModel att,
			PayCodeModel pc, float currentHourPartGreater, float currentHourPartLess, User user) {
		if (totalHours < currentHourPartLess) {
			currentHourPartLess = totalHours;
		}
		float hours = 0;
		hours = currentHourPartLess - currentHourPartGreater;
		PayrollModel pl = null;
		if (att.getSeq() > 1) {
			List<PayrollModel> matchingPayrolls = payrolls.stream()
					.filter(p -> p.getPayCode() != pc.getId() && p.getDay() == att.getDay())
					.collect(Collectors.toList());

			if (!matchingPayrolls.isEmpty()) {
				for (PayrollModel matchingPl : matchingPayrolls) {
					if (matchingPl.getToHour() > currentHourPartGreater) {
						currentHourPartGreater = matchingPl.getToHour();
					}
					logger.info("minus:" + matchingPl.getPayCode() + ",day: " + matchingPl.getDay() + ", month: "
							+ matchingPl.getMonth() + ",from:" + matchingPl.getFromHour() + ",to:"
							+ matchingPl.getToHour());
					hours = currentHourPartLess - currentHourPartGreater;
				}
			}
		}
		if (hours > 0) {
			pl = new PayrollModel();
			payrolls.add(pl);
			pl.setEmpId(att.getEmpId());
			pl.setDay(att.getDay());
			pl.setMonth(att.getMonth());
			pl.setCreater(user.getUsername());
			pl.setPayCode(pc.getId());
			pl.setYear(att.getYear());
			pl.setTitle(pc.getTitle());
			pl.setCoefficient(pc.getCoefficient());
			pl.setFromHour(currentHourPartGreater);
			pl.setToHour(currentHourPartLess);
			pl.setHours(hours);
		}
		return pl;
	}

	public List<PayrollModel> getPayroll(int year, int month, int empId) {
		logger.info("Service:getPayroll[" + year + "/" + month + "]");
		PayrollModel payroll = new PayrollModel();
		payroll.setYear(year);
		payroll.setMonth(month);
		payroll.setEmpId(empId);
		List<PayrollModel> payrolls = null;
		payrolls = payrollRepository.findPayrollByUserMon(payroll);
		return payrolls;
	}

	public void deletePayroll(int year, int month, int empid) {
		PayrollModel payroll = new PayrollModel();
		payroll.setYear(year);
		payroll.setMonth(month);
		payroll.setEmpId(empid);
		payrollRepository.deletePayrollByUserMon(payroll);
	}

	public void addPayroll(List<PayrollModel> models) {
		payrollRepository.addPayroll(models);
	}

	public ByteArrayOutputStream exportAllUsersToExcel(int year, int month) throws IOException {
		PayrollModel payroll = new PayrollModel();
		payroll.setYear(year);
		payroll.setMonth(month);
		List<PayrollModel> payrolls = payrollRepository.findPayrollByAllUserMon(payroll);
		PayrollModel prePayroll = new PayrollModel();
		if (month - 1 == 0) {
			prePayroll.setYear(year - 1);
			prePayroll.setMonth(12);
		} else {
			prePayroll.setYear(year);
			prePayroll.setMonth(month - 1);
		}
		List<PayrollModel> prePayrolls = payrollRepository.findPayrollByAllUserMon(prePayroll);
		// 创建 Excel 工作簿
		XSSFWorkbook workbook = new XSSFWorkbook();

		// CellStyle dateTimeCellStyle = workbook.createCellStyle();
		// CreationHelper createHelper = workbook.getCreationHelper();
		// dateTimeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd
		// HH:mm:ss"));
		String sheetName = String.valueOf(year) + "-" + StringUtils.leftPad(String.valueOf(month), 2, "0");
		XSSFSheet sheet1 = workbook.createSheet(sheetName);
		if (!CollectionUtils.isEmpty(payrolls)) {
			// 提取唯一的 payCode 和 title
			Map<String, Float> uniquePayCodeTitleMap = payrolls.stream()
					.collect(Collectors.toMap(PayrollModel::getTitle, // key 是 payCode
							PayrollModel::getCoefficient, // value 是 title
							(existing, replacement) -> existing, // 如果 key 重複，選擇保留已有的
							LinkedHashMap::new));
			// 创建表头
			Row header1 = sheet1.createRow(0);
			header1.createCell(0).setCellValue("EMP_ID");
			header1.createCell(1).setCellValue("NAME");
			int index = 2;
			LinkedHashMap<String, Integer> colMap = new LinkedHashMap<String, Integer>();
			for (Map.Entry<String, Float> entry : uniquePayCodeTitleMap.entrySet()) {
				String key = entry.getKey(); // 獲取鍵
				Float value = entry.getValue(); // 獲取值
				header1.createCell(index).setCellValue(key + "*" + String.valueOf(value));
				colMap.put(key, index);
				index++; // 增加索引
			}
			header1.createCell(index).setCellValue("TOTAL_WEIGHTED");
			header1.createCell(index + 1).setCellValue("TOTAL_TAX_FREE_WEIGHTED");
			header1.createCell(index + 2).setCellValue("TOTAL_PREMONTH_WEIGHTED");
			// 填充数据
			Map<Integer, Float> preHoursMap = new LinkedHashMap<Integer, Float>();
			if (!CollectionUtils.isEmpty(prePayrolls)) {
				// 使用 LinkedHashMap 以 empId 為 key，將 PayrollModel 收集到 List 中
				Map<Integer, List<PayrollModel>> prePayrollMap = prePayrolls.stream()
						.collect(Collectors.groupingBy(PayrollModel::getEmpId, // 鍵是 empId
								LinkedHashMap::new, // 使用 LinkedHashMap 保持順序
								Collectors.toList() // 值是 List<PayrollModel>
						));
				for (int key : prePayrollMap.keySet()) {
					float totalWeight = 0;
					List<PayrollModel> value = prePayrollMap.get(key);
					for (PayrollModel pay : value) {
						totalWeight = totalWeight + pay.getHours() * pay.getCoefficient();
					}
					preHoursMap.put(key, totalWeight);
				}

			}
			// 使用 LinkedHashMap 以 empId 為 key，將 PayrollModel 收集到 List 中
			Map<Integer, List<PayrollModel>> payrollMap = payrolls.stream()
					.collect(Collectors.groupingBy(PayrollModel::getEmpId, // 鍵是 empId
							LinkedHashMap::new, // 使用 LinkedHashMap 保持順序
							Collectors.toList() // 值是 List<PayrollModel>
					));
			// 現在可以按插入順序遍歷 payrollMap
			int rowNum = 1;
			for (int key : payrollMap.keySet()) {
				float totalWeight = 0;
				float totaltaxFreeWeight = 0;
				Row row1 = sheet1.createRow(rowNum++);
				row1.createCell(0).setCellValue(key);
				List<PayrollModel> value = payrollMap.get(key);
				row1.createCell(1).setCellValue(value.get(0).getNameEn() + " " + value.get(0).getLastNameEn());
				for (PayrollModel pay : value) {
					row1.createCell(colMap.get(pay.getTitle())).setCellValue(pay.getHours() * pay.getCoefficient());
					totalWeight = totalWeight + pay.getHours() * pay.getCoefficient();
					totaltaxFreeWeight = totaltaxFreeWeight + pay.getTaxFreeHours() * pay.getCoefficient();
				}
				row1.createCell(index).setCellValue(totalWeight);
				row1.createCell(index + 1).setCellValue(totaltaxFreeWeight);
				if (!preHoursMap.isEmpty()) {
					row1.createCell(index + 2).setCellValue(preHoursMap.get(key));
				}
			}
			// 自動調整所有列的寬度
			int numberOfColumns = sheet1.getRow(0).getPhysicalNumberOfCells(); // 獲取列數
			for (int i = 0; i < numberOfColumns; i++) {
				sheet1.autoSizeColumn(i); // 自動調整每一列的寬度
			}

			// 創建圖表
			XSSFSheet sheet2 = workbook.createSheet("Salary Comparison");
			XSSFDrawing drawing = sheet2.createDrawingPatriarch();

			XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, rowNum + 2, 10, rowNum + 20);
			XSSFChart chart = drawing.createChart(anchor);
			chart.setTitleText("Employee Hours Worked");
			chart.setTitleOverlay(false);

			XDDFChartLegend legend = chart.getOrAddLegend();
			legend.setPosition(LegendPosition.TOP_RIGHT);

			// 設置圖表數據範圍
			XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet1,
					new CellRangeAddress(1, rowNum - 1, 0, 1));
			XDDFNumericalDataSource<Double> totalHours = XDDFDataSourcesFactory.fromNumericCellRange(sheet1,
					new CellRangeAddress(1, rowNum - 1, index, index));
			XDDFNumericalDataSource<Double> totalpreHours = XDDFDataSourcesFactory.fromNumericCellRange(sheet1,
					new CellRangeAddress(1, rowNum - 1, index + 2, index + 2));

			// 創建堆積柱形圖
			XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
			bottomAxis.setTitle("Employee Name");
			XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
			leftAxis.setTitle("Hours/Weighted Value");
			leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN);
			XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
			XDDFBarChartData barData = (XDDFBarChartData) data;

			barData.setBarDirection(BarDirection.COL);

			// 添加數據
			XDDFBarChartData.Series series1 = (XDDFBarChartData.Series) barData.addSeries(categories, totalHours);
			series1.setTitle("Total Hours", null);
			XDDFBarChartData.Series series2 = (XDDFBarChartData.Series) barData.addSeries(categories, totalpreHours);
			series2.setTitle("Total PreMonth Hours", null);
			// data.setVaryColors(true);
			// 設置堆積樣式
			barData.setBarGrouping(BarGrouping.CLUSTERED);
			chart.plot(data);
		}

		// 2. 将 Excel 写入 ByteArrayOutputStream
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();
		return out;
	}

	public ByteArrayOutputStream exportUsersToExcel(int year, int month, int empid) throws IOException {
		AttendanceModel attendanceModel = new AttendanceModel();
		attendanceModel.setEmpId(empid);
		attendanceModel.setYear(year);
		attendanceModel.setMonth(month);
		List<AttendanceModel> attendances = attendanceRepository.findAttendanceByUserMon(attendanceModel);
		PayrollModel payroll = new PayrollModel();
		payroll.setYear(year);
		payroll.setMonth(month);
		payroll.setEmpId(empid);
		List<PayrollModel> payrolls = payrollRepository.findPayrollDetailByUserMon(payroll);

		// 创建 Excel 工作簿
		Workbook workbook = new XSSFWorkbook();

		CellStyle dateTimeCellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		dateTimeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

		Sheet sheet1 = workbook.createSheet("Payroll");
		// 创建表头
		Row header1 = sheet1.createRow(0);
		header1.createCell(0).setCellValue("EMP_ID");
		header1.createCell(1).setCellValue("YEAR");
		header1.createCell(2).setCellValue("MONTH");
		header1.createCell(3).setCellValue("DAY");
		header1.createCell(4).setCellValue("PAY_CODE");
		header1.createCell(5).setCellValue("TITLE");
		header1.createCell(6).setCellValue("HOURS");
		header1.createCell(7).setCellValue("TAX_FREE_HOURS");
		header1.createCell(8).setCellValue("COEFFIICIENT");
		header1.createCell(9).setCellValue("FROM_HOUR");
		header1.createCell(10).setCellValue("TO_HOUR");
		// 填充数据
		int rowNum = 1;
		if (!CollectionUtils.isEmpty(payrolls)) {
			for (PayrollModel pay : payrolls) {
				Row row1 = sheet1.createRow(rowNum++);
				row1.createCell(0).setCellValue(pay.getEmpId());
				row1.createCell(1).setCellValue(pay.getYear());
				row1.createCell(2).setCellValue(pay.getMonth());
				row1.createCell(3).setCellValue(pay.getDay());
				row1.createCell(4).setCellValue(pay.getPayCode());
				row1.createCell(5).setCellValue(pay.getTitle());
				row1.createCell(6).setCellValue(pay.getHours());
				row1.createCell(7).setCellValue(pay.getTaxFreeHours());
				row1.createCell(8).setCellValue(pay.getCoefficient());
				row1.createCell(9).setCellValue(pay.getFromHour());
				row1.createCell(10).setCellValue(pay.getToHour());
			}
		}

		Sheet sheet2 = workbook.createSheet("Attendance");
		Row header2 = sheet2.createRow(0);
		header2.createCell(0).setCellValue("EMP_ID");
		header2.createCell(1).setCellValue("YEAR");
		header2.createCell(2).setCellValue("MONTH");
		header2.createCell(3).setCellValue("DAY");
		header2.createCell(4).setCellValue("SEQ");
		header2.createCell(5).setCellValue("Arrival_datetime");
		header2.createCell(6).setCellValue("Leave_datetime");
		header2.createCell(7).setCellValue("Work_hours");
		header2.createCell(8).setCellValue("Note");
		header2.createCell(9).setCellValue("Approval");
		header2.createCell(10).setCellValue("Day_code");
		header2.createCell(11).setCellValue("Overtime");
		header2.createCell(12).setCellValue("Start_datetime");
		header2.createCell(13).setCellValue("End_datetime");
		header2.createCell(14).setCellValue("Week");
		header2.createCell(15).setCellValue("Reason");
		header2.createCell(16).setCellValue("Shift");
		header2.createCell(17).setCellValue("Status");
		header2.createCell(18).setCellValue("Comp_time");
		header2.createCell(19).setCellValue("Comp_reason");
		header2.createCell(20).setCellValue("Period");
		header2.createCell(21).setCellValue("Paid_leave");
		header2.createCell(22).setCellValue("Remain_tax_free");
		header2.createCell(23).setCellValue("Over_start_datetime");
		header2.createCell(24).setCellValue("Over_end_datetime");

		rowNum = 1;
		if (!CollectionUtils.isEmpty(attendances)) {
			for (AttendanceModel att : attendances) {
				Row row2 = sheet2.createRow(rowNum++);
				row2.createCell(0).setCellValue(att.getEmpId());
				row2.createCell(1).setCellValue(att.getYear());
				row2.createCell(2).setCellValue(att.getMonth());
				row2.createCell(3).setCellValue(att.getDay());
				row2.createCell(4).setCellValue(att.getSeq());
				Cell cellArrivalDatetime = row2.createCell(5);
				if (att.getArrivalDatetime() != null) {
					cellArrivalDatetime.setCellValue(Timestamp
							.valueOf(att.getArrivalDatetime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellArrivalDatetime.setCellValue(att.getArrivalDatetime());
				}
				cellArrivalDatetime.setCellStyle(dateTimeCellStyle);
				Cell cellLeaveDatetime = row2.createCell(6);
				if (att.getLeaveDatetime() != null) {
					cellLeaveDatetime.setCellValue(Timestamp
							.valueOf(att.getLeaveDatetime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellLeaveDatetime.setCellValue(att.getLeaveDatetime());
				}
				cellLeaveDatetime.setCellStyle(dateTimeCellStyle);
				row2.createCell(7).setCellValue(att.getWorkHours());
				row2.createCell(8).setCellValue(att.getNote());
				row2.createCell(9).setCellValue(att.getApproval());
				row2.createCell(10).setCellValue(att.getDayCode());
				row2.createCell(11).setCellValue(att.getOvertime());
				Cell cellStartDatetime = row2.createCell(12);
				if (att.getStartDatetime() != null) {
					cellStartDatetime.setCellValue(Timestamp
							.valueOf(att.getStartDatetime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellStartDatetime.setCellValue(att.getStartDatetime());
				}
				cellStartDatetime.setCellStyle(dateTimeCellStyle);
				Cell cellEndDatetime = row2.createCell(13);
				if (att.getEndDatetime() != null) {
					cellEndDatetime.setCellValue(Timestamp
							.valueOf(att.getEndDatetime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellEndDatetime.setCellValue(att.getEndDatetime());
				}
				cellEndDatetime.setCellStyle(dateTimeCellStyle);
				row2.createCell(14).setCellValue(att.getWeek());
				row2.createCell(15).setCellValue(att.getReason());
				row2.createCell(16).setCellValue(att.getShift());
				row2.createCell(17).setCellValue(att.getStatus());
				row2.createCell(18).setCellValue(att.getCompTime());
				row2.createCell(19).setCellValue(att.getCompReason());
				row2.createCell(20).setCellValue(att.getPeriod());
				row2.createCell(21).setCellValue(att.getPaidLeave());
				row2.createCell(22).setCellValue(att.getRemainTaxFree());
				Cell cellOverStartDatetime = row2.createCell(23);
				if (att.getOverStartDatetime() != null) {
					cellOverStartDatetime.setCellValue(Timestamp.valueOf(
							att.getOverStartDatetime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellOverStartDatetime.setCellValue(att.getOverStartDatetime());
				}
				cellOverStartDatetime.setCellStyle(dateTimeCellStyle);
				Cell cellOverEndDatetime = row2.createCell(24);
				if (att.getOverStartDatetime() != null) {
					cellOverEndDatetime.setCellValue(Timestamp
							.valueOf(att.getOverEndDatetime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()));
				} else {
					cellOverEndDatetime.setCellValue(att.getOverStartDatetime());
				}
				cellOverEndDatetime.setCellStyle(dateTimeCellStyle);
			}
		}

		// 2. 将 Excel 写入 ByteArrayOutputStream
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();
		return out;
	}
}
