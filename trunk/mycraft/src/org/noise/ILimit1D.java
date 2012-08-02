package org.noise;

public interface ILimit1D extends IFunc1D {
	
	enum Condition {
		ABOVE,
		ABOVE_OR_EQUAL,
		BELOW,
		BELOW_OR_EQUAL;
	}
	
	void set(Condition c1, float x1, float x2);
	
	float get(float x);
	
}
