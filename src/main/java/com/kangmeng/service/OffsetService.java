package com.kangmeng.service;

public interface OffsetService {

	void saveOffset(Long cartId,Integer partition,Long offset);

	Long getOffset(Integer partition);

	void savePrinted(Long cartId);
}
