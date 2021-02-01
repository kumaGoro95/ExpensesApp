package com.example.demo.model.beans;

import java.io.Serializable;

import lombok.Data;

@Data
public class RefineCondition implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String categoryCode;
	private String startDate;
	private String endDate;

	public RefineCondition(String categoryCode, String startDate, String endDate) {
			this.categoryCode = categoryCode;
			this.startDate = startDate;
			this.endDate = endDate;
		}
}