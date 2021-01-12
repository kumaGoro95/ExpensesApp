package com.example.demo.dao;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.demo.model.CategoryName;

@Repository
public interface CategoryDao <T> extends Serializable {
	public List<CategoryName> getCategory();
	public String getCategoryName(int categoryId);
}
