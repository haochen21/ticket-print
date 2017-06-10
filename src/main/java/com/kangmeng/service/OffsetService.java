package com.kangmeng.service;

public interface OffsetService {

	void saveOffset(Long cartId,String topic,Integer partition,Long offset);

	Long getOffset(String topic,Integer partition);

	void savePrinted(Long cartId);
}
