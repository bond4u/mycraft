package org.mycraft.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//import java.util.Random;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.noise.IFunc2D;

public class Block {
	
	private static final int DIM = 15; // block dimensions
	// = center + x rows & x columns & x layers
	// constants
	private static final int BITS_PER_BYTE = Byte.SIZE; // 8
	private static final int BYTES_PER_FLOAT = Float.SIZE / BITS_PER_BYTE; // 32/8=4
	private static final int COMPONENTS_PER_POINT = 3; // 3d - 3 components - 3 axis
	private static final int BYTES_PER_VERTEX = COMPONENTS_PER_POINT * BYTES_PER_FLOAT; // 3 * 4 = 12
	private static final int POINTS_PER_FACE = 4;
	private static final int VERTEXBYTES_PER_FACE = POINTS_PER_FACE * BYTES_PER_VERTEX; // 4 * 12 = 48
	private static final int BYTES_PER_COLORCOMPONENT = 1;
	private static final int COMPONENTS_PER_COLOR = 4; // r,g,b,a
	private static final int BYTES_PER_COLOR = COMPONENTS_PER_COLOR * BYTES_PER_COLORCOMPONENT; // 3 * 1 = 3
	private static final int COLORBYTES_PER_FACE = POINTS_PER_FACE * BYTES_PER_COLOR; // 4 * 3 = 12
	
	private final Terrain t;
	private final int x;
	private final int y;
//	private final Random rnd;
	private final IFunc2D func;
	private final float data[][];
	private final int bufId;
	private float lowest;
	private float highest;
	
	private int facesCount;
	private int vertexBytes;
	private int colorBytes;
	
	public Block(Terrain t, int x, int y/*, Random rnd*/, IFunc2D f) {
		this.t = t;
		this.x = x;
		this.y = y;
//		log("Block.ctor(" + x + "," + y + ")");
//		this.rnd = rnd;
		this.func = f;
		this.data = gen();
		this.bufId = createVBO();
//		initVBO();
	}
	
	public static short getDim() {
		return DIM;
	}
	
	private float[][] gen() {
		resetLowHigh();
		final int d = (DIM - 1) / 2;
//		log("gen range " + DIM + " vs delta " + d);
		final float[][] a = new float[DIM][DIM];
		for (int x2 = 0; x2 < DIM; x2++) {
			for (int y2 = 0; y2 < DIM; y2++) {
//				a[x2][y2] = (short) (rnd.nextInt(DIM) - d);
				float x3 = x + (x2 - d);
				float y3 = y + (y2 - d);
//				log("[" + x2 + "," + y2 + "] = [" + x3 + "," + y3 + "]");
				float v = func.get(x3, y3);
				checkLowHigh(v);
				a[x2][y2] = v;
			}
		}
		assert lowest == highest : "flat block";
		return a;
	}
	
	protected void resetLowHigh() {
		lowest = Byte.MAX_VALUE;
		highest = Byte.MIN_VALUE;
	}
	
