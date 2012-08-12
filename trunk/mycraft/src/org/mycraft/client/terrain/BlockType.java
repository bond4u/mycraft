package org.mycraft.client.terrain;

public enum BlockType {

	Air(),
	Ground();
	
//	private int type;
	
	BlockType(/*int type*/) {
//		this.type = type;
	}
	
	public boolean isDense() {
		return (this == Ground);
	}
}
