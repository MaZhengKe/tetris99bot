package my.mk.tetris99bot.piece;

public class PieceI extends Piece {


    @Override
    public byte number() {
        return 2;
    }

    @Override
    public char character() {
        return 'I';
    }

    @Override
    protected PieceShape[] shapes() {
        return new PieceShape[]{
                new PieceShape(4,1,3, 0, new Point(0, 0), new Point(0, 1), new Point(0, 2), new Point(0, 3)),
                new PieceShape(1,4,4, 1, new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0))
        };
    }
}
