package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import my.mk.tetris99bot.piece.Piece;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
class Move implements Comparable<Move>{
    final M m;
    final Value value;
    Move next = null;

    @Data
    @AllArgsConstructor
   public static class M {
        final Piece piece;
        final int y;
        final int rotateIndex;
        boolean isUseHold;
    }

    Move(int y, int rotateIndex, Piece piece, Value value, boolean isUseHold) {
        this.m = new M(piece,y,rotateIndex,isUseHold);
        this.value = value;
    }

    public int getAllMoveValue() {
        int avg = this.value.avgV;
        int num = 1;
        Move current = this;
        while (current.next != null) {
            num++;
            avg += current.next.value.avgV;
            current = current.next;
        }
        return avg / num + current.value.sumV;
    }

    @Override
    public String toString() {
        return m.piece + " " + m.y + " " + m.rotateIndex +
                ",  V=" + getAllMoveValue() +
                (m.isUseHold ? " useHold" : " notUse ") +
                " value=" + value +
                "\n" + (next != null ? next : "");
    }

    @Override
    public int compareTo(@NotNull Move o) {
        return o.getAllMoveValue() - this.getAllMoveValue() ;
    }
}
