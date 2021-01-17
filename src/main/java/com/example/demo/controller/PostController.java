package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

import com.example.demo.model.Post;
import com.example.demo.model.Like;
import com.example.demo.model.PostComment;
import com.example.demo.repository.SiteUserRepository;
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

	// 投稿系ホーム画面
	@GetMapping("/postmain")
	public String goToPost(@ModelAttribute("posts") Post post, Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("posts", postRepository.findAll());

		return "postmain";
	}

	// 投稿画面へ遷移
	@GetMapping("/post")
	public String post(@ModelAttribute("post") Post post, Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));

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

		// ZonedDateTime zonedDateTime = ZonedDateTime.now();
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd
		// HH:mm:ss.SSSxxxxx VV");
		// String s = zonedDateTime.format(formatter);

		post.setCreatedAt(ldt);
		postRepository.save(post);

		return "redirect:/postmain?recordPost";
	}

	// 投稿内容詳細画面へ遷移
	@RequestMapping("/post/{postId}")
	public String showPost(@PathVariable("postId") Long postId, @ModelAttribute("comment") PostComment comment,
			Authentication loginUser, Model model) {
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("post", postRepository.findByPostId(postId));
		model.addAttribute("comments", commentRepository.findByPostId(postId));
		model.addAttribute("theNumberOfLikes", likeRepository.countByPostId(postId));
		

		return "postdetail";
	}

	// コメント投稿を実行
	@PostMapping("/postComment")
	public String postComment(@Validated @ModelAttribute("comment") PostComment comment, BindingResult result,
			Authentication loginUser) {
		if (result.hasErrors()) {
			System.out.println(result);
			System.out.println(comment.getCommentBody());
			return "redirect:/post?post";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		// ZonedDateTime zonedDateTime = ZonedDateTime.now();
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd
		// HH:mm:ss.SSSxxxxx VV");
		// String s = zonedDateTime.format(formatter);

		comment.setCreatedAt(ldt);
		commentRepository.save(comment);

		return "redirect:/postmain?postdetail";
	}

	// いいね実行
	@RequestMapping("/like/{postId}")
	public String Like(@PathVariable("postId") Long postId, @ModelAttribute("like") Like like, Authentication loginUser,
			Model model) {
		if(likeRepository.existsByUsernameAndPostId(loginUser.getName(), postId) == true) {
			likeRepository.deleteByUsernameAndPostId(loginUser.getName(), postId);
		}else {
		like.setPostId(postId);
		like.setUsername(loginUser.getName());
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		like.setCreatedAt(ldt);
		likeRepository.save(like);
		}

		return "redirect:/postmain?postdetail";
	}

	// いいね一覧を表示
	@RequestMapping("/likesList/{postId}")
	public String showLikes(@PathVariable("postId") Long postId, @ModelAttribute("likes") Like like,
			Authentication loginUser, Model model) {
		model.addAttribute("likes", likeRepository.findByPostId(postId));

		return "likesDetail";
	}

	// コメント削除
	@Transactional
	@GetMapping("/deleteComment/{commentId}")
	public String deleteComment(@PathVariable("commentId") Long commentId, Model model) {
		commentRepository.deleteByCommentId(commentId);

		return "redirect:/postmain?postdetail";
	}

	// コメント編集画面へ遷移
	@GetMapping("/editComment/{commentId}")
	public String editComment(@PathVariable("commentId") Long commentId, Authentication loginUser, Model model) {
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
	public String deletePost(@PathVariable("postId") Long postId, Model model) {
		postRepository.deleteByPostId(postId);

		return "redirect:/postmain?postdetail";
	}

	// 投稿編集画面へ遷移
	@GetMapping("/editPost/{postId}")
	public String editPost(@PathVariable("postId") Long postId, Authentication loginUser, Model model) {
		model.addAttribute("post", postRepository.findByPostId(postId));
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));

		return "postEdit";
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