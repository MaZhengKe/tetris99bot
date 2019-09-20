package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Point;

@Data
@AllArgsConstructor
public class SimilarPoint {
    Point point;
    double similarity;

}
