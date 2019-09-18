package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.mk.tetris99bot.piece.Piece;

@Data
@AllArgsConstructor
public class Move {
    final int y;
    final int xun;
    final Piece piece;
    final Value value;
}
