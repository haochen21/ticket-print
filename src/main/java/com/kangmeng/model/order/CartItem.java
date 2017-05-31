package com.kangmeng.model.order;

import com.kangmeng.model.order.store.Product;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {

	protected Long id;

	protected String name;

	protected Integer quantity;

	protected BigDecimal unitPrice;

	protected BigDecimal totalPrice;

	protected Product product;

	private static final long serialVersionUID = 6852793237053469465L;

	public CartItem() {

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

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
}
