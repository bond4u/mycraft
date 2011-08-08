package org.mycraft.client;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;

import org.noise.IFunc2D;
import org.noise.TestGround;

public class TestTerrain extends Terrain {
	
	private Map<Point2i, Block> blocks;
	
	public TestTerrain(Random r, Viewport v, Camera c) {
		super(r, v, c);
	}
	
	// override ground
	protected IFunc2D createFunc(Random r) {
		return new TestGround(r);
	}
	
	// override blocks map creation
	protected Map<Point2i, Block> createBlocksMap(Comparator<Point2i> c) {
		blocks = super.createBlocksMap(c);
		return blocks;
	}
	
	// override blocks creation
	public void create() {
		resetLowHigh();
		// just create 2 neighboring blocks
		final short blkDim = Block.getDim();
		int x1 = 0;
		int y1 = 0;
		Point2i p1 = new Point2i(x1, y1);
		Block b1 = createBlock(x1, y1);
		blocks.put(p1, b1);
		checkLowHigh(b1.lowest(), b1.highest());
		int x2 = -blkDim;
		int y2 = 0;
		Point2i p2 = new Point2i(x2, y2);
		Block b2 = createBlock(x2, y2);
		blocks.put(p2, b2);
		checkLowHigh(b2.lowest(), b2.highest());
	}
	
	protected Block createBlock(int x, int y) {
		return new Block(this, x, y, getFunc()) {
			protected int createVBO() {
				return 0;
			}
		};
	}
	
}
