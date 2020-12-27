package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;

public interface MoneyRecordRepository extends JpaRepository<MoneyRecord, Long> {
	
	public List<MoneyRecord> findByUsername(String username);
	
	public MoneyRecord findByRecordId(Long recordId);
	
	public void deleteByRecordId(Long recordId);
	
	public List<MoneyRecord> findByUsernameOrderByRecordDate(String username);
	
}
