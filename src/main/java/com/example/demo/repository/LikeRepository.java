package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
	
	public List<Like> findByPostId(Long postId);
	public int countByPostId(Long postId);

}
