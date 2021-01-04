package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	
	public List<PostComment> findByPostId(long postId);
	
	public PostComment findByCommentId(long commentId);
	
	public void deleteByCommentId(Long commentId);
	

}
