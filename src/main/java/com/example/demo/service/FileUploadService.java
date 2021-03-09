package com.example.demo.service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.awt.*;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileUploadService {

	// 画像アップロード
	public String uploadImage(HttpServletResponse response, MultipartFile file) {
		// ファイルが空の場合は HTTP 400 を返す。
		if (file.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "";
		}
		// アップロードされたファイルを保存。
		try {

			File convFile = new File(file.getOriginalFilename());

			// 拡張子を除いたファイル名を取得
			int dotPlace = file.getOriginalFilename().indexOf(".");
			String fileType = file.getOriginalFilename().substring(dotPlace);
			System.out.println(fileType);
			File temporaryFile = new File("icon" + fileType);
			System.out.println(convFile.renameTo(temporaryFile));
			System.out.println("name" + temporaryFile.getName());
			temporaryFile.createNewFile();
			String filepath = Paths.get(temporaryFile.getName()).toString();
			FileOutputStream fos = new FileOutputStream(filepath);
			fos.write(file.getBytes());
			fos.close();

			// 画像のリサイズ
			String filename = resizingImage(temporaryFile.getName());

			return filename;

		} catch (IOException e) {
			throw new RuntimeException("Error uploading file.", e);
		}
	}

	// リサイズする画像を取得し、リサイズする
	public String resizingImage(String file) throws IOException {
		// 拡張子を除いたファイル名を取得
		int dotPlace = file.indexOf(".");
		String filename = file.substring(0, dotPlace);

		// パスを含めたファイル名を取得
		String filepath = Paths.get(file).toString();
		File originalFile = new File(filepath);

		// リサイズ
		BufferedImage bi = scaleImage(originalFile, 120, 120);
		
		//オリジナルファイルを削除
		originalFile.delete();

		// リサイズ後のファイルを保存
		String resizedFilename = toStr(LocalDateTime.now(), "yyyy-MM-dd-mm-ss-SSS") + ".jpg";
		String resizedFilepath = Paths.get(resizedFilename).toString();
		File resizedFile = new File(resizedFilepath);

		// 2番目の引数が画像の形式、3番目がファイル名
		ImageIO.write(bi, "jpeg", resizedFile);

		// AWSへ接続
		BasicAWSCredentials creds = new BasicAWSCredentials("",
				"");
		final AmazonS3 s3 = AmazonS3Client.builder().withRegion("ap-northeast-1")
				.withCredentials(new AWSStaticCredentialsProvider(creds)).build();

		try {
			
			// ファイルをS3にアップロードする
			s3.putObject("piggy-box-s3/icons", resizedFilename, new File(resizedFilepath));
			//リサイズ後のファイルを削除
			resizedFile.delete();
		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			System.exit(1);
		}
		

		return resizedFilename;
	}

	/**
	 * 
	 * @param in         読み込むファイル
	 * @param destWidth  出力する画像の横の最大サイズ
	 * @param destHeight 出力する画像の縦の最大サイズ
	 * @return BufferedImage
	 * @throws IOException
	 */
	public BufferedImage scaleImage(File in, int destWidth, int destHeight) throws IOException {
		BufferedImage src = ImageIO.read(in);

		int width = src.getWidth(); // オリジナル画像の幅
		int height = src.getHeight(); // オリジナル画像の高さ

		// 縦横の比率から、scaleを決める
		double widthScale = (double) destWidth / (double) width;
		double heightScale = (double) destHeight / (double) height;
		double scale = widthScale < heightScale ? widthScale : heightScale;

		ImageFilter filter = new AreaAveragingScaleFilter((int) (src.getWidth() * scale),
				(int) (src.getHeight() * scale));
		ImageProducer p = new FilteredImageSource(src.getSource(), filter);
		Image dstImage = Toolkit.getDefaultToolkit().createImage(p);
		BufferedImage dst = new BufferedImage(dstImage.getWidth(null), dstImage.getHeight(null),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dst.createGraphics();
		g.drawImage(dstImage, 0, 0, null);
		g.dispose();

		return dst;
	}

	public static String toStr(LocalDateTime localDateTime, String format) {

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		return localDateTime.format(dateTimeFormatter);

	}
}
