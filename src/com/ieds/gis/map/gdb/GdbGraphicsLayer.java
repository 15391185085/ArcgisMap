package com.ieds.gis.map.gdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.text.TextUtils;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.ieds.gis.base.dao.Selector;
import com.ieds.gis.base.test.dao.SqliteDAO;
import com.ieds.gis.base.util.JWD;
import com.ieds.gis.map.bo.SymbolEnum;
import com.ieds.gis.map.util.WktUtil;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.exception.NullArgumentException;
import com.vividsolutions.jts.io.ParseException;

public class GdbGraphicsLayer extends GraphicsLayer {
	private static final int ON_CLICK_TOLERANCE = 30; // 点击-误差范围

	private boolean showElement = false; // 显示要素
	private boolean showLable = false; // 显示标注
	private ILayer myLayer;
	private Map<String, Integer> idGraphicList = new HashMap<String, Integer>();
	public TextLayer tl;
	public Class<IGeometryTable> igtClass;
	private SqliteDAO db;
	private AbsSymobFather iSymobSvg;

	public GdbGraphicsLayer(AbsSymobFather iSymobSvg, Class igtClass,
			ILayer myLayer, SqliteDAO db) {
		super();
		this.db = db;
		this.tl = new TextLayer();
		this.myLayer = myLayer;
		this.igtClass = igtClass;
		this.iSymobSvg = iSymobSvg;
	}

	public void refreshLayer(MapView mv, Polygon refreshP) throws Exception {
		synchronized (GdbGraphicsLayer.class) {
			if (mv == null || refreshP == null) {
				throw new NullArgumentException();
			}
			double labMax = myLayer.getLabel_show_max();
			double labMin = myLayer.getLabel_show_min();
			double gMax = myLayer.getGeneral_show_max();
			double gMin = myLayer.getGeneral_show_min();
			if (mv.getScale() >= gMin && mv.getScale() <= gMax) {
				showElement = true;
			}
			if (mv.getScale() >= labMin && mv.getScale() <= labMax) {
				showLable = true;
			}
			if (!showElement) {
				removeGAll();
			}

			if (!showLable) {
				tl.removeLAll();
			}
			// 查询数据
			Selector selector = Selector.from(igtClass);
			Point middleEarthP = GeometryEngine.getLabelPointForPolygon(
					refreshP, mv.getSpatialReference());

			Polygon pl = (Polygon) WktUtil.getLatLonFromEarth(refreshP, mv);
			selector.where(WhereBuilder.b("Intersects((select "
					+ WktUtil.getWktFromGeometry(pl) + "), geometry)"));
			List<IGeometryTable> currentGeoList = SqliteDAO.getInstance()
					.findAll(selector);
			// 移出缓存外的数据
			removeG(mv, pl, idGraphicList, this);
			removeG(mv, pl, tl.idTextList, tl);
			for (Iterator iterator = currentGeoList.iterator(); iterator
					.hasNext();) {
				IGeometryTable iGeometryTable = (IGeometryTable) iterator
						.next();
				addGeometryTable(iSymobSvg.getTextSymbol(iGeometryTable), mv,
						pl, iterator, middleEarthP);
			}
		}
	}

	/**
	 * 
	 * @param mv
	 * @param pl
	 * @param iterator
	 * @param middleEarthP
	 *            地图的中心
	 * @throws ParseException
	 * @throws Exception
	 * @throws HttpException
	 * @throws NullArgumentException
	 */
	public void addGeometryTable(TextSymbol ts, MapView mv, Polygon pl,
			Iterator iterator, Point middleEarthP) throws ParseException,
			Exception, HttpException, NullArgumentException {
		IGeometryTable geometryTable = (IGeometryTable) iterator.next();

		String wkt = geometryTable.getGeometry();
		if (TextUtils.isEmpty(wkt)) {
			// 位置坐标为空不显示该条记录
			return;
		}
		Geometry latLonGeometry = WktUtil.getGeometryFromWkt(wkt);
		if (GeometryEngine.intersects(pl, latLonGeometry,
				SpatialReference.create(Selector.sr4326))) {
			// 经纬度转大地坐标系
			Geometry earthGeometry = WktUtil.getEarthFromLatLon(latLonGeometry,
					mv);

			// 点在范围内
			if (showElement || idGraphicList.get(geometryTable.getId()) == null) {
				if (idGraphicList.get(geometryTable.getId()) == null) {
					// 添加一个设备
					idGraphicList.put(geometryTable.getId(), this
							.addGraphic(getGeometrySymbol(geometryTable,
									earthGeometry)));
				}
			}

			// 点在范围内
			if (showLable || tl.idTextList.get(geometryTable.getId()) == null) {
				tl.addText(mv, geometryTable, earthGeometry, ts, middleEarthP,
						tl.idTextList);
			}
		}
	}

