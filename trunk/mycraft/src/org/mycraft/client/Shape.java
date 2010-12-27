package org.mycraft.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;

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
	
//	private static FloatBuffer mat;

	public Shape(TextureLoader texLoader, float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		if (txtr == null) {
			try {
				txtr = texLoader.getTexture("org/mycraft/client/cube.png");
			} catch (IOException e) {
				log("texture loading error: " + e);
			}
		}
		if (bufId == -1) {
			bufId = ARBBufferObject.glGenBuffersARB();
			logIfError();
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, bufId);
			logIfError();
			// one point consists of a)vertex b)texCoord c)index = everyone in block by itself
			log("vertices=" + VERTICES_BYTES + ", texCoords=" + TEXCOORDS_BYTES + ", indices=" + INDICES_BYTES);
			int size = VERTICES_BYTES + TEXCOORDS_BYTES /*+ INDICES_BYTES*/;
			ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, size,
					ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
			logIfError();
			ByteBuffer buf = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,
					ARBVertexBufferObject.GL_WRITE_ONLY_ARB, size, null);
			logIfError();
			fillVBO(buf);
			logIfError();
			ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
			logIfError();
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
			logIfError();
		}
//		if (vtxBufId == -1) {
//			vtxBufId = ARBBufferObject.glGenBuffersARB();
//			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vtxBufId);
//			int size = VERTICES_COUNT * VERTEX_SIZE * shortSize;
//			ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, size,
//					ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
//			ByteBuffer buf = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,
//					ARBVertexBufferObject.GL_WRITE_ONLY_ARB, size, null);
//			buf = buf.order(ByteOrder.nativeOrder());
//			fillVertices(buf);
//			buf.flip();
//			ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
//			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
//		}
//		if (idxBufId == -1) {
//			idxBufId = ARBBufferObject.glGenBuffersARB();
//			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, idxBufId);
//			int size = INDICES_COUNT * QUAD_INDICES_SIZE * byteSize;
//			ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, size,
//					ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
//			ByteBuffer buf = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB,
//					ARBVertexBufferObject.GL_WRITE_ONLY_ARB, size, null);
//			buf = buf.order(ByteOrder.nativeOrder());
//			fillIndices(buf);
//			buf.flip();
//			ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB);
//			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
//		}
//		if (mat == null) {
//			mat = BufferUtils.createFloatBuffer(4);
//			mat.put(new float[] { 1f, 1f, 1f, 1f, }); // 4th=0f=transparent cube/1f=solid cube
//			mat.flip();
//		}
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
		log("vertices=" + cnt + " shorts");
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
		log("texCoords=" + cnt + " floats");
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
	
