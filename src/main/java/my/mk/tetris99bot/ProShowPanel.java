package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import my.mk.ns.Button;
import my.mk.ns.DPad;
import my.mk.ns.NSInputState;
import org.bytedeco.opencv.opencv_core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedBlockingQueue;

import static my.mk.tetris99bot.Util.sleep8ms;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

@Slf4j
class ProShowPanel extends JPanel {
    private Mat switchMat = imread("D:\\99bot\\switch.png");
    private BufferedImage bufferedImage = toBufferedImage(switchMat);
    private boolean toChange = false;
    private NSInputState state = NSInputState.NONE;

    private volatile LinkedBlockingQueue<InputFrame> queue = new LinkedBlockingQueue<>();

    void  addToShow(InputFrame frame){
        try {
            queue.put(frame);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    void init() {
        new Thread(() -> {
            InputFrame inputFrame = null;
            while (true) {
                if (inputFrame != null && inputFrame.time > 0) {
                    if (toChange) {
                        toChange = false;
                        state = inputFrame.NSInputState;
                        repaint();
                    }
                    sleep8ms();
                    inputFrame.time--;
                } else {
                    InputFrame newScriptFrame = queue.poll();
                    if (newScriptFrame != null) {
                        log.trace("got to show,{}",newScriptFrame);
                        inputFrame = newScriptFrame;
                        toChange = true;
                    } else {
                        sleep8ms();
                    }
                }
            }
        }).start();
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);
        graphics.drawImage(bufferedImage, 0, 0, Color.GREEN, this);

        graphics.setColor(Color.BLUE);
        if (state.buttons.contains(Button.A)) {
            graphics.fillOval(719, 156, 60, 60);
        }
        if (state.buttons.contains(Button.X)){
            graphics.fillOval(650, 96, 60, 60);
        }
        if (state.buttons.contains(Button.L)){
            graphics.fillRect(208, 2, 60, 40);
        }

        if(state.dPad.equals(DPad.Left)){
            graphics.fillRect(227, 284, 60, 50);
        }
        else if(state.dPad.equals(DPad.Right)){
            graphics.fillRect(325, 284, 60, 50);
        }
        else if(state.dPad.equals(DPad.Up)){
            graphics.fillRect(282, 230, 60, 50);
        }
    }
}
