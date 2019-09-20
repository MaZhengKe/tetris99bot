package my.mk.tetris99bot;

import lombok.Data;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.opencv_core.Mat;

@Data
class Block {
    Mat mat;
    String colour;
    int que;
    int [][][] hsv = new int[8][8][3];

    Block(Mat mat, String colour) {
        this(mat,colour,20);
    }

    Block(Mat mat, String colour, int que) {
        this.mat = Util.toHSV(mat);
        this.colour = colour;
        this.que = que;
        int[] tmp= new int[3];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                UByteIndexer indexer = this.mat.createIndexer();
                indexer.get(x,y,tmp);
                System.arraycopy(tmp, 0, hsv[x][y], 0, 3);
            }
        }
    }
}
