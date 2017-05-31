package com.kangmeng.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientServer {
	private final String host;

	private final int port;

	private SocketChannel socketChannel;

	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);

	private static final String HEARTBEAT = "A*设备ID*0*AS01#";

	private final static Logger logger = LoggerFactory.getLogger(ClientServer.class);

	public ClientServer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();

		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host, port))
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new ClientHandler());
					}
				});
		ChannelFuture future = bootstrap.connect(host, port).sync();
		if (future.isSuccess()) {
			socketChannel = (SocketChannel) future.channel();
			System.out.println("connect server success---------");
		}

	}

	public void hearBeat() {
		try {
			byte[] bytes = HEARTBEAT.getBytes("UTF-8");
			scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
				public void run() {
					ByteBuf byteBuf = Unpooled.buffer();
					byteBuf.writeBytes(bytes);
					socketChannel.writeAndFlush(byteBuf);
				}
			}, 2,1, TimeUnit.SECONDS);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// String host = "172.17.60.210";
		String host = "127.0.0.1";
		int port = Integer.parseInt("9000");
		ClientServer server = new ClientServer(host, port);
		try {
			server.start();
			// 心跳
			server.hearBeat();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
