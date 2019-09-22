package my.mk.ns;

import lombok.extern.slf4j.Slf4j;
import my.mk.serial.ParamConfig;
import my.mk.serial.SerialPortUtils;

import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class StateSender {
    private static SerialPortUtils serialPort;

    private volatile LinkedBlockingQueue<ScriptFrame> queue = new LinkedBlockingQueue<>();

    public void addToSend(ScriptFrame frame){
        try {
            queue.put(frame);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public StateSender() {
        serialPort = new SerialPortUtils();
        ParamConfig paramConfig = new ParamConfig("COM8", 19200, 0, 8, 1);
        serialPort.init(paramConfig);
        Thread thread = new Thread(() -> {
            try {
                ScriptFrame scriptFrame = null;

                while (true) {
                    if (scriptFrame != null && scriptFrame.time > 0) {
                        serialPort.send(scriptFrame.bytes);
                        scriptFrame.time--;
                    } else {
                        ScriptFrame newScriptFrame = queue.poll();
                        if (newScriptFrame != null) {
                            log.trace("got to send,{}",newScriptFrame.bytes);
                            scriptFrame = newScriptFrame;

                            serialPort.send(scriptFrame.bytes);
                            scriptFrame.time--;
                        }else {
                            serialPort.send( NSInputState.NONE.toBytes());
                        }
                    }
                }
            } catch (Throwable ignore) {
            }
        });

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }
}
