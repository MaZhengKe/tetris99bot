package my.mk.ns;

import lombok.Data;
import my.mk.tetris99bot.InputFrame;

import java.util.EnumSet;

@Data
public class NSInputState {

    public final static NSInputState NONE = new NSInputState(EnumSet.noneOf(Button.class), DPad.None, (byte) 128, (byte) 128, (byte) 128, (byte) 128);
    public final static byte[] NONE_BYTES = NONE.toBytes();

    static {
        NONE.cache = NONE.toBytes();
    }

    public EnumSet<Button> buttons;
    public DPad dPad;
    public int leftX;
    public int leftY;
    public int rightX;
    public int rightY;
    public byte[] cache;

    public NSInputState(EnumSet<Button> buttons, DPad dPad, int leftX, int leftY, int rightX, int rightY) {
        this.buttons = buttons;
        this.dPad = dPad;
        this.leftX = leftX;
        this.leftY = leftY;
        this.rightX = rightX;
        this.rightY = rightY;
    }

    public NSInputState(EnumSet<Button> buttons, DPad dPad) {
        this(buttons, dPad, 128, 128, 128, 128);
    }

    public NSInputState(EnumSet<Button> buttons) {
        this(buttons, DPad.None, 128, 128, 128, 128);
    }

    public NSInputState(Button button) {
        this(EnumSet.of(button), DPad.None, 128, 128, 128, 128);
    }

    public NSInputState(Button button, DPad dPad) {
        this(EnumSet.of(button), dPad, 128, 128, 128, 128);
    }

    public NSInputState(DPad dPad) {
        this(EnumSet.noneOf(Button.class), dPad, 128, 128, 128, 128);
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NSInputState)) {
            return false;
        }
        NSInputState other = (NSInputState) obj;
        if (other.dPad != dPad)
            return false;
        if (buttons.size() != other.buttons.size())
            return false;
        return other.buttons.containsAll(buttons);
    }

    @Override
    public int hashCode() {
        int result = dPad.hashCode();
        for (Button button : buttons) {
            result = 31 * result + button.hashCode();
        }
        return result;
    }

    public byte[] toBytes() {

        byte[] buf = new byte[9];
        int buttonByte = 0;
        for (Button button : buttons) {
            buttonByte |= button.getValue();
        }


        buf[0] = (byte) ((buttonByte & 0xFF00) >> 8);
        buf[1] = (byte) (buttonByte & 0xFF);
        buf[2] = dPad.getValue();
        buf[3] = (byte) leftX;
        buf[4] = (byte) leftY;
        buf[5] = (byte) rightX;
        buf[6] = (byte) rightY;
        buf[7] = 0;
        buf[8] = CalculateCrc8(buf, 0, buf.length - 1);
        return buf;
    }


    private static byte CalculateCrc8(byte[] data, int off, int len) {
        byte output = 0;
        for (int i = off; i < len; i++) {
            output ^= data[i];
            for (int j = 0; j < 8; j++) {
                if ((output & 0x80) != 0) {
                    output <<= 1;
                    output ^= 0x07;
                } else {
                    output <<= 1;
                }
            }
        }

        return output;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(dPad).append("\t");

        for (Button button : buttons) {
            str.append(button).append(" ");
        }
        if (leftX == (byte) 0)
            str.append("左摇杆向左");
        if (leftX == (byte) 255)
            str.append("左摇杆向右");
        if (leftY == (byte) 0)
            str.append("左摇杆向上");
        if (leftY == (byte) 255)
            str.append("左摇杆向下");
        if (rightX == (byte) 0)
            str.append("右摇杆向左");
        if (rightX == (byte) 255)
            str.append("右摇杆向右");
        if (rightY == (byte) 0)
            str.append("右摇杆向上");
        if (rightY == (byte) 255)
            str.append("右摇杆向下");
        String s = str.toString();

        return s.equals(" ") ? "Nothing" : s;
    }
}
