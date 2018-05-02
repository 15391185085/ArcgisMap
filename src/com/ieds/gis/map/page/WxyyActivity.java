package com.ieds.gis.map.page;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ieds.gis.base.BaseActivity;
import com.ieds.gis.map.R;
import com.ieds.gis.map.gps.GpsService;
import com.ieds.gis.map.gps.SatellitesView;
import com.ieds.gis.map.util.WktUtil;

public class WxyyActivity extends BaseActivity {
	private Activity activity;
	int acury;
	long azimu;
	int elev;
	IntentFilter filter;
	double latN = 0.0D;
	String lockSateSize;
	double logE = 0.0D;
	String satelliteSize;
	public SatellitesView satellitesView;
	float speed;
	private TextView tv_accuracy;
	private TextView tv_azimuth;
	private TextView tv_elevation;
	private TextView tv_latN;
	private TextView tv_lgE;
	private TextView tv_lockC;
	private TextView tv_speed;
	private TextView tv_visualC;
	private SensorManager sensorManager;
	float degree;
	private SensorEventListener listener = new SensorEventListener() {
		float[] accelerometerValues = new float[3];
		float[] magneticValues = new float[3];

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerValues = event.values.clone();
			} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticValues = event.values.clone();
			}
			float[] R = new float[9];
			float[] values = new float[3];
			SensorManager.getRotationMatrix(R, null, accelerometerValues,
					magneticValues);
			SensorManager.getOrientation(R, values);
			degree = -(float) Math.toDegrees(values[0]);// 旋转角度
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	private void init() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor magneticSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(listener, magneticSensor,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(listener, accelerometerSensor,
				SensorManager.SENSOR_DELAY_GAME);
		ImageView back = (ImageView) this.findViewById(R.id.ivfanhui);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				WxyyActivity.this.finish();
			}

		});

		TextView title = (TextView) this.findViewById(R.id.main_txt);
		title.setText("卫星状态");
		this.satellitesView = ((SatellitesView) this
				.findViewById(R.id.satellitesView));
		this.tv_lgE = ((TextView) this.findViewById(R.id.tv_lgE));
		this.tv_latN = ((TextView) this.findViewById(R.id.tv_latN));
		this.tv_speed = ((TextView) this.findViewById(R.id.tv_speed));
		this.tv_azimuth = ((TextView) this.findViewById(R.id.tv_azimuth));
		this.tv_elevation = ((TextView) this.findViewById(R.id.tv_elevation));
		this.tv_accuracy = ((TextView) this.findViewById(R.id.tv_accuracy));
		this.tv_visualC = ((TextView) this.findViewById(R.id.tv_visual_countS));
		this.tv_lockC = ((TextView) this.findViewById(R.id.tv_lock_countS));
		setTextViewValue(0.0D, 0.0D, 0, 0, 0.0F, 0L);
	}

	private void setTextViewValue(double paramDouble1, double paramDouble2,
			int paramInt1, int paramInt2, float paramFloat, long paramLong) {
		Object localObject = new DecimalFormat("##.000000");
		String str = ((DecimalFormat) localObject).format(paramDouble1);
		localObject = ((DecimalFormat) localObject).format(paramDouble2);
		this.tv_lgE.setText(str + "°");
		this.tv_latN.setText(localObject + "°");
		this.tv_elevation.setText(paramInt1 + "米");
		this.tv_accuracy.setText(paramInt2 + "米");
		this.tv_speed.setText(WktUtil.getDouble(paramFloat, 1) + "米/秒");
		this.tv_azimuth.setText(paramLong + "°");
	}

	public Object getFragmentValue() {
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fragment_wxyy_positon);
		init();
		GpsService.addLocation(locationCallBack);
	}

	GpsService.LocationCallBack locationCallBack = new GpsService.LocationCallBack() {

		@Override
		public void descartesLocationChanged(Location location) {
			// TODO Auto-generated method stub

		}

		@Override
		public void descartesLocationChanged(double addMileage, double addTime) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			WxyyActivity.this.logE = location.getLongitude();
			WxyyActivity.this.latN = location.getLatitude();
			WxyyActivity.this.elev = (int) location.getAltitude();
			WxyyActivity.this.acury = (int) location.getAccuracy();
			WxyyActivity.this.speed = location.getSpeed();
			WxyyActivity.this.azimu = (long) location.getBearing();
			WxyyActivity.this.setTextViewValue(WxyyActivity.this.logE,
					WxyyActivity.this.latN, WxyyActivity.this.elev,
					WxyyActivity.this.acury, WxyyActivity.this.speed,
					WxyyActivity.this.azimu);
		}

		@Override
		public void onStateChanged(String location) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSatelliteChanged(List<GpsSatellite> gpsSatelliteList,
				boolean isGpsValid) {
			// TODO Auto-generated method stub
			if (WxyyActivity.this.satellitesView.isShown()) {
				WxyyActivity.this.satellitesView.repaintSatellites(
						gpsSatelliteList, degree);
				WxyyActivity.this.satelliteSize = gpsSatelliteList.size() + "颗";
				WxyyActivity.this.tv_visualC
						.setText(WxyyActivity.this.satelliteSize);
				WxyyActivity.this.tv_lockC.setText("0");
				WxyyActivity.this.lockSateSize = WxyyActivity.this.satellitesView
						.getLockSateSize() + "颗";
				WxyyActivity.this.tv_visualC
						.setText(WxyyActivity.this.satelliteSize);
				WxyyActivity.this.tv_lockC
						.setText(WxyyActivity.this.lockSateSize);
			}
		}

	};

	public void onDestroy() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(listener);
		}
		GpsService.removeLocation(locationCallBack);
		super.onDestroy();
	}

	public void onPause() {
		if (this.satellitesView != null) {
			this.satellitesView.setVisibility(View.GONE);
		}
		super.onPause();
	}

	public void onResume() {
		if (this.satellitesView != null) {
			this.satellitesView.setVisibility(View.VISIBLE);
		}
		super.onResume();
	}

	public void onStop() {
		super.onStop();
	}

}
