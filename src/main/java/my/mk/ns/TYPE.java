package my.mk.ns;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public enum TYPE {
    A(Button.A),
    B(Button.B),
    X(Button.X),
    Y(Button.Y),

    RB(Button.R),
    LB(Button.L),
    ZL(Button.ZL),
    ZR(Button.ZR),

    L3(Button.L3),
    R3(Button.R3),
    M(Button.Minus),
    P(Button.Plus),
    HOME(Button.Home),

    U(state -> state.dPad = DPad.Up),
    D(state -> state.dPad = DPad.Down),
    R(state -> state.dPad = DPad.Right),
    L(state -> state.dPad = DPad.Left),

    RU(pressStick((byte) 128, (byte) 128, (byte) 128, (byte) 0)),
    RD(pressStick((byte) 128, (byte) 128, (byte) 128, (byte) 255)),
    RL(pressStick((byte) 128, (byte) 128, (byte) 0, (byte) 128)),
    RR(pressStick((byte) 128, (byte) 128, (byte) 255, (byte) 128)),

    LU(pressStick((byte) 128, (byte) 0, (byte) 128, (byte) 128)),
    LD(pressStick((byte) 128, (byte) 255, (byte) 128, (byte) 128)),
    LL(pressStick((byte) 0, (byte) 128, (byte) 128, (byte) 128)),
    LR(pressStick((byte) 255, (byte) 128, (byte) 128, (byte) 128)),
    ILLEGAL(state -> {
    });

    private static Consumer<NSInputState> pressButton(Button button) {
        return state -> state.buttons.add(button);
    }

    private static Consumer<NSInputState> releaseButton(Button button) {
        return state -> state.buttons.remove(button);
    }

    private static Consumer<NSInputState> pressStick(byte LeftX, byte LeftY, byte RightX, byte RightY) {
        return state -> {
            state.leftX = LeftX;
            state.leftY = LeftY;
            state.rightX = RightX;
            state.rightY = RightY;
        };
    }

    private static Map<String, TYPE> cache = new HashMap<>();
    Consumer<NSInputState> press;
    Consumer<NSInputState> release;

    static {
        cache.put("A", A);
        cache.put("B", B);
        cache.put("X", X);
        cache.put("Y", Y);
        cache.put("RB", RB);
        cache.put("LB", LB);
        cache.put("ZL", ZL);
        cache.put("ZR", ZR);
        cache.put("L3", L3);
        cache.put("R3", R3);
        cache.put("M", M);
        cache.put("P", P);
        cache.put("U", U);
        cache.put("D", D);
    }

    TYPE(Consumer<NSInputState> press) {
        this.press = press;
    }
    TYPE(Button button) {
        this(pressButton(button), releaseButton(button));
    }

    TYPE(Consumer<NSInputState> press, Consumer<NSInputState> release) {
        this.press = press;
        this.release = release;
    }

    public void press(NSInputState state) {
        press.accept(state);
    }
    public void release(NSInputState state) {
        release.accept(state);
    }

    public static TYPE parse(String str) {
        switch (str) {
            case "W":
                return TYPE.U;
            case "A":
                return TYPE.L;
            case "S":
                return TYPE.D;
            case "D":
                return TYPE.R;
            case "Q":
                return TYPE.A;
            case "E":
                return TYPE.B;
        }
        try {
            return TYPE.valueOf(str);
        } catch (Throwable throwable) {
            return ILLEGAL;
        }
    }
}
