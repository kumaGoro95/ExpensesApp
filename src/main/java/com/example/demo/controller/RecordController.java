package com.example.demo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.dao.MoneyRecordDaoImpl;
import com.example.demo.model.Category;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
import com.example.demo.model.beans.DailySumGraph;
import com.example.demo.model.beans.MoneyRecordList;
import com.example.demo.model.beans.RecordDataList;
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
@SessionAttributes(types = RecordDataList.class)
public class RecordController {

	// DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private final MoneyRecordService mrService;
	private final DateService dService;
	private MoneyRecordDaoImpl mrDao;
	private final CategoryRepository categoryRepository;
	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void init() {
		mrDao = new MoneyRecordDaoImpl(em);
	}

	// 出入金記録を登録
	@PostMapping("/money-record/post")
	public String process(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result,
			Authentication loginUser, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			System.out.println(result);
			return "redirect:/money-record?money-record";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		moneyRecord.setCreatedAt(ldt);
		moneyRecordRepository.save(moneyRecord);
		redirectAttributes.addFlashAttribute("flashMsg", "投稿しました");

		return "redirect:/money-record?money-record";
	}

	// 履歴画面へ遷移(日付降順)
	@GetMapping("/money-record/history")
	public String showRecords(@ModelAttribute("refineCondition") RefineCondition refineCondition,
			Authentication loginUser, Model model) {
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordList(loginUser.getName());
		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// 履歴データがあるかチェック用
		List<MoneyRecordList> nullRecord = new ArrayList<MoneyRecordList>();

		// 並べ替え用recordId一覧
		List<Integer> intList = new ArrayList<Integer>();
		for (int i = 0; i < records.size(); i++) {
			intList.add(records.get(i).getRecordId());
		}
		Integer[] recordIds = intList.toArray(new Integer[records.size()]);
		RecordDataList dataList = new RecordDataList(recordIds);

		refineCondition.setCategoryCode("all");
		refineCondition.setStartDate(mrService.getOldestDate(loginUser.getName()));
		refineCondition.setEndDate(LocalDate.now().toString());

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("categories", categories);
		model.addAttribute("nullRecord", nullRecord);
		model.addAttribute("refineCondition", refineCondition);
		model.addAttribute("dataList", dataList);

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
		List<MoneyRecordList> nullRecord = new ArrayList<MoneyRecordList>();

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("nullRecord", nullRecord);
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
		List<MoneyRecordList> nullRecord = new ArrayList<MoneyRecordList>();

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("nullRecord", nullRecord);
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
		List<MoneyRecordList> nullRecord = new ArrayList<MoneyRecordList>();

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("nullRecord", nullRecord);
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
		List<MoneyRecordList> nullRecord = new ArrayList<MoneyRecordList>();

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("categories", categories);
		model.addAttribute("nullRecord", nullRecord);
		model.addAttribute("refineCondition", refineCondition);

		System.out.println(refineCondition);

		return "record-history";
	}

	// カレンダーから一覧へ遷移
	@GetMapping("/money-record/history/{date}")
	public String showRecordsByDate(@PathVariable("date") String date,
			@ModelAttribute("refineCondition") RefineCondition refineCondition, Authentication loginUser, Model model) {

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// 履歴データがあるかチェック用
		List<MoneyRecordList> nullRecord = new ArrayList<MoneyRecordList>();

		model.addAttribute("records", moneyRecordRepository.findOneDayRecord(loginUser.getName(), date));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("categories", categories);
		model.addAttribute("nullRecord", nullRecord);

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
		model.addAttribute("record", moneyRecordRepository.findByRecordId(recordId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);
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

		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		moneyRecord.setUpdatedAt(ldt);

		moneyRecordRepository.save(moneyRecord);

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
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());
		model.addAttribute("user", currentUser);

		// カテゴリ一覧を登録
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);

		// 月一覧を取得
		String[] allMonths = dService.getAllMonths(loginUser.getName());

		// 現在の月を取得
		LocalDate now = LocalDate.now();
		String strNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String currentMonth = strNow.substring(0, 7);
		String year = currentMonth.substring(0, 4);
		String month = currentMonth.substring(5, 7);

		// 日ごとグラフ用のパラメータ
		// 支出
		BigDecimal dailyAmmount[] = mrService.getDailyAmmount(loginUser.getName(), currentMonth);
		// 収入
		BigDecimal dailyAmmountIncome[] = mrService.getDailyAmmountIncome(loginUser.getName(), currentMonth);
		Integer days[] = mrService.getDays(loginUser.getName(), currentMonth);

		// 収支別円グラフパラメータ
		String expenseLabel[] = mrService.getExpenseLabel();
		BigDecimal expenseData[] = mrService.getExpenseData(loginUser.getName(), currentMonth);
		String incomeLabel[] = mrService.getIncomeLabel();
		BigDecimal incomeData[] = mrService.getIncomeData(loginUser.getName(), currentMonth);

		/* 支出円グラフ用 */

