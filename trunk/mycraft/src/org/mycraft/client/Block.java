package org.mycraft.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.noise.IFunc2D;

public class Block {
	
	private static final int DIM = 16; // block dimensions (block data is byte)
	// = center + x rows & x columns & x layers
	// constants
	private static final int BITS_PER_BYTE = Byte.SIZE; // 8
	private static final int BYTES_PER_FLOAT = Float.SIZE / BITS_PER_BYTE; // 32/8=4
	private static final int COMPONENTS_PER_POINT = 3; // 3d - 3 components - 3 axis
	private static final int BYTES_PER_VERTEX = COMPONENTS_PER_POINT * BYTES_PER_FLOAT; // 3 * 4 = 12
//	private static final int POINTS_PER_FACE = 4;
//	private static final int VERTEXBYTES_PER_FACE = POINTS_PER_FACE * BYTES_PER_VERTEX; // 4 * 12 = 48
	private static final int BYTES_PER_COLORCOMPONENT = 1;
	private static final int COMPONENTS_PER_COLOR = 4; // r,g,b,a
	private static final int BYTES_PER_COLOR = COMPONENTS_PER_COLOR * BYTES_PER_COLORCOMPONENT; // 3 * 1 = 3
//	private static final int COLORBYTES_PER_FACE = POINTS_PER_FACE * BYTES_PER_COLOR; // 4 * 3 = 12
	private static final int BYTES_PER_SHORT = Short.SIZE / BITS_PER_BYTE; // 16/8=2
	
	private final Terrain terra;
	private final float blockX; // min(cellx)
	private final float blockY; // min(celly)
	private final float blockZ; // min(cellz)
	
	private List<Point3f> vertices;
	private List<Integer> colors;
	private List<Short> indices;
	
	private int faceCount = 0;
	private int vertexCount = 0;
	private int vxBufId = 0;
	private byte lowest;
	private byte highest;
	
	private int vertexBytes;
	private int colorBytes;
	private int indexBytes;
	
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
	
	protected float round(float f) {
		float r = (f >= 0f) ? f + 0.49999999f : f - 0.49999999f;
		double d = (r >= 0f) ? Math.floor(r) : Math.ceil(r);
		return (float) d;
	}
	
	// height - it may too low for this block; or too high; slice it into range [0..dim]
	protected float height(float tY) {
		// 0 & 10-5 = 0 & 5 = 5
		// 0 & 10--5 = 0 & 15 = 15
		// 0 & -10-5 = 0 & -15 = 0
		// 0 & -10--5 = 0 & -5 = 0
		float low = Math.max(0f, tY - blockY); // 0 or higher
		// 5 & 5 = 5
		// 5 & 15 = 5
		// 5 & 0 = 0
		// 5 & 0 = 0
		float high = Math.min(getDim(), low); // dim or lower
		return high;
	}
	
	protected int addVertexAndColor(Point3f p, Short i, List<Point3f> vrt, List<Integer> cls, float h) {
		vrt.add(p);
		// add color to vertex
		float spaceRange = (getDim() * 2.5f);
		float colorRange = 128f;
		float ratio = (colorRange / spaceRange);
		byte c2 = (byte) ((colorRange / 2) + (ratio * h));
		Integer c = 0; // r
		c |= (c2 << 8); // g
		c |= (0x00 << 16); // b
		c |= (Byte.MAX_VALUE << 24); // a
//		Integer c = 0xFF | (0xFF << 8) | (0xFF << 16) | (0xFF << 24); // rgba
//		log("c=" + Integer.toHexString(c));
		cls.add(c);
		return i;
	}
	
