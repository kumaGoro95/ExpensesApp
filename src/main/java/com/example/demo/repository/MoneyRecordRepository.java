package com.example.demo.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.MoneyRecord;
import com.example.demo.model.beans.MoneyRecordList;
import com.example.demo.model.beans.MonthlySummary;
import com.example.demo.model.beans.SummariesByMonthAndCategory;
import com.example.demo.model.beans.SummaryByCategory;
import com.example.demo.model.beans.DailySumGraph;
import com.example.demo.model.beans.DailySummary;

public interface MoneyRecordRepository extends JpaRepository<MoneyRecord, Long> {

	public List<MoneyRecord> findByUsername(String username);

	public MoneyRecord findByRecordId(int recordId);

	public void deleteByRecordId(int recordId);

	public void deleteByUsername(String username);

	public List<MoneyRecord> findByUsernameOrderByRecordDate(String username);

	public List<MoneyRecord> findByRecordDateBetweenOrderByRecordDateAsc(LocalDate start, LocalDate end);
	
	public boolean existsByUsername(String username);

	// 支出履歴（最新から10件取得・ホーム用）
	@Query(value = "select record_id, record_date, concat(case when M.category_id not like '99%' then '-' else '' end, income_and_expense), "
			+ "C.category_code, subcategory_name, record_note from money_records M left join categories C on C.category_id = M.category_id "
			+ "where M.user_id = :username and M.category_id order by record_date desc limit 10", nativeQuery = true)
	public List<Object[]> getMoneyRecordListLimit10(@Param("username") String username);

	default List<MoneyRecordList> findMoneyRecordListLimit10(String username) {
		return getMoneyRecordListLimit10(username).stream().map(MoneyRecordList::new).collect(Collectors.toList());
	}

	// 支出履歴一覧（日付降順）
	@Query(value = "select record_id, record_date, concat(case when M.category_id not like '99%' then '-' else '' end, income_and_expense), "
			+ "C.category_code, subcategory_name, record_note from money_records M left join categories C on C.category_id = M.category_id "
			+ "where M.user_id = :username and M.category_id order by record_date desc", nativeQuery = true)
	public List<Object[]> getMoneyRecordList(@Param("username") String username);

	default List<MoneyRecordList> findMoneyRecordList(String username) {
		return getMoneyRecordList(username).stream().map(MoneyRecordList::new).collect(Collectors.toList());
	}

	// 支出履歴絞り込み検索（日付降順）
	@Query(value = "select record_id, record_date, concat(case when M.category_id not like '99%' then '-' else '' end, income_and_expense), "
			+ "C.category_code, subcategory_name, record_note from money_records M left join categories C on C.category_id = M.category_id "
			+ "where M.user_id = :username and C.category_code like :categoryCode and M.record_date between :startDate and :endDate order by record_date desc", nativeQuery = true)
	public List<Object[]> getMoneyRecordRefinedList(@Param("username") String username, @Param("categoryCode") String categoryCode, @Param("startDate") String startDate, @Param("endDate") String endDate);

	default List<MoneyRecordList> findMoneyRecordRefinedList(String username, String categoryCode, String startDate, String endDate) {
		return getMoneyRecordRefinedList(username, categoryCode, startDate, endDate).stream().map(MoneyRecordList::new).collect(Collectors.toList());
	}

	// 並べ替え（日付昇順）
	@Query(value = "select record_id, record_date, concat(case when M.category_id not like '99%' then '-' else '' end, income_and_expense), "
			+ "C.category_code, subcategory_name, record_note from money_records M left join categories C on C.category_id = M.category_id "
			+ "where M.user_id = :username and C.category_code like :categoryCode and M.record_date between :startDate and :endDate order by record_date asc", nativeQuery = true)
	public List<Object[]> getMoneyRecordListOrderByDateAsc(@Param("username") String username, @Param("categoryCode") String categoryCode, @Param("startDate") String startDate, @Param("endDate") String endDate);

	default List<MoneyRecordList> findMoneyRecordListOrderByDateAsc(String username, String categoryCode, String startDate, String endDate) {
		return getMoneyRecordListOrderByDateAsc(username, categoryCode, startDate, endDate).stream().map(MoneyRecordList::new)
				.collect(Collectors.toList());
	}

	// 支出履歴一覧（金額降順）
	@Query(value = "select record_id, record_date, concat(case when M.category_id not like '99%' then '-' else '' end, income_and_expense), "
			+ "C.category_code, subcategory_name, record_note from money_records M left join categories C on C.category_id = M.category_id "
			+ "where M.user_id = :username and C.category_code like :categoryCode and M.record_date between :startDate and :endDate order by income_and_expense desc", nativeQuery = true)
	public List<Object[]> getMoneyRecordListOrderByMoneyDesc(@Param("username") String username, @Param("categoryCode") String categoryCode, @Param("startDate") String startDate, @Param("endDate") String endDate);

