package com.ieds.gis.map.gdb;

import java.io.Serializable;
import java.util.Date;

public interface IGeometryTable extends Serializable {

	public String getId();

	public void setId(String id);

	public String getName();

	public void setName(String name);

	public String getGeometry();

	public void setGeometry(String Geometry);

}
