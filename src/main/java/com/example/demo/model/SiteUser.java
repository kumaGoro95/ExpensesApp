package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="users")
public class SiteUser{

	@Id //主キー

	@NotNull
	@Size(min = 4, max = 20)
	@Column(name = "user_id", nullable = false)
	private String username;
	
	@Size(min = 2, max = 20)
	@Column(name = "user_name", nullable = false)
	private String userNickname;
	
	@NotBlank
	@Email
	@Column(name = "user_email", nullable = false)
	private String email;
	
	@NotBlank
	@Size(min = 4, max = 255)
	@Column(name = "user_password", nullable = false)
	private String password;
	
	@Column(name = "user_gender")
	private int gender;
	
	@Column(name = "user_dob")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private LocalDate dateOfBirth;
	
	@Max(100)
	@Column(name = "user_icon")
	private String icon;
	
	private boolean admin;
	
	@Column(name = "user_role")
	private String role;
	
	@Column(name = "created_at")
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private LocalDateTime createdAt;
	
	@Column(name = "updatedAt")
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updatedAt;
}