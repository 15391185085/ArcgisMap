package com.ieds.gis.map.test.po;

import javax.persistence.Id;

public class DIS_P_PDKG_L {

	@Id
	private String id;
	private String name;
	private String geometry;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGeometry() {
		return geometry;
	}

	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}

	public DIS_P_PDKG_L() {
		super();
		// TODO Auto-generated constructor stub
	}

}
