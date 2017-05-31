package com.kangmeng.model.order.store;

import java.io.Serializable;

public class Product implements Serializable {

	protected Long id;

	protected String name;

	private static final long serialVersionUID = 3277060162706927687L;

	public Product() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
