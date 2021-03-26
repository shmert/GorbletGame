package com.sambarnum.gorblet;

import java.util.HashSet;
import java.util.Set;

/**
 * @author sbarnum
 */
public class Solver {
	public static void main(String[] args) {
		new Solver().solve(Board.newInstance(), Color.White, new Move(), 0);
	}

	Set<Board> evaluatedPositions = new HashSet<>();

	public boolean solve(final Board board, Color whoseTurn, Move move, int indent) {
		final int[] winner = board.getWinner();
		if(winner != null) {
			System.out.println("Four-in-a-row from squares " + winner[0] + " to " + winner[3] + " wins for " + board.largestPieceAt(winner[0]).color + " in " + indent + " moves:\n" + board);
			return false;
			//throw new RuntimeException("Done!");
		}
		if ( indent > 10) {
			return true; // at the limit, try other things
		}
		//System.out.println("  ".repeat(indent) + move);
		// try placing new pieces from the stack
		for (int size = 0; size < 3; size++) {
			if (board.stackShowingOfSize(whoseTurn, size) != 0) {
				for (int dest = 0; dest < 16; dest++) {
					Board nextBoard = board.moveFromStackIfPossible(whoseTurn, size, dest);
					// optimize! should we not check for null, and just try adding it?
					if (nextBoard != null && evaluatedPositions.add(nextBoard)) {
						if (!solve(nextBoard, whoseTurn.opponent(), move.withMoveFromStack(whoseTurn, size, dest), indent+1)) {
							return true; // found a solution, try other moves further up
						}
					}
				}
			}
		}
		// try moving pieces already in play
		for (int from = 0; from < 16; from++) {
			final Piece piece = board.largestPieceAt(from);
			if (piece.color == whoseTurn) { // it's our own piece, let's try moving it
				for (int dest = 0; dest < 16; dest++) {
					Board nextBoard = board.movePieceIfPossible(piece, from, dest);
					// optimize! should we not check for null, and just try adding it?
					if (nextBoard != null && evaluatedPositions.add(nextBoard)) {
						if (!solve(nextBoard, whoseTurn.opponent(), move.withMoveInPlay(whoseTurn, from, dest), indent+1)) {
							return true; // found a solution, try other moves further up
						}
					}
				}
			}
		}
		return true;
	}

}
