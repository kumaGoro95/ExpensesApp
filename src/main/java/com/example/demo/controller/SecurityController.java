package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
import com.example.demo.model.SiteUser;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.util.Role;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class SecurityController {

	// DI
	private final SiteUserRepository userRepository;
	private MoneyRecordDaoImpl mrDao;
	private final BCryptPasswordEncoder passwordEncoder;

	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void init() {
		mrDao = new MoneyRecordDaoImpl(em);
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/")
	// Authentication・・・認証済みのユーザー情報を取得
	public String loginProcess(Authentication loginUser, Model model) {
		model.addAttribute("records", mrDao.findByUsername(loginUser.getName()));
		//model.addAttribute("records", moneyRecordRepository.findByUsernameOrderByRecordDate(loginUser.getName()));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("temporaryRecords", mrDao.find(loginUser.getName(), "2020-12"));

		return "main";
	}

	@GetMapping("/register")
	public String register(@ModelAttribute("user") SiteUser user) {
		return "register";
	}

	@PostMapping("/register")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result) {
		// @Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		user.setUserNickname(user.getUsername());
		if(result.hasErrors()) { 
			System.out.println(result);
      
			return "register";
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		if (user.isAdmin()) {
			user.setRole(Role.ADMIN.name());
		} else {
			user.setRole(Role.USER.name());
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setCreatedAt(ldt);
		userRepository.save(user);

		return "redirect:/login?register";
	}
}