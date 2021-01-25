package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dao.MoneyRecordDaoImpl;
import com.example.demo.model.Category;
import com.example.demo.model.SiteUser;
import com.example.demo.model.beans.SummaryByCategory;
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
	public String login(@ModelAttribute("user") SiteUser user, Authentication loginUser) {
		if (loginUser != null) {
			return "redirect:/?login";
		}
		return "login";
	}

	@GetMapping("/register")
	public String register(@ModelAttribute("user") SiteUser user) {
		return "register";
	}

	@PostMapping("/register")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result,
			RedirectAttributes redirectAttributes) {
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

		redirectAttributes.addFlashAttribute("flashMsg", "登録しました");

		return "redirect:/?register";
	}
}