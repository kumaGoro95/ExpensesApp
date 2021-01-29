package com.example.demo.repository;


import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Post;
import com.example.demo.model.beans.DailySumGraph;
import com.example.demo.model.beans.MoneyRecordList;
import com.example.demo.model.beans.PostByNickname;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
	
	public Post findByPostId(int postId);
	
	public void deleteByPostId(int postId);
	
	public List<Post> findByUsername(String username);

	
	//コメント数を集計
	@Query(value = "select post_id, count(*) from comments group by post_id", nativeQuery = true)
	public List<Object[]> getCommentCount();
	
	default Map<Integer, BigInteger> findCommentCount() {	
		Map<Integer, BigInteger> map = new HashMap<Integer, BigInteger>();
		for(int i = 0; i < getCommentCount().size(); i++) {
			int key = (int)getCommentCount().get(i)[0];
			BigInteger value = (BigInteger)getCommentCount().get(i)[1];
			map.put(key,value);
		}
		return map;
	}
	
	//全投稿を取得
	@Query(value = "select post_id, U.user_id, U.user_name, post_body, P.created_at, P.updated_at "
			+ "from posts P left join users U on P.user_id = U.user_id", nativeQuery = true)
	public List<Object[]> getAllPosts();
	
	default List<PostByNickname> findAllPosts(){
		return getAllPosts().stream().map(PostByNickname::new).collect(Collectors.toList());
	}
	
	//固有のユーザーの投稿を取得
	@Query(value = "select post_id, U.user_id, U.user_name, post_body, P.created_at, P.updated_at "
			+ "from posts P left join users U on P.user_id = U.user_id where P.user_id = :username", nativeQuery = true)
	public List<Object[]> getAllPostsByUsername(@Param("username") String username);
	
	default List<PostByNickname> findAllPostsByUsername(String username){
		return getAllPostsByUsername(username).stream().map(PostByNickname::new).collect(Collectors.toList());
	}
	
	//postIdで検索
	@Query(value = "select post_id, U.user_id, U.user_name, post_body, P.created_at, P.updated_at "
			+ "from posts P left join users U on P.user_id = U.user_id where P.post_id = :postId", nativeQuery = true)
	public List<Object[]> getPostByPostId(@Param("postId") int postId);
	
	default List<PostByNickname> findPostByPostId(int postId){
		return getPostByPostId(postId).stream().map(PostByNickname::new).collect(Collectors.toList());
	}
	
}
