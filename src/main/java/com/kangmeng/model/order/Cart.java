package com.kangmeng.model.order;

import com.kangmeng.model.security.Customer;
import com.kangmeng.model.security.Merchant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class Cart implements Serializable {

	protected Long id;

	protected String no;

	protected String name;

	protected String phone;

	protected String address;

	protected Merchant merchant;

	protected Customer customer;

	protected CartStatus status;

	protected Boolean needPay;

	protected BigDecimal takeOutFee;

	protected BigDecimal productPrice;

	protected BigDecimal packageFee;

	protected BigDecimal totalPrice;

	protected Integer payTimeLimit;

	protected Date payTime;

	protected Integer takeTimeLimit;

	protected Date takeTime;

	protected Date takeBeginTime;

	protected Date takeEndTime;

	protected Date createdOn;

	protected Date updatedOn;

	protected String remark;

	protected Boolean takeOut;

	protected Collection<CartItem> cartItems = new ArrayList<CartItem>();

	protected long version;

	protected Boolean cardUsed = true;

	private static final long serialVersionUID = -5938391683195581548L;

	public Cart() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public CartStatus getStatus() {
		return status;
	}

	public void setStatus(CartStatus status) {
		this.status = status;
	}

	public Boolean getNeedPay() {
		return needPay;
	}

	public void setNeedPay(Boolean needPay) {
		this.needPay = needPay;
	}

	public BigDecimal getTakeOutFee() {
		return takeOutFee;
	}

	public void setTakeOutFee(BigDecimal takeOutFee) {
		this.takeOutFee = takeOutFee;
	}

	public BigDecimal getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}

	public BigDecimal getPackageFee() {
		return packageFee;
	}

	public void setPackageFee(BigDecimal packageFee) {
		this.packageFee = packageFee;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Integer getPayTimeLimit() {
		return payTimeLimit;
	}

	public void setPayTimeLimit(Integer payTimeLimit) {
		this.payTimeLimit = payTimeLimit;
	}

	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public Integer getTakeTimeLimit() {
		return takeTimeLimit;
	}

	public void setTakeTimeLimit(Integer takeTimeLimit) {
		this.takeTimeLimit = takeTimeLimit;
	}

	public Date getTakeTime() {
		return takeTime;
	}

	public void setTakeTime(Date takeTime) {
		this.takeTime = takeTime;
	}

	public Date getTakeBeginTime() {
		return takeBeginTime;
	}

	public void setTakeBeginTime(Date takeBeginTime) {
		this.takeBeginTime = takeBeginTime;
	}

	public Date getTakeEndTime() {
		return takeEndTime;
	}

	public void setTakeEndTime(Date takeEndTime) {
		this.takeEndTime = takeEndTime;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Collection<CartItem> getCartItems() {
		return cartItems;
	}

	public void setCartItems(Collection<CartItem> cartItems) {
		this.cartItems = cartItems;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Boolean getCardUsed() {
		return cardUsed;
	}

	public void setCardUsed(Boolean cardUsed) {
		this.cardUsed = cardUsed;
	}

	public Boolean getTakeOut() {
		return takeOut;
	}

	public void setTakeOut(Boolean takeOut) {
		this.takeOut = takeOut;
	}
}