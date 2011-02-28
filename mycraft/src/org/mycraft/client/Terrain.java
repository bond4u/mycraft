package org.mycraft.client;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.SharedDrawable;
import org.lwjgl.util.glu.GLU;
import org.noise.Ground;
import org.noise.IFunc2D;

public class Terrain {
	
//	private static final int DIM = 16;

	private final IFunc2D func;
	private final Comparator<Point2i> comp;
	private final Map<Point2i, Block> blocks;
	private final Thread thread; // terrain calc thread
//	private final Map<Point2i, Block> add;
//	private final Map<Point2i, Block> rem;
	
	private short lowest;
	private short highest;
	
	
	private boolean calcing;
	
	public Terrain(Random r, Viewport v, Camera c) {
		func = createFunc(r); //new Ground(r);
		comp = new Point2i(0, 0);
		blocks = createBlocksMap(comp);
		SharedDrawable sd = null;
		try {
			sd = new SharedDrawable(Display.getDrawable());
		} catch (LWJGLException e) {
			log("SharedDrawable ctor ex: " + e);
		}
		thread = createCalcer(sd, v, c);
//		add = new TreeMap<Point2i, Block>(comp);
//		rem = new TreeMap<Point2i, Block>(comp);
	}
	
	protected IFunc2D createFunc(Random r) {
		return new Ground(r);
	}
	
	protected Map<Point2i, Block> createBlocksMap(Comparator<Point2i> c) {
		Map<Point2i, Block> m = new TreeMap<Point2i, Block>(c);
//		Map<Point2i, Block> sm = Collections.synchronizedMap(m);
		return m;
	}
	
	protected IFunc2D getFunc() {
		return func;
	}
	
//	protected Map<Point2i, Block> getMap() {
//		return blocks;
//	}
	
