package com.example.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.standard.expression.MinusExpression;

import com.example.demo.repository.DateRepository;
import com.example.demo.repository.MoneyRecordRepository;

@Service
public class DateService {
	
	static final private String DATE_FORMAT = "yyyy-MM-dd";

	@Autowired
	private final DateRepository dateRepository;
	private final MoneyRecordRepository moneyRecordRepository;

	public DateService(DateRepository dateRepository, MoneyRecordRepository moneyRecordRepository) {
		this.dateRepository = dateRepository;
		this.moneyRecordRepository = moneyRecordRepository;
	}

	public String[] getAllMonths(String username) {
		String oldestDateForSql = "";
		if (moneyRecordRepository.getOldestDate(username) != null) {
			//一番古い記録を取得する
			String oldestDateStr = moneyRecordRepository.getOldestDate(username).toString();
			LocalDate ld = LocalDate.parse(oldestDateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
			//ひと月分減算する
			ld = ld.minusMonths(1);
			//String化
			oldestDateForSql = ld.toString();
		} else {
			oldestDateForSql = LocalDate.now().toString();
		}
		String months[] = dateRepository.findAllMonths(oldestDateForSql);

		return months;
	}

}
