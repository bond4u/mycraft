package org.noise;

/**
 * Converts [0..+1] range to [-1..+1] range.
 */
public class Convert1D implements IConv1D {
	
	private float fromStart;
	private float fromEnd;
	private float toStart;
	private float toEnd;
	
	@Override
	public void setFrom(float x1, float x2) {
		fromStart = x1;
		fromEnd = x2;
	}
	
	@Override
	public void setTo(float x1, float x2) {
		toStart = x1;
		toEnd = x2;
	}
	
	@Override
	public float get(float x) {
		float x2;
		float dx = fromEnd - fromStart;
		if ((x >= fromStart && x <= fromEnd) || (x <= fromStart && x >= fromEnd)) {
			float d = x - fromStart;
			float d2 = toEnd - toStart;
			x2 = -1 + (d2 * dx * d);
		} else if ((dx > 0 && x < fromStart) || (dx < 0 && x < fromEnd)) {
			x2 = toStart;
		} else /*if (dx < 0 && (x > start || x < end))*/ {
			x2 = toEnd;
		}
		return x2;
	}

}
