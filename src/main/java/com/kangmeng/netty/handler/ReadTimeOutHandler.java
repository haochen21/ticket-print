package com.kangmeng.netty.handler;


import com.kangmeng.netty.AttributeMapConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;

public class ReadTimeOutHandler extends ChannelInboundHandlerAdapter {

    private final static Logger logger = LoggerFactory.getLogger(ReadTimeOutHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                Attribute<String> attr = ctx.channel().attr(AttributeMapConstant.NETTY_CHANNEL_KEY);
                String deviceId = attr.get();
                logger.info("add message listener,deviceId is {},channel id is: {}", deviceId, ctx.channel().id().asShortText());
                ctx.close();
            }
        }
    }
}
