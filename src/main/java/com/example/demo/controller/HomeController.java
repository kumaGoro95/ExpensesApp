package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
import com.example.demo.model.CategoryName;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
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
	
	//ホーム画面へ遷移
	@GetMapping("/main")
	public String main(@ModelAttribute MoneyRecord moneyRecord, Authentication loginUser, Model model) {
		SiteUser user = userRepository.findByUsername(loginUser.getName());
		model.addAttribute("user", user);
		model.addAttribute("records", moneyRecordRepository.findByUsernameOrderByRecordDate(loginUser.getName()));
		model.addAttribute("temporaryRecords", mrDao.find(loginUser.getName(), "2020-12"));
		model.addAttribute("total", mrDao.sumMonthExpense(loginUser.getName(), "2020-12"));
		model.addAttribute("monthExpenses", moneyRecordRepository.findMonthSummaries(loginUser.getName()));
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
		List<CategoryName> list = cDao.getCategory();
		model.addAttribute("categories", list);

		for (int i = 0; i < list.size(); i++) {
			CategoryName a = list.get(i);
			System.out.println(a.getCategoryName());
			System.out.println(a.getNumberOfSubcategory());
		}
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

		// ZonedDateTime zonedDateTime = ZonedDateTime.now();
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd
		// HH:mm:ss.SSSxxxxx VV");
		// String s = zonedDateTime.format(formatter);

		moneyRecord.setCreatedAt(ldt);
		moneyRecordRepository.save(moneyRecord);

		return "redirect:/main?recordPost";
	}
	
	//出入金記録詳細画面へ遷移
	@RequestMapping("/record/{recordId}")
	public String showRecord(@PathVariable("recordId") Long recordId, Model model) {
		model.addAttribute("record", moneyRecordRepository.findByRecordId(recordId));

		return "record";
	}
	
	//出入金記録を削除
	@Transactional
	@GetMapping("/deleteRecord/{recordId}")
	public String deleteRecord(@PathVariable("recordId") Long recordId, Model model) {
		moneyRecordRepository.deleteById(recordId);

		return "redirect:/main?record";
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