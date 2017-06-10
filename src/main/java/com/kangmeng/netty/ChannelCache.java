package com.kangmeng.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ChannelCache {

    INSTANCE;

    private Map<String,ChannelHandlerContext> channelMap = new ConcurrentHashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(ChannelCache.class);

    public void addChannel(String deviceId,ChannelHandlerContext ctx){
        channelMap.put(deviceId,ctx);
    }

    public ChannelHandlerContext getChannel(String deviceId){
        return channelMap.get(deviceId);
    }

    public boolean removeChannel(String deviceId){
        ChannelHandlerContext ctx = channelMap.get(deviceId);
        if(ctx != null) {
            Attribute<Boolean> timeoutAttr = ctx.channel().attr(AttributeMapConstant.TIMEOUT_CHANNEL_KEY);
            boolean timeout = timeoutAttr.get();
            logger.info("{} timeout status is : {}",deviceId,timeout);
            if (timeout) {
                channelMap.remove(deviceId);
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
}
