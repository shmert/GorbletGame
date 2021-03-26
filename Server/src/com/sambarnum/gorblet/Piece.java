package com.sambarnum.gorblet;

import static com.sambarnum.gorblet.Color.Black;
import static com.sambarnum.gorblet.Color.White;

/**
 * 0 is the largest, 3 is the smallest.
 *
 * @author sbarnum
 */
public enum Piece {

	WHITE_0(White, 0),
	BLACK_0(Black, 0),
	WHITE_1(White, 1),
	BLACK_1(Black, 1),
	WHITE_2(White, 2),
	BLACK_2(Black, 2),
	WHITE_3(White, 3),
	BLACK_3(Black, 3),
	NO_PIECE(null, Integer.MAX_VALUE);

	private static final String[] ICONS = new String[]{
			"➊",
			"➀",
			"➋",
			"➁",
			"➌",
			"➂",
			"➍",
			"➃",
			"✛"};

	public final int size;
	public final Color color;

	Piece(Color color, int size) {
		this.size = size;
		this.color = color;
	}

	public static Piece withColorAndSize(final Color c, final int size) {
		for (Piece eachPiece : values()) {
			if (eachPiece.color == c && eachPiece.size == size) {
				return eachPiece;
			}
		}
		return NO_PIECE;
	}

	public boolean isIn(final byte square) {
		int bit = 1 << (size * 2 + color.ordinal());
		return (square & bit) != 0;
	}

	public boolean canBeCapturedBy(final int capturingSize) {
		return this.size > capturingSize;
	}

	public String toChessPiece() {
		return ICONS[ordinal()];
	}

	public String getSizeName() {
		switch (size) {
			case 0:
				return "XL";
			case 1:
				return "L";
			case 2:
				return "M";
			case 3:
				return "S";
			default:
				return "?";
		}
	}
}
