package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.MoneyRecord;
import com.example.demo.model.MonthlySummary;
import com.example.demo.model.SummaryByCategory;

public interface MoneyRecordRepository extends JpaRepository<MoneyRecord, Long>{
	
	public List<MoneyRecord> findByUsername(String username);
	
	public MoneyRecord findByRecordId(Long recordId);
	
	public void deleteByRecordId(Long recordId);
	
	public List<MoneyRecord> findByUsernameOrderByRecordDate(String username);
	
	public List<MoneyRecord> findByRecordDateBetweenOrderByRecordDateAsc(LocalDate start, LocalDate end);
	
	@Query(value = "SELECT DATE_FORMAT(record_date, '%Y%m'), sum(income_and_expense) FROM money_records "
			+ "where user_id = :username and category_id not like '20%' "
			+ "group by DATE_FORMAT(record_date, '%Y%m') order by record_date asc" , nativeQuery = true)
	public List<Object[]> getMonthSummaries(@Param("username") String username);
	
	default List<MonthlySummary> findMonthSummaries(String username) {
        return getMonthSummaries(username).stream().map(MonthlySummary::new).collect(Collectors.toList());
    }
	
	@Query(value = "select left(category_id, 2) as category, sum(income_and_expense) as sum"
			+ " from money_records where user_id = :username and record_date like concat(:month,'%') and category_id not like '200%'"
			+ "group by left(category_id, 2)", nativeQuery = true)
	public List<Object[]> getCategorySummaries(@Param("username") String username, @Param("month") String month);
	
	default List<SummaryByCategory> findCategorySummaries(String username, String month) {
        return getCategorySummaries(username, month).stream().map(SummaryByCategory::new).collect(Collectors.toList());
    }

}
