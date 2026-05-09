package com.alerts;

public class PriorityAlertDecorator extends AlertDecorator {

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    private final Priority priority;

    /**
     * @param decoratedAlert the alert to decorate
     * @param priority       the urgency level to attach
     */
    public PriorityAlertDecorator(Alert decoratedAlert, Priority priority) {
        super(decoratedAlert);
        this.priority = priority;
    }

    // Returns the condition with priority tag prepended

    @Override
    public String getCondition() {
        return "[" + priority.name() + "] " + decoratedAlert.getCondition();
    }

    public Priority getPriority() {
        return priority;
    }
}