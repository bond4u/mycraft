package org.noise;

import java.util.Random;

import org.mycraft.client.BlockType;

/*
 * Generates heightmap and scales it.
 * Creates permutations table.
 * Calculates noise at 2d point x,z.
 * Scales y up.
 * Height y at point x,z results 3d point x,y,z aka heightmap.
 */
public class Ground {
	
	private final short MULT = 16;
	
	private PermutationsTable tbl;
	
	private IMult coord;
	
	private IFunc2D src;
	
	private IMult height;
	
	private PermutationsTable tbl2;
	
	private Turbulence turb;
	
	public Ground(Random r) {
		// heightmap table
		tbl = createPermTbl(r);
		// scale plane (more detailed terrain)
		coord = createPlaneMult();
		// generate heightmap
		src = createHeightMap2D(tbl);
		// scale height up
		height = createHeightMult();
		// caves table
		tbl2 = createPermTbl(r);
		// caves
		turb = createTurbulence(tbl2);
	}
	
	protected PermutationsTable createPermTbl(Random r) {
		PermutationsTable t = new PermutationsTable(r);
		return t;
	}
	
	protected short getMult() {
		return MULT;
	}
	
	protected IMult createPlaneMult() {
		Mult m = new Mult();
		m.setPower(1f / getMult()/*0.125f*/);
		return m;
	}
	
	protected IFunc2D createHeightMap2D(PermutationsTable pTbl) {
		FBM2 f = new FBM2(pTbl);
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
	
	protected Turbulence createTurbulence(PermutationsTable pTbl) {
		Turbulence t = new Turbulence(pTbl);
		t.set(0.5f, 1f);
		return t;
	}
	
//	protected float round(float f) {
//		float r = (f >= 0f) ? f + 0.49999999f : f - 0.49999999f;
//		double d = (r >= 0f) ? Math.floor(r) : Math.ceil(r);
//		return (float) d;
//	}

	public BlockType get(float x, float y, float z) {
		float x2 = coord.get(x);
		float z2 = coord.get(z);
		float y2 = src.get(x2, z2);
		y2 = height.get(y2);
//		float y3 = round(y2);
		BlockType bt = BlockType.Air;
		if (y <= y2) {
			// underground, start digging
			float t = turb.get(x2, coord.get(y), z2);
		    if (t < 1.0f) {
		    	bt = BlockType.Ground;
		    }
		}
		return bt;
	}

}
