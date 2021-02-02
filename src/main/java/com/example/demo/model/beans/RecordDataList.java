package com.example.demo.model.beans;

import lombok.Data;

@Data
public class RecordDataList{
	private Integer[] recordId;
	
	public RecordDataList(Integer[] recordId){
		this.recordId = recordId;
	}
	
}