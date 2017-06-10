package com.kangmeng.netty;

import io.netty.util.AttributeKey;

public class AttributeMapConstant {

	//deviceId
	public static final AttributeKey<String> NETTY_CHANNEL_KEY = AttributeKey.valueOf("deviceId");

	public static final AttributeKey<Boolean> TIMEOUT_CHANNEL_KEY = AttributeKey.valueOf("timeout");
}
