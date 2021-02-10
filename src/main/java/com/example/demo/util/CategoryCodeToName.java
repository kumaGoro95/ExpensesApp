package com.example.demo.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryCodeToName {
	public static final Map<Integer, String> Categories;
	static {
		//Map<キーのクラス,値のクラス>
		Map<Integer, String> categories = new LinkedHashMap<>();
		//putで値とキーを対応付ける
		categories.put(1, "食費");
		categories.put(2, "日用品");
		categories.put(3, "交通費");
		categories.put(4, "交際費");
		categories.put(5, "趣味・娯楽");
		categories.put(6, "教育費");
		categories.put(7, "衣服・美容");
		categories.put(8, "健康・医療");
		categories.put(9, "通信費");
		categories.put(10, "水道光熱費");
		categories.put(11, "家・クルマ");
		categories.put(12, "税金・保険");
		categories.put(13, "大型出費");
		categories.put(14, "その他");
		categories.put(15, "貯金");
		categories.put(99, "収入");
		
		//Categoriesを変更不可にする
		Categories = Collections.unmodifiableMap(categories);
	}

}
