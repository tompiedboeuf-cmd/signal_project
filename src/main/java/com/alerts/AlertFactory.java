package com.alerts;

public abstract class AlertFactory {

    /**
     * Factory method to create an Alert.
     *
     * @param patientId the ID of the patient
     * @param condition the condition that triggered the alert
     * @param timestamp the time the alert was generated
     * @return a new Alert instance
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}