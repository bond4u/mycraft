package org.mycraft.client;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Terrain {
	
	private static final int DIM = 14;
	private final Random rnd;
	private final Comparator<Point2i> comp;
//	private final Map<Point2i, Integer> points;
//	private final Block block;
	private final Map<Point2i, Block> blocks;
	
	public Terrain(Random r) {
		rnd = r;
		comp = new Point2i(0, 0);
//		points = new TreeMap<Point2i, Integer>(comp);
//		block = new Block(0, 0, rnd);
		blocks = new TreeMap<Point2i, Block>(comp);
		for (int x = -DIM / 2; x <= DIM / 2; x++) {
			for (int y = -DIM / 2; y <= DIM / 2; y++) {
				final int xx = x * Block.DIM;
				final int yy = y * Block.DIM;
				Point2i p = new Point2i(xx, yy);
				blocks.put(p, new Block(xx, yy, rnd));
			}
		}
	}
	
	public void draw() {
//	    GL11.glPushMatrix();
//	    logGlErrorIfAny();
//	    
//	    float r = 0f;
//	    float b = 0f;
//	    float a = 1f;
//	    
//	    for (int x = -64; x < 65; x++) {
//	    	for (int y = -128; y < 127; y++) {
//	    		int ll = get(x, y);
////	    		int lr = get(x + 1, y);
////	    		int ur = get(x + 1, y + 1);
////	    		int ul = get(x, y + 1);
//	    		GL11.glBegin(GL11.GL_QUADS);
////	    	    GL11.glColor4f(r, 1f, b, a);
//	    		GL11.glColor4f(r, 0.5f + ll / 5f, b, a);
////	    		GL11.glVertex3i(x + 1, lr, y); // 1
//	    	    GL11.glVertex3f(x + 0.5f, ll, y - 0.5f);
////	    	    GL11.glColor4f(r, 0.9f, b, a);
//	    	    GL11.glColor4f(r, 0.5f + ll / 5f, b, a);
////	    		GL11.glVertex3i(x, ll, y); // 0
//	    	    GL11.glVertex3f(x - 0.5f, ll, y - 0.5f);
////	    	    GL11.glColor4f(r, 0.8f, b, a);
//	    	    GL11.glColor4f(r, 0.5f + ll / 5f, b, a);
////	    		GL11.glVertex3i(x, ul, y + 1); // 3
//	    	    GL11.glVertex3f(x - 0.5f, ll, y + 0.5f);
////	    	    GL11.glColor4f(r, 0.7f, b, a);
//	    	    GL11.glColor4f(r, 0.5f + ll / 5f, b, a);
////	    		GL11.glVertex3i(x + 1, ur, y + 1); // 2
//	    	    GL11.glVertex3f(x + 0.5f, ll, y + 0.5f);
//	    		GL11.glEnd();
//	    	}
//	    }
//	    GL11.glPopMatrix();
//	    logGlErrorIfAny();
//		block.draw();
		for (Block b : blocks.values()) {
			b.draw();
		}
	}
	
//	public int get(int x, int y) {
//		Point2i p = new Point2i(x, y);
//		Integer h = points.get(p);
//		if (h == null) {
//			h = rnd.nextInt(5) - 2; //rnd.nextInt(256) - 128;
////			log("point @ " + x + "x" + y + " was missing; now it's " + h);
//			points.put(p, h);
//		} else {
////			log("point @ " + x + "x" + y + " is " + h);
//		}
//		return h;
//	}
	
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
