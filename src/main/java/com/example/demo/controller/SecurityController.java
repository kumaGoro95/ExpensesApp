package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.SiteUser;
import com.example.demo.service.SecurityService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class SecurityController {

	// DI
	private final SecurityService sService;
	
	@GetMapping("/")
	public String index(@ModelAttribute("user") SiteUser user, Authentication loginUser) {
		if (loginUser != null) {
			return "redirect:/money-record?/";
		}
		return "index";
	}

	@GetMapping("/login")
	public String login(@ModelAttribute("user") SiteUser user, Authentication loginUser) {
		if (loginUser != null) {
			return "redirect:/?login";
		}
		return "login";
	}
	
	@GetMapping("/guest-login")
	public String guestLogin(@ModelAttribute("user") SiteUser user, Authentication loginUser) {
		if (loginUser != null) {
			return "redirect:/?login";
		}
		return "guestlogin";
	}

	@GetMapping("/register")
	public String register(@ModelAttribute("user") SiteUser user) {
		return "register";
	}

	@PostMapping("/register")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result,
			RedirectAttributes redirectAttributes) {
		
		// @Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		// デフォルトのニックネームとしてユーザーIDを代入
		user.setUserNickname(user.getUsername());
		if (result.hasErrors()) {
			System.out.println(result);

			return "register";
		}
		
		boolean register = sService.registUser(user);
		if(register) {
			redirectAttributes.addFlashAttribute("flashMsg", "登録しました");
			return "redirect:/money-record?register";
		}else {
			return "redirect:/register?register";
		}

	}
}