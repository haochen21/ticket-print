package com.kangmeng.repository;

import com.kangmeng.model.kafka.CartOffset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartOffsetRepository extends JpaRepository<CartOffset, Long>,CartOffsetRepositoryCustom {

    @Query(value = "select c from CartOffset c where c.cartId = :cartId and c.topic = :topic")
    CartOffset findByCartAndTopic(@Param("cartId") Long cartId,@Param("topic") String topic);
}
