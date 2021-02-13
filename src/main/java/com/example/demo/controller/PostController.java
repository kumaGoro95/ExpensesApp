package com.example.demo.controller;

import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.model.Post;
import com.example.demo.model.Like;
import com.example.demo.model.PostComment;
import com.example.demo.model.beans.PostByNickname;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.service.PostService;
import com.example.demo.util.CategoryCodeToName;
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
	@GetMapping("/postmain")
	public String goToPost(@ModelAttribute("posts") Post post, Authentication loginUser, Model model) {
		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();

		// 検索用
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

		return "postmain";
	}

	@PostMapping("/search")
	public String post(@ModelAttribute("swords") SearchingWords swords, Authentication loginUser, Model model) {

		List<PostByNickname> list = pService.getWords(swords.getWord());
		model.addAttribute("posts", list);

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;
		Map<Integer, String> postCategoriesToIcon = PostCategoryCodeToIcon.PostCategoriesToIcon;

		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		Map<Integer, BigInteger> commentCount = postRepository.findCommentCount();
		Map<Integer, BigInteger> likeCount = likeRepository.findLikeCount();
		model.addAttribute("commentCount", commentCount);
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		model.addAttribute("postCategories", postCategories);
		model.addAttribute("postCategoriesToIcon", postCategoriesToIcon);

		return "postmain";
	}

	@GetMapping("/showPost/{category}")
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
		model.addAttribute("posts", postRepository.findPostByCategory(category));

		// 当該カテゴリの投稿が存在するか・・・falseで「投稿がありません」メッセージを表示
		model.addAttribute("checknull", postRepository.existsByPostCategory(category));
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("myLikes", likeRepository.findMyLikes(loginUser.getName()));
		model.addAttribute("swords", swords);
		model.addAttribute("postCategories", postCategories);
		model.addAttribute("postCategoriesToIcon", postCategoriesToIcon);

		return "postmain";
	}

	// 投稿画面へ遷移
	@GetMapping("/post")
	public String post(@ModelAttribute("post") Post post, Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));

		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;

		model.addAttribute("postCategories", postCategories);

		return "post";
	}

	// 投稿実行
	@PostMapping("/post")
	public String post(@Validated @ModelAttribute("post") Post post, BindingResult result, Authentication loginUser) {
		if (result.hasErrors()) {
			System.out.println(result);
			System.out.println(post.getPostBody());
			return "redirect:/post?post";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		post.setCreatedAt(ldt);
		postRepository.save(post);

		return "redirect:/postmain?recordPost";
	}

	// 投稿内容詳細画面へ遷移
	@RequestMapping("/post/{postId}")
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

		return "postdetail";
	}

	// コメント投稿を実行
	@PostMapping("/postComment")
	public String postComment(@Validated @ModelAttribute("comment") PostComment comment, BindingResult result,
			Authentication loginUser, UriComponentsBuilder builder) {
		
		//リダイレクト先を指定
		URI location = builder.path("/post/" + comment.getPostId()).build().toUri();
		
		if (result.hasErrors()) {
			System.out.println(result);
			System.out.println(comment.getCommentBody());
			
			return "redirect:" + location.toString();
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		comment.setCreatedAt(ldt);
		commentRepository.save(comment);

		return "redirect:" + location.toString();

	}

	// いいね実行
	@RequestMapping("/like/{postId}")
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

		URI location = builder.path("/post/" + postId).build().toUri();
	    return "redirect:" + location.toString();
	}

	// コメント削除
	@Transactional
	@GetMapping("/deleteComment/{commentId}")
	public String deleteComment(@PathVariable("commentId") int commentId, Model model) {
		commentRepository.deleteByCommentId(commentId);

		return "redirect:/postmain?postdetail";
	}

	// コメント編集画面へ遷移
	@GetMapping("/editComment/{commentId}")
	public String editComment(@PathVariable("commentId") int commentId, Authentication loginUser, Model model) {
		model.addAttribute("comment", commentRepository.findByCommentId(commentId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));

		return "commentEdit";
	}

	// コメント編集を実行
	@PostMapping("/updateComment")
	public String updateComment(@Validated @ModelAttribute("comment") PostComment comment, BindingResult result,
			Authentication loginUser) {
		if (result.hasErrors()) {
			return "main";
		}

		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		comment.setUpdatedAt(ldt);

		commentRepository.save(comment);

		return "redirect:/postmain?commentEdit";
	}

	// 投稿削除
	@Transactional
	@GetMapping("/deletePost/{postId}")
	public String deletePost(@PathVariable("postId") int postId, Model model) {
		postRepository.deleteByPostId(postId);

		return "redirect:/postmain?postdetail";
	}

	// 投稿編集画面へ遷移
	@GetMapping("/editPost/{postId}")
	public String editPost(@PathVariable("postId") int postId, Authentication loginUser, Model model) {
		model.addAttribute("post", postRepository.findByPostId(postId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		// カテゴリ
		Map<Integer, String> postCategories = PostCategoryCodeToName.PostCategories;

		model.addAttribute("postCategories", postCategories);

		return "post";
	}

	// 投稿編集を実行
	@PostMapping("/updatePost")
	public String updatePost(@Validated @ModelAttribute("post") Post post, BindingResult result,
			Authentication loginUser) {
		if (result.hasErrors()) {
			return "main";
		}

		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		post.setUpdatedAt(ldt);

		postRepository.save(post);

		return "redirect:/postmain?postEdit";
	}

}