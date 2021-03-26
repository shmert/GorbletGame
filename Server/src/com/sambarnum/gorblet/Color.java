package com.sambarnum.gorblet;

/**
 * @author sbarnum
 */
public enum Color {
	White, Black;

	public Color opponent() {
		if ( this == Black) {
			return White;
		} else {
			return Black;
		}
	}
}
