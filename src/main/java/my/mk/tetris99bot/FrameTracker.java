package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;

@Slf4j
public class FrameTracker extends Thread {
    private Mat back = null;

    private OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    private Mat converted;

    synchronized Mat get() {
        log.trace("get");
        return back.clone();
    }

    private synchronized void set() {
        log.trace("set");
        back = converted.clone();
    }

    @Override
    public void run() {
        try {
            FrameGrabber grabber = new FFmpegFrameGrabber("video=AVerMedia Live Gamer HD 2");
            grabber.setOption("loglevel", "quiet");
            grabber.setOption("hide_banner", " ");

            grabber.setFormat("dshow");
            grabber.setOption("video_size", "1920x1080");
            grabber.setOption("framerate", "60");
            grabber.setOption("pixel_format", "yuyv422");
            grabber.start();
            CanvasFrame frame = new CanvasFrame("ScreenCastingTest", 1.0);
            converted = converterToMat.convert(grabber.grab());
            while (frame.isVisible()) {
                Frame f = grabber.grab();
                set();
                frame.showImage(f);
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