		// カテゴリアイコン取得
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		// カテゴリ毎の合計を取得
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(loginUser.getName(),
				currentMonth);

		// 収入分を削除
		expenseByCategory.remove(expenseByCategory.size() - 1);

		// 月の合計額を算出
		BigDecimal totalAmmountExpense = new BigDecimal(0.0);
		for (int i = 0; i < expenseByCategory.size(); i++) {
			totalAmmountExpense = totalAmmountExpense.add(expenseByCategory.get(i).getSum());
		}
		// カテゴリ÷全体支出の割合を算出
		Map<Integer, BigDecimal> percentages = new HashMap<Integer, BigDecimal>();
		for (int i = 0; i < expenseByCategory.size(); i++) {
			if (totalAmmountExpense == BigDecimal.valueOf(0)) {
				break;
			}
			BigDecimal number = BigDecimal.valueOf(100);
			BigDecimal result = expenseByCategory.get(i).getSum().divide(totalAmmountExpense, 3, RoundingMode.DOWN)
					.multiply(number).setScale(1, RoundingMode.DOWN);
			percentages.put(i + 1, result);
		}

		/* 収入円グラフ用 */
		// サブカテゴリ毎の合計を算出
		List<SummaryByCategory> incomeTotalsBySubCategory = moneyRecordRepository
				.findSubcategorySummaries(currentUser.getUsername(), currentMonth, 99);
		// サブカテゴリを取得
		List<Category> incomeSubcategoryList = categoryRepository.findBycategoryCode(99);
		Map<Integer, String> incomeSubcategories = new HashMap<Integer, String>();
		for (int i = 0; i < incomeSubcategoryList.size(); i++) {
			Integer key = incomeSubcategoryList.get(i).getCategoryId();
			String value = incomeSubcategoryList.get(i).getSubcategoryName();
			incomeSubcategories.put(key, value);
		}

		// 収入の合計を算出
		BigDecimal totalAmmountIncome = new BigDecimal(0.0);
		for (int i = 0; i < incomeTotalsBySubCategory.size(); i++) {
			totalAmmountIncome = totalAmmountIncome.add(incomeTotalsBySubCategory.get(i).getSum());
		}

		Map<Integer, BigDecimal> incomePercentages = new HashMap<Integer, BigDecimal>();
		for (int i = 0; i < 0 + incomeTotalsBySubCategory.size(); i++) {
			if (totalAmmountIncome == BigDecimal.valueOf(0)) {
				break;
			}
			int incomeSubcateFirstNumber = 9901;
			BigDecimal number = BigDecimal.valueOf(100);
			BigDecimal result = incomeTotalsBySubCategory.get(i).getSum()
					.divide(totalAmmountIncome, 3, RoundingMode.DOWN).multiply(number).setScale(1, RoundingMode.DOWN);
			incomePercentages.put(i + incomeSubcateFirstNumber, result);
		}

		// 円グラフに表示するデータがあるか確認
		List<BigDecimal> checknullList = new ArrayList<BigDecimal>();
		for (int i = 0; i < 15; i++) {
			checknullList.add(BigDecimal.valueOf(0));
		}
		List<BigDecimal> checknullIncomeList = new ArrayList<BigDecimal>();
		for (int i = 0; i < 3; i++) {
			checknullIncomeList.add(BigDecimal.valueOf(0));
		}
		BigDecimal checknull[] = checknullList.toArray(new BigDecimal[checknullList.size()]);
		BigDecimal checknullIncome[] = checknullIncomeList.toArray(new BigDecimal[checknullIncomeList.size()]);

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
		model.addAttribute("percentages", percentages);
		model.addAttribute("totalsByCategory", expenseByCategory);
		model.addAttribute("totalAmmountExpense", totalAmmountExpense);

		// 収入内訳用
		model.addAttribute("incomeTotals", incomeTotalsBySubCategory);
		model.addAttribute("incomeSubcategories", incomeSubcategories);
		model.addAttribute("totalAmmountIncome", totalAmmountIncome);
		model.addAttribute("incomePercentages", incomePercentages);

		// 収支別円グラフ
		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		model.addAttribute("incomeLabel", incomeLabel);
		model.addAttribute("incomeData", incomeData);

		// 円グラフデータチェック用
		model.addAttribute("checknull", checknull);
		model.addAttribute("checknullIncome", checknullIncome);

		// 日ごとグラフ
		model.addAttribute("label", days);
		model.addAttribute("dailyAmmountExpense", dailyAmmount);
		model.addAttribute("dailyAmmountIncome", dailyAmmountIncome);

		// 月切り替え用パラメータ
		model.addAttribute("months", allMonths);
		model.addAttribute("year", year);
		model.addAttribute("month", month);

		// 現在の月をドロップダウンに表示
		model.addAttribute("currentMonth", currentMonth);

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
		String currentYear = month.substring(0, 4);
		String currentMonth = month.substring(5, 7);

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
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(loginUser.getName(),
				month);

