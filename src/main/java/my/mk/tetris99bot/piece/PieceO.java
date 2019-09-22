package my.mk.tetris99bot.piece;


public class PieceO extends Piece {


    @Override
    public byte number() {
        return 1;
    }

    @Override
    public char character() {
        return 'O';
    }

    @Override
    protected PieceShape[] shapes() {
        return new PieceShape[]{
                new PieceShape(2,2,4, 0, new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1))
        };
    }
}
