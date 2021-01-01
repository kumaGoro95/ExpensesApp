package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.MoneyRecord;
import com.example.demo.model.Summary;

public interface MoneyRecordRepository extends JpaRepository<MoneyRecord, Long>{
	
	public List<MoneyRecord> findByUsername(String username);
	
	public MoneyRecord findByRecordId(Long recordId);
	
	public void deleteByRecordId(Long recordId);
	
	public List<MoneyRecord> findByUsernameOrderByRecordDate(String username);
	
	public List<MoneyRecord> findByRecordDateBetweenOrderByRecordDateAsc(LocalDate start, LocalDate end);
	
	//@Query("SELECT NEW com.example.demo.model.Summary(SUM(income_and_expense), DATE_FORMAT(record_date, '%Y%m'))"
		//	+ "FROM MoneyRecord "
		//	+ "where user_id = :username and category_id not like '20%'"
		//	+ "group by DATE_FORMAT(record_date, '%Y%m') order by record_date asc")
	//public List<Summary> findSummaries(@Param("username") String username);
}
