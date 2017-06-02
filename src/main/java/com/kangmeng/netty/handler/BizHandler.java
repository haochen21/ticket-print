package com.kangmeng.netty.handler;

import com.kangmeng.GlobalContext;
import com.kangmeng.message.CartMessageService;
import com.kangmeng.netty.AttributeMapConstant;
import com.kangmeng.netty.ChannelCache;
import com.kangmeng.repository.CartOffsetRepository;
import com.kangmeng.service.OffsetService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class BizHandler extends ChannelInboundHandlerAdapter {

	private CartMessageService cartMessageService;

	private OffsetService offsetService;

	private static final String HEARTBEAT = "AS02#";

	private final static Logger logger = LoggerFactory.getLogger(BizHandler.class);

	public BizHandler(CartMessageService cartMessageService,OffsetService offsetService){
		this.cartMessageService = cartMessageService;
		this.offsetService = offsetService;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		String body = (String) msg;
		logger.info("receive msg : ["
				+ body + "]");
		if(body.endsWith("AS01")){
			Attribute<String> attr = ctx.channel().attr(AttributeMapConstant.NETTY_CHANNEL_KEY);
			String deviceKey = attr.get();
			// 设备第一次上线
			if (deviceKey == null) {
				String[] splits = body.split("\\*");
				String deviceId = splits[1];
				logger.info("device {} first login",deviceId);
				attr.setIfAbsent(deviceId);

				ChannelCache.INSTANCE.addChannel(deviceId,ctx);

				cartMessageService.createMsgListener(deviceId);

				//心跳
				String command = HEARTBEAT+"AS48*1#";
				byte[] bytes = command.getBytes("GBK");
				ByteBuf byteBuf = Unpooled.buffer();
				byteBuf.writeBytes(bytes);
				ctx.write(byteBuf);
			}
			//心跳
			byte[] bytes = HEARTBEAT.getBytes("GBK");
			ByteBuf byteBuf = Unpooled.buffer();
			byteBuf.writeBytes(bytes);
			ctx.write(byteBuf);

		}else if(body.endsWith("AS04")){
			// 打印机已接收订单
			String[] splits = body.split("\\*");
			String cardId = splits[2];
			String printCommand = "AS38*"+cardId+"*0#";
			logger.info("response receive,command is: {}", printCommand);
			byte[] bytes = printCommand.getBytes("GBK");
			ByteBuf byteBuf = Unpooled.buffer();
			byteBuf.writeBytes(bytes);
			ctx.write(byteBuf);
		}else if(body.endsWith("AS05")){
			// 打印机已打印订单
			String[] splits = body.split("\\*");
			String orderId = splits[2];

			String[] cartIds = orderId.split(":");
			Long cartId = Long.parseLong(cartIds[1]);
			offsetService.savePrinted(cartId);

			String printCommand = "AS39*"+orderId+"#";
			logger.info("response print,command is: {}", printCommand);

			byte[] bytes = printCommand.getBytes("GBK");
			ByteBuf byteBuf = Unpooled.buffer();
			byteBuf.writeBytes(bytes);
			ctx.write(byteBuf);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Assert.notNull(ctx, "ChannelHandlerContext must not be null");

		Attribute<String> attr = ctx.channel().attr(AttributeMapConstant.NETTY_CHANNEL_KEY);
		String deviceIdKey = attr.get();
		if (deviceIdKey != null) {
			logger.info("device {} link is offline",deviceIdKey);
			ChannelCache.INSTANCE.removeChannel(deviceIdKey);
			cartMessageService.removeMsgListener(deviceIdKey);
		}
	}
}
