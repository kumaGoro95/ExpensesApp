package com.example.demo.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.example.demo.util.CategoryCodeToIcon;
import com.example.demo.util.CategoryCodeToName;
import com.example.demo.model.Category;
import com.example.demo.model.Like;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
import com.example.demo.model.beans.MoneyRecordList;
import com.example.demo.model.beans.SummaryByCategory;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.service.MoneyRecordService;
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
	private final MoneyRecordService mrService;
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

		// カテゴリ取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;
		model.addAttribute("categories", categories);

		// カテゴリアイコン取得
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;
		model.addAttribute("categoriesToIcon", categoriesToIcon);

		// 現在の月を取得
		LocalDate now = LocalDate.now();
		String strNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String currentMonth = strNow.substring(0, 7);
		String year = currentMonth.substring(0, 4);
		String month = currentMonth.substring(5, 7);

		// カテゴリ毎の合計を取得
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(loginUser.getName(),
				currentMonth);
		// 収入分を削除
		expenseByCategory.remove(expenseByCategory.size() - 1);
		// 月の合計額を算出
		BigDecimal totalAmmount = new BigDecimal(0.0);
		for (int i = 0; i < expenseByCategory.size(); i++) {
			totalAmmount = totalAmmount.add(expenseByCategory.get(i).getSum());
		}
		// カテゴリ÷全体支出の割合を算出
		Map<Integer, BigDecimal> percentages = new HashMap<Integer, BigDecimal>();
		for (int i = 0; i < expenseByCategory.size(); i++) {
			BigDecimal number = BigDecimal.valueOf(100);
			BigDecimal result = expenseByCategory.get(i).getSum().divide(totalAmmount, 1, RoundingMode.DOWN)
					.multiply(number);
			percentages.put(i + 1, result);
		}

		model.addAttribute("percentages", percentages);
		model.addAttribute("totalsByCategory", expenseByCategory);

		return "test";
	}

	@GetMapping("/")
	// Authentication・・・認証済みのユーザー情報を取得
	public String loginProcess(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser,
			Model model) {
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());

		// カテゴリ一覧を取得
		Map<Integer, String> categories = CategoryCodeToName.Categories;

		// 現在の月を取得
		LocalDate now = LocalDate.now();
		String strNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String currentMonth = strNow.substring(0, 7);
		String year = currentMonth.substring(0, 4);
		String month = currentMonth.substring(5, 7);

		/* 円グラフ用 */

		// 収支別円グラフパラメータ
		String expenseLabel[] = mrService.getExpenseLabel();
		BigDecimal expenseData[] = mrService.getExpenseData(loginUser.getName(), currentMonth);

		// 円グラフに表示するデータがあるか確認
		List<BigDecimal> checknullList = new ArrayList<BigDecimal>();
		for (int i = 0; i < 15; i++) {
			checknullList.add(BigDecimal.valueOf(0));
		}
		BigDecimal checknull[] = checknullList.toArray(new BigDecimal[checknullList.size()]);

		/* 最近の履歴表示用 */
		List<MoneyRecordList> recordsLimit10 = moneyRecordRepository.findMoneyRecordListLimit10(loginUser.getName());
		Map<Integer, String> categoriesToIcon = CategoryCodeToIcon.CategoriesToIcon;

		/* 予算-収入=残金用 */

		// 支出合計を算出
		List<SummaryByCategory> expenseByCategory = moneyRecordRepository.findCategorySummaries(loginUser.getName(),
				currentMonth);
		BigDecimal totalAmmount = new BigDecimal(0.0);
		for (int i = 0; i < expenseByCategory.size(); i++) {
			totalAmmount = totalAmmount.add(expenseByCategory.get(i).getSum());
		}

		// 予算を取得
		BigDecimal budget = currentUser.getBudget().setScale(0, RoundingMode.DOWN);
		// 予算－支出を計算
		BigDecimal balance = budget.subtract(totalAmmount);
		
		//履歴データがあるかチェック用
		List<MoneyRecordList> nullRecord = new ArrayList<MoneyRecordList>();
		
		/* model */

		model.addAttribute("user", currentUser);
		model.addAttribute("subcategories", categoryRepository.findAll());
		model.addAttribute("categories", categories);
		// 円グラフ
		model.addAttribute("expenseLabel", expenseLabel);
		model.addAttribute("expenseData", expenseData);
		// 〇年〇月の状況 用
		model.addAttribute("year", year);
		model.addAttribute("month", month);
		// 予算-収入＝残金
		model.addAttribute("checknull", checknull);
		model.addAttribute("budget", budget);
		model.addAttribute("totalAmmount", totalAmmount);
		model.addAttribute("balance", balance);
		// 履歴10件
		model.addAttribute("recordsLimit10", recordsLimit10);
		model.addAttribute("categoriesToIcon", categoriesToIcon);
		model.addAttribute("nullRecord", nullRecord);
		
	

		return "main";
	}

	// ユーザー詳細画面へ遷移
	@GetMapping("/userdetail/{username}")
	public String userDetail(@PathVariable("username") String username, Authentication loginUser, Model model) {
		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();
		model.addAttribute("commentCount", commentCount);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("selectedUser", userRepository.findByUsername(username));
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("usersPosts", postRepository.findAllPostsByUsername(username));
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));

		return "userdetail";
	}

	// マイページへ遷移
	@GetMapping("/mypage")
	public String mypage(Authentication loginUser, Model model) {
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();
		Map<Integer, BigInteger> result = postRepository.findCommentCount();
		model.addAttribute("selectedUser", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("commentCount", result);
		model.addAttribute("usersPosts", postRepository.findAllPostsByUsername(loginUser.getName()));
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
		moneyRecordRepository.deleteByUsername(loginUser.getName());
		userRepository.deleteByUsername(loginUser.getName());

		return "redirect:/logout?setting";
	}

}