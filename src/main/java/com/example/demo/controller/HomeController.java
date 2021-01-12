package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javax.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dao.CategoryDaoImpl;
import com.example.demo.util.CategoryCodeToName;
import com.example.demo.model.Category;
import com.example.demo.model.CategoryName;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
import com.example.demo.model.SummaryByCategory;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.dao.MoneyRecordDaoImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class HomeController {

	// DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private MoneyRecordDaoImpl mrDao;
	private CategoryDaoImpl cDao;
	private final CategoryRepository categoryRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void init() {
		mrDao = new MoneyRecordDaoImpl(em);
		cDao = new CategoryDaoImpl(em);
	}
	
	//テスト
	@GetMapping("/test")
	public String test(Model model) {
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);
		return "test";
	}
	
	//ホーム画面へ遷移
	@GetMapping("/main")
	public String main(@ModelAttribute MoneyRecord moneyRecord, Authentication loginUser, Model model) {
		//支出のカテゴリ一覧を設定
		List<String> expenseCategory = new ArrayList<String>();
		for(int i = 1; i < CategoryCodeToName.Categories.size(); i++) {
				expenseCategory.add(CategoryCodeToName.Categories.get(i));
		}
		String expenseLabel[] = expenseCategory.toArray(new String[expenseCategory.size()]);
		
		//支出のカテゴリ毎の合計を設定
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(loginUser.getName(), "2020-12");
		List<BigDecimal> expenseAmmount = new ArrayList<BigDecimal>();
		for(int i = 0; i < expenseByCategory.size()-1; i++) {
			expenseAmmount.add(expenseByCategory.get(i).getSum());
		}
		BigDecimal expenseData[] = expenseAmmount.toArray(new BigDecimal[expenseAmmount.size()]);
		
		//収入のカテゴリ一覧を設定
		List<Category> incomeCategories = categoryRepository.findBycategoryCode(99);
		System.out.println(categoryRepository.findBycategoryCode(99));
		List<String> incomeCategoriesStr = new ArrayList<String>();
		for(int i = 0; i < incomeCategories.size(); i++) {
			incomeCategoriesStr.add(incomeCategories.get(i).getSubcategoryName());
		}
		String incomeLabel[] = incomeCategoriesStr.toArray(new String[incomeCategoriesStr.size()]);
		
		//収入のカテゴリ毎の合計を設定
		List<SummaryByCategory> incomeByCategory = moneyRecordRepository.findSubcategorySummaries(loginUser.getName(), "2020-12", 99);
		List<BigDecimal> incomeAmmount = new ArrayList<BigDecimal>();
		for(int i = 0; i < incomeByCategory.size(); i++) {
			incomeAmmount.add(incomeByCategory.get(i).getSum());
		}
		BigDecimal incomeData[] = incomeAmmount.toArray(new BigDecimal[incomeAmmount.size()]);
		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		model.addAttribute("incomeLabel", incomeLabel);
		model.addAttribute("incomeData", incomeData);	
		
		return "main";
	}
	
	//ユーザー設定変更画面へ遷移
	@GetMapping("/setting")
	public String setting(Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		return "setting";
	}
	
	//ユーザー設定変更を実行
	@PostMapping("/setting")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result,
			Authentication loginUser) {
		// 同名ユーザー、メールアドレスのアカウントが存在していないか確認
		if (userRepository.existsByUsername(user.getUsername()) == true
				| userRepository.existsByEmail(user.getEmail()) == true) {
			return "main";
		}
		// @Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		if (result.hasErrors()) {
			return "main";
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setUpdatedAt(ldt);
		userRepository.save(user);

		return "redirect:/main?setting";
	}
	
	//ユーザーを削除
	@Transactional
	@GetMapping("/deleteUser")
	public String deleteUser(Authentication loginUser) {
		userRepository.deleteByUsername(loginUser.getName());

		return "login";
	}
	//出入金記録登録画面へ遷移
	@GetMapping("/record")
	public String record(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser,
			Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);
		
		return "recordPost";
	}

	
	//出入金記録を登録
	@PostMapping("/record")
	public String process(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result,
			Authentication loginUser) {
		if (result.hasErrors()) {
			System.out.println(result);
			System.out.println(moneyRecord.getNote());
			return "redirect:/post?post";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		moneyRecord.setCreatedAt(ldt);
		moneyRecordRepository.save(moneyRecord);

		return "redirect:/main?recordPost";
	}
	
	//履歴画面へ遷移
	@GetMapping("/showRecords")
	public String showRecords(Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("records", moneyRecordRepository.findByUsernameOrderByRecordDate(loginUser.getName()));
		return "record";
	}
	
	//出入金記録詳細画面へ遷移
	@RequestMapping("/record/{recordId}")
	public String showRecord(@PathVariable("recordId") Long recordId, Model model) {
		model.addAttribute("record", moneyRecordRepository.findByRecordId(recordId));

		return "";
	}
	
	//出入金記録を削除
	@Transactional
	@GetMapping("/deleteRecord/{recordId}")
	public String deleteRecord(@PathVariable("recordId") Long recordId, Model model) {
		moneyRecordRepository.deleteById(recordId);

		return "redirect:/showRecords?showRecords";
	}
	
	//出入金記録編集画面へ遷移
	@GetMapping("/editRecord/{recordId}")
	public String editRecord(@PathVariable("recordId") Long recordId, Authentication loginUser, Model model) {
		model.addAttribute("record", moneyRecordRepository.findByRecordId(recordId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("categories", categoryRepository.findAll());

		return "recordEdit";
	}
	
	//出入金記録の編集を実行
	@PostMapping("/updateRecord")
	public String updateRecord(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result,
			Authentication loginUser) {
		if (result.hasErrors()) {
			return "main";
		}

		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		moneyRecord.setUpdatedAt(ldt);

		moneyRecordRepository.save(moneyRecord);

		return "redirect:/main?editRecord";
	}

}