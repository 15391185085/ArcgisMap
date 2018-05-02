/**
 * 文 件 名 : Modle_Point_Manager.java
 * CopyRright (c) 2010-2011: 
 * 文件编号	： M0002
 * 日    期	： 2011.9.15
 * 日  期		： 2011.11.2
 * 描  述		： 坐标过滤模块
 * 版 本 号	： v1.0
 */

package com.ieds.gis.map.gps;

import java.util.Date;
import java.util.Iterator;

import android.location.Location;

import com.esri.core.geometry.Point;
import com.ieds.gis.map.gps.GpsService.LocationCallBack;
import com.ieds.gis.map.util.WktUtil;
import com.lidroid.xutils.util.DateUtil;
import com.lidroid.xutils.util.LogUtils;

/**
 * 模块类 坐标过滤模块 针对GPS定位获取而来的坐标，进行曲率圆滑性的过滤工作
 * 
 * @version v1.0
 * 
 */
public class SqliteDescartesPoint {
	private int timeSign; // 心跳
	private GpsService gs;
	/**
	 * 记录gps的有效频率
	 */
	public static final int mobileEffectiveHZ = 6;

	public SqliteDescartesPoint(GpsService gs) {
		super();
		this.gs = gs;
	}

	/**
	 * 数据类 -- 笛卡尔坐标类 记录笛卡尔坐标,主要加载从GPS坐标到笛卡尔坐标的转换,以及对数个 笛卡尔坐标的距离,夹角等的计算工作.
	 * 
	 * 使用样例:
	 * 
	 * {@code
	 *  
	 *  1.通过GPS对象构造笛卡尔坐标对象
	 *  
	 *  Descartes_Point _dp = new Descartes_Point(_recv_location);
	 *  
	 *  2..获取角P0P1P2的余弦值
	 *  
	 *  Descartes_Point _p0 = new Descartes_Point(_rec_loc_mid);
	 *  Descartes_Point _p1 = new Descartes_Point(_rec_loc_first);
	 *  Descartes_Point _p2 = new Descartes_Point(_rec_loc_third);
	 *  
	 *  Double _cos_012 = _p2.getAngleP0_P2_P1(_p0,_p1);
	 *  
	 *  
	 *  3.获取两点之间的距离
	 *   Descartes_Point _p1 = new Descartes_Point(Location _loc_1);
	 *   Descartes_Point _p2 = new Descartes_Point(Location _loc_2);
	 *   
	 *   Double _dis = _p1.getDistance(_p2);
	 *   
	 *  }
	 * 
	 * 
	 * @version v1.0
	 * 
	 */
	private class Descartes_Point {
		/** 笛卡尔坐标的X轴的值 */
		public Double mX;

		/** 笛卡尔坐标的Y轴的值 */
		public Double mY;

		/**
		 * * 带参数构造函数,初始化笛卡尔坐标对象,对域成员进行初始化 *
		 * 
		 * @param Location
		 *            _org_location 传入的Android GPS坐标对象
		 * 
		 * 
		 * @version v1.0
		 * 
		 */
		public Descartes_Point(Location _org_location) {
			/** 传入原始的GPS坐标值 */
			/** 对其进行复制 */
			Location _location = new Location(_org_location);

			/** 以下则是通过纬度和经度数据计算在地图上对应的笛卡尔坐标值 */

			/** 偏移 */
			_location.setLatitude(_location.getLatitude() - 0.000890);
			_location.setLongitude(_location.getLongitude() - 0.000390);

			/** 计算因子 */
			Double gA = 95657.666649244;
			Double gB = 241.317577244235;
			Double gC = -11626722.0873722;
			Double gD = -30.5861721426051;
			Double gE = 110808.774127432;
			Double gF = -3457402.52425841;

			/** 笛卡尔坐标值 */
			mX = gA * _location.getLatitude() + gB * _location.getLongitude()
					+ gC;
			mY = gD * _location.getLatitude() + gE * _location.getLongitude()
					+ gF;
		}

		/**
		 * 计算角P0 P1 P2 的余弦值*
		 * 
		 * @param Descartes_Point
		 *            _p0 传入的角参照点
		 * 
		 * @param Descartes_Point
		 *            _p1 传入角的顶点
		 * 
		 * @return Double 返回以p1为角度的中心,角P0P2P1的余弦值 返回值范围：1->-1
		 * 
		 * 
		 * @version v1.0
		 */
		public Double getAngleP0_P2_P1(Descartes_Point _p0, Descartes_Point _p1) {
			/** 原理 : 余弦 = 向量p1p2和向量p0p1的向量乘积与向量长度之积 */

			/** 向量的数量积 */
			Double _S = (this.mX - _p1.mX) * (_p0.mX - _p1.mX)
					+ (this.mY - _p1.mY) * (_p0.mY - _p1.mY);

			/** 向量长度之积 */
			Double _D = Math.sqrt(Math.pow(_p1.mX - this.mX, 2)
					+ Math.pow(_p1.mY - this.mY, 2))
					* Math.sqrt(Math.pow(_p1.mX - _p0.mX, 2)
							+ Math.pow(_p1.mY - _p0.mY, 2));

			/** 比值即是余弦值 */
			return _S / _D;
		}

