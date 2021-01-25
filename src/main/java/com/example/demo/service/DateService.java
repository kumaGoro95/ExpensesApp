package com.example.demo.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.repository.DateRepository;
import com.example.demo.repository.MoneyRecordRepository;

@Service
public class DateService {
	
	@Autowired
	private final DateRepository dateRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	
	public DateService(DateRepository dateRepository, MoneyRecordRepository moneyRecordRepository) {
		this.dateRepository = dateRepository;
		this.moneyRecordRepository = moneyRecordRepository;
	}
	
	public String[] getAllMonths(String username) {
		String oldestDate = "";
		if(moneyRecordRepository.getOldestDate(username) != null) {
		oldestDate = moneyRecordRepository.getOldestDate(username).toString();
		}else {
		oldestDate = LocalDate.now().toString();
		}
		String months[] = dateRepository.findAllMonths(oldestDate);
		
		return months;
	}

}
