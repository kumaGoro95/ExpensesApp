package com.example.demo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dao.MoneyRecordDaoImpl;
import com.example.demo.model.Category;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
import com.example.demo.model.beans.DailySumGraph;
import com.example.demo.model.beans.MoneyRecordList;
import com.example.demo.model.beans.SummaryByCategory;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.util.CategoryCodeToName;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class RecordController {

	// DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private MoneyRecordDaoImpl mrDao;
	private final CategoryRepository categoryRepository;
	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void init() {
		mrDao = new MoneyRecordDaoImpl(em);
	}

	// 出入金記録登録画面へ遷移
	@GetMapping("/record")
	public String record(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser,
			Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);

		return "recordPost";
	}

	// 出入金記録を登録
	@PostMapping("/record")
	public String process(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result,
			Authentication loginUser, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			System.out.println(result);
			return "redirect:/recordPost?recordPost";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		moneyRecord.setCreatedAt(ldt);
		moneyRecordRepository.save(moneyRecord);
		redirectAttributes.addFlashAttribute("flashMsg", "投稿しました");

		return "redirect:/?recordPost";
	}

	// 履歴画面へ遷移(日付降順)
	@GetMapping("/showRecords")
	public String showRecords(Authentication loginUser, Model model) {
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordList(loginUser.getName());
		for(int i = 0; i < records.size(); i++) {
			if(records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		model.addAttribute("icon", "fas fa-utensils");
		return "record";
	}

	// 履歴画面へ遷移(日付降順)
	@GetMapping("/showRecordsOrderByDateAsc")
	public String showRecordsOrderByDateAsc(Authentication loginUser, Model model) {
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordListOrderByDateAsc(loginUser.getName());
		for(int i = 0; i < records.size(); i++) {
			if(records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		return "record";
	}

	@GetMapping("/showRecordsOrderByMoneyDesc")
	public String showRecordsOrderByMoneyDesc(Authentication loginUser, Model model) {
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordListOrderByMoneyDesc(loginUser.getName());
		for(int i = 0; i < records.size(); i++) {
			if(records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		return "record";
	}

	@GetMapping("/showRecordsOrderByMoneyAsc")
	public String showRecordsOrderByMoneyAsc(Authentication loginUser, Model model) {
		List<MoneyRecordList> records = moneyRecordRepository.findMoneyRecordListOrderByMoneyAsc(loginUser.getName());
		for(int i = 0; i < records.size(); i++) {
			if(records.get(i).getNote().length() > 13) {
				records.get(i).setNote(records.get(i).getNote().substring(0, 10) + "…");
			}
		}
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", records);
		return "record";
	}

	// カレンダーから一覧へ遷移
	@GetMapping("/Records/{date}")
	public String showRecordsByDate(@PathVariable("date") String date, Authentication loginUser, Model model) {
		model.addAttribute("records", moneyRecordRepository.findOneDayRecord(loginUser.getName(), date));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("icon", "fas fa-utensils");

		return "refinedRecord";
	}

	// 出入金記録を削除
	@Transactional
	@GetMapping("/deleteRecord/{recordId}")
	public String deleteRecord(@PathVariable("recordId") int recordId, Model model, RedirectAttributes redirectAttributes) {
		moneyRecordRepository.deleteByRecordId(recordId);
		
		redirectAttributes.addFlashAttribute("flashMsg", "削除しました");

		return "redirect:/showRecords?showRecords";
	}

	// 出入金記録編集画面へ遷移
	@GetMapping("/editRecord/{recordId}")
	public String editRecord(@PathVariable("recordId") int recordId, Authentication loginUser, Model model) {
		model.addAttribute("record", moneyRecordRepository.findByRecordId(recordId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);

		return "recordEdit";
	}

	// 出入金記録の編集を実行
	@PostMapping("/updateRecord")
	public String updateRecord(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result,
			Authentication loginUser, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "/";
		}

		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		moneyRecord.setUpdatedAt(ldt);

		moneyRecordRepository.save(moneyRecord);
		redirectAttributes.addFlashAttribute("flashMsg", "変更しました");

		return "redirect:/showRecords?editRecord";
	}

	@GetMapping("/analysis")
	// Authentication・・・認証済みのユーザー情報を取得
	public String analysis(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser,
			Model model) {
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());
		model.addAttribute("user", currentUser);
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);
		
		//記録の内一番古いものを獲得
		Object a = moneyRecordRepository.getOldestDate(loginUser.getName());
		String oldDate = a.toString();
		String month = oldDate.substring(0, 7);
		//※古いものを取得して、現在までの年月一覧を取得したいのだが・・・
		
		

		// 現在の月を取得
		LocalDate now = LocalDate.now();
		String strNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String currentMonth = strNow.substring(0, 7);
		// 初日と末日を取得
		String firstDayStr = currentMonth + "-01";
		LocalDate firstDay = LocalDate.parse(firstDayStr, DateTimeFormatter.ISO_DATE);
		LocalDate lastDay = YearMonth.now().atEndOfMonth();

		// グラフに代入する数値を用意
		List<DailySumGraph> dailySum = moneyRecordRepository.findDailyGraph(loginUser.getName(), firstDay, lastDay);
		List<Integer> daysList = new ArrayList<Integer>();
		for (int i = 1; i < dailySum.size() + 1; i++) {
			daysList.add(i);
		}
		Integer days[] = daysList.toArray(new Integer[dailySum.size()]);
		List<BigDecimal> dailyAmmountList = new ArrayList<BigDecimal>();
		for (int i = 0; i < dailySum.size(); i++) {
			dailyAmmountList.add(dailySum.get(i).getSum());
		}
		BigDecimal dailyAmmount[] = dailyAmmountList.toArray(new BigDecimal[dailyAmmountList.size()]);
		model.addAttribute("label", days);
		model.addAttribute("data", dailyAmmount);

		// 支出のカテゴリ一覧を設定
		List<String> expenseCategory = new ArrayList<String>();
		for (int i = 1; i < CategoryCodeToName.Categories.size(); i++) {
			expenseCategory.add(CategoryCodeToName.Categories.get(i));
		}
		String expenseLabel[] = expenseCategory.toArray(new String[expenseCategory.size()]);

		// 支出のカテゴリ毎の合計を設定
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(loginUser.getName(),
				currentMonth);
		List<BigDecimal> expenseAmmount = new ArrayList<BigDecimal>();
		for (int i = 0; i < expenseByCategory.size() - 1; i++) {
			expenseAmmount.add(expenseByCategory.get(i).getSum());
		}
		BigDecimal expenseData[] = expenseAmmount.toArray(new BigDecimal[expenseAmmount.size()]);

		// 収入のカテゴリ一覧を設定
		List<Category> incomeCategories = categoryRepository.findBycategoryCode(99);
		List<String> incomeCategoriesStr = new ArrayList<String>();
		for (int i = 0; i < incomeCategories.size(); i++) {
			incomeCategoriesStr.add(incomeCategories.get(i).getSubcategoryName());
		}
		String incomeLabel[] = incomeCategoriesStr.toArray(new String[incomeCategoriesStr.size()]);

		// 収入のカテゴリ毎の合計を設定
		List<SummaryByCategory> incomeByCategory = moneyRecordRepository.findSubcategorySummaries(loginUser.getName(),
				currentMonth, 99);
		List<BigDecimal> incomeAmmount = new ArrayList<BigDecimal>();
		for (int i = 0; i < incomeByCategory.size(); i++) {
			incomeAmmount.add(incomeByCategory.get(i).getSum());
		}
		BigDecimal incomeData[] = incomeAmmount.toArray(new BigDecimal[incomeAmmount.size()]);

		// 予算の取得
		BigDecimal budget = currentUser.getBudget();
		BigDecimal totalAmmount = mrDao.sumMonthExpense(currentUser.getUsername(), month);
		BigDecimal percent = totalAmmount.divide(budget, 2, RoundingMode.HALF_UP);
		String totalLabel = "月";

		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		model.addAttribute("incomeLabel", incomeLabel);
		model.addAttribute("incomeData", incomeData);

		model.addAttribute("total", percent);
		model.addAttribute("totalLabel", totalLabel);

		return "analysis";
	}

	@GetMapping("/analysis/{month}")
	// Authentication・・・認証済みのユーザー情報を取得
	public String analysisByMonth(@PathVariable("month") String month, Authentication loginUser,
			Model model) {
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());
		model.addAttribute("user", currentUser);
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);
		
		//記録の内一番古いものを獲得
		Object a = moneyRecordRepository.getOldestDate(loginUser.getName());
		String oldDate = a.toString();
		String firstDay = oldDate.substring(0, 7);
		
		return "hoge";

	}
}
