package com.example.demo.model.beans;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class CommentByNickname implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private int commentId;
	
	private int postId;
	
	private String username;

	private String userNickname;
	
	private String userIcon;

	private String commentBody;

	private Timestamp createdAt;

	private Timestamp updatedAt;
	
	public CommentByNickname(int commentId, int postId, String username, String userNickname, String userIcon, String commentBody, Timestamp createdAt, Timestamp updatedAt) {
		this.commentId = commentId;
		this.postId = postId;
		this.username = username;
		this.userNickname = userNickname;
		this.userIcon = userIcon;
		this.commentBody = commentBody;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public CommentByNickname(Object[] objects) {
        this((int) objects[0],(int) objects[1], (String) objects[2], (String) objects[3], (String) objects[4], (String) objects[5], (Timestamp) objects[6], (Timestamp) objects[7]);
    }

}