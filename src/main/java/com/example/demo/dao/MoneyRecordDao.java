package com.example.demo.dao;

import java.io.Serializable;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.demo.model.CategorySum;

@Repository
public interface MoneyRecordDao <T> extends Serializable {
	public List<T> getAll();
	public List<T> findByUsername(String username);
	public List<T> find(String fstr, String a);
	public BigDecimal sumMonthExpense(String fstr, String a);
	public CategorySum sumCategoryExpense(String fstr, String month, int category);
}