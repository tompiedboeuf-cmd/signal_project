package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

public interface AlertStrategy {

    /**
     * Checks patient records and returns any triggered alerts.
     *
     * @param patient the patient being evaluated
     * @param records the patient's records to analyze
     * @return a list of triggered Alert objects
     */
    List<Alert> checkAlert(Patient patient, List<PatientRecord> records);
}