package com.example.demo.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CategorySum {
	private long categoryId;
	private BigDecimal sum;
	
	public CategorySum(long categoryId, BigDecimal sum) {
		this.categoryId = categoryId;
		this.sum = sum;
	}

}