	protected void checkLowHigh(float h) {
		if (h < lowest) {
			lowest = h;
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
	
	protected int createVBO() {
		final int id = ARBBufferObject.glGenBuffersARB();
		logGlErrorIfAny();
		return id;
	}
	
	public void initVBO() {
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, this.bufId);
		logGlErrorIfAny();
//		log("gl buffer id " + this.bufId);
		facesCount = countQuads(); //BYTES_PER_VERTICES + BYTES_PER_COLORS;
//		log("faces count " + facesCount);
		vertexBytes = facesCount * VERTEXBYTES_PER_FACE;
		colorBytes = facesCount * COLORBYTES_PER_FACE;
		final int size = vertexBytes + colorBytes;
//		log("vertices=" + BYTES_PER_VERTICES + " bytes, colors=" + BYTES_PER_COLORS + " bytes = "
//				+ "total=" + size + " bytes");
		// create VBO
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,
				size, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
		logGlErrorIfAny();
		// map it for write
		ByteBuffer buf = ARBVertexBufferObject.glMapBufferARB(
				ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,
				ARBVertexBufferObject.GL_WRITE_ONLY_ARB, size, null);
		buf = buf.order(ByteOrder.nativeOrder());
//		log("buffer cap:" + buf.capacity() + " lim:" + buf.limit() +
//				" pos:" + buf.position() + " rem:" + buf.remaining() + " dir:" + buf.isDirect() +
//				" ro:" + buf.isReadOnly() + " ord:" + buf.order());
		logGlErrorIfAny();
		// fill buffer with data
		fillVBO(buf);
		buf.flip();
		// unmap
		ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
		logGlErrorIfAny();
		// unbind - only one active array buffer at a time 
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		logGlErrorIfAny();
	}
	
	private int countQuads() {
		int cnt = 0;
		int cnt2 = 0;
		int cnt3 = 0;
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				cnt++;
				final float height = data[x][y];
				// right edge
				final float height2 = (x < DIM - 1) ? data[x+1][y] : queryHeightAt(x + 1, y);
				if (height != height2) {
					cnt2++;
				}
				// top edge
				final float height3 = (y < DIM - 1) ? data[x][y+1] : queryHeightAt(x, y + 1);
				if (height != height3) {
					cnt3++;
				}
			}
		}
//		log("cnts=" + cnt + " / " + cnt2 + " / " + cnt3 + " / " + (cnt+cnt2+cnt3));
		return cnt + cnt2 + cnt3;
	}
	
	private void fillVBO(ByteBuffer b) {
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				final float height = data[x][y];
				// quad corners
				b.putFloat((x + 1));
				b.putFloat(height);
				b.putFloat(y);
				
				b.putFloat(x);
				b.putFloat(height);
				b.putFloat(y);
				
				b.putFloat(x);
				b.putFloat(height);
				b.putFloat((y + 1));
				
				b.putFloat((x + 1));
				b.putFloat(height);
				b.putFloat((y + 1));
				// right edge
				final float height2 = (x < DIM - 1) ? data[x+1][y] : queryHeightAt(x + 1, y);
				if (height != height2) {
					b.putFloat((x + 1));
					b.putFloat(height2);
					b.putFloat(y);
					
					b.putFloat((x + 1));
					b.putFloat(height);
					b.putFloat(y);
					
					b.putFloat((x + 1));
					b.putFloat(height);
					b.putFloat((y + 1));
					
					b.putFloat((x + 1));
					b.putFloat(height2);
					b.putFloat((y + 1));
				}
				// top edge
				final float height3 = (y < DIM - 1) ? data[x][y+1] : queryHeightAt(x, y + 1);
				if (height != height3) {
					b.putFloat((x + 1));
					b.putFloat(height);
					b.putFloat((y + 1));
					
					b.putFloat(x);
					b.putFloat(height);
					b.putFloat((y + 1));
					
					b.putFloat(x);
					b.putFloat(height3);
					b.putFloat((y + 1));
					
					b.putFloat((x + 1));
					b.putFloat(height3);
					b.putFloat((y + 1));
				}
			}
		}
		final byte val = 24; // TEMP
		if (val <= 0) {
			log("terrain has no height: " + val);
		}
		assert val <= 0 : "terrain has no height";
		//final int step = 256 / DIM;
		final float step = 128 / (float) val; // 255/32=7.96875
		//final int base = (1 + (len/*DIM*/ - 1) / 2) * step;
		final float base = step * 8; // 16*7.96875=127.5
//		log("step " + step + " & base " + base);
		final byte z = Byte.MIN_VALUE;
		final byte a = Byte.MAX_VALUE;
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				final float height = data[x][y];
				if (height < -val+1) {
					log("block is lower than -16: " + height);
				}
				if (height > val-1) {
					log("block is higher than 16: " + height);
				}
				assert height > t.highest() : "block has higher quad than terrain";
				// rgb for each quad corner
				byte c = (byte) (base + height * step);
//				c &= 0xFF;
//				log("h=" + height + " base=" + base + " step=" + step + " c=" + c);
				b.put(z);
				b.put(c);
				b.put(z);
				b.put(a);
				
				b.put(z);
				b.put(c);
				b.put(z);
				b.put(a);
				
				b.put(z);
				b.put(c);
				b.put(z);
				b.put(a);
				
				b.put(z);
				b.put(c);
				b.put(z);
				b.put(a);
				// right edge
				final float height2 = (x < DIM - 1) ? data[x+1][y] : queryHeightAt(x + 1, y);
				if (height != height2) {
//					short s2 = (short) (base + height2 * step);
					byte c2 = (byte) (base + height2 * step);
//					c2 &= 0xFF;
//					log("s2=" + s2 + " c2=" + c2);
					b.put(z);
					b.put(c2);
					b.put(z);
					b.put(a);
					
					b.put(z);
					b.put(c);
					b.put(z);
					b.put(a);
					
					b.put(z);
					b.put(c);
					b.put(z);
					b.put(a);
					
					b.put(z);
					b.put(c2);
					b.put(z);
					b.put(a);
				}
				// top edge
				final float height3 = (y < DIM - 1) ? data[x][y+1] : queryHeightAt(x, y + 1);
				if (height != height3) {
//					short s3 = (short) (base + height3 * step);
					byte c3 = (byte) (base + height3 * step);
//					c3 &= 0xFF;
//					log("s3=" + s3 + " c3=" + c3);
					b.put(z);
					b.put(c);
					b.put(z);
					b.put(a);
					
					b.put(z);
					b.put(c);
					b.put(z);
					b.put(a);
					
					b.put(z);
					b.put(c3);
					b.put(z);
					b.put(a);
					
					b.put(z);
					b.put(c3);
					b.put(z);
					b.put(a);
				}
			}
		}
	}
	
	public void draw() {
//		long start = System.currentTimeMillis();
	    GL11.glPushMatrix();
	    logGlErrorIfAny();
//	    long dura0 = System.currentTimeMillis() - start;
//	    if (dura0 > 1000 / 60) {
//	    	log("dura0 " + dura0 + " ms");
//	    }
	    
//	    GL11.glScalef(0.5f, 0.5f, 0.5f);
//	    logGlErrorIfAny();
//	    long start1 = System.currentTimeMillis();
	    GL11.glTranslatef(this.x - DIM / 2f, 0f, this.y - DIM / 2f);
	    logGlErrorIfAny();
//	    long dura1 = System.currentTimeMillis() - start1;
//	    if (dura1 > 1000 / 60) {
//	    	log("dura1 " + dura1 + " ms");
//	    }
	    
//	    long start2 = System.currentTimeMillis();
	    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	    logGlErrorIfAny();
	    GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
	    logGlErrorIfAny();
//	    long dura2 = System.currentTimeMillis() - start2;
//	    if (dura2 > 1000 / 60) {
//	    	log("dura2 " + dura2 + " ms");
//	    }
	    
//	    long start3 = System.currentTimeMillis();
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, bufId);
	    logGlErrorIfAny();
