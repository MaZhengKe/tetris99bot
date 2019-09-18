package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import my.mk.ns.*;
import my.mk.tetris99bot.piece.Piece;
import my.mk.tetris99bot.piece.PieceShape;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Slf4j
public class Controller {

    public volatile SwitchInputState state = SwitchInputState.NONE;

    ControllerLinster controllerLinster = new ControllerLinster(state);
    //Socket socket = new Socket("localhost", 31337);
    //OutputStream outputStream = socket.getOutputStream();

    public Controller() throws IOException {

    }

    public static SwitchInputState A = new SwitchInputState(Button.A);
    public static SwitchInputState B = new SwitchInputState(Button.B);
    public static SwitchInputState X = new SwitchInputState(Button.X);
    public static SwitchInputState ZL = new SwitchInputState(Button.L);
    public static SwitchInputState Down = new SwitchInputState(DPad.Down);
    public static SwitchInputState Up = new SwitchInputState(DPad.Up);
    public static SwitchInputState Left = new SwitchInputState(DPad.Left);
    public static SwitchInputState Right = new SwitchInputState(DPad.Right);

    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();
        controller.put(Up);
        controller.put(Up);
        controller.put(Up);
        controller.put(Up);
        controller.put(Up);
    }

    void put(SwitchInputState state) {
        put(state,4);
    }

    void put(SwitchInputState state,int time) {
        try {
            byte[] bytes = state.toBytes();
            controllerLinster.queue.put(new ScriptFrame(time, bytes));
            controllerLinster.queue.put(new ScriptFrame(4, SwitchInputState.NONE.toBytes()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void exec(Move move, boolean isHold) {

        Piece piece = move.piece;
        if (isHold) {
            put(ZL);
        }
        List<Button> xuan = new ArrayList<>();
        List<DPad> movedY = new ArrayList<>();

        int ni = piece.rotationsEndIndex() + 1 - move.xun;
        if (ni < move.xun) {
            for (int i = 0; i < ni; i++) {
                xuan.add(Button.A);
                //put(A);
            }
        } else {
            for (int i = 0; i < move.xun; i++) {
                xuan.add(Button.X);
                //put(X);
            }
        }

        PieceShape shape = piece.getShape(move.xun);
        int y = shape.getY();
        if (y > move.y) {
            for (int i = 0; i < y - move.y; i++) {
                movedY.add(DPad.Left);
                //put(Left);
            }
        } else {
            for (int i = 0; i < move.y - y; i++) {
                movedY.add(DPad.Right);
                //put(Right);
            }
        }


        int max = Math.max(xuan.size(), movedY.size());
        for (int i = 0; i < max; i++) {
            if(i<xuan.size() && i<movedY.size()){
                put(new SwitchInputState(xuan.get(i) , movedY.get(i)));
            }
            else if(i<xuan.size()&&i>=movedY.size()){
                put(new SwitchInputState(xuan.get(i)));
            }
            else {
                put(new SwitchInputState(movedY.get(i)));
            }
        }
        put(Up);
        log.info("\n" + move.toString());
    }
}
