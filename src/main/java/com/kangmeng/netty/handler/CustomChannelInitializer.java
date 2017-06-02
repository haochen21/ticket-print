package com.kangmeng.netty.handler;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;

public class CustomChannelInitializer extends ChannelInitializer<SocketChannel> {

	private EventExecutorGroup eventExecutorGroup;

	private int readTimeOut;

	private ByteBuf delimiter = Unpooled.copiedBuffer("#"
			.getBytes());

	public CustomChannelInitializer(EventExecutorGroup eventExecutorGroup, int readTimeOut) {
		super();
		this.eventExecutorGroup = eventExecutorGroup;
		this.readTimeOut = readTimeOut;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		// 都属于ChannelOutboundHandler，逆序执行
		ch.pipeline().addLast(eventExecutorGroup, new PrintInfoResponse());

		// 都属于ChannelIntboundHandler，按照顺序执行
		ch.pipeline().addLast(new IdleStateHandler(readTimeOut, 0, 0, TimeUnit.SECONDS));
		ch.pipeline().addLast(new ReadTimeOutHandler());
		ch.pipeline().addLast(new DelimiterBasedFrameDecoder(512,
				delimiter));
		ch.pipeline().addLast(new StringDecoder(Charset.forName("GBK")));
		ch.pipeline().addLast(eventExecutorGroup, new BizHandler());

	}

}
