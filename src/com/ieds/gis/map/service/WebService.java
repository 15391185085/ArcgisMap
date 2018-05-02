package com.ieds.gis.map.service;

import com.ieds.gis.base.util.GisHttpUtils;
import com.ieds.gis.base.util.SettingUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.exception.NullArgumentException;
import java.util.Map;

/**
 * 获取和服务器通信的相关数据
 * 
 * @update 2014-9-26 上午9:27:51<br>
 * @author <a href="mailto:gaohaiyanghy@126.com">高海燕</a>
 * 
 */
public class WebService {
	/**
	 * 可以一次上传下载多个对象
	 * 
	 * @param urlEnd
	 * @param po
	 *            存Map<key，json>格式的键值对
	 * @return
	 * @throws HttpException
	 * @throws NullArgumentException
	 */
	public static String postHttpData(String urlEnd, String json)
			throws HttpException, NullArgumentException {
		if (json == null) {
			throw new NullArgumentException();
		}

		return GisHttpUtils.postHttpData(getDataUrlHead() + "/" + urlEnd, json);
	}

	/**
	 * 可以一次上传下载多个对象
	 * 
	 * @param urlEnd
	 * @param po
	 *            存Map<key，json>格式的键值对
	 * @return
	 * @throws HttpException
	 * @throws NullArgumentException
	 */
	public static String getHttpData(String urlEnd, Map<String, Object> po)
			throws HttpException, NullArgumentException {
		if (po == null) {
			throw new NullArgumentException();
		}

		return GisHttpUtils.getHttpData(getDataUrlHead() + "/" + urlEnd, po);
	}

	public static String getDataUrlHead() {
		// 直接使用默认ip
		String ip = SettingUtil.getInstance().getIp();
		String port = SettingUtil.getInstance().getPort();
		return "http://" + ip + ":" + port;
	}

}
