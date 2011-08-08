package org.noise;

import java.util.Random;

public class Ground implements IFunc2D {
	
	private final short MULT = 16;
	
	private IMult coord;
	
	private IFunc2D src;
	
	private IMult height;
	
	public Ground(Random r) {
		// scale plane (more detailed terrain)
		coord = createPlaneMult();
		// generate heightmap
		src = createHeightMap2D(r);
		// scale height up
		height = createHeightMult();
	}
	
	protected short getMult() {
		return MULT;
	}
	
	protected IMult createPlaneMult() {
		Mult m = new Mult();
		m.setPower(1f / getMult()/*0.125f*/);
		return m;
	}
	
	protected IFunc2D createHeightMap2D(Random r) {
		FBM2 f = new FBM2(r);
		f.setOctaves(2);
		f.setLacunarity(1.75f);
		f.setGain(1f);
		return f;
	}
	
	protected IMult createHeightMult() {
		Mult m = new Mult();
		m.setPower(getMult()/*0.3f*/);
		return m;
	}
	
	@Override
	public float get(float x, float y) {
		float x2 = coord.get(x);
		float y2 = coord.get(y);
		float f = src.get(x2, y2); // get height of a point
		f = height.get(f); // make it x times higher
		return f;
	}

}
