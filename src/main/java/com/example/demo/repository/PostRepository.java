package com.example.demo.repository;


import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Post;
import com.example.demo.model.beans.DailySumGraph;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
	
	public Post findByPostId(int postId);
	
	public void deleteByPostId(int postId);
	
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

}
