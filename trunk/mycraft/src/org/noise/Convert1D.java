package org.noise;

/**
 * Converts [0..+1] range to [-1..+1] range.
 */
public class Convert1D implements IConv1D {
	
	private float fromStart;
	private float fromEnd;
	private float toStart;
	private float toEnd;
	
	@Override
	public void set(float fx1, float fx2, float tx1, float tx2) {
		fromStart = fx1;
		fromEnd = fx2;
		toStart = tx1;
		toEnd = tx2;
	}
	
	@Override
	public float get(float x) {
		float x2 = x;
		if ((x >= fromStart && x <= fromEnd)) {
			float fl = fromEnd - fromStart;
			float fd = x - fromStart;
			float p = fd / fl;
			float tl = toEnd - toStart;
			float td = p * tl;
			x2 = toStart + td;
//			log("converted " + x + " [" + fromStart + ".." + toStart +
//					"] to " + x2 + " [" + toStart + ".." + toEnd + "]");
		}
		return x2;
	}

	protected void log(String s) {
		System.out.println(s);
	}
}
