package com.ieds.gis.map.data;

import com.lidroid.xutils.exception.NullArgumentException;
import com.lidroid.xutils.util.FileUtil;

public class FileData {
	/*************************** 本地下载的系统apk更新包所在地址 ***************************/
	public static final String apkPath() throws NullArgumentException {
		return FileUtil.getSdcardPath() + "/" + "巡检系统.apk";
	}

}
