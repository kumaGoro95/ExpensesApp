package com.example.demo.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.example.demo.model.Category;
import com.example.demo.model.CategoryName;

@Repository
public class CategoryDaoImpl implements CategoryDao<Category> {

	private static final long serialVersionUID = 1L;
	private EntityManager em;

	public CategoryDaoImpl() {
		super();
	}

	public CategoryDaoImpl(EntityManager manager) {
		this();
		em = manager;
	}

	@Override
	public List<CategoryName> getCategory() {
		Query query = em.createQuery("select categoryName from Category");
		@SuppressWarnings("unchecked")
		List<String> list = query.getResultList();
		em.close();

		List<CategoryName> categoryList = new ArrayList<>();
		
		int number;
		String name;
		for (int i = 0; list.size() > 1;) {
			number = 1;
			while (list.get(i).equals(list.get(i + 1))) {
				list.remove(i + 1);
				number += 1;
			}
			name = list.get(i);
			CategoryName category = new CategoryName(name, number);
			categoryList.add(category);
			list.remove(i);
		}
		name = list.get(0);
		number = 1;
		CategoryName category = new CategoryName(name, number);
		categoryList.add(category);

		return categoryList;
	}

}
