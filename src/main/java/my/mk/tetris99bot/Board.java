package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mk.tetris99bot.piece.*;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Core;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Board {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    boolean[][] filled = new boolean[20][10];

    Piece[] next = new Piece[6];

    Piece hold;

    boolean canUseHold;

    public Board copy() {
        return new Board(copy(filled), next.clone(), hold,canUseHold);
    }

    private static boolean[][] copy(boolean[][] sourceRows) {
        boolean[][] newRows = new boolean[20][10];
        for (int i = 0; i < newRows.length; i++) {
            newRows[i] = sourceRows[i].clone();
        }
        return newRows;
    }

    public void fill(int x, int y) {
        filled[x][y] = true;
    }

    public int fill(int y, PieceShape shape, int h) {
        int x = minX(y, shape);
        if (x == 20) {
            log.error("over");
            return x;
        }
        int cleaned = fill(x, y, shape);
        int height = x + (int) ((float) h / 2 - 0.5);
        return 34 * 2 * cleaned - height * 45;
    }


    public void fill(Piece piece) {
        int recodeY = 0;
        int recodeI = 0;
        int maxValue = Integer.MIN_VALUE;


        for (int i = 0; i <= piece.rotationsEndIndex(); i++) {
            PieceShape shape = piece.getShape(i);
            int width = piece.width(i);
            for (int y = 0; y <= 10 - width; y++) {
                Board copy = copy();
                int v = copy.fill(y, shape, piece.height(i));
                //copy.paint();
                Value value = copy.value(v);
                //System.out.println(value);
                if (value.v > maxValue) {
                    maxValue = value.v;
                    recodeY = y;
                    recodeI = i;
                }
            }
        }


        fill(recodeY, piece.getShape(recodeI), piece.height(recodeI));
        clean();

    }


    public int fill(int x, int y, PieceShape shape) {
        Point[] points = new Point[4];

        Point[] shapePoints = shape.getPoints();
        for (int i = 0; i < 4; i++) {
            Point shapePoint = shapePoints[i];
            points[i] = new Point(x - shapePoint.x, y + shapePoint.y);
            fill(x - shapePoint.x, y + shapePoint.y);
        }
        int num = 0;

        for (Point point : points) {
            if (isFulledRow(filled[point.x]))
                num++;
        }
        return num;
    }

    private int minX(int y, PieceShape shape) {
        for (int x = 19; x >= -1; x--) {
            Point[] points = shape.getPoints();
            for (int j = 0; j < 4; j++) {
                Point point = points[j];
                if (cannotFill(x - point.x, y + point.y)) {
                    return x + 1;
                }
            }
        }


        throw new RuntimeException("未检测底部?");
    }


    public boolean cannotFill(int x, int y) {

        if (x < 0 || x >= 20 || y < 0 || y >= 10)
            return true;
        return filled[x][y];
    }


    public int clean() {
        int num = 0;
        for (int x = 0; x < 20; x++) {
            if (isFulledRow(filled[x])) {
                for (int xx = x; xx < 19; xx++) {
                    filled[xx] = filled[xx + 1].clone();
                }
                filled[19] = new boolean[10];
                x--;
                num++;
            }
        }
        return num;
    }

    public Value value(int v) {
        int clean = clean();
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
                if (filled[x][y] != now) {
                    now = filled[x][y];
                    rowTransitions++;
                }
            }
            if (!now)
                rowTransitions++;
        }

        for (int x = 0; x < 20; x++) {
            boolean now = true;
            for (int y = 0; y < 10; y++) {
                if (filled[x][y] != now) {
                    now = filled[x][y];
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
                if (filled[x][y]) {
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
                120, 136, 153, 171, 190, 210};        //井的打表

        for (int y = 0; y < 10; y++)
            for (int x = 0; x < 20; x++) {
                if (!filled[x][y] && (y == 0 || filled[x][y - 1]) && (y == 9 || filled[x][y + 1])) {
                    cnt++;
                } else {
                    wellSums += well[cnt];
                    cnt = 0;
                }
            }


//        System.out.println(" h:" + h +
//                " clean：" + clean +
//                " row：" + rowTransitions +
//                " col：" + columTransitions +
//                " holes：" + numberofHoles +
//                " well：" + wellSums
//        );

        int q;

        switch (clean) {
            case 1:
                q = -200;
                break;
            case 2:
                q = 400;
                break;
            case 3:
                q = 1600;
                break;
            case 4:
                q = 3200;
                break;
            default:
                q = 0;
        }
        int vv = v
                + q
                - 32 * rowTransitions
                - 93 * colTransitions
                - 79 * numberOfHoles
                - 34 * wellSums;
        return new Value(v, vv, clean > 1);

//
//        for (int y = 0; y < 10; y++) {
//            int maxX = -1;
//            int filledNum = 0;
//            for (int x = 0; x < 20; x++) {
//                if (filled[x][y]) {
//                    maxX = x;
//                    filledNum++;
//                }
//
//            }
//            kong += (maxX - filledNum + 1);
//        }
//
//        for (int x = 0; x < 20; x++) {
//            int maxY = -1;
//            int filledNum = 0;
//            for (int y = 0; y < 10; y++) {
//                if (filled[x][y]) {
//                    maxY = y;
//                    filledNum++;
//                }
//            }
//
//            int minY = 10;
//            for (int y = 9; y >= 0; y--) {
//                if (filled[x][y]) {
//                    minY = y;
//                }
//            }
//            if (maxY != -1)
//                kong += (maxY - minY - filledNum + 1);
//        }
//
//
//        // System.out.println("kong:" +kong);
//
//        return mixHeight * 2 - kong;
    }

    public int height() {
        for (int i = 0; i < 20; i++) {
            boolean[] row = filled[i];
            if (isEmptyRow(row))
                return i;
        }
        return 20;
    }


    public boolean isEmptyRow(boolean[] row) {
        for (boolean b : row)
            if (b)
                return false;
        return true;
    }

    public boolean isFulledRow(boolean[] row) {
        for (boolean b : row)
            if (!b)
                return false;
        return true;
    }

    public void paint() {
        StringBuilder s = new StringBuilder("\n");
        for (int x = 19; x >= 0; x--) {
            s.append("|");
            for (int y = 0; y < 10; y++) {
                if (filled[x][y]) {
                    s.append("██");
                } else {
                    s.append("  ");
                }
            }
            s.append("|\n");
        }
        s.append("----------------------\n");
        s.append(Arrays.toString(next));
        log.info(s.toString());
    }


    public static void main(String[] args) {
        Piece[] pieces = new Piece[]{new PieceO(), new PieceI(), new PieceS(), new PieceZ(), new PieceL(), new PieceJ(), new PieceT()};

        String s = "I";
        Random random = new Random();
        Board board = new Board();
        StringBuilder recod = new StringBuilder();

        for (char c : s.toCharArray()) {
            int i = Piece.indexOf(c) - 1;
            Piece piece = pieces[i];
            board.fill(piece);
            board.paint();
            System.out.println("---------------------------------" + piece.character());
        }
//
//        for (int i = 0; i < 100; i++) {
//            Piece piece = pieces[random.nextInt(7)];
//            board.fill(piece);
//            board.paint();
//            recod.append(piece.character());
//            System.out.println("---------------------------------" + piece.character() + " " + recod.toString());
//        }

    }


    public Move getMove(Piece piece, int dep) {
        filled[19] = new boolean[10];
        filled[18] = new boolean[10];
        filled[17] = new boolean[10];

        int recodeY = 0;
        int recodeI = 0;
        Value maxValue = new Value(0, Integer.MIN_VALUE, false);

        List<Move> moves = new ArrayList<>();

        for (int i = 0; i <= piece.rotationsEndIndex(); i++) {
            PieceShape shape = piece.getShape(i);
            int width = piece.width(i);
            for (int y = 0; y <= 10 - width; y++) {
                Board copy = copy();
                int v = copy.fill(y, shape, piece.height(i));
                //copy.paint();
                Value value = copy.value(v);
                //System.out.println(value);
                moves.add(new Move(y, i, piece, value));
                if (value.v > maxValue.v) {
                    maxValue = value;
                    recodeY = y;
                    recodeI = i;
                }
            }
        }

        if (dep <= 0) {
            return new Move(recodeY, recodeI, piece, maxValue);
        }

        Piece nextPiece = next[0];


        int maxV = Integer.MIN_VALUE;
        Move res = null;
        for (Move move : moves) {
            Board copy = fill(move);

            for (int i = 0; i < 5; i++) {
                copy.next[i] = copy.next[i + 1];
            }
            Move nextMove = copy.getMove(nextPiece, --dep);
            int avgV = (nextMove.value.getJiluV() + move.value.getJiluV()) / 2 + nextMove.value.v - nextMove.value.jiluV;
            if (avgV > maxV) {
                res = move;
                maxV = avgV;
            }
        }
        return res;

    }

    @NotNull
    public Board fill(Move move) {
        Board copy = copy();
        copy.fill(move.y, move.piece.getShape(move.xun), move.piece.height(move.xun));
        copy.clean();
        return copy;
    }

    public int getHeight() {
        int h = -1;
        for (int y = 0; y < 10; y++) {
            for (int x = 19; x >= 0; x--) {
                if (filled[x][y] && x > h) {
                    h = x;
                    break;
                }
            }
        }
        return h+1;
    }
}
