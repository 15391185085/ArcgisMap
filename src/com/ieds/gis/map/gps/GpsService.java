package com.ieds.gis.map.gps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.IBinder;

import com.ieds.gis.base.BaseApp;
import com.ieds.gis.map.util.WktUtil;

/**
 * 采集位置坐标的服务类
 * 
 * @update 2015-3-18 下午5:01:36<br>
 * 
 */
public class GpsService extends Service {

	/**
	 * gps接收数据的正常回调
	 */
	public static List<LocationCallBack> locationChangeCallBack = new ArrayList<LocationCallBack>();

	/**
	 * 所有Service的方法都要有监听 服务中返回数据的监听
	 * 
	 * @update 2014-7-8 下午3:47:47<br>
	 * 
	 */
	public static interface LocationCallBack {
		public void onLocationChanged(Location location);

		/**
		 * 不同卫星状态显示的数据
		 * 
		 * @param location
		 */
		public void onStateChanged(String location);

		/**
		 * 
		 * @param gpsSatelliteList
		 *            卫星列表
		 * @param isGpsValid
		 *            true 有效卫星大于等于3颗
		 */
		public void onSatelliteChanged(List<GpsSatellite> gpsSatelliteList,
				boolean isGpsValid);

		/**
		 * 高级定位，用笛卡尔算法进行过滤，掉飘点，减少了位置个数
		 * 
		 * @param location
		 */
		public void descartesLocationChanged(Location location);

		/**
		 * 高级定位，用笛卡尔算法进行过滤
		 * @param addMileage 两次定位之间的距离
		 * @param addTime 两次定位之间的时间（单位：秒）
		 */
		public void descartesLocationChanged(double addMileage, double addTime);

	}

	/**
	 * 所有Service的方法都要有监听 服务中返回数据的监听
	 * 
	 * @update 2014-7-8 下午3:47:47<br>
	 * 
	 */
	public static interface TaskCallBack {
		public void onTaskChanged();
	}

	/**
	 * 注册位置监听变化监听 首次注册时将最后位置返回，以后凡有位置变化时返回变化后的位置
	 * 
	 * @update 2014-8-9 上午11:00:27<br>
	 * 
	 * @return
	 */
	public static void addLocation(LocationCallBack callback) {
		removeLocation(callback);
		locationChangeCallBack.add(callback);
	}

	/**
	 * 得到最后的位置坐标
	 * 
	 * @update 2014-8-9 上午11:00:27<br>
	 * 
	 * @return
	 */
	public static void removeLocation(LocationCallBack callback) {
		locationChangeCallBack.remove(callback);
	}

	private static final String GOOD_SPEED = "<font color='green'>优</font>";
	private static final String BAD_SPEED = "<font color='red'>差</font>";
	/**
	 * 高程的精度
	 */
	private static final int GPS_ALT_SIZE = 2;
	/**
	 * 经纬度的精度
	 */
	private static final int GPS_LATLON_SIZE = 8;
	private static final float MIN_SAVE_METER = 0.0f;
	public LocationManager locManager;
	private static final long MIN_TIME = 1000;// 每次获取定位时间，单位：毫秒

