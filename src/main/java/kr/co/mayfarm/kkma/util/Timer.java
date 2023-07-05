package kr.co.mayfarm.kkma.util;

public class Timer {
    long startTime = 0L;
    long endTime = 0L;

    public Timer() {
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        this.endTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public double getInterval() {
        return (double)this.getIntervalL() / 1000.0D;
    }

    public long getIntervalL() {
        return this.startTime < this.endTime ? this.endTime - this.startTime : 0L;
    }

    public void printMsg(String msg) {
        try {
            System.out.println(msg + "::" + this.getInterval() + " seconds");
        } catch (Exception var3) {
            System.err.println("print error [" + msg + "]");
        }

    }
}