//	protected void fillTexCoords(ByteBuffer b) {
//		// 1. face
////		b.putFloat(0f).putFloat(0.25f);
////		b.putFloat(0.25f).putFloat(0.25f);
////		b.putFloat(0.25f).putFloat(0f);
////		b.putFloat(0f).putFloat(0f);
////		b.putFloat(0f).putFloat(0.75f);
////		b.putFloat(0.25f).putFloat(0.75f);
////		b.putFloat(0.25f).putFloat(1f);
////		b.putFloat(0f).putFloat(1f);
//		for (int i = 1; i <= 6; i++) {
//			b.putFloat(0f).putFloat(0f);
//			b.putFloat(1f).putFloat(0f);
//			b.putFloat(1f).putFloat(1f);
//			b.putFloat(0f).putFloat(1f);
//		}
//		// 2. face
////		b.putFloat(0.25f).putFloat(0.25f);
////		b.putFloat(0.5f).putFloat(0.25f);
////		b.putFloat(0.5f).putFloat(0f);
////		b.putFloat(0.25f).putFloat(0f);
////		// 3. face
////		b.putFloat(0.5f).putFloat(0.25f);
////		b.putFloat(0.75f).putFloat(0.25f);
////		b.putFloat(0.75f).putFloat(0f);
////		b.putFloat(0.5f).putFloat(0f);
////		// 4. face
////		b.putFloat(0.75f).putFloat(0.25f);
////		b.putFloat(1f).putFloat(0.25f);
////		b.putFloat(1f).putFloat(0f);
////		b.putFloat(0.75f).putFloat(0f);
////		// 5. face
////		b.putFloat(0.75f).putFloat(0.5f);
////		b.putFloat(1f).putFloat(0.5f);
////		b.putFloat(1f).putFloat(0.25f);
////		b.putFloat(0.75f).putFloat(0.25f);
////		// 6. face
////		b.putFloat(0.75f).putFloat(0.75f);
////		b.putFloat(1f).putFloat(0.75f);
////		b.putFloat(1f).putFloat(0.5f);
////		b.putFloat(0.75f).putFloat(0.5f);
//	}
//	
//	protected void fillVertices(ByteBuffer b) {
//		b.putShort((short)-CUBE_DIM).putShort((short)-CUBE_DIM).putShort(CUBE_DIM);
//		b.putShort(CUBE_DIM).putShort((short)-CUBE_DIM).putShort(CUBE_DIM);
//		b.putShort(CUBE_DIM).putShort(CUBE_DIM).putShort(CUBE_DIM);
//		b.putShort((short)-CUBE_DIM).putShort(CUBE_DIM).putShort(CUBE_DIM);
//		b.putShort(CUBE_DIM).putShort((short)-CUBE_DIM).putShort((short)-CUBE_DIM);
//		b.putShort((short)-CUBE_DIM).putShort((short)-CUBE_DIM).putShort((short)-CUBE_DIM);
//		b.putShort((short)-CUBE_DIM).putShort(CUBE_DIM).putShort((short)-CUBE_DIM);
//		b.putShort(CUBE_DIM).putShort(CUBE_DIM).putShort((short)-CUBE_DIM);
//	}
//	
//	protected void fillIndices(ByteBuffer b) {
//		short[][] i = indices();
//		for (short[] a : i) {
//			for (short v : a) {
//				b.put((byte)v);
//			}
//		}
//	}
	
	public void draw() {
	    GL11.glPushMatrix();
	    GL11.glTranslatef(x, y, z);
	    
//		GL11.glShadeModel(GL11.GL_SMOOTH);
//		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE, mat);
	    txtr.bind();
	    
	    GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
	    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
//	    GL11.glEnableClientState(GL11.GL_INDEX_ARRAY);
	    
//	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, txcBufId);
//	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vtxBufId);
//	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, idxBufId);
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, bufId);
	    
//	    GL11.glTexCoordPointer(TEXCOORD_SIZE, GL11.GL_FLOAT, /*4 * TEXCOORD_SIZE * floatSize*/0, 0);
//	    GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
//	    GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_SHORT, /*VERTEX_SIZE * shortSize*/0, 0);
//	    GL11.glDrawElements(GL11.GL_QUADS, INDICES_COUNT * QUAD_INDICES_SIZE * byteSize, GL11.GL_UNSIGNED_BYTE, 0);
	    GL11.glTexCoordPointer(TEXCOORD_SIZE, GL11.GL_FLOAT, 0, VERTICES_BYTES);
	    GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_SHORT, 0, 0);
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, VERTICES_COUNT);
	    
//	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
//	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_BINDING_ARB, 0);
	    
//	    GL11.glDisableClientState(GL11.GL_INDEX_ARRAY);
	    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	    GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
	    
	    GL11.glPopMatrix();
	}
	
	public void destroy() {
		if (bufId != -1) {
			ARBVertexBufferObject.glDeleteBuffersARB(bufId);
			bufId = -1;
		}
		if (txtr != null) {
			txtr = null;
		}
	}
	
	private void log(String s) {
		System.out.println(s);
	}
	
	private void logIfError() {
		final int e = GL11.glGetError();
		if (e != 0) {
			log("err=" + e);
		}
	}
	
}
