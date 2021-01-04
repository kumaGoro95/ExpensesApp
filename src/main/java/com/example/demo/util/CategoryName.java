package com.example.demo.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryName {
	public static final Map<Integer, String> Categories;
	static {
		//Map<キーのクラス,値のクラス>
		Map<Integer, String> categories = new LinkedHashMap<>();
		//putで値とキーを対応付ける
		categories.put(10, "食費");
		categories.put(20, "日用品");
		categories.put(30, "交通費");
		categories.put(40, "交際費");
		categories.put(50, "趣味・娯楽");
		categories.put(60, "教育費");
		categories.put(70, "衣服・美容");
		categories.put(80, "健康・医療");
		categories.put(90, "通信費");
		categories.put(11, "水道・光熱費");
		categories.put(12, "住宅");
		categories.put(13, "自動車");
		categories.put(14, "税金・保険");
		categories.put(15, "大型出費");
		categories.put(16, "その他");
		categories.put(17, "貯金");
		categories.put(18, "未分類");
		
		//Categoriesを変更不可にする
		Categories = Collections.unmodifiableMap(categories);
	}

}
