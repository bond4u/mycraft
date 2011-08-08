package org.mycraft.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.noise.IFunc2D;

public class Block {
	
	private static final int DIM = 5; // block dimensions (block data is byte)
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
	
	private final Terrain terra;
	private final float blockX; // min(cellx)
	private final float blockY; // min(celly)
	private final float blockZ; // min(cellz)
	private byte data[][][];
	private int faceCount = 0;
	private int vertexCount = 0;
	private int vxBufId = 0;
	private byte lowest;
	private byte highest;
	
	private int vertexBytes;
	private int colorBytes;
	
	public Block(Terrain t, float x, float y, float z) {
		this.terra = t;
		this.blockX = x;
		this.blockY = y;
		this.blockZ = z;
//		log("Block.ctor(" + x + "," + y + "," + z + ")");
	}
	
	public static short getDim() {
		return DIM;
	}
	
	public float getX() {
		return blockX;
	}
	
	public float getY() {
		return blockY;
	}
	
	public float getZ() {
		return blockZ;
	}
	
	public void generate() {
		if (data != null) {
			log("block " + blockX + ", " + blockY + ", " + blockZ + " already has data");
			return;
		}
		resetLowHigh();
		final int dim = getDim();
		if (data == null) {
			data = new byte[dim][dim][dim];
		}
		faceCount = 0;
		Set<Point3f> verts = new HashSet<Point3f>();
		IFunc2D func = terra.getFunc();
//		log("generating block @ " + blockX + ", " + blockY + ", " + blockZ);
		for (byte iX = 0; iX < dim; iX++) {
			for (byte iZ = 0; iZ < dim; iZ++) {
				float cellX = blockX + iX;
				float cellZ = blockZ + iZ; // Y is up
//				log("[" + x2 + "," + y2 + "] = [" + x3 + "," + y3 + "]");
				float cellY = func.get(cellX, cellZ);
				float f = (cellY >= 0f) ? cellY + 0.49999999f : cellY - 0.49999999f;
				double d = (f >= 0f) ? Math.floor(f) : Math.ceil(f);
				float cellH = (float) d;
				// height - it may too low for this block; or too high; slice it into range [0..dim]
				float low = cellH - blockY; // Y is up
				float high = Math.max(0f, low); // 0 or higher
//				log("cellX=" + cellX + " cellY=" + cellY + " cellZ=" + cellZ + " low=" + lowZ + " high=" + highZ);
				cellH = Math.min(dim, high); // "dim" or lower
				byte h = (byte) cellH;
				faceCount += h*6;
//				log("cell=" + cellZ + " h=" + h + " faceCount=" + faceCount);
				checkLowHigh(h);
				// fill the the 3rd dim
				for (byte iY = 0; iY < dim; iY++) {
					byte t = (byte) (h > iY ? 1 : 0);
//					log("type=" + t + " for height " + iZ);
					if (1 == t) { // earth
						Point3f p = new Point3f(iX, iY, iZ); // front bottom left
						verts.add(p);
						p = new Point3f(iX, iY+1, iZ); // front top left
						verts.add(p);
						p = new Point3f(iX+1, iY, iZ); // front bottom right
						verts.add(p);
						p = new Point3f(iX+1, iY+1, iZ); // front top right
						verts.add(p);
						p = new Point3f(iX, iY, iZ+1); // back bottom left
						verts.add(p);
						p = new Point3f(iX, iY+1, iZ+1); // back top left
						verts.add(p);
						p = new Point3f(iX+1, iY, iZ+1); // back bottom right
						verts.add(p);
						p = new Point3f(iX+1, iY+1, iZ+1); // back top right
						verts.add(p);
					}
					data[iX][iY][iZ] = t; // block types: 0-air, 1-earth
				}
			}
		}
		vertexCount = verts.size();
//		log("[" + blockX + "," + blockY + "," + blockZ + "] faces=" + faceCount + " l=" + lowest + " h=" + highest);
//		if (lowest >= highest) {
//			log("flat block: low=" + lowest + "; high=" + highest);
//		}
		assert lowest == highest : "flat block";
	}
	
	protected byte[][][] getData() {
		return data;
	}
	
