package com.example.demo.model.beans;

import java.io.Serializable;

import lombok.Data;

@Data
public class DailySummary implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String title;
	private String start;
	private String end;
	
	public DailySummary(String title, String start, String end) {
		super();
		this.title = title;
		this.start = start;
		this.end = end;
	}
	
	public DailySummary(Object[] objects) {
        this((String) objects[0], (String) objects[1], (String)objects[1]);
    }
	

}
