package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.MoneyRecordDaoImpl;
import com.example.demo.dao.PostDaoImpl;
import com.example.demo.model.beans.PostByNickname;
import com.example.demo.repository.DateRepository;
import com.example.demo.repository.MoneyRecordRepository;
import com.example.demo.repository.PostRepository;

@Service
public class PostService {

	@Autowired
	private PostDaoImpl pDao;
	private final PostRepository postRepository;

	public PostService(PostDaoImpl pDao, PostRepository postRepository) {
		this.pDao = pDao;
		this.postRepository = postRepository;
	}

	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void init() {
		pDao = new PostDaoImpl(em);
	}

	// 複数ワードに対応した検索
	public List<PostByNickname> getWords(String words) {
		String wordsStr = words;
		// 入れ物としてlistを用意
		List<String> keyWordsList = new ArrayList<String>();

		// 検索ワードが１つの場合
		if (wordsStr.indexOf(" ") == -1 && wordsStr.indexOf("　") == -1) {
			keyWordsList.add(wordsStr);
			// 検索ワードが複数の場合
		} else {
			// 空白が全角、半角の場合も対応
			while (wordsStr.indexOf(" ") != -1 || wordsStr.indexOf("　") != -1) {
				int number = 0;
				int numberHalf = wordsStr.indexOf(" ");
				int numberFull = wordsStr.indexOf("　");
				// 一番手前の空白の番号を割り出す（半角なのか全角なのかで条件分け）
				if (numberHalf != -1 && numberHalf < numberFull) {
					number = numberHalf;
				} else {
					if (numberFull != -1) {
						number = numberFull;
					} else {
						number = numberHalf;
					}
				}
				String word = wordsStr.substring(0, number);
				// 抜き出した単語をlistにadd
				keyWordsList.add(word);
				// 抜き出した分を削除
				wordsStr = wordsStr.substring(number + 1);
			}
			keyWordsList.add(wordsStr);
		}
		String keyWords[] = keyWordsList.toArray(new String[keyWordsList.size()]);

		List<PostByNickname> list = pDao.findPostByWords(keyWords);
		List<PostByNickname> result = adjustPostBody(list);

		return result;

	}

	// 投稿一覧を取得（posthome用）
	public List<PostByNickname> getAllPosts() {
		List<PostByNickname> posts = postRepository.findAllPosts();

		List<PostByNickname> allPosts = adjustPostBody(posts);

		return allPosts;
	}

	//特定のユーザーの投稿を取得
	public List<PostByNickname> getSpecificPosts(String username) {
		List<PostByNickname> posts = postRepository.findAllPostsByUsername(username);

		List<PostByNickname> specificPosts = adjustPostBody(posts);

		return specificPosts;

	}
	
	//特定のユーザーの「お気に入り」投稿を取得
	public List<PostByNickname> getLikedPosts(String username) {
		List<PostByNickname> posts = postRepository.findLikedPostsByUsername(username);
		List<PostByNickname> specificPosts = adjustPostBody(posts);

		return specificPosts;

	}
	

	// 投稿タイトル・本文省略メソッド（posthome用）
	public List<PostByNickname> adjustPostBody(List<PostByNickname> posts) {
		List<PostByNickname> adjustedPosts = posts;

		// 本文省略
		for (int i = 0; i < adjustedPosts.size(); i++) {
			if (adjustedPosts.get(i).getPostBody().length() > 40) {
				adjustedPosts.get(i).setPostBody(adjustedPosts.get(i).getPostBody().substring(0, 40) + "…");
			}
		}

		// タイトル省略
		for (int i = 0; i < adjustedPosts.size(); i++) {
			if (adjustedPosts.get(i).getPostTitle().length() > 20) {
				adjustedPosts.get(i).setPostTitle(adjustedPosts.get(i).getPostTitle().substring(0, 20) + "…");
			}
		}

		return adjustedPosts;
	}

}
