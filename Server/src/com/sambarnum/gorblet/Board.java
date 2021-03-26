package com.sambarnum.gorblet;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.sambarnum.gorblet.Color.Black;
import static com.sambarnum.gorblet.Color.White;
import static com.sambarnum.gorblet.Piece.NO_PIECE;

/**
 * There are 16 squares.
 * A square on the board is a single byte with each bit representing a piece of a specific size and color.
 * 00_00_00_00 is an empty square
 * 00_00_00_01 is a 0-size (largest) white piece
 * 00_00_00_10 is a 0-size (largest) black piece
 * 00_00_00_11 is invalid, since you cannot have two pieces of the same size on a square
 * 10_01_10_01 is has white pieces of 0- and 2-size, black pieces of 1- and 3-size
 * <p>
 * There are two stacks.
 * Each player's unplayed stacks are represented by a single byte representing the count (0-3) of each size (0-3)
 * A stack is grouped into two-bit sections representing the number of 3-, 2-, 1-, or 0-size pieces in the stack.
 * 11_11_11_11 is a full stack
 * 11_11_11_10 has had a 0-size piece removed
 * 11_11_10_10 has had a 0-size and 1-size piece removed
 * 10_10_10_10 has had one piece of each size removed
 * 11_11_10_01 has had two 0-size and 1 1-size piece removed
 * 11_11_10_11 is invalid, as you may not remove a 1-size piece while all 0-size pieces are still there
 *
 * @author sbarnum
 */
public final class Board {
	private static final Piece[] ALL_PIECES = Piece.values(); // so we only need to call values() one time
	private static int[][] WINNABLE_GROUPS = {
			{
					0,
					1,
					2,
					3},
			{
					4,
					5,
					6,
					7},
			{
					8,
					9,
					10,
					11},
			{
					12,
					13,
					14,
					15},
			// row wins
			{
					0,
					4,
					8,
					12},
			{
					1,
					5,
					9,
					13},
			{
					2,
					6,
					10,
					14},
			{
					3,
					7,
					11,
					15},
			// column wins
			{
					1,
					5,
					10,
					15},
			{
					3,
					6,
					9,
					12}
			// diagonal wins
	};

	byte[] data;

	public static Board newInstance() {
		byte[] tmpData = new byte[18];
		tmpData[Board.stackIndex(White)] = (byte) 0b11111111;
		tmpData[Board.stackIndex(Black)] = (byte) 0b11111111;
		return new Board(tmpData);
	}

	private static Board canonicalVersionOf(final byte[] newData) {
		final byte[] flipped = new byte[newData.length];
		System.arraycopy(newData, 0, flipped, 0, newData.length);
		Stream.of(new Board(newData))
		return new Board(newData); // FIX!!! rotate and permutate and stuff, then take the minimum
	}

	private Board(final byte[] data) {
		this.data = data;
	}

	public Piece largestPieceAt(int index) {
		final byte square = data[index];
		if (square == 0) { // optimization
			return NO_PIECE;
		}
		for (Piece eachPiece : ALL_PIECES) {
			if (eachPiece.isIn(square)) {
				return eachPiece;
			}
		}
		return NO_PIECE;
	}

	/**
	 * @return The number of `size`-size pieces showing on the stack belonging to `whoseTurn`, where "showing" means "not obscured by a larger piece"
	 */
	public int stackShowingOfSize(final Color whoseTurn, final int size) {
		int stack = data[stackIndex(whoseTurn)];
		int obscuring = size == 0 ? 0 : (stack >>> ((size-1) * 2)) & 0b11;
		int inStack = (stack >>> size * 2) & 0b11;
		return Math.max(0, inStack - obscuring);
	}

	public Board moveFromStackIfPossible(final Color whoseTurn, final int size, final int dest) {
		assert stackShowingOfSize(whoseTurn, size) > 0;
		Piece captured = largestPieceAt(dest);
		if (!captured.canBeCapturedBy(size)) { // cannot move to a square with a larger/equal piece there
			return null;
		}
		if (captured.color != null) {
			boolean isValid = false;
			// you can only capture from the stack if you are blocking a three-in-a-row
			for (int[] eachWinnable : WINNABLE_GROUPS) {
				boolean winnableIsDestination = false;
				for (int square : eachWinnable) {
					if (dest == square) {
						// the destination is in this winnable group
						// does the winnable group show three of the opponent's pieces?
						winnableIsDestination = true;
					}
				}
				if (winnableIsDestination) {
					int opponentCount = 0;
					for (int square : eachWinnable) {
						if (largestPieceAt(square).color == whoseTurn.opponent()) {
							opponentCount++;
						}
					}
					if (opponentCount == 3) {
						isValid = true;
					}
				}
			}
			if (!isValid) {
				return null;
			}
		}
		// looks like a valid move. Create a new Board array with a decremented stack and a modified square
		final byte[] newData = Arrays.copyOf(data, data.length);

		{
			// take the piece off the stack
			final int newCount = stackShowingOfSize(whoseTurn, size) - 1;
			assert newCount >= 0;
			final int stackIndex = stackIndex(whoseTurn);
			byte tmpStack = newData[stackIndex];
			// decrement the two-bit section representing `size`
			int delta = 1 << (size * 2);
			tmpStack -= delta;
			// write the stack back to the array
			newData[stackIndex] = tmpStack;
		}

		{
			// now add the piece to the board
			int pieceBit = 1 << (size * 2 + whoseTurn.ordinal());
			newData[dest] |= pieceBit;
		}
		return Board.canonicalVersionOf(newData);
	}

	public Board movePieceIfPossible(final Piece moving, final int from, final int dest) {
		final Piece captured = largestPieceAt(dest);
		if (!captured.canBeCapturedBy(moving.size)) {
			return null;
		}
		byte[] newData = Arrays.copyOf(data, data.length);
		int pieceBit = 1 << (moving.size * 2 + moving.color.ordinal());
		newData[from] &= ~pieceBit; // remove the piece from the old square
		newData[dest] |= pieceBit; // add the piece to the new square
		return Board.canonicalVersionOf(newData);

	}

	public static int stackIndex(final Color whoseTurn) {
		return 16 + whoseTurn.ordinal();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Board board = (Board) o;
		return Arrays.equals(data, board.data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			final Piece piece = largestPieceAt(i);
			sb.append(piece.toChessPiece());
			if (((i+1) % 4) == 0) {
				sb.append("\n");
			}
		}
		// append the unplayed stacks
		for (Color c : Color.values()) {
			for (int size = 0; size < 3; size++) {
				sb.append(Piece.withColorAndSize(c, size)
						.toChessPiece()
						.repeat(stackShowingOfSize(c, size)));
			}
			sb.append(" ");
		}
		return sb.toString().trim();
	}

	public int[] getWinner() {
		for (int[] eachWinnable : WINNABLE_GROUPS) {
			Piece piece = largestPieceAt(eachWinnable[0]);
			if (piece.color == null) {
				continue;
			}
			for (int j = 1; true; j++) {
				if (largestPieceAt(eachWinnable[j]).color != piece.color) {
					break;
				}
				if (j == 3) {
					return eachWinnable;
				}
			}
		}
		return null;
	}

}
