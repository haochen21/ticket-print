package com.kangmeng.model.kafka;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "CARTOFFSET")
public class CartOffset implements Serializable{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Long id;

	@Column(name = "CARTID")
	protected Long cartId;

	@Column(name = "TOPIC")
	protected String topic;

	@Column(name = "KAFKAPARTITION")
	protected Integer partition;

	@Column(name = "OFFSET")
	protected Long offset;

	@Column(name = "PRINTED")
	protected Boolean printed;

	public CartOffset(){

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCartId() {
		return cartId;
	}

	public void setCartId(Long cartId) {
		this.cartId = cartId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Integer getPartition() {
		return partition;
	}

	public void setPartition(Integer partition) {
		this.partition = partition;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public Boolean getPrinted() {
		return printed;
	}

	public void setPrinted(Boolean printed) {
		this.printed = printed;
	}


}
