package org.noise;

public class Mult implements IMult {
	
	private float pow;
	
	@Override
	public float get(float x) {
		return x * pow;
	}

	@Override
	public void setPower(float p) {
		pow = p;
	}

}
