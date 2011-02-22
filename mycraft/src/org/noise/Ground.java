package org.noise;

import java.util.Random;

public class Ground implements IFunc2D {
	
	private final short MULT = 16;
	
	private Mult coord;
	
	private FBM src;
	
	private Mult height;
	
	public Ground(Random r) {
		coord = new Mult();
		coord.setPower(1f / MULT/*0.125f*/);
		src = new FBM(r);
		src.setOctaves(2);
		src.setLacunarity(1.75f);
		src.setGain(1f);
		height = new Mult();
		height.setPower(MULT/*0.3f*/);
	}
	
	@Override
	public float get(float x, float y) {
		float x2 = coord.get(x);
		float y2 = coord.get(y);
		float f = src.get(x2, y2); // get height of a point
		f = height.get(f); // make it 4 times higher
		return f;
	}

}