	public void create() {
		resetLowHigh();
//		short blkDim = Block.getDim();
//		final long startTime = System.currentTimeMillis();
//		for (int x = -DIM / 2; x <= DIM / 2; x++) {
//			for (int y = -DIM / 2; y <= DIM / 2; y++) {
//				final int xc = x * blkDim;
//				final int yc = y * blkDim;
		final int xc = 0;
		final int yc = 0;
//				log("p=" + xx + "x" + yy);
		Point2i p = new Point2i(xc, yc);
		Block b = createBlock(xc, yc);
		b.initVBO(); // may need to re-calc once neighbours appear?
		blocks.put(p, b);
		checkLowHigh(b.lowest(), b.highest());
//			}
//		}
//		final long delta = System.currentTimeMillis() - startTime;
//		log("terrain created in " + delta + " ms; low=" + lowest + " high=" + highest);
//		assert lowest == highest : "flat terrain";
		startCalcer();
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
	
//	public void init() {
//		final long startTime = System.currentTimeMillis();
//		for (Block b : getMap().values()) {
//			b.initVBO();
//		}
//		final long delta = System.currentTimeMillis() - startTime;
//		log("terrain filled in " + delta + " millis");
//	}
	
	protected Thread createCalcer(final Drawable d, final Viewport v, final Camera c) {
//		cam = c;
		log("creating terrain calcer");
		Thread t = new Thread() {
			public void run() {
//				try {
				calc(d, v, c);
//				} catch (Throwable t) {
//					log("terrain calc thread ex: " + t);
//				}
			}
		};
		return t;
	}
	
	protected void startCalcer() {
		log("starting terrain calcer");
		calcing = true;
		thread.start();
	}
	
	protected void calc(Drawable d, Viewport v, Camera c) {
		log("terrain calc starting..");
		try {
			log("Drawable.iscurrent=" + d.isCurrent());
			if (!d.isCurrent()) {
				d.makeCurrent();
			}
			GLContext.useContext(d);
		} catch (LWJGLException e) {
			log("drawable.makeCurrent ex: " + e);
		}
		boolean smallSleep = true;
		int gc = 0;
		// terrain calculation thread
		while (calcing) {
//			log("terrain calcer tick");
			// wake up at least once every frame (assuming 60fps)
			if (!smallSleep) {
				gc++;
				if (gc >= 30) {
//					Runtime r = Runtime.getRuntime();
//					long t = r.totalMemory();
//					long f = r.freeMemory();
//					log("gc: " + t + ", " + f + ", " + (t-f));
					System.gc();
					gc = 0;
				}
			}
			try {
				Thread.sleep(1000 / (smallSleep ? 60 : 30)); // smallSleep=1 frame
				smallSleep = false;
			} catch (InterruptedException e) {
				log("terrain.calcer sleep interrupted: " + e);
			}
			// too much recursion - stack overflow
//			float[] camPos = c.getPosition();
//			float[] camDir = c.getRotation();
//			smallSleep = createBlock(c, v, camPos, false);
//			if (!smallSleep) {
//				// create nearest block recursively
//				smallSleep = createBlock(c, v, camPos, true);
//			}
			smallSleep = genBlocks(c, v);
			dropBlocks(c, v);
		}
		log("terrain calc finished.");
	}
	
//	protected boolean createBlock(Camera c, Viewport v, float[] p, boolean r) {
//		Point2i pt = calcPoint(p[0], p[2]);
//		Block bl;
//		boolean b = false;
//		if (!r) {
//			bl = getMap().get(pt);
//			b = (bl == null);
//			if (b) {
//				bl = addBlock(pt);
//			}
//		}
//		if (r) {
//			short blkDim = Block.getDim();
//			b = false;
//			float a = 0f;
//			int s = 1;
//			float[] camDir = c.getRotation();
//			for (int i = 0; b == false && i < 8; i++) {
//				a += 45f * s * i;
//				float dz = (float) (-blkDim * Math.cos(camDir[1] + a)); // go forward/backward
//				float dx = (float) (-blkDim * Math.sin(camDir[1] + a)); // go sideways
//				b = createBlock(c, v,
//						new float[] { pt.getX() + dx, 0f, pt.getY() + dz, },
//						false);
//				s *= -1;
//			}
//			float dim = v.getFar() + blkDim;
//			float x0 = p[0] - dim;
//			float x1 = p[0] + dim;
//			float z0 = p[2] - dim;
//			float z1 = p[2] + dim;
//			float[] cp = c.getPosition();
//			// if within view distance, then recurse
//			if (x0 < cp[0] && cp[0] < x1 && z0 < cp[2] && cp[2] < z1) {
//				a = 0f;
//				s = 1;
//				for (int i = 0; b == false && i < 8; i++) {
//					a += 45f * s * i;
//					float dy = (float) (-blkDim * Math.cos(camDir[1] + a)); // go forward/backward
//					float dx = (float) (-blkDim * Math.sin(camDir[1] + a)); // go sideways
//					b = createBlock(c, v,
//							new float[] { pt.getX() + dy, 0f, pt.getY() + dx, },
//							true);
//					s *= -1;
//				}
//			}
//		}
//		return b;
//	}
	
//	protected Point2i calcPoint(float x, float y) {
//		Point2i p = Block.calcCenter((int)x, (int)y);
//		return p;
//	}
	
	protected boolean genBlocks(Camera c, Viewport v) {
		// track camera ?
		float[] cpf = c.getPosition(); // x=l/r, y=u/d, z=f/b
		// cam coords floats to ints - ignore height/Y for now
		int[] cpi = new int[] { (int)cpf[0], (int)cpf[2], };
		// get block center
		Point2i bp = Block.calcCenter(cpi[0], cpi[1]);
		// get cam block
		Block cb;
		synchronized (blocks) {
			cb = blocks.get(bp);
		}
		if (cb == null) {
//			log("gen cam block " + cpf[0] + "," + cpf[1] + "," + cpf[2] +
//					" > " + cpi[0] + "," + cpi[1] +
//					" > " + bp.getX() + "," + bp.getY());
			addBlock(bp);
			return true;
		} else {
			// where is camera looking at
			float[] cof = c.getRotation();
			// looking down: pos x, looking up: neg x, rot around x axis
			// rotating left: neg y, looking right: pos y, rot around y axis
			// z never changes, tilting left-right
			short blkDim = Block.getDim();
			short hDim = (short) (blkDim / 2);
			// 0-angle means straight on
			float far = v.getFar() + blkDim * 2;
			short depth = (short) Math.ceil(far);
//			log("depth=" + depth + " far=" + (depth / blkDim) + " ss=" + smallSleep);
			for (int i = 1; i <= (depth / hDim) ; i++) {
				float angle = 45f / i;
				for (int a = -4 * i; a <= 4 * i; a++) {
					int dx = (int) (i * -hDim * Math.sin(cof[0] + a * angle));
					int dy = (int) (i * -hDim * Math.cos(cof[1] + a * angle));
					int[] coi = new int[] { cpi[0] + dx, cpi[1] + dy, };
					Point2i op = Block.calcCenter(coi[0], coi[1]);
					Block ob;
					synchronized (blocks) {
						ob = blocks.get(op);
					}
					if (ob == null) {
//						log("looking at " + cof[0] + "," + cof[1] + "," + cof[2] +
//								" > " + dx + "x" + dy +
//								" > " + coi[0] + "," + coi[1] +
//								" > " + op.getX() + "," + op.getY());
						addBlock(op);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected void dropBlocks(Camera c, Viewport v) {
		float[] camPos = c.getPosition();
		short blkDim = Block.getDim();
		float dim = v.getFar() + blkDim * 4;
		synchronized (blocks) {
			for (Point2i p : blocks.keySet()) {
				if (p.getX() < camPos[0] - dim || p.getX() > camPos[0] + dim ||
						p.getY() < camPos[2] - dim || p.getY() > camPos[2] + dim) {
					Block b = blocks.remove(p);
					b.destroy();
//					log("removed block @ " + p.getX() + "x" + p.getY());
//					log("map size " + m.size() + " blocks.");
					break; // one at a time
				}
			}
		}
	}
	
	protected Block addBlock(Point2i p) {
		Block b = createBlock(p.getX(), p.getY());
		b.initVBO();
		synchronized (blocks) {
			blocks.put(p, b);
		}
//		log("added block @ " + p.getX() + "x" + p.getY());
		checkLowHigh(b.lowest(), b.highest());
//		log("map size " + getMap().size() + " blocks.");
		return b;
	}
	
	protected void stopCalcer() {
		log("stopping terrain calcer");
		calcing = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			log("terrain calcer thread.join() interrupted");
		}
	}
	
	public void draw() {
//		long start = System.currentTimeMillis();
//		int cnt = 0;
		synchronized (blocks) {
			for (Block b : blocks.values()) {
				b.draw();
//				cnt++;
			}
		}
//		long duration = System.currentTimeMillis() - start;
//		if (duration > 1000 / 60) {
//			log("terrain.draw " + cnt + " blocks in " + duration + " ms");
//		}
	}
	
	public void destroy() {
		stopCalcer();
		for (Block b : blocks.values()) {
			b.destroy();
		}
	}
	
	public short getHeightAt(int x, int y) {
		final Point2i c = Block.calcCenter(x, y);
		// terrain coords to block center coords
		final Block b;
		synchronized (blocks) {
			b = blocks.get(c);
		}
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
