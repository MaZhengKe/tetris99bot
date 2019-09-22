package my.mk.ns;

public enum DPad {
    Up(0),
    UpRight(1),
    Right(2),
    DownRight(3),
    Down(4),
    DownLeft(5),
    Left(6),
    UpLeft(7),
    None(8);

    private byte value;

    public byte getValue() {
        return value;
    }

    DPad(int value) {
        this.value =(byte) value;
    }
}
