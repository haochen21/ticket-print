package com.kangmeng.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class CartOffsetRepositoryImpl implements CartOffsetRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Long getOffset(String topic,Integer partition) {
		String sql = "SELECT MAX(c.offset) FROM CartOffset c where c.topic = :topic and c.partition = :partition AND c.printed = true";
		Query query = em.createQuery(sql);
		query.setParameter("topic",topic);
		query.setParameter("partition", partition);
		Object value = query.getSingleResult();
		if(value != null){
			long number = ((Number) query.getSingleResult()).longValue();
			return number;
		}
		else {
			return null;
		}
	}
}
