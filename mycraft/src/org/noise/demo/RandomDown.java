package org.noise.demo;

import java.util.Random;

import org.noise.IConv1D;

/**
 * Convert range [0,5..0,75] to wider range [0,25..0,75] by adding random number [0..0,25].
 */
public class RandomDown implements IConv1D {

	private float fromX1;
	private float fromX2;
	private float toX1;
	private float toX2;
	
	private Random rnd = new Random();
	
	@Override
	public void set(float fx1, float fx2, float tx1, float tx2) {
		fromX1 = fx1;
		fromX2 = fx2;
		toX1 = tx1;
		toX2 = tx2;
	}

	@Override
	public float get(float x) {
		float x2 = x;
		if (x >= fromX1 && x <= fromX2) {
			float fdx = fromX2 - fromX1;
			float tdx = toX2 - toX1;
			float d = fromX2 - x;
			float r = rnd.nextFloat() / (1f / (tdx - fdx)); // add max 0.25
			x2 = toX2 - (d + r);
		}
		return x2;
	}

}
