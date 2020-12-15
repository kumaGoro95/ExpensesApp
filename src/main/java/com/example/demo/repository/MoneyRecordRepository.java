package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.MoneyRecord;

public interface MoneyRecordRepository extends JpaRepository<MoneyRecord, Long> {
	
	
}
