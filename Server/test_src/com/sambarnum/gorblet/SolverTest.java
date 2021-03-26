package com.sambarnum.gorblet;

import junit.framework.TestCase;

import java.util.List;
import java.util.stream.Collectors;

import static com.sambarnum.gorblet.Color.Black;
import static com.sambarnum.gorblet.Color.White;

/**
 * @author sbarnum
 */
public class SolverTest extends TestCase {
	public void testNearlyDone() throws Exception {
		final Move move = new Move()
				.withMoveFromStack(White, 0, 0)
				.withMoveFromStack(Black, 0, 1)
				.withMoveFromStack(White, 1, 4)
				.withMoveFromStack(Black, 0, 2)
				.withMoveFromStack(White, 2, 15)
				.withMoveFromStack(Black, 0, 3)
				.withMoveFromStack(White, 3, 5)
				.withMoveFromStack(Black, 1, 7);
		List<Move> moveList = move.stream().collect(Collectors.toList());
		assertEquals(8, moveList.size());
		Board board = move.setupNewBoardWithMoves();
		System.out.println("Starting position\n" + board.toString());
		new Solver().solve(board, White, move, 0);


	}

}
