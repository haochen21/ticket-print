package com.kangmeng.message;

import com.kangmeng.service.OffsetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class CartMessageService {

	@Autowired
	private Properties consumerProperties;

	@Autowired
	private OffsetService offsetService;

	private Map<String, DeviceMsgListener> deviceMsgThreadMap = new HashMap<>();

	private final static Logger logger = LoggerFactory.getLogger(CartMessageService.class);

	public CartMessageService() {

	}

	public void createMsgListener(String deviceId) {
		if (!deviceMsgThreadMap.containsKey(deviceId)) {
			DeviceMsgListener listener = new DeviceMsgListener(consumerProperties, deviceId, offsetService);
			listener.setName("deviceId");
			deviceMsgThreadMap.put(deviceId, listener);
			logger.info("add message listener,deviceId is {}.", deviceId);
			listener.start();
		}
	}

	public void removeMsgListener(String deviceId) {
		if (deviceMsgThreadMap.containsKey(deviceId)) {
			deviceMsgThreadMap.get(deviceId).shutdown();
			deviceMsgThreadMap.remove(deviceId);
			logger.info("remove message listener,deviceId is {}.", deviceId);
		}
	}

}
