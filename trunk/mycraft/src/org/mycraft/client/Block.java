package org.mycraft.client;

import java.nio.ByteBuffer;
//import java.util.Random;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.noise.IFunc2D;

public class Block {
	
	private static final int DIM = 15; // block dimensions
	// = center + x rows & x columns & x layers
	// vertex data
	private static final int BYTES_PER_SHORT = 2; // 2 bytes per element
	private static final int ELEM_PER_VERTEX = 3; // 3 elements per vertex
	private static final int BYTES_PER_VERTEX = ELEM_PER_VERTEX * BYTES_PER_SHORT; // 3 * 2 = 6 bytes per vertex
	private static final int VERTICES_PER_FACE = 4; // 4 vertices per face
	private static final int VERTEX_BYTES_PER_FACE = VERTICES_PER_FACE * BYTES_PER_VERTEX; // 4 * 6 = 24 bytes per face
//	private static final int FACES_COUNT = 1; // one face = top
//	private static final int FACES_COUNT = DIM * DIM; // 5 * 5 = 25 faces per block
//	private static final int VERTICES_COUNT = FACES_COUNT * VERTICES_PER_FACE;
//	private static final int BYTES_PER_VERTICES = FACES_COUNT * BYTES_PER_FACE; // 25 * 24 bytes for block vertices
	// color data per vertex
	private static final int BYTES_PER_COLOR = 3;
	private static final int COLOR_BYTES_PER_FACE = VERTICES_PER_FACE * BYTES_PER_COLOR;
//	private static final int BYTES_PER_COLORS = FACES_COUNT * VERTICES_PER_FACE * BYTES_PER_COLOR;
	
	private final Terrain t;
	private final int x;
	private final int y;
//	private final Random rnd;
	private final IFunc2D func;
	private final short data[][];
	private final int bufId;
	private short lowest;
	private short highest;
	
	private int facesCount;
	private int vertexBytes;
	private int colorBytes;
	
	public Block(Terrain t, int x, int y/*, Random rnd*/, IFunc2D f) {
		this.t = t;
		this.x = x;
		this.y = y;
//		this.rnd = rnd;
		this.func = f;
		this.data = gen();
		this.bufId = createVBO();
//		initVBO();
	}
	
	public static short getDim() {
		return DIM;
	}
	
	private short[][] gen() {
		lowest = Short.MAX_VALUE;
		highest = Short.MIN_VALUE;
		final short d = (DIM - 1) / 2;
		final short[][] a = new short[DIM][DIM];
		float[] fmm = new float[2];
		for (int x2 = 0; x2 < DIM; x2++) {
			for (int y2 = 0; y2 < DIM; y2++) {
//				a[x2][y2] = (short) (rnd.nextInt(DIM) - d);
				float x3 = x + (x2 - d);
				float y3 = y + (y2 - d);
//				log("[" + x2 + "," + y2 + "] = [" + x3 + "," + y3 + "]");
				float v = func.get(x3, y3);
				if (v < fmm[0]) {
					fmm[0] = v;
				} else if (v > fmm[1]) {
					fmm[1] = v;
				}
				short h = (short) v;
				if (h < lowest) {
					lowest = h;
				} else if (h > highest) {
					highest = h;
				}
				a[x2][y2] = h;
			}
		}
//		log("fmm=" + fmm[0] + "," + fmm[1] + "; smm=" + lowest + "," + highest);
		assert lowest == highest : "flat block";
		return a;
	}
	
	public short lowest() {
		return lowest;
	}
	
