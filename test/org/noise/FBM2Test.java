package org.noise;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FBM2Test {

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
	public void testFBM2() {
		Random r = new Random(789473253);
		try {
			FBM2 f = new FBM2(r);
		} catch (Throwable t) {
			fail("FBM2 ctor must not throw");
		}
	}

	@Test
	public void testGetFloatFloat() {
		Random r = new Random(838374747);
		FBM2 f = new FBM2(r);
		f.setOctaves(2);
		f.setLacunarity(1.75f);
		f.setGain(1f);
		int cnt = 50;
		float mult = 16f;
		String form = "%+.4f";
		for (int i = -cnt; i <= cnt; i++) {
			float x = i * 1f / mult;
			float hm1 = f.get(x, -1f / mult);
			float h0 = f.get(x, 0f);
			float h1 = f.get(x, 1f / mult);
			String sx = String.format(Locale.ENGLISH, form, x);
			String sm1 = String.format(Locale.ENGLISH, form, hm1 * mult);
			String s0 = String.format(Locale.ENGLISH, form, h0 * mult);
			String s1 = String.format(Locale.ENGLISH, form, h1 * mult);
			int dm1 = (int) ((h0 - hm1) / (1f / mult));
			int d1 = (int) ((h1 - h0) / (1f / mult));
			log("x=" + sx + "\t-1=" + sm1 + "\td=" + dm1
					+ "\t0=" + s0 + "\td=" + d1 + "\t+1=" + s1);
		}
	}
	
	protected void log(String s) {
		System.out.println(s);
	}
	
}
