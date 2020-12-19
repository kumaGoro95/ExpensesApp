package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;

public interface MoneyRecordRepository extends JpaRepository<MoneyRecord, Long> {
	
	public List<MoneyRecord> findByUserId(Long userId);
	
}
