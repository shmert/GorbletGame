package com.sambarnum.gorblet;

import java.util.stream.Stream;

import static com.sambarnum.gorblet.Color.White;

/**
 * @author sbarnum
 */
public class Move {
	final Move parent;
	final Color whoseTurn;
	/**
	 * Negative values store a value of `(-1 - size-from-stack)`, positive values store a square index.
	 */
	final byte fromOrStack;
	/**
	 * Square the piece was moved to
	 */
	final byte dest;

	public Move() {
		this(null, null, (byte) 0, (byte) 0);
	}

	private Move(final Move parent, final Color whoseTurn, final byte fromOrStack, final byte dest) {
		this.parent = parent;
		this.whoseTurn = whoseTurn;
		this.fromOrStack = fromOrStack;
		this.dest = dest;
	}

	public Move withMoveFromStack(final Color whoseTurn, final int size, final int dest) {
		return new Move(this, whoseTurn, (byte) (-1 - size), (byte) dest);
	}

	public Move withMoveInPlay(final Color whoseTurn, final int from, final int dest) {
		return new Move(this, whoseTurn, (byte) from, (byte) dest);
	}

	private int getSizeFromStack() {
		return -1 - fromOrStack;
	}

	@Override
	public String toString() {
		if (fromOrStack < 0) {
			return whoseTurn + " add " + Piece.withColorAndSize(White, getSizeFromStack()).getSizeName() + " to " + dest;
		} else {
			return whoseTurn + " from " + fromOrStack + " to " + dest;
		}
	}

	public Stream<Move> stream() {
		if (parent == null || parent.whoseTurn == null) {
			return Stream.of(this);
		}
		return Stream.concat(parent.stream(), Stream.of(this));
	}

	/**
	 * Creates a new Board and applies all this Move's ancestors and itself to the board, returning the finished board.
	 */
	public Board setupNewBoardWithMoves() {
		return stream().reduce(
				Board.newInstance(),
				(board, move) -> {
					if (move.fromOrStack < 0) {
						return board.moveFromStackIfPossible(move.whoseTurn, getSizeFromStack(), move.dest);
					} else {
						return board.movePieceIfPossible(board.largestPieceAt(move.fromOrStack), move.fromOrStack, move.dest);
					}
				},
				(board, board2) -> board);
	}

}