	private SqliteDescartesPoint mpm;
	private long addFlux = 0;
	private long startFlux = 0; // 起点流量
	private List<GpsStatus> gpsList = new ArrayList<GpsStatus>();
	private boolean isGpsValid;
	/**
	 * Gps状态监听
	 */
	private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			GpsStatus gpsStatus = locManager.getGpsStatus(null);
			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS: // 周期的报告卫星状态
				// 得到所有收到的卫星的信息，包括 卫星的高度角、方位角、信噪比、和伪随机号（及卫星编号）
				Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
				List<GpsSatellite> satelliteList = new ArrayList<GpsSatellite>();
				int n = 0;
				for (GpsSatellite satellite : satellites) {
					satelliteList.add(satellite);
					int snr = SatellitesView.snrToSignalLevel((int) satellite
							.getSnr());
					if (snr > 2) {
						n++;
					}
				}
				if (n >= 3) {
					isGpsValid = true;
				} else {
					isGpsValid = false;
				}
				for (Iterator iterator = locationChangeCallBack.iterator(); iterator
						.hasNext();) {
					LocationCallBack lcb = (LocationCallBack) iterator.next();
					lcb.onSatelliteChanged(satelliteList, isGpsValid);
				}
				break;
			}
		}
	};

	public long getAddFlux() {
		return addFlux;
	}

	public static long getCurrentSystemFlux() {
		long g3_down_total = TrafficStats.getMobileRxBytes();
		long g3_up_total = TrafficStats.getMobileTxBytes();
		long total = g3_down_total + g3_up_total;
		return total;
	}

	/**
	 * 记录流量
	 */
	public synchronized void recordFlux() {
		long currentFlux = getCurrentSystemFlux();
		if (currentFlux > 0) {
			// 当前网络为3g网络
			if (startFlux == 0) {
				// 没有开始流量
				startFlux = currentFlux;
			} else {
				// 流量累计
				addFlux += currentFlux - startFlux;
				// 重置开始流量
				startFlux = currentFlux;
			}
		}
	}

	/**
	 * 累计值被记录后初始化记录流量
	 */
	public synchronized void initRecordFlux() {
		addFlux = 0;
		startFlux = 0;
	}

	/**
	 * 每次位置坐标改变调用该方法
	 * 
	 * @param location
	 */
	public void locationChanged(final Location l) {
		Location location = new Location(l);
		if (isTrueLocation(location)) {
			location.setAltitude(WktUtil.getDouble(location.getAltitude(),
					GPS_ALT_SIZE));
			location.setLatitude(WktUtil.getDouble(location.getLatitude(),
					GPS_LATLON_SIZE));
			location.setLongitude(WktUtil.getDouble(location.getLongitude(),
					GPS_LATLON_SIZE));
			for (Iterator iterator = locationChangeCallBack.iterator(); iterator
					.hasNext();) {
				LocationCallBack lcb = (LocationCallBack) iterator.next();
				lcb.onLocationChanged(location);
				lcb.onStateChanged("a:<font color='blue'>"
						+ (int) location.getAccuracy() + "</font>,x:"
						+ location.getLongitude() + ",y:"
						+ location.getLatitude());
			}
			mpm.addLocation(location, isGpsValid);
		}
	}

	private LocationListener saveListener = new LocationListener() {
		// 当位置改变时调用下面的函数

		@Override
		public void onLocationChanged(Location location) {
			locationChanged(location);
		}

		@Override
		public void onProviderDisabled(String arg0) {
			updateText(locManager);
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// 当前代理可用, 刷新代理
			updateText(locManager);
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}
	};

	public static void updateText(LocationManager locManager) {
		String p = getGPSText(locManager);
		for (Iterator iterator = locationChangeCallBack.iterator(); iterator
				.hasNext();) {
			LocationCallBack lcb = (LocationCallBack) iterator.next();
			lcb.onStateChanged(p);
		}
	}

	/**
	 * @param lastLocation
	 *            这是一个真实的地址 true:该次定位有效
	 */
	public static boolean isTrueLocation(Location lastLocation) {
		if (lastLocation != null && lastLocation.getLatitude() > 0
				&& lastLocation.getLatitude() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();
		mpm = new SqliteDescartesPoint(this);
		openGPSSettings();
		// myHandler.postDelayed(testRunnable, 1000);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	// private Handler myHandler = new Handler();
	// Runnable testRunnable = new Runnable() {
	// double sign = 0;
	// int n = 0;
	//
	// @Override
	// public void run() {
	// Location l = new Location("m");
	// Random r = new Random();
	// double dx = (double) r.nextInt(10) / 100000;
	// l.setLatitude(39.80475123124123 + dx);
	// l.setLongitude(110.0741112412412 + dx);
	// n++;
	// l.setSpeed((float) 2);
	// l.setBearing(10);
	// l.setAccuracy(10);
	// l.setAltitude(900);
	// l.setTime(new Date().getTime() - 10000000);
	// LogUtils.v("单次轨迹" + DateUtil.getChineLongDate(new Date()));
	// locationChanged(l);
	// myHandler.postDelayed(this, 1000);
	// }
	// };

	public boolean onUnbind(Intent intent) {
		return true;
	}

	public void onDestroy() {
		super.onDestroy();
		// myHandler.removeCallbacks(testRunnable);
		removeListener();
	}

	/**
	 * 
	 */
	public void removeListener() {
		if (locManager != null) {
			locManager.removeUpdates(saveListener);
			locManager.removeGpsStatusListener(this.gpsStatusListener);
		}
	}

	private void openGPSSettings() {
		locManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		updateText(locManager);
		removeListener();
		locManager.requestLocationUpdates(
				android.location.LocationManager.GPS_PROVIDER, MIN_TIME,
				MIN_SAVE_METER, saveListener);
		locManager.removeGpsStatusListener(this.gpsStatusListener);
		locManager.addGpsStatusListener(this.gpsStatusListener);

	}

	public static String getGPSText(LocationManager lm) {
		String p;
		if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			p = "<font color='red'>GPS：搜索中...</font>";
		} else {
			p = "<font color='red'>GPS：未启动</font>";
		}
		return p;
	}

}
