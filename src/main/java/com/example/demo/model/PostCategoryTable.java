package com.example.demo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
@Entity
@Table(name="post_categories")
public class PostCategoryTable {
	
	@Id
	@NotNull
	@Column(name = "post_id")
	private int postId;
	
	@NotNull
	@Column(name = "category_id")
	private int categoryId;
}
