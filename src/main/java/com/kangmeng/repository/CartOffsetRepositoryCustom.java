package com.kangmeng.repository;

public interface CartOffsetRepositoryCustom {

	Long getOffset(String topic,Integer partition);
}
