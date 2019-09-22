package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import my.mk.tetris99bot.piece.Piece;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

import static my.mk.tetris99bot.Util.*;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MJPEG;
import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_PANIC;
import static org.bytedeco.ffmpeg.global.avutil.av_log_set_level;

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

    synchronized long[] getFilled(boolean isOnlyUseGray) {
        waitFor(filledChanged);
        log.trace("getfilled start");
        Mat img = new Mat(converted, boardRect);
        log.trace("scr");
        Mat mat = toHSV(img);
        log.trace("toHSV");
        long[] filled = Util.getFilled(mat, isOnlyUseGray);
        filledChanged = false;
        log.trace("getfilled end ");
        return filled;
    }

    private void waitFor(boolean flag) {
        if (flag)
            return;
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized long[] getFilled() {
        return getFilled(false);
    }

    synchronized Piece[] getNextPieces() {
        waitFor(nextChanged);
        log.trace("getNext start");
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
        log.trace("getNext end");
        return nextPieces;
    }

    @Override
    public void run() {
        try {

            av_log_set_level(AV_LOG_PANIC);
            FrameGrabber grabber = new FFmpegFrameGrabber("video=AVerMedia Live Gamer HD 2");
            grabber.setFormat("dshow");
            grabber.setOption("video_size", "1920x1080");
            grabber.setOption("framerate", "60.0002");
            grabber.setOption("pixel_format", "yuyv422");
            grabber.setVideoCodec(AV_CODEC_ID_MJPEG);
            grabber.start();
            CanvasFrame frame = new CanvasFrame("ScreenCastingTest", 1.0);
            frame.setSize(960,540);
            frame.setLocation(0,0);
            Frame image = grabber.grab();

            converted = converterToMat.convert(image);
            long lastShow = 0;
            while (frame.isVisible()) {
                synchronized (this) {
                    log.trace("grabber start");
                    grabber.grab();
                    nextChanged = true;
                    filledChanged = true;
                    //long now = System.currentTimeMillis();
                    //log.trace("grabber end cost:{} costFromLast:{},", Timer.end(), now - lastShow);
                    //lastShow = now;
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
