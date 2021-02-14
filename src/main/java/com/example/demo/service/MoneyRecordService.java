package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.util.CategoryCodeToName;
import com.example.demo.model.Category;
import com.example.demo.model.beans.DailySumGraph;
import com.example.demo.model.beans.SummaryByCategory;

@Service
public class MoneyRecordService {

	@Autowired
	private final MoneyRecordRepository moneyRecordRepository;
	private final CategoryRepository categoryRepository;

	public MoneyRecordService(MoneyRecordRepository moneyRecordRepository, CategoryRepository categoryRepository) {
		this.moneyRecordRepository = moneyRecordRepository;
		this.categoryRepository = categoryRepository;
	}

	// 円グラフ（支出）用ラベル
	public String[] getExpenseLabel() {
		List<String> expenseCategory = new ArrayList<String>();
		for (int i = 1; i < CategoryCodeToName.Categories.size(); i++) {
			expenseCategory.add(CategoryCodeToName.Categories.get(i));
		}
		String expenseLabel[] = expenseCategory.toArray(new String[expenseCategory.size()]);

		return expenseLabel;
	}

	// 円グラフ（収入）用ラベル
	public String[] getIncomeLabel() {
		List<Category> incomeCategories = categoryRepository.findBycategoryCode(99);
		List<String> incomeCategoriesStr = new ArrayList<String>();
		for (int i = 0; i < incomeCategories.size(); i++) {
			incomeCategoriesStr.add(incomeCategories.get(i).getSubcategoryName());
		}
		String incomeLabel[] = incomeCategoriesStr.toArray(new String[incomeCategoriesStr.size()]);

		return incomeLabel;
	}

	public BigDecimal[] getExpenseData(String username, String currentMonth) {
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(username, currentMonth);
		List<BigDecimal> expenseAmmount = new ArrayList<BigDecimal>();
		for (int i = 0; i < expenseByCategory.size() - 1; i++) {
			expenseAmmount.add(expenseByCategory.get(i).getSum());
		}
		BigDecimal expenseData[] = expenseAmmount.toArray(new BigDecimal[expenseAmmount.size()]);

		return expenseData;
	}

	public BigDecimal[] getIncomeData(String username, String currentMonth) {
		List<SummaryByCategory> incomeByCategory = moneyRecordRepository.findSubcategorySummaries(username,
				currentMonth, 99);
		List<BigDecimal> incomeAmmount = new ArrayList<BigDecimal>();
		for (int i = 0; i < incomeByCategory.size(); i++) {
			incomeAmmount.add(incomeByCategory.get(i).getSum());
		}
		BigDecimal incomeData[] = incomeAmmount.toArray(new BigDecimal[incomeAmmount.size()]);

		return incomeData;
	}
	
    //日ごとグラフの支出金額を算出
	public BigDecimal[] getDailyAmmount(String username, String currentMonth) {
		//その月の初日を取得
		String firstDayStr = currentMonth + "-01";
		LocalDate firstDay = LocalDate.parse(firstDayStr, DateTimeFormatter.ISO_DATE);
		LocalDate lastDay = YearMonth.now().atEndOfMonth();
		List<DailySumGraph> dailySum = moneyRecordRepository.findDailyGraph(username, firstDay, lastDay);
		List<BigDecimal> dailyAmmountList = new ArrayList<BigDecimal>();
		for (int i = 0; i < dailySum.size(); i++) {
			dailyAmmountList.add(dailySum.get(i).getSum());
		}
		BigDecimal dailyAmmount[] = dailyAmmountList.toArray(new BigDecimal[dailyAmmountList.size()]);

		return dailyAmmount;
	}
	
	//日ごとグラフの収入金額を算出
	public BigDecimal[] getDailyAmmountIncome(String username, String currentMonth) {
		//その月の初日を取得
		String firstDayStr = currentMonth + "-01";
		LocalDate firstDay = LocalDate.parse(firstDayStr, DateTimeFormatter.ISO_DATE);
		//その月の末日を取得
		LocalDate lastDay = YearMonth.now().atEndOfMonth();
		
		List<DailySumGraph> dailySum = moneyRecordRepository.findDailyGraphIncome(username, firstDay, lastDay);
		List<BigDecimal> dailyAmmountList = new ArrayList<BigDecimal>();
		for (int i = 0; i < dailySum.size(); i++) {
			dailyAmmountList.add(dailySum.get(i).getSum());
		}
		BigDecimal dailyAmmountIncome[] = dailyAmmountList.toArray(new BigDecimal[dailyAmmountList.size()]);

		return dailyAmmountIncome;
	}

	//日ごとグラフの日付一覧を取得
	public Integer[] getDays(String username, String currentMonth) {
		String firstDayStr = currentMonth + "-01";
		LocalDate firstDay = LocalDate.parse(firstDayStr, DateTimeFormatter.ISO_DATE);
		LocalDate lastDay = YearMonth.from(firstDay).atEndOfMonth();
		List<DailySumGraph> dailySum = moneyRecordRepository.findDailyGraph(username, firstDay, lastDay);
		List<Integer> daysList = new ArrayList<Integer>();
		for (int i = 1; i < dailySum.size() + 1; i++) {
			daysList.add(i);
		}
		Integer days[] = daysList.toArray(new Integer[dailySum.size()]);

		return days;
	}
	
	//一番古い記録を取得する
	public String getOldestDate(String username) {
		String oldestDate = null;
		//出入金記録があるか確認
		if(moneyRecordRepository.existsByUsername(username)) {
			oldestDate = moneyRecordRepository.getOldestDate(username).toString();
		}else {
			LocalDate ld = LocalDate.now();
			oldestDate = ld.toString();
		}
		
		return oldestDate;
	}

}
