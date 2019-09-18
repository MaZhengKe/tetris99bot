package my.mk.ns;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScriptFrame {
    Integer time;
    byte[] bytes;
}
