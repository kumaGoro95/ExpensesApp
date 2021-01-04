package com.example.demo.controller;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

import com.example.demo.dao.CategoryDaoImpl;
import com.example.demo.model.CategoryName;
import com.example.demo.model.MoneyRecord;
import com.example.demo.model.Post;
import com.example.demo.model.PostComment;
import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.PostCommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.dao.MoneyRecordDaoImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class PostController {
	
	//DI
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private final PostRepository postRepository;
	private final PostCommentRepository commentRepository;
	private MoneyRecordDaoImpl mrDao;
	private CategoryDaoImpl cDao;
	private final CategoryRepository categoryRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	@PersistenceContext
	EntityManager em;
	
	@PostConstruct
	public void init() {
		mrDao = new MoneyRecordDaoImpl(em);
		cDao = new CategoryDaoImpl(em);
	}

	
	@GetMapping("/postmain")
	public String goToPost(@ModelAttribute("posts") Post post, Authentication loginUser, Model model){
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("posts", postRepository.findAll());
		
		return "postmain";
	}
	
	@GetMapping("/post")
	public String post(@ModelAttribute("post") Post post, Authentication loginUser, Model model){
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		
		return "post";
	}
	
	@PostMapping("/post")
	public String post(@Validated @ModelAttribute("post") Post post, BindingResult result, Authentication loginUser) {
		if(result.hasErrors()) {
			System.out.println(result);
			System.out.println(post.getPostBody());
			return "redirect:/post?post";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		
		//ZonedDateTime zonedDateTime = ZonedDateTime.now();
		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSSxxxxx VV");
		//String s = zonedDateTime.format(formatter);
		
		post.setCreatedAt(ldt);
		postRepository.save(post);
		
		return "redirect:/postmain?recordPost";
	}
	
	@RequestMapping("/post/{postId}")
	public String showPost(@PathVariable("postId") Long postId, @ModelAttribute("comment") PostComment comment, Authentication loginUser, Model model){
		model.addAttribute("user", userRepository.findByUsername(loginUser.getName()));
		model.addAttribute("post", postRepository.findByPostId(postId));
		model.addAttribute("comments", commentRepository.findByPostId(postId));
		
		return "postdetail";
	}
	
	@PostMapping("/postComment")
	public String postComment(@Validated @ModelAttribute("comment") PostComment comment, BindingResult result, Authentication loginUser) {
		if(result.hasErrors()) {
			System.out.println(result);
			System.out.println(comment.getCommentBody());
			return "redirect:/post?post";
		}
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		
		//ZonedDateTime zonedDateTime = ZonedDateTime.now();
		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSSxxxxx VV");
		//String s = zonedDateTime.format(formatter);
		
		comment.setCreatedAt(ldt);
		commentRepository.save(comment);
		
		return "redirect:/postmain?postdetail";
	}

}