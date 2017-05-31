package com.kangmeng.netty.handler;

import com.kangmeng.netty.AttributeMapConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;

public class PrintInfoResponse extends ChannelOutboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(PrintInfoResponse.class);

	@Override
	public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(ByteBufUtil.hexDump((ByteBuf) obj));
		}
		ChannelFuture future = ctx.writeAndFlush(obj);
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture f) {
				if (!f.isSuccess()) {
					Attribute<String> attr = ctx.channel().attr(AttributeMapConstant.NETTY_CHANNEL_KEY);
					String gatewayActorKey = attr.get();
					logger.info(gatewayActorKey.toString(), f.cause().getMessage());
					f.channel().close();
				}
			}
		});
	}
}
