package loa;

import java.util.Random;


/** An automated Player.
 *  @author Zixuan Ge */
class MachinePlayer extends Player {

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    Move makeMove() {
        Game game = getGame();
        Board board = game.getBoard();
        if (board.isLegalMove()) {
            int length = board.getLegalMoves().size();
            Random r = new Random();
            int m = r.nextInt(length - 1);
            Move move = board.getLegalMoves().get(m);
            if (board.turn().fullName().equals("black")) {
                System.out.println("B::" + move);
            } else {
                System.out.println("W::" + move);
            }
            return move;
        }
        return null;


    }
}