		// 収入分を削除
		expenseByCategory.remove(expenseByCategory.size() - 1);

		// 月の合計額を算出
		BigDecimal totalAmmountExpense = new BigDecimal(0.0);
		for (int i = 0; i < expenseByCategory.size(); i++) {
			totalAmmountExpense = totalAmmountExpense.add(expenseByCategory.get(i).getSum());
		}
		// カテゴリ÷全体支出の割合を算出
		Map<Integer, BigDecimal> percentages = new HashMap<Integer, BigDecimal>();
		for (int i = 0; i < expenseByCategory.size(); i++) {
			if (totalAmmountExpense == BigDecimal.valueOf(0)) {
				break;
			}
			BigDecimal number = BigDecimal.valueOf(100);
			BigDecimal result = expenseByCategory.get(i).getSum().divide(totalAmmountExpense, 3, RoundingMode.DOWN)
					.multiply(number).setScale(1, RoundingMode.DOWN);
			percentages.put(i + 1, result);
		}

		/* 収入円グラフ用 */

		// サブカテゴリ毎の合計を算出
		List<SummaryByCategory> incomeTotalsBySubCategory = moneyRecordRepository
				.findSubcategorySummaries(currentUser.getUsername(), month, 99);
		// サブカテゴリを取得
		List<Category> incomeSubcategoryList = categoryRepository.findBycategoryCode(99);
		Map<Integer, String> incomeSubcategories = new HashMap<Integer, String>();
		for (int i = 0; i < incomeSubcategoryList.size(); i++) {
			Integer key = incomeSubcategoryList.get(i).getCategoryId();
			String value = incomeSubcategoryList.get(i).getSubcategoryName();
			incomeSubcategories.put(key, value);
		}
		// 収入の合計を算出
		BigDecimal totalAmmountIncome = new BigDecimal(0.0);
		for (int i = 0; i < incomeTotalsBySubCategory.size(); i++) {
			totalAmmountIncome = totalAmmountIncome.add(incomeTotalsBySubCategory.get(i).getSum());
		}
		// 収入の割合を算出
		Map<Integer, BigDecimal> incomePercentages = new HashMap<Integer, BigDecimal>();
		for (int i = 0; i < incomeTotalsBySubCategory.size(); i++) {
			if (totalAmmountIncome == BigDecimal.valueOf(0)) {
				break;
			}
			BigDecimal number = BigDecimal.valueOf(100);
			BigDecimal result = incomeTotalsBySubCategory.get(i).getSum()
					.divide(totalAmmountIncome, 3, RoundingMode.DOWN).multiply(number).setScale(1, RoundingMode.DOWN);
			incomePercentages.put(i + 1, result);
		}

		// 円グラフに表示するデータがあるか確認
		List<BigDecimal> checknullList = new ArrayList<BigDecimal>();
		for (int i = 0; i < 15; i++) {
			checknullList.add(BigDecimal.valueOf(0));
		}
		List<BigDecimal> checknullIncomeList = new ArrayList<BigDecimal>();
		for (int i = 0; i < 3; i++) {
			checknullIncomeList.add(BigDecimal.valueOf(0));
		}
		BigDecimal checknull[] = checknullList.toArray(new BigDecimal[checknullList.size()]);
		BigDecimal checknullIncome[] = checknullIncomeList.toArray(new BigDecimal[checknullIncomeList.size()]);

		// 今月の収支を計算
		BigDecimal total = totalAmmountIncome.subtract(totalAmmountExpense);
		BigDecimal totalRatio = currentUser.getBudget().subtract(totalAmmountExpense);

		// 収支合計額表示
		model.addAttribute("total", total);
		model.addAttribute("budgetRatio", totalRatio);

		// 支出内訳用
		model.addAttribute("categories", categories);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("percentages", percentages);
		model.addAttribute("totalsByCategory", expenseByCategory);
		model.addAttribute("totalAmmountExpense", totalAmmountExpense);

		// 収入内訳用
		model.addAttribute("incomeTotals", incomeTotalsBySubCategory);
		model.addAttribute("incomeSubcategories", incomeSubcategories);
		model.addAttribute("totalAmmountIncome", totalAmmountIncome);
		model.addAttribute("incomePercentages", incomePercentages);

		// 収支別円グラフ
		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		model.addAttribute("incomeLabel", incomeLabel);
		model.addAttribute("incomeData", incomeData);

		// 円グラフデータチェック用
		model.addAttribute("checknull", checknull);
		model.addAttribute("checknullIncome", checknullIncome);

		// 日ごとグラフ
		model.addAttribute("label", days);
		model.addAttribute("dailyAmmountExpense", dailyAmmount);
		model.addAttribute("dailyAmmountIncome", dailyAmmountIncome);

		// 月切り替え用パラメータ
		model.addAttribute("months", allMonths);
		model.addAttribute("year", currentYear);
		model.addAttribute("month", currentMonth);

		// ドロップダウン用
		model.addAttribute("currentMonth", month);

		return "record-analysis";

	}
}