	public Graphic getGeometrySymbol(IGeometryTable geometryTable,
			Geometry earthGeometry) throws Exception {
		SymbolEnum ss = iSymobSvg.getSymbolStyle(geometryTable);
		switch (ss) {
		case SVG:
			SvnPictureMarkerSymbol svnP = iSymobSvg.getSvnSymbol(geometryTable);
			Polyline svnPL = svnP.getSvgBo().getParseLine(
					((Polyline) earthGeometry).getPoint(0),
					((Polyline) earthGeometry)
							.getPoint(((Polyline) earthGeometry)
									.getPointCount() - 1));
			return new Graphic(svnPL, svnP);
		case CHAR:
			PictureMarkerSymbol pms = iSymobSvg.getCharSymbol(geometryTable);
			return new Graphic(earthGeometry, pms);

		case LINE:
			LineSymbol ls = iSymobSvg.getLineSymbol(geometryTable);
			return new Graphic(earthGeometry, ls);
		}

		return null;
	}

	/**
	 * 写入服务端的角度
	 * 
	 * @param pl
	 * @return
	 */
	public static double writeAngle(Polyline pl) {
		return calcAngle(pl) + 90; // 字符是从左到右，从上到下为0度。
	}

	public static double calcAngle(Polyline pl) {
		Double angle = 0.0;
		Point start = pl.getPoint(0);
		if (start == null) {
			return angle;
		}
		Point end = pl.getPoint(pl.getPointCount() - 1);
		if (end == null) {
			return angle;
		}
		JWD A = new JWD(start.getX(), start.getY());
		JWD B = new JWD(end.getX(), end.getY());
		return JWD.angle(A, B);
	}

	public void removeGAll() {
		Object[] os = idGraphicList.keySet().toArray();
		int[] removes = new int[idGraphicList.size()];
		for (int i = 0; i < os.length; i++) {
			removes[i] = idGraphicList.get(os[i]);
		}
		this.removeGraphics(removes);
		idGraphicList.clear();
	}

	public static void removeG(MapView mv, Polygon pl,
			Map<String, Integer> list, GraphicsLayer gl) {
		List<String> removeList = new ArrayList<String>();
		Set set = list.keySet();
		for (Iterator iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Graphic g = gl.getGraphic(list.get(key));
			Geometry gy = g.getGeometry();
			if (g == null || gy == null || mv == null) {
				removeList.add(key);
				continue;
			}
			Geometry llCurrentG = WktUtil.getLatLonFromEarth(gy, mv);
			if (!GeometryEngine.intersects(pl, llCurrentG,
					SpatialReference.create(Selector.sr4326))) {
				removeList.add(key);
			}
		}
		if (removeList.size() > 0) {
			// 移出缓存外的数据
			int[] removes = new int[removeList.size()];
			for (int i = 0; i < removeList.size(); i++) {
				removes[i] = list.remove(removeList.get(i));
			}
			gl.removeGraphics(removes);
		}
	}

	public void removeAll() {
		removeGAll();
		tl.removeLAll();
	}

	public static WhereBuilder getROWIDs(String tableName, double xmin,
			double xmax, double ymin, double ymax) {
		return WhereBuilder.b("ROWID IN (SELECT a.pkid FROM idx_" + tableName
				+ "_" + Selector.GEOMETRY_FIELD + " a WHERE a.xmin > " + xmin
				+ " AND a.xmax < " + xmax + " AND a.ymin > " + ymin
				+ " AND a.ymax < " + ymax + ")");
	}

}
