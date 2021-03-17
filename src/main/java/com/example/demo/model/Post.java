package com.example.demo.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import lombok.Data;

@Data
@Entity
@Table(name="posts")
public class Post{
	

	@Id //主キー
	//DBのidentity列を使用して、キーを自動採番(strategyが無いとautoになる)
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@NotNull
	@Column(name = "post_id")
	private int postId;
	
	@Min(1)
	@Column(name = "post_category")
	private int postCategory;
	
	@NotNull
	@Column(name = "user_id")
	private String username;
	
	@Length(min=0, max=40)
	@NotBlank
	@Column(name = "post_title")
	private String postTitle;
	
	@Length(min=0, max=500)
	@NotBlank
	@Column(name = "post_body")
	private String postBody;
	
	@Column(name = "created_at")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	
	@Column(name = "updatedAt")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;
	
}
