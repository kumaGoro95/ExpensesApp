package com.example.demo.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.example.demo.model.MoneyRecord;
import com.example.demo.model.beans.MoneyRecordList;
import com.example.demo.model.beans.PostByNickname;

@Repository
public class PostDaoImpl implements PostDao<PostByNickname> {

	private static final long serialVersionUID = 1L;
	private EntityManager em;

	public PostDaoImpl() {
		super();
	}

	public PostDaoImpl(EntityManager manager) {
		this();
		em = manager;
	}

	//投稿検索（複数ワード対応）
	@SuppressWarnings("unchecked")
	@Override
	public List<PostByNickname> findPostByWords(String words[]) {
		String qstr1 = "select P.postId, P.postCategory, U.username, U.userNickname, P.postTitle, P.postBody, P.createdAt, P.updatedAt from Post P ";
		String qstr2 = "left join SiteUser U on P.username = U.username where P.postBody like concat('%', ?1, '%') ";
		String qstr3 = "and P.postBody like concat('%', ?number, '%')";
		String qstr = qstr1 + qstr2;
		//検索ワード数が２以上の場合
		if (words.length > 1) {
			for (int i = 2; i < words.length + 1; i++) {
				String currentNumber = String.valueOf(i);
				//SQL文の引数に数字を振る
				qstr3 = qstr3.replace("number", currentNumber);
				//SQL文に結合
				qstr += qstr3;
				//引数を所定のワードに戻す
				qstr3 = qstr3.replace(currentNumber, "number");
			}
		}
		Query query = em.createQuery(qstr);
		//検索ワード数に応じて、パラメータをセット
		for (int i = 0; i < words.length; i++) {
			query.setParameter(i + 1, words[i]);
		}
		List<Object[]> listObj = query.getResultList();
		List<PostByNickname> list = listObj.stream().map(PostByNickname::new).collect(Collectors.toList());

		return list;
	}

}
