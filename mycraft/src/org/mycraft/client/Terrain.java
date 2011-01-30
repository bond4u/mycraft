package org.mycraft.client;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Terrain {
	
	private static final int DIM = 12;
	private final Random rnd;
	private final Comparator<Point2i> comp;
	private final Map<Point2i, Block> blocks;
	
	public Terrain(Random r) {
		rnd = r;
		comp = new Point2i(0, 0);
		blocks = new TreeMap<Point2i, Block>(comp);
//		init();
	}
	
	public void create() {
		final long startTime = System.currentTimeMillis();
		for (int x = -DIM / 2; x <= DIM / 2; x++) {
			for (int y = -DIM / 2; y <= DIM / 2; y++) {
				final int xx = x * Block.DIM;
				final int yy = y * Block.DIM;
				Point2i p = new Point2i(xx, yy);
				log("p=" + xx + "x" + yy);
				blocks.put(p, new Block(this, xx, yy, rnd));
			}
		}
		final long delta = System.currentTimeMillis() - startTime;
		log("terrain created in " + delta + " millis");
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
		// terrain coords to block center coords
		final short d = (Block.DIM - 1) / 2;
		final int fx = x + d + (x >= 0 ? 0 : 1);
		final int fy = y + d + (y >= 0 ? 0 : 1);
		final int blockX = Block.DIM * (fx / Block.DIM - (x<-4?1:0));
		final int blockY = Block.DIM * (fy / Block.DIM - (y<-4?1:0));
		assert Math.abs(x - blockX) <= 4 : "block center x cant be farther than 4";
		assert Math.abs(y - blockY) <= 4 : "block center y cant be farther than 4";
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
