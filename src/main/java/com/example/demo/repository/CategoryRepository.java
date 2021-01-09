package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	
	public List<Category> findBycategoryCode(int categoryCode);

}

