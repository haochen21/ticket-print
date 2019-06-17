package com.kangmeng.netty.handler;

import com.kangmeng.message.CartMessageService;
import com.kangmeng.message.DeviceMsgListener;
import com.kangmeng.netty.AttributeMapConstant;
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

    private static final String HEARTBEAT = "AS02#";

    private final static Logger logger = LoggerFactory.getLogger(BizHandler.class);

    public BizHandler(CartMessageService cartMessageService) {
        this.cartMessageService = cartMessageService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        String body = (String) msg;

        logger.info("receive msg [{}],channel id is: {}", body, ctx.channel().id().asShortText());
        if (body.endsWith("AS01")) {
            String[] splits = body.split("\\*");
            String deviceId = splits[1];
            Attribute<String> attr = ctx.channel().attr(AttributeMapConstant.NETTY_CHANNEL_KEY);
            if (attr.get() == null) {
                attr.setIfAbsent(deviceId);
            }
            cartMessageService.createMsgListener(deviceId, ctx);

            //心跳
            byte[] bytes = HEARTBEAT.getBytes("GBK");
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeBytes(bytes);
            ctx.write(byteBuf);

        } else if (body.endsWith("AS04")) {
            // 打印机已接收订单
            String[] splits = body.split("\\*");
            String deviceId = splits[1];
            String offsetInfo = splits[2];

            DeviceMsgListener deviceMsgListener = cartMessageService.getDeviceMsgListener(deviceId);
            if (deviceMsgListener != null) {
                deviceMsgListener.addPrintedOffset(offsetInfo);
            }

            String printCommand = "AS38*" + offsetInfo + "*0#";
            //logger.info("command receive,command is: {}", printCommand);
            byte[] bytes = printCommand.getBytes("GBK");
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeBytes(bytes);
            ctx.write(byteBuf);
        } else if (body.endsWith("AS05")) {
            // 打印机已打印订单
            String[] splits = body.split("\\*");
            String offsetInfo = splits[2];

            String printCommand = "AS39*" + offsetInfo + "#";
            //logger.info("command print,command is: {}", printCommand);

            byte[] bytes = printCommand.getBytes("GBK");
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeBytes(bytes);
            ctx.write(byteBuf);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Assert.notNull(ctx, "ChannelHandlerContext must not be null");

        Attribute<String> attr = ctx.channel().attr(AttributeMapConstant.NETTY_CHANNEL_KEY);
        String deviceIdKey = attr.get();
        if (deviceIdKey != null) {
            logger.info("channel inactive,deviceId is {},channel id is: {}", deviceIdKey, ctx.channel().id().asShortText());
            cartMessageService.removeMsgListener(deviceIdKey, ctx);
        }
        ctx.close();
    }
}
