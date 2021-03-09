package com.example.demo.controller;

import java.math.BigInteger;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.security.core.Authentication;
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
import com.example.demo.service.UserService;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.LikeRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

	// DI
	private final SiteUserRepository userRepository;
	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	private final FileUploadService iResizer;
	private final PostService pService;
	private final UserService uService;

	// ユーザー詳細画面へ遷移
	@GetMapping("/user/{username}")
	public String userDetail(@PathVariable("username") String username, Authentication loginUser, Model model) {
		SiteUser selectedUser = userRepository.findByUsername(username);

		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		// アイコン表示用のURL
		String iconUrl = "https://piggy-box-s3.s3-ap-northeast-1.amazonaws.com/icons/";
		iconUrl = iconUrl + selectedUser.getIcon();

		model.addAttribute("iconUrl", iconUrl);
		model.addAttribute("commentCount", commentCount);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("selectedUser", selectedUser);
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
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		// アイコン表示用のURL
		String iconUrl = "https://piggy-box-s3.s3-ap-northeast-1.amazonaws.com/icons/";
		iconUrl = iconUrl + currentUser.getIcon();

		model.addAttribute("iconUrl", iconUrl);
		model.addAttribute("selectedUser", currentUser);
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
		SiteUser currentUser = userRepository.findByUsername(loginUser.getName());
		
		// アイコン表示用のURL
		String iconUrl = "https://piggy-box-s3.s3-ap-northeast-1.amazonaws.com/icons/";
		iconUrl = iconUrl + currentUser.getIcon();

		model.addAttribute("iconUrl", iconUrl);
		model.addAttribute("user", currentUser);
		return "setting";
	}

	// ユーザー設定変更を実行
	@Transactional
	@PostMapping("/setting")
	public String process(@Validated @ModelAttribute("user") SiteUser user, BindingResult result,
			@ModelAttribute("file") FileUploadForm file, HttpServletResponse response, Authentication loginUser,
			RedirectAttributes redirectAttributes) {

		// もし同名ユーザー・同メールアドレスユーザーが存在すれば、リダイレクトする
		if (uService.existsSameAccount(loginUser.getName(), user)) {
			return "redirect:/setting?setting";
		}
		// @Validatedで入力値チェック→BindingResultに結果が入る→result.hasErrors()でエラーがあるか確認
		if (result.hasErrors()) {
			System.out.println(result);
			return "redirect:/setting?setting";
		}

		/* ユーザープロフィールを更新する */

		// アイコン画像画像アップロード・リサイズ
		String iconStr = iResizer.uploadImage(response, file.getUploadedFile());
		// 更新処理
		uService.updateSetting(user, iconStr);

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

		uService.changePassword(user);

		redirectAttributes.addFlashAttribute("flashMsg", "パスワードを変更しました");

		return "redirect:/setting?setting-pass";
	}

	// ユーザーを削除
	@Transactional
	@GetMapping("/deleteUser")
	public String deleteUser(Authentication loginUser) {

		uService.deleteUserInfo(loginUser.getName());

		return "redirect:/logout?setting";
	}

}