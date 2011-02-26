package org.mycraft.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;

public class Shape {

	private float x, y, z;
	
	private static Texture txtr;
	
	private static final byte CUBE_DIM = 1;
//	private static final int byteSize = Byte.SIZE / 8;
	private static final int shortSize = Short.SIZE / 8;
	private static final int floatSize = Float.SIZE / 8;
	
	private static final int FACES_COUNT = 6;
	private static final int POINTS_PER_FACE = 4;
	
	private static final int VERTEX_SIZE = 3; // shorts
	private static final int VERTEX_BYTES = VERTEX_SIZE * shortSize;
	private static final int VERTICES_COUNT = FACES_COUNT * POINTS_PER_FACE;
	private static final int VERTICES_BYTES = VERTICES_COUNT * VERTEX_BYTES;
	
	private static final int TEXCOORD_SIZE = 2; // floats
	private static final int TEXCOORD_BYTES = TEXCOORD_SIZE * floatSize;
	private static final int TEXCOORDS_COUNT = FACES_COUNT * POINTS_PER_FACE;
	private static final int TEXCOORDS_BYTES = TEXCOORDS_COUNT * TEXCOORD_BYTES;
	
	private static final int INDEX_SIZE = 2; // shorts
	private static final int INDEX_BYTES = INDEX_SIZE * shortSize;
	private static final int INDICES_COUNT = FACES_COUNT * POINTS_PER_FACE;
	private static final int INDICES_BYTES = INDICES_COUNT * INDEX_BYTES;
	
	private static int bufId = -1;
	
	public Shape(Textures texs, float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		if (txtr == null) {
			txtr = texs.get("org/mycraft/client/cube.png");
		}
		if (bufId == -1) {
			bufId = ARBBufferObject.glGenBuffersARB();
		    logGlErrorIfAny();
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, bufId);
		    logGlErrorIfAny();
			// one point consists of a)vertex b)texCoord c)index = everyone in block by itself
//			log("vertices=" + VERTICES_BYTES + ", texCoords=" + TEXCOORDS_BYTES + ", indices=" + INDICES_BYTES);
			int size = VERTICES_BYTES + TEXCOORDS_BYTES /*+ INDICES_BYTES*/;
			ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,
					size, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
		    logGlErrorIfAny();
			ByteBuffer buf = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,
					ARBVertexBufferObject.GL_WRITE_ONLY_ARB, size, null);
		    logGlErrorIfAny();
			fillVBO(buf);
		    logGlErrorIfAny();
			ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
		    logGlErrorIfAny();
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		    logGlErrorIfAny();
		}
	}
	
	protected void fillVBO(ByteBuffer b) {
		b = b.order(ByteOrder.nativeOrder());
		final short[][] vrt = vertices();
		final short[][] idx = indices();
		int cnt = 0;
		for (int face = 0; face < FACES_COUNT; face++) {
			short[] face_idxs = idx[face];
			for (int pt = 0; pt < POINTS_PER_FACE; pt++) {
				int vrt_idx = face_idxs[pt];
				short[] v = vrt[vrt_idx];
				for (short s : v) {
					b.putShort(s);
					cnt++;
				}
			}
		}
//		log("vertices=" + cnt + " shorts");
		final float[][][] tc = texCoords();
		cnt = 0;
		for (int face = 0; face < FACES_COUNT; face++) {
			float[][] face_tcs = tc[face];
			for (int pt = 0; pt < POINTS_PER_FACE; pt++) {
				float[] face_pt_tc = face_tcs[pt];
				for (float f : face_pt_tc) {
					b.putFloat(f);
					cnt++;
				}
			}
		}
//		log("texCoords=" + cnt + " floats");
		b.flip();
	}
	
	protected short[][] vertices() {
		final short[][] v = new short[][] {
				{ -CUBE_DIM, -CUBE_DIM, CUBE_DIM, },
				{ CUBE_DIM, -CUBE_DIM, CUBE_DIM, },
				{ CUBE_DIM, CUBE_DIM, CUBE_DIM, },
				{ -CUBE_DIM, CUBE_DIM, CUBE_DIM, },
				{ CUBE_DIM, -CUBE_DIM, -CUBE_DIM, },
				{ -CUBE_DIM, -CUBE_DIM, -CUBE_DIM, },
				{ -CUBE_DIM, CUBE_DIM, -CUBE_DIM, },
				{ CUBE_DIM, CUBE_DIM, -CUBE_DIM, },
		};
		return v;
	}
	
	protected short[][] indices() {
		final short[][] i = new short[][] {
				{ 0, 1, 2, 3, },
				{ 1, 4, 7, 2, },
				{ 4, 5, 6, 7, },
				{ 7, 6, 3, 2, },
				{ 5, 0, 3, 6, },
				{ 4, 1, 0, 5, },
		};
		return i;
	}
	
	protected float[][][] texCoords() {
		final float[][][] c = new float[][][] {
			{ // front
				{ 0f, 0.25f, },
				{ 0.25f, 0.25f, },
				{ 0.25f, 0f, },
				{ 0f, 0f, },
			},
			{ // right
				{ 0.25f, 0.25f, },
				{ 0.5f, 0.25f, },
				{ 0.5f, 0f, },
				{ 0.25f, 0f, },
			},
			{ // back
				{ 0.5f, 0.25f, },
				{ 0.75f, 0.25f, },
				{ 0.75f, 0f, },
				{ 0.5f, 0f, },
			},
			{ // top
				{ 1f, 0f, },
				{ 0.75f, 0f, },
				{ 0.75f, 0.25f, },
				{ 1f, 0.25f, },
			},
			{ // left
				{ 0.75f, 0.5f, },
				{ 1f, 0.5f, },
				{ 1f, 0.25f, },
				{ 0.75f, 0.25f, },
			},
			{ // bottom
				{ 1f, 0.75f, },
				{ 1f, 0.5f, },
				{ 0.75f, 0.5f, },
				{ 0.75f, 0.75f, },
			},
		};
		return c;
	}
	
	public void draw() {
	    GL11.glPushMatrix();
	    logGlErrorIfAny();
	    
	    GL11.glScalef(0.5f, 0.5f, 0.5f);
	    logGlErrorIfAny();
	    GL11.glTranslatef(x, y, z);
	    logGlErrorIfAny();
	    
	    GL11.glColor4f(1f, 1f, 1f, 1f);
	    logGlErrorIfAny();
	    txtr.bind();
	    
	    GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
	    logGlErrorIfAny();
	    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	    logGlErrorIfAny();
	    
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, bufId);
	    logGlErrorIfAny();
	    
	    GL11.glTexCoordPointer(TEXCOORD_SIZE, GL11.GL_FLOAT, 0, VERTICES_BYTES);
	    logGlErrorIfAny();
	    GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_SHORT, 0, 0);
	    logGlErrorIfAny();
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, VERTICES_COUNT);
	    logGlErrorIfAny();
	    
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
	    logGlErrorIfAny();
	    
	    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	    logGlErrorIfAny();
	    GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
	    logGlErrorIfAny();
	    
	    GL11.glPopMatrix();
	    logGlErrorIfAny();
	}
	
	public void destroy() {
		if (bufId != -1) {
			ARBVertexBufferObject.glDeleteBuffersARB(bufId);
		    logGlErrorIfAny();
			bufId = -1;
		}
		if (txtr != null) {
			txtr = null;
		}
	}
	
	private void log(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
	
	private void logGlErrorIfAny() {
		final int e = GL11.glGetError();
		if (e != 0) {
			log("err=" + e + ": " + GLU.gluErrorString(e));
		}
	}
	
}
