package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.SiteUser;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.LikeRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.PostCommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.SiteUserRepository;

@Service
public class UserService {

	// DI
	@Autowired
	private final SiteUserRepository userRepository;
	private final MoneyRecordRepository moneyRecordRepository;
	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	private final PostCommentRepository commentRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserService(SiteUserRepository userRepository, MoneyRecordRepository moneyRecordRepository, 
			PostRepository postRepository,LikeRepository likeRepository,PostCommentRepository commentRepository,
			BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.moneyRecordRepository = moneyRecordRepository;
		this.postRepository = postRepository;
		this.likeRepository = likeRepository;
		this.commentRepository = commentRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// ユーザープロフィール変更時、同名ユーザー、メールアドレスのアカウントが存在していないか確認
	public boolean existsSameAccount(String username, SiteUser user) {

		// ログインユーザーの変更前メールアドレス
		SiteUser currentEmail = userRepository.findByUsername(username);

		// 同名ユーザー、同メールアドレスユーザーが存在すればtrueを返す
		if (user.getEmail().equals(currentEmail.getEmail()) && userRepository.countByEmail(user.getEmail()) > 1) {
			return true;
		} else if (user.getEmail().equals(currentEmail.getEmail()) == false
				&& userRepository.countByEmail(user.getEmail()) == 1) {
			return true;
		} else {
			return false;
		}

	}

	// ユーザープロファイル更新
	public void updateSetting(SiteUser user, String iconStr) {

		//更新日時を取得
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		
		user.setUpdatedAt(ldt);
		user.setIcon(iconStr);
		user.setPassword(user.getPassword());

		userRepository.save(user);
	}
	
	public void changePassword(SiteUser user) {
		
		//更新日時を取得
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setUpdatedAt(ldt);
		
		//パスワードをエンコード
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		userRepository.save(user);
	}
	
	//ユーザー削除
	public void deleteUserInfo(String username) {
		
		//ユーザーに関する全情報を削除
		moneyRecordRepository.deleteByUsername(username);
		userRepository.deleteByUsername(username);
		postRepository.deleteByUsername(username);
		likeRepository.deleteByUsername(username);
		commentRepository.deleteByUsername(username);
	}

}
