package com.kangmeng.netty;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public enum ChannelCache {

    INSTANCE;

    private Map<String, List<ChannelHandlerContext>> channelMap = new ConcurrentHashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(ChannelCache.class);

    public void addChannel(String deviceId, ChannelHandlerContext ctx) {
        channelMap.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>())
                .add(ctx);
        logger.info("add channel context,deviceId is: {}, channel is: {} ", deviceId, ctx.channel().id().asShortText());
    }

    public List<ChannelHandlerContext> getChannel(String deviceId) {
        return channelMap.getOrDefault(deviceId, new Vector<>());
    }

    public void removeChannel(String deviceId, ChannelHandlerContext ctx) {
        channelMap.get(deviceId).remove(ctx);
        logger.info("remove channel context,deviceId is: {}, channel is: {} ", deviceId, ctx.channel().id().asShortText());
    }
}
