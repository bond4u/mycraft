package org.noise;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.noise.ILimit1D.Condition;

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
			t.set(Condition.ABOVE, 0.5f, 1f);
		} catch (Throwable t) {
			fail("set(above) must not throw");
		}
	}

	@Test
	public void testSetBelow() {
		try {
			Threshold1D t = new Threshold1D();
			t.set(Condition.BELOW, 0.5f, 0f);
		} catch (Throwable t) {
			fail("set(below) must not throw");
		}
	}

	@Test
	public void testGet() {
		Threshold1D t = new Threshold1D();
		float e0 = 0f;
		float r0 = t.get(0f);
		assertTrue("must equal 0f == 0f", e0 == r0);
		t.set(Condition.BELOW, 0f, -1f);
		float e1 = -1f;
		float r1 = t.get(-0.25f);
		assertTrue("must equal 1f == -0.25f", e1 == r1);
		t.set(Condition.ABOVE, 0f, 1f);
		float e2 = 1f;
		float r2 = t.get(0.25f);
		assertTrue("must equal 1f == 0.25f", e2 == r2);
	}

}
