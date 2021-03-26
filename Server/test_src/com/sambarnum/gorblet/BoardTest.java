package com.sambarnum.gorblet;

import junit.framework.TestCase;

import static com.sambarnum.gorblet.Board.stackIndex;
import static com.sambarnum.gorblet.Color.Black;
import static com.sambarnum.gorblet.Color.White;

/**
 * 0  1  2  3
 * 4  5  6  7
 * 8  9  10 11
 * 12 13 14 15
 *
 * @author sbarnum
 */
@SuppressWarnings("ConstantConditions")
public class BoardTest extends TestCase {
	public void testNewBoard() throws Exception {
		final Board board = Board.newInstance();
		for (int i = 0; i < 16; i++) {
			assertNull(board.largestPieceAt(i).color);
		}
		assertEquals(3, board.stackShowingOfSize(White, 0));
		assertEquals(0, board.stackShowingOfSize(White, 1));
		assertEquals(0, board.stackShowingOfSize(White, 2));
		assertEquals(0, board.stackShowingOfSize(White, 3));
		assertEquals(3, board.stackShowingOfSize(Black, 0));
		assertEquals(0, board.stackShowingOfSize(Black, 1));
		assertEquals(0, board.stackShowingOfSize(Black, 2));
		assertEquals(0, board.stackShowingOfSize(Black, 3));
		assertNull(board.getWinner());
	}

	public void testMoves() throws Exception {
		Board board = Board.newInstance();
		assertEquals(
				"++++\n" +
						"++++\n" +
						"++++\n" +
						"++++\n" +
						"➊➊➊ ➀➀➀", board.toString());
		board = board.moveFromStackIfPossible(White, 0, 5);
		{
			assertBoardIsValid(board);
			assertEquals(2, board.stackShowingOfSize(White, 0));
			assertEquals(1, board.stackShowingOfSize(White, 1));
			assertEquals(0, board.stackShowingOfSize(White, 2));
			assertEquals(0, board.stackShowingOfSize(White, 3));
			assertEquals(3, board.stackShowingOfSize(Black, 0));
			assertEquals(0, board.stackShowingOfSize(Black, 1));
			assertEquals(0, board.stackShowingOfSize(Black, 2));
			assertEquals(0, board.stackShowingOfSize(Black, 3));
		}
		assertNull("Cannot move 0-size atop 0-size", board.moveFromStackIfPossible(Black, 0, 5));
		board = board.moveFromStackIfPossible(Black, 0, 6);
		{
			assertBoardIsValid(board);
			assertEquals(2, board.stackShowingOfSize(White, 0));
			assertEquals(1, board.stackShowingOfSize(White, 1));
			assertEquals(0, board.stackShowingOfSize(White, 2));
			assertEquals(0, board.stackShowingOfSize(White, 3));
			assertEquals(2, board.stackShowingOfSize(Black, 0));
			assertEquals(1, board.stackShowingOfSize(Black, 1));
			assertEquals(0, board.stackShowingOfSize(Black, 2));
			assertEquals(0, board.stackShowingOfSize(Black, 3));
		}

		board = board.moveFromStackIfPossible(White, 0, 9);
		assertBoardIsValid(board);

		board = board.moveFromStackIfPossible(Black, 0, 10);
		assertBoardIsValid(board);

		board = board.moveFromStackIfPossible(White, 1, 13);// three-in-a-row
		assertBoardIsValid(board);

		assertNotNull("allowed to capture 1-size with 0-size since white has three in a row", board = board.moveFromStackIfPossible(Black, 0, 13));
		assertBoardIsValid(board);

		board = board.moveFromStackIfPossible(White, 1, 1); // three-in-a-row again
		assertBoardIsValid(board);

		Board victory = board.movePieceIfPossible(board.largestPieceAt(13), 13, 14);
		assertNotNull(victory);
		assertNotNull(victory.getWinner());
	}

	public void testCanonical() throws Exception {
		Board board = Board.newInstance();
		Board board1 = board.moveFromStackIfPossible(White, 0, 0);
		Board board1_1 = board1.moveFromStackIfPossible(Black, 0, 1);
		Board board1_2 = board1.moveFromStackIfPossible(Black, 0, 4);
		assertEquals("These should be canonically equal", board1_1, board1_2);

	}

	public void testBoardShowing() throws Exception {
		final Board board = Board.newInstance();
		board.data[stackIndex(White)] = (byte) 0b11111101; // white is missing two 0-size pieces
		assertEquals(1, board.stackShowingOfSize(White, 0));
		assertEquals(2, board.stackShowingOfSize(White, 1));
		assertEquals(0, board.stackShowingOfSize(White, 2));
		assertEquals(0, board.stackShowingOfSize(White, 3));
	}

	private void assertBoardIsValid(final Board board) {
		assertNotNull("Board is null", board);
		assertNull(board.getWinner());
		for (Color c : Color.values()) {
			final int sumShowing = board.stackShowingOfSize(c, 0) + board.stackShowingOfSize(c, 1) + board.stackShowingOfSize(c, 2) + board.stackShowingOfSize(c, 3);
			assertTrue(c + " stack count of " + sumShowing + " should not exceed three in \n" + board, sumShowing <= 3);
		}
	}
}
