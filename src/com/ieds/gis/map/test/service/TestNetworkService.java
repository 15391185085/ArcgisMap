package com.ieds.gis.map.test.service;

import android.content.Context;

import com.ieds.gis.map.service.INetworkService;

public class TestNetworkService implements INetworkService {

	private Context con;

	public TestNetworkService(Context con) {
		super();
		this.con = con;
	}

	@Override
	public String downAPKPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNewSystemAPK() {
		// TODO Auto-generated method stub
		return false;
	}

}
