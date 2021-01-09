package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.dao.MoneyRecordDaoImpl;
import com.example.demo.model.Category;
import com.example.demo.model.SiteUser;
import com.example.demo.model.SummaryByCategory;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.util.CategoryCodeToName;
import com.example.demo.util.Role;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class SecurityController {

	// DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final CategoryRepository categoryRepository;

	@GetMapping("/login")
	public String login(@ModelAttribute("user") SiteUser user) {
		return "login";
	}

	@GetMapping("/")
	// Authentication・・・認証済みのユーザー情報を取得
	public String loginProcess(Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		// 支出のカテゴリ一覧を設定
		List<String> expenseCategory = new ArrayList<String>();
		for (int i = 1; i < CategoryCodeToName.Categories.size(); i++) {
			expenseCategory.add(CategoryCodeToName.Categories.get(i));
		}
		String expenseLabel[] = expenseCategory.toArray(new String[expenseCategory.size()]);

		// 支出のカテゴリ毎の合計を設定
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(loginUser.getName(),
				"2020-12");
		List<BigDecimal> expenseAmmount = new ArrayList<BigDecimal>();
		for (int i = 0; i < expenseByCategory.size() - 1; i++) {
			expenseAmmount.add(expenseByCategory.get(i).getSum());
		}
		BigDecimal expenseData[] = expenseAmmount.toArray(new BigDecimal[expenseAmmount.size()]);

		// 収入のカテゴリ一覧を設定
		List<Category> incomeCategories = categoryRepository.findBycategoryCode(99);
		System.out.println(categoryRepository.findBycategoryCode(99));
		List<String> incomeCategoriesStr = new ArrayList<String>();
		for (int i = 0; i < incomeCategories.size(); i++) {
			incomeCategoriesStr.add(incomeCategories.get(i).getSubcategoryName());
		}
		String incomeLabel[] = incomeCategoriesStr.toArray(new String[incomeCategoriesStr.size()]);

		// 収入のカテゴリ毎の合計を設定
		List<SummaryByCategory> incomeByCategory = moneyRecordRepository.findSubcategorySummaries(loginUser.getName(),
				"2020-12", 99);
		List<BigDecimal> incomeAmmount = new ArrayList<BigDecimal>();
		for (int i = 0; i < incomeByCategory.size(); i++) {
			incomeAmmount.add(incomeByCategory.get(i).getSum());
		}
		BigDecimal incomeData[] = incomeAmmount.toArray(new BigDecimal[incomeAmmount.size()]);
		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		model.addAttribute("incomeLabel", incomeLabel);
		model.addAttribute("incomeData", incomeData);

		return "main";
	}

	@GetMapping("/register")
	public String register(@ModelAttribute("user") SiteUser user) {
		return "register";
	}

	@PostMapping("/register")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result) {
		// @Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認

		// 同名ユーザー、メールアドレスのアカウントが存在していないか確認
		if (userRepository.existsByUsername(user.getUsername()) == true
				| userRepository.existsByEmail(user.getEmail()) == true) {
			return "register";
		}
		// デフォルトのニックネームとしてユーザーIDを代入
		user.setUserNickname(user.getUsername());
		if (result.hasErrors()) {
			System.out.println(result);

			return "register";
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		if (user.isAdmin()) {
			user.setRole(Role.ADMIN.name());
		} else {
			user.setRole(Role.USER.name());
		}

		// 現在日時を取得、登録日時にセット
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setCreatedAt(ldt);
		userRepository.save(user);

		return "redirect:/login?register";
	}
}