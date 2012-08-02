package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import org.noise.Convert1D;
import org.noise.FBM2;
import org.noise.IConv1D;
import org.noise.IFbm2D;
import org.noise.IFunc2D;
import org.noise.ILimit1D;
import org.noise.PermutationsTable;
import org.noise.Threshold1D;
import org.noise.ILimit1D.Condition;

@SuppressWarnings("serial")
public class demo2de extends DemoBase {

	public demo2de(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo2de demo = new demo2de("demo 2d e");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private PermutationsTable tbl = new PermutationsTable(new Random());
	
	private IFbm2D dist = new FBM2(tbl);
	
	private ILimit1D top = new Threshold1D();	
	private ILimit1D bot = new Threshold1D();

	private IConv1D conv = new Convert1D();
	
	private ILimit1D tp2 = new Threshold1D();
	private ILimit1D bt2 = new Threshold1D();
	
	private IFunc2D grad = new Grad();
	
	private ILimit1D tp3 = new Threshold1D();
	private ILimit1D bt3 = new Threshold1D();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (null == dist) {
			System.out.println("not initialized yet; not ready to paint");
			return;
		}
		
		int w = getWidth();
		int h = getHeight();
		
		dist.setOctaves(6);
		dist.setLacunarity(2f);
		dist.setGain(0.85f);
		
		top.set(Condition.ABOVE, 1f, 1f);
		bot.set(Condition.BELOW, -1f, -1f);

		conv.set(-1.0f, +1.0f, -0.25f, 0.25f);
		
		tp2.set(Condition.ABOVE, 0.25f, 0.25f);
		bt2.set(Condition.BELOW, -0.25f, -0.25f);

		tp3.set(Condition.ABOVE_OR_EQUAL, 0.5f, 1f);
		bt3.set(Condition.BELOW, 0.5f, 0f);
		
		for (int i = 0; i < w; i++) {
			// get disturbance for point [i,0]
			float d = dist.get(i/80f, 0f);
			if (d < -1f || 1f < d) {
		System.out.println("i: " + i + " d: " + d);
			}
			// make sure disturbance is in range
			float e = bot.get(top.get(d));
			// convert from range [-1..+1] to range [-0.25..+0.25f]
			float f = conv.get(d);
			if (f < -0.25f || 0.25f < f) {
		System.out.println("i: " + " d: " + d + " e: " + e + " f: " + f);
			}
			float b = bt2.get(tp2.get(f));
			for (int j = 0; j < h; j++) {
				// disturb the point
				float y = j + (h * b);
				// get the gradient at disturbed point [i,y]
				float a = grad.get(i, y);
				// convert to color
				float k = 1f - (a / h);
				if (k < 0f || 1f < k) {
			System.out.println("i: " + i + " j: " + j + " k: " + k);
				}
				float l = bt3.get(tp3.get(k));
				// paint the point at the original non-transformed location [i,j]
				Color c = new Color(l, l, l);
				g.setColor(c);
				g.drawLine(i, j, i, j);
			}
		}
	}

}