	public short highest() {
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
		facesCount = countQuads(); //BYTES_PER_VERTICES + BYTES_PER_COLORS;
		vertexBytes = facesCount * VERTEX_BYTES_PER_FACE;
		colorBytes = facesCount * COLOR_BYTES_PER_FACE;
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
		logGlErrorIfAny();
		// fill buffer with data
		fillVBO(buf);
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
				final int height = data[x][y];
				// right edge
				final int height2 = (x < DIM - 1) ? data[x+1][y] : queryHeightAt(x + 1, y);
				if (height != height2) {
					cnt2++;
				}
				// top edge
				final int height3 = (y < DIM - 1) ? data[x][y+1] : queryHeightAt(x, y + 1);
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
				final short height = (short) data[x][y];
				// quad corners
				b.putShort((short) (x + 1));
				b.putShort(height);
				b.putShort((short) y);
				b.putShort((short) x);
				b.putShort(height);
				b.putShort((short) y);
				b.putShort((short) x);
				b.putShort(height);
				b.putShort((short) (y + 1));
				b.putShort((short) (x + 1));
				b.putShort(height);
				b.putShort((short) (y + 1));
				// right edge
				final short height2 = (x < DIM - 1) ? data[x+1][y] : queryHeightAt(x + 1, y);
				if (height != height2) {
					b.putShort((short) (x + 1));
					b.putShort(height2);
					b.putShort((short) y);
					b.putShort((short) (x + 1));
					b.putShort(height);
					b.putShort((short) y);
					b.putShort((short) (x + 1));
					b.putShort(height);
					b.putShort((short) (y + 1));
					b.putShort((short) (x + 1));
					b.putShort(height2);
					b.putShort((short) (y + 1));
				}
				// top edge
				final short height3 = (y < DIM - 1) ? data[x][y+1] : queryHeightAt(x, y + 1);
				if (height != height3) {
					b.putShort((short) (x + 1));
					b.putShort(height);
					b.putShort((short) (y + 1));
					b.putShort((short) x);
					b.putShort(height);
					b.putShort((short) (y + 1));
					b.putShort((short) x);
					b.putShort(height3);
					b.putShort((short) (y + 1));
					b.putShort((short) (x + 1));
					b.putShort(height3);
					b.putShort((short) (y + 1));
				}
			}
		}
		final float len = (short) (t.highest() - t.lowest());
		assert len == 0f : "terrain has no height";
		//final int step = 256 / DIM;
		final float step = 255 / len;
		//final int base = (1 + (len/*DIM*/ - 1) / 2) * step;
		final float base = -t.lowest() * step;
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				final short height = (short) data[x][y];
				assert height > t.highest() : "block has higher quad than terrain";
				// rgb for each quad corner
				b.put((byte) 0);
				b.put((byte) (base + height * step));
				b.put((byte) 0);
				b.put((byte) 0);
				b.put((byte) (base + height * step));
				b.put((byte) 0);
				b.put((byte) 0);
				b.put((byte) (base + height * step));
				b.put((byte) 0);
				b.put((byte) 0);
				b.put((byte) (base + height * step));
				b.put((byte) 0);
				// right edge
				final short height2 = (x < DIM - 1) ? data[x+1][y] : queryHeightAt(x + 1, y);
				if (height != height2) {
					b.put((byte) 0);
					b.put((byte) (base + height2 * step));
					b.put((byte) 0);
					b.put((byte) 0);
					b.put((byte) (base + height * step));
					b.put((byte) 0);
					b.put((byte) 0);
					b.put((byte) (base + height * step));
					b.put((byte) 0);
					b.put((byte) 0);
					b.put((byte) (base + height2 * step));
					b.put((byte) 0);
				}
				// top edge
				final short height3 = (y < DIM - 1) ? data[x][y+1] : queryHeightAt(x, y + 1);
				if (height != height3) {
					b.put((byte) 0);
					b.put((byte) (base + height * step));
					b.put((byte) 0);
					b.put((byte) 0);
					b.put((byte) (base + height * step));
					b.put((byte) 0);
					b.put((byte) 0);
					b.put((byte) (base + height3 * step));
					b.put((byte) 0);
					b.put((byte) 0);
					b.put((byte) (base + height3 * step));
					b.put((byte) 0);
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
	    GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
	    logGlErrorIfAny();
	    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
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
	    
//	    long start4 = System.currentTimeMillis();
	    GL11.glColorPointer(BYTES_PER_COLOR, GL11.GL_UNSIGNED_BYTE, 0, vertexBytes);
	    logGlErrorIfAny();
	    GL11.glVertexPointer(ELEM_PER_VERTEX, GL11.GL_SHORT, 0, 0);
	    logGlErrorIfAny();
//	    long dura4 = System.currentTimeMillis() - start4;
//	    if (dura4 > 1000 / 60) {
//	    	log("dura4 " + dura4 + " ms");
//	    }
	    
//	    long start5 = System.currentTimeMillis();
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, facesCount * VERTICES_PER_FACE);
	    logGlErrorIfAny();
//	    long dura5 = System.currentTimeMillis() - start5;
//	    if (dura5 > 1000 / 60) {
//	    	log("dura5 " + bufId + " " + facesCount + " faces in " + dura5 + " ms");
//	    }
	    
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
	    logGlErrorIfAny();
	    
	    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	    logGlErrorIfAny();
	    GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
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
	
	private short queryHeightAt(int x, int y) {
		final short d = (DIM - 1) / 2;
		final int landX = this.x - d + x; // 9 -> 5
		final int landY = this.y - d + y; // 0 -> -4
		short h = t.getHeightAt(landX, landY);
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
	
	public short getHeightAt(int x, int y) {
		final short d = (DIM - 1) / 2;
		final int dx = x + d - this.x;
		final int dy = y + d - this.y;
		assert Math.abs(this.x - x) <= d : "x must not differ more than 4";
		assert Math.abs(this.y - y) <= d : "y must not differ more than 4";
		assert dx >= 0 && dx < DIM : "x idx out of array";
		assert dy >= 0 && dy < DIM : "y idx out of array";
		final short h = data[dx][dy];
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
