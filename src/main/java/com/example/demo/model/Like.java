package com.example.demo.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
@Entity
@Table(name="likes")
public class Like {
	
	@Id //主キー
	//DBのidentity列を使用して、キーを自動採番(strategyが無いとautoになる)
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@NotNull
	@Column(name = "like_id")
	private long likeId;
	
	@NotNull
	@Column(name = "user_id")
	private String username;
	
	@NotNull
	@Column(name = "post_id")
	private long postId;
	
	@Column(name = "created_at")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

}
