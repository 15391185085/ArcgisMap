/**  
 * Desc:   
 * @title AllPoletower.java   
 * @update 2013-5-29 上午9:41:22  
 * @version V1.0 
 * Note: This content is restricted to IEDS within the company 
 	circulated to prohibit leakage as well as for other commercial purposes 
 */
package com.ieds.gis.map.layer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.ieds.gis.base.dao.Selector;
import com.ieds.gis.map.R;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.util.Tools;

/**
 * 特殊图层，定位当前位置
 * 
 * @version 1.0
 * @update 2013-5-29 上午9:41:22
 */
public class LocationCentreLayer extends GraphicsLayer {
	private static final int INT_ID = -1;
	public static final int CIRCLE_ALPHA = 20; // 圆的透明度

	private Drawable icon_compass;
	private Drawable icon_compass_no;
	private Context context;
	private Graphic currentCircleGraphic;
	private int lId = INT_ID;
	private int cId = INT_ID;
	private Polygon currentCirclePolygon;
	private float currentDirection; // 当前角度
	private PictureMarkerSymbol deviceSymbol;
	private MapView mv;

	/**
	 * 该点是否在当前的圈内
	 * 
	 * @param point
	 * @return
	 */
	public boolean isWithInCircle(Point point, MapView mv) {
		if (currentCirclePolygon != null) {
			return GeometryEngine.within(point, currentCirclePolygon,
					mv.getSpatialReference());
		} else {
			return false;
		}
	}

	public LocationCentreLayer(Context context, MapView mv) {
		super();
		this.mv = mv;
		this.context = context;
		icon_compass = context.getResources().getDrawable(R.drawable.compass);
		icon_compass_no = context.getResources().getDrawable(
				R.drawable.compass_no);
		initDirection(0);
	}

	/**
	 * 画方向箭头
	 */
	public void drawDirection(Point currentPoint, float bear) {
		initDirection(bear);
		// 把最后一个图形加在队列中为首位
		if (lId != INT_ID) {
			if (isUpdateSymbol) {
				isUpdateSymbol = false;
				LocationCentreLayer.this.updateGraphic(lId, deviceSymbol);
			}
			LocationCentreLayer.this.updateGraphic(lId, currentPoint);

		} else {
			Graphic graphicPoint = new Graphic(currentPoint, deviceSymbol);
			lId = LocationCentreLayer.this.addGraphic(graphicPoint);
		}
	}

	boolean isUpdateSymbol;

	/**
	 * @param direction
	 * @return
	 */
	public void initDirection(float direction) {
		if (deviceSymbol == null || direction == 0) {
			Drawable currentIconDirection = BitmapUtils.angleBitmap(
					icon_compass_no, direction, Tools.dipToPx(context, 50),
					Tools.dipToPx(context, 50));
			deviceSymbol = new PictureMarkerSymbol(currentIconDirection);
			isUpdateSymbol = true;
			currentDirection = 0;

		} else if (Math.abs(currentDirection - direction) > 30) { // 转角大于30度
			Drawable currentIconDirection = BitmapUtils.angleBitmap(
					icon_compass, direction, Tools.dipToPx(context, 50),
					Tools.dipToPx(context, 50));
			deviceSymbol = new PictureMarkerSymbol(currentIconDirection);
			isUpdateSymbol = true;
			currentDirection = direction;

		}

	}

	/**
	 * gps变化
	 */
	public void onDrawLocation(Point currentLocation, float bear,
			double accuracy, MapView mapView) {
		try {
			if (accuracy >= 1) {
				drawCircle(currentLocation, CIRCLE_ALPHA, Color.BLUE, accuracy);
			} else {
				// 清除圆
				removeCircle();
			}
			drawDirection(currentLocation, bear);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void removeLoc() {
		this.removeGraphic(lId);
		lId = INT_ID;
	}

	public void removeCircle() {
		this.removeGraphic(cId);
		cId = INT_ID;
	}

	/**
	 * 绘制圆,配合 clearDrawCircleLayer()清除
	 * 
	 * @param center
	 *            圆心
	 * @param RADIUS
	 *            半径
	 * @param alpha
	 *            填充的透明度 0-100
	 * @param fillColor
	 *            填充的颜色
	 */
	private void drawCircle(Point center, int alpha, int fillColor,
			double accuracy) {
		currentCirclePolygon = getCircle(center, accuracy);
		FillSymbol symbol = new SimpleFillSymbol(fillColor);
		symbol.setAlpha(alpha);
		SimpleLineSymbol outline = new SimpleLineSymbol(fillColor, 1);
		symbol.setOutline(outline);
		currentCircleGraphic = new Graphic(currentCirclePolygon, symbol);
		if (cId >= 0) {
			LocationCentreLayer.this.updateGraphic(cId, currentCircleGraphic);
		} else {
			cId = LocationCentreLayer.this.addGraphic(currentCircleGraphic);
		}
	}

	public void hideCircle() {
		if (cId >= 0) {
			LocationCentreLayer.this.removeGraphic(cId);
			cId = INT_ID;
		}
	}

	public void showCircle() {
		if (cId < 0 && currentCircleGraphic != null) {
			cId = LocationCentreLayer.this.addGraphic(currentCircleGraphic);
		}
	}

	public Polygon getCircle(Point center, double radius) {
		return GeometryEngine.geodesicEllipse(center, mv.getSpatialReference(),
				radius, radius, null);
	}

}
