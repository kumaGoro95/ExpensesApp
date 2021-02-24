package com.example.demo.controller;

import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.core.Conventions;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.model.Post;
import com.example.demo.model.Like;
import com.example.demo.model.PostComment;
import com.example.demo.model.beans.PostByNickname;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.service.PostService;
import com.example.demo.util.PostCategoryCodeToIcon;
import com.example.demo.util.PostCategoryCodeToName;
import com.example.demo.repository.PostCommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.LikeRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class PostController {

	// DI
	private final SiteUserRepository userRepository;
	private final PostRepository postRepository;
	private final PostCommentRepository commentRepository;
	private final LikeRepository likeRepository;
	private final PostService pService;

	// 投稿検索用インナークラス
	private class SearchingWords {
		private String word;

		public SearchingWords(String word) {
			this.word = word;
		}

		public String getWord() {
			return this.word;
		}
	}

	// 投稿系ホーム画面
	@GetMapping("/qanda")
	public String goToPost(@ModelAttribute("posts") Post post, Authentication loginUser, Model model) {
		
		//コメント数、クリップ数
		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();

		// 検索ワード格納用変数
		SearchingWords searchingWords = new SearchingWords(null);

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		// 40文字まで

		model.addAttribute("commentCount", commentCount);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("posts", pService.getAllPosts());
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		model.addAttribute("swords", searchingWords);
		model.addAttribute("postCategories", postCategories);
		model.addAttribute("postCategoriesToIcon", postCategoriesToIcon);

		return "qanda";
	}

	@PostMapping("/qanda/search")
	public String post(@ModelAttribute("swords") SearchingWords swords, Authentication loginUser, Model model) {
		
		//コメント数、クリップ数
		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();

		List<PostByNickname> list = pService.getWords(swords.getWord());
		model.addAttribute("posts", list);

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("commentCount", commentCount);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		model.addAttribute("postCategories", postCategories);
		model.addAttribute("postCategoriesToIcon", postCategoriesToIcon);

		return "qanda";
	}

	@GetMapping("qanda/category/{category}")
	public String showPostByCategory(@PathVariable("category") int category, @ModelAttribute("posts") Post post,
			Authentication loginUser, Model model) {
		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();

		// 検索用
		SearchingWords swords = new SearchingWords(null);

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		model.addAttribute("commentCount", commentCount);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("posts", pService.getPostsByCategory(category));

		// 当該カテゴリの投稿が存在するか・・・falseで「投稿がありません」メッセージを表示
		model.addAttribute("checknull", postRepository.existsByPostCategory(category));
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		model.addAttribute("swords", swords);
		model.addAttribute("postCategories", postCategories);
		model.addAttribute("postCategoriesToIcon", postCategoriesToIcon);

		return "qanda";
	}

	// 投稿画面へ遷移
	@GetMapping("/qanda/post")
	public String post(@ModelAttribute("post") Post post, Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		
		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;

		model.addAttribute("postCategories", postCategories);

		return "qanda-post";
	}

	// 投稿実行
	@PostMapping("/qanda/post")
	public String post(@Validated @ModelAttribute("post") Post post, BindingResult result, Authentication loginUser, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
			
			// カテゴリ
			Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;

			model.addAttribute("postCategories", postCategories);

			return "qanda-post";

		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		post.setCreatedAt(ldt);
		postRepository.save(post);
		
		redirectAttributes.addFlashAttribute("flashMsg", "投稿しました");

		return "redirect:/qanda?qanda-post";
	}

	// 投稿内容詳細画面へ遷移
	@RequestMapping("/qanda/{postId}")
	public String showPost(@PathVariable("postId") int postId, @ModelAttribute("comment") PostComment comment,
			Authentication loginUser, Model model) {
		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();

		// 検索用
		SearchingWords searchingWords = new SearchingWords(null);

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		List<PostByNickname> list = postRepository.findPostByPostId(postId);

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("post", list.get(0));
		model.addAttribute("comments", commentRepository.findCommentsByPostId(postId));
		model.addAttribute("commentCount", commentCount);
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		model.addAttribute("swords", searchingWords);
		model.addAttribute("postCategories", postCategories);
		model.addAttribute("postCategoriesToIcon", postCategoriesToIcon);

		return "qanda-postdetail";
	}

	// コメント投稿を実行
	@PostMapping("qanda/comment")
	public String postComment(@Validated @ModelAttribute("comment") PostComment comment, BindingResult result,
			Authentication loginUser, UriComponentsBuilder builder, RedirectAttributes redirectAttributes) {

		// リダイレクト先を指定
		URI location = builder.path("/qanda/" + comment.getPostId()).build().toUri();

		if (result.hasErrors()) {
			System.out.println(result);

			return "redirect:" + location.toString();
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		comment.setCreatedAt(ldt);
		commentRepository.save(comment);
		
		redirectAttributes.addFlashAttribute("flashMsg", "投稿しました");

		return "redirect:" + location.toString();

	}

	// いいね実行
	@RequestMapping("qanda/like/{postId}")
	@Transactional
	public String Like(@PathVariable("postId") int postId, @ModelAttribute("like") Like like, Authentication loginUser,
			Model model, UriComponentsBuilder builder) {
		
		
		if (likeRepository.existsByUsernameAndPostId(loginUser.getName(), postId) == true) {
			likeRepository.deleteByUsernameAndPostId(loginUser.getName(), postId);
		} else {
			like.setPostId(postId);
			like.setUsername(loginUser.getName());
			LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
			like.setCreatedAt(ldt);
			likeRepository.save(like);
		}

		URI location = builder.path("/qanda/" + postId).build().toUri();
		return "redirect:" + location.toString();
	}

	// コメント削除
	@Transactional
	@GetMapping("qanda/comment/{commentId}/delete")
	public String deleteComment(@PathVariable("commentId") int commentId, Model model) {
		commentRepository.deleteByCommentId(commentId);

		return "redirect:/qanda?qanda-postdetail";
	}

	// コメント編集画面へ遷移
	@GetMapping("/qanda/comment/{commentId}/edit")
	public String editComment(@PathVariable("commentId") int commentId, Authentication loginUser, Model model) {
		
		model.addAttribute("comment", commentRepository.findByCommentId(commentId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));

		return "qanda-comment-edit";
	}

	// コメント編集を実行
	@PostMapping("/qanda/updateComment")
	public String updateComment(@Validated @ModelAttribute("comment") PostComment comment, BindingResult result,
			Authentication loginUser, UriComponentsBuilder builder, RedirectAttributes redirectAttributes) {

		// リダイレクト先を指定
		URI location = builder.path("/qanda/" + comment.getPostId()).build().toUri();

		if (result.hasErrors()) {
			return "redirect:" + location.toString();
		}

		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		comment.setUpdatedAt(ldt);

		commentRepository.save(comment);
		
		redirectAttributes.addFlashAttribute("flashMsg", "投稿しました");

		return "redirect:" + location.toString();
	}

	// 投稿削除
	@Transactional
	@GetMapping("/qanda/{postId}/delete")
	public String deletePost(@PathVariable("postId") int postId, Model model) {
		postRepository.deleteByPostId(postId);

		return "redirect:/qanda?qanda-postdetail";
	}

	// 投稿編集画面へ遷移
	@GetMapping("/qanda/{postId}/edit")
	public String editPost(@PathVariable("postId") int postId, Authentication loginUser, Model model) {
		model.addAttribute("post", postRepository.findByPostId(postId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;

		model.addAttribute("postCategories", postCategories);

		return "qanda-post-edit";
	}

	// 投稿編集を実行
	@PostMapping("/qanda/updatePost")
	public String updatePost(@Validated @ModelAttribute("post") Post post, BindingResult result,
			Authentication loginUser, UriComponentsBuilder builder, RedirectAttributes redirectAttributes) {

		// エラー時のリダイレクト先を指定
		URI locationForErrors = builder.path("/qanda/" + post.getPostId() + "/edit").build().toUri();

		if (result.hasErrors()) {
			System.out.println(result);
			redirectAttributes.addFlashAttribute("post", post);
	        // テンプレート内で#fieldsやth:errorsでバリデーションエラーを参照するには、
	        // org.springframework.validation.BindingResult.{クラス名 (Camel Case)}というキー名で設定する必要がある。
	        redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + Conventions.getVariableName(post), result);
	        System.out.println(BindingResult.MODEL_KEY_PREFIX);
	        System.out.println(Conventions.getVariableName(post));

			return "redirect:" + locationForErrors.toString();

		}
		
		//投稿を更新
		pService.updatePost(post);
		
		redirectAttributes.addFlashAttribute("flashMsg", "更新しました");
		
		//リダイレクト先を指定
		URI location = builder.replacePath("/qanda/" + post.getPostId()).build().toUri();
		
		return "redirect:" + location.toString();
	}

}