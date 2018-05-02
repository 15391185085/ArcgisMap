package com.ieds.gis.map.test.po;

import javax.persistence.Id;

public class SYS_SYMBOL {

	@Id
	private String id;
	private String content;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public SYS_SYMBOL() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
