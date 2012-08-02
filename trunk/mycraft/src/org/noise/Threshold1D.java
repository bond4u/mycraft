package org.noise;

/**
 * Limits values above ABOVE value to TOPvalue
 * and values below BELOW value to BOTTOM value.
 */
public class Threshold1D implements ILimit1D {
	
	private Condition cond;
	private float lim;
	private float val;
	
	@Override
	public void set(Condition c1, float x1, float x2) {
		cond = c1;
		lim = x1;
		val = x2;
	}
	
	@Override
	public float get(float x) {
		float x2 = x;
		if (Condition.ABOVE == cond && x > lim) {
			x2 = val;
		} else if (Condition.ABOVE_OR_EQUAL == cond && x >= lim) {
			x2 = val;
		} else if (Condition.BELOW == cond && x < lim) {
			x2 = val;
		} else if (Condition.BELOW_OR_EQUAL == cond && x <= lim) {
			x2 = val;
		}
		return x2;
	}
	
}
