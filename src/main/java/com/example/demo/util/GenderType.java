package com.example.demo.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GenderType {
	public static final Map<Integer, String> GENDERS;
	static {
		//Map<キーのクラス,値のクラス>
		Map<Integer, String> genders = new LinkedHashMap<>();
		//putで値とキーを対応付ける
		genders.put(0, "未選択");
		genders.put(1, "男性");
		genders.put(2, "女性");
		genders.put(3, "その他");
		//GENDERSを変更不可にする
		GENDERS = Collections.unmodifiableMap(genders);
	}

}