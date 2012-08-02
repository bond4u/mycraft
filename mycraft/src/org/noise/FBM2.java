package org.noise;

public class FBM2 extends FBM3 implements IFbm2D {
	
	public FBM2(PermutationsTable pTbl) {
		super(pTbl);
	}
	
	@Override
	public float get(float x, float y) {
		return super.get(x, y, 0f);
	}
	
}
