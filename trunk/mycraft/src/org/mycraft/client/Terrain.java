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
		func = createFunc(r); //new Ground(r);
		comp = new Point2i(0, 0);
		blocks = new TreeMap<Point2i, Block>(comp);
//		init();
	}
	
	protected IFunc2D createFunc(Random r) {
		return new Ground(r);
	}
	
	protected IFunc2D getFunc() {
		return func;
	}
	
	protected Map<Point2i, Block> getMap() {
		return blocks;
	}
	
	public void create() {
		resetLowHigh();
		short blkDim = Block.getDim();
		final long startTime = System.currentTimeMillis();
		for (int x = -DIM / 2; x <= DIM / 2; x++) {
			for (int y = -DIM / 2; y <= DIM / 2; y++) {
				final int xx = x * blkDim;
				final int yy = y * blkDim;
//				log("p=" + xx + "x" + yy);
				Point2i p = new Point2i(xx, yy);
				Block b = createBlock(xx, yy);
				getMap().put(p, b);
				checkLowHigh(b.lowest(), b.highest());
			}
		}
		final long delta = System.currentTimeMillis() - startTime;
		log("terrain created in " + delta + " millis; low=" + lowest + " high=" + highest);
		assert lowest == highest : "flat terrain";
	}
	
	protected void resetLowHigh() {
		lowest = Short.MAX_VALUE;
		highest = Short.MIN_VALUE;
	}
	
	protected Block createBlock(int x, int y) {
		return new Block(this, x, y, getFunc());
	}
	
	protected void checkLowHigh(short l, short h) {
		if (l < lowest) {
			lowest = l;
		}
		if (h > highest) {
			highest = h;
		}
	}
	
	public short lowest() {
		return lowest;
	}
	
	public short highest() {
		return highest;
	}
	
	public void init() {
		final long startTime = System.currentTimeMillis();
		for (Block b : getMap().values()) {
			b.initVBO();
		}
		final long delta = System.currentTimeMillis() - startTime;
		log("terrain filled in " + delta + " millis");
	}
	
	public void draw() {
//		long start = System.currentTimeMillis();
		int cnt = 0;
		for (Block b : getMap().values()) {
			b.draw();
			cnt++;
		}
//		long duration = System.currentTimeMillis() - start;
//		if (duration > 1000 / 60) {
//			log("terrain.draw " + cnt + " blocks in " + duration + " ms");
//		}
	}
	
	public void destroy() {
		for (Block b : getMap().values()) {
			b.destroy();
		}
	}
	
	public short getHeightAt(int x, int y) {
		final Point2i c = Block.calcCenter(x, y);
		// terrain coords to block center coords
		final Block b = getMap().get(c);
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