	public int getFaceCount() {
		return faceCount;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	protected void resetLowHigh() {
		lowest = Byte.MAX_VALUE;
		highest = Byte.MIN_VALUE;
	}
	
	protected void checkLowHigh(byte h) {
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
	
	public int getVboId() {
		return vxBufId;
	}
	
	public void initVBO() {
		if (faceCount <= 0 || vxBufId != 0) {
			return;
		}
		vxBufId = ARBBufferObject.glGenBuffersARB();
		logGlErrorIfAny();
//		log("created vbo " + vxBufId);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vxBufId);
		logGlErrorIfAny();
		vertexBytes = faceCount * VERTEXBYTES_PER_FACE;
		colorBytes = faceCount * COLORBYTES_PER_FACE;
		final int size = vertexBytes + colorBytes;
//		log("faces=" + faceCount + " vertex=" + vertexBytes + " bytes, color=" + colorBytes + " bytes = "
//				+ "total=" + size + " bytes");
		// create VBO
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,
				size, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
		logGlErrorIfAny();
		// map it for write
		ByteBuffer buf = ARBVertexBufferObject.glMapBufferARB(
				ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,
				ARBVertexBufferObject.GL_WRITE_ONLY_ARB, size, null);
		if (buf != null) {
			buf = buf.order(ByteOrder.nativeOrder());
		}
//		log("buffer cap:" + buf.capacity() + " lim:" + buf.limit() +
//				" pos:" + buf.position() + " rem:" + buf.remaining() + " dir:" + buf.isDirect() +
//				" ro:" + buf.isReadOnly() + " ord:" + buf.order());
		logGlErrorIfAny();
		// fill buffer with data
		fillVBO(buf);
		if (buf != null) {
			buf.flip();
		}
		// unmap
		ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
		logGlErrorIfAny();
		// unbind - only one active array buffer at a time 
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		logGlErrorIfAny();
	}
	
	private void fillVBO(ByteBuffer b) {
		final int dim = getDim();
		final byte zero = Byte.MIN_VALUE;
		final byte alpha = Byte.MAX_VALUE;
		final byte val = 24; // TEMP
		final float step = 128 / (float) val; // 255/32=7.96875
		final float base = step * 8; // 16*7.96875=127.5
		for (int x = 0; x < dim; x++) {
			for (int z = 0; z < dim; z++) {
				for (int y = 0; y < dim; y++) {
					byte t = data[x][y][z];
					if (t != 0) {
						byte c = (byte) (base + (blockY+y-1) * step);
						// top face
						b.putFloat((x + 1));
						b.putFloat(y+1);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// top2
						b.putFloat(x);
						b.putFloat(y+1);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// top3
						b.putFloat(x);
						b.putFloat(y+1);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// top4
						b.putFloat((x + 1));
						b.putFloat(y+1);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// right face
						b.putFloat((x + 1));
						b.putFloat(y);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// right2
						b.putFloat((x + 1));
						b.putFloat(y+1);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// right3
						b.putFloat((x + 1));
						b.putFloat(y+1);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// right4
						b.putFloat((x + 1));
						b.putFloat(y);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// front face
						b.putFloat((x + 1));
						b.putFloat(y+1);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// front2
						b.putFloat(x);
						b.putFloat(y+1);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// front3
						b.putFloat(x);
						b.putFloat(y);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// front4
						b.putFloat((x + 1));
						b.putFloat(y);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// left face
						b.putFloat(x);
						b.putFloat(y+1);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// left2
						b.putFloat(x);
						b.putFloat(y);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// left3
						b.putFloat(x);
						b.putFloat(y);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// left4
						b.putFloat(x);
						b.putFloat(y+1);
						b.putFloat((z + 1));
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// back face
						b.putFloat((x + 1));
						b.putFloat(y);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// front2
						b.putFloat(x);
						b.putFloat(y);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// front3
						b.putFloat(x);
						b.putFloat(y+1);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// front4
						b.putFloat((x + 1));
						b.putFloat(y+1);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// top face
						b.putFloat((x + 1));
						b.putFloat(y);
						b.putFloat(z+1);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// top2
						b.putFloat(x);
						b.putFloat(y);
						b.putFloat(z+1);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// top3
						b.putFloat(x);
						b.putFloat(y);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
						// top4
						b.putFloat((x + 1));
						b.putFloat(y);
						b.putFloat(z);
						// color
						b.put(zero);
						b.put(c);
						b.put(zero);
						b.put(alpha);
					}
				} // y
			} // z
		} // x
	}
	
	public void draw() {
		if (faceCount <= 0 || vxBufId == 0) {
			return;
		}
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
	    GL11.glTranslatef(blockX, blockY, blockZ);
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
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vxBufId);
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
	    GL11.glVertexPointer(COMPONENTS_PER_POINT, GL11.GL_FLOAT, BYTES_PER_VERTEX + BYTES_PER_COLOR, 0);
	    logGlErrorIfAny();
	    GL11.glColorPointer(BYTES_PER_COLOR, GL11.GL_BYTE, BYTES_PER_VERTEX + BYTES_PER_COLOR, BYTES_PER_VERTEX);
	    logGlErrorIfAny();
//	    long dura4 = System.currentTimeMillis() - start4;
//	    if (dura4 > 1000 / 60) {
//	    	log("dura4 " + dura4 + " ms");
//	    }
	    
//	    long start5 = System.currentTimeMillis();
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, faceCount * POINTS_PER_FACE);
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
	
	public void freeVbo() {
		if (0 != vxBufId) {
//			System.out.println("deleting vboId=" + vxBufId);
			ARBVertexBufferObject.glDeleteBuffersARB(vxBufId);
			logGlErrorIfAny();
			vxBufId = 0;
		}
	}
	
	public void destroy() {
		if (data != null) {
			data = null;
		}
		faceCount = 0;
	}
	
	public static Point3f calcBlockPoint(float x, float y, float z) {
		final int dim = getDim();
		final float bx = dim * (float) Math.floor(x / dim);
		final float by = dim * (float) Math.floor(y / dim);
		final float bz = dim * (float) Math.floor(z / dim);
//		System.out.println("x=" + x + " y=" + y + " xc=" + xc + " yc=" + yc);
		return new Point3f(bx, by, bz); // 8+7=15/15=1
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
