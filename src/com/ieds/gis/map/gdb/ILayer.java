package com.ieds.gis.map.gdb;

import javax.persistence.Transient;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Blob;
import java.io.Serializable;

public interface ILayer {

	public String getTable_name();

	public Double getGeneral_show_max();

	public Double getGeneral_show_min();

	public Double getLabel_show_max();

	public Double getLabel_show_min();

	public Integer getLable_enable();

}
