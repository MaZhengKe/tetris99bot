package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.opencv.core.Point;

@Data
@AllArgsConstructor
public class SimilarPoint {
    Point point;
    double similarity;
}
