package com.example.demo.repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

	public List<Post> findByPostBody(String word);
	
	public boolean existsByPostCategory(int postCategory);

	// コメント数を集計
	@Query(value = "select P.post_id, count(C.comment_id) from posts P left join comments C on P.post_id = C.post_id group by P.post_id", nativeQuery = true)
	public List<Object[]> getCommentCount();

	default Map<Integer, BigInteger> findCommentCount() {
		Map<Integer, BigInteger> map = new HashMap<Integer, BigInteger>();
		for (int i = 0; i < getCommentCount().size(); i++) {
			int key = (int) getCommentCount().get(i)[0];
			BigInteger value = (BigInteger) getCommentCount().get(i)[1];
			map.put(key, value);
		}
		return map;
	}

	// 全投稿を取得
	@Query(value = "select post_id, post_category, U.user_id, U.user_name, post_title, post_body, P.created_at, P.updated_at "
			+ "from posts P left join users U on P.user_id = U.user_id", nativeQuery = true)
	public List<Object[]> getAllPosts();

	default List<PostByNickname> findAllPosts() {
		List<Object[]> list = getAllPosts();
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
		List<PostByNickname> returnList = list.stream().map(PostByNickname::new).collect(Collectors.toList());
		return returnList;
	}

	// 固有のユーザーの投稿を取得
	@Query(value = "select post_id, post_category, U.user_id, U.user_name, post_title, post_body, P.created_at, P.updated_at "
			+ "from posts P left join users U on P.user_id = U.user_id where P.user_id = :username", nativeQuery = true)
	public List<Object[]> getAllPostsByUsername(@Param("username") String username);

	default List<PostByNickname> findAllPostsByUsername(String username) {
		List<Object[]> list = getAllPostsByUsername(username);
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
		List<PostByNickname> returnList = list.stream().map(PostByNickname::new).collect(Collectors.toList());
		return returnList;
	}

	// postIdで検索
	@Query(value = "select post_id, post_category, U.user_id, U.user_name, post_title, post_body, P.created_at, P.updated_at "
			+ "from posts P left join users U on P.user_id = U.user_id where P.post_id = :postId", nativeQuery = true)
	public List<Object[]> getPostByPostId(@Param("postId") int postId);

	default List<PostByNickname> findPostByPostId(int postId) {
		List<Object[]> list = getPostByPostId(postId);
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
		List<PostByNickname> returnList = list.stream().map(PostByNickname::new).collect(Collectors.toList());
		return returnList;
	}

	//カテゴリで検索
	@Query(value = "select post_id, post_category, U.user_id, U.user_name, post_title, post_body, P.created_at, P.updated_at "
			+ "from posts P left join users U on P.user_id = U.user_id where P.post_category = :category", nativeQuery = true)
	public List<Object[]> getPostByCategory(@Param("category") int category);

	default List<PostByNickname> findPostByCategory(int category) {
		List<Object[]> list = getPostByCategory(category);
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
		List<PostByNickname> returnList = list.stream().map(PostByNickname::new).collect(Collectors.toList());
		return returnList;
	}
}
