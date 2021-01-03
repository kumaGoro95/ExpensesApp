package com.example.demo.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Summary implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private BigDecimal sum;
	private String month;
	
	public Summary(BigDecimal sum, String month) {
		super();
		this.sum = sum;
		this.month = month;
	}
	
	public Summary(Object[] objects) {
        this((BigDecimal) objects[0], (String) objects[1]);
    }

}
