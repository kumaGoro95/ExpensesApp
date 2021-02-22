package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.util.Role;

@Service
public class SecurityService {
	
	@Autowired
	private final SiteUserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	public SecurityService(SiteUserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
public boolean registUser(SiteUser user) {
		
		// 同名ユーザー、メールアドレスのアカウントが存在していないか確認
		if (userRepository.existsByUsername(user.getUsername()) == true
				| userRepository.existsByEmail(user.getEmail()) == true) {
			return false;
		}
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		if (user.isAdmin()) {
			user.setRole(Role.ADMIN.name());
		} else {
			user.setRole(Role.USER.name());
		}
		
		//デフォルトのアイコン画像を設定
		user.setIcon("default-icon.jpg");

		// 現在日時を取得、登録日時にセット
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		user.setCreatedAt(ldt);
		userRepository.save(user);
		
		return true;

	}

}
