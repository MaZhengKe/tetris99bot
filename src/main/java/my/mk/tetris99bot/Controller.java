package my.mk.tetris99bot;

import lombok.extern.slf4j.Slf4j;
import my.mk.ns.*;
import my.mk.tetris99bot.piece.Piece;
import my.mk.tetris99bot.piece.PieceShape;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static my.mk.tetris99bot.Util.pieces;

@Slf4j
public class Controller extends JFrame {
    ProShowPanel proShowPanel;

    private StateSender stateSender = new StateSender();

    Controller() {
        proShowPanel = new ProShowPanel();
        this.add(proShowPanel);
        this.setTitle("Controller");
        this.setLocation(0, 0);
        this.setSize(900, 650);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        proShowPanel.init();
        initCache();
    }

    static NSInputState A = new NSInputState(Button.A);
    static NSInputState B = new NSInputState(Button.B);
    private static NSInputState X = new NSInputState(Button.X);
    static NSInputState ZL = new NSInputState(Button.L);
    public static NSInputState Down = new NSInputState(DPad.Down);
    private static NSInputState Up = new NSInputState(DPad.Up);
    public static NSInputState Left = new NSInputState(DPad.Left);
    public static NSInputState Right = new NSInputState(DPad.Right);

    public static void main(String[] args) throws IOException, InterruptedException {

        FrameTracker tracker = new FrameTracker();
        tracker.start();
        Controller controller = new Controller();
        while (true) {
            controller.put(Up, 4);
            controller.put(Left, 4);
            controller.put(Right, 4);
            controller.put(A, 4);
            controller.put(B, 4);
            Thread.sleep(2000);
        }
    }

    private void put(NSInputState state) {
        put(state, 4);
    }

//    private void put2(NSInputState state) {
//        put2(state, 4);
//    }

    void put(NSInputState state, int time) {

        byte[] bytes = state.toBytes();
        stateSender.addToSend(new ScriptFrame(time, bytes));
        stateSender.addToSend(new ScriptFrame(4, NSInputState.NONE.toBytes()));
        proShowPanel.addToShow(new InputFrame(state, time));
        proShowPanel.addToShow(new InputFrame(NSInputState.NONE, time));

    }

    void put2(NSInputState state, int time) {

        log.info("put2 {} start", state.dPad);
        byte[] bytes = state.toBytes();
        stateSender.addToSend(new ScriptFrame(time, bytes));
        proShowPanel.addToShow(new InputFrame(state, time));

        log.info("put2 {} over", state.dPad);

    }

    private HashMap<Move.M, List<NSInputState>> cache = new HashMap<>();


    //TODO 这里的获取逻辑太奇葩了
    private void initCache() {
        boolean[] allBoolean = new boolean[]{false, true};
        for (boolean isUseHold : allBoolean) {
            for (Piece piece : pieces) {
                for (PieceShape pieceShape : piece.getPieceShapes()) {
                    for (int y = 0; y < 10; y++) {

                        List<Button> rotate = new ArrayList<>();
                        List<DPad> moving = new ArrayList<>();
                        List<NSInputState> list = new ArrayList<>();
                        if (isUseHold) {
                            list.add(ZL);
                            list.add(NSInputState.NONE);
                        }

                        int rotateIndex = pieceShape.getRotateIndex();
                        int ni = piece.rotationsEndIndex() + 1 - rotateIndex;
                        int startY = pieceShape.getY();

                        if (piece.rotationsEndIndex() == 1 && rotateIndex == 1 && y > startY) {
                            rotate.add(Button.A);
                            for (int i = 0; i < y - startY - 1; i++) {
                                moving.add(DPad.Right);
                            }
                        }else {
                            if (ni < rotateIndex) {
                                for (int i = 0; i < ni; i++) {
                                    rotate.add(Button.A);
                                }
                            } else {
                                for (int i = 0; i < rotateIndex; i++) {
                                    rotate.add(Button.X);
                                }
                            }

                            if (startY > y) {
                                for (int i = 0; i < startY - y; i++) {
                                    moving.add(DPad.Left);
                                }
                            } else {
                                for (int i = 0; i < y - startY; i++) {
                                    moving.add(DPad.Right);
                                }
                            }
                        }

                        int max = Math.max(rotate.size(), moving.size());
                        for (int i = 0; i < max; i++) {
                            if (i < rotate.size() && i < moving.size()) {
                                list.add(new NSInputState(rotate.get(i), moving.get(i)));
                                list.add(NSInputState.NONE);
                            } else if (i < rotate.size()) {
                                list.add(new NSInputState(rotate.get(i)));
                                list.add(NSInputState.NONE);
                            } else {
                                list.add(new NSInputState(moving.get(i)));
                                list.add(NSInputState.NONE);
                            }
                        }
                        list.add(Up);
                        list.add(NSInputState.NONE);

                        for (NSInputState nsInputState : list) {
                            nsInputState.cache = nsInputState.toBytes();
                        }
                        Move.M m = new Move.M(piece, y, rotateIndex, isUseHold);
                        cache.put(m, list);
                    }
                }
            }
        }
    }

    void exec(Move move) {
        List<NSInputState> list = cache.get(move.m);
        for (NSInputState nsInputState : list) {
            stateSender.addToSend(new ScriptFrame(4, nsInputState.cache));
            proShowPanel.addToShow(new InputFrame(nsInputState, 4));
        }
        log.info("命令缓存完成\n" + move.toString() + "\n");
    }
}
