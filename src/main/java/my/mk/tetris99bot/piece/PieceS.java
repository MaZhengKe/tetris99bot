package my.mk.tetris99bot.piece;


public class PieceS extends Piece {


    @Override
    public byte number() {
        return 3;
    }

    @Override
    public char character() {
        return 'S';
    }

    @Override
    protected PieceShape[] shapes() {
        return new PieceShape[]{
                new PieceShape(3,2,3, 0, new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2)),
                new PieceShape(2,3,3, 1, new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1))
        };
    }
}
