package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryName {
	private String categoryName;
	private int numberOfSubcategory; 
	
	public CategoryName(String categoryName, int numberOfSubcategory) {
		this.categoryName = categoryName;
		this.numberOfSubcategory = numberOfSubcategory;
	}
}
