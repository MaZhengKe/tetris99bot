package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import my.mk.tetris99bot.piece.*;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static my.mk.tetris99bot.Controller.ZL;
import static org.opencv.imgproc.Imgproc.*;

@Slf4j
public class Util {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final Robot robot = initRobot();
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final Rectangle screenRectangle = new Rectangle(screenSize);


    private static Robot initRobot() {
        System.out.println("init robot");
        try {
            return new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    private static Mat bufImg2Mat(BufferedImage image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mat img = Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        return toHSV(img);

    }

    @NotNull
    public static Mat toHSV(Mat img) {
        Mat imgHSV = new Mat(img.rows(), img.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(img, imgHSV, Imgproc.COLOR_BGR2HSV);
        return imgHSV;
    }

    private static BufferedImage mat2BufImg(Mat matrix) {
        String fileExtension = ".png";
        // convert the matrix into a matrix of bytes appropriate for
        // this file extension
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        // convert the "matrix of bytes" into a byte array
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }

    public static List<BufferedImage> mats2BufImgs(List<Mat> mats) {
        return mats.stream().map(Util::mat2BufImg).collect(Collectors.toList());
    }

    private static Mat mat2GrayMat(Mat mat) {
        Mat grayMat = new Mat();
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY);
        return grayMat;
    }


    public static Mat mat2ThresholdMat(Mat mat) {
        Mat grayMat = Util.mat2GrayMat(mat);
        Mat thresholdMat = new Mat();
        Imgproc.adaptiveThreshold(grayMat, thresholdMat, 255, ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 11);
        return thresholdMat;

    }

    private static SimilarPoint maxPointInMat(Mat mat, Mat subMat) {
        Mat result = new Mat();
        Imgproc.matchTemplate(mat, subMat, result, Imgproc.TM_CCORR_NORMED);
        Core.MinMaxLocResult mmlr = Core.minMaxLoc(result);
        Point maxLoc = mmlr.maxLoc;
        double v = result.get((int) maxLoc.y, (int) maxLoc.x)[0];
        return new SimilarPoint(maxLoc, v);
    }


    private static List<Mat> excludedMats = new ArrayList<>();
    private static List<Block> includedMats = new ArrayList<>();

    private static  Block gray = new Block(Imgcodecs.imread("D:/99bot/gray.png"), "gray", 25);
    static {
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn1.png"));
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn2.png"));
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn3.png"));
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn4.png"));
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn5.png"));
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn6.png"));
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn7.png"));
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn8.png"));
        excludedMats.add(Imgcodecs.imread("D:/test/sub/zn9.png"));

        includedMats.add(new Block(Imgcodecs.imread("D:/99bot/darkblue.png"), "dkblue", 7));
        includedMats.add(gray);
        includedMats.add(new Block(Imgcodecs.imread("D:/99bot/green.png"), "green"));
        includedMats.add(new Block(Imgcodecs.imread("D:/99bot/lightblue.png"), "ltblue"));
        includedMats.add(new Block(Imgcodecs.imread("D:/99bot/purple.png"), "purple"));
        includedMats.add(new Block(Imgcodecs.imread("D:/99bot/red.png"), "red   ", 11));
        includedMats.add(new Block(Imgcodecs.imread("D:/99bot/yellow.png"), "yellow"));
        includedMats.add(new Block(Imgcodecs.imread("D:/99bot/orange.png"), "orange"));
    }


    private static boolean isIncludeMat(Mat mat) {
        long maxSim = -1;
        for (Block excludedMat : includedMats) {
            double similarity = similarity(mat, excludedMat);
            if (similarity > 40)
                return true;
        }
        return false;
    }

    private static Piece[] pieces = new Piece[]{new PieceO(), new PieceI(), new PieceS(), new PieceZ(), new PieceL(), new PieceJ(), new PieceT()};

    private static double similarity(Mat mat, Block block) {

        if(block.equals(gray)){

            int all = 0;

            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {

                    double[] doubles1 = mat.get(x * 6, y * 6);
                    double[] doubles2 = block.mat.get(x * 6, y * 6);

                    int num = (int)Math.abs(doubles1[1] - doubles2[1]) + (int)Math.abs(doubles1[2] - doubles2[2]);

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

                double[] doubles1 = mat.get(x * 6, y * 6);


                double[] doubles2 = block.mat.get(x * 6, y * 6);

                if (doubles1[2] < 85){
                    all-=2;
                    continue;
                }

                int num = (int)Math.abs(doubles1[0] - doubles2[0]);

                if (num < 10)
                    all++;
            }
        }


        //float v = (float) all / sum;
        log.trace("{}\t{}", block.colour, all);

        return all;

    }

    private static boolean isStar(double[] doubles1) {
        return false;
    }


    private static boolean isFilled(Mat mat) {
        return isIncludeMat(mat);
        //return !isExcludedMat(mat);
    }


    private static boolean[][] getFilled() {
        Mat mat = getFilledMat();

        return getFilled(mat);
    }

    @NotNull
    private static Mat getBoardMat() {
        BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(720, 62, 605, 960));
        return bufImg2Mat(screenCapture);
    }

    private static Mat getFilledMat() {
        BufferedImage filledCapture = robot.createScreenCapture(new Rectangle(720, 62, 480, 960));
        return bufImg2Mat(filledCapture);
    }

    private static Mat getNextsMat() {

        BufferedImage nextsCapture = robot.createScreenCapture(new Rectangle(1210, 106, 115, 522));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(nextsCapture, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mat img = Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        return img;
    }

    private static boolean[][] getFilled(Mat mat) {

        boolean[][] filled = new boolean[20][10];
        int unit = 48;

        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 10; y++) {

                int rowStart = (unit * y);
                int colStart = ((19 - x) * unit);
                Mat subMat = mat.submat(colStart, colStart + unit, rowStart, rowStart + unit);

                log.trace("检测 {} {}", x, y);
                if (isFilled(subMat))
                    filled[x][y] = true;
            }
        }
        return filled;
    }

