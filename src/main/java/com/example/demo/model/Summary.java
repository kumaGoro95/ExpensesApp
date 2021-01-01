package com.example.demo.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class Summary implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private BigDecimal sum;
	private LocalDate month;
	
	public Summary(BigDecimal sum, LocalDate month) {
		super();
		this.sum = sum;
		this.month = month;
	}

}
