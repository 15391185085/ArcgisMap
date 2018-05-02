package com.ieds.gis.map.svgtoline;

import java.util.ArrayList;
import java.util.List;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.ieds.gis.base.dao.Selector;

/*

 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

/**
 * Entry point for parsing SVG files for Android. Use one of the various static
 * methods for parsing SVGs by resource, asset or input stream. Optionally, a
 * single color can be searched and replaced in the SVG while parsing. You can
 * also parse an svg path directly.
 * 
 * @author Larva Labs, LLC
 * @see #getSVGFromResource(android.content.res.Resources, int)
 * @see #getSVGFromAsset(android.content.res.AssetManager, String)
 * @see #getSVGFromString(String)
 * @see #getSVGFromInputStream(java.io.InputStream)
 * @see #parsePath(String)
 */
public class SVGPath {
	private MapView mv;

	public SVGPath(MapView mv) {
		super();
		this.mv = mv;
	}

	/**
	 * This is where the hard-to-parse paths are handled. Uppercase rules are
	 * absolute positions, lowercase are relative. Types of path rules:
	 * <p/>
	 * <ol>
	 * <li>M/m - (x y)+ - Move to (without drawing)
	 * <li>Z/z - (no params) - Close path (back to starting point)
	 * <li>L/l - (x y)+ - Line to
	 * <li>H/h - x+ - Horizontal ine to
	 * <li>V/v - y+ - Vertical line to
	 * <li>C/c - (x1 y1 x2 y2 x y)+ - Cubic bezier to
	 * <li>S/s - (x2 y2 x y)+ - Smooth cubic bezier to (shorthand that assumes
	 * the x2, y2 from previous C/S is the x1, y1 of this bezier)
	 * <li>Q/q - (x1 y1 x y)+ - Quadratic bezier to
	 * <li>T/t - (x y)+ - Smooth quadratic bezier to (assumes previous control
	 * point is "reflection" of last one w.r.t. to current point)
	 * </ol>
	 * <p/>
	 * Numbers are separate by whitespace, comma or nothing at all (!) if they
	 * are self-delimiting, (ie. begin with a - sign)
	 * 
	 * @param s
	 *            the path string from the XML
	 */
	public List<Polyline> doPath(String s) {
		List<Polyline> list = new ArrayList<Polyline>();
		int n = s.length();
		ParserHelper ph = new ParserHelper(s, 0);
		ph.skipWhitespace();
		Polyline p = new Polyline();
		float lastX = 0;
		float lastY = 0;
		float lastX1 = 0;
		float lastY1 = 0;
		float subPathStartX = 0;
		float subPathStartY = 0;
		char prevCmd = 0;
		while (ph.pos < n) {
			char cmd = s.charAt(ph.pos);
			switch (cmd) {
			case '-':
			case '+':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				if (prevCmd == 'm' || prevCmd == 'M') {
					cmd = (char) (((int) prevCmd) - 1);
					break;
				} else if (prevCmd == 'c' || prevCmd == 'C') {
					cmd = prevCmd;
					break;
				} else if (prevCmd == 'l' || prevCmd == 'L') {
					cmd = prevCmd;
					break;
				}
			default: {
				ph.advance();
				prevCmd = cmd;
			}
			}

			boolean wasCurve = false;
			switch (cmd) {
			case 'm':
			case 'M':
				float x = ph.nextFloat();
				float y = ph.nextFloat();
				subPathStartX = x;
				subPathStartY = y;
				p.startPath(x, y);
				lastX = x;
				lastY = y;
				break;
			case 'a':
			case 'A':
				float rx = ph.nextFloat();
				float ry = ph.nextFloat();
				float theta = ph.nextFloat();
				int largeArc = (int) ph.nextFloat();
				int sweepArc = (int) ph.nextFloat();
				x = ph.nextFloat();
				y = ph.nextFloat();
				Polyline newP = (Polyline) GeometryEngine.geodesicEllipse(
						new Point(y, y), mv.getSpatialReference(), rx, ry, 0,
						10, null, Geometry.Type.POLYLINE);
				list.add(newP);
				lastX = x;
				lastY = y;
				break;
			case 'z':
			case 'Z':
				p.lineTo(subPathStartX, subPathStartY);
				lastX = subPathStartX;
				lastY = subPathStartY;
				lastX1 = subPathStartX;
				lastY1 = subPathStartY;
				wasCurve = true;
				list.add(p);
				return list;
			case 'l':
			case 'L':
				x = ph.nextFloat();
				y = ph.nextFloat();
				p.lineTo(x, y);
				lastX = x;
				lastY = y;
				break;
			case 'h':
			case 'H':
				x = ph.nextFloat();
				p.lineTo(x, lastY);
				lastX = x;
				break;
			case 'v':
			case 'V':
				y = ph.nextFloat();
				p.lineTo(lastX, y);
				lastY = y;
				break;
			}
			if (!wasCurve) {
				lastX1 = lastX;
				lastY1 = lastY;
			}
			ph.skipWhitespace();
		}
		list.add(p);
		return list;
	}

}
