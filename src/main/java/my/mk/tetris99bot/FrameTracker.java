package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MJPEG;
import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_PANIC;
import static org.bytedeco.ffmpeg.global.avutil.av_log_set_level;

@Slf4j
public class FrameTracker extends Thread {

    private OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    private Mat converted;

    synchronized Mat get() {
        return converted.clone();
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
            Frame image = grabber.grab();
            converted = converterToMat.convert(image);
            while (frame.isVisible()) {
                synchronized (this) {
                    grabber.grab();
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
