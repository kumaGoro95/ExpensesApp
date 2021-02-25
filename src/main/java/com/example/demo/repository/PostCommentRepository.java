package com.example.demo.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.PostComment;
import com.example.demo.model.beans.CommentByNickname;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	
	public List<PostComment> findByPostId(int postId);
	
	public PostComment findByCommentId(int commentId);
	
	public void deleteByCommentId(int commentId);
	
	public void deleteByUsername(String username);
	
	@Query(value = "select comment_id, post_id, U.user_id, U.user_name, comment_body, C.created_at, C.updated_at "
			+ "from comments C left join users U on C.user_id = U.user_id where C.post_id = :postId", nativeQuery = true)
	public List<Object[]> getCommentsByPostId(@Param("postId") int postId);
	
	default List<CommentByNickname> findCommentsByPostId(int postId){
		return getCommentsByPostId(postId).stream().map(CommentByNickname::new).collect(Collectors.toList());
	}
	

}
