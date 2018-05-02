package com.ieds.gis.map.util;

import java.util.Formatter;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.ieds.gis.base.dao.Selector;
import com.ieds.gis.base.dialog.MyToast;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * 测绘相关
 * 
 * @update 2014-7-31 下午3:18:28<br>
 * @author <a href="mailto:lihaoxiang@ieds.com.cn">李昊翔</a>
 * 
 */
public class WktUtil {
	public static final String UPDATE_LINE = "更新线路";

	/**
	 * 把wkt转换成geometry字段的值
	 * 
	 * @return
	 */
	private static String getGeometryWkt(String wkt) {
		return Selector.GEOM_FROM_TEXT + "('" + wkt + "', " + Selector.sr4326
				+ ")";
	}

	/**
	 * 获取距离米，保留两位小数
	 * 
	 * @param earthP1
	 * @param earthP2
	 * @return
	 */
	public static Double getEarthDistance(Point earthP1, Point earthP2,
			MapView mv) {
		double addMileage = GeometryEngine.geodesicDistance(earthP1, earthP2,
				mv.getSpatialReference(), null);
		return WktUtil.getDouble(addMileage, 2);
	}

	/**
	 * 获取距离米，保留两位小数
	 * 
	 * @param latlonP1
	 * @param latlonP2
	 * @return
	 */
	public static Double getLatLonDistance(Point latlonP1, Point latlonP2) {
		double addMileage = GeometryEngine.geodesicDistance(latlonP1, latlonP2,
				SpatialReference.create(Selector.sr4326), null);
		return WktUtil.getDouble(addMileage, 2);
	}

	/**
	 * 四舍五入到几位小数
	 * 
	 * @param a
	 * @return
	 */
	public static double getDouble(double a, int size) {
		return Double.parseDouble(new Formatter().format("%." + size + "f", a)
				.toString());
	}

