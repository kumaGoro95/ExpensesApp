package com.example.demo.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PostCategoryCodeToIcon {
	public static final Map<Integer, String> PostCategoriesToIcon;
	static {
		//Map<キーのクラス,値のクラス>
		Map<Integer, String> categoriesToIcon = new LinkedHashMap<>();
		//putで値とキーを対応付ける
		categoriesToIcon.put(1, "fas fa-utensils");
		categoriesToIcon.put(2, "fas fa-magic");
		categoriesToIcon.put(3, "fas fa-weight");
		categoriesToIcon.put(4, "fas fa-child");
		categoriesToIcon.put(5, "fas fa-music");
		categoriesToIcon.put(6, "fas fa-home");
		categoriesToIcon.put(7, "fas fa-paw");
		categoriesToIcon.put(8, "fas fa-clipboard-list");
		categoriesToIcon.put(9, "fas fa-user-tie");
		categoriesToIcon.put(10, "fas fa-coins");
		categoriesToIcon.put(11, "fas fa-money-check");
		
		//Categoriesを変更不可にする
		PostCategoriesToIcon = Collections.unmodifiableMap(categoriesToIcon);
	}

}
