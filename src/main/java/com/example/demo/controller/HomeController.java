package com.example.demo.controller;


import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.MoneyRecordRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class HomeController {
	
	//DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/main")
	public String main(Authentication loginUser, Model model){
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		return "main";
	}
	
	@GetMapping("/setting")
	public String setting(Authentication loginUser, Model model){
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		return "setting";
	}
	
	@PostMapping("/setting")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result) {
		//@Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		if(result.hasErrors()) { 
			return "main";
		}
		//LocalDate a = LocalDate.parse(user.getDateOfBirth());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
		
		return "redirect:/main?setting";
	}
	
	@GetMapping("/post")
	public String post(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser, Model model){
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		return "post";
	}
	
	@PostMapping("/post")
	public String process(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result, Authentication loginUser) {
		//@Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		if(result.hasErrors()) { 
			return "post";
		}
		SiteUser user = userRepository.findByUsername(loginUser.getName());
		moneyRecord.setUserId(user.getUserId());
		LocalDateTime ldt = LocalDateTime.now();
		moneyRecord.setCreatedAt(ldt);
		moneyRecordRepository.save(moneyRecord);
		
		return "redirect:/main?post";
	}
}
