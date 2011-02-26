package org.noise;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mycraft.client.Block;
import org.mycraft.client.Point2i;
import org.mycraft.client.Terrain;
import org.mycraft.client.TestTerrain;

public class BlockTest {

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
	public void testBlock() {
		Random r = new Random(653654654);
		Terrain t = new Terrain(r);
		IFunc2D f = new IFunc2D() {
			@Override
			public float get(float x, float y) {
				return 0;
			}
		};
		try {
			Block b = new Block(t, 0, 0, f) {
				protected int createVBO() {
					// nothing, dont do GL
					return 0;
				}
			};
		} catch (Throwable th) {
			fail("Block ctor must not throw");
		}
	}

//	@Test
//	public void testGetDim() {
//		Random r = new Random(653654654);
//		Terrain t = new Terrain(r);
//		IFunc2D f = new IFunc2D() {
//			@Override
//			public float get(float x, float y) {
//				return 0;
//			}
//		};
//		Block b = new Block(t, 0, 0, f);
//		short e = 15;
//		short s = Block.getDim();
//		assertEquals("dim must be 15", e, s);
//	}

//	@Test
//	public void testLowest() {
//	}
//
//	@Test
//	public void testHighest() {
//	}
//
//	@Test
//	public void testInitVBO() {
//	}
//
//	@Test
//	public void testDraw() {
//	}
//
//	@Test
//	public void testDestroy() {
//	}
//
//	@Test
//	public void testGetHeightAt() {
//	}
	
	@Test
	public void testCalcCenter() {
		// check block values -22..-8,-7..7,8..23
		short blkDim = Block.getDim();
//		short hd = (short) (blkDim / 2);
		for (int x = 2 * -blkDim; x < 2 * blkDim; x++) {
			Point2i c = Block.calcCenter(x, 0);
//			log("x=" + x + " c=" + c);
			if (x < -22) {
				assertTrue(c.getX() < -22);
			} else if (x < -7) {
				assertTrue(c.getX() < -7);
			} else if (x < 8) {
				assertTrue(c.getX() < 8);
			} else if (x < 23) {
				assertTrue(c.getX() < 23);
			}
		}
	}
	
	@Test
	public void testBlockBorder() {
		Random r = new Random(546543654);
		TestTerrain t = new TestTerrain(r);
		t.create();
		// check block values
		short blkDim = Block.getDim();
		short hd = (short) (Block.getDim() / 2);
		for (int x = 2 * -blkDim; x < blkDim; x++) {
			short h = t.getHeightAt(x, 0);
//			log("x=" + x + " h=" + h);
			if (x > -23 && x < 8) {
				assertTrue(h > 0);
			} else {
				assertTrue(h == 0);
			}
		}
	}
	
	protected void log(String s) {
		System.out.println(s);
	}
	
}
