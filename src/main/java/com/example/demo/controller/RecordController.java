package com.example.demo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.model.Category;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
import com.example.demo.model.beans.MoneyRecordList;
import com.example.demo.model.beans.RefineCondition;
import com.example.demo.model.beans.SummaryByCategory;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.service.DateService;
import com.example.demo.service.MoneyRecordService;
import com.example.demo.util.CategoryCodeToIcon;
import com.example.demo.util.CategoryCodeToName;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class RecordController {

	// DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private final MoneyRecordService mrService;
	private final DateService dService;
	private final CategoryRepository categoryRepository;

	// アプリホーム画面
	@GetMapping("/money-record")
	// Authentication・・・認証済みのユーザー情報を取得
	public String loginProcess(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser,
			Model model) {
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;

		// 現在の年月を取得
		String currentMonthForSql = mrService.getCurrentMonthForSql();
		String currentMonthForView = mrService.getCurrentMonthForView(currentMonthForSql);

		/* 円グラフ用 */

		// 円グラフパラメータ
		String expenseLabel[] = mrService.getExpenseLabel();
		BigDecimal expenseData[] = mrService.getExpenseData(currentUser.getUsername(), currentMonthForSql);

		// 円グラフに表示するデータがあるか確認
		boolean graphDataExists = mrService.existsGraphData(expenseData);

		/* 最近の履歴表示用 */
		List<MoneyRecordList> recordsLimit10 = moneyRecordRepository
				.findMoneyRecordListLimit10(currentUser.getUsername());
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		/* 予算-収入=残金用 */

		// 支出合計を算出
		BigDecimal totalAmmount = moneyRecordRepository.findExpenseSum(currentUser.getUsername(), currentMonthForSql);

		// 予算を取得
		BigDecimal budget = currentUser.getBudget().setScale(0, RoundingMode.DOWN);
		// 予算－支出を計算
		BigDecimal balance = budget.subtract(totalAmmount);

		// 履歴データがあるかチェック用
		boolean historyDataExists = mrService.existsHistoryData(recordsLimit10);

		/* model */

		model.addAttribute("user", currentUser);
		model.addAttribute("subcategories", categoryRepository.findAll());
		model.addAttribute("categories", categories);
		// 円グラフ
		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		// 〇年〇月の状況 用
		model.addAttribute("currentMonth", currentMonthForView);
		// 予算-収入＝残金
		model.addAttribute("graphDataExists", graphDataExists);
		model.addAttribute("budget", budget);
		model.addAttribute("totalAmmount", totalAmmount);
		model.addAttribute("balance", balance);
		// 履歴10件
		model.addAttribute("recordsLimit10", recordsLimit10);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("historyDataExists", historyDataExists);

		return "record-home";
	}

	// 出入金記録を登録
	@PostMapping("/money-record/post")
	public String process(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result,
			Authentication loginUser, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			System.out.println(result);
			return "redirect:/money-record?money-record";
		}

		redirectAttributes.addFlashAttribute("flashMsg", "投稿しました");

		return "redirect:/money-record?money-record";
	}

	// 履歴画面へ遷移(日付降順)
	@GetMapping("/money-record/history")
	public String showRecords(@ModelAttribute("refineCondition") RefineCondition refineCondition,
			Authentication loginUser, Model model) {

		// 出入金履歴一覧を取得
		List<MoneyRecordList> records = mrService.getMoneyRecordList(loginUser.getName());

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// 履歴データがあるかチェック用
		boolean historyDataExists = mrService.existsHistoryData(records);

		// 絞込条件を設定（すべての履歴が表示される条件設定）
		refineCondition = mrService.setRefineCondition(refineCondition, loginUser.getName());

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("categories", categories);
		model.addAttribute("historyDataExists", historyDataExists);
		model.addAttribute("refineCondition", refineCondition);

		return "record-history";
	}

	// 履歴画面へ遷移(日付昇順)
	@GetMapping("/money-record/history/date-asc/{categoryCode}/{startDate}/{endDate}")
	public String showRecordsOrderByDateAsc(@ModelAttribute("categoryCode") String categoryCode,
			@ModelAttribute("startDate") String startDate, @ModelAttribute("endDate") String endDate,
			@ModelAttribute("refineCondition") RefineCondition refineCondition, Authentication loginUser, Model model) {

		// カテゴリ未選択の場合
		if (categoryCode.equals("all")) {
			categoryCode = "%";
		}

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// 上記の条件で絞り込み検索を実施
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordListOrderByDateAsc(loginUser.getName(),
				categoryCode, startDate, endDate);

		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}

		// 履歴データがあるかチェック用
		boolean historyDataExists = mrService.existsHistoryData(records);

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("historyDataExists", historyDataExists);
		model.addAttribute("categories", categories);

		return "record-history";
	}

	// 金額降順
	@GetMapping("/money-record/history/money-desc/{categoryCode}/{startDate}/{endDate}")
	public String showRecordsOrderByMoneyDesc(@ModelAttribute("categoryCode") String categoryCode,
			@ModelAttribute("startDate") String startDate, @ModelAttribute("endDate") String endDate,
			@ModelAttribute("refineCondition") RefineCondition refineCondition, Authentication loginUser, Model model) {

		// カテゴリ未選択の場合
		if (categoryCode.equals("all")) {
			categoryCode = "%";
		}

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// 上記の条件で絞り込み検索を実施
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordListOrderByMoneyDesc(loginUser.getName(),
				categoryCode, startDate, endDate);

		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}

		// 履歴データがあるかチェック用
		boolean historyDataExists = mrService.existsHistoryData(records);

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("historyDataExists", historyDataExists);
		model.addAttribute("categories", categories);

		return "record-history";
	}

	// 金額昇順
	@GetMapping("/money-record/history/money-asc/{categoryCode}/{startDate}/{endDate}")
	public String showRecordsOrderByMoneyAsc(@ModelAttribute("categoryCode") String categoryCode,
			@ModelAttribute("startDate") String startDate, @ModelAttribute("endDate") String endDate,
			@ModelAttribute("refineCondition") RefineCondition refineCondition, Authentication loginUser, Model model) {

		// カテゴリ未選択の場合
		if (categoryCode.equals("all")) {
			categoryCode = "%";
		}

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// 上記の条件で絞り込み検索を実施
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordListOrderByMoneyAsc(loginUser.getName(),
				categoryCode, startDate, endDate);

		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}

		// 履歴データがあるかチェック用
		boolean historyDataExists = mrService.existsHistoryData(records);

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("historyDataExists", historyDataExists);
		model.addAttribute("categories", categories);

		return "record-history";
	}

	// 絞込検索
	@PostMapping("/money-record/history/refine")
	public String refine(@ModelAttribute("refineCondition") RefineCondition refineCondition, Authentication loginUser,
			Model model) {
		// カテゴリ未選択の場合
		if (refineCondition.getCategoryCode().equals("all")) {
			refineCondition.setCategoryCode("%");
		}
		// 開始日未選択の場合
		if (refineCondition.getStartDate().equals("")) {
			// 一番古い記録日を代入
			Object obj = moneyRecordRepository.getOldestDate(loginUser.getName());
			String OldestRecordDate = obj.toString();
			refineCondition.setStartDate(OldestRecordDate);
		}
		// 終了日未選択の場合
		if (refineCondition.getEndDate().equals("")) {
			// 今日の日付を代入
			refineCondition.setEndDate(LocalDate.now().toString());
		}
		String categoryCode = refineCondition.getCategoryCode();
		String startDate = refineCondition.getStartDate();
		String endDate = refineCondition.getEndDate();

		// 上記の条件で絞り込み検索を実施
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordRefinedList(loginUser.getName(),
				categoryCode, startDate, endDate);
		// 備考欄に表示する文字数を調整
		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}

		// カテゴリ未選択の場合(文字列を%からallに戻す)
		if (refineCondition.getCategoryCode().equals("%")) {
			refineCondition.setCategoryCode("all");
		}

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// 履歴データがあるかチェック用
		boolean historyDataExists = mrService.existsHistoryData(records);

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("categories", categories);
		model.addAttribute("historyDataExists", historyDataExists);
		model.addAttribute("refineCondition", refineCondition);

		return "record-history";
	}

	// カレンダーから一覧へ遷移
	@GetMapping("/money-record/history/{date}")
	public String showRecordsByDate(@PathVariable("date") String date,
			@ModelAttribute("refineCondition") RefineCondition refineCondition, Authentication loginUser, Model model) {

		// 特定の日付の出入金履歴を取得
		List<MoneyRecordList> records = mrService.getOneDayRecordList(loginUser.getName(), date);

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// 履歴データがあるかチェック用
		boolean historyDataExists = mrService.existsHistoryData(records);

		model.addAttribute("records", records);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("categories", categories);
		model.addAttribute("historyDataExists", historyDataExists);

		return "record-history";
	}

	// 出入金記録を削除
	@Transactional
	@GetMapping("/money-record/delete/{recordId}/{pageNum}")
	public String deleteRecord(@PathVariable("recordId") int recordId, @PathVariable("pageNum") int pageNum,
			Model model, RedirectAttributes redirectAttributes, UriComponentsBuilder builder) {

		moneyRecordRepository.deleteByRecordId(recordId);

		redirectAttributes.addFlashAttribute("flashMsg", "削除しました");

		// ホーム画面から削除したか、履歴画面から削除したかでリダイレクト先が異なる
		if (pageNum == 1) {
			return "redirect:/money-record?history";
		} else {
			return "redirect:/money-record/history?history";
		}
	}

	// 出入金記録編集画面へ遷移
	@GetMapping("/money-record/edit/{recordId}/{pageNum}")
	public String editRecord(@PathVariable("recordId") int recordId, @PathVariable("pageNum") int pageNum,
			Authentication loginUser, Model model) {

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		List<Category> subcategories = categoryRepository.findAll();

		MoneyRecord record = moneyRecordRepository.findByRecordId(recordId);

		model.addAttribute("record", record);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("subcategories", subcategories);
		model.addAttribute("categories", categories);
		// 遷移元がホーム画面か履歴画面か判別するための引数
		model.addAttribute("pageNum", pageNum);

		return "record-edit";
	}

	// 出入金記録の編集を実行
	@PostMapping("money-record/updateRecord")
	public String updateRecord(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result,
			@ModelAttribute("pageNum") int pageNum, Authentication loginUser, RedirectAttributes redirectAttributes,
			UriComponentsBuilder builder) {

		// リダイレクト先を指定
		URI location = builder.path("/money-record/edit/" + moneyRecord.getRecordId() + "/" + pageNum).build().toUri();

		if (result.hasErrors()) {
			return "redirect:" + location.toString();
		}

		mrService.updateRecord(moneyRecord);

		redirectAttributes.addFlashAttribute("flashMsg", "変更しました");

		// ホーム画面から削除したか、履歴画面から削除したかでリダイレクト先が異なる
		if (pageNum == 1) {
			return "redirect:/money-record?editRecord";
		} else {
			return "redirect:/money-record/history?editRecord";
		}

	}

	@GetMapping("/money-record/analysis")
	// Authentication・・・認証済みのユーザー情報を取得
	public String analysis(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser,
			Model model) {

		// ログインユーザーの情報を取得
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());
		model.addAttribute("user", currentUser);

		// カテゴリ一覧を登録
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);

		// 月一覧を取得
		String[] allMonths = dService.getAllMonths(loginUser.getName());

		// 現在の年月を取得
		String currentMonthForSql = mrService.getCurrentMonthForSql();
		String currentMonthForView = mrService.getCurrentMonthForView(currentMonthForSql);

		/* 日ごとグラフ用のパラメータ */
		// 支出
		BigDecimal dailyAmmount[] = mrService.getDailyAmmount(loginUser.getName(), currentMonthForSql);
		// 収入
		BigDecimal dailyAmmountIncome[] = mrService.getDailyAmmountIncome(loginUser.getName(), currentMonthForSql);
		Integer days[] = mrService.getDays(loginUser.getName(), currentMonthForSql);

		
		/* 収支別円グラフパラメータ */
		String expenseLabel[] = mrService.getExpenseLabel();
		BigDecimal expenseData[] = mrService.getExpenseData(loginUser.getName(), currentMonthForSql);
		String incomeLabel[] = mrService.getIncomeLabel();
		BigDecimal incomeData[] = mrService.getIncomeData(loginUser.getName(), currentMonthForSql);

		/* 支出円グラフ用 */

		// カテゴリアイコン取得
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// カテゴリ毎の合計を取得
		List<SummaryByCategory> expenseByCategory = mrService.getExpenseByCategory(loginUser.getName(),
				currentMonthForSql);

		// 月の支出合計額を算出
		BigDecimal totalAmmountExpense = moneyRecordRepository.findExpenseSum(currentUser.getUsername(), currentMonthForSql);

		// カテゴリ÷全体支出の割合を算出
		Map<Integer, BigDecimal> expensePercentages = mrService.getPercentages(expenseByCategory, totalAmmountExpense);
		
		
		/* 収入円グラフ用 */
		// サブカテゴリ毎の合計を算出
		List<SummaryByCategory> incomeBySubcategory = moneyRecordRepository
				.findSubcategorySummaries(currentUser.getUsername(), currentMonthForSql, 99);
		// サブカテゴリを取得
		Map<Integer, String> incomeSubcategories = mrService.findSubcategory(99);
		
		// 収入の合計を算出
		BigDecimal totalAmmountIncome = moneyRecordRepository.findIncomeSum(currentUser.getUsername(), currentMonthForSql);

		//カテゴリ÷収入合計の割合を表示
		Map<Integer, BigDecimal> incomePercentages = mrService.getPercentagesForIncome(incomeBySubcategory, totalAmmountIncome);
		
		/* 円グラフに表示するデータがあるか確認 */
		//支出
		boolean expenseDataExists = mrService.existsGraphData(expenseData);
		//収入
		boolean incomeDataExists = mrService.existsGraphDataForIncome(incomeData);

		// 今月の収支を計算
		BigDecimal total = totalAmmountIncome.subtract(totalAmmountExpense);
		BigDecimal budget = currentUser.getBudget().setScale(0, RoundingMode.DOWN);
		BigDecimal totalRatio = budget.subtract(totalAmmountExpense);

		// 収支合計額表示
		model.addAttribute("total", total);
		model.addAttribute("budgetRatio", totalRatio);

		// 支出内訳用
		model.addAttribute("categories", categories);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("percentages", expensePercentages);
		model.addAttribute("totalsByCategory", expenseByCategory);
		model.addAttribute("totalAmmountExpense", totalAmmountExpense);

		// 収入内訳用
		model.addAttribute("incomeTotals", incomeBySubcategory);
		model.addAttribute("incomeSubcategories", incomeSubcategories);
		model.addAttribute("totalAmmountIncome", totalAmmountIncome);
		model.addAttribute("incomePercentages", incomePercentages);

		// 収支別円グラフ
		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		model.addAttribute("incomeLabel", incomeLabel);
		model.addAttribute("incomeData", incomeData);

		// 円グラフデータチェック用
		model.addAttribute("expenseDataExists", expenseDataExists);
		model.addAttribute("incomeDataExists", incomeDataExists);

		// 日ごとグラフ
		model.addAttribute("label", days);
		model.addAttribute("dailyAmmountExpense", dailyAmmount);
		model.addAttribute("dailyAmmountIncome", dailyAmmountIncome);

		// 月切り替え用パラメータ
		model.addAttribute("months", allMonths);
		model.addAttribute("currentMonth", currentMonthForView);

		return "record-analysis";
	}

	// 過去月の分析を表示
	@GetMapping("/money-record/analysis/{month}")
	// Authentication・・・認証済みのユーザー情報を取得
	public String analysisByMonth(@PathVariable("month") String month, Authentication loginUser, Model model) {
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());
		
		model.addAttribute("user", currentUser);
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);

		// 年月を取得
		String currentMonthForSql = month;
		String currentMonthForView = mrService.getCurrentMonthForView(currentMonthForSql);

		// 月一覧を取得
		String[] allMonths = dService.getAllMonths(loginUser.getName());

		// 日ごとグラフ用のパラメータ
		BigDecimal dailyAmmount[] = mrService.getDailyAmmount(loginUser.getName(), month);
		Integer days[] = mrService.getDays(loginUser.getName(), month);
		// 収入
		BigDecimal dailyAmmountIncome[] = mrService.getDailyAmmountIncome(loginUser.getName(), month);

		// 収支別円グラフパラメータ
		String expenseLabel[] = mrService.getExpenseLabel();
		BigDecimal expenseData[] = mrService.getExpenseData(loginUser.getName(), month);
		String incomeLabel[] = mrService.getIncomeLabel();
		BigDecimal incomeData[] = mrService.getIncomeData(loginUser.getName(), month);

		/* 支出円グラフ用 */

		// カテゴリアイコン取得
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// カテゴリ毎の合計を取得
		List<SummaryByCategory> expenseByCategory = mrService.getExpenseByCategory(loginUser.getName(),
				currentMonthForSql);

		// 月の支出合計額を算出
		BigDecimal totalAmmountExpense = moneyRecordRepository.findExpenseSum(currentUser.getUsername(), currentMonthForSql);
		
		// カテゴリ÷全体支出の割合を算出
		Map<Integer, BigDecimal> expensePercentages = mrService.getPercentages(expenseByCategory, totalAmmountExpense);
		
		
		/* 収入円グラフ用 */

		// サブカテゴリ毎の合計を算出
		List<SummaryByCategory> incomeBySubcategory = moneyRecordRepository
				.findSubcategorySummaries(currentUser.getUsername(), currentMonthForSql, 99);
		
		// サブカテゴリを取得
		Map<Integer, String> incomeSubcategories = mrService.findSubcategory(99);
		
		// 収入の合計を算出
		BigDecimal totalAmmountIncome = moneyRecordRepository.findIncomeSum(currentUser.getUsername(), currentMonthForSql);
		
		// 収入の割合を算出
		Map<Integer, BigDecimal> incomePercentages = mrService.getPercentagesForIncome(incomeBySubcategory, totalAmmountIncome);

		/* 円グラフに表示するデータがあるか確認 */
		//支出
		boolean expenseDataExists = mrService.existsGraphData(expenseData);
		//収入
		boolean incomeDataExists = mrService.existsGraphDataForIncome(incomeData);

		// 今月の収支を計算
		BigDecimal total = totalAmmountIncome.subtract(totalAmmountExpense);
		BigDecimal totalRatio = currentUser.getBudget().subtract(totalAmmountExpense);

		// 収支合計額表示
		model.addAttribute("total", total);
		model.addAttribute("budgetRatio", totalRatio);

		// 支出内訳用
		model.addAttribute("categories", categories);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("percentages", expensePercentages);
		model.addAttribute("totalsByCategory", expenseByCategory);
		model.addAttribute("totalAmmountExpense", totalAmmountExpense);

		// 収入内訳用
		model.addAttribute("incomeTotals", incomeBySubcategory);
		model.addAttribute("incomeSubcategories", incomeSubcategories);
		model.addAttribute("totalAmmountIncome", totalAmmountIncome);
		model.addAttribute("incomePercentages", incomePercentages);

		// 収支別円グラフ
		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		model.addAttribute("incomeLabel", incomeLabel);
		model.addAttribute("incomeData", incomeData);

		// 円グラフデータチェック用
		model.addAttribute("expenseDataExists", expenseDataExists);
		model.addAttribute("incomeDataExists", incomeDataExists);

		// 日ごとグラフ
		model.addAttribute("label", days);
		model.addAttribute("dailyAmmountExpense", dailyAmmount);
		model.addAttribute("dailyAmmountIncome", dailyAmmountIncome);

		// 月切り替え用パラメータ
		model.addAttribute("months", allMonths);
		model.addAttribute("currentMonth",currentMonthForView); 

		return "record-analysis";

	}
}
