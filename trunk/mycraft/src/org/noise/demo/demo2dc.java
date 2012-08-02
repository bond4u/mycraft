package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import org.noise.Convert1D;
import org.noise.FBM2;
import org.noise.IConv1D;
import org.noise.IFbm2D;
import org.noise.ILimit1D;
import org.noise.PermutationsTable;
import org.noise.Threshold1D;
import org.noise.ILimit1D.Condition;

@SuppressWarnings("serial")
public class demo2dc extends DemoBase {

	public demo2dc(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo2dc demo = new demo2dc("demo 2d c");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private PermutationsTable tbl = new PermutationsTable(new Random());
	
	private IFbm2D func = new FBM2(tbl);
	
	private IConv1D conv = new Convert1D();
	
	private ILimit1D top = new Threshold1D();
	
	private ILimit1D bot = new Threshold1D();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (null == func) {
			System.out.println("not initialized yet; not ready to paint");
			return;
		}
		
		int w = getWidth();
		int h = getHeight();
		
		func.setOctaves(5);
		func.setLacunarity(2f);
		func.setGain(0.85f);
		
		conv.set(-1.0f, +1.0f, 0f, 1f);

		top.set(Condition.ABOVE, 1f, 1f);
		bot.set(Condition.BELOW, 0, 0);
		
		float m = -1f;
		float n = 1f;
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				// calc value for point [i,j]
				float f = func.get(i/80f,  j/80f);
				m = Math.max(m, f);
				n = Math.min(n, f);
				// convert from range [-1..+1] to range [0..1f] (color)
				float k = conv.get(f);
				if (k < -1.0f || k >  1.0f) {
			System.out.println("i: " + j + " j: " + j + " f: " + f + " k: " + k);
				}
				// make sure color is in range
				k = bot.get(top.get(k));
				// paint the point at the original non-transformed location [i,j]
				Color c = new Color(k, k, k);
				g.setColor(c);
				g.drawLine(i, j, i, j);
			}
		}
		System.out.println("min: " + n + " max: " + m);
	}

}
