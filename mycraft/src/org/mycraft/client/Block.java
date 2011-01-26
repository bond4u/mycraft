package org.mycraft.client;

import java.nio.ByteBuffer;
import java.util.Random;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Block {
	
	public static final int DIM = 7; // block dimension - 5 rows & 5 columns
	// vertex data
	private static final int BYTES_PER_SHORT = 2; // 2 bytes per element
	private static final int ELEM_PER_VERTEX = 3; // 3 elements per vertex
	private static final int BYTES_PER_VERTEX = ELEM_PER_VERTEX * BYTES_PER_SHORT; // 3 * 2 = 6 bytes per vertex
	private static final int VERTICES_PER_FACE = 4; // 4 vertices per face
	private static final int BYTES_PER_FACE = VERTICES_PER_FACE * BYTES_PER_VERTEX; // 4 * 6 = 24 bytes per face
//	private static final int FACES_COUNT = 1; // one face = top
	private static final int FACES_COUNT = DIM * DIM; // 5 * 5 = 25 faces per block
	private static final int VERTICES_COUNT = FACES_COUNT * VERTICES_PER_FACE;
	private static final int BYTES_PER_VERTICES = FACES_COUNT * BYTES_PER_FACE; // 25 * 24 bytes for block vertices
	// color data per vertex
	private static final int BYTES_PER_COLOR = 3;
	private static final int BYTES_PER_COLORS = FACES_COUNT * VERTICES_PER_FACE * BYTES_PER_COLOR;
	
	private final int x;
	private final int y;
	private final Random rnd;
	private final int data[][];
	private final int bufId;
	
	public Block(int x, int y, Random rnd) {
		this.x = x;
		this.y = y;
		this.rnd = rnd;
		this.data = gen();
		this.bufId = createVBO();
		initVBO();
	}
	
	private int[][] gen() {
		final int d = (DIM - 1) / 2;
//		final int x0 = this.x - d;
//		final int x1 = x0 + DIM;
//		final int y0 = this.y - d;
//		final int y1 = y0 + DIM;
		final int[][] a = new int[DIM][DIM];
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				a[x][y] = rnd.nextInt(DIM) - d;
			}
		}
		return a;
	}
	
	private int createVBO() {
		final int id = ARBBufferObject.glGenBuffersARB();
		logGlErrorIfAny();
		return id;
	}
	
	private void initVBO() {
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, this.bufId);
		logGlErrorIfAny();
		final int size = BYTES_PER_VERTICES + BYTES_PER_COLORS;
		log("vertices=" + BYTES_PER_VERTICES + " bytes, colors=" + BYTES_PER_COLORS + " bytes = "
				+ " total=" + size + " bytes");
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
	
	private void fillVBO(ByteBuffer b) {
//		final int d = (DIM - 1) / 2;
//		final int x0 = this.x - d;
//		final int x1 = x0 + DIM;
//		final int y0 = this.y - d;
//		final int y1 = y0 + DIM;
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				b.putShort((short) (x + 1));
				b.putShort((short) data[x][y]);
				b.putShort((short) y);
				b.putShort((short) x);
				b.putShort((short) data[x][y]);
				b.putShort((short) y);
				b.putShort((short) x);
				b.putShort((short) data[x][y]);
				b.putShort((short) (y + 1));
				b.putShort((short) (x + 1));
				b.putShort((short) data[x][y]);
				b.putShort((short) (y + 1));
			}
		}
		final int step = 256 / DIM;
		final int base = (1 + (DIM - 1) / 2) * step;
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				b.put((byte) 0);
				b.put((byte) (base + data[x][y] * step));
				b.put((byte) 0);
				b.put((byte) 0);
				b.put((byte) (base + data[x][y] * step));
				b.put((byte) 0);
				b.put((byte) 0);
				b.put((byte) (base + data[x][y] * step));
				b.put((byte) 0);
				b.put((byte) 0);
				b.put((byte) (base + data[x][y] * step));
				b.put((byte) 0);
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
	    
	    GL11.glColorPointer(BYTES_PER_COLOR, GL11.GL_UNSIGNED_BYTE, 0, BYTES_PER_VERTICES);
	    logGlErrorIfAny();
	    GL11.glVertexPointer(ELEM_PER_VERTEX, GL11.GL_SHORT, 0, 0);
	    logGlErrorIfAny();
	    
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, VERTICES_COUNT);
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
