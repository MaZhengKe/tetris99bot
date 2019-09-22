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

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

@Slf4j
public class Util {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Loader.load(opencv_core.class);
    }

    private static List<Block> includedMats = new ArrayList<>();
    private static String ROOT_PATH = "D:/99bot/theme/";
    private static String THEME = "Kirby";
    public static String PATH = ROOT_PATH + THEME;
    private static String PIECE_PATH = PATH + "/piece/";
    private static Block gray = new Block(imread(PIECE_PATH + "gray.png"), "gray", 25);

    static {
        includedMats.add(new Block(imread(PIECE_PATH + "Dblue.png"), "Dkblue", 7));
        includedMats.add(gray);
        includedMats.add(new Block(imread(PIECE_PATH + "Green.png"), "Green"));
        includedMats.add(new Block(imread(PIECE_PATH + "Lblue.png"), "Ltblue"));
        includedMats.add(new Block(imread(PIECE_PATH + "Purple.png"), "Purple"));
        includedMats.add(new Block(imread(PIECE_PATH + "Red.png"), "Red   ", 11));
        includedMats.add(new Block(imread(PIECE_PATH + "Yellow.png"), "Yellow"));
        includedMats.add(new Block(imread(PIECE_PATH + "Orange.png"), "Orange"));
    }

    @NotNull
    static Mat toHSV(Mat img) {
        Mat imgHSV = new Mat();
        cvtColor(img, imgHSV, Imgproc.COLOR_BGR2HSV);
        return imgHSV;
    }

    private static float matSim(Mat mat, Mat subMat, int h, int w) {
        int match = 0;

        UByteIndexer matIndexer = mat.createIndexer();
        UByteIndexer subMatIndexer = subMat.createIndexer();
        int[] matPix = new int[3];
        int[] subMatPix = new int[3];
        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                matIndexer.get(x * 4, y * 4, matPix);
                subMatIndexer.get(x * 4, y * 4, subMatPix);
                int diff = abs(matPix[0] - subMatPix[0]) + abs(matPix[1] - subMatPix[1]) + abs(matPix[2] - subMatPix[2]);
                //log.info("{} {} {}", x, y, diff);
                if (diff < 80) {
                    match++;
                }
            }
        }
        return (float) match / h / w;
    }

    private static boolean isIncludeMat(Mat mat) {
        for (Block excludedMat : includedMats) {
            double similarity = similarity(mat, excludedMat);
            if (similarity > 40)
                return true;
        }
        return false;
    }

    static Piece[] pieces = new Piece[]{new PieceO(), new PieceI(), new PieceS(), new PieceZ(), new PieceL(), new PieceJ(), new PieceT()};

    private static double similarity(Mat mat, Block block) {
        UByteIndexer indexer = mat.createIndexer();
        int[] tmp = new int[3];
        if (block.equals(gray)) {

            int all = 0;

            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    indexer.get(x * 6, y * 6, tmp);
                    int num = abs(tmp[1] - block.hsv[x][y][1]) + abs(tmp[2] - block.hsv[x][y][2]);
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

                indexer.get(x * 6, y * 6, tmp);
                if (tmp[2] < 85) {
                    all -= 2;
                    continue;
                }
                int num = abs(tmp[0] - block.hsv[x][y][0]);

                if (num < 10)
                    all++;
            }
        }

        log.trace("{}\t{}", block.colour, all);
        return all;
    }

    private static FrameTracker frameTracker = new FrameTracker();

