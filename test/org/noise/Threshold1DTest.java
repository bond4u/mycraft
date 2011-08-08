package org.noise;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Threshold1DTest {

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
	public void testSetAbove() {
		try {
			Threshold1D t = new Threshold1D();
			t.setAbove(0f, 1f);
		} catch (Throwable t) {
			fail("setAbove() must not throw");
		}
	}

	@Test
	public void testSetBelow() {
		try {
			Threshold1D t = new Threshold1D();
			t.setBelow(0f, 1f);
		} catch (Throwable t) {
			fail("setBelow() must not throw");
		}
	}

	@Test
	public void testGet() {
		Threshold1D t = new Threshold1D();
		t.setBelow(0f, -1f);
		t.setAbove(0f, 1f);
		float e0 = 0f;
		float r0 = t.get(0f);
		assertTrue("must equal 0f == 0f", e0 == r0);
		float e1 = -1f;
		float r1 = t.get(-0.25f);
		assertTrue("must equal 1f == -0.25f", e1 == r1);
		float e2 = 1f;
		float r2 = t.get(0.25f);
		assertTrue("must equal 1f == 0.25f", e2 == r2);
	}

}
