package com.example.demo.controller;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

import com.example.demo.model.MoneyRecord;
import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class HomeController {
	
	//DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private final CategoryRepository categoryRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/main")
	public String main(@ModelAttribute MoneyRecord moneyRecord, Authentication loginUser, Model model){
		SiteUser user = userRepository.findByUsername(loginUser.getName());
		model.addAttribute("user", user);
		model.addAttribute("records", moneyRecordRepository.findByUsernameOrderByRecordDate(loginUser.getName()));
		return "main";
	}
	
	@GetMapping("/setting")
	public String setting(Authentication loginUser, Model model){
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));	
		return "setting";
	}
	
	@PostMapping("/setting")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result, Authentication loginUser) {
		//@Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		if(result.hasErrors()) { 
			return "main";
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setUpdatedAt(ldt);
		userRepository.save(user);
		
		return "redirect:/main?setting";
	}
	
	@Transactional
	@GetMapping("/deleteUser")
	public String deleteUser(Authentication loginUser) {
		userRepository.deleteByUsername(loginUser.getName());
		
		return "login";
	}
	
	@GetMapping("/post")
	public String post(@ModelAttribute("moneyRecord") MoneyRecord moneyRecord, Authentication loginUser, Model model){
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("categories", categoryRepository.findAll());
		return "post";
	}
	
	@PostMapping("/post")
	public String process(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result, Authentication loginUser) {
		if(result.hasErrors()) {
			System.out.println(result);
			System.out.println(moneyRecord.getNote());
			return "redirect:/post?post";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		
		//ZonedDateTime zonedDateTime = ZonedDateTime.now();
		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSSxxxxx VV");
		//String s = zonedDateTime.format(formatter);
		
		moneyRecord.setCreatedAt(ldt);
		moneyRecordRepository.save(moneyRecord);
		
		return "redirect:/main?post";
	}
	
	@RequestMapping("/record/{recordId}")
	public String showRecord(@PathVariable("recordId") Long recordId, Model model){
		model.addAttribute("record", moneyRecordRepository.findByRecordId(recordId));
		
		return "record";
	}
	
	@Transactional
	@GetMapping("/deleteRecord/{recordId}")
	public String deleteRecord(@PathVariable("recordId") Long recordId, Model model) {
		moneyRecordRepository.deleteById(recordId);
		
		return "redirect:/main?record";
	}
	
	@GetMapping("/editRecord/{recordId}")
	public String editRecord(@PathVariable("recordId") Long recordId, Authentication loginUser, Model model){
		model.addAttribute("record", moneyRecordRepository.findByRecordId(recordId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("categories", categoryRepository.findAll());
		
		return "recordEdit";
	}
	
	@PostMapping("/editRecord/{recordId}")
	public String updateRecord(@Validated @ModelAttribute("moneyRecord") MoneyRecord moneyRecord, BindingResult result, Authentication loginUser) {
		if(result.hasErrors()) {
			return "main";
		}

		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		moneyRecord.setUpdatedAt(ldt);

		
		moneyRecordRepository.save(moneyRecord);
		
		return "redirect:/main?editRecord";
	}

}