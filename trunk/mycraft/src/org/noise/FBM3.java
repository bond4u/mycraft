package org.noise;

public class FBM3 extends ImprovedPerlinNoise implements IFbm3D {
	
	private int oct;
	private float lac;
	private float gain;
	
	public FBM3(PermutationsTable pTbl) {
		super(pTbl);
	}
	
	public void setOctaves(int o) {
		this.oct = o;
	}
	
	public void setLacunarity(float l) {
		this.lac = l;
	}
	
	public void setGain(float g) {
		this.gain = g;
	}
	
	@Override
	public float get(float x, float y, float z) {
		float freq = 1.0f;
		float ampl = 0.5f;
		float sum = 0.0f;

		for (int i = 0; i < oct; i++) {
			sum += super.get(x * freq, y * freq, z * freq) * ampl;
			freq *= lac;
			ampl *= gain;
		}
		return sum;
	}
	
	protected void log(String s) {
		System.out.println(s);
	}
	
}
