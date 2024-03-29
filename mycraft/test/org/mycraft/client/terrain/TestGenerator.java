package org.mycraft.client.terrain;

import java.util.Random;

import org.mycraft.client.terrain.Generator;
import org.noise.IFunc2D;

public class TestGenerator extends Generator {
	
//	private short mult;
	
	public TestGenerator(Random r) {
		super(r);
	}
	
	protected short getMult() {
		return 1;
	}
	
//	protected IMult createPlaneMult() {
//		mult = 1;
//		return super.createPlaneMult();
//	}
	
	// override heightmap generation
	protected IFunc2D createHeightMap2D(Random r) {
		IFunc2D f = new IFunc2D() {
			@Override
			public float get(float x, float y) {
				float h = (x + 5 + y * 5) % 5;
				while (h < 0f) {
					h += 5f;
				}
//				if (y == 0f) {
//					log("x=" + x + " y=" + y + " gen=" + (h+1));
//				}
				return h + 1;
			}
		};
		return f;
	}
	
//	protected IMult createHeightMult() {
//		mult = 5;
//		return super.createHeightMult();
//	}
	
	protected void log(String s) {
		System.out.println(s);
	}
	
}
