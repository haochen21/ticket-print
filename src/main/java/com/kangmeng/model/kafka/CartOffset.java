package com.kangmeng.model.kafka;

import com.kangmeng.model.Constants;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CARTOFFSET")
public class CartOffset implements Serializable{

	@Id
	protected Long id;

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
