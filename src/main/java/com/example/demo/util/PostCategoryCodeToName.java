package com.example.demo.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PostCategoryCodeToName {
	public static final Map<Integer, String> PostCategories;
	static {
		//Map<キーのクラス,値のクラス>
		Map<Integer, String> categories = new LinkedHashMap<>();
		//putで値とキーを対応付ける
		categories.put(1, "料理・レシピ");
		categories.put(2, "ファッション・美容");
		categories.put(3, "健康・ダイエット");
		categories.put(4, "出産・子育て");
		categories.put(5, "趣味");
		categories.put(6, "住宅・インテリア");
		categories.put(7, "ペット");
		categories.put(8, "ライフプラン");
		categories.put(9, "冠婚葬祭");
		categories.put(10, "投資");
		categories.put(11, "税金・保険");
		
		//Categoriesを変更不可にする
		PostCategories = Collections.unmodifiableMap(categories);
	}
}
