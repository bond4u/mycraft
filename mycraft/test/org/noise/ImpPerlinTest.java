package org.noise;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImpPerlinTest {

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
	public void test() {
		PermutationsTable tbl = new PermutationsTable(new Random());
		ImprovedPerlinNoise perlin = new ImprovedPerlinNoise(tbl);
		float s = 0.0f;
		float n = 100.0f;
		float m = -100.0f;
		long ndur = 0;
		for (int i = -500; i <= 500; i++) {
			for (int j = -500; j <= 500; j++) {
				float x = i / 50.0f;
				float y = j / 50.0f;
				float z = 0;
				long nstart = System.nanoTime();
				float p = perlin.get(x, y, z);
				long nend = System.nanoTime();
				ndur += (nend - nstart);
				assertTrue(p >= -1.0f);
				assertTrue(p <= 1.0f);
				n = Math.min(n, p);
				m = Math.max(m, p);
				s += p;
			}
		}
		float a = s / (1001.0f * 1001.0f);
		System.out.println("min: " + n + " max: " + m + " avg: " + a + " ndur: " + ndur);
	}

}
