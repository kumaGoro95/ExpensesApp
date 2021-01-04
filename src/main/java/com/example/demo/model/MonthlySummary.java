package com.example.demo.model;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class MonthlySummary implements Serializable{

	private static final long serialVersionUID = 1L;

	private String month;
	private BigDecimal sum;

	
	public MonthlySummary(String month, BigDecimal sum) {
		super();
		this.month = month;
		this.sum = sum;
	}
	
	public MonthlySummary(Object[] objects) {
        this((String) objects[0], (BigDecimal) objects[1]);
    }

}
