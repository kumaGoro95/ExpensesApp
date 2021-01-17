package com.example.demo.model.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class DailySumGraph implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String date;
	private BigDecimal sum;
	
	public DailySumGraph(String date, BigDecimal sum) {
		super();
		this.date = date;
		this.sum = sum;
	}
	
	public DailySumGraph(Object[] objects) {
        this((String) objects[0], (BigDecimal) objects[1]);
    }
	

}

