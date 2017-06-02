package com.kangmeng.service;

import com.kangmeng.model.kafka.CartOffset;
import com.kangmeng.repository.CartOffsetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class OffsetServiceImpl implements OffsetService {

	@Autowired
	CartOffsetRepository cartOffsetRepository;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveOffset(Long cartId, Integer partition, Long offset) {
		CartOffset cartOffset = cartOffsetRepository.findOne(cartId);
		if (cartOffset == null) {
			cartOffset = new CartOffset();
			cartOffset.setId(cartId);
		}
		if (cartOffset != null && cartOffset.getPartition() != null) {
			cartOffset.setPartition(partition);
			cartOffset.setOffset(offset);
		}
	}

	@Override
	public Long getOffset(Integer partition) {
		return cartOffsetRepository.getOffset(partition);
	}

	@Override
	public void savePrinted(Long cartId) {
		CartOffset cartOffset = cartOffsetRepository.findOne(cartId);
		if (cartOffset != null) {
			cartOffset.setPrinted(true);
		}
	}
}
