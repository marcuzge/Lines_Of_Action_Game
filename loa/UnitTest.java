
package loa;

import static loa.Piece.*;
import static loa.Board.*;
import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;


/** The suite of all JUnit tests for the loa package.
 *  @author Zixuan Ge
 */
public class UnitTest {

    /**
     * Run the JUnit tests in the loa package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /**
     * Test get method.
     */
    @Test
    public void testGet() {
        Board board = new Board();
        assertTrue("f7 = BP", board.get(7, 1) == Piece.BP);
        assertTrue("a5 = WP", board.get(1, 5) == Piece.WP);
        assertTrue("d5 = EMP", board.get(4, 5) == Piece.EMP);
        assertTrue("h6 = WP", board.get(8, 6) == Piece.WP);
    }

    /**
     * Test for isLegal method.
     */
    @Test
    public void testIsLegal() {
        Board board = new Board();
        Move test1Invalid = Move.create(9, 1, 10, 1, board);
        Move test2Invalid = Move.create(1, 10, 3, 10, board);
        assertEquals(false, board.isLegal(test1Invalid));
        assertEquals(false, board.isLegal(test2Invalid));
    }

    /**
     * Test for set method.
     */
    @Test
    public void testSet() {
        Board board = new Board();
        board.set(4, 4, BP);
        board.set(5, 5, WP);
        assertEquals(BP, board.get(4, 4));
        assertEquals(WP, board.get(5, 5));
    }

    /**
     * Test for pieceCountAlong method.
     */
    @Test
    public void testpieceCountAlong() {
        Board board = new Board();
        Move move1 = Move.create(5, 1, 5, 3, board);
        assertEquals(board.pieceCountAlong(move1), 2);
        Move move2 = Move.create(5, 1, 7, 3, board);
        assertEquals(board.pieceCountAlong(move2), 2);
        Move move5 = Move.create(3, 1, 5, 3, board);
        assertEquals(2, (board.pieceCountAlong(move5)));
    }

    /**
     * Test for blocked method.
     */
    @Test
    public void testBlocked() {
        Piece[][] testBlocked = {
                {EMP, BP, BP, BP, BP, BP, BP, EMP},
                {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
                {WP, EMP, EMP, EMP, EMP, BP, EMP, WP},
                {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
                {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
                {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
                {WP, WP, EMP, EMP, EMP, EMP, EMP, WP},
                {EMP, BP, BP, BP, BP, BP, BP, EMP}
        };
        Board board = new Board(testBlocked, BP);
        Move test1 = Move.create(2, 8, 2, 6, board);
        assertEquals(true, board.blocked(test1));
    }

    /**
     * Test for piecesContiguous method.
     */
    @Test
    public void testPiecesContiguous() {
        Piece[][] testBlack = {
                {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
                {EMP, WP, WP, BP, BP, EMP, EMP, EMP},
                {EMP, WP, BP, BP, EMP, WP, EMP, WP},
                {EMP, WP, BP, WP, WP, EMP, EMP, EMP},
                {EMP, EMP, WP, BP, BP, WP, EMP, EMP},
                {EMP, EMP, EMP, EMP, BP, BP, EMP, EMP},
                {EMP, EMP, EMP, EMP, BP, EMP, EMP, EMP},
                {EMP, EMP, WP, EMP, EMP, EMP, WP, EMP},
        };
        Board board = new Board(testBlack, BP);
        assertEquals(true, board.piecesContiguous(BP));
        Board board2 = new Board(testBlack, WP);
        assertEquals(false, board2.piecesContiguous(WP));
    }
}