//	    long dura3 = System.currentTimeMillis() - start3;
//	    if (dura3 > 1000 / 60) {
//	    	log("dura3 " + dura3 + " ms");
//	    }
	    
	    // FIXME i'm not sure what was the problem
	    // 1) ati
	    // 2) laptop
	    // 3) rgb color (instead of rgba)
	    // 4) vertices as shorts (floats are best i guess)
	    // .. but it's working now
	    
//	    long start4 = System.currentTimeMillis();
	    GL11.glVertexPointer(COMPONENTS_PER_POINT, GL11.GL_FLOAT, 0, 0);
	    logGlErrorIfAny();
	    GL11.glColorPointer(BYTES_PER_COLOR, GL11.GL_BYTE, 0, vertexBytes);
	    logGlErrorIfAny();
//	    long dura4 = System.currentTimeMillis() - start4;
//	    if (dura4 > 1000 / 60) {
//	    	log("dura4 " + dura4 + " ms");
//	    }
	    
//	    long start5 = System.currentTimeMillis();
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, facesCount * POINTS_PER_FACE);
	    logGlErrorIfAny();
//	    long dura5 = System.currentTimeMillis() - start5;
//	    if (dura5 > 1000 / 60) {
//	    	log("dura5 " + bufId + " " + facesCount + " faces in " + dura5 + " ms");
//	    }
	    
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
	    logGlErrorIfAny();
	    
	    GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
	    logGlErrorIfAny();
	    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	    logGlErrorIfAny();
	    
	    GL11.glPopMatrix();
	    logGlErrorIfAny();
//	    long dura = System.currentTimeMillis() - start;
//	    if (dura > 1000 / 60) {
//	    	log("block.draw " + x + "x" + y + " " + dura + " ms");
//	    }
	}
	
	public void destroy() {
		ARBVertexBufferObject.glDeleteBuffersARB(bufId);
	    logGlErrorIfAny();
	}
	
	private float queryHeightAt(int x, int y) {
		final int d = (DIM - 1) / 2;
		final int landX = this.x - d + x; // 9 -> 5
		final int landY = this.y - d + y; // 0 -> -4
		float h = t.getHeightAt(landX, landY);
		return h;
	}
	
	public static Point2i calcCenter(int x, int y) {
		final short d = (DIM - 1) / 2; // -8+7=-1/15=-1
		final int x1 = x + d + (x < -d ? 1 : 0);
		final int y1 = y + d + (y < -d ? 1 : 0);
		final int x2 = x1 / DIM; // -7+7=0/15=0
		final int y2 = y1 / DIM; // 0+7=7/15=0
		final int x3 = x2 - (x < -d ? 1 : 0); // neg bonus
		final int y3 = y2 - (y < -d ? 1 : 0);
//		System.out.println("x=" + x + " y=" + y + " xc=" + xc + " yc=" + yc);
		return new Point2i(x3 * DIM, y3 * DIM); // 8+7=15/15=1
	}
	
	public float getHeightAt(int x, int y) {
		final int d = (DIM - 1) / 2;
		final int dx = x + d - this.x;
		final int dy = y + d - this.y;
		assert Math.abs(this.x - x) <= d : "x must not differ more than 4";
		assert Math.abs(this.y - y) <= d : "y must not differ more than 4";
		assert dx >= 0 && dx < DIM : "x idx out of array";
		assert dy >= 0 && dy < DIM : "y idx out of array";
		final float h = data[dx][dy];
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
