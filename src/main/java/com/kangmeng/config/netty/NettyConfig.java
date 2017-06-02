package com.kangmeng.config.netty;

import com.kangmeng.message.CartMessageService;
import com.kangmeng.netty.handler.CustomChannelInitializer;
import com.kangmeng.service.OffsetService;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class NettyConfig {

	@Autowired
	private NettyProperties nettyProperties;

	@Autowired
	private CartMessageService cartMessageService;

	@Autowired
	private OffsetService offsetService;

	public NettyConfig() {

	}

	// 处理客户端的TCP连接请求,如果系统只有一个服务端端口需要监听，则建议bossGroup线程组线程数设置为1
	@Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup bossGroup() {
		return new NioEventLoopGroup(nettyProperties.getBossThreadCount());
	}

	// 负责I/O读写操作的线程组，通过ServerBootstrap的group方法进行设置，用于后续的Channel绑定
	@Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup workerGroup() {
		return new NioEventLoopGroup(nettyProperties.getWorkThreadCount());
	}

	@Bean(name = "tcpChannelOptions")
	public Map<ChannelOption<?>, Object> tcpChannelOptions() {
		Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
		options.put(ChannelOption.SO_KEEPALIVE, nettyProperties.isKeepAlive());
		options.put(ChannelOption.SO_BACKLOG, nettyProperties.getBackLog());
		return options;
	}

	@Bean(name = "tcpSocketAddress")
	public InetSocketAddress tcpPort() {
		return new InetSocketAddress(nettyProperties.getPort());
	}

	@Autowired
	@Qualifier("customChannelInitializer")
	private CustomChannelInitializer customChannelInitializer() {
		final EventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(nettyProperties.getEventExecutor());
		return new CustomChannelInitializer(eventExecutorGroup, cartMessageService, offsetService,
				nettyProperties.getReadTimeOut());
	}

	@SuppressWarnings("unchecked")
	@Bean(name = "serverBootstrap")
	public ServerBootstrap bootstrap() {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup(), workerGroup()).channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(customChannelInitializer());
		Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
		Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
		for (@SuppressWarnings("rawtypes")
		ChannelOption option : keySet) {
			b.option(option, tcpChannelOptions.get(option));
		}
		return b;
	}
}
