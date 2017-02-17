package loa;

import java.util.*;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Direction.*;

/** Represents the state of a game of Lines of Action.
 *  @author Zixuan Ge
 */
class Board implements Iterable<Move> {

    /**
     * Size of a board.
     */
    static final int M = 8;

    /**
     * Pattern describing a valid square designator (cr).
     */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /**
     * A Board whose initial contents are taken from INITIALCONTENTS
     * and in which the player playing TURN is to move. The resulting
     * Board has
     * get(col, row) == INITIALCONTENTS[row-1][col-1]
     * Assumes that PLAYER is not null and INITIALCONTENTS is MxM.
     * <p>
     * CAUTION: The natural written notation for arrays initializers puts
     * the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /**
     * A new board in the standard initial position.
     */
    Board() {
        clear();
    }

    /**
     * A Board whose initial contents and state are copied from
     * BOARD.
     */
    Board(Board board) {
        copyFrom(board);
    }

    /**
     * Set my state to CONTENTS with SIDE to move.
     */
    void initialize(Piece[][] contents, Piece side) {
        _moves.clear();

        _pieces = new Piece[M][M];
        for (int r = 1; r <= M; r += 1) {
            for (int c = 1; c <= M; c += 1) {
                set(c, r, contents[r - 1][c - 1]);
            }
        }
        _turn = side;
    }