//    private static Mat getFilledMat() {
//        Mat mat = frameTracker.get();
//        return toHSV(new Mat(mat, boardRect));
//    }

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

    static boolean[][] getFilled(Mat mat, boolean onlyUseGray) {

        boolean[][] filled = new boolean[20][10];

        if (onlyUseGray) {
            for (int x = 0; x < 20; x++) {
                for (int y = 0; y < 10; y++) {
                    Mat subMat = mat.apply(rects[x][y]);
                    log.trace("检测 {} {} 是否是gray", x, y);
                    if (similarity(subMat, gray) > 40)
                        filled[x][y] = true;
                }
            }
        } else {
            for (int x = 0; x < 20; x++) {
                for (int y = 0; y < 10; y++) {
                    Mat subMat = mat.apply(rects[x][y]);
                    log.trace("检测 {} {}", x, y);
                    if (isIncludeMat(subMat))
                        filled[x][y] = true;
                }
            }
        }

        return filled;
    }

    /**
     * @return 从当前画面识别原始的场地和NextPiece信息
     */
    private static Board getBoard() {

        Board board = new Board();
        board.setFilled(frameTracker.getFilled());
        board.setNext(frameTracker.getNextPieces());

        return board;

    }

    //static Random random = new Random();


    public static void main(String[] args) throws InterruptedException {
        frameTracker.start();
        Thread.sleep(4000);
        play();
    }

    private static void play() {
        //初始化开始游戏
        Controller controller = new Controller();
        controller.put(Controller.B, 8);
        controller.put(Controller.A, 8);
        controller.put(Controller.A, 128);

        //等待开始
        while (true) {
            if (ready(frameTracker.getNextPieces()))
                break;
        }
        log.info("already");
        //第一次查看画面
        Board board = getBoard();
        Board tmp;
        board.paint();
        //依据现有的行动预测下一步的画面
        Board nextBoard = new Board();
        nextBoard.setNext(frameTracker.getNextPieces());
        nextBoard.currentPiece = nextBoard.next[0];
        nextBoard.refreshNext();

        while (true) {

            Piece[] nextPieces = frameTracker.getNextPieces();
            if (notEqual(nextBoard.next, nextPieces)) {
                continue;
            }
            log.info("开始获取场地信息");

            board.filled = correction(frameTracker.getFilled(), nextBoard.filled, 5);
            board.next = nextPieces;
            board.hold = nextBoard.hold;
            board.currentPiece = nextBoard.currentPiece;
            log.info("场地信息获取结束");
            Move move = board.get(4);
            controller.exec(move);


            board.paintAll(move);
            board.useMove(move);
            //交换空间节省时间开销
            tmp = nextBoard;
            nextBoard = board;
            board = tmp;


        }
    }

    public static void sleep8ms() {
        try {
            Thread.sleep(8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static boolean ready(Piece[] nextPieces) {
        for (int i = 0; i < 6; i++) {
            if (nextPieces[i] == null)
                return false;
        }
        return true;
    }

    private static int getHeight(boolean[][] filled) {
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

    private static boolean[][] correction(boolean[][] board, boolean[][] expected, int numToRefresh) {

        if (numToRefresh < 0) {
            log.info("刷新场地");
            board = frameTracker.getFilled();
            numToRefresh = 5;
        }

        int height = getHeight(expected);

        int startX = 0;
        float maxSim = 0;
        boolean hasEq = false;
        for (int x = 0; x < 20 - height; x++) {

            int all = 0;
            int match = 0;

            for (int xx = 0; xx < height; xx++) {
                for (int y = 0; y < 10; y++) {
                    all++;
                    if (expected[xx][y] == board[xx + x][y]) {
                        match++;
                    }
                }
            }
            float f = (float) match / all;
            if (f == maxSim) {
                hasEq = true;
            }
            if (f > maxSim) {
                hasEq = false;
                maxSim = f;
                startX = x;
            }
        }

        if (hasEq) {
            // TODO 这样判断并不准确
            log.info("无法判断增加行数");
            board = frameTracker.getFilled();
            return correction(board, expected, numToRefresh - 1);
        }

        if (startX > 0) {
            log.trace("场地高度增加了");
            // 重新获取上升位置的场地信息，只能存在灰色方块
            board = frameTracker.getFilled(true);
            for (int x = 0; x < startX; x++) {
                if (!isGarbage(board[x])) {
                    log.info("图像不全，重新用Gray获取,高度{},刷新次数{}", startX, numToRefresh);
                    board = frameTracker.getFilled(true);
                    filling(board, expected, startX, height);
                    return correction(board, expected, numToRefresh - 1);
                }
            }
            log.trace("图像完整");
        } else {
            log.trace("没有上升");
        }
        log.trace("开始修正");

        filling(board, expected, startX, height);
        return board;
    }

    private static void filling(boolean[][] board, boolean[][] expected, int startX, int expectedHight) {
        for (int x = startX; x < 20; x++) {
            if (x - startX < expectedHight)
                board[x] = expected[x - startX].clone();
            else
                board[x] = new boolean[10];
        }
    }

    private static boolean isGarbage(boolean[] row) {
        int n = 0;
        for (int y = 0; y < 10; y++) {
            n += row[y] ? 1 : 0;
        }
        return n == 9;
    }

    private static boolean notEqual(Piece[] expected, Piece[] now) {
        for (int i = 0; i < 6; i++) {
            if (now[i] == null || (i < 4 && !now[i].equals(expected[i])))
                return true;
        }
        return false;
    }

    public static Piece mat2Piece(Mat mat) {
        Piece candidate = null;
        double max = 0.95;
        for (Piece piece : pieces) {
            Mat mat1 = piece.getMat();
            double similarity = matSim(mat, mat1, 11, 22);
            //log.info("{} {}",piece,similarity);
            if (similarity > max) {
                max = similarity;
                candidate = piece;
            }
        }
        return candidate;
    }

    public static Piece bigMat2Piece(Mat mat) {
        Piece candidate = null;
        double max = 0.95;
        //BufferedImage bufferedImage = toBufferedImage(mat);
        for (Piece piece : pieces) {
            double similarity = matSim(mat, piece.getBigMat(), 13, 26);
            //log.info("{} {}",piece,similarity);
            if (similarity > max) {
                max = similarity;
                candidate = piece;
            }
        }
        return candidate;
    }
}
