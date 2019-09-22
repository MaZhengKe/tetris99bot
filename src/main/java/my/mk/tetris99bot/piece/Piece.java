package my.mk.tetris99bot.piece;


import org.bytedeco.opencv.opencv_core.Mat;

import java.util.HashMap;
import java.util.Map;

import static my.mk.tetris99bot.Util.PATH;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

/**
 * Represents one of the pieces: O, I, S, Z, L, J, T
 */
public abstract class Piece {

    private Mat mat = imread(PATH + "/small/" + character() + ".png");
    private Mat bigMat = imread(PATH + "/big/" + character() + ".png");
    private PieceShape[] pieceShapes = shapes();


    private static final int[] ROTATION_MODULUS = {0, 1, 0, 3};
    public static final int O = 1;
    public static final int I = 2;
    public static final int S = 3;
    public static final int Z = 4;
    public static final int L = 5;
    public static final int J = 6;
    public static final int T = 7;
    private static final Map<Character, Integer> indexMap = new HashMap<>();

    static {
        indexMap.put('-', 0);
        indexMap.put('O', 1);
        indexMap.put('I', 2);
        indexMap.put('S', 3);
        indexMap.put('Z', 4);
        indexMap.put('L', 5);
        indexMap.put('J', 6);
        indexMap.put('T', 7);
        indexMap.put('x', 8);
        indexMap.put('+', 9);
    }

    public static int indexOf(char piece) {
        if (!indexMap.containsKey(piece)) {
            throw new IllegalArgumentException("Illegal board character '" + piece + "', expected: -OISZLJTx+");
        }
        return indexMap.get(piece);
    }

    public abstract byte number();

    public Mat getMat() {
        return mat;
    }

    public Mat getBigMat() {
        return bigMat;
    }

    public PieceShape[] getPieceShapes() {
        return pieceShapes;
    }

    public abstract char character();

    public int rotationsEndIndex() {
        return heights().length - 1;
    }

    public int rotationModulus() {
        return ROTATION_MODULUS[rotationsEndIndex()];
    }

    public int width(int rotation) {
        return widths()[rotation];
    }

    public int height(int rotation) {
        return heights()[rotation];
    }

    public PieceShape getShape(int rotation) {
        return pieceShapes[rotation];
    }

    protected abstract int[] widths();

    protected abstract int[] heights();

    protected abstract PieceShape[] shapes();

    @Override
    public int hashCode() {
        return number();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Piece)) {
            return false;
        }
        return ((Piece) obj).number() == number();
    }

    @Override
    public String toString() {
        return String.valueOf(character());
    }
}
