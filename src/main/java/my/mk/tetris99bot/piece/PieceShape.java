package my.mk.tetris99bot.piece;

import lombok.Data;

@Data
public class PieceShape {
    private Point[] shape;
    int y;

    public PieceShape( int y,Point...  shape) {
        this.shape = shape;
        this.y = y;
    }

    public Point[] getPoints() {
        return shape;
    }
}
