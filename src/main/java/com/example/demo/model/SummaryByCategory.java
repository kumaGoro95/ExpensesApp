package com.example.demo.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class SummaryByCategory implements Serializable{
	 
	private static final long serialVersionUID = 1L;
	
	private String category;
	private BigDecimal sum;
	
	public SummaryByCategory(String category, BigDecimal sum) {
		super();
		this.category = category;
		this.sum = sum;
	}
	
	public SummaryByCategory(Object[] objects) {
        this((String) objects[0], (BigDecimal) objects[1]);
    }

}
