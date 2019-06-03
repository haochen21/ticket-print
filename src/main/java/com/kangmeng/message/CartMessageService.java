package com.kangmeng.message;

import com.kangmeng.service.OffsetService;
import io.netty.channel.ChannelHandlerContext;
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

    // key: deviceId+"~"+channel.id
    private Map<String, DeviceMsgListener> deviceMsgThreadMap = new HashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(CartMessageService.class);

    public CartMessageService() {

    }

    public void createMsgListener(String deviceId, ChannelHandlerContext ctx) {
        String key = deviceId + "~" + ctx.channel().id().asShortText();
        deviceMsgThreadMap.computeIfAbsent(key, k -> {
            DeviceMsgListener listener = new DeviceMsgListener(consumerProperties, deviceId, offsetService);
            listener.setName(key);
            logger.info("add message listener,deviceId is {},channel id is: {}", deviceId, ctx.channel().id().asShortText());
            return listener;
        }).start();
    }

    public void removeMsgListener(String deviceId, ChannelHandlerContext ctx) {
        String key = deviceId + "~" + ctx.channel().id().asShortText();
        DeviceMsgListener listener = deviceMsgThreadMap.remove(key);
        if (listener != null) {
            deviceMsgThreadMap.get(deviceId).shutdown();
        }
        logger.info("remove message listener,deviceId is {},channel id is: {}", deviceId, ctx.channel().id().asShortText());
    }

}
