package com.ieds.gis.map.gdb;

import java.io.IOException;

import com.esri.core.symbol.PictureMarkerSymbol;
import com.ieds.gis.map.svgtoline.SvgBo;

public class SvnPictureMarkerSymbol extends PictureMarkerSymbol {
	private SvgBo svgBo;

	public SvgBo getSvgBo() {
		return svgBo;
	}

	public void setSvgBo(SvgBo svgBo) {
		this.svgBo = svgBo;
	}

	public SvnPictureMarkerSymbol(SvgBo svgBo) throws IOException {
		super();
		this.svgBo = svgBo;
	}

}
