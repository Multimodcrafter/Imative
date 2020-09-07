package ch.orbitsapps.imative;

public final class TimeKeeper {
    static long time = 0;
    static long initialTime = 0;
    static final boolean enabled = false;
    static void RecordStep(String text) {
        if(enabled) {
            if (time == 0) {
                time = System.currentTimeMillis();
                initialTime = time;
            } else {
                System.out.println(String.format("%s: %f", text, (System.currentTimeMillis() - time) / 1000.0));
                time = System.currentTimeMillis();
            }
        }
    }
    static void PrintTotal() {
        if(enabled) System.out.println(String.format("Total: %f", (System.currentTimeMillis() - initialTime) / 1000.0));
    }
}
