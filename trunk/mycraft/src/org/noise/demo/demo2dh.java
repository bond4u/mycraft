package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import org.noise.Convert1D;
import org.noise.FBM3;
import org.noise.IConv1D;
import org.noise.ILimit1D;
import org.noise.PermutationsTable;
import org.noise.Threshold1D;
import org.noise.Turbulence;

@SuppressWarnings("serial")
public class demo2dh extends DemoBase {

	public demo2dh(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo2dh demo = new demo2dh("demo 2d g");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private Random rnd = new Random();
	
	private PermutationsTable tbl = new PermutationsTable(rnd);
	
	private FBM3 noise = new FBM3(tbl);
	
	private PermutationsTable tb2 = new PermutationsTable(rnd);
	
	private Turbulence turb = new Turbulence(tb2);
	
	private IConv1D conv = new Convert1D();
	
	private IConv1D con2 = new Convert1D();
	
	private ILimit1D top = new Threshold1D();	
	private ILimit1D bot = new Threshold1D();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (null == turb) {
			System.out.println("not initialized yet; not ready to paint");
			return;
		}
		
		int w = getWidth();
		int h = getHeight();
		
		noise.setOctaves(2);
		noise.setLacunarity(1.95f);
		noise.setGain(1.95f);
		
		turb.set(0.5f, 1f);
		
		conv.set(-1.0f, 1.0f, 0.0f, 1.0f); // [-1..+1]->[0..1]
		
		con2.set(0.0f, 2.5f, 0.0f, 1.0f); // [0..2.25]->[0..1]
		
		top.set(ILimit1D.Condition.ABOVE_OR_EQUAL, 1.0f, 1.0f);
		bot.set(ILimit1D.Condition.BELOW, 0.0f, 0.0f);

		float n = 100.0f;
		float m = -100.0f;
		float n2 = 100.0f;
		float m2 = -100.0f;
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				float x = i / 75.0f; // frequency=1/5=is sharp, like tv white noise
				float y = j / 75.0f; // 1/50 is smoother
				// get noise for point
				float a = noise.get(x, y, 0.0f);
				n = Math.min(n, a);
				m = Math.max(m, a);
				float t = turb.get(x, y, 0.0f);
				n2 = Math.min(n2, t);
				m2 = Math.max(m2, t);
				// convert to color
				float k = conv.get(a);
				if (k < 0f || 1f < k) {
			System.out.println("i: " + i + " j: " + j + " k: " + k);
				}
				// convert turbulence
				float o = con2.get(t);
				// turb below 0.15 is cave
				if (o < 0.15f) {
					k = 0.0f;
				}
				float l = bot.get(top.get(k));
				// paint the point at the original non-transformed location [i,j]
				Color c = new Color(l, l, l);
				g.setColor(c);
				g.drawLine(i, j, i, j);
			}
		}
		System.out.println("min: " + n + " max: " + m + " min2: " + n2 + " max2: " + m2);
	}

}