		/**
		 * 计算角两个笛卡尔坐标之间的距离*
		 * 
		 * @param Descartes_Point
		 *            _p 传入的另一个笛卡尔坐标点
		 * 
		 * 
		 * @return Double 返回自身点坐标与另一个传入点坐标的距离
		 * 
		 * 
		 * @version v1.0
		 */
		public Double getDistance(Descartes_Point _p) {
			/** D = sqrt( (X1-X2)^2 + (Y1-Y2)^2 ) */
			Double _D = Math.sqrt(Math.pow(this.mX - _p.mX, 2)
					+ Math.pow(this.mY - _p.mY, 2));

			/** 返回计算值 */
			return _D;
		}
	}

	private volatile Point startPoint; // 起点
	private volatile long startTime; // 开始时间
	// 大于30分钟，不计入里程，防止显示无效里程
	public static final int MAX_Mil_RECORD_TIME = 30 * 60;

	class MyRunnable implements Runnable {
		private Location location;

		public MyRunnable(Location location) {
			super();
			this.location = location;
		}

		@Override
		public void run() {
			synchronized (GpsService.class) {
				try {
					if (startPoint == null) {
						startPoint = new Point(location.getLongitude(),
								location.getLatitude());
						startTime = location.getTime();
					} else {
						Point endPoint = new Point(location.getLongitude(),
								location.getLatitude());
						long endTime = location.getTime();

						double addMileage = WktUtil.getLatLonDistance(
								startPoint, endPoint);
						if (isRecordMileage(endTime, startTime)) {
							double addTime = getDifferenceSecond(endTime,
									startTime);
							for (Iterator iterator = GpsService.locationChangeCallBack
									.iterator(); iterator.hasNext();) {
								LocationCallBack lcb = (LocationCallBack) iterator
										.next();
								lcb.descartesLocationChanged(addMileage,
										addTime);
							}
						}
						startPoint = endPoint;
						startTime = endTime;
					}
					for (Iterator iterator = GpsService.locationChangeCallBack
							.iterator(); iterator.hasNext();) {
						LocationCallBack lcb = (LocationCallBack) iterator
								.next();
						lcb.descartesLocationChanged(location);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * 是否记录里程
	 * 
	 * @param endTime
	 * @param startTime
	 * @return
	 */
	public static boolean isRecordMileage(long endTime, long startTime) {
		double addTime = getDifferenceSecond(endTime, startTime);
		if (addTime <= SqliteDescartesPoint.MAX_Mil_RECORD_TIME) {
			return true;
		} else {
			// 大于30分钟，不计入里程
			return false;
		}
	}

	public static double getDifferenceSecond(long endTime, long startTime) {
		double addTime = (endTime - startTime) / 1000;
		return addTime;
	}

	/** 以下是Modle_Point_Manager的成员域 */

	/** 存储所有GPS接收到的原始坐标数据 */
	Location mPoints[] = new Location[2];

	/** 符合标准的最大余弦值 */
	final Double MAX_COS = 0.7;

	/** 成员函数 */
	public void addLocation(Location _loc, boolean isGpsValid) {
		// 获取第一个钝角的三个点为起点
		// 第一个坐标点
		// 第二个坐标点
		if (mPoints[0] == null) {
			mPoints[0] = _loc;
			return;
		}
		if (mPoints[1] == null) {
			mPoints[1] = _loc;
			return;
		}
		/** 2011.11.29 */
		/** 增加速度限制 */
		if (!isGpsValid) {
			// 无效的gps数据
			return;
		}
		// 从第三个坐标点开始进行判断
		// 如果p0p1p2判断为钝角，则加入p2,并将mP0_ID加1
		// 如果p0p1p2判断为锐角，则不加入p2,而在Points中则删除p2,,mP0_ID也不变
		Descartes_Point _p0 = new Descartes_Point(mPoints[0]);
		Descartes_Point _p1 = new Descartes_Point(mPoints[1]);
		Descartes_Point _p2 = new Descartes_Point(_loc);
		if (_p2.getAngleP0_P2_P1(_p0, _p1) > MAX_COS) {
			// 锐角
			// 删除p1
		} else {
			// 钝角
			LogUtils.v("记录轨迹" + DateUtil.getChineLongDate(new Date()));
			toReadTrack(_loc);
		}
		mPoints[0] = mPoints[1];
		mPoints[1] = _loc;
	}

	private void toReadTrack(Location location) {

		if (timeSign >= 99999) {
			timeSign = 0;
		}
		timeSign++;

		if (location.getSpeed() == 0) {
			return;
		} else if ((location.getSpeed() > 0)
				&& (location.getSpeed() <= mobileEffectiveHZ)) {
			if (timeSign % (mobileEffectiveHZ) == 0) {
				LogUtils.v("速度-慢" + DateUtil.getChineLongDate(new Date()));
				// 慢
				new Thread(new MyRunnable(location)).start();
			}
		} else if ((location.getSpeed() > mobileEffectiveHZ)) {
			// 快
			if (timeSign % ((int) location.getSpeed()) == 0) {
				new Thread(new MyRunnable(location)).start();
				LogUtils.v("速度-快" + DateUtil.getChineLongDate(new Date()));
			}
		}
	}
}