	protected short addCube(float x, float y, float z, short i,
			List<Point3f> vrt, List<Integer> cls, List<Short> idx) {
		// top face
		Point3f p = new Point3f(x+1, y+1, z+1); // top right back
		int i0 = vrt.indexOf(p);
		if (i0 == -1) { // new point
			float h = blockY; h += y; h += 1;
			i0 = addVertexAndColor(p, new Short(i), vrt, cls, h);
			i++;
		} // else got index of existing point
		idx.add((short)i0);
		p = new Point3f(x+1, y+1, z); // top right front
		int i1 = vrt.indexOf(p);
		if (i1 == -1) {
			float h = blockY; h += y; h += 1;
			i1 = addVertexAndColor(p, new Short(i), vrt, cls, h);
			i++;
		}
		idx.add((short)i1);
		p = new Point3f(x, y+1, z); // top left front
		int i2 = vrt.indexOf(p);
		if (i2 == -1) {
			float h = blockY; h += y; h += 1;
			i2 = addVertexAndColor(p, new Short(i), vrt, cls, h);
			i++;
		}
		idx.add((short)i2);
		p = new Point3f(x, y+1, z+1); // top left back
		int i3 = vrt.indexOf(p);
		if (i3 == -1) {
			float h = blockY; h += y; h += 1;
			i3 = addVertexAndColor(p, new Short(i), vrt, cls, h);
			i++;
		}
		idx.add((short)i3);
		// front face
		idx.add((short)i1);
		p = new Point3f(x+1, y, z); // bottom right front
		int i5 = vrt.indexOf(p);
		if (i5 == -1) {
			float h = blockY; h += y;
			i5 = addVertexAndColor(p, new Short(i), vrt, cls, h);
			i++;
		}
		idx.add((short)i5);
		p = new Point3f(x, y, z); // bottom left front
		int i6 = vrt.indexOf(p);
		if (i6 == -1) {
			float h = blockY; h += y;
			i6 = addVertexAndColor(p, new Short(i), vrt, cls, h);
			i++;
		}
		idx.add((short)i6);
		idx.add((short)i2);
		// right face
		idx.add((short)i0);
		p = new Point3f(x+1, y, z+1); // bottom right back
		int i4 = vrt.indexOf(p);
		if (i4 == -1) {
			float h = blockY; h += y;
			i4 = addVertexAndColor(p, new Short(i), vrt, cls, h);
			i++;
		}
		idx.add((short)i4);
		idx.add((short)i5);
		idx.add((short)i1);
		// left face
		idx.add((short)i2);
		idx.add((short)i6);
		p = new Point3f(x, y, z+1); // bottom left back
		int i7 = vrt.indexOf(p);
		if (i7 == -1) {
			float h = blockY; h += y;
			i7 = addVertexAndColor(p, new Short(i), vrt, cls, h);
			i++;
		}
		idx.add((short)i7);
		idx.add((short)i3);
		// back face
		idx.add((short)i3);
		idx.add((short)i7);
		idx.add((short)i4);
		idx.add((short)i0);
		// bottom face
		idx.add((short)i5);
		idx.add((short)i4);
		idx.add((short)i7);
		idx.add((short)i6);
		return i; // new (next) index
	}
	
	protected boolean isDense(float sY, byte cY) {
		return (sY > cY) ? true : false;
	}
	
	protected void indexVertices(List<Point3f> vrt, List<Integer> cls, List<Short> idx) {
		resetLowHigh();
		final int dim = getDim();
		IFunc2D func = terra.getFunc();
		short i = 0;
		for (byte iX = 0; iX < dim; iX++) {
			for (byte iZ = 0; iZ < dim; iZ++) {
				float terraX = blockX + iX;
				float terraZ = blockZ + iZ;
				float terraY = func.get(terraX, terraZ);
				float roundedY = round(terraY);
				float surfaceY = height(roundedY);
				checkLowHigh((byte)surfaceY);
				byte cnt = 0;
				for (byte iY = (byte) (dim-1); iY >= 0; iY--) {
					boolean dense = isDense(surfaceY, iY);
					if (dense) {
						if (cnt == 0) { // first (topmost) cube, create it
							i = addCube(iX, iY, iZ, i, vrt, cls, idx);
						} else {
							// check neighbours
							float tYr = func.get(terraX+1, terraZ);
							tYr = round(tYr);
							tYr = height(tYr);
							boolean denseR = isDense(tYr, iY);
							float tYl = func.get(terraX-1, terraZ);
							tYl = round(tYl);
							tYl = height(tYl);
							boolean denseL = isDense(tYl, iY);
							float tYf = func.get(terraX, terraZ-1);
							tYf = round(tYf);
							tYf = height(tYf);
							boolean denseF = isDense(tYf, iY);
							float tYb = func.get(terraX, terraZ+1);
							tYb = round(tYb);
							tYb = height(tYb);
							boolean denseB = isDense(tYb, iY);
							if (denseR == false || denseL == false || denseF == false || denseB == false) {
								// one of the neighbours is missing, add another cube
								i = addCube(iX, iY, iZ, i, vrt, cls, idx);
							}
						}
						cnt++;
					}
				}
			}
		}
	}
	
