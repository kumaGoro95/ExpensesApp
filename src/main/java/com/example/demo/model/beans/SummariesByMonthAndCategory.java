package com.example.demo.model.beans;

import java.io.Serializable;
import java.math.BigDecimal;

public class SummariesByMonthAndCategory implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String month;
	private String category;
	private BigDecimal sum;
	
	public SummariesByMonthAndCategory(String month, String category, BigDecimal sum) {
		super();
		this.month = month;
		this.category = category;
		this.sum = sum;
	}
	
	public SummariesByMonthAndCategory(Object[] objects) {
        this((String) objects[0], (String)objects[1], (BigDecimal) objects[2]);
    }

}
