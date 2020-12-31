package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.SiteUser;

public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {
	//Emailでユーザーを検索
	public SiteUser findByEmail(String email);
	//usernameで以下同上
	public SiteUser findByUsername(String username);
	//同名のユーザーが存在するか判定
	boolean existsByUsername(String username);
	
	void deleteByUsername(String username);

}
