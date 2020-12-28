package com.example.demo.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.example.demo.model.MoneyRecord;

@Repository
public class MoneyRecordDaoImpl implements MoneyRecordDao<MoneyRecord> {

	private static final long serialVersionUID = 1L;
	private EntityManager em;
	
	public MoneyRecordDaoImpl() {
		super();
	}
	
	public MoneyRecordDaoImpl(EntityManager manager) {
		this();
		em = manager;
	}
	
	@Override
	public List<MoneyRecord> getAll(){
		Query query = em.createQuery("from MoneyRecord");
		@SuppressWarnings("unchecked")
		List<MoneyRecord> list = query.getResultList();
		em.close();
		return list;
	}
	
	@Override
	public List<MoneyRecord> findByUsername(String username){
		String qstr = "from MoneyRecord where username = :fstr";
		Query query = em.createQuery(qstr).setParameter("fstr", username);
		@SuppressWarnings("unchecked")
		List<MoneyRecord> list = query.getResultList();
		em.close();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MoneyRecord> find(String fstr, String a){
		List<MoneyRecord> list = null;
		String qstr = "from MoneyRecord where username = ?1 and record_date like ?2 and category_id not like ?3";
		Query query = em.createQuery(qstr).setParameter(1, fstr)
				.setParameter(2, a + "%")
				.setParameter(3, 20 + "%");
		list = query.getResultList();
		
		return list;
	}

}
