package org.noise;

import java.util.Random;

public class FBM2 extends FBM3 implements IFbm2D {
	
	public FBM2(Random s) {
		super(s);
	}
	
	@Override
	public float get(float x, float y) {
		return super.get(x, y, 0f); // easy hack
	}
	
}
