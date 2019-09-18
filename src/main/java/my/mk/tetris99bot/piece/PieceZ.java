package my.mk.tetris99bot.piece;


public class PieceZ extends Piece {


    @Override public byte number() { return 4; }
    @Override public char character() { return 'Z'; }
    @Override protected int[] widths() { return new int[] { 3, 2  }; }
    @Override protected int[] heights() { return new int[] { 2, 3}; }
    @Override protected PieceShape[] shapes() {
        return new PieceShape[] {
                new PieceShape(3,new Point(0,0), new Point(0,1), new Point(1,1), new Point(1,2)),
                new PieceShape(3,new Point(1,0), new Point(2,0), new Point(0,1), new Point(1,1))
        };
    }
}
