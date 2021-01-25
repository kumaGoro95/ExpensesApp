package com.example.demo.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

	public List<Like> findByPostId(int postId);
	
	public List<Like> findByUsername(String username);

	public int countByPostId(int postId);

	// 既にいいねしているか検索
	boolean existsByUsernameAndPostId(String username, int postId);

	public void deleteByUsernameAndPostId(String username, int postId);

	@Query(value = "select post_id from likes where user_id = :username", nativeQuery = true)
	public Object[] getMyLikes(String username);
	
	default List<Integer> findMyLikes(String username) {
		List<Integer> myLikes = new ArrayList<Integer>();
		Object[] myLikesObj = getMyLikes(username);
		for (int i = 0; i < myLikesObj.length; i++) {
			myLikes.add((Integer)myLikesObj[i]);
		}
		return myLikes;
	}
	
	
	// いいねの数を集計
	@Query(value = "select post_id, count(*) from likes group by post_id", nativeQuery = true)
	public List<Object[]> getLikeCount();

	default Map<Integer, BigInteger> findLikeCount() {
		Map<Integer, BigInteger> map = new HashMap<Integer, BigInteger>();
		for (int i = 0; i < getLikeCount().size(); i++) {
			int key = (int) getLikeCount().get(i)[0];
			BigInteger value = (BigInteger) getLikeCount().get(i)[1];
			map.put(key, value);
		}
		return map;
	}

}
