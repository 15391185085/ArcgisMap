package com.ieds.gis.map.gps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.location.GpsSatellite;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ieds.gis.map.R;
import com.ieds.gis.map.gps.util.GnssType;
import com.ieds.gis.map.gps.util.GpsTestUtil;
import com.lidroid.xutils.BitmapUtils;

public class SatellitesView extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final int TIME_IN_FRAME = 30;
	private static final String LOG_TAG = "SatellitesView";
	private SurfaceHolder holder = getHolder();
	int lockCout = 0;
	private Context m_context;
	boolean m_running = true;
	private DrawSatellitesThread thread;

	public SatellitesView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		this.holder.addCallback(this);
		this.holder.setFormat(-2);
		this.m_context = paramContext;
	}

	public void destroyedWxt() {
		surfaceDestroyed(this.holder);
	}

	public int getLockSateSize() {
		return this.lockCout;
	}

	public DrawSatellitesThread getThread() {
		return this.thread;
	}

	public void repaintSatellites(List<GpsSatellite> paramList, float degree) {
		if (this.thread != null) {
			this.thread.repaintSatellites(paramList, degree);
		}
	}

	public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1,
			int paramInt2, int paramInt3) {
		this.m_running = true;
		this.thread = new DrawSatellitesThread(paramSurfaceHolder,
				this.m_context);
		this.thread.start();
	}

	public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
		this.m_running = true;
	}

	public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
		this.m_running = false;
	}

	public static int snrToSignalLevel(float paramFloat) {
		if ((paramFloat >= 0.0F) && (paramFloat < 10.0F)) {
			return 1;
		} else if ((paramFloat >= 10.0F) && (paramFloat < 20.0F)) {
			return 2;
		} else if ((paramFloat >= 20.0F) && (paramFloat < 35.0F)) {
			return 3;
		} else if ((paramFloat >= 35.0F) && (paramFloat < 50.0F)) {
			return 4;
		} else {
			return 5;
		}
	}

	@SuppressLint("ResourceAsColor")
	class DrawSatellitesThread extends Thread {
		private Paint paint;
		PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, 3);
		private Paint rectPaint;
		Resources res;
		private Bitmap satelliteBitmap;
		private SurfaceHolder surfaceHolder;

		public DrawSatellitesThread(SurfaceHolder paramSurfaceHolder,
				Context paramContext) {
			this.surfaceHolder = paramSurfaceHolder;
			this.res = paramContext.getResources();
			this.paint = new Paint();
			this.paint.setSubpixelText(true);
			this.paint.setAntiAlias(true);
			this.paint.setFilterBitmap(true);

			int tSize = (int) m_context.getResources().getDimension(
					R.dimen.common_textsize1);

			this.paint.setColor(m_context.getResources()
					.getColor(R.color.white));
			this.paint.setTextSize(tSize);
			this.paint.setTextAlign(Paint.Align.CENTER);
			this.rectPaint = new Paint();
			this.rectPaint.setSubpixelText(true);
			this.rectPaint.setAntiAlias(true);
			this.rectPaint.setFilterBitmap(true);
		}

		private double degreeToRadian(double paramDouble) {
			return 3.141592653589793D * paramDouble / 180.0D;
		}

		private void doDraw(Canvas paramCanvas, List<GpsSatellite> paramList) {
			if (paramCanvas != null) {
				paramCanvas.drawColor(Color.BLACK);
				Drawable d = m_context.getResources().getDrawable(
						R.drawable.satellite_compass);
				int cw = paramCanvas.getWidth();
				int ch = paramCanvas.getHeight();
				int cleft = 0; // 左边距离
				int ctop = 0; // 左边距离

				if (cw > ch) {
					cleft = (cw - ch) / 2;
					cw = ch;

				} else {
					ctop = (ch - cw) / 2;
					ch = cw;
				}
				paramCanvas.rotate(degree, cw / 2 + cleft, ch / 2 + ctop);

				// 画星盘
				Bitmap bitmap = BitmapUtils.createImage(d, cw, ch);
				paramCanvas.drawBitmap(bitmap, cleft, ctop, paint);
				// 画卫星
				for (Iterator iterator = paramList.iterator(); iterator
						.hasNext();) {
					GpsSatellite gpsSatellite = (GpsSatellite) iterator.next();
					drawSatellite(paramCanvas, (GpsSatellite) gpsSatellite,
							cw / 2, ch / 2, cw / 2, cleft, ctop);
				}
				SatellitesView.this.lockCout = drawSingalView(paramCanvas,
						paramList);
				return;
			}

		}

		/**
		 * 
		 * @param paramCanvas
		 * @param paramGpsSatellite
		 * @param width
		 *            星图的宽
		 * @param height
		 *            星图的高
		 * @param r
		 *            星图的半径
		 */
		private void drawSatellite(Canvas paramCanvas,
				GpsSatellite paramGpsSatellite, int width, int height, int r,
				int cleft, int ctop) {
			float f = paramGpsSatellite.getElevation();
			double d2 = r * ((90.0F - f) / 90.0F);
			double d3 = degreeToRadian(360.0D - paramGpsSatellite.getAzimuth() + 90.0D);
			double d1 = width + Math.cos(d3) * d2 + cleft;
			d2 = height + Math.sin(d3) * d2 + ctop;
			height = snrToSignalLevel((int) paramGpsSatellite.getSnr());
			int sateDraw = R.drawable.satellite_mark;
			if ((height == 1) || (height == 2)) {
				sateDraw = R.drawable.satellite_mark;
			} else if (height == 3) {
				sateDraw = R.drawable.satellite_good;
			} else if (height >= 4) {
				sateDraw = R.drawable.satellite_better;
			}
			this.satelliteBitmap = BitmapFactory.decodeResource(this.res,
					sateDraw);
			int paintSize = (int) m_context.getResources().getDimension(
					R.dimen.common_textsize_two);
			this.satelliteBitmap = Bitmap.createScaledBitmap(
					this.satelliteBitmap, paintSize, paintSize, false);
			width = this.satelliteBitmap.getWidth() / 2;
			paramCanvas.drawBitmap(this.satelliteBitmap, (float) (d1 - width),
					(float) (d2 - width), this.paint);

			GnssType type = GpsTestUtil.getGnssType(paramGpsSatellite.getPrn());
			String name = "中";
			switch (type) {
			case NAVSTAR:
				name = "美";
				break;
			case GLONASS:
				name = "俄";
				break;
			case QZSS:
				name = "日";
				break;
			case BEIDOU:
				name = "中";
				break;
			}
			paramCanvas.drawText(name, (float) d1, (float) (7.0D + d2),
					this.paint);

		}

		private int drawSingalView(Canvas paramCanvas,
				List<GpsSatellite> paramList) {
			int j = 0;
			if (paramList.isEmpty()) {
				return 0;
			}
			for (Iterator iterator = paramList.iterator(); iterator.hasNext();) {
				GpsSatellite localGpsSatellite = (GpsSatellite) iterator.next();
				int i1 = (int) localGpsSatellite.getSnr();
				i1 = snrToSignalLevel(i1);
				if (i1 > 2) {
					j++;
				}
			}
			return j;
		}

		private List<GpsSatellite> paramList;
		private float degree;

		public synchronized void repaintSatellites(
				List<GpsSatellite> paramList, float degree) {
			this.paramList = paramList;
			this.degree = degree;
		}

		public void run() {
			SurfaceHolder surfaceHolder = holder;
			// 重复绘图循环，直到线程停止
			while (m_running) {
				// 锁定SurfaceView，并返回到要绘图的Canvas
				Canvas canvas = surfaceHolder.lockCanvas(); // ①
				// 绘制背景图片
				if (canvas != null) {
					synchronized (surfaceHolder) {
						if (paramList != null && !paramList.isEmpty()) {
							doDraw(canvas, paramList);
						} else {
							doDraw(canvas, new ArrayList<GpsSatellite>());
						}
					}
				}
				// 解锁Canvas，并渲染当前图像
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
				// //确保每次更新时间的帧数
				try {
					Thread.sleep(TIME_IN_FRAME);
				} catch (InterruptedException e) {
				}

			}
		}
	}
}