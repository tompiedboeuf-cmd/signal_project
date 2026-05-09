package com.alerts;

public class RepeatedAlertDecorator extends AlertDecorator {

    private final long repeatIntervalMs;
    private int repeatCount;

    /**
     * @param decoratedAlert   the alert to decorate
     * @param repeatIntervalMs how often (in ms) the alert should repeat
     */
    public RepeatedAlertDecorator(Alert decoratedAlert, long repeatIntervalMs) {
        super(decoratedAlert);
        this.repeatIntervalMs = repeatIntervalMs;
        this.repeatCount = 0;
    }

    // Returns the condition with repeat count appended.
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition() + " [Repeat #" + repeatCount + "]";
    }

    /**
     * Checks whether enough time has passed to repeat this alert.
     *
     * @param currentTimeMs the current time in milliseconds
     * @return true if the alert should be repeated
     */
    public boolean shouldRepeat(long currentTimeMs) {
        return currentTimeMs - decoratedAlert.getTimestamp() >= repeatIntervalMs;
    }

    // Increments the repeat counter and returns the updated count.
    public int incrementRepeat() {
        return ++repeatCount;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public long getRepeatIntervalMs() {
        return repeatIntervalMs;
    }
}