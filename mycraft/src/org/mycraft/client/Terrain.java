package org.mycraft.client;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.noise.Ground;
import org.noise.IFunc2D;

public class Terrain {
	
	private static final int DIM = 16;
//	private final Random rnd;
	private final IFunc2D func;
	private final Comparator<Point2i> comp;
	private final Map<Point2i, Block> blocks;
	
	private short lowest;
	private short highest;
	
	public Terrain(Random r) {
//		rnd = r;
		func = new Ground(r);
		comp = new Point2i(0, 0);
		blocks = new TreeMap<Point2i, Block>(comp);
//		init();
	}
	
	public void create() {
		lowest = Short.MAX_VALUE;
		highest = Short.MIN_VALUE;
		short blkDim = Block.getDim();
		final long startTime = System.currentTimeMillis();
		for (int x = -DIM / 2; x <= DIM / 2; x++) {
			for (int y = -DIM / 2; y <= DIM / 2; y++) {
				final int xx = x * blkDim;
				final int yy = y * blkDim;
//				log("p=" + xx + "x" + yy);
				Point2i p = new Point2i(xx, yy);
				Block b = new Block(this, xx, yy/*, rnd*/, func);
				blocks.put(p, b);
				short l = b.lowest();
				if (l < lowest) {
					lowest = l;
				}
				short h = b.highest();
				if (h > highest) {
					highest = h;
				}
			}
		}
		final long delta = System.currentTimeMillis() - startTime;
		log("terrain created in " + delta + " millis; low=" + lowest + " high=" + highest);
		assert lowest == highest : "flat terrain";
	}
	
	public short lowest() {
		return lowest;
	}
	
	public short highest() {
		return highest;
	}
	
	public void init() {
		final long startTime = System.currentTimeMillis();
		for (Block b : blocks.values()) {
			b.initVBO();
		}
		final long delta = System.currentTimeMillis() - startTime;
		log("terrain filled in " + delta + " millis");
	}
	
	public void draw() {
		for (Block b : blocks.values()) {
			b.draw();
		}
	}
	
	public void destroy() {
		for (Block b : blocks.values()) {
			b.destroy();
		}
	}
	
	public short getHeightAt(int x, int y) {
		short blkDim = Block.getDim();
		// terrain coords to block center coords
		final short d = (short) ((blkDim - 1) / 2);
		final int fx = x + d + (x >= 0 ? 0 : 1);
		final int fy = y + d + (y >= 0 ? 0 : 1);
		final int blockX = blkDim * (fx / blkDim - (x<-d?1:0));
		final int blockY = blkDim * (fy / blkDim - (y<-d?1:0));
		assert Math.abs(x - blockX) <= d : "block center x cant be farther than 4";
		assert Math.abs(y - blockY) <= d : "block center y cant be farther than 4";
		final Point2i p = new Point2i(blockX, blockY);
		final Block b = blocks.get(p);
		short h = 0;
		if (b != null) {
			h = b.getHeightAt(x, y);
		}
		return h;
	}
	
	protected void log(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
	
	private void logGlErrorIfAny() {
		final int e = GL11.glGetError();
		if (e != 0) {
			log("err=" + e + ": " + GLU.gluErrorString(e));
		}
	}

}
