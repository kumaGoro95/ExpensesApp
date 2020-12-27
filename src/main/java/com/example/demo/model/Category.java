package com.example.demo.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="categories")
public class Category {
	
	@Id
	@NotNull
	@Column(name = "category_id", nullable = false)
	private int categoryId;
	
	@NotBlank
	@Column(name = "category_name", nullable = false)
	private String categoryName;
	
	@NotBlank
	@Column(name = "subcategory_name", nullable = false)
	private String subcategoryName;
}