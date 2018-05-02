package com.ieds.gis.map.util;

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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.esri.core.symbol.TextSymbol;
import com.ieds.gis.base.BaseApp;
import com.ieds.gis.map.data.EquipData;
import com.lidroid.xutils.util.Tools;

/**
 * 格式转换工具类
 * 
 * @author lihx
 * 
 */
public class FormatUtil {
	protected static final String TAG = "GDBUtil";

	/**
	 * byte转kb，结尾保留4位小数
	 * 
	 * @return
	 */
	public static double getByteToKB(long b) {
		double m = ((double) (b)) / 1024;
		return WktUtil.getDouble((double) m / 1024, 4);
	}

	/**
	 * 米转合适的字符
	 * 
	 * @param time
	 * @return
	 */
	public static String getKB(double b) {
		if (b <= 0) {
			return "0B";
		}
		String formatStr = null;
		if (b >= 1024 * 1024) {
			b /= (1024 * 1024);
			formatStr = "%1$.1fMB";
		} else {
			b /= 1024;
			formatStr = "%1$.1fKB";
		}
		return String.format(formatStr, b);
	}

	/**
	 * 米转合适的字符
	 * 
	 * @param time
	 * @return
	 */
	public static String getTimeText(double time) {
		if (time <= 0) {
			return "0秒";
		}
		String formatStr = null;
		if (time >= 3600) {
			time /= 3600;
			formatStr = "%1$.1f小时";
		} else {
			time /= 60;
			formatStr = "%1$.1f分钟";
		}
		return String.format(formatStr, (float) time);
	}

	public static String getUsageText(double usage) {
		if (usage <= 0) {
			return "0K";
		}
		String formatStr = null;
		if (usage >= 1024 * 1024) {
			usage /= 1024 * 1024;
			formatStr = "%1$.1fM";
		} else {
			usage /= 1024;
			formatStr = "%1$.1fK";
		}
		return String.format(formatStr, (float) usage);
	}

	/**
	 * 标尺转换米
	 * 
	 * @param meter
	 * @return
	 */
	public static String getMeterText(double meter) {
		if (meter <= 0) {
			return "0米";
		}
		String formatStr = null;
		if (meter >= 1000) {
			meter /= 1000;
			formatStr = "%1$.1f公里";
		} else {
			formatStr = "%1$.1f米";
		}
		return String.format(formatStr, (float) meter);
	}

	/**
	 * 标尺转换米
	 * 
	 * @param scale
	 * @return
	 */
	public static String getScaleText(double scale) {
		if (scale <= 0) {
			return "0米";
		}
		scale /= 100;// 转成厘米：米，用来显示一厘米的长度
		String formatStr = null;
		if (scale >= 1000) {
			scale /= 1000;
			formatStr = "%1$.1f公里";
		} else if (scale >= 1) {
			formatStr = "%1$.1f米";
		} else if (scale >= 0.1) {
			scale *= 10;
			formatStr = "%1$.1f分米";
		} else {
			scale *= 100;
			formatStr = "%1$.1f厘米";
		}
		return String.format(formatStr, (float) scale);
	}

	/**
	 * 两点间距离转换米
	 * 
	 * @param scale
	 * @return
	 */
	public static String getLongText(double scale) {
		if (scale <= 0) {
			return "0米";
		}
		String formatStr = null;
		if (scale >= 1000) {
			scale /= 1000;
			formatStr = "%1$.1f公里";
		} else if (scale >= 1) {
			formatStr = "%1$.1f米";
		} else if (scale >= 0.1) {
			scale *= 10;
			formatStr = "%1$.1f分米";
		} else {
			scale *= 100;
			formatStr = "%1$.1f厘米";
		}
		return String.format(formatStr, (float) scale);
	}

	public static TextSymbol getLableSymbol(int size, String text, String color) {
		TextSymbol lableSymbol = getLableSymbol(size, text,
				Color.parseColor(color));
		return lableSymbol;
	}

	/**
	 * @param size
	 * @param text
	 * @param color
	 * @return
	 */
	public static TextSymbol getLableSymbol(int size, String text, int color) {
		TextSymbol lableSymbol = new TextSymbol(Tools.dipToPx(
				BaseApp.getmContext(), size), text, color);
		lableSymbol.setFontFamily("DroidSansFallback.ttf");
		return lableSymbol;
	}

	/**
	 * 转换文字到图片参考YupontGIS
	 * 
	 * @param con
	 * @param text
	 * @param width
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getTTFBitmap(Context con, char text, int length,
			int color) {
		Typeface tf = Typeface
				.createFromAsset(con.getAssets(), "YupontGIS.otf");
		return getTextBitmap(con, text, length, color, tf);
	}

	/**
	 * 转换文字到图片参考YupontGIS，并且在中间添加白线
	 * 
	 * @param con
	 * @param text
	 * @param width
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getWhiteTTFBitmap(Context con, char text, int length,
			int color) {
		Typeface tf = Typeface
				.createFromAsset(con.getAssets(), "YupontGIS.otf");
		return getWhiteTextBitmap(con, text, length, color, tf);
	}

	/**
	 * 垂直居中对其：绘制字符文件时需要已（0,0）为中心点开始绘制
	 * 
	 * @param con
	 * @param text
	 * @param width
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getTextBitmap(Context con, char text, int length,
			int color, Typeface tf) {
		Rect bounds = new Rect();
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(length);
		if (tf != null) {
			paint.setTypeface(tf);
		}
		// 测量文字的大小放入bounds里
		paint.getTextBounds("" + text, 0, 1, bounds);
		int x = Math.abs(bounds.left) > Math.abs(bounds.right) ? Math
				.abs(bounds.left) * 2 : Math.abs(bounds.right) * 2;
		int y = Math.abs(bounds.top) > Math.abs(bounds.bottom) ? Math
				.abs(bounds.top) * 2 : Math.abs(bounds.bottom) * 2;
		// 垂直居中对其
		Bitmap bitmap = Bitmap.createBitmap(x, y, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		paint.setColor(color);
		paint.setStrokeWidth(Tools.dipToPx(con, EquipData.PAINT_WIDTH));
		canvas.drawText("" + text, x / 2, y / 2, paint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return bitmap;
	}

	/**
	 * 要素需要居中对齐,并且在中间添加白线
	 * 
	 * @param con
	 * @param text
	 * @param width
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getWhiteTextBitmap(Context con, char text, int length,
			int color, Typeface tf) {
		Rect bounds = new Rect();
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(length);
		if (tf != null) {
			paint.setTypeface(tf);
		}
		// 测量文字的大小放入bounds里
		paint.getTextBounds("" + text, 0, 1, bounds);
		int x = Math.abs(bounds.left) > Math.abs(bounds.right) ? Math
				.abs(bounds.left) * 2 : Math.abs(bounds.right) * 2;
		int y = Math.abs(bounds.top) > Math.abs(bounds.bottom) ? Math
				.abs(bounds.top) * 2 : Math.abs(bounds.bottom) * 2;
		// 垂直居中对其
		Bitmap bitmap = Bitmap.createBitmap(x, y, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(Tools.dipToPx(con, EquipData.PAINT_WIDTH));
		canvas.drawLine(0, y / 2, x, y / 2, paint);
		paint.setColor(color);
		paint.setStrokeWidth(Tools.dipToPx(con, EquipData.PAINT_WIDTH));
		canvas.drawText("" + text, x / 2, y / 2, paint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		return bitmap;
	}

	/**
	 * 要素需要居中对齐,并且在中间添加白线
	 * 
	 * @param con
	 * @param text
	 * @param width
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getWhitePicture(Context con, int length, int color,
			Picture p) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// 垂直居中对其
		Bitmap bitmap = Bitmap.createBitmap(length, length, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(Tools.dipToPx(con, EquipData.PAINT_WIDTH));
		canvas.drawLine(0, length / 2, length, length / 2, paint);
		canvas.drawPicture(p);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return bitmap;
	}
}
