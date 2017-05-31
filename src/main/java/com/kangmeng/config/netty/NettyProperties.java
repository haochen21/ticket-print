package com.kangmeng.config.netty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {

	private int port;

	private boolean keepAlive;

	private int backLog;

	private int readTimeOut;

	private int eventExecutor;

	private int bossThreadCount;

	private int workThreadCount;

	public NettyProperties() {

	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public int getBackLog() {
		return backLog;
	}

	public void setBackLog(int backLog) {
		this.backLog = backLog;
	}

	public int getReadTimeOut() {
		return readTimeOut;
	}

	public void setReadTimeOut(int readTimeOut) {
		this.readTimeOut = readTimeOut;
	}

	public int getEventExecutor() {
		return eventExecutor;
	}

	public void setEventExecutor(int eventExecutor) {
		this.eventExecutor = eventExecutor;
	}

	public int getBossThreadCount() {
		return bossThreadCount;
	}

	public void setBossThreadCount(int bossThreadCount) {
		this.bossThreadCount = bossThreadCount;
	}

	public int getWorkThreadCount() {
		return workThreadCount;
	}

	public void setWorkThreadCount(int workThreadCount) {
		this.workThreadCount = workThreadCount;
	}
}
