package org.noise;

/**
 * Limits values above ABOVE value to TOPvalue
 * and values below BELOW value to BOTTOM value.
 */
public class Threshold1D implements ILimit1D {
	
	private float above;
	private float upper;
	private float below;
	private float lower;
	
	@Override
	public void setAbove(float x1, float x2) {
		above = x1;
		upper = x2;
	}

	@Override
	public void setBelow(float x1, float x2) {
		below = x1;
		lower = x2;
	}
	
	@Override
	public float get(float x) {
		float x2 = (x > above) ? upper : ((x < below) ? lower : x);
		return x2;
	}
	
}
