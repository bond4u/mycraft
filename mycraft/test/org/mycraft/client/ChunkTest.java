package org.mycraft.client;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mycraft.client.Chunk;
import org.mycraft.client.Camera;
import org.mycraft.client.Point3f;
import org.mycraft.client.Viewport;
import org.mycraft.client.terrain.BlockType;
import org.mycraft.client.terrain.Generator;
import org.mycraft.client.terrain.Terrain;

public class ChunkTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testChunk() {
		Random r = new Random(653654654);
		Viewport v = new Viewport();
		Camera c = new Camera();
		Terrain t = new Terrain(r, v, c);
		try {
			// block ctor doesnt "do" GL
			Chunk b = new Chunk(t.getGenerator(), 0f, 0f, 0f);
			// neither does "generate"
			b.generate();
		} catch (Throwable th) {
			fail("Block ctor must not throw");
		}
	}

//	@Test
//	public void testDraw() {
//	}
//
//	@Test
//	public void testDestroy() {
//	}
	
	@Test
	public void testCalcCenter() {
		// check block values -22..-8,-7..7,8..23
		short blkDim = Chunk.getDim();
		for (float x = 2 * -blkDim; x < 2 * blkDim; x += 0.5f) {
			for (float y = 2 * -blkDim; y < 2 * blkDim; y += 0.5f) {
				for (float z = 2 * -blkDim; z < 2 * blkDim; z += 0.5f) {
					Point3f c = Chunk.calcBlockPoint(x, y, z);
					float x2 = (float) (blkDim * Math.floor(x / blkDim));
					float y2 = (float) (blkDim * Math.floor(y / blkDim));
					float z2 = (float) (blkDim * Math.floor(z / blkDim));
//					log("x=" + x + ",y=" + y + ",z=" + z + " c=" + c + " x2=" + x2 + ",y2=" + y2 + ",z2=" + z2);
					assertTrue(c.getX() == x2);
					assertTrue(c.getY() == y2);
					assertTrue(c.getZ() == z2);
				}
			}
		}
	}
	
	protected byte height(Chunk b, Generator g, int x, int z) {
		byte h = 0;
		for (byte y = 0; y < Chunk.getDim(); y++) {
			BlockType bt = g.get(b.getX() + x, b.getY() + y, b.getZ() + z);
			if (bt.isDense()) {
				h = y;
			}
		}
		return h;
	}
	
	@Test
	@Ignore /* no longer valid - ground can have holes in it, caves, overhangs etc */
	public void testChunkBorder() {
		Random r = new Random(5432543);//546543654);
		Viewport v = new Viewport();
		Camera c = new Camera();
		final Chunk[] blocks = { null, null, null, null, null, };
		final int dim = Chunk.getDim();
		final float tx = dim * 3;
		final float ty = 0f;
		final float tz = dim * 4;
		Terrain t = new Terrain(r, v, c) {
			protected Chunk createBlock(float x, float y, float z) {
				return new Chunk(getGenerator(), x, y, z) {
					public void initVBO() {
						// do nothing
					}
				};
			}
			protected void calc(Viewport v, Camera c) {
				blocks[0] = addBlock(new Point3f(tx, ty-2*dim, tz));
				blocks[1] = addBlock(new Point3f(tx, ty-dim, tz));
				blocks[2] = addBlock(new Point3f(tx, ty, tz));
				blocks[3] = addBlock(new Point3f(tx, ty+dim, tz));
				blocks[4] = addBlock(new Point3f(tx, ty+2*dim, tz));
				log("notifying blocks");
				synchronized(blocks) {
					blocks.notify();
				}
			}
		};
		t.create();
		try {
			log("waiting for blocks");
			synchronized(blocks) {
				blocks.wait();
			}
		} catch (InterruptedException ie) {
		}
		for (int x = 0; x < dim; x++) {
			for (int z = 0; z < dim; z++) {
				byte[] b = {
						height(blocks[0], t.getGenerator(), x, z), // lower2 block
						height(blocks[1], t.getGenerator(), x, z), // lower block
						height(blocks[2], t.getGenerator(), x, z), // middle block
						height(blocks[3], t.getGenerator(), x, z),// upper block
						height(blocks[4], t.getGenerator(), x, z), }; // upper2 block
				log("b0=" + b[0] + " b1=" + b[1] + " b2=" + b[2] + " b3=" + b[3] + " b4=" + b[4]);
				// if lower block is max then upper block must be?
				if (b[0] >= dim) {
					assertTrue(b[1] >= 0);
				} else {
					assertTrue(b[1] == 0); // middle block cant be higher than lower block
				}
				if (b[1] >= dim) {
					assertTrue(b[2] >= 0);
				} else {
					assertTrue(b[2] == 0); // upper block cant be higher than middle block
				}
				if (b[2] >= dim) {
					assertTrue(b[3] >= 0);
				} else {
					assertTrue(b[3] == 0); // upper block cant be higher than middle block
				}
				if (b[3] >= dim) {
					assertTrue(b[4] >= 0);
				} else {
					assertTrue(b[4] == 0); // upper block cant be higher than middle block
				}
			}
		}
	}
	
	protected void log(String s) {
		System.out.println(s);
	}
	
}
