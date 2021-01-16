package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.DailySummary;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.MoneyRecordList;
import com.example.demo.model.MonthlySummary;
import com.example.demo.model.SummariesByMonthAndCategory;
import com.example.demo.model.SummaryByCategory;

public interface MoneyRecordRepository extends JpaRepository<MoneyRecord, Long> {

	public List<MoneyRecord> findByUsername(String username);

	public MoneyRecord findByRecordId(int recordId);

	public void deleteByRecordId(int recordId);

	public List<MoneyRecord> findByUsernameOrderByRecordDate(String username);

	public List<MoneyRecord> findByRecordDateBetweenOrderByRecordDateAsc(LocalDate start, LocalDate end);
	
	@Query(value = "select record_id, record_date, income_and_expense, subcategory_name, record_note  "
			+"from money_records M left join categories C on C.category_id = M.category_id "
			+"where M.user_id = :username", nativeQuery = true)
	public List<Object[]> getMoneyRecordList(@Param("username")String username);
	
	default List<MoneyRecordList> findMoneyRecordList(String username) {
		return getMoneyRecordList(username).stream().map(MoneyRecordList::new).collect(Collectors.toList());
	}

	// 月ごとの支出の合計を算出
	@Query(value = "SELECT DATE_FORMAT(record_date, '%Y%m'), sum(income_and_expense) FROM money_records "
			+ "where user_id = :username and category_id not like '99%' "
			+ "group by DATE_FORMAT(record_date, '%Y%m') order by record_date asc", nativeQuery = true)
	public List<Object[]> getMonthSummaries(@Param("username") String username);

	default List<MonthlySummary> findMonthSummaries(String username) {
		return getMonthSummaries(username).stream().map(MonthlySummary::new).collect(Collectors.toList());
	}

	// 特定の月の、カテゴリー毎の合計を算出
	@Query(value = "select category_code, ifnull(sum(income_and_expense), 0) "
			+ "from categories C left join money_records M "
			+ "on M.user_id = :username and M.record_date like concat(:month, '%') and C.category_id = M.category_id "
			+ "group by category_code order by category_code asc", nativeQuery = true)
	public List<Object[]> getCategorySummaries(@Param("username") String username, @Param("month") String month);

	default List<SummaryByCategory> findCategorySummaries(String username, String month) {
		return getCategorySummaries(username, month).stream().map(SummaryByCategory::new).collect(Collectors.toList());
	}

	// 特定の月の、サブカテゴリー毎の合計を算出
	@Query(value = "select C.category_id, ifnull(sum(income_and_expense), 0) "
			+ "from categories C left join money_records M "
			+ "on M.user_id = :username and M.record_date like concat(:month, '%') and C.category_id = M.category_id "
			+ "where category_code = :categoryCode group by C.category_id order by C.category_id asc", nativeQuery = true)
	public List<Object[]> getSubcategorySummaries(@Param("username") String username, @Param("month") String month,
			@Param("categoryCode") int categoryCode);

	default List<SummaryByCategory> findSubcategorySummaries(String username, String month, int categoryCode) {
		return getSubcategorySummaries(username, month, categoryCode).stream().map(SummaryByCategory::new)
				.collect(Collectors.toList());
	}

	// 月ごと、カテゴリー毎の合計一覧を算出
	@Query(value = "select DATE_FORMAT(record_date, \"%Y%m\") as YM, left(category_id, 2) as category, sum(income_and_expense) as sum "
			+ "from money_records where user_id = :username and category_id not like '99%' "
			+ "group by left(category_id, 2),DATE_FORMAT(record_date, '%Y%m') "
			+ "order by record_date and left(category_id, 2) asc", nativeQuery = true)
	public List<Object[]> getMonthAndCategorySummaries(@Param("username") String username);

	default List<SummariesByMonthAndCategory> findMonthAndCategorySummaries(String username) {
		return getMonthAndCategorySummaries(username).stream().map(SummariesByMonthAndCategory::new)
				.collect(Collectors.toList());
	}
	
	//日ごとの支出合計を算出
	@Query(value = "select CAST(concat(sum(income_and_expense), '円') as char), DATE_FORMAT(record_date, '%Y-%m-%d') "
			+ "from money_records where user_id = :username and category_id not like '99%' "
			+ "group by record_date order by record_date asc", nativeQuery = true)
	public List<Object[]> getDailySummaries(@Param("username") String username);

	default List<DailySummary> findDailySummaries(String username) {
		return getDailySummaries(username).stream().map(DailySummary::new)
				.collect(Collectors.toList());
	}

}
