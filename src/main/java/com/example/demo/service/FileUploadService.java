package com.example.demo.service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class FileUploadService {
	
	//画像アップロード
	public String uploadImage(HttpServletResponse response, MultipartFile file) {
		// ファイルが空の場合は HTTP 400 を返す。
		if (file.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "";
		}
		// アップロードされたファイルを保存。
		try {
			BufferedInputStream in = new BufferedInputStream(file.getInputStream());
			String filepath = Paths.get("src/main/resources/static/images/" + file.getOriginalFilename()).toString();
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filepath));
			FileCopyUtils.copy(in, out);
			
			//画像のリサイズ
			String filename = resizingImage(file.getOriginalFilename());
			
			return filename;
			
		} catch (IOException e) {
			throw new RuntimeException("Error uploading file.", e);
		}
	}
	
	
	//リサイズする画像を取得し、リサイズする
	public String resizingImage(String file) throws IOException{
			//拡張子を除いたファイル名を取得
			int dotPlace = file.indexOf(".");
			String filename = file.substring(0,dotPlace);
			
			//パスを含めたファイル名を取得
			String filepath = Paths.get("src/main/resources/static/images/" + file).toString();
			
			//リサイズ
			BufferedImage bi = scaleImage(new File(filepath), 120, 120);
			
			//リサイズ後のファイルを保存
			String resizedFilepath = Paths.get("src/main/resources/static/icons/" + filename + ".jpg").toString();
			String resizedFilename = filename + ".jpg";
			
			// 2番目の引数が画像の形式、3番目がファイル名
			ImageIO.write(bi, "jpeg", new File(resizedFilepath));
			
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
}
