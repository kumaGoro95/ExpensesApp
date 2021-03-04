package com.example.demo.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

@Controller
@RequestMapping("/s3")
public class S3RestController {

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public String download() {
		BasicAWSCredentials creds = new BasicAWSCredentials("AKIAXQDP475AY6KFP2HP", "Ht6ED5zZjxfBbQAZS6aSbZVNARd/nCXgK47g3RLW");
		final AmazonS3 s3 = AmazonS3Client.builder()
			    .withRegion("ap-northeast-1")
			    .withCredentials(new AWSStaticCredentialsProvider(creds))
			    .build();

        try {
            // S3のオブジェクトを取得する
            S3Object o = s3.getObject("piggy-box-s3/icons", "favicon.jpg");
            S3ObjectInputStream s3is = o.getObjectContent();

            // ダウンロード先のファイルパスを指定する
            FileOutputStream fos = new FileOutputStream(new File("C:\\tmp/favicon.jpg"));

            // S3のオブジェクトを1024byteずつ読み込み、ダウンロード先のファイルに書き込んでいく
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }

            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done!");
        
        return "redirect:/?/";
	}

	@RequestMapping(value="/upload", method=RequestMethod.GET)
	public String upload() {
		BasicAWSCredentials creds = new BasicAWSCredentials("AKIAXQDP475AY6KFP2HP", "Ht6ED5zZjxfBbQAZS6aSbZVNARd/nCXgK47g3RLW");
		final AmazonS3 s3 = AmazonS3Client.builder()
			    .withRegion("ap-northeast-1")
			    .withCredentials(new AWSStaticCredentialsProvider(creds))
			    .build();

        try {
            // ファイルをS3にアップロードする
            s3.putObject("piggy-box-s3/icons", "test.txt", new File("C:\\tmp/test.txt"));
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
        
        return "redirect:/?/";
    }
}
