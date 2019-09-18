package my.mk.ns;

public enum DPad {
    Up((byte)0),
    UpRight((byte)1),
    Right((byte)2),
    DownRight((byte)3),
    Down((byte)4),
    DownLeft((byte)5),
    Left((byte)6),
    UpLeft((byte)7),
    None((byte)8);

    private byte value;

    public byte getValue() {
        return value;
    }

    DPad(byte value) {
        this.value = value;
    }
}
