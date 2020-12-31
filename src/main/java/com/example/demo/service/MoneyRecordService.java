package com.example.demo.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.model.MoneyRecord;

@Service
public class MoneyRecordService {
	
	private final MoneyRecordRepository moneyRecordRepository;
	
	public MoneyRecordService(MoneyRecordRepository moneyRecordRepository) {
		this.moneyRecordRepository = moneyRecordRepository;
	}
	
	public List<MoneyRecord> getMonthExpense(){
		LocalDate start = LocalDate.of(2020, 12, 1);
		LocalDate now = LocalDate.now();
		LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()); // 末日
		List<MoneyRecord> list = moneyRecordRepository.findByRecordDateBetweenOrderByRecordDateAsc(start, lastDayOfMonth);
		for (int i = 0; i < list.size(); i++) {
			MoneyRecord moneyRecord = list.get(i);
			String categoryIdStr =  String.valueOf(moneyRecord.getCategoryId());
	        if (categoryIdStr.indexOf("20") != -1) {
	            list.remove(i);
	            --i;
	        }
	    }
		return list;
	}
	

}
