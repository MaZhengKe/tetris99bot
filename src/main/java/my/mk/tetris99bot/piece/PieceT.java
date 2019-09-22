package my.mk.tetris99bot.piece;


public class PieceT extends Piece {


    @Override
    public byte number() {
        return 7;
    }

    @Override
    public char character() {
        return 'T';
    }

    @Override
    protected PieceShape[] shapes() {
        return new PieceShape[]{
                new PieceShape(3,2,3, 0, new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)),
                new PieceShape(2,3,3, 1, new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)),
                new PieceShape(3,2,3, 2, new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2)),
                new PieceShape(2,3,4, 3, new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(1, 1))
        };
    }
}