	public static void main(String args[]) {
		String s;
		try {
			Point p = new Point(123, 32);
			Polyline pl = new Polyline();
			pl.startPath(p);
			pl.lineTo(p);
			pl.lineTo(p);

			Polygon po = new Polygon();
			po.startPath(p);
			po.lineTo(p);
			po.lineTo(p);
			po.lineTo(p);

			System.out.println(getWktFromGeometry(p));
			System.out.println(getWktFromGeometry(pl));
			System.out.println(getWktFromGeometry(po));

			getGeometryFromWkt("GeomFromText('POINT (123 32)', "
					+ Selector.sr4326 + ")");
			getGeometryFromWkt("GeomFromText('LINESTRING (123 32, 123 32, 123 32)', "
					+ Selector.sr4326 + ")");
			getGeometryFromWkt("GeomFromText('POLYGON ((123 32, 123 32, 123 32, 123 32))', "
					+ Selector.sr4326 + ")");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static String getWktFromGeometry(Geometry geometry) {
		if (geometry == null) {
			throw new NullPointerException();
		}
		com.vividsolutions.jts.geom.Geometry vivGeo = null;
		if (geometry instanceof Point) {
			Point p = (Point) geometry;
			Coordinate cs = new Coordinate(p.getX(), p.getY());
			vivGeo = new com.vividsolutions.jts.geom.Point(cs,
					new PrecisionModel(), Selector.sr4326);
		} else if (geometry instanceof Polyline) {
			Polyline pl = (Polyline) geometry;
			int n = pl.getPointCount();
			Coordinate[] cs = new Coordinate[n];
			for (int i = 0; i < n; i++) {
				Point p = pl.getPoint(i);
				cs[i] = new Coordinate(p.getX(), p.getY());
			}
			vivGeo = new com.vividsolutions.jts.geom.LineString(cs,
					new PrecisionModel(), Selector.sr4326);
		} else if (geometry instanceof Polygon) {
			Polygon pl = (Polygon) geometry;
			int n = pl.getPointCount();
			Coordinate[] cs = new Coordinate[n + 1];
			for (int i = 0; i < n; i++) {
				Point p = pl.getPoint(i);
				cs[i] = new Coordinate(p.getX(), p.getY());
			}
			cs[n] = new Coordinate(pl.getPoint(0).getX(), pl.getPoint(0).getY());

			LinearRing lr = new LinearRing(cs, new PrecisionModel(),
					Selector.sr4326);
			vivGeo = new com.vividsolutions.jts.geom.Polygon(lr,
					new PrecisionModel(), Selector.sr4326);
		}
		if (vivGeo == null) {
			throw new NullPointerException();
		}
		WKTWriter w = new WKTWriter();
		String ws = w.write(vivGeo);
		return getGeometryWkt(ws);
	}

	/**
	 * @param args
	 */
	public static Geometry getGeometryFromWkt(String wkt) throws ParseException {
		if (wkt == null) {
			throw new NullPointerException("查询设备没有位置坐标！");
		}
		Geometry g = null;
		int startN = wkt.indexOf("'");
		int endN = wkt.lastIndexOf("'");
		String wktS = new String(wkt.getBytes(), startN + 1,
				(endN - startN) - 1);
		WKTReader r = new WKTReader();
		if (wkt.indexOf("POINT") != -1) {
			com.vividsolutions.jts.geom.Point p = (com.vividsolutions.jts.geom.Point) r
					.read(wktS);
			g = new Point(p.getX(), p.getY());
		} else if (wkt.indexOf("LINESTRING") != -1) {
			com.vividsolutions.jts.geom.LineString p = (com.vividsolutions.jts.geom.LineString) r
					.read(wktS);
			g = new Polyline();
			Coordinate[] cs = p.getGeometryN(0).getCoordinates();
			for (int i = 0; i < cs.length; i++) {
				if (i == 0) {
					((Polyline) g).startPath(new Point(cs[i].x, cs[i].y));
				} else {
					((Polyline) g).lineTo(new Point(cs[i].x, cs[i].y));
				}
			}
		} else if (wkt.indexOf("POLYGON") != -1) {
			com.vividsolutions.jts.geom.Polygon p = (com.vividsolutions.jts.geom.Polygon) r
					.read(wktS);
			g = new Polygon();
			Coordinate[] cs = p.getGeometryN(0).getCoordinates();
			for (int i = 0; i < cs.length; i++) {
				if (i == 0) {
					((Polygon) g).startPath(new Point(cs[i].x, cs[i].y));
				} else {
					((Polygon) g).lineTo(new Point(cs[i].x, cs[i].y));
				}
			}
		}
		if (g == null) {
			throw new NullPointerException();
		}
		return g;
	}

	/**
	 * 从大地转经纬
	 * 
	 * @param ptLatLon
	 * @param map
	 * @return
	 */
	public static final Geometry getLatLonFromEarth(Geometry earthG, MapView map) {
		return GeometryEngine.project(earthG, map.getSpatialReference(),
				SpatialReference.create(Selector.sr4326));
	}

	/**
	 * 从经纬转大地
	 * 
	 * @param ptLatLon
	 * @param map
	 * @return
	 */
	public static final Geometry getEarthFromLatLon(Geometry latLonG, MapView map) {
		return GeometryEngine.project(latLonG,
				SpatialReference.create(Selector.sr4326),
				map.getSpatialReference());

	}

	/**
	 * 得到移动后的位置
	 * 
	 * @param moveX
	 * @param moveY
	 * @param geoP
	 * @return
	 */
	public static Geometry getMoveGeometry(double moveX, double moveY,
			Geometry geoP) {
		if (geoP instanceof Point) {
			Point p = (Point) geoP;
			geoP = new Point(p.getX() + moveX, p.getY() + moveY);
		} else if (geoP instanceof Polyline) {
			Polyline pl = (Polyline) geoP;
			Polyline newPl = new Polyline();
			int n = pl.getPointCount();
			for (int i = 0; i < n; i++) {
				Point p = pl.getPoint(i);
				p = new Point(p.getX() + moveX, p.getY() + moveY);
				if (i == 0) {
					newPl.startPath(p);
				} else {
					newPl.lineTo(p);
				}
			}
			geoP = newPl;
		} else if (geoP instanceof Polygon) {
			Polygon pl = (Polygon) geoP;
			Polygon newPl = new Polygon();
			int n = pl.getPointCount();
			for (int i = 0; i < n; i++) {
				Point p = pl.getPoint(i);
				p = new Point(p.getX() + moveX, p.getY() + moveY);
				if (i == 0) {
					newPl.startPath(p);
				} else {
					newPl.lineTo(p);
				}
			}
			geoP = newPl;
		}
		return geoP;
	}

	public static Point getMiddle(Point A, Point B) {
		return new Point((A.getX() + B.getX()) / 2, (A.getY() + B.getY()) / 2);
	}
}