	default List<MoneyRecordList> findMoneyRecordListOrderByMoneyDesc(String username, String categoryCode, String startDate, String endDate) {
		return getMoneyRecordListOrderByMoneyDesc(username, categoryCode, startDate, endDate).stream().map(MoneyRecordList::new)
				.collect(Collectors.toList());
	}

	// 支出履歴一覧（金額昇順）
	@Query(value = "select record_id, record_date, concat(case when M.category_id not like '99%' then '-' else '' end, income_and_expense), "
			+ "C.category_code, subcategory_name, record_note from money_records M left join categories C on C.category_id = M.category_id "
			+ "where M.user_id = :username and C.category_code like :categoryCode and M.record_date between :startDate and :endDate order by income_and_expense asc", nativeQuery = true)
	public List<Object[]> getMoneyRecordListOrderByMoneyAsc(@Param("username") String username, @Param("categoryCode") String categoryCode, @Param("startDate") String startDate, @Param("endDate") String endDate);

	default List<MoneyRecordList> findMoneyRecordListOrderByMoneyAsc(String username, String categoryCode, String startDate, String endDate) {
		return getMoneyRecordListOrderByMoneyAsc(username, categoryCode, startDate, endDate).stream().map(MoneyRecordList::new)
				.collect(Collectors.toList());
	}

	// 履歴一覧用（日にちで取得）
	@Query(value = "select record_id, record_date, concat(case when M.category_id not like '99%' then '-' else '' end, income_and_expense), "
			+ "C.category_code, subcategory_name, record_note from money_records M left join categories C on C.category_id = M.category_id "
			+ "where M.user_id = :username and M.record_date = :date order by record_date desc", nativeQuery = true)
	public List<Object[]> getOneDayRecord(@Param("username") String username, @Param("date") String date);

	default List<MoneyRecordList> findOneDayRecord(String username, String date) {
		return getOneDayRecord(username, date).stream().map(MoneyRecordList::new).collect(Collectors.toList());
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

	// 日ごとの支出合計を算出（カレンダー用）
	@Query(value = "select CAST(concat(format(sum(income_and_expense), 0), '円') as char), DATE_FORMAT(record_date, '%Y-%m-%d') "
			+ "from money_records where user_id = :username and category_id not like '99%' "
			+ "group by record_date order by record_date asc", nativeQuery = true)
	public List<Object[]> getDailyExpenseSummaries(@Param("username") String username);

	default List<DailySummary> findDailyExpenseSummaries(String username) {
		return getDailyExpenseSummaries(username).stream().map(DailySummary::new).collect(Collectors.toList());
	}

	// 日ごとの収入合計を算出（カレンダー用）
	@Query(value = "select CAST(concat(format(sum(income_and_expense), 0 ), '円') as char), DATE_FORMAT(record_date, '%Y-%m-%d') "
			+ "from money_records where user_id = :username and category_id like '99%' "
			+ "group by record_date order by record_date asc", nativeQuery = true)
	public List<Object[]> getDailyIncomeSummaries(@Param("username") String username);

	default List<DailySummary> findDailyIncomeSummaries(String username) {
		return getDailyIncomeSummaries(username).stream().map(DailySummary::new).collect(Collectors.toList());
	}

	// 日別グラフ用
	@Query(value = "select adddate(:firstDay ,number) as date, ifnull(sum(M.income_and_expense), 0) as money from numbers "
			+ "N left join money_records M on adddate(:firstDay ,number) = M.record_date and M.user_id = :username "
			+ "and M.category_id not like '99%' where adddate(:firstDay, number) between :firstDay and :lastDay "
			+ "group by adddate(:firstDay, number)", nativeQuery = true)
	public List<Object[]> getDailyGraph(@Param("username") String username, @Param("firstDay") LocalDate firstDay,
			@Param("lastDay") LocalDate lastDay);

	default List<DailySumGraph> findDailyGraph(String username, LocalDate firstDay, LocalDate lastDay) {
		return getDailyGraph(username, firstDay, lastDay).stream().map(DailySumGraph::new).collect(Collectors.toList());
	}

	// 最古の記録を取得する
	@Query(value = "select record_date from money_records where user_id = :username order by record_date asc limit 1", nativeQuery = true)
	public Object getOldestDate(@Param("username") String username);

}
