package com.kangmeng.repository;

import com.kangmeng.model.kafka.CartOffset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartOffsetRepository extends JpaRepository<CartOffset, Long>,CartOffsetRepositoryCustom {
}
