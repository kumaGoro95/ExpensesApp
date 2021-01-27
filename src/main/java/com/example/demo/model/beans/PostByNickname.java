package com.example.demo.model.beans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PostByNickname implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private int postId;
	
	private String username;

	private String userNickname;

	private String postBody;

	private Timestamp createdAt;

	private Timestamp updatedAt;
	
	public PostByNickname(int postId, String username, String userNickname, String postBody, Timestamp createdAt, Timestamp updatedAt) {
		this.postId = postId;
		this.username = username;
		this.userNickname = userNickname;
		this.postBody = postBody;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public PostByNickname(Object[] objects) {
        this((int) objects[0], (String) objects[1], (String) objects[2], (String) objects[3],(Timestamp) objects[4], (Timestamp) objects[5]);
    }

}