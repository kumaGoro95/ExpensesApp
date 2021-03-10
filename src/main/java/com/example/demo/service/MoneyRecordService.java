package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.util.CategoryCodeToName;
import com.example.demo.model.Category;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.beans.DailySumGraph;
import com.example.demo.model.beans.MoneyRecordList;
import com.example.demo.model.beans.RefineCondition;
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

	/* 円グラフ用メソッド */

	// 円グラフ（支出）用ラベルを取得する
	public String[] getExpenseLabel() {
		List<String> expenseCategory = new ArrayList<String>();
		for (int i = 1; i < CategoryCodeToName.Categories.size(); i++) {
			expenseCategory.add(CategoryCodeToName.Categories.get(i));
		}
		String expenseLabel[] = expenseCategory.toArray(new String[expenseCategory.size()]);

		return expenseLabel;
	}

	// 円グラフ（収入）用ラベルを取得する
	public String[] getIncomeLabel() {
		List<Category> incomeCategories = categoryRepository.findBycategoryCode(99);
		List<String> incomeCategoriesStr = new ArrayList<String>();
		for (int i = 0; i < incomeCategories.size(); i++) {
			incomeCategoriesStr.add(incomeCategories.get(i).getSubcategoryName());
		}
		String incomeLabel[] = incomeCategoriesStr.toArray(new String[incomeCategoriesStr.size()]);

		return incomeLabel;
	}

	// 円グラフ（支出）用金額データを取得
	public BigDecimal[] getExpenseData(String username, String currentMonth) {
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(username, currentMonth);
		List<BigDecimal> expenseAmmount = new ArrayList<BigDecimal>();
		for (int i = 0; i < expenseByCategory.size() - 1; i++) {
			expenseAmmount.add(expenseByCategory.get(i).getSum());
		}
		BigDecimal expenseData[] = expenseAmmount.toArray(new BigDecimal[expenseAmmount.size()]);

		return expenseData;
	}

	// 円グラフ（収入）用金額データを取得
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

	// 円グラフに表示する出入金データが存在するかどうかチェック（支出用）
	public boolean existsGraphData(BigDecimal[] data) {

		// 確認用の配列を用意
		List<BigDecimal> checknullList = new ArrayList<BigDecimal>();
		for (int i = 0; i < 15; i++) {
			checknullList.add(BigDecimal.valueOf(0));
		}
		BigDecimal checknull[] = checknullList.toArray(new BigDecimal[checknullList.size()]);

		// 確認用配列と出入金データが等値ならtrue,そうでないならfalse
		if (Arrays.equals(data, checknull)) {
			return true;
		} else {
			return false;
		}
	}

	// 円グラフに表示する出入金データが存在するかどうかチェック（収入用）
	public boolean existsGraphDataForIncome(BigDecimal[] data) {

		// 確認用の配列を用意
		List<BigDecimal> checknullList = new ArrayList<BigDecimal>();
		for (int i = 0; i < 4; i++) {
			checknullList.add(BigDecimal.valueOf(0));
		}
		BigDecimal checknull[] = checknullList.toArray(new BigDecimal[checknullList.size()]);

		// 確認用配列と出入金データが等値ならtrue,そうでないならfalse
		if (Arrays.equals(data, checknull)) {
			return true;
		} else {
			return false;
		}
	}

	/* 日ごとグラフ用メソッド */

	// 日ごとグラフの支出金額を算出
	public BigDecimal[] getDailyAmmount(String username, String currentMonth) {
		// その月の初日を取得
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

	// 日ごとグラフの収入金額を算出
	public BigDecimal[] getDailyAmmountIncome(String username, String currentMonth) {
		// その月の初日を取得
		String firstDayStr = currentMonth + "-01";
		LocalDate firstDay = LocalDate.parse(firstDayStr, DateTimeFormatter.ISO_DATE);
		// その月の末日を取得
		LocalDate lastDay = YearMonth.now().atEndOfMonth();

		List<DailySumGraph> dailySum = moneyRecordRepository.findDailyGraphIncome(username, firstDay, lastDay);
		List<BigDecimal> dailyAmmountList = new ArrayList<BigDecimal>();
		for (int i = 0; i < dailySum.size(); i++) {
			dailyAmmountList.add(dailySum.get(i).getSum());
		}
		BigDecimal dailyAmmountIncome[] = dailyAmmountList.toArray(new BigDecimal[dailyAmmountList.size()]);

		return dailyAmmountIncome;
	}

	// 日ごとグラフの日付一覧を取得
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

	/* 履歴表示用メソッド */

	// 一番古い記録を取得する
	public String getOldestDate(String username) {
		String oldestDate = null;
		// 出入金記録があるか確認
		if (moneyRecordRepository.existsByUsername(username)) {
			oldestDate = moneyRecordRepository.getOldestDate(username).toString();
		} else {
			LocalDate ld = LocalDate.now();
			oldestDate = ld.toString();
		}

		return oldestDate;
	}
	
	//一番新しい記録を取得する
	public String getLatestDate(String username) {
		String latestDate = null;
		// 出入金記録があるか確認
		if (moneyRecordRepository.existsByUsername(username)) {
			latestDate = moneyRecordRepository.getLatestDate(username).toString();
		} else {
			LocalDate ld = LocalDate.now();
			latestDate = ld.toString();
		}

		return latestDate;
	}

	// 履歴データが存在するか確認する
	public boolean existsHistoryData(List<MoneyRecordList> records) {

		// 確認用Listクラス
		List<MoneyRecordList> nullRecord = new ArrayList<MoneyRecordList>();

		// 確認用Listとデータが等値ならtrue,そうでないならfalse
		if (records.equals(nullRecord)) {
			return true;
		} else {
			return false;
		}
	}

	// 出入金履歴を取得する
	public List<MoneyRecordList> getMoneyRecordList(String username) {
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordList(username);
		// 備考欄の文字数を一定数でカット
		records = cutNoteOfRecordList(records);

		return records;

	}

	// 特定の日付の出入金履歴を取得（カレンダー用）
	public List<MoneyRecordList> getOneDayRecordList(String username, String date) {

		List<MoneyRecordList> records = moneyRecordRepository.findOneDayRecord(username, date);
		// 備考欄の文字数を一定数でカット
		records = cutNoteOfRecordList(records);

		return records;

	}

	// 出入金履歴の備考を一定の文字数でカットする
	public List<MoneyRecordList> cutNoteOfRecordList(List<MoneyRecordList> records) {

		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}

		return records;
	}

	// 絞込条件を設定（初期設定）
	public RefineCondition setRefineCondition(RefineCondition condition, String username) {

		// 全ての履歴が表示される条件設定にする
		condition.setCategoryCode("all");
		condition.setStartDate(getOldestDate(username));
		condition.setEndDate(getLatestDate(username));

		return condition;

	}

	/* 分析ページ用 */

	// 特定の月の、カテゴリ毎の支出金額を取得
	public List<SummaryByCategory> getExpenseByCategory(String username, String currentMonth) {
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(username, currentMonth);

		// 収入分のデータを削除
		expenseByCategory.remove(expenseByCategory.size() - 1);

		return expenseByCategory;
	}

	// サブカテゴリのカテゴリ名・カテゴリIDをMapで取得
	public Map<Integer, String> findSubcategory(int categoryCode) {

		List<Category> subcategoryList = categoryRepository.findBycategoryCode(categoryCode);
		Map<Integer, String> subcategories = new HashMap<Integer, String>();

		// キーにカテゴリID、値にカテゴリ名を格納する
		for (int i = 0; i < subcategoryList.size(); i++) {
			Integer key = subcategoryList.get(i).getCategoryId();
			String value = subcategoryList.get(i).getSubcategoryName();
			subcategories.put(key, value);
		}

		return subcategories;
	}

	// カテゴリ÷支出の割合を算出
	public Map<Integer, BigDecimal> getPercentages(List<SummaryByCategory> data, BigDecimal total) {

		Map<Integer, BigDecimal> percentages = new HashMap<Integer, BigDecimal>();
		for (int i = 0; i < data.size(); i++) {
			if (total.compareTo(BigDecimal.valueOf(0)) == 0) {
				break;
			}else {
			// 掛け数
			BigDecimal number = BigDecimal.valueOf(100);
			BigDecimal result = data.get(i).getSum().divide(total, 3, RoundingMode.DOWN).multiply(number).setScale(1,
					RoundingMode.DOWN);
			percentages.put(i + 1, result);
			}
		}

		return percentages;
	}

	// サブカテゴリ÷収入の割合を算出
	public Map<Integer, BigDecimal> getPercentagesForIncome(List<SummaryByCategory> data, BigDecimal total) {

		Map<Integer, BigDecimal> percentages = new HashMap<Integer, BigDecimal>();
		for (int i = 0; i < data.size(); i++) {
			if (total.compareTo(BigDecimal.valueOf(0)) == 0) {
				break;
			}else {
			// 掛け数
			BigDecimal number = BigDecimal.valueOf(100);
			BigDecimal result = data.get(i).getSum().divide(total, 3, RoundingMode.DOWN).multiply(number).setScale(1,
					RoundingMode.DOWN);
			
			//サブカテゴリの最初のID 9901を足す
			int incomeSubcateFirstNumber = 9901;
			percentages.put(i + incomeSubcateFirstNumber, result);
			}
		}

		return percentages;
	}

	/* 全般用 */

	// 現在の年月を取得する(SQL用）
	public String getCurrentMonthForSql() {
		LocalDate now = LocalDate.now();
		String strNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String currentMonth = strNow.substring(0, 7); // 〇〇〇〇-〇〇

		return currentMonth;
	}

	// 現在の年月を取得する(View用）
	public String getCurrentMonthForView(String currentMonth) {
		String year = currentMonth.substring(0, 4);
		String month = currentMonth.substring(5, 7);
		String currentMonthForView = year + "年" + month + "月";

		return currentMonthForView;
	}

	/* 出入金CRUD処理 */

	// 出入金記録を登録する
	public void saveRecord(MoneyRecord moneyRecord) {
		// 作成日時を取得
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		moneyRecord.setCreatedAt(ldt);

		moneyRecordRepository.save(moneyRecord);

	}

	// 出入金記録を更新する
	public void updateRecord(MoneyRecord moneyRecord) {
		// 作成日時を取得
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		moneyRecord.setUpdatedAt(ldt);

		moneyRecordRepository.save(moneyRecord);

	}

}
