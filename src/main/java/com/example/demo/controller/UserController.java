package com.example.demo.controller;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
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

import com.example.demo.util.PostCategoryCodeToIcon;
import com.example.demo.util.PostCategoryCodeToName;
import com.example.demo.model.SiteUser;
import com.example.demo.model.beans.FileUploadForm;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.service.FileUploadService;
import com.example.demo.service.PostService;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.LikeRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

	// DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	private final FileUploadService iResizer;
	private final PostService pService;
	private final BCryptPasswordEncoder passwordEncoder;


	// ユーザー詳細画面へ遷移
	@GetMapping("/user/{username}")
	public String userDetail(@PathVariable("username") String username, Authentication loginUser, Model model) {

		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		model.addAttribute("commentCount", commentCount);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("selectedUser", userRepository.findByUsername(username));
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("usersPosts", pService.getSpecificPosts(username));
		model.addAttribute("likedPosts", pService.getLikedPosts(username));
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		model.addAttribute("postCategories", postCategories);
		model.addAttribute("postCategoriesToIcon", postCategoriesToIcon);

		return "userdetail";
	}

	// マイページへ遷移
	@GetMapping("/mypage")
	public String mypage(Authentication loginUser, Model model) {
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();
		Map<Integer, BigInteger> result = postRepository.findCommentCount();

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		model.addAttribute("selectedUser", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("commentCount", result);
		model.addAttribute("usersPosts", pService.getSpecificPosts(loginUser.getName()));
		model.addAttribute("likedPosts", pService.getLikedPosts(loginUser.getName()));
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		model.addAttribute("postCategories", postCategories);
		model.addAttribute("postCategoriesToIcon", postCategoriesToIcon);

		return "userdetail";
	}

	// ユーザー設定変更画面へ遷移
	@GetMapping("/setting")
	public String setting(@ModelAttribute("file") FileUploadForm file, Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		return "setting";
	}

	// ユーザー設定変更を実行
	@Transactional
	@PostMapping("/setting")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result,
			@ModelAttribute("file") FileUploadForm file, HttpServletResponse response, Authentication loginUser,
			RedirectAttributes redirectAttributes) {
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
			System.out.println(result);
			return "redirect:setting?setting";
		}
		// 画像アップロード・リサイズ
		String str = iResizer.uploadImage(response, file.getUploadedFile());

		user.setIcon(str);
		user.setPassword(user.getPassword());
		System.out.println(user.getPassword());
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setUpdatedAt(ldt);
		userRepository.save(user);

		redirectAttributes.addFlashAttribute("flashMsg", "プロフィールを変更しました");

		return "redirect:/money-record?setting";
	}

	// パスワード変更画面へ遷移
	@GetMapping("/setting/pass")
	public String passwordSetting(Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		return "setting-pass";
	}

	// パスワード変更を実行
	@Transactional
	@PostMapping("/setting/pass")
	public String ChangePass(@Validated @ModelAttribute("user") SiteUser user, BindingResult result,
			Authentication loginUser, RedirectAttributes redirectAttributes) {

		// @Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		if (result.hasErrors()) {
			return "setting";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setUpdatedAt(ldt);
		
		System.out.println("変更前:" + user.getPassword());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		System.out.println("変更後:" + user.getPassword());
		
		userRepository.save(user);

		redirectAttributes.addFlashAttribute("flashMsg", "パスワードを変更しました");

		return "redirect:/setting?setting-pass";
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