package com.kangmeng.model.security;

import java.io.Serializable;

public class Merchant implements Serializable {

	protected Long id;

	protected String loginName;

	protected String name;

	protected String deviceNo;

	protected String phone;

	protected Boolean printVoice;

	private static final long serialVersionUID = -1573726069064463313L;

	public Merchant() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}

	public Boolean getPrintVoice() {
		return printVoice;
	}

	public void setPrintVoice(Boolean printVoice) {
		this.printVoice = printVoice;
	}
}
