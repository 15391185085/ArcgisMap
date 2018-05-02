package com.ieds.gis.map.test.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.view.View;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.symbol.TextSymbol;
import com.ieds.gis.base.dao.Selector;
import com.ieds.gis.base.test.dao.SqliteDAO;
import com.ieds.gis.map.gdb.GdbGraphicsLayer;
import com.ieds.gis.map.gdb.IGeometryTable;
import com.ieds.gis.map.page.MapActivity;
import com.ieds.gis.map.service.INetworkService;
import com.ieds.gis.map.test.po.DIS_P_PDKG_L;
import com.ieds.gis.map.test.po.MyLayer;
import com.ieds.gis.map.test.po.SYS_SYMBOL;
import com.ieds.gis.map.test.service.TestNetworkService;
import com.ieds.gis.map.test.unit.MySymobSvg;
import com.ieds.gis.map.util.WktUtil;
import com.lidroid.xutils.exception.DbException;

public class TestMapActivity extends MapActivity {

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getOnlineUrl() {
		List<String> sList = new ArrayList<String>();
		sList.add("https://map23.epa.gov/arcgis/rest/services/cimc/Cleanups_geoplatform/MapServer");
		return sList;
	}

	@Override
	public INetworkService getINetworkService() {
		// TODO Auto-generated method stub
		return new TestNetworkService(this);
	}

	@Override
	public void onMapSingleTap(Point earthP) throws DbException {

	}

	@Override
	public void ReloadLayout() throws Exception {
		Polygon p = mapView.getExtent();
		Polyline pl = new Polyline();
		pl.startPath(p.getPoint(0));
		pl.lineTo(p.getPoint(2));

		Double dd = WktUtil.getLatLonDistance(
				(Point) WktUtil.getLatLonFromEarth(p.getPoint(0), mapView),
				(Point) WktUtil.getLatLonFromEarth(p.getPoint(2), mapView));

		DIS_P_PDKG_L d = new DIS_P_PDKG_L();
		d.setGeometry(WktUtil.getWktFromGeometry(WktUtil.getLatLonFromEarth(pl,
				mapView)));
		d.setId("1");
		d.setName("开关");
		SqliteDAO.getInstance().replace(d);

		Map<String, String> svnMap = new HashMap<String, String>();

		MyLayer m = new MyLayer("DIS_P_PDKG_L", 18055.954822, 0.0, 4513.988705,
				0.0, 1);
		List<SYS_SYMBOL> sList = SqliteDAO.getInstance().findAll(
				Selector.from(SYS_SYMBOL.class));
		for (Iterator iterator = sList.iterator(); iterator.hasNext();) {
			SYS_SYMBOL sys_SYMBOL = (SYS_SYMBOL) iterator.next();
			svnMap.put(sys_SYMBOL.getId(), sys_SYMBOL.getContent());
		}

		MySymobSvg ms = new MySymobSvg(svnMap, mapView);
		GdbGraphicsLayer g = new GdbGraphicsLayer(ms, DIS_P_PDKG_L.class, m,
				SqliteDAO.getInstance());
		this.mapView.addLayer(g);
	}
}
