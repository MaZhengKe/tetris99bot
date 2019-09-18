package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.opencv.core.Mat;

@Data
public class Block {
    Mat mat;
    String colour;
    int que;

    public Block(Mat mat, String colour) {
        this(mat,colour,20);
    }

    public Block(Mat mat, String colour, int que) {
        this.mat = Util.toHSV(mat);
        this.colour = colour;
        this.que = que;
    }
}
