package com.kangmeng.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class CartOffsetRepositoryImpl implements CartOffsetRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@Override public Long getOffset(Integer partition) {
		String sql = "SELECT MAX(c.offset) FROM CartOffset c where c.partition = :partition AND c.printed = true";
		Query query = em.createQuery(sql);
		query.setParameter("partition", partition);
		long number = ((Number) query.getSingleResult()).longValue();
		if (number > 0)
			return number;
		else
			return 0L;
	}
}
