package org.noise.demo;

import org.noise.IFunc1D;

/**
 * returns sine of given x
 */
public class Sine implements IFunc1D {

	@Override
	public float get(float x) {
		// 180 deg = pi rad
		// x deg = y rad = x * PI / 180
//		double rad = (x * Math.PI) / 180.0;
		double r = Math.sin(x);
		return (float)r;
	}

}
