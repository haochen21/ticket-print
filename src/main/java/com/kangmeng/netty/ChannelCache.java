package com.kangmeng.netty;


import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public enum ChannelCache {

    INSTANCE;

    private Map<String,ChannelHandlerContext> channelMap = new HashMap<>();

    public void addChannel(String deviceId,ChannelHandlerContext ctx){
        channelMap.put(deviceId,ctx);
    }

    public ChannelHandlerContext getChannel(String deviceId){
        return channelMap.get(deviceId);
    }
}
