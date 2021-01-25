package com.example.demo.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.util.CategoryCodeToName;
import com.example.demo.model.Category;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
import com.example.demo.model.beans.SummaryByCategory;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.LikeRepository;
import com.example.demo.dao.MoneyRecordDaoImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class HomeController {

	// DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private MoneyRecordDaoImpl mrDao;
	private final CategoryRepository categoryRepository;
	private final PostRepository postRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final LikeRepository likeRepository;

	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void init() {
		mrDao = new MoneyRecordDaoImpl(em);
	}


	// テスト
	@GetMapping("/upload")
	public String test(Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));

		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());
		// 現在の月を取得
		LocalDate now = LocalDate.now();
		String strNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String month = strNow.substring(0, 7);

		// 予算の取得
		BigDecimal budget = currentUser.getBudget();
		BigDecimal totalAmmount = mrDao.sumMonthExpense(currentUser.getUsername(), month);
		BigDecimal percent = totalAmmount.divide(budget, 2, RoundingMode.HALF_UP);
		BigDecimal a = new BigDecimal(100);
		percent = percent.multiply(a);
		BigDecimal data[] = {percent};
		String totalLabel = "月";

		model.addAttribute("total", data);
		model.addAttribute("totalLabel", totalLabel);

		return "test";
	}

	@GetMapping("/")
	// Authentication・・・認証済みのユーザー情報を取得
	public String loginProcess(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser, Model model) {
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());
		model.addAttribute("user", currentUser);
		model.addAttribute("subcategories", categoryRepository.findAll());
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);

		// 現在の月を取得
		LocalDate now = LocalDate.now();
		String strNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String month = strNow.substring(0, 7);

		// 支出のカテゴリ一覧を設定
		List<String> expenseCategory = new ArrayList<String>();
		for (int i = 1; i < CategoryCodeToName.Categories.size(); i++) {
			expenseCategory.add(CategoryCodeToName.Categories.get(i));
		}
		String expenseLabel[] = expenseCategory.toArray(new String[expenseCategory.size()]);

		// 支出のカテゴリ毎の合計を設定
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(loginUser.getName(),
				month);
		List<BigDecimal> expenseAmmount = new ArrayList<BigDecimal>();
		for (int i = 0; i < expenseByCategory.size() - 1; i++) {
			expenseAmmount.add(expenseByCategory.get(i).getSum());
		}
		BigDecimal expenseData[] = expenseAmmount.toArray(new BigDecimal[expenseAmmount.size()]);
		
		List<BigDecimal> checknullList = new ArrayList<BigDecimal>();
		for(int i = 0; i < 15; i++) {
			checknullList.add(BigDecimal.valueOf(0));
		}
		BigDecimal checknull[] = checknullList.toArray(new BigDecimal[checknullList.size()]);

		//支出合計を算出
		BigDecimal totalAmmount = new BigDecimal(0.0);
		for(int i = 0; i < expenseByCategory.size() ; i++) {
			totalAmmount = totalAmmount.add(expenseByCategory.get(i).getSum());
		}
		
		//予算－支出を計算
		BigDecimal balance = currentUser.getBudget().subtract(totalAmmount);

		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);

		model.addAttribute("checknull", checknull);
		model.addAttribute("totalAmmount", totalAmmount);
		model.addAttribute("balance", balance);

		//model.addAttribute("total", percent);
		//model.addAttribute("totalLabel", totalLabel);

		return "main";
	}
	
	//ユーザー詳細画面へ遷移
	@GetMapping("/userdetail/{username}")
	public String userDetail(@PathVariable("username") String username, Authentication loginUser, Model model) {
		Map<Integer, BigInteger> result = postRepository.findCommentCount();
		model.addAttribute("commentCount", result);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("thisUser", userRepository.findByUsername(username));
		model.addAttribute("posts", postRepository.findByUsername(username));
		
		return "userdetail";
	}
	
	//マイページへ遷移
	@GetMapping("/mypage")
	public String mypage(Authentication loginUser, Model model) {
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();
		Map<Integer, BigInteger> result = postRepository.findCommentCount();
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("commentCount", result);
		model.addAttribute("posts", postRepository.findByUsername(loginUser.getName()));
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		
		return "userdetail";
	}

	// ユーザー設定変更画面へ遷移
	@GetMapping("/setting")
	public String setting(Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		return "setting";
	}

	// ユーザー設定変更を実行
	@PostMapping("/setting")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result,
			Authentication loginUser, RedirectAttributes redirectAttributes) {
		// 同名ユーザー、メールアドレスのアカウントが存在していないか確認
		SiteUser emailCheck = userRepository.findByUsername(loginUser.getName());
		if (user.getEmail().equals(emailCheck.getEmail()) && userRepository.countByEmail(user.getEmail()) > 1) {
			return "redirect:/setting?setting";
		}
		if (user.getEmail().equals(emailCheck.getEmail()) == false
				&& userRepository.countByEmail(user.getEmail()) == 1) {
			return "redirect:/setting?setting";
		}

		// @Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		if (result.hasErrors()) {
			return "main";
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setUpdatedAt(ldt);
		userRepository.save(user);
		
		redirectAttributes.addFlashAttribute("flashMsg", "設定変更しました");

		return "redirect:/?setting";
	}
	
	// パスワード変更画面へ遷移
		@GetMapping("/passwordSetting")
		public String passwordSetting(Authentication loginUser, Model model) {
			model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
			return "passwordSetting";
		}
		
		// パスワード変更を実行
		@PostMapping("/passwordSetting")
		public String ChangePass(@Validated @ModelAttribute("user") SiteUser user, BindingResult result,
				Authentication loginUser, RedirectAttributes redirectAttributes) {
			// 同名ユーザー、メールアドレスのアカウントが存在していないか確認
			SiteUser emailCheck = userRepository.findByUsername(loginUser.getName());
			if (user.getEmail().equals(emailCheck.getEmail()) && userRepository.countByEmail(user.getEmail()) > 1) {
				return "redirect:/setting?setting";
			}
			if (user.getEmail().equals(emailCheck.getEmail()) == false
					&& userRepository.countByEmail(user.getEmail()) == 1) {
				return "redirect:/setting?setting";
			}

			// @Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
			if (result.hasErrors()) {
				return "main";
			}
			LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
			user.setUpdatedAt(ldt);
			userRepository.save(user);
			
			redirectAttributes.addFlashAttribute("flashMsg", "設定変更しました");

			return "redirect:/?setting";
		}

	// ユーザーを削除
	@Transactional
	@GetMapping("/deleteUser")
	public String deleteUser(Authentication loginUser) {
		userRepository.deleteByUsername(loginUser.getName());

		return "redirect:/logout?setting";
	}

	
}