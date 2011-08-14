package org.mycraft.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.noise.Ground;
import org.noise.IFunc2D;

public class Terrain {
	
	private final IFunc2D func;
	private final Comparator<Point3f> comp;
	private final Map<Point3f, Block> addBlocks; // newly created blocks
 	private final Map<Point3f, Block> renderBlocks; // currently being rendered 
	private final Map<Point3f, Block> cacheBlocks; // removed from rendering, may be re-used
	private final List<Block> removeBlocks;
	private final Thread thread; // terrain calc thread
	
	private float lowest;
	private float highest;
	
	private boolean calcing;
	
	public Terrain(Random r, Viewport v, Camera c) {
		func = createFunc(r);
		comp = new Point3f(0f, 0f, 0f);
		addBlocks = createAddBlocksMap(comp);
		renderBlocks = createRenderBlocksMap(comp);
		cacheBlocks = createCacheBlocksMap(comp);
		removeBlocks = createRemoveBlocksList();
		thread = createCalcer(v, c);
	}
	
	protected IFunc2D createFunc(Random r) {
		return new Ground(r);
	}
	
	protected Map<Point3f, Block> createAddBlocksMap(Comparator<Point3f> c) {
		Map<Point3f, Block> m = new TreeMap<Point3f, Block>();
		return m;
	}
	
	protected Map<Point3f, Block> createRenderBlocksMap(Comparator<Point3f> c) {
		Map<Point3f, Block> m = new TreeMap<Point3f, Block>(c);
//		Map<Point2i, Block> sm = Collections.synchronizedMap(m);
		return m;
	}
	
	protected Map<Point3f, Block> createCacheBlocksMap(Comparator<Point3f> c) {
		Map<Point3f, Block> m = new TreeMap<Point3f, Block>(c);
		return m;
	}
	
	protected List<Block> createRemoveBlocksList() {
		List<Block> l = new ArrayList<Block>();
		return l;
	}
	
	protected IFunc2D getFunc() {
		return func;
	}
	
	public void create() {
		resetLowHigh();
		// just screate one block at (0,0)
		final float startX = 0f;
		final float startY = 0f;
		final float startZ = 0f;
		Point3f p = new Point3f(startX, startY, startZ);
		Block b = createBlock(startX, startY, startZ);
		b.generate();
		b.initVBO();
		renderBlocks.put(p, b);
//		addBlocks.add(b);
		checkLowHigh(b.lowest(), b.highest());
		assert lowest == highest : "flat terrain";
		startCalcer();
	}
	
	protected void resetLowHigh() {
		lowest = Short.MAX_VALUE;
		highest = Short.MIN_VALUE;
	}
	
	protected Block createBlock(float x, float y, float z) {
		return new Block(this, x, y, z/*, getFunc()*/);
	}
	
	protected void checkLowHigh(float l, float h) {
		if (l < lowest) {
			lowest = l;
		}
		if (h > highest) {
			highest = h;
		}
	}
	
	public float lowest() {
		return lowest;
	}
	
	public float highest() {
		return highest;
	}
	
	protected Thread createCalcer(final Viewport v, final Camera c) {
		log("creating terrain calcer");
		Thread t = new Thread() {
			public void run() {
				calc(v, c);
			}
		};
		return t;
	}
	
	protected void startCalcer() {
		log("starting terrain calcer");
		calcing = true;
		thread.start();
	}
	
	protected void calc(Viewport v, Camera c) {
		log("terrain calc starting..");
		int rval = 0;
		// terrain calculation thread
		while (calcing) {
			rval = genBlocks(c, v);
			dropBlocks(c, v);
			// wake up at least once a second (assuming 60fps)
			try {
				long sleep = 1000 / (rval == 0 ? 60 : 30);
				Thread.sleep(sleep); // 1=smallsleep
				rval = 0;
			} catch (InterruptedException e) {
				log("terrain.calcer sleep interrupted: " + e);
			}
		}
		log("terrain calc finished.");
	}
	
	private Point3f lastPoint = null;
	float[] delta = new float[] { 0f, 0f, 0f, };
	float radius = Block.getDim();
	int stage = 0;
	
