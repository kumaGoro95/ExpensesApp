package com.example.demo.model;

import javax.persistence.Column;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="money_records")
public class MoneyRecord{
	
	@Id //主キー
	//DBのidentity列を使用して、キーを自動採番(strategyが無いとautoになる)
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@NotNull
	@Column(name = "record_id")
	private int recordId;
	
	@NotNull
	@Column(name = "user_id")
	private String username;
	
	@NotNull
	@Column(name = "record_date")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private LocalDate recordDate;
	
	@NotNull
	@Column(name = "income_and_expense")
	private int incomeAndExpense;
	
	@NotNull
	@Column(name = "category_id")
	private int categoryId;
	
	@Length(min=0, max=20)
	@Column(name = "record_note")
	private String note;
	
	@Column(name = "created_at")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	
	@Column(name = "updatedAt")
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private LocalDateTime updatedAt;
}