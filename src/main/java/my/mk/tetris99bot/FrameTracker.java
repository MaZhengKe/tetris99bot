package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import my.mk.tetris99bot.piece.Piece;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

import static my.mk.tetris99bot.Util.*;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MJPEG;

@Slf4j
public class FrameTracker extends Thread {

    private static Rect boardRect = new Rect(720, 60, 480, 960);
    private static Rect bigNextPiece = new Rect(1222, 123, 104, 52);
    private static Rect[] smallNextPieces = new Rect[5];

    static {
        for (int i = 0; i < 5; i++) {
            smallNextPieces[i] = new Rect(1224, 216 + i * (44 + 38), 88, 44);
        }
    }

    private OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    private Mat converted;
    private boolean nextChanged = false;
    private boolean filledChanged = false;

    synchronized boolean[][] getFilled(boolean isOnlyUseGray) {
        waitFor(filledChanged);
        log.info("getfilled start");
        Timer.start();
        Mat mat = toHSV(new Mat(converted, boardRect));
        boolean[][] filled = Util.getFilled(mat, isOnlyUseGray);
        filledChanged = false;
        log.info("getfilled end cost:{}" ,Timer.end());
        return filled;
    }

    private void waitFor(boolean flag) {
        if(flag)
            return;
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized boolean[][] getFilled() {
        return getFilled(false);
    }

    synchronized Piece[] getNextPieces() {
        waitFor(nextChanged);
        log.info("getNext start");
        Timer.start();
        Piece[] nextPieces = new Piece[6];
        Mat nextMat = converted.apply(bigNextPiece);
        Piece nextPiece = bigMat2Piece(nextMat);
        nextPieces[0] = nextPiece;
        for (int i = 0; i < 5; i++) {
            Mat next = converted.apply(smallNextPieces[i]);
            Piece piece = mat2Piece(next);
            nextPieces[i + 1] = piece;
        }
        nextChanged = false;
        log.info("getNext end cost:{}",Timer.end());
        return nextPieces;
    }

    @Override
    public void run() {
        try {

            //av_log_set_level(AV_LOG_PANIC);
            FrameGrabber grabber = new FFmpegFrameGrabber("video=AVerMedia Live Gamer HD 2");
            grabber.setFormat("dshow");
            grabber.setOption("video_size", "1920x1080");
            grabber.setOption("framerate", "60.0002");
            grabber.setOption("pixel_format", "yuyv422");
            grabber.setVideoCodec(AV_CODEC_ID_MJPEG);
            grabber.start();
            CanvasFrame frame = new CanvasFrame("ScreenCastingTest", 1.0);
            Frame image = grabber.grab();
            converted = converterToMat.convert(image);
            long lastShow = 0;
            while (frame.isVisible()) {
                synchronized (this) {
                    log.info("grabber start");
                    Timer.start();
                    grabber.grab();
                    nextChanged = true;
                    filledChanged = true;
                    long now = System.currentTimeMillis();
                    log.info("grabber end cost:{} costFromLast:{},", Timer.end(), now - lastShow);
                    lastShow = now;
                    this.notify();
                }
                frame.showImage(image);
            }
            grabber.stop();
            frame.dispose();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FrameTracker tracker = new FrameTracker();
        tracker.start();
    }
}
