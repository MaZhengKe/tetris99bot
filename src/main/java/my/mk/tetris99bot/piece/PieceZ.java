package my.mk.tetris99bot.piece;


public class PieceZ extends Piece {


    @Override
    public byte number() {
        return 4;
    }

    @Override
    public char character() {
        return 'Z';
    }

    @Override
    protected PieceShape[] shapes() {
        return new PieceShape[]{
                new PieceShape(3,2,3, 0, new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)),
                new PieceShape(2,3,3, 1, new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1))
        };
    }
}
