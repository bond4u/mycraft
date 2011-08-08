package org.noise;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Convert1DTest {

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
	public void testSetStart() {
		try {
			Convert1D c = new Convert1D();
			c.setFrom(0f, 1f);
		} catch (Throwable t) {
			fail("setStart() must not throw");
		}
	}

	@Test
	public void testSetEnd() {
		try {
			Convert1D c = new Convert1D();
			c.setTo(-1f, 1f);
		} catch (Throwable t) {
			fail("setEnd() must not throw");
		}
	}

	@Test
	public void testGet() {
		Convert1D c = new Convert1D();
		c.setFrom(0f, 1f);
		c.setTo(-1f, 1f);
		float e0 = -1f;
		float r0 = c.get(-0.25f);
		assertTrue("must convert -0.25f -> -1f", e0 == r0);
		float e1 = -1f;
		float r1 = c.get(0f);
		assertTrue("must convert 0f -> -1f", e1 == r1);
		float e2 = -0.5f;
		float r2 = c.get(0.25f);
		assertTrue("must convert 0.25f -> -0.5f", e2 == r2);
		float e3 = 0f;
		float r3 = c.get(0.5f);
		assertTrue("must convert 0.5f -> 0f", e3 == r3);
		float e4 = 0.5f;
		float r4 = c.get(0.75f);
		assertTrue("must convert 0.75f -> 0.5f", e4 == r4);
		float e5 = 1f;
		float r5 = c.get(1f);
		assertTrue("must convert 1f -> 1f", e5 == r5);
		float e6 = 1f;
		float r6 = c.get(1.25f);
		assertTrue("must convert 1.25f -> 1f", e6 == r6);
	}
	
}
