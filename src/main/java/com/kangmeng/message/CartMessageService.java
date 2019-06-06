package com.kangmeng.message;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CartMessageService {

    @Autowired
    private Properties consumerProperties;

    // key: deviceId
    private Map<String, DeviceMsgListener> deviceChannelMap = new ConcurrentHashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(CartMessageService.class);

    public CartMessageService() {

    }

    public void createMsgListener(String deviceId, ChannelHandlerContext ctx) {
        if (deviceChannelMap.containsKey(deviceId)) {
            if (!deviceChannelMap.get(deviceId).getCtx().equals(ctx)) {
                deviceChannelMap.get(deviceId).shutdown();
                deviceChannelMap.remove(deviceId);
                create(deviceId, ctx);
            }
        } else {
            create(deviceId, ctx);
        }

    }

    public void removeMsgListener(String deviceId, ChannelHandlerContext ctx) {
        if (deviceChannelMap.containsKey(deviceId)
                && deviceChannelMap.get(deviceId).getCtx().equals(ctx)) {
            deviceChannelMap.get(deviceId).shutdown();
            deviceChannelMap.remove(deviceId);
            logger.info("remove message listener,deviceId is {},channel id is: {}", deviceId, ctx.channel().id().asShortText());
        }
    }

    public DeviceMsgListener getDeviceMsgListener(String deviceId) {
        if (deviceChannelMap.containsKey(deviceId)) {
            return deviceChannelMap.get(deviceId);
        }
        return null;
    }

    private void create(String deviceId, ChannelHandlerContext ctx) {
        logger.info("online,deviceId is {},channel id is: {}", deviceId, ctx.channel().id().asShortText());
        deviceChannelMap.computeIfAbsent(deviceId, k -> {
            DeviceMsgListener listener = new DeviceMsgListener(ctx, consumerProperties, deviceId);
            logger.info("add message listener,deviceId is {},channel id is: {}", deviceId, ctx.channel().id().asShortText());
            return listener;
        }).start();
    }

}
