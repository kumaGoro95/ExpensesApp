package com.example.demo.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface MoneyRecordDao <T> extends Serializable {
	public List<T> getAll();
	public List<T> findByUsername(String username);

}
