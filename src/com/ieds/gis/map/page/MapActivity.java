package com.ieds.gis.map.page;

/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.location.GpsSatellite;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.event.OnPanListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Point;
import com.ieds.gis.base.BaseActivity;
import com.ieds.gis.base.dialog.MyToast;
import com.ieds.gis.base.util.SettingUtil;
import com.ieds.gis.map.R;
import com.ieds.gis.map.data.MapData;
import com.ieds.gis.map.gps.GpsService;
import com.ieds.gis.map.layer.LocationCentreLayer;
import com.ieds.gis.map.service.INetworkService;
import com.ieds.gis.map.util.FormatUtil;
import com.ieds.gis.map.util.NotificationUtil;
import com.ieds.gis.map.util.WktUtil;
import com.lidroid.xutils.exception.NullArgumentException;

/**
 * 大量的地图要素绘制会堵塞主线程，少量数据影响不大
 */
public abstract class MapActivity extends BaseActivity implements
		View.OnClickListener {
	protected MapView mapView;
	protected TextView location1;
	protected TextView location2;
	protected NotificationUtil warn;
	protected LocationCentreLayer locationCentreLayer;
	// 当前的位置坐标
	protected Location currentLocation;

	public abstract INetworkService getINetworkService();

	public void reLocation() {
		String x = SettingUtil.getInstance().getPointX();
		String y = SettingUtil.getInstance().getPointY();
		String s = SettingUtil.getInstance().getScale();

		if (x != null && y != null && s != null) {
			double px = Double.valueOf(x);
			double py = Double.valueOf(y);
			double sy = Double.valueOf(s);
			Point p = new Point(px, py);
			mapToCenter(sy, p);
		}
	}

	/**
	 * @param scale
	 * @param center
	 */
	public void mapToCenter(double scale, Point center) {
		mapView.centerAt(center, false);
		mapView.setScale(scale, false);
		refreshGdb();
	}

	/**
	 * 重新加载地图业务数据图层
	 * 
	 * @param isMust
	 */
	private void refreshGdb() {
		if (location1 == null) {
			return;
		}
		location1.setText(FormatUtil.getScaleText(mapView.getScale()));
		Thread tr = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				synchronized (MapActivity.class) {
					try {
						ReloadLayout();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						MyToast.showToast(e.getMessage());
					}
				}
			}

		};
		tr.start();
	}

	/**
	 * 重新加载地图业务数据图层
	 */
	public abstract void ReloadLayout() throws Exception;

	/**
	 * 更新时间
	 */
	private boolean updateGpsTime = false;
	GpsService.LocationCallBack locationCallBack = new GpsService.LocationCallBack() {
		boolean isGpsValid;

		@Override
		public void descartesLocationChanged(Location location) {
			// TODO Auto-generated method stub

		}

		@Override
		public void descartesLocationChanged(double addMileage, double addTime) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStateChanged(String location) {
			// TODO Auto-generated method stub
			if (location2.isShown()) {
				if (location2 == null) {
					return;
				}
				location2.setText(Html.fromHtml(location));
			}
		}

		@Override
		public void onSatelliteChanged(List<GpsSatellite> gpsSatelliteList,
				boolean isGpsValid) {
			// TODO Auto-generated method stub
			this.isGpsValid = isGpsValid;
		}

		@Override
		public void onLocationChanged(Location location) {
			currentLocation = location;
			if (mapView.isShown()) {
				Point point = new Point(location.getLongitude(),
						location.getLatitude());
				Point currentEarthPoint = (Point) WktUtil.getEarthFromLatLon(
						point, mapView);
				if (locationCentreLayer != null) {
					locationCentreLayer.onDrawLocation(currentEarthPoint,
							location.getBearing(), location.getAccuracy(),
							mapView);
				}
				if (warn != null && location.getTime() != 0 && !updateGpsTime) {
					updateGpsTime = true;
					warn.updataTime(new Date(location.getTime()));
				}
			}
		}
	};

	private void setScale(Layer l) {
		l.setMaxScale(MapData.MAX_SCALE);
		l.setMinScale(MapData.MIN_SCALE);
	}

	private void setScale(MapView l) {
		l.setMaxScale(MapData.MAX_SCALE);
		l.setMinScale(MapData.MIN_SCALE);
	}

	public abstract List<String> getOnlineUrl();

	/**
	 * @throws NullArgumentException
	 */
	public void initMapView() {
		if (getOnlineUrl() != null) {
			List<String> onlineUrlList = getOnlineUrl();
			for (Iterator iterator = onlineUrlList.iterator(); iterator
					.hasNext();) {
				String object = (String) iterator.next();
				Layer onlineTileLayer = new ArcGISDynamicMapServiceLayer(object);
				setScale(onlineTileLayer);
				mapView.addLayer(onlineTileLayer);
			}

		}

		setScale(mapView);
		// 加用户当前位置标记
		locationCentreLayer = new LocationCentreLayer(this, mapView);
		mapView.addLayer(locationCentreLayer);

		/**
		 * When the basemap is initialized the status will be true.
		 */
		mapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(final Object source, final STATUS status) {
				if (STATUS.INITIALIZED == status) {
					reLocation();
				}

			}
		});
		mapView.setOnPanListener(new OnPanListener() {

			@Override
			public void prePointerUp(float arg0, float arg1, float arg2,
					float arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void prePointerMove(float arg0, float arg1, float arg2,
					float arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void postPointerUp(float arg0, float arg1, float arg2,
					float arg3) {
				refreshGdb();
			}

			@Override
			public void postPointerMove(float arg0, float arg1, float arg2,
					float arg3) {
				// TODO Auto-generated method stub
			}
		});
		mapView.setOnZoomListener(new OnZoomListener() {

			@Override
			public void preAction(float arg0, float arg1, double arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void postAction(float arg0, float arg1, double arg2) {
				// TODO Auto-generated method stub
				refreshGdb();
			}

		});
		mapView.setOnSingleTapListener(new OnSingleTapListener() {

			@Override
			public void onSingleTap(float arg0, float arg1) {
				// TODO Auto-generated method stub
				Point point = mapView.toMapPoint(new Point(arg0, arg1));
				try {
					onMapSingleTap(point);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MyToast.showToast(e.getMessage());
				}
			}
		});
	}

	public abstract void onMapSingleTap(Point earthP) throws Exception;

	public void initView() {
		location2 = (TextView) findViewById(R.id.location2);
		location1 = (TextView) findViewById(R.id.location1);
		mapView = ((MapView) findViewById(R.id.map));
		warn = new NotificationUtil(this, getINetworkService());
		warn.showWarn();
		warn.showGPS(location2);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_world);
		ArcGISRuntime.setClientId("zNFtEVeblOmK5MFP");
		initView();
		initMapView();
		GpsService.addLocation(locationCallBack);
	}

	@Override
	public void onDestroy() {
		warn.close();
		GpsService.removeLocation(locationCallBack);
		super.onDestroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 结束刷新
		mapView.pause();
		writeLocation();
	}

	public void writeLocation() {
		Point p = mapView.getCenter();
		if (p != null) {
			SettingUtil.getInstance().setPointX("" + p.getX());
			SettingUtil.getInstance().setPointY("" + p.getY());
			SettingUtil.getInstance().setScale("" + mapView.getScale());
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mapView.unpause();
	}

}
