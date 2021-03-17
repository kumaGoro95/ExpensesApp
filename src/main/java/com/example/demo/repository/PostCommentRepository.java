package com.example.demo.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.PostComment;
import com.example.demo.model.beans.CommentByNickname;
import com.example.demo.model.beans.PostByNickname;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	
	public List<PostComment> findByPostId(int postId);
	
	public PostComment findByCommentId(int commentId);
	
	public void deleteByCommentId(int commentId);
	
	public void deleteByUsername(String username);
	
	@Query(value = "select comment_id, post_id, U.user_id, U.user_name, U.user_icon, comment_body, C.created_at, C.updated_at "
			+ "from comments C left join users U on C.user_id = U.user_id where C.post_id = :postId", nativeQuery = true)
	public List<Object[]> getCommentsByPostId(@Param("postId") int postId);
	
	default List<CommentByNickname> findCommentsByPostId(int postId){
		List<Object[]> list = getCommentsByPostId(postId);
		for (int i = 0; i < list.size(); i++) {
			Timestamp tst1 = (Timestamp) list.get(i)[6];
			LocalDateTime ldt1 = tst1.toLocalDateTime();
			list.get(i)[6] = ldt1;

			if (list.get(i)[7] != null) {
				Timestamp tst2 = (Timestamp) list.get(i)[7];
				LocalDateTime ldt2 = tst2.toLocalDateTime();
				list.get(i)[7] = ldt2;
			}
		}
		
		List<CommentByNickname> returnList = list.stream().map(CommentByNickname::new).collect(Collectors.toList());
		return returnList;
	}
	

}
