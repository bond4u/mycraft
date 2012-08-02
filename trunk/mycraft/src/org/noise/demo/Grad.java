package org.noise.demo;

import org.noise.IFunc2D;

public class Grad implements IFunc2D {

	@Override
	public float get(float x, float y) {
		return y;
	}

}
