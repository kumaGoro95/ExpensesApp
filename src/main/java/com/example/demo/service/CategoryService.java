package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;

import com.example.demo.dao.CategoryDaoImpl;

@Service
public class CategoryService {
	
	private CategoryDaoImpl cDao;
	
	@PersistenceContext
	EntityManager em;
	
	@PostConstruct
	public void init() {
		cDao = new CategoryDaoImpl(em);
	}
	
	public CategoryService(CategoryDaoImpl cDao) {
		this.cDao = cDao;
	}
	

}
