package com.kangmeng.netty.handler;

import com.kangmeng.netty.AttributeMapConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BizHandler extends ChannelInboundHandlerAdapter {

	private static final String HEARTBEAT = "AS02#";

	private final static Logger logger = LoggerFactory.getLogger(BizHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		String body = (String) msg;
		if(body.endsWith("AS01")){
			Attribute<String> attr = ctx.channel().attr(AttributeMapConstant.NETTY_CHANNEL_KEY);
			String gatewayActorKey = attr.get();
			// 设备第一次上线
			if (gatewayActorKey == null) {
				String[] splits = body.split("\\*");
				String deviceId = splits[1];
				logger.info("device {} first login",deviceId);
				gatewayActorKey = "merchant" + "-" + deviceId;
				attr.setIfAbsent(gatewayActorKey);
			}
			//心跳
			byte[] bytes = HEARTBEAT.getBytes("UTF-8");
			ByteBuf byteBuf = Unpooled.buffer();
			byteBuf.writeBytes(bytes);
			ctx.write(byteBuf);
			logger.info("receive heart beat : ["
					+ body + "]");
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();// 发生异常，关闭链路
	}
}
