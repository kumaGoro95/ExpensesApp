package com.example.demo.service;

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
		String oldestDate = moneyRecordRepository.getOldestDate(username).toString();
		String months[] = dateRepository.findAllMonths(oldestDate);
		
		return months;
	}

}
