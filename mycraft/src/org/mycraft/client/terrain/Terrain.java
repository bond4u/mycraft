package org.mycraft.client.terrain;

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
import org.mycraft.client.Camera;
import org.mycraft.client.Chunk;
import org.mycraft.client.Point3f;
import org.mycraft.client.Viewport;

/**
 * Chunkmanager - holder of chunks.
 */
public class Terrain {
	
	private final Generator generator;
	private final Comparator<Point3f> comp;
	private final Map<Point3f, Chunk> addBlocks; // newly created blocks
 	private final Map<Point3f, Chunk> renderBlocks; // currently being rendered 
	private final Map<Point3f, Chunk> cacheBlocks; // removed from rendering, may be re-used
	private final List<Chunk> removeBlocks;
	private final Thread thread; // terrain calc thread

	private boolean calcing;
	
	public Terrain(Random r, Viewport v, Camera c) {
		generator = createGenerator(r);
		comp = new Point3f(0f, 0f, 0f);
		addBlocks = createAddBlocksMap(comp);
		renderBlocks = createRenderBlocksMap(comp);
		cacheBlocks = createCacheBlocksMap(comp);
		removeBlocks = createRemoveBlocksList();
		thread = createCalcer(v, c);
	}
	
	protected Generator createGenerator(Random r) {
		return new Generator(r);
	}
	
	protected Map<Point3f, Chunk> createAddBlocksMap(Comparator<Point3f> c) {
		Map<Point3f, Chunk> m = new TreeMap<Point3f, Chunk>();
		return m;
	}
	
	protected Map<Point3f, Chunk> createRenderBlocksMap(Comparator<Point3f> c) {
		Map<Point3f, Chunk> m = new TreeMap<Point3f, Chunk>(c);
//		Map<Point2i, Block> sm = Collections.synchronizedMap(m);
		return m;
	}
	
	protected Map<Point3f, Chunk> createCacheBlocksMap(Comparator<Point3f> c) {
		Map<Point3f, Chunk> m = new TreeMap<Point3f, Chunk>(c);
		return m;
	}
	
	protected List<Chunk> createRemoveBlocksList() {
		List<Chunk> l = new ArrayList<Chunk>();
		return l;
	}
	
	public Generator getGenerator() {
		return generator;
	}
	
	public void create() {
		// just screate one block at (0,0)
		final float startX = 0f;
		final float startY = 0f;
		final float startZ = 0f;
		Point3f p = new Point3f(startX, startY, startZ);
		Chunk b = createBlock(startX, startY, startZ);
		b.generate();
		b.initVBO();
		renderBlocks.put(p, b);
		startCalcer();
	}
	
	protected Chunk createBlock(float x, float y, float z) {
		return new Chunk(getGenerator(), x, y, z);
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
				Thread.sleep(sleep); // 0=long sleep, 1=short sleep
				rval = 0;
			} catch (InterruptedException e) {
				log("terrain.calcer sleep interrupted: " + e);
			}
		}
		log("terrain calc finished.");
	}
	
	private Point3f lastPoint = null;
	private float[] delta = new float[] { 0f, 0f, 0f, };
	private float radius = Chunk.getDim();
	private int stage = 0;
	
	protected int genBlocks(Camera c, Viewport v) {
		// track camera ?
		float[] fCamPos = c.getPosition(); // x=l/r, y=u/d, z=f/b
		fCamPos[1] = 0f; // Y is up
		Point3f cpt = Chunk.calcBlockPoint(fCamPos[0], fCamPos[1], fCamPos[2]);
		// get cam block
		Chunk b;
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
					synchronized (addBlocks) {
						addBlocks.put(cpt, b);
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
			int dim = Chunk.getDim();
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
							synchronized (addBlocks) {
								addBlocks.put(pt, b);
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
		short blkDim = Chunk.getDim();
		float far = v.getFar() + blkDim * 4;
		float minX = camPos[0] - far;
		float maxX = camPos[0] + far;
		float minZ = camPos[2] - far;
		float maxZ = camPos[2] + far;
		int bc = 100;
		synchronized (renderBlocks) {
			for (Point3f p : renderBlocks.keySet()) {
				if (p.getX() < minX || p.getX() > maxX ||
						/*p.getY() < camPos[1] - dim || p.getY() > camPos[1] + dim ||*/
						p.getZ() < minZ || p.getZ() > maxZ) {
					Chunk b = renderBlocks.remove(p);
					synchronized (cacheBlocks) {
						cacheBlocks.put(p, b);
					}
//					log("removed block @ " + p.getX() + "x" + p.getY() + "x" + p.getZ());
//					log("map size " + m.size() + " blocks.");
					break; // one at a time
				}
			}
			bc = renderBlocks.size();
		}
		synchronized (cacheBlocks) {
			if (cacheBlocks.size() > bc) {
				Iterator<Point3f> it = cacheBlocks.keySet().iterator();
				Point3f p = it.next();
				Chunk b = cacheBlocks.remove(p);
				synchronized (removeBlocks) {
					removeBlocks.add(b);
				}
			}
		}
	}
	
	protected Chunk addBlock(Point3f p) {
		Chunk b = createBlock(p.getX(), p.getY(), p.getZ());
		b.generate();
		// vbo stuff is done in (main GL) drawing thread
		synchronized (addBlocks) {
			addBlocks.put(p, b);
		}
//		log("added block @ " + p.getX() + "x" + p.getY() + "x" + p.getZ());
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
		int vf = 0;
		Chunk a = null;
		synchronized (addBlocks) {
			Iterator<Chunk> it = addBlocks.values().iterator();
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
			for (Chunk b : renderBlocks.values()) {
				b.draw();
				bc++;
				fc += b.getFaceCount();
			}
		}
		// it's not nice, but we have to free vbo somewhere
		Chunk c = null;
		synchronized (removeBlocks) {
			if (removeBlocks.size() > 0) {
				c = removeBlocks.remove(0);
			}
		}
		if (c != null) {
			c.freeVbo();
			c.destroy();
			vf++;
		}
		long duration = Sys.getTime() - time;
		if (duration > (1000 / 60)) {
			log("terrain.draw: created " + vc + " vbos; drawn " + bc + " vbos & " + fc +
					" faces; freed " + vf + " vbos in " + duration  + " ms");
		}
	}
	
	public void destroy() {
		stopCalcer();
		for (Chunk b : addBlocks.values()) {
			b.freeVbo();
			b.destroy();
		}
		for (Chunk b : renderBlocks.values()) {
			b.freeVbo();
			b.destroy();
		}
		for (Chunk b : cacheBlocks.values()) {
			b.freeVbo();
			b.destroy();
		}
		for (Chunk b : removeBlocks) {
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
