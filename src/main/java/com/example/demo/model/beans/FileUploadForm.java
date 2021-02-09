package com.example.demo.model.beans;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class FileUploadForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private MultipartFile uploadedFile;
}