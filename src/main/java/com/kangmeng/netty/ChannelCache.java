package com.kangmeng.netty;


import io.netty.channel.ChannelHandlerContext;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ChannelCache {

    INSTANCE;

    private Map<String,ChannelHandlerContext> channelMap = new ConcurrentHashMap<>();

    public void addChannel(String deviceId,ChannelHandlerContext ctx){
        channelMap.put(deviceId,ctx);
    }

    public ChannelHandlerContext getChannel(String deviceId){
        return channelMap.get(deviceId);
    }

    public void removeChannel(String deviceId){
        channelMap.remove(deviceId);
    }
}
