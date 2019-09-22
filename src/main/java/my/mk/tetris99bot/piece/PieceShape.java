package my.mk.tetris99bot.piece;

import lombok.Data;

@Data
public class PieceShape {
    private Point[] points;
    int y;
    int rotateIndex;
    public int w;
    public int h;

    public PieceShape(int y, Point... points) {
        this.y = y;
        this.points = points;
    }

    public PieceShape(int w,int h, int y, int rotateIndex, Point... points) {
        this.w = w;
        this.h = h;
        this.y = y;
        this.rotateIndex = rotateIndex;
        this.points = points;
    }

    public Point[] getPoints() {
        return points;
    }
}
