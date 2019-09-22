package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mk.tetris99bot.piece.Piece;
import my.mk.tetris99bot.piece.PieceShape;
import my.mk.tetris99bot.piece.Point;
import org.opencv.core.Core;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Board {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // TODO 不使用二维数组
    long[] rows = new long[20];
    Piece[] next = new Piece[6];
    Piece hold = null;
    Piece currentPiece = null;

    @Override
    public int hashCode() {
        int result = hold.hashCode();
        result = 31 * result + currentPiece.hashCode();
        result = 31 * result + Arrays.hashCode(rows);
        return result;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;

        Board board = (Board) that;

        if (hold != board.hold) return false;
        if (currentPiece != board.currentPiece) return false;
        if (!Arrays.equals(rows, board.rows)) return false;

        return true;
    }

    private Board copy() {
        return new Board(rows.clone(), next.clone(), hold, currentPiece);
    }

    /**
     * Sets part of a piece on the board.
     *
     * @param x             board row
     * @param pieceRowCells filled cells of a specific piece row
     */
    private void setBits(int x, long pieceRowCells) {
        rows[x] |= pieceRowCells;
    }

    /**
     * Cleares part of a piece on the board.
     *
     * @param x                    board row
     * @param inversePieceRowCells filled cells of a specific piece row
     */
    public void clearBits(int x, long inversePieceRowCells) {
        rows[x] &= inversePieceRowCells;
    }

    /**
     * @param x             board row
     * @param pieceRowCells filled cells of a specific piece row
     * @return true if the piece row cells are not occupied on the board
     */
    public boolean isBitsFree(int x, long pieceRowCells) {
        try {
            return (rows[x] & pieceRowCells) == 0;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    private void fill(int x, int y) {
        setBits(x, 1 << y);
    }

    private static int[] s = new int[]{0, -800, -500, 1600, 6200};

    private int fill(int y, PieceShape shape) {
        int x = minX(y, shape);
        if (x == 20) {
            return x;
        }
        fill(x, y, shape);

        int clean = clearRows(x, shape.h);
//        int height = 2*x + shape.h - 1;
//        return s[clean]- (int)(height * 22.5);


        int height = x + (int) ((float) shape.h / 2 - 0.5);
//        return 34 * cleaned - height * 45;
        return s[clean] - height * 45;
    }

    private void fill(int x, int y, PieceShape shape) {
        Point[] shapePoints = shape.getPoints();
        for (int i = 0; i < 4; i++) {
            Point shapePoint = shapePoints[i];
            fill(x - shapePoint.x, y + shapePoint.y);
        }
    }

    private int minX(int y, PieceShape shape) {
        Point[] points = shape.getPoints();
        for (int x = 19; x >= 0; x--) {
            for (int j = 0; j < 4; j++) {
                Point point = points[j];
                if (isFilled(x - point.x - 1, y + point.y)) {
                    return x;
                }
            }
        }
        throw new RuntimeException("未检测底部?");
    }

    private int clearRows(int x, int pieceHeight) {
        int clearedRows = 0;
        int x1 = x - pieceHeight;

        // Find first row to clear
        do {
            x1++;
            if (rows[x1] == FULL_ROW) {
                clearedRows++;
            }
        } while (clearedRows == 0 && x1 < x);

        // Clear rows
        if (clearedRows > 0) {
            int x2 = x1;

            while (x1 < 20) {
                x2++;
                while (x2 <= x && rows[x2] == FULL_ROW) {
                    clearedRows++;
                    x2++;
                }
                if (x2 < 20) {
                    rows[x1] = rows[x2];
                } else {
                    rows[x1] = Board.EMPTY_ROW;
                }
                x1++;
            }
        }
        return clearedRows;
    }

    private Value value(int avgV) {
//        int mixHeight = 20 - height();
//
//        int kong = 0;

        int rowTransitions = 0;
        int colTransitions = 0;
        int numberOfHoles = 0;
        int wellSums = 0;
        for (int y = 0; y < 10; y++) {
            boolean now = true;

            for (int x = 0; x < 20; x++) {
                if (isFilled(x, y) != now) {
                    now = isFilled(x, y);
                    rowTransitions++;
                }
            }

            if (!now)
                rowTransitions++;
        }

        for (int x = 0; x < 20; x++) {
            boolean now = true;
            for (int y = 0; y < 10; y++) {
                if (isFilled(x, y) != now) {
                    now = isFilled(x, y);
                    colTransitions++;
                }
            }
            if (!now)
                colTransitions++;
        }
        for (int y = 0; y < 10; y++) {
            boolean f = false;
            int block = 0;
            for (int x = 19; x >= 0; x--) {
                if (isFilled(x, y)) {
                    f = true;
                    block++;
                } else if (f) {
                    numberOfHoles += block;
                    f = false;
                    block = 0;
                }
            }
        }

        int cnt = 0;
        int[] well = new int[]{0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 66, 78, 91, 105,
                120, 136, 153, 171, 190, 210};

        for (int y = 0; y < 10; y++)
            for (int x = 0; x < 20; x++) {
                if (!isFilled(x, y) && (y == 0 || isFilled(x, y - 1)) && (y == 9 || isFilled(x, y + 1))) {
                    cnt++;
                } else {
                    wellSums += well[cnt];
                    cnt = 0;
                }
            }

        int sumV = -32 * rowTransitions
                - 93 * colTransitions
                - 79 * numberOfHoles
                - 34 * wellSums;
        return new Value(avgV, sumV);
    }

    public static long EMPTY_ROW = 0;
    public static long FULL_ROW = 0x3FF;

    private boolean isFilled(int x, int y) {
        if (x < 0)
            return true;
        return (rows[x] & (1L << y)) != 0;
    }

    void paintAll(Move move) {
        Board copy = copy();
        List<Point> list = new ArrayList<>();
        Move.M m = move.m;
        PieceShape shape = m.piece.getShape(m.rotateIndex);
        for (Point point : shape.getPoints()) {
            int x = copy.minX(m.y, shape) - point.x;
            int y = m.y + point.y;
            list.add(new Point(x, y));
        }
        String[] p = new String[20];
        for (int x = 19; x >= 0; x--) {
            p[x] = "|";
            for (int y = 0; y < 10; y++) {
                if (copy.isFilled(x, y)) {
                    p[x] += ("██");
                } else if (list.contains(new Point(x, y))) {
                    p[x] += ("▒▒");
                } else {
                    p[x] += ("  ");
                }
            }
            p[x] += "|";
        }

        while (move.next != null) {
            copy.fill(move);
            list.clear();
            m = move.next.m;
            shape = m.piece.getShape(m.rotateIndex);
            for (Point point : shape.getPoints()) {
                int x = copy.minX(m.y, shape) - point.x;
                int y = m.y + point.y;
                list.add(new Point(x, y));
            }


            for (int x = 19; x >= 0; x--) {
                p[x] += "|";
                for (int y = 0; y < 10; y++) {
                    if (copy.isFilled(x, y)) {
                        p[x] += ("██");
                    } else if (list.contains(new Point(x, y))) {
                        p[x] += ("▒▒");
                    } else {
                        p[x] += ("  ");
                    }
                }
                p[x] += "|";
            }
            move = move.next;
        }
        for (int x = 19; x >= 0; x--) {
            log.info(p[x]);
        }
        log.info("--------------------------------------------------------------------------------------------------------------");


    }

    void paint() {
        StringBuilder s = new StringBuilder("\n");
        for (int x = 19; x >= 0; x--) {
            s.append("|");
            for (int y = 0; y < 10; y++) {
                if (isFilled(x, y)) {
                    s.append("██");
                } else {
                    s.append("  ");
                }
            }
            s.append("|\n");
        }
        s.append("----------------------\n");
        s.append("nextList:").append(Arrays.toString(next)).append("\n")
                .append("hold    : ").append(hold).append("\n")
                .append("current : ").append(currentPiece).append("\n");
        log.info(s.toString());
    }


    private static Comparator<Move> comparator = Comparator.comparingInt(move -> -move.getAllMoveValue());

    Move get(int dep) {
        if (hold == null) {
            hold = next[0];
            refreshNext();
            return new Move(0, 0, hold, new Value(0, 0), true);
        }

        TreeSet<Move> moves = getMoves();

        if (dep <= 0) {
            return moves.first();
        }

        Move rs = null;
        Iterator<Move> iterator = moves.iterator();
        for (int i = 0; i < 3; i++) {
            if (iterator.hasNext()) {
                Move move = iterator.next();
                Board copy = copy();
                copy.useMove(move);
                copy.paint();
                move.next = copy.get(dep - 1);
                if (rs == null || move.getAllMoveValue() > rs.getAllMoveValue()) {
                    rs = move;
                }
            }
        }

        return rs;

//
//        TreeSet<Move> newMoves = new TreeSet<>();
//
//
//        moves.stream().limit(5).forEach(move -> {
//                    Board copy = copy();
//                    copy.useMove(move);
//                    move.next = copy.get(dep - 1);
//                    newMoves.add(move);
//                }
//        );
//        return newMoves.first();
    }


    public static HashMap<Board, TreeSet<Move>> cache = new HashMap<>();
    public static long cached = 0;
    public static long notCached = 0;

    private TreeSet<Move> getMoves() {
        TreeSet<Move> moves = cache.get(this);
        if (moves != null) {
            //log.info("got cache");
            cached++;
            return moves;
        }
        notCached++;

        Piece piece = currentPiece;
        moves = new TreeSet<>(getPieceMoves(piece));

        piece = hold;
        List<Move> holdMoves = getPieceMoves(piece);
        for (Move holdMove : holdMoves) {
            holdMove.m.isUseHold = true;
            moves.add(holdMove);
        }

        cache.put(this, moves);
        return moves;
    }

    private List<Move> getPieceMoves(Piece piece) {

        List<Move> moves = new LinkedList<>();
        for (int i = 0; i <= piece.rotationsEndIndex(); i++) {
            PieceShape shape = piece.getShape(i);
            int width = shape.getW();
            for (int y = 0; y <= 10 - width; y++) {
                Board copy = copy();
                int v = copy.fill(y, shape);
                Value value = copy.value(v);
                Move tmp = new Move(y, i, piece, value);
                moves.add(tmp);
            }
        }
        return moves;
    }

    void useMove(Move move) {
        if (move.m.isUseHold)
            hold = currentPiece;
        currentPiece = next[0];
        refreshNext();
        fill(move);
    }

    void refreshNext() {
        System.arraycopy(next, 1, next, 0, 5);
    }

    private void fill(Move move) {
        PieceShape shape = move.m.piece.getShape(move.m.rotateIndex);
        fill(move.m.y, shape);
    }

}
