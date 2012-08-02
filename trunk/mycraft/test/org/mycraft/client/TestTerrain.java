package org.mycraft.client;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.SharedDrawable;
import org.noise.Ground;
import org.noise.TestGround;

public class TestTerrain extends Terrain {
	
	private Map<Point3f, Block> blocks;
	
	public TestTerrain(Random r, Viewport v, Camera c) {
		super(r, v, c);
	}
	
	// override ground
	protected Ground createGenerator(Random r) {
		return new TestGround(r);
	}
	
	// override blocks map creation
	protected Map<Point3f, Block> createRenderBlocksMap(Comparator<Point3f> c) {
		blocks = super.createRenderBlocksMap(c);
		return blocks;
	}
	
	protected SharedDrawable createSharedDrawable() {
		return null; // not supported in tests
	}
	
	// override blocks creation
	public void create() {
//		resetLowHigh();
		// just create 2 neighboring blocks
		final short blkDim = Block.getDim();
		float x1 = 0f;
		float y1 = 0f;
		float z1 = 0f;
		Point3f p1 = new Point3f(x1, y1, z1);
		Block b1 = createBlock(x1, y1, z1);
		blocks.put(p1, b1);
//		checkLowHigh(b1.lowest(), b1.highest());
		float x2 = -blkDim;
		float y2 = 0;
		float z2 = 0;
		Point3f p2 = new Point3f(x2, y2, z2);
		Block b2 = createBlock(x2, y2, z2);
		blocks.put(p2, b2);
//		checkLowHigh(b2.lowest(), b2.highest());
	}
	
	protected Block createBlock(float x, float y, float z) {
		return new Block(this, x, y, z/*getFunc()*/) {
//			protected int createVBO() {
//				return 0;
//			}
		};
	}
	
}
