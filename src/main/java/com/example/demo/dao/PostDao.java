package com.example.demo.dao;

import java.io.Serializable;
import java.util.List;

public interface PostDao <T> extends Serializable{
	public List<T> findPostByWords(String words[]);
}
