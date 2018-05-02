package com.ieds.gis.map.gdb;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.symbol.TextSymbol.HorizontalAlignment;
import com.esri.core.symbol.TextSymbol.VerticalAlignment;
import com.ieds.gis.map.util.WktUtil;

public class TextLayer extends GraphicsLayer {
	private static final int MAX_NAME_SIZE = 7;
	private static final int ON_SHOW_TOLERANCE = 80; // 显示-误差范围
	public Map<String, Integer> idTextList = new HashMap<String, Integer>();

	/**
	 * 
	 */
	public void removeLAll() {
		Object[] os = idTextList.keySet().toArray();
		int[] removes = new int[idTextList.size()];
		for (int i = 0; i < os.length; i++) {
			removes[i] = idTextList.get(os[i]);
		}
		this.removeGraphics(removes);
		idTextList.clear();
	}

	public void addText(MapView mv, IGeometryTable geometryTable,
			Geometry earthGeometry, TextSymbol ts, Point middleEarthP,
			Map<String, Integer> idTextList) {
		StringBuilder task = new StringBuilder();
		ts.setText(getEquipName(task.toString()));

		Point mapP = null;
		if (earthGeometry instanceof Point) {
			ts.setHorizontalAlignment(HorizontalAlignment.LEFT);
			ts.setVerticalAlignment(VerticalAlignment.TOP);
			mapP = (Point) earthGeometry;
		} else if (earthGeometry instanceof Polygon) {
			ts.setHorizontalAlignment(HorizontalAlignment.LEFT);
			ts.setVerticalAlignment(VerticalAlignment.BOTTOM);
			mapP = GeometryEngine.getLabelPointForPolygon(
					((Polygon) earthGeometry), mv.getSpatialReference());
		} else if (earthGeometry instanceof Polyline) {
			ts.setHorizontalAlignment(HorizontalAlignment.RIGHT);
			ts.setVerticalAlignment(VerticalAlignment.BOTTOM);
			mapP = WktUtil.getMiddle(((Polyline) earthGeometry).getPoint(0),
					((Polyline) earthGeometry)
							.getPoint(((Polyline) earthGeometry)
									.getPointCount() - 1));
		}
		Point screenPoint = mv.toScreenPoint(mapP);
		int sign = -1;
		if (screenPoint != null) {
			int[] graphicIds = this.getGraphicIDs((float) screenPoint.getX(),
					(float) screenPoint.getY(), ON_SHOW_TOLERANCE);
			sign = graphicIds.length;
		}
		if (sign == 0) {
			// 该位置没有设备
			if (idTextList.get(geometryTable.getId()) == null) {
				// 添加一个设备
				idTextList.put(geometryTable.getId(),
						this.addGraphic(new Graphic(mapP, ts)));
			} else {
				// 更新位置
				this.updateGraphic(idTextList.get(geometryTable.getId()),
						new Graphic(mapP, ts));
			}
		}
		if (sign > 1) {// 原来的位置已经有数据了，
			if (idTextList.get(geometryTable.getId()) != null) {
				this.removeGraphic(idTextList.remove(geometryTable.getId()));
			}
		}
	}

	public static String getEquipName(String string) {
		int n = 0;
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char a = string.charAt(i);
			if (a == '[') {
				n = MAX_NAME_SIZE;
			}
			if (n < MAX_NAME_SIZE) {
				sb1.append(a);
			} else {
				sb2.append(a);
			}
			n++;
		}
		return sb1.toString() + "\n" + sb2.toString();
	}

}
