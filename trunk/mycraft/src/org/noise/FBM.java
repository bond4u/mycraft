package org.noise;

import java.util.Random;

public class FBM implements IFbm2D {
	
	private int[] ms_p = new int[512];
	
	private int oct;
	private float lac;
	private float gain;
	
	public FBM(Random s) {
		int nbVals = (1 << 8);
		int[] ms_perm = new int[nbVals];
		for (int i = 0; i < nbVals; i++) {
			ms_perm[i] = -1;
		}
		for (int i = 0; i < nbVals; i++) {
			// for each value, find an empty spot, and place it in it
			while (true) {
				// generate rand # with max a nbvals
				int p = s.nextInt(256);
				if (ms_perm[p] == -1) {
					ms_perm[p] = i;
					break;
				}
			}
		}
		for (int i = 0; i < nbVals; i++) {
			ms_p[nbVals + i] = ms_p[i] = ms_perm[i];
		} 
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
	public float get(float x, float y) {
		//fBm(double x, double y, double z, int octaves, float lacunarity, float gain)
		float freq = 1.0f;
		float ampl = 0.5f;
		float sum = 0.0f;

		for (int i = 0; i < oct; i++) {
			sum += noise(x * freq, y * freq/*, z * freq*/) * ampl;
			freq *= lac;
			ampl *= gain;
		}
		return sum;
	}
	
	protected float noise(float x, float y) {
		//noise (double x, double y, double z)
		int z = 0; // no 3rd dim
		int X = (int)x & 255;
		int Y = (int)y & 255;
		int Z = (int)z & 255;
		x -= Math.floor(x);  
		y -= Math.floor(y);  
		z -= Math.floor(z);  
		float u = fade(x);
		float v = fade(y);
		float w = fade(z);
		int A = ms_p[X] + Y;
		int AA = ms_p[A] + Z;
		int AB = ms_p[A + 1] + Z;
		int B = ms_p[X + 1] + Y;
		int BA = ms_p[B] + Z;
		int BB = ms_p[B + 1] + Z;
		float gAA = grad(ms_p[AA], x, y, z);
		float gAA1 = grad(ms_p[AA+1], x, y, z-1);
		float gBA = grad(ms_p[BA], x-1, y, z);
		float gBA1 = grad(ms_p[BA+1], x-1, y, z-1);
		float gAB = grad(ms_p[AB], x, y-1, z);
		float gAB1 = grad(ms_p[AB+1], x, y-1, z-1);
		float gBB = grad(ms_p[BB], x-1, y-1, z);
		float gBB1 = grad(ms_p[BB+1], x-1, y-1, z-1);
		float luAABA = lerp(u, gAA, gBA);
		float luABBB = lerp(u, gAB, gBB);
		float luAA1BA1 = lerp(u, gAA1, gBA1);
		float luAB1BB1 = lerp(u, gAB1, gBB1);
		float lvAB = lerp(v, luAABA, luABBB);
		float lvAB1 = lerp(v, luAA1BA1, luAB1BB1);
		return lerp(w, lvAB, lvAB1);
	}
	
	protected float fade(float t) {
		return (t * t * t * (t * (t * 6 - 15) + 10));
	}
	
	protected float lerp(float t, float a, float b) {
		return (a + t * (b - a));
    }
	
	protected float grad(int hash, float x, float y, float z) {
		int h = hash & 15;
		float u = h<8 ? x : y;
		float v = h<4 ? y : h==12||h==14 ? x : z;
		return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
	}
	
}
