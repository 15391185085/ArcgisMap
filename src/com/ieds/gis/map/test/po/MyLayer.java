package com.ieds.gis.map.test.po;

import javax.persistence.Id;

import com.ieds.gis.map.gdb.ILayer;

public class MyLayer implements ILayer {

	private String table_name;

	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	public String getTable_name() {
		return table_name;
	}

	private Double general_show_max;

	public void setGeneral_show_max(Double general_show_max) {
		this.general_show_max = general_show_max;
	}

	public Double getGeneral_show_max() {
		return general_show_max;
	}

	private Double general_show_min;

	public void setGeneral_show_min(Double general_show_min) {
		this.general_show_min = general_show_min;
	}

	public Double getGeneral_show_min() {
		return general_show_min;
	}

	private Double label_show_max;

	public void setLabel_show_max(Double label_show_max) {
		this.label_show_max = label_show_max;
	}

	public Double getLabel_show_max() {
		return label_show_max;
	}

	private Double label_show_min;

	public void setLabel_show_min(Double label_show_min) {
		this.label_show_min = label_show_min;
	}

	public Double getLabel_show_min() {
		return label_show_min;
	}

	private Integer lable_enable;

	public void setLable_enable(Integer lable_enable) {
		this.lable_enable = lable_enable;
	}

	public Integer getLable_enable() {
		return lable_enable;
	}

	public MyLayer(String table_name, Double general_show_max,
			Double general_show_min, Double label_show_max,
			Double label_show_min, Integer lable_enable) {
		super();
		this.table_name = table_name;
		this.general_show_max = general_show_max;
		this.general_show_min = general_show_min;
		this.label_show_max = label_show_max;
		this.label_show_min = label_show_min;
		this.lable_enable = lable_enable;
	}

}
