package com.example.demo.model.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import lombok.Data;

@Data
public class MoneyRecordList implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int recordId;

	private Date recordDate;
	
	private String incomeAndExpense;
	
	private String subcategoryName;
	
	private String note;
	
	public MoneyRecordList(int recordId, Date recordDate, String incomeAndExpense, String subcategoryName, String note) {
		super();
		this.recordId = recordId;
		this.recordDate = recordDate;
		this.incomeAndExpense = incomeAndExpense;
		this.subcategoryName = subcategoryName;
		this.note = note;
	}
	
	public MoneyRecordList(Object[] objects) {
        this((int)objects[0], (Date) objects[1],(String) objects[2], (String) objects[3], (String) objects[4]);
    }

}
