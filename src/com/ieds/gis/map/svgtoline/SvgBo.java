package com.ieds.gis.map.svgtoline;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Transformation2D;
import com.ieds.gis.base.dao.Selector;
import com.ieds.gis.base.util.JWD;
import com.ieds.gis.map.util.WktUtil;

/**
 * Created on 2017/6/1 15:24
 * 
 * @author PengZee
 */

public class SvgBo {

	private MapView mv;
	float w;
	float h;
	List<Polyline> svgBoList = new ArrayList<Polyline>();
	private double radiusY;

	public Polyline getParseLine(Point p1, Point p2) throws Exception {
		// 找到左下角的位置开始绘制真实比例
		double meter = GeometryEngine
				.distance(p1, p2, mv.getSpatialReference());
		Point m = WktUtil.getMiddle(p1, p2);
		double wr = meter / w;
		double hr = meter / h;

		double mx = m.getX() - meter / 2;
		double my = m.getY() - meter / 2;

		Polyline pl = new Polyline();

		int size = svgBoList.size();
		for (int j = 0; j < size; j++) {
			Polyline newP = new Polyline();
			Polyline svgPl = (Polyline) svgBoList.get(j);
			int n = svgPl.getPointCount();
			for (int i = 0; i < n; i++) {
				Point b = svgPl.getPoint(i);
				if (i == 0) {
					newP.startPath(mx + b.getX() * wr, my + b.getY() * hr);
				} else {
					newP.lineTo(mx + b.getX() * wr, my + b.getY() * hr);
				}
			}
			pl.add(newP, false);
		}
		Transformation2D td = new Transformation2D();
		p1 = (Point) WktUtil.getLatLonFromEarth(p1, mv);
		p2 = (Point) WktUtil.getLatLonFromEarth(p2, mv);
		JWD A = new JWD(p1.getX(), p1.getY());
		JWD B = new JWD(p2.getX(), p2.getY());

		double d = 90 - JWD.angle(A, B);
		td.rotate(Math.cos(2 * Math.PI / 360 * d),
				Math.sin(2 * Math.PI / 360 * d), m);
		pl.applyTransformation(td);
		return pl;
	}

	public SvgBo(byte[] bs, MapView mv) throws Exception {
		this.mv = mv;
		SVGPath svg = new SVGPath(mv);
		ByteArrayInputStream is = new ByteArrayInputStream(bs);
		// svg从左下角开始绘制
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(is);
		NodeList svgNodeList = document.getElementsByTagName("svg");
		if (svgNodeList.getLength() == 1) {
			Element element = (Element) svgNodeList.item(0);
			w = Float.valueOf(element.getAttribute("width"));
			h = Float.valueOf(element.getAttribute("height"));

			NodeList lineList = document.getElementsByTagName("line");
			for (int i = 0; i < lineList.getLength(); i++) {
				element = (Element) lineList.item(i);
				Polyline p = new Polyline();
				// 重算比例
				Double x1 = Double.valueOf(element.getAttribute("x1"));
				Double y1 = Double.valueOf(element.getAttribute("y1"));
				Double x2 = Double.valueOf(element.getAttribute("x2"));
				Double y2 = Double.valueOf(element.getAttribute("y2"));
				p.startPath(x1, y1);
				p.lineTo(x2, y2);
				svgBoList.add(p);
			}
			lineList = document.getElementsByTagName("rect");
			for (int i = 0; i < lineList.getLength(); i++) {
				element = (Element) lineList.item(i);
				Polyline p = new Polyline();
				// 重算比例
				Double x1 = Double.valueOf(element.getAttribute("x"));
				Double y1 = Double.valueOf(element.getAttribute("y"));
				Double x2 = Double.valueOf(element.getAttribute("width"));
				Double y2 = Double.valueOf(element.getAttribute("height"));
				p.startPath(x1, y1);
				p.lineTo(x1 + x2, y1);
				p.lineTo(x1 + x2, y1 + y2);
				p.lineTo(x1, y1 + y2);
				p.lineTo(x1, y1);
				svgBoList.add(p);
			}
			lineList = document.getElementsByTagName("ellipse");
			for (int i = 0; i < lineList.getLength(); i++) {
				element = (Element) lineList.item(i);
				// 重算比例
				Float centerX = Float.valueOf(element.getAttribute("cx"));
				Float centerY = Float.valueOf(element.getAttribute("cy"));
				Float radiusX = Float.valueOf(element.getAttribute("rx"));
				Float radiusY = Float.valueOf(element.getAttribute("ry"));
				Polyline p = (Polyline) GeometryEngine.geodesicEllipse(
						new Point(centerX, centerY), mv.getSpatialReference(),
						radiusX, radiusY, 0, 10, null, Geometry.Type.POLYLINE);
				svgBoList.add(p);
			}
			lineList = document.getElementsByTagName("path");
			for (int i = 0; i < lineList.getLength(); i++) {
				element = (Element) lineList.item(i);
				// 重算比例
				String d = String.valueOf(element.getAttribute("d"));
				List<Polyline> pList = svg.doPath(d);
				svgBoList.addAll(pList);
			}

		}
	}

}
