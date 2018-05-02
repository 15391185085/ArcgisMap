package com.ieds.gis.map.test.unit;

import java.util.Map;

import com.esri.android.map.MapView;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.ieds.gis.map.bo.SymbolEnum;
import com.ieds.gis.map.gdb.IGeometryTable;
import com.ieds.gis.map.gdb.AbsSymobFather;
import com.ieds.gis.map.gdb.SvnPictureMarkerSymbol;

public class MySymobSvg extends AbsSymobFather {

	@Override
	public SymbolEnum getSymbolStyle(IGeometryTable geometryTable)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SvnPictureMarkerSymbol getSvnSymbol(IGeometryTable geometryTable)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PictureMarkerSymbol getCharSymbol(IGeometryTable geometryTable)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LineSymbol getLineSymbol(IGeometryTable geometryTable)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextSymbol getTextSymbol(IGeometryTable igt) {
		// TODO Auto-generated method stub
		return null;
	}

	public MySymobSvg(Map<String, String> svnMap, MapView mv) throws Exception {
		super(svnMap, mv);
		// TODO Auto-generated constructor stub
	}

 
}
