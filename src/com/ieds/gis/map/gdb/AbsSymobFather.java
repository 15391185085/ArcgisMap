package com.ieds.gis.map.gdb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.esri.android.map.MapView;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.ieds.gis.map.bo.SymbolEnum;
import com.ieds.gis.map.svgtoline.SvgBo;

/**
 * svg的组装接口
 * 
 * @author lihx
 * 
 */
public abstract class AbsSymobFather {
	private Map<String, SvnPictureMarkerSymbol> symbolMap = new HashMap<String, SvnPictureMarkerSymbol>();
	private MapView mv;

	public Map<String, SvnPictureMarkerSymbol> getSymbolMap() {
		return symbolMap;
	}


	public MapView getMv() {
		return mv;
	}


	/**
	 * 将string转成svg的样式 Map<String, String> String 过滤条件， String svg的值
	 * 
	 * @param svnMap
	 * @throws Exception
	 */
	public AbsSymobFather(Map<String, String> svnMap, MapView mv) throws Exception {
		super();
		this.mv = mv;
		Set s = svnMap.keySet();
		for (Iterator iterator = s.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			String svn = svnMap.get(key);
			SvgBo sb = new SvgBo(svn.getBytes(), mv);
			SvnPictureMarkerSymbol svnP = new SvnPictureMarkerSymbol(sb);
			symbolMap.put(key, svnP);
		}
	}


	public abstract SymbolEnum getSymbolStyle(IGeometryTable geometryTable)
			throws Exception;

	public abstract SvnPictureMarkerSymbol getSvnSymbol(
			IGeometryTable geometryTable) throws Exception;

	public abstract PictureMarkerSymbol getCharSymbol(
			IGeometryTable geometryTable) throws Exception;

	public abstract TextSymbol getTextSymbol(IGeometryTable igt);

	public abstract LineSymbol getLineSymbol(IGeometryTable geometryTable)
			throws Exception;
	
}