    /**
     * Set me to the initial configuration.
     */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /**
     * Set my state to a copy of BOARD.
     */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _moves.clear();
        _moves.addAll(board._moves);
        _turn = board._turn;
        _pieces = board._pieces;
    }

    /**
     * Return the contents of column C, row R, where 1 <= C,R <= 8,
     * where column 1 corresponds to column 'a' in the standard
     * notation.
     */
    Piece get(int c, int r) {
        assert 1 <= c && c <= 8 && 1 <= r && r <= 8;
        return _pieces[r - 1][c - 1];
    }


    /**
     * Return the contents of the square SQ.  SQ must be the
     * standard printed designation of a square (having the form cr,
     * where c is a letter from a-h and r is a digit from 1-8).
     */
    Piece get(String sq) {
        return get(col(sq), row(sq));
    }

    /**
     * Return the column number (a value in the range 1-8) for SQ.
     * SQ is as for {@link get(String)}.
     */
    static int col(String sq) {
        if (!ROW_COL.matcher(sq).matches()) {
            throw new IllegalArgumentException("bad square designator");
        }
        return sq.charAt(0) - 'a' + 1;
    }

    /**
     * Return the row number (a value in the range 1-8) for SQ.
     * SQ is as for {@link get(String)}.
     */
    static int row(String sq) {
        if (!ROW_COL.matcher(sq).matches()) {
            throw new IllegalArgumentException("bad square designator");
        }
        return sq.charAt(1) - '0';
    }

    /**
     * Set the square at column C, row R to V, and make NEXT the next side
     * to move, if it is not null.
     */
    void set(int c, int r, Piece v, Piece next) {
        _pieces[r - 1][c - 1] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /**
     * Set the square at column C, row R to V.
     */
    void set(int c, int r, Piece v) {
        set(c, r, v, null);
    }

    /**
     * Assuming isLegal(MOVE), make MOVE.
     */
    void makeMove(Move move) {
        assert isLegal(move);
        _moves.add(move);
        Piece replaced = move.replacedPiece();
        int c0 = move.getCol0(), c1 = move.getCol1();
        int r0 = move.getRow0(), r1 = move.getRow1();
        if (replaced != EMP) {
            set(c1, r1, EMP);
        }
        set(c1, r1, move.movedPiece());
        set(c0, r0, EMP);
        _turn = _turn.opposite();
    }

    /**
     * Retract (unmake) one move, returning to the state immediately before
     * that move.  Requires that movesMade () > 0.
     */
    void retract() {
        assert movesMade() > 0;
        Move move = _moves.remove(_moves.size() - 1);
        Piece replaced = move.replacedPiece();
        int c0 = move.getCol0(), c1 = move.getCol1();
        int r0 = move.getRow0(), r1 = move.getRow1();
        Piece movedPiece = move.movedPiece();
        set(c1, r1, replaced);
        set(c0, r0, movedPiece);
        _turn = _turn.opposite();
    }

    /**
     * Return the Piece representing who is next to move.
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Return true iff MOVE is legal for the player currently on move.
     */
    boolean isLegal(Move move) {
        if (move != null) {
            int c0 = move.getCol0();
            int r0 = move.getRow0();
            Piece start = get(c0, r0);
            if (move.length() != pieceCountAlong(move)) {
                return false;
            }
            if (start != _turn) {
                return false;
            }
            if (blocked(move)) {
                return false;
            }
        } else if (move == null) {
            return false;
        }
        return true;
    }


    /**
     * Return a sequence of all legal moves from this position.
     */
    Iterator<Move> legalMoves() {
        return new MoveIterator();
    }

    @Override
    public Iterator<Move> iterator() {
        return legalMoves();
    }

    /**
     * Return true if there is at least one legal move for the player
     * on move.
     */
    public boolean isLegalMove() {
        return iterator().hasNext();
    }



    /**
     * Return true iff either player has all his pieces continguous.
     */
    boolean gameOver() {
        return piecesContiguous(BP) || piecesContiguous(WP);
    }


    /** Return true iff SIDE's pieces are contiguous.
     * @param side   BP/WP
     * @return       return an ArrayList */
    boolean piecesContiguous(Piece side) {
        ArrayList<Integer[]> coors = new ArrayList<>();
        ArrayList<Integer[]> connected = new ArrayList<>();
        for (int r = 1; r <= M; r += 1) {
            for (int c = 1; c <= M; c += 1) {
                if (get(c, r).equals(side)) {
                    Integer[] local = new Integer[2];
                    local[0] = r;
                    local[1] = c;
                    coors.add(local);
                }
            }
        }
        if (coors.size() > 1) {
            connected.add(coors.remove(0));
            boolean notcontiguous;
            while (coors.size() != 0) {
                notcontiguous = true;
                for (Integer[] i : coors) {
                    if (pieceConnected(i, connected)) {
                        connected.add(i);
                        coors.remove(i);
                        notcontiguous = false;
                        break;
                    }
                }
                if (notcontiguous) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /** Returns true if PIECE is connected to any of the pieces in REST.
     * @param piece   coordinate
     * @param others   An ArrayList of coordinates of the other pieces
     * @return       whether PIECE is adjacent to any piece  */
    static boolean pieceConnected(Integer[] piece,
                                  ArrayList<Integer[]> others) {
        for (Integer[] r : others) {
            int horiDistance = Math.abs(piece[0] - r[0]);
            int vertiDistance = Math.abs(piece[1] - r[1]);
            if (horiDistance <= 1 && vertiDistance <= 1) {
                return true;
            }
        }
        return false;
    }


    /**
     * Return the total number of moves that have been made (and not
     * retracted).  Each valid call to makeMove with a normal move increases
     * this number by 1.
     */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return b == this;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = M; r >= 1; r -= 1) {
            out.format("    ");
            for (int c = 1; c <= M; c += 1) {
                out.format("%s ", get(c, r).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /**
     * Return the number of pieces in the line of action indicated by MOVE.
     */
    public int pieceCountAlong(Move move) {
        int c0 = move.getCol0() - 1;
        int r0 = move.getRow0() - 1;
        int c1 = move.getCol1() - 1;
        int r1 = move.getRow1() - 1;

        if (c0 == c1 && r0 < r1) {
            return pieceCountAlong(c0, r0, N);
        } else if (c0 < c1 && r0 < r1) {
            return pieceCountAlong(c0, r0, NE);
        } else if (c0 < c1 && r0 == r1) {
            return pieceCountAlong(c0, r0, E);
        } else if (c0 < c1 && r0 > r1) {
            return pieceCountAlong(c0, r0, SE);
        } else if (c0 == c1 && r0 > r1) {
            return pieceCountAlong(c0, r0, S);
        } else if (c0 > c1 && r0 > r1) {
            return pieceCountAlong(c0, r0, SW);
        } else if (c0 > c1 && r0 == r1) {
            return pieceCountAlong(c0, r0, W);
        } else {
            return pieceCountAlong(c0, r0, NW);
        }
    }

    /**
     * Return the number of pieces in the line of action in direction DIR and
     * containing the square at column C and row R.
     */
    public int pieceCountAlong(int c, int r, Direction dir) {
        int count = 0;
        if (dir == E || dir == W) {
            for (Piece p : _pieces[r]) {
                if (p != EMP) {
                    count += 1;
                }
            }
            return count;
        } else if (dir == N || dir == S) {
            int i = 0;
            while (i < M) {
                if (_pieces[i][c] != EMP) {
                    count += 1;
                }
                i++;
            }
            return count;
        } else if (dir == NE || dir == SW) {
            for (int i = 0; i < M; i += 1) {
                int ver = c - r + i;
                if (ver >= 0 && ver < 8 && _pieces[i][ver] != EMP) {
                    count += 1;
                }
            }
            return count;
        } else {
            for (int i = 0; i < M; i += 1) {
                int ver = c + r - i;
                if (ver >= 0 && ver < 8 && _pieces[i][ver] != EMP) {
                    count += 1;
                }
            }
            return count;
        }
    }

    /**
     * Return true IFF (C, R) denotes a square on the board, that is if
     * 1 <= C <= M, 1 <= R <= M.
     */
    public static boolean inBounds(int c, int r) {
        return 1 <= c && c <= M && 1 <= r && r <= M;
    }

    /**
     * Return true iff MOVE is blocked by an opposing piece or by a
     * friendly piece on the target square.
     */
    public boolean blocked(Move move) {
        int c0 = move.getCol0();
        int r0 = move.getRow0();
        int length = move.length();
        Direction dir = move.getDirection();
        int c, r;
        Piece p;
        if (move.replacedPiece() == _turn) {
            return true;
        }
        for (int i = 0; i < length; i += 1) {
            c = c0 + dir.dc * i;
            r = r0 + dir.dr * i;
            p = get(c, r);
            if (p != _turn && p != EMP) {
                return true;
            }
        }
        return false;
    }

    /**
     * The standard initial configuration for Lines of Action.
     */
    static final Piece[][] INITIAL_PIECES = {
            {EMP, BP, BP, BP, BP, BP, BP, EMP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {EMP, BP, BP, BP, BP, BP, BP, EMP}
    };

    /**
     * List of all unretracted moves on this board, in order.
     */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /**
     * Current side on move.
     */
    private Piece _turn;

    /** Returns the board now playing.
     *  @return The current board. */
    Board nowplaying() {
        return this;
    }

    /**
     * Current state of pieces of 8x8 board.
     */
    private Piece[][] _pieces;

    public LinkedList<Move> ValidMoves = new LinkedList<>();

    public LinkedList<Move> getLegalMoves() {
        Iterator<Move> iter = legalMoves();
        for (int i = 0; i < 2; i += 1) {
            if (iter.hasNext()) {
                Move next = iter.next();
                ValidMoves.add(next);
            } else {
                break;
            }
        }
        return ValidMoves;
    }

//    Iterator<Move> legalMoves = b.legalMoves();
//    while (legalMoves.hasNext()) {
//        Move next = legalMoves.next();
//        b.makeMove(next);


    /**
     * An iterator returning the legal moves from the current board.
     */
    private class MoveIterator implements Iterator<Move> {
        /**
         * Current piece under consideration.
         */
        private int _c, _r;
        /**
         * Next direction of current piece to return.
         */
        private Direction _dir;
        /**
         * Next move.
         */
        private Move _move;
        /**
         * The board we're using.
         */
        private Board _board = nowplaying();

        /**
         * A new move iterator for turn().
         */
        MoveIterator() {
            _c = 1;
            _r = 1;
            _dir = NOWHERE;
            _board = nowplaying();
            incr();
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Move next() {
            if (_move == null) {
                throw new NoSuchElementException("no legal move");
            }

            Move move = _move;
            incr();
            return move;
        }

        @Override
        public void remove() {
        }


        /**
         * Advance to the next legal move.
         */
        private void incr() {
            _turn = Board.this.turn();
            if (!inBounds(_c, _r)) {
                return;
            }
            while (inBounds(_c, _r)) {
                while (!get(_c, _r).equals(_turn)) {
                    _dir = NOWHERE;
                    if (_r < M) {
                        _r++;
                    } else if (_c < M) {
//                    } else {
                        _c++;
                        _r = 1;
//                    }
                    } else {
//
                        _c = -1;
                        _r = -1;
                    }
                }
                if (!inBounds(_c, _r)) {
                    return;
                }
                if (_dir == null) {
                    _dir = NOWHERE;
                    if (_r < M) {
                        _r++;
                    } else if (_c < M) {
                        _c++;
                        _r = 1;
                    } else {
                        _c = -1;
                        _r = -1;
                    }
                } else {
                    _dir = _dir.succ();
                    while (_dir != null) {
                        int length = pieceCountAlong(_c - 1 , _r - 1, _dir);
                        Move move = Move.create(_c, _r, length , _dir, _board);
                        if (isLegal(move)) {
                            _move = move;
                            return;
                        }
                        _dir = _dir.succ();
                    }
                }
            }
            return;
        }
    }
}

