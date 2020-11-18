package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.util.Role;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class SecurityController {
	
	//DI
	private final SiteUserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/")
	//Authentication・・・認証済みのユーザー情報を取得
	public String showList(Authentication loginUser, Model model) {
		//Thymeleafに値を渡すためにModelに追加
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));		
		return "main";
	}
	
	@GetMapping("/register")
	public String register(@ModelAttribute("user") SiteUser user){
		return "register";
	}
	
	@PostMapping("/register")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result) {
		//@Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		if(result.hasErrors()) { 
			return "register";
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		if(user.isAdmin()) {
			user.setRole(Role.ADMIN.name());
		}else {
			user.setRole(Role.USER.name());
		}
		userRepository.save(user);
		
		return "redirect:/login?register";
	}
}
