package com.example.demo.model.beans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PostByNickname implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private int postId;
	
	private int postCategory;
	
	private String username;

	private String userNickname;
	
	private String postTitle;

	private String postBody;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;
	
	public PostByNickname(int postId, int postCategory, String username, String userNickname, String postTitle, String postBody, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.postId = postId;
		this.postCategory = postCategory;
		this.username = username;
		this.userNickname = userNickname;
		this.postTitle = postTitle;
		this.postBody = postBody;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public PostByNickname(Object[] objects) {
        this((int) objects[0], (int) objects[1], (String) objects[2], (String) objects[3], (String) objects[4], (String) objects[5],(LocalDateTime) objects[6], (LocalDateTime) objects[7]);
    }

}