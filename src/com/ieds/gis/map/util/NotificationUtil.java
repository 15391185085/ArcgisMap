package com.ieds.gis.map.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.ieds.gis.base.BaseApp;
import com.ieds.gis.base.R;
import com.ieds.gis.base.dialog.BaseDialog;
import com.ieds.gis.base.dialog.MyToast;
import com.ieds.gis.base.util.InstallApk;
import com.ieds.gis.base.util.ProgressTask;
import com.ieds.gis.map.data.FileData;
import com.ieds.gis.map.gps.GpsService;
import com.ieds.gis.map.page.MapActivity;
import com.ieds.gis.map.service.INetworkService;
import com.ieds.gis.map.service.WebService;
import com.lidroid.xutils.util.DateUtil;
import com.lidroid.xutils.util.LogUtils;

public class NotificationUtil {
	private Activity act;
	private BroadcastReceiver receiver;
	private INetworkService iNetworkService;

	public NotificationUtil(Activity act, INetworkService iNetworkService) {
		super();
		this.act = act;
		this.iNetworkService = iNetworkService;
	}

	@SuppressLint("NewApi")
	private boolean isNotificationEnabled(Context context) {

		String CHECK_OP_NO_THROW = "checkOpNoThrow";
		String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

		AppOpsManager mAppOps = (AppOpsManager) context
				.getSystemService(Context.APP_OPS_SERVICE);
		ApplicationInfo appInfo = context.getApplicationInfo();
		String pkg = context.getApplicationContext().getPackageName();
		int uid = appInfo.uid;

		Class appOpsClass = null;
		/* Context.APP_OPS_MANAGER */
		try {
			appOpsClass = Class.forName(AppOpsManager.class.getName());
			Method checkOpNoThrowMethod = appOpsClass
					.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
							String.class);
			Field opPostNotificationValue = appOpsClass
					.getDeclaredField(OP_POST_NOTIFICATION);

			int value = (Integer) opPostNotificationValue.get(Integer.class);
			return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid,
					pkg) == AppOpsManager.MODE_ALLOWED);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 显示提醒
	 */
	public void showWarn() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (!isNotificationEnabled(act)) {
				BaseDialog dialog = BaseDialog.getDialog(act,
						R.string.dialog_tips,
						"请开启本系统的通知许可！\n当前程序-通知管理-开启“允许通知”和“在状态栏上显示”", "确 定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								goToSET();
							}
						});
				dialog.setCancelable(false);
				dialog.show();

			}
		}

		LocationManager locManager = (LocationManager) act
				.getSystemService(Context.LOCATION_SERVICE);
		if (!locManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			BaseDialog dialog = BaseDialog.getDialog(act, R.string.dialog_tips,
					"请开启GPS功能！", "确 定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							goToGPS();
						}
					});
			dialog.setCancelable(false);
			dialog.show();
		}
		downApk();
	}

	/**
	 * 显示gps
	 * 
	 * @param tv
	 */
	public void showGPS(TextView tv) {
		LocationManager locManager = (LocationManager) act
				.getSystemService(Context.LOCATION_SERVICE);
		tv.setText(Html.fromHtml(GpsService.getGPSText(locManager)));
	}

	/**
	 * 同步时间
	 */
	public void updataTime(Date date) {
		LogUtils.d("循环检测-启动和服务器对时");
		try {
			final String realTimeResult = DateUtil.dateToString(date,
					DateUtil.FORMAT_ONE);

			boolean isSuccess = onTimeWithin(realTimeResult,
					DateUtil.getCurrDate(DateUtil.FORMAT_ONE), 10);
			if (!isSuccess) {
				act.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						BaseDialog dialog = BaseDialog.getDialog(act,
								R.string.dialog_tips, "您的时间有误，请修改到当前时间："
										+ realTimeResult, "确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent(
												android.provider.Settings.ACTION_DATE_SETTINGS);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										act.startActivity(intent);
									}
								}, "取消", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								});
						dialog.show();
					}
				});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 判断时间是否在指定的范围内
	 * 
	 * @update 2014-8-25 下午1:34:26<br>
	 * @author <a href="mailto:gaohaiyanghy@126.com">高海燕</a>
	 * @param firstTime
	 * @param lastTime
	 * @param range
	 *            允许的差值单位分钟
	 * @return true: 误差时间在范围内
	 */
	public boolean onTimeWithin(String firstTime, String lastTime, int range) {
		try {
			long firstDate = DateUtil.stringtoDate(firstTime,
					DateUtil.FORMAT_ONE).getTime();
			long lastDate = DateUtil
					.stringtoDate(lastTime, DateUtil.FORMAT_ONE).getTime();
			long distime = Math.abs(firstDate - lastDate);
			if (distime < range * 1000 * 60) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param con
	 * @param busiType
	 * @param equipType
	 * @param versioNo
	 * @param creator
	 * @param mobileSerial
	 * @param mobileName
	 */
	public void downSystemApk(final Activity con) {
		new ProgressTask(con) {

			private volatile String url;

			@Override
			public boolean onRunning() throws Exception {
				url = iNetworkService.downAPKPath();
				if (url != null) {
					return true;
				} else {
					this.setErrorInfo("安装失败，安装包没有在服务器部署！");
					return false;
				}
			}

			@Override
			public void onSucceed() {
				try {
					downAndInstallApkFile(url, FileData.apkPath());
				} catch (Exception e) {
					e.printStackTrace();
					MyToast.showToast(e.getMessage());
				}
			}

			@Override
			public void onFail(String errorInfo) {
				MyToast.showToast(errorInfo);
			}
		};
	}

	/**
	 * 下载并且安装程序包
	 * 
	 * @param fileUri
	 * @param filePath
	 */
	public void downAndInstallApkFile(String fileUri, final String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
		String serviceString = Context.DOWNLOAD_SERVICE;
		DownloadManager downloadManager = (DownloadManager) BaseApp
				.getmContext().getSystemService(serviceString);
		Uri uri = Uri.parse(fileUri);
		DownloadManager.Request request = new Request(uri);
		request.setTitle("下载");
		request.setDescription("巡检系统安装包正在下载");
		request.setNotificationVisibility(View.VISIBLE);
		request.setDestinationUri(Uri.fromFile(new File(filePath)));
		final long reference = downloadManager.enqueue(request);

		IntentFilter filter = new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		closeReceiver();
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				long id = intent.getLongExtra(
						DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				if (id == reference) {
					if (intent.getAction().equals(
							DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
						// 下载完成后，提示安装apk
						InstallApk.openFile(BaseApp.getmContext(), new File(
								filePath));
					}
				}
			}
		};
		BaseApp.getmContext().registerReceiver(receiver, filter);
	}

	public void close() {
		closeReceiver();
	}

	private void closeReceiver() {
		if (receiver != null) {
			BaseApp.getmContext().unregisterReceiver(receiver);
		}
	}

	public void downApk() {
		new ProgressTask(act) {
			@Override
			public boolean onRunning() throws Exception {
				return iNetworkService.isNewSystemAPK();
			}

			@Override
			public void onSucceed() {
				// TODO Auto-generated method stub
				BaseDialog dialog = BaseDialog
						.getDialog(
								act,
								R.string.dialog_tips,
								"您的客户端有新的版本需要升级，是否立刻执行？\n注意：安装新版本成功后需要点击‘更新用户与系统参数’按钮更新配置参数!",
								"升级", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										downSystemApk(act);
									}
								}, "取消", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										act.finish();
									}
								});
				dialog.show();
				dialog.setCancelable(false);
			}

			@Override
			public void onFail(String errorInfo) {
				// TODO Auto-generated method stub

			}
		};
	}

	private void goToGPS() {
		// 进入设置系统应用权限界面
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		act.startActivity(intent);
	}

	private void goToSET() {
		// 进入设置系统应用权限界面
		Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
		act.startActivity(intent);
	}
}
