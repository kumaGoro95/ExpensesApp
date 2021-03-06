package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.format.annotation.NumberFormat;

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
	
	@Size(min = 4, max = 255)
	@Column(name = "user_password", nullable = false)
	private String password;
	
	@Size(min = 0, max = 300)
	@Column(name ="introduce")
	private String introduce;
	
	@Column(name = "user_icon")
	private String icon;
    
	@NotNull
	@Column(name = "budget")
	@NumberFormat(pattern = "#,###")
	private BigDecimal budget;
	
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