package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import my.mk.tetris99bot.piece.*;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Core;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.minMaxLoc;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.matchTemplate;

@Slf4j
public class Util {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Loader.load(opencv_core.class);
    }

    private static DoublePointer minVal = new DoublePointer(1);
    private static DoublePointer maxVal = new DoublePointer(1);
    private static Point min = new Point(1);
    private static Point max = new Point(1);
    private static List<Block> includedMats = new ArrayList<>();
    public static String Path = "D:\\99bot\\kb\\a\\";
    private static String piecePath = Path + "piece/";
    private static Block gray = new Block(imread(piecePath + "gray.png"), "gray", 25);

    static {
        includedMats.add(new Block(imread(piecePath + "Dblue.png"), "Dkblue", 7));
        includedMats.add(gray);
        includedMats.add(new Block(imread(piecePath + "Green.png"), "Green"));
        includedMats.add(new Block(imread(piecePath + "Lblue.png"), "Ltblue"));
        includedMats.add(new Block(imread(piecePath + "Purple.png"), "Purple"));
        includedMats.add(new Block(imread(piecePath + "Red.png"), "Red   ", 11));
        includedMats.add(new Block(imread(piecePath + "Yellow.png"), "Yellow"));
        includedMats.add(new Block(imread(piecePath + "Orange.png"), "Orange"));
    }

    @NotNull
    static Mat toHSV(Mat img) {
        Mat imgHSV = new Mat();
        cvtColor(img, imgHSV, Imgproc.COLOR_BGR2HSV);
        return imgHSV;
    }

    private static SimilarPoint maxPointInMat(Mat mat, Mat subMat) {
        Mat result = new Mat();
        matchTemplate(mat, subMat, result, Imgproc.TM_CCORR_NORMED);
        minMaxLoc(result, minVal, maxVal, min, max, null);
        return new SimilarPoint(max, maxVal.get());
    }

    private static boolean isIncludeMat(Mat mat) {
        for (Block excludedMat : includedMats) {
            double similarity = similarity(mat, excludedMat);
            if (similarity > 40)
                return true;
        }
        return false;
    }

    private static Piece[] pieces = new Piece[]{new PieceO(), new PieceI(), new PieceS(), new PieceZ(), new PieceL(), new PieceJ(), new PieceT()};

    private static double similarity(Mat mat, Block block) {
        UByteIndexer indexer = mat.createIndexer();
        int[] a = new int[3];
        if (block.equals(gray)) {

            int all = 0;

            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    indexer.get(x, y, a);
                    int num = Math.abs(a[1] - block.hsv[x][y][1]) + Math.abs(a[2] - block.hsv[x][y][2]);
                    if (num < 35)
                        all++;
                }
            }

            log.trace("{}\t{}", block.colour, all);
            return all;
        }

        int all = 0;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {

                indexer.get(x, y, a);
                if (a[2] < 85) {
                    all -= 2;
                    continue;
                }
                int num = Math.abs(a[0] - block.hsv[x][y][0]);

                if (num < 10)
                    all++;
            }
        }

        log.trace("{}\t{}", block.colour, all);
        return all;
    }

    private static boolean[][] getFilled() {
        log.info("start to get mat");
        Mat mat = getFilledMat();
        log.info("get mat end");
        boolean[][] filled = getFilled(mat);
        log.info("trans end");
        return filled;
    }

    private static FrameTracker frameTracker = new FrameTracker();
    private static Rect boardRect = new Rect(720, 60, 480, 960);

    private static Mat getFilledMat() {
        Mat mat = frameTracker.get();
        return toHSV(new Mat(mat, boardRect));
    }

    private static Mat getAllMat() {
        return frameTracker.get();
    }

    private static Rect[][] rects = new Rect[20][10];

    static {

        int unit = 48;
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 10; y++) {

                int rowStart = (unit * y);
                int colStart = ((19 - x) * unit);
                rects[x][y] = new Rect(rowStart, colStart, unit, unit);
            }
        }
    }

    private static boolean[][] getFilled(Mat mat) {

        boolean[][] filled = new boolean[20][10];

        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 10; y++) {

                Mat subMat = mat.apply(rects[x][y]);

                log.trace("检测 {} {}", x, y);
                if (isIncludeMat(subMat))
                    filled[x][y] = true;
            }
        }
        return filled;
    }

    private static Board getBoard() {

        Board board = new Board();

        board.setFilled(getFilled());
        board.setNext(getNexts());

        return board;

    }

    private static Rect bigNext = new Rect(1221, 123, 107, 52);
    private static Rect[] smallNexts = new Rect[5];

    static {

        for (int i = 0; i < 5; i++) {
            smallNexts[i] = new Rect(1223, 216 + i * (44 + 38), 89, 44);
        }
    }

    private static Piece[] getNexts() {
        Mat mat = getAllMat();
        Piece[] nextPieces = new Piece[6];
        Mat nextMat = mat.apply(bigNext);
        Piece nextPiece = bigMat2Piece(nextMat);
        nextPieces[0] = nextPiece;
        for (int i = 0; i < 5; i++) {
            Mat next = mat.apply(smallNexts[i]);
            Piece piece = mat2Piece(next);
            nextPieces[i + 1] = piece;
        }
        return nextPieces;
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        frameTracker.start();
        Thread.sleep(4000);
        play();
    }

    private static void play() throws IOException {
        Controller controller = new Controller();

        controller.put(Controller.B, 8);
        controller.put(Controller.A, 8);
        controller.put(Controller.A, 128);

        while (true) {
            if (ready(getNexts()))
                break;
        }
        log.info("already");
        Board board = getBoard();
        board.paint();
        Board nextBoard = new Board();
        nextBoard.setNext(getNexts());
        nextBoard.currentPiece = nextBoard.next[0];
        nextBoard.useNext();
        while (true) {

            Piece[] nexts = getNexts();
            if (notChanged(nextBoard.next, nexts)) {
                continue;
            }
            board = new Board();
            log.info("nexts changed");
            board.setFilled(getFilled());

            log.info("获取屏幕信息结束");
            board.setNext(nexts);
            board.paint();

            Piece[] back = board.next;
            board = correction(board, nextBoard);
            board.next = back;

            board.hold = nextBoard.hold;
            board.canUseHold = nextBoard.canUseHold;
            board.currentPiece = nextBoard.currentPiece;

            log.info("校正后");
            board.paint();

            Move move = board.get(1);
            nextBoard = board.fill(move);
            controller.exec(move);

        }
    }

    private static boolean ready(Piece[] nexts) {
        for (int i = 0; i < 6; i++) {
            if (nexts[i] == null)
                return false;
        }
        return true;
    }

    private static Board correction(Board board, Board expected) {
        int height = expected.getHeight();

        int finalX = 0;
        float sim = 0;
        for (int x = 0; x < 20 - height; x++) {

            int all = 0;
            int match = 0;

            for (int xx = 0; xx < height; xx++) {
                for (int y = 0; y < 10; y++) {
                    all++;
                    if (expected.filled[xx][y] == board.filled[xx + x][y]) {
                        match++;
                    }
                }
            }
            float f = (float) match / all;
            if (f > sim) {
                sim = f;
                finalX = x;
            }
        }


        if (finalX > 0) {
            log.info("上升了");
            for (int x = 0; x < finalX; x++) {
                if (!isHoldRow(board.filled[x])) {
                    log.info("图像不全，重新获取,高度{}", finalX);
                    board.setFilled(getFilled());
                    board.paint();
                    return correction(board, expected);

                }
            }
            log.info("图像完整");
        } else {
            log.info("没有上升");
        }
        log.info("开始修正");

        for (int x = finalX; x < 20; x++) {
            if (x - finalX < height)
                board.filled[x] = expected.filled[x - finalX].clone();
            else
                board.filled[x] = new boolean[10];
        }
        return board;
    }


    private static boolean isHoldRow(boolean[] row) {

        int n = 0;
        for (int y = 0; y < 10; y++) {
            n += row[y] ? 1 : 0;
        }
        return n == 9;
    }

    private static boolean notChanged(Piece[] next1, Piece[] next2) {


        for (int i = 0; i < 6; i++) {
            if (next2[i] == null)
                return true;
        }

        for (int i = 0; i < 6; i++) {
            if (next1[i] == null)
                return false;
        }


        for (int i = 0; i < 4; i++) {
            if (!next2[i].equals(next1[i]))
                return true;
        }
        return false;
    }


    private static Piece mat2Piece(Mat mat) {
        Piece candidate = null;
        double max = 0.95;
        for (Piece piece : pieces) {
            Mat mat1 = piece.getMat();
            SimilarPoint similarPoint = maxPointInMat(mat, mat1);
            double similarity = similarPoint.getSimilarity();
            if (piece.character() != 'O' && similarity > max || piece.character() == 'O' && similarity > 0.98) {
                max = similarity;
                candidate = piece;
            }
        }
        return candidate;
    }

    private static Piece bigMat2Piece(Mat mat) {
        Piece candidate = null;
        double max = 0.95;
        for (Piece piece : pieces) {
            SimilarPoint similarPoint = maxPointInMat(mat, piece.getBigMat());
            double similarity = similarPoint.getSimilarity();
            if (similarity > max) {
                max = similarity;
                candidate = piece;
            }
        }
        return candidate;
    }
}
