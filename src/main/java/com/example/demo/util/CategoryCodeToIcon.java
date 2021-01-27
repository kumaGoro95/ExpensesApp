package com.example.demo.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryCodeToIcon {
	public static final Map<Integer, String> CategoriesToIcon;
	static {
		//Map<キーのクラス,値のクラス>
		Map<Integer, String> categoriesToIcon = new LinkedHashMap<>();
		//putで値とキーを対応付ける
		categoriesToIcon.put(1, "fas fa-utensils");
		categoriesToIcon.put(2, "fas fa-shopping-basket");
		categoriesToIcon.put(3, "fas fa-subway");
		categoriesToIcon.put(4, "fas fa-glass-cheers");
		categoriesToIcon.put(5, "fas fa-music");
		categoriesToIcon.put(6, "fas fa-book");
		categoriesToIcon.put(7, "fas fa-tshirt");
		categoriesToIcon.put(8, "fas fa-notes-medical");
		categoriesToIcon.put(9, "fas fa-wifi");
		categoriesToIcon.put(10, "fas fa-burn");
		categoriesToIcon.put(11, "fas fa-caravan");
		categoriesToIcon.put(12, "fas fa-money-check");
		categoriesToIcon.put(13, "fas fa-luggage-cart");
		categoriesToIcon.put(14, "fas fa-feather-alt");
		categoriesToIcon.put(15, "fas fa-coins");
		categoriesToIcon.put(99, "fas fa-money-bill-wave");
		
		//Categoriesを変更不可にする
		CategoriesToIcon = Collections.unmodifiableMap(categoriesToIcon);
	}

}
