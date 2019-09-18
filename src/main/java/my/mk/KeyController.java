package my.mk;

import my.mk.ns.*;
import my.mk.ns.Button;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.concurrent.LinkedBlockingQueue;

import static java.awt.event.KeyEvent.*;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

public class KeyController extends JFrame {
    public volatile SwitchInputState state = SwitchInputState.NONE;
    public static volatile Boolean record = false;
    public static volatile boolean isFirst = true;
    public static volatile boolean over = false;

    public static volatile LinkedBlockingQueue<ScriptFrame> queue = new LinkedBlockingQueue<>();
    private KeyDPad dPad = new KeyDPad(false, false, false, false);
    JButton btn = null;

    public KeyController() {
        setTitle("KeyController");
        setBounds(400, 400, 400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        btn = new JButton("打开");
        JButton cancel = new JButton("取消");
        btn.setBounds(0, 0, 80, 40);
        cancel.setBounds(80, 0, 80, 40);
        JTextField label = new JTextField("");
        label.setBounds(0, 40, 80, 40);
        this.add(label);
        this.setLayout(null);
        this.add(btn);
        this.add(cancel);
        cancel.addActionListener(e -> {
                    queue.clear();
                    SwitchInputState plus = new SwitchInputState(EnumSet.of(Button.Plus), DPad.None, (byte) 128, (byte) 128, (byte) 128, (byte) 128);
                    try {
                        queue.put(new ScriptFrame(3, plus.toBytes()));
                        queue.put(new ScriptFrame(3, SwitchInputState.NONE.toBytes()));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
        );
        btn.addActionListener(e -> {
//            JFileChooser chooser = new JFileChooser();
//            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
//            chooser.showDialog(new JLabel(), "选择");
//            File file = chooser.getSelectedFile();
//            if (file != null) {
            if (true) {
                try {
//                    String path = file.getPath();
                    String path = "C:\\Users\\MK\\Documents\\0903back.txt";
//                    String path = "C:\\Users\\MK\\Documents\\new.txt";

                    String[] scriptList = new String(readAllBytes(get(path))).split("\n");
                    for (int i = 1; i < scriptList.length - 1; i++) {
                        String script = scriptList[i];

                        if(script.startsWith("//")||script.trim().equals("")){
                            //System.out.println(script);
                            continue;
                        }
                        String[] strings = script.split("[\t|\r]");
                        Integer time = Integer.parseInt(strings[0]);

                        //System.out.println(time /2 +"\t" + strings[1] +"\t"+ (strings.length > 2 ? strings[2] : ""));
                        DPad dPad = DPad.valueOf(strings[1].trim());
                        String[] buttonStrs = (strings.length > 2 ? strings[2] : "").split("[ |\r]");
                        EnumSet<Button> buttons = EnumSet.noneOf(Button.class);
                        for (String buttonStr : buttonStrs) {
                            if (buttonStr.length() > 0)
                                buttons.add(Button.valueOf(buttonStr));
                        }

                        SwitchInputState state = new SwitchInputState(buttons, dPad);
                        queue.put(new ScriptFrame(time , state.toBytes()));
                    }
                    over = true;

                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }


            }
        });


        label.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) { // 按键被按下时被触发

                int code = e.getKeyCode();

                TYPE type = null;

                switch (code) {
                    case VK_UP:
                        dPad.up();
                        break;
                    case VK_LEFT:
                        dPad.left();
                        break;
                    case VK_DOWN:
                        dPad.down();
                        break;
                    case VK_RIGHT:
                        dPad.right();
                        break;
                    case VK_SPACE:
                        type = TYPE.A;
                        break;
                    case VK_D:
                        type = TYPE.B;
                        break;
                    case VK_A:
                        type = TYPE.X;
                        break;
                    case VK_SHIFT:
                        type = TYPE.Y;
                        break;
                    case VK_S:
                        type = TYPE.RB;
                        break;
                    case VK_L:
                        type = TYPE.LB;
                        break;
                    case VK_Q:
                        type = TYPE.ZL;
                        break;
                    case VK_E:
                        type = TYPE.ZR;
                        break;
                    case VK_M:
                        type = TYPE.M;
                        break;
                    case VK_P:
                        type = TYPE.P;
                        break;
                    case VK_U:
                        type = TYPE.HOME;
                        break;
                    default:
                        return;
                }

                if (type != null) {
                    type.press(state);
                } else {
                    state.setDPad(dPad.toDpad());
                }
            }

            public void keyTyped(KeyEvent e) { // 发生击键事件时被触发
                //System.out.println("此次输入的是“" + e.getKeyChar() + "”");// 获得输入的字符
            }

            public void keyReleased(KeyEvent e) { // 按键被释放时被触发

                int code = e.getKeyCode();

                TYPE type = null;

                switch (code) {
                    case VK_UP:
                        dPad.releaseUp();
                        break;
                    case VK_LEFT:
                        dPad.releaseLeft();
                        break;
                    case VK_DOWN:
                        dPad.releaseDown();
                        break;
                    case VK_RIGHT:
                        dPad.releaseRight();
                        break;
                    case VK_SPACE:
                        type = TYPE.A;
                        break;
                    case VK_D:
                        type = TYPE.B;
                        break;
                    case VK_A:
                        type = TYPE.X;
                        break;
                    case VK_SHIFT:
                        type = TYPE.Y;
                        break;
                    case VK_S:
                        type = TYPE.RB;
                        break;
                    case VK_L:
                        type = TYPE.LB;
                        break;
                    case VK_Q:
                        type = TYPE.ZL;
                        break;
                    case VK_E:
                        type = TYPE.ZR;
                        break;
                    case VK_M:
                        type = TYPE.M;
                        break;
                    case VK_P:
                        type = TYPE.P;
                        break;
                    case VK_U:
                        type = TYPE.HOME;
                        break;
                    case VK_ENTER:
                        record = !record;
                        if (!record) {
                            System.out.println("----------------录制结束 " + LocalTime.now() + "----------------");
                            isFirst = true;
                        }
                    default:
                        return;
                }

                if (type != null) {
                    type.release(state);
                } else {
                    state.setDPad(dPad.toDpad());
                }
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        KeyController controller = new KeyController();
        new ControllerLinster(controller.state);

    }
}
