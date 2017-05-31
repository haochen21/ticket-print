package com.kangmeng.netty.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.kangmeng.model.order.Cart;
import com.kangmeng.model.order.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class PrintInfoEncoder extends ChannelOutboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(PrintInfoEncoder.class);

	@Override
	public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String cartJson = (String) obj;
		Cart cart = objectMapper.readValue(cartJson, Cart.class);

		StringBuilder sb = new StringBuilder();
		// 数据起始标示符
		sb.append("&!");
		// 数据分隔符
		sb.append("*");
		// 接受方式
		sb.append("1");
		// 订单号
		sb.append("订单号：").append(cart.getId());
		// 数据分隔符
		sb.append("*");
		for (CartItem cartItem : cart.getCartItems()) {
			// 打印数据时换行符t
			sb.append(cartItem.getProduct()).append("  ").append(cartItem.getQuantity());
			sb.append("*");
		}
		// 数据结束标示符
		sb.append("#");

		ByteBuf byteBuf = Unpooled.buffer();
		byteBuf.writeBytes(sb.toString().getBytes("UTF-8"));

		ctx.write(byteBuf);
	}

}
