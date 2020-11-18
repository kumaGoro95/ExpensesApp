package com.example.demo.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import com.sun.istack.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="users")
public class SiteUser {
	
	@Id //主キー
	//DBのidentity列を使用して、キーを自動採番(strategyが無いとautoになる)
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@NotNull
	@Column(name = "user_id", nullable = false)
	private long id;
	
	@NotBlank
	@Size(min = 2, max = 20)
	@Column(name = "user_name", nullable = false)
	private String username;
	
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
	
	@Max(100)
	@Column(name = "money_records_name")
	private String moneyRecordsName;
	
	@Column(name = "created_at")
	private String createdAt;
	
	@Column(name = "updatedAt")
	private String updatedAt;
}