	protected int genBlocks(Camera c, Viewport v) {
		// track camera ?
		float[] fCamPos = c.getPosition(); // x=l/r, y=u/d, z=f/b
		fCamPos[1] = 0f; // Y is up
		Point3f cpt = Block.calcBlockPoint(fCamPos[0], fCamPos[1], fCamPos[2]);
		// get cam block
		Block b;
		synchronized (renderBlocks) {
			b = renderBlocks.get(cpt);
		}
		// has it allready been added?
		if (b == null) {
			synchronized (addBlocks) {
				b = addBlocks.get(cpt);
			}
		}
		// try cache
		if (b == null) {
			synchronized (cacheBlocks) {
				b = cacheBlocks.get(cpt);
				if (b != null) { // available and ready for re-use
					cacheBlocks.remove(cpt);
					synchronized (renderBlocks) {
						renderBlocks.put(cpt, b);
					}
				}
			}
		}
		if (b == null) {
//			log("gen cam block " + cpf[0] + "," + cpf[1] + "," + cpf[2] +
//					" > " + cpi[0] + "," + cpi[1] +
//					" > " + bp.getX() + "," + bp.getY());
			addBlock(cpt);
//			return true;
		} else {
			// looking down: pos x, looking up: neg x, rot around x axis
			// rotating left: neg y, looking right: pos y, rot around y axis
			// z never changes, tilting left-right
			int dim = Block.getDim();
			// 0-angle means straight on
			float far = v.getFar() + dim * 2f;
			far = (float) (dim * Math.floor(far / dim));
			Point3f pt;
			if (!cpt.equals(lastPoint)) { // moved, start over
//				log("moved, starting over");
				delta[0] = 0f;
				delta[1] = 0f;
				delta[2] = 0f;
				radius = dim;
				stage = 0;
			}
			while (null != b) {
				pt = new Point3f(cpt.getX() + delta[0], cpt.getY() + delta[1], cpt.getZ() + delta[2]);
				// in view range
				synchronized (renderBlocks) {
					b = renderBlocks.get(pt);
				}
				// has it allready been added?
				if (b == null) {
					synchronized (addBlocks) {
						b = addBlocks.get(pt);
					}
				}
				// try cache
				if (b == null) {
					synchronized (cacheBlocks) {
						b = cacheBlocks.get(pt);
						if (b != null) {
							cacheBlocks.remove(pt);
							synchronized (renderBlocks) {
								renderBlocks.put(pt, b);
							}
						}
					}
				}
				if (b == null) {
//							log("looking at " + cof[0] + "," + cof[1] + "," + cof[2] +
//								" > " + dx + "x" + dy +
//								" > " + coi[0] + "," + coi[1] +
//								" > " + op.getX() + "," + op.getY());
//							log("requesting to add block at [" + pt.getX() + ", " + pt.getY() + ", " + pt.getZ() + "]");
					addBlock(pt);
//							return true;
				} else { // next
					if (0 == stage) { // init z = 1 plane
						delta[0] = -radius;
						delta[1] = Math.max(-radius, -dim*2);//-radius;
						delta[2] = radius;
//						log(stage + " from " + delta[0] + ", " + delta[1] + ", " + delta[2] + "; r=" + radius);
						stage++;
					} else if (1 == stage || 3 == stage) {
						delta[0] += dim;
//						log(stage + " to " + delta[0] + ", " + delta[1] + ", " + delta[2]);
						if (delta[0] > radius) {
							delta[0] = -radius;
							delta[1] += dim;
//							log(stage + " to2 " + delta[0] + ", " + delta[1] + ", " + delta[2] + "; r=" + radius);
							if (delta[1] > Math.min(radius, dim*2)) {//radius) {
								stage++;
							}
						}
					} else if (2 == stage) { // init z = -1 plane
						delta[0] = -radius;
						delta[1] = Math.max(-radius, -dim*2);//-radius;
						delta[2] = -radius;
//						log(stage + " from " + delta[0] + ", " + delta[1] + ", " + delta[2] + ", r=" + radius);
						stage++;
					} else if (4 == stage) { // init y = 1 plane
						delta[0] = -radius;
						delta[1] = Math.min(dim*2, radius);
						delta[2] = -radius;
//						log(stage + " from " + delta[0] + ", " + delta[1] + ", " + delta[2] + "; r=" + radius);
						stage++;
					} else if (5 == stage || 7 == stage) {
						delta[0] += dim;
//						log(stage + " to " + delta[0] + ", " + delta[1] + ", " + delta[2]);
						if (delta[0] > radius) {
							delta[0] = -radius;
							delta[2] += dim;
//							log(stage + " to2 " + delta[0] + ", " + delta[1] + ", " + delta[2]);
							if (delta[2] > radius) {
								stage++;
							}
						}
					} else if (6 == stage) { // init y = -1 plane
						delta[0] = -radius;
						delta[1] = Math.max(-dim*2, -radius);
						delta[2] = -radius;
//						log(stage + " from " + delta[0] + ", " + delta[1] + ", " + delta[2] + "; r=" + radius);
						stage++;
					} else if (8 == stage) { // init x = 1 plane
						delta[0] = radius;
						delta[1] = Math.max(-radius, -dim*2);
						delta[2] = -radius;
//						log(stage + " from " + delta[0] + ", " + delta[1] + ", " + delta[2] + "; r=" + radius);
						stage++;
					} else if (9 == stage || 11 == stage) {
						delta[1] += dim;
//						log(stage + " to " + delta[0] + ", " + delta[1] + ", " + delta[2] + "; r=" + radius);
						if (delta[1] > Math.min(radius, dim*2)) {//radius) {
							delta[1] = Math.max(-dim*2, -radius);
							delta[2] += dim;
//							log(stage + " to2 " + delta[0] + ", " + delta[1] + ", " + delta[2] + "; r=" + radius);
							if (delta[2] > radius) {
								stage++;
							}
						}
					} else if (10 == stage) { // init x = -1 plane
						delta[0] = -radius;
						delta[1] = Math.max(-dim*2, -radius);
						delta[2] = -radius;
//						log(stage + " from " + delta[0] + ", " + delta[1] + ", " + delta[2] + "; r=" + radius);
						stage++;
					}
					if (stage >= 12) {
						stage = 0; // start over
						radius += dim;
						if (radius > far) {
//							log("all generated");
							return 2; // all done
						}
					}
//							log("next is [" + d[0] + ", " + d[1] + ", " + d[2] + "] " + s[0] + "," + s[1] + "," + s[2]);
				}
			}
		}
		lastPoint = cpt;
		return (null == b) ? 0 : 1; // block was not found (was added)
	}
	
