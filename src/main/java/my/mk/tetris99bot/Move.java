package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.mk.tetris99bot.piece.Piece;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.opencv.core.Core;

import java.awt.image.BufferedImage;

import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

@Data
@AllArgsConstructor
public class Move {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    final int y;
    final int xun;
    final Piece piece;
    final Value value;
    boolean isUseHold;
    Move next = null;

    public Move(int i, int i1, Piece toUse, Value value, boolean b) {
        this(i, i1, toUse, value, b, null);
    }

    public static void main(String[] args) {
        Mat mat = imread("D:/99bot/darkblue.png");
        System.out.println("a");
        System.out.println("a");
        System.out.println("a");
        System.out.println("a");
        BufferedImage bufferedImage = toBufferedImage(mat);
        Mat sub = mat.apply(new Rect(0, 0, 1, 1));
        BufferedImage subimage = toBufferedImage(sub);
        System.out.println("end");
    }
}
