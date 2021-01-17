package com.example.demo.model.beans;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class SummaryByCategory implements Serializable{
	 
	private static final long serialVersionUID = 1L;
	
	private int categoryCode;
	private BigDecimal sum;
	
	public SummaryByCategory(int categoryCode, BigDecimal sum) {
		super();
		this.categoryCode = categoryCode;
		this.sum = sum;
	}
	
	public SummaryByCategory(Object[] objects) {
        this((int) objects[0], (BigDecimal) objects[1]);
    }

}