	protected void dropBlocks(Camera c, Viewport v) {
		float[] camPos = c.getPosition();
		short blkDim = Block.getDim();
		float far = v.getFar() + blkDim * 4;
		synchronized (renderBlocks) {
			for (Point3f p : renderBlocks.keySet()) {
				if (p.getX() < camPos[0] - far || p.getX() > camPos[0] + far ||
						/*p.getY() < camPos[1] - dim || p.getY() > camPos[1] + dim ||*/
						p.getZ() < camPos[2] - far || p.getZ() > camPos[2] + far) {
					Block b = renderBlocks.remove(p);
//					b.destroy();
					synchronized (cacheBlocks) {
						cacheBlocks.put(p, b);
					}
//					log("removed block @ " + p.getX() + "x" + p.getY() + "x" + p.getZ());
//					log("map size " + m.size() + " blocks.");
					break; // one at a time
				}
			}
		}
	}
	
	protected Block addBlock(Point3f p) {
		Block b = createBlock(p.getX(), p.getY(), p.getZ());
		b.generate();
		// vbo stuff is done in (main GL) drawing thread
		synchronized (addBlocks) {
			addBlocks.put(p, b);
		}
//		log("added block @ " + p.getX() + "x" + p.getY() + "x" + p.getZ());
		checkLowHigh(b.lowest(), b.highest());
//		log("terrain lowest " + lowest + " & highest " + highest);
//		synchronized (blocks) {
//			log("map size " + blocks.size() + " blocks.");
//		}
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
		long time = Sys.getTime();
		int vc = 0;
		int bc = 0;
		int fc = 0;
		Block a = null;
		synchronized (addBlocks) {
			Iterator<Block> it = addBlocks.values().iterator();
			if (it.hasNext()) {
				a = it.next();
				it.remove();
			}
		}
		if (a != null && a.getFaceCount() > 0 && a.getVboId() == 0) {
			// guaranteed to be called within GL main thread
			a.initVBO();
			vc++;
		}
		synchronized (renderBlocks) {
			if (a != null) {
				renderBlocks.put(new Point3f(a.getX(), a.getY(), a.getZ()), a);
			}
			for (Block b : renderBlocks.values()) {
				b.draw();
				bc++;
				fc += b.getFaceCount();
			}
		}
		// it's not nice, but we have to free vbo somewhere
		Block c = null;
		synchronized (removeBlocks) {
			if (removeBlocks.size() > 0) {
				c = removeBlocks.remove(0);
			}
		}
		if (c != null) {
			c.freeVbo();
			c.destroy();
			vc++;
		}
		long duration = Sys.getTime() - time;
		if (duration > (1000 / 60)) {
			log("terrain.draw " + vc + " vbos; " + bc + " blocks; " + fc + " faces in " + duration  + " ms");
		}
	}
	
	public void destroy() {
		stopCalcer();
		for (Block b : addBlocks.values()) {
			b.freeVbo();
			b.destroy();
		}
		for (Block b : renderBlocks.values()) {
			b.freeVbo();
			b.destroy();
		}
		for (Block b : cacheBlocks.values()) {
			b.freeVbo();
			b.destroy();
		}
		for (Block b : removeBlocks) {
			b.freeVbo();
			b.destroy();
		}
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
