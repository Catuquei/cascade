package cascade.util.misc;

import cascade.util.Util;

public class Timer implements Util {

    private long time = -1L;
    private final long current;
    long startTime;
    long delay;
    boolean paused;

    public Timer() {
        this.startTime = System.currentTimeMillis();
        this.delay = 0L;
        this.paused = false;
        current = -1;
    }

    public final boolean hasReached(final long delay) {
        return System.currentTimeMillis() - this.current >= delay;
    }

    public boolean passedS(double s) {
        return this.getMs(System.nanoTime() - this.time) >= (long) (s * 1000.0);
    }

    public boolean passedM(double m) {
        return this.getMs(System.nanoTime() - this.time) >= (long) (m * 1000.0 * 60.0);
    }

    public boolean passedDms(double dms) {
        return this.getMs(System.nanoTime() - this.time) >= (long) (dms * 10.0);
    }

    public boolean passedDs(double ds) {
        return this.getMs(System.nanoTime() - this.time) >= (long) (ds * 100.0);
    }

    public boolean passedNS(long ns) {
        return System.nanoTime() - this.time >= ns;
    }

    public void setMs(long ms) {
        this.time = System.nanoTime() - ms * 1000000L;
    }

    public boolean isPassed() {
        return !this.paused && System.currentTimeMillis() - this.startTime >= this.delay;
    }

    public void setDelay(final long delay) {
        this.delay = delay;
    }

    public void setPaused(final boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public long getTime() {
        return this.time;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean passedMs(long ms) {
        return getMs(System.nanoTime() - time) >= ms;
    }

    public long getPassedTimeMs() {
        return getMs(System.nanoTime() - time);
    }

    public void reset() {
        time = System.nanoTime();
    }

    public long getMs(long time) {
        return time / 1000000L;
    }

    //man it only do onupdate for modules dum dum
    /*public void onUpdate() {
        tick++;
    }

    public boolean passedTicks(int ticks) {
        return (tick >= ticks);
    }

    public void resetTicks() {
        tick = 0;
    }*/
}