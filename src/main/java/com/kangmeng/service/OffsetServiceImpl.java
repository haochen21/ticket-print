package com.kangmeng.service;

import com.kangmeng.model.kafka.CartOffset;
import com.kangmeng.repository.CartOffsetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class OffsetServiceImpl implements OffsetService {

    @Autowired
    CartOffsetRepository cartOffsetRepository;

    private final static Logger logger = LoggerFactory.getLogger(OffsetServiceImpl.class);

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void saveOffset(Long cartId, String topic, Integer partition, Long offset) {
        CartOffset cartOffset = cartOffsetRepository.findByCartAndTopic(cartId, topic);
        if (cartOffset == null) {
            cartOffset = new CartOffset();
            cartOffset.setCartId(cartId);
            cartOffset.setTopic(topic);
            cartOffset.setPartition(partition);
            cartOffset.setOffset(offset);

            cartOffsetRepository.save(cartOffset);
        }
    }

    @Override
    public Long getOffset(String topic, Integer partition) {
        return cartOffsetRepository.getOffset(topic, partition);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void savePrinted(Long cartId, String topic) {
        logger.info("update printed status ,cartId is: {},topic is: {}.", cartId, topic);
        CartOffset cartOffset = cartOffsetRepository.findByCartAndTopic(cartId, topic);
        if (cartOffset != null && cartOffset.getPrinted() == null) {
            cartOffset.setPrinted(true);
        }
    }
}
