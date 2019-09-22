package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Value {
    int avgV;
    int sumV;
    int all ;

    public Value(int avgV, int sumV) {
        this.avgV = avgV;
        this.sumV = sumV;
        this.all = avgV +sumV;
    }
}