    private static Board getBoard() {

        Board board = new Board();

        board.setFilled(getFilled());
        board.setNext(getNexts());

//        不需要用识别的方式去读
//        Mat hold = mat.subMat(110, 595, 226, 710);
//        Piece holdPiece = bigMat2Piece(hold);
//        board.setHold(holdPiece);

        return board;

    }

    private static Piece[] getNexts() {
        Mat mat = getNextsMat();
        return getNexts(mat);
    }


    private static Piece[] getNexts(Mat mat) {

        Piece[] nextPieces = new Piece[6];
        Mat nextPiecesMat = mat.submat(93, 93 + 429, 0, 115);
        Mat nextMat = mat.submat(0, 93, 0, 115);


        Piece nextPiece = bigMat2Piece(nextMat);
        nextPieces[0] = nextPiece;

        int unit = 82;
        for (int i = 0; i < 5; i++) {
            Mat next = nextPiecesMat.submat(i * unit, i * unit + unit, 0, 115);
            Piece piece = mat2Piece(next);
            nextPieces[i + 1] = piece;
        }
        return nextPieces;
    }

    public static void main(String[] args) throws IOException {


//        test();
        play();


    }

    private static void test() {
        long l = System.currentTimeMillis();
        Board board1 = getBoard();
        System.out.println(System.currentTimeMillis() - l);
        l = System.currentTimeMillis();
        board1 = getBoard();
        System.out.println(System.currentTimeMillis() - l);
        board1.paint();
    }

    private static void play() throws IOException {
        Controller controller = new Controller();

        controller.put(Controller.B, 8);
        controller.put(Controller.A, 8);
        controller.put(Controller.A, 128);

        Board board;
        Board last = getBoard();
        last.paint();
        Piece hold = null;
        boolean isClean = false;

        while (true) {
            Piece[] nexts = getNexts();

            if (notChanged(last.next, nexts)) {
                continue;
            }

            if (isClean) {
                try {
                    log.info("sleep");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("changed");
            board = getBoard();
            log.info("获取屏幕信息结束");

            if (notChanged(last.next, board.next)) {
                continue;
            }

//            boolean[][] checkFilled = getFilled();
//            log.info("获取校验信息结束");
//
//            for (int x = 0; x < 20; x++) {
//                for (int y = 0; y < 10; y++) {
//                    if (checkFilled[x][y])
//                        board.fill(x, y);
//                }
//            }


            Move holdMove = null;
            if (hold != null) {
                log.info("开始计算holdValue");
                holdMove = board.getMove(hold,3);
                log.info("结束计算holdValue");
            }

            Piece piece = last.next[0];
            Move pieceMove;

            if (piece != null) {
                log.info("开始计算value");
                pieceMove = board.getMove(piece,5);
                log.info("结束计算value");
                if (holdMove == null) {
                    Piece nextPiece = board.next[0];
                    Move nextMove = board.getMove(nextPiece,5);
                    if (nextMove.value.v > pieceMove.value.v) {
                        hold = piece;
                        //controller.exec("lb");
                        controller.put(ZL);
                        log.info("init hold");
                    } else {
                        controller.exec(pieceMove, false);
                        isClean = pieceMove.value.isClean;
                    }
                } else {
                    if (pieceMove.value.v < holdMove.value.v) {
                        hold = piece;
                        controller.exec(holdMove, true);
                        isClean = holdMove.value.isClean;
                    } else {
                        controller.exec(pieceMove, false);
                        isClean = pieceMove.value.isClean;
                    }
                }
            }

            board.paint();
            last = board;
        }
    }

    private static boolean notChanged(Piece[] next1, Piece[] next2) {


        for (int i = 0; i < 6; i++) {
            if (next2[i] == null)
                return true;
        }
        for (int i = 0; i < 6; i++) {
            if (!next2[i].equals(next1[i]))
                return false;
        }
        return true;
    }


    private static Piece mat2Piece(Mat mat) {
        Piece candidate = null;
        double max = 0.95;
        for (Piece piece : pieces) {
            Mat mat1 = piece.getMat();
            SimilarPoint similarPoint = maxPointInMat(mat, mat1);
            double similarity = similarPoint.getSimilarity();
            if (similarity > max) {
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