	public void generate() {
		if (vertices != null) {
			log("block " + blockX + ", " + blockY + ", " + blockZ + " already has data");
			return;
		}
//		long start = Sys.getTime();
		vertices = new ArrayList<Point3f>();
		colors = new ArrayList<Integer>();
		indices = new ArrayList<Short>();
//		log("generating block @ " + blockX + ", " + blockY + ", " + blockZ);
		indexVertices(vertices, colors, indices);
//		long dur = Sys.getTime() - start;
//		log("generate duration: " + dur);
		if (vertices.size() != colors.size()) {
			log("whoops! vertex & color counts dont match!");
		}
		vertexCount = vertices.size();
		if ((indices.size() % 4) != 0) {
			log("whoops! not enough indices for quads!");
		}
		faceCount = indices.size() / 4;
//		log("[" + blockX + "," + blockY + "," + blockZ + "] faces=" + faceCount +
//				" vertices=" + vertexCount + " l=" + lowest + " h=" + highest);
//		if (lowest >= highest) {
//			log("flat block: low=" + lowest + "; high=" + highest);
//		}
//		assert lowest == highest : "flat block";
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
		if (faceCount <= 0 || vxBufId != 0 || vertexCount <= 0) {
			return;
		}
		vxBufId = ARBBufferObject.glGenBuffersARB();
		logGlErrorIfAny();
//		log("created vbo " + vxBufId);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vxBufId);
		logGlErrorIfAny();
//		vertexBytes = faceCount * VERTEXBYTES_PER_FACE;
//		colorBytes = faceCount * COLORBYTES_PER_FACE;
		vertexBytes = vertices.size() * BYTES_PER_VERTEX;
		colorBytes = colors.size() * BYTES_PER_COLOR;
		indexBytes = indices.size() * BYTES_PER_SHORT;
		final int size = vertexBytes + colorBytes + indexBytes;
//		log("faces=" + faceCount + " vertices=" + vertexCount + "; vertex=" + vertexBytes + " bytes + " +
//				"color=" + colorBytes + " bytes + index=" + indexBytes + " bytes = "
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
		if (buf != null) {
			buf = buf.order(ByteOrder.nativeOrder());
		}
//		log("buffer cap:" + buf.capacity() + " lim:" + buf.limit() +
//				" pos:" + buf.position() + " rem:" + buf.remaining() + " dir:" + buf.isDirect() +
//				" ro:" + buf.isReadOnly() + " ord:" + buf.order());
		// fill buffer with data
		fillVBO(buf, vertices, colors, indices);
		if (buf != null) {
			buf.flip();
		}
		// unmap
		ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
		logGlErrorIfAny();
		// unbind - only one active array buffer at a time 
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		logGlErrorIfAny();
		indices = null;
		colors = null;
		vertices = null;
	}
	
	private void fillVBO(ByteBuffer b, List<Point3f> vrt, List<Integer> cls, List<Short> idx) {
		int i = 0;
		do {
			Point3f p = vrt.get(i);
			b.putFloat(p.getX());
			b.putFloat(p.getY());
			b.putFloat(p.getZ());
			Integer c = cls.get(i);
//			b.putInt(c);
			byte r = (byte) (c & 0xFF);
			byte g = (byte) ((c >> 8) & 0xFF);
			byte bb = (byte) ((c >> 16) & 0xFF);
			byte a = (byte) ((c >> 24) & 0xFF);
			b.put(r);
			b.put(g);
			b.put(bb);
			b.put(a);
			i++;
		} while (i < vrt.size());
		for (Short s : idx) {
			b.putShort(s);
//			byte l = (byte) (s & 0xFF);
//			byte h = (byte) ((s >> 8) & 0xFF);
//			b.put(l);
//			b.put(h);
		}
	}
	
	public void draw() {
		if (faceCount <= 0 || vxBufId == 0 || vertexCount <= 0) {
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
//	    GL11.glDrawArrays(GL11.GL_QUADS, 0, faceCount * POINTS_PER_FACE);
//	    logGlErrorIfAny();
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, vxBufId);
	    logGlErrorIfAny();
	    
//	    GL12.glDrawRangeElements(GL11.GL_QUADS, 0, (vertexBytes / BYTES_PER_VERTEX)-1, indexBytes / 2,
//	    		GL11.GL_UNSIGNED_SHORT, vertexBytes + colorBytes);
	    GL11.glDrawElements(GL11.GL_QUADS, indexBytes / 2, GL11.GL_UNSIGNED_SHORT, vertexBytes + colorBytes);
	    logGlErrorIfAny();
//	    long dura5 = System.currentTimeMillis() - start5;
//	    if (dura5 > 1000 / 60) {
//	    	log("dura5 " + bufId + " " + facesCount + " faces in " + dura5 + " ms");
//	    }
	    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
	    logGlErrorIfAny();
	    
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
		vertices = null;
		colors = null;
		indices = null;
		faceCount = 0;
		vertexCount = 0;
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
