package my.mk.tetris99bot.piece;

import lombok.Data;

@Data
public class PieceShape {
    private Point[] points;
    int y;
    int rotateIndex;

    public PieceShape(int y, Point... points) {
        this.y = y;
        this.points = points;
    }

    public PieceShape(int y, int rotateIndex, Point... points) {
        this.y = y;
        this.rotateIndex = rotateIndex;
        this.points = points;
    }

    public Point[] getPoints() {
        return points;
    }
}
