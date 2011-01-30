package org.mycraft.client;

import java.nio.ByteBuffer;
import java.util.Random;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Block {
	
	public static final int DIM = 9; // block dimension - 5 rows & 5 columns
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
	private final Random rnd;
	private final short data[][];
	private final int bufId;
	
	private int facesCount;
	private int vertexBytes;
	private int colorBytes;
	
	public Block(Terrain t, int x, int y, Random rnd) {
		this.t = t;
		this.x = x;
		this.y = y;
		this.rnd = rnd;
		this.data = gen();
		this.bufId = createVBO();
//		initVBO();
	}
	
	private short[][] gen() {
		final short d = (DIM - 1) / 2;
		final short[][] a = new short[DIM][DIM];
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				a[x][y] = (short) (rnd.nextInt(DIM) - d);
			}
		}
		return a;
	}
	
	private int createVBO() {
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
		final int step = 256 / DIM;
		final int base = (1 + (DIM - 1) / 2) * step;
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				final short height = (short) data[x][y];
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
	    GL11.glPushMatrix();
	    logGlErrorIfAny();
	    
//	    GL11.glScalef(0.5f, 0.5f, 0.5f);
//	    logGlErrorIfAny();
	    GL11.glTranslatef(this.x - DIM / 2f, 0f, this.y - DIM / 2f);
	    logGlErrorIfAny();
	    
	    GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
	    logGlErrorIfAny();
	    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	    logGlErrorIfAny();
	    
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, bufId);
	    logGlErrorIfAny();
	    
	    GL11.glColorPointer(BYTES_PER_COLOR, GL11.GL_UNSIGNED_BYTE, 0, vertexBytes);
	    logGlErrorIfAny();
	    GL11.glVertexPointer(ELEM_PER_VERTEX, GL11.GL_SHORT, 0, 0);
	    logGlErrorIfAny();
	    
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, facesCount * VERTICES_PER_FACE);
	    logGlErrorIfAny();
	    
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
	    logGlErrorIfAny();
	    
	    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	    logGlErrorIfAny();
	    GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
	    logGlErrorIfAny();
	    
	    GL11.glPopMatrix();
	    logGlErrorIfAny();
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
	
	public short getHeightAt(int x, int y) {
		final short d = (DIM - 1) / 2;
		final int dx = x + d - this.x;
		final int dy = y + d - this.y;
		assert Math.abs(this.x - x) <= 4 : "x must not differ more than 4";
		assert Math.abs(this.y - y) <= 4 : "y must not differ more than 4";
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
