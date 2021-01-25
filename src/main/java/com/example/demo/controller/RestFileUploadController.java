package com.example.demo.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class RestFileUploadController {
    
  // 引数名 `file` は、ファイルのリクエストパラメータ名と一致させる。
  @RequestMapping(value="/upload", method=RequestMethod.POST)
  public void handle(HttpServletResponse response, @RequestParam MultipartFile file){
    // ファイルが空の場合は HTTP 400 を返す。
    if (file.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    // アップロードされたファイルを保存。
    try {
      BufferedInputStream in = new BufferedInputStream(file.getInputStream());
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file.getOriginalFilename()));
      FileCopyUtils.copy(in, out);
    } catch (IOException e) {
      throw new RuntimeException("Error uploading file.", e);
    }
  }
}