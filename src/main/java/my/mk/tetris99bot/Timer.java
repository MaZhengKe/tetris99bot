package my.mk.tetris99bot;

public class Timer {
    private static  long ll;

    public static  void start(){
        ll = System.currentTimeMillis();
    }

    public static long end(){
        return System.currentTimeMillis() - ll;
    }


    long l;

    public Timer() {
        l = System.currentTimeMillis();
    }

    public long cost() {
        return System.currentTimeMillis() - l;
    }
}
