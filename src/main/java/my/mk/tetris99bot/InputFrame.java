package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import my.mk.ns.NSInputState;

@Data
@AllArgsConstructor
public class InputFrame {
    NSInputState NSInputState;
    int time;
}
