package my.mk.tetris99bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mk.tetris99bot.piece.*;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    Piece hold = null;
    Piece currentPiece = null;
    boolean canUseHold;

    private Board copy() {
        return new Board(copy(filled), next.clone(), hold, currentPiece, canUseHold);
    }

    private static boolean[][] copy(boolean[][] sourceRows) {
        boolean[][] newRows = new boolean[20][10];
        for (int i = 0; i < newRows.length; i++) {
            newRows[i] = sourceRows[i].clone();
        }
        return newRows;
    }

    private void fill(int x, int y) {
        filled[x][y] = true;
    }

    private int fill(int y, PieceShape shape, int h) {
        int x = minX(y, shape);
        if (x == 20) {
            //log.error("over");
            return x;
        }
        int cleaned = fill(x, y, shape);
        int height = x + (int) ((float) h / 2 - 0.5);
        return 34 * 2 * cleaned - height * 45;
    }


    private void fill(Piece piece) {
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


    private int fill(int x, int y, PieceShape shape) {
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


    private boolean cannotFill(int x, int y) {

        if (x < 0 || x >= 20 || y < 0 || y >= 10)
            return true;
        return filled[x][y];
    }


    private int clean() {
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

    private Value value(int v) {
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
                q = 0;
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


    private boolean isEmptyRow(boolean[] row) {
        for (boolean b : row)
            if (b)
                return false;
        return true;
    }

    private boolean isFulledRow(boolean[] row) {
        for (boolean b : row)
            if (!b)
                return false;
        return true;
    }

    void paint() {
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
        s.append("nexts:").append(Arrays.toString(next)).append("\n")
                .append("hold :").append(hold).append("\n")
                .append("canus:").append(canUseHold).append("\n")
                .append("curre:").append(currentPiece).append("\n");
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


    Move get(int dep) {
        if (hold == null) {
            hold = currentPiece;
            Piece toUse = next[0];
            useNext();
            this.currentPiece = next[0];
            useNext();
            return new Move(0, 0, toUse, new Value(0, 0, false), true);
        }

        Move max = new Move(0, 0, null, new Value(0, Integer.MIN_VALUE, false), false);

        List<Move> moves = new ArrayList<>();
        Piece piece = currentPiece;

        for (int i = 0; i <= piece.rotationsEndIndex(); i++) {
            PieceShape shape = piece.getShape(i);
            int width = piece.width(i);
            for (int y = 0; y <= 10 - width; y++) {
                Board copy = copy();
                int v = copy.fill(y, shape, piece.height(i));
                //copy.paint();
                Value value = copy.value(v);
                //System.out.println(value);
                Move tmp = new Move(y, i, piece, value, false);
                moves.add(tmp);
                if (value.v > max.value.v) {
                    max = tmp;
                }
            }
        }
        piece = hold;
        for (int i = 0; i <= piece.rotationsEndIndex(); i++) {
            PieceShape shape = piece.getShape(i);
            int width = piece.width(i);
            for (int y = 0; y <= 10 - width; y++) {
                Board copy = copy();
                int v = copy.fill(y, shape, piece.height(i));
                //copy.paint();
                Value value = copy.value(v);
                //System.out.println(value);
                Move tmp = new Move(y, i, piece, value, true);
                moves.add(tmp);
                if (value.v >  max.value.v) {
                    max = tmp;
                }
            }
        }

        if(dep<=0){
            if(max.isUseHold)
                hold = currentPiece;
            currentPiece = next[0];
            useNext();
            return max;
        }

        dep--;
        int maxV = Integer.MIN_VALUE;
        Move res = null;
        for (Move move : moves) {
            Board copy = fill(move);
            if(move.isUseHold){
                copy.hold = copy.currentPiece;
            }
            copy.currentPiece = copy.next[0];
            copy.useNext();

            //copy.paint();
            Move nextMove = copy.get(dep - 1);
            move.next = nextMove;
            int avgV = (nextMove.value.getJiluV() + move.value.getJiluV()) / 2 + nextMove.value.v - nextMove.value.jiluV;
            if (avgV > maxV) {
                res = move;
                maxV = avgV;
            }

        }
        assert res != null;
        if(res.isUseHold)
            hold = currentPiece;
        currentPiece = next[0];
        useNext();
        return res;
    }


    private Move getMove(int dep) {
        //paint();
        Move pieceMove = getMove(currentPiece, dep, false);

        Move holdMove = null;
        if (canUseHold) {
            log.trace("开始计算holdValue");
            holdMove = getMove(hold, dep, true);
            holdMove.isUseHold = true;
            log.trace("结束计算holdValue");
        }
        if (holdMove == null) {
            if (hold == null) {
                Board copy = copy();
                copy.hold = copy.currentPiece;
                copy.canUseHold = false;
                copy.currentPiece = copy.next[0];
                copy.useNext();
                Move nextMove = copy.getMove(dep - 1);
                //Move pieceMoveM = getMove(currentPiece,dep - 1,false);
                if (nextMove.value.v > pieceMove.value.v) {
                    useNext();
                    hold = currentPiece;
                    currentPiece = next[0];
                    useNext();

                    canUseHold = true;
                    nextMove.isUseHold = true;
                    //log.info("init hold");
                    return nextMove;
                } else {
                    currentPiece = next[0];
                    useNext();
                    return pieceMove;
                }
            } else {
                currentPiece = next[0];
                useNext();
                return pieceMove;
            }
        } else {
            if (canUseHold && pieceMove.value.v < holdMove.value.v) {
                hold = currentPiece;
                currentPiece = next[0];
                useNext();
                return holdMove;
            } else {
                currentPiece = next[0];
                useNext();
                canUseHold = true;
                return pieceMove;
            }
        }
    }


    private Move getMove(Piece piece, int dep, boolean isUseHold) {
        //paint();

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
                moves.add(new Move(y, i, piece, value, isUseHold));
                if (value.v > maxValue.v) {
                    maxValue = value;
                    recodeY = y;
                    recodeI = i;
                }
            }
        }

        if (dep <= 0) {
            return new Move(recodeY, recodeI, piece, maxValue, isUseHold);
        }


        int maxV = Integer.MIN_VALUE;
        Move res = null;
        for (Move move : moves) {
            Board copy = fill(move);
            copy.currentPiece = copy.next[0];
            copy.useNext();
            //copy.paint();
            Move nextMove = copy.getMove(dep - 1);
            int avgV = (nextMove.value.getJiluV() + move.value.getJiluV()) / 2 + nextMove.value.v - nextMove.value.jiluV;
            if (avgV > maxV) {
                res = move;
                maxV = avgV;
            }
        }
        return res;

    }

    void useNext() {
        System.arraycopy(next, 1, next, 0, 5);
    }

    @NotNull
    Board fill(Move move) {
        Board copy = copy();
        copy.fill(move.y, move.piece.getShape(move.xun), move.piece.height(move.xun));
        copy.clean();
        return copy;
    }

    int getHeight() {
        int h = -1;
        for (int y = 0; y < 10; y++) {
            for (int x = 19; x >= 0; x--) {
                if (filled[x][y] && x > h) {
                    h = x;
                    break;
                }
            }
        }
        return h + 1;
    }
}
