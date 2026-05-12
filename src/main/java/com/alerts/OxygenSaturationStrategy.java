package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OxygenSaturationStrategy implements AlertStrategy {

    private static final double LOW_SATURATION = 92.0;
    private static final double RAPID_DROP = 5.0;
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000L;

    private final AlertFactory factory = new BloodOxygenAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String pid = String.valueOf(patient.getPatientId());

        List<PatientRecord> satRecords = records.stream()
                .filter(r -> r.getRecordType().equals("Saturation"))
                .collect(Collectors.toList());

        for (PatientRecord r : satRecords) {
            // Low saturation alert
            if (r.getMeasurementValue() < LOW_SATURATION) {
                alerts.add(factory.createAlert(pid, "Low Blood Saturation Alert", r.getTimestamp()));
            }
            // Rapid drop within 10 minutes
            for (PatientRecord earlier : satRecords) {
                if (earlier.getTimestamp() < r.getTimestamp()
                        && r.getTimestamp() - earlier.getTimestamp() <= TEN_MINUTES_MS
                        && earlier.getMeasurementValue() - r.getMeasurementValue() >= RAPID_DROP) {
                    alerts.add(factory.createAlert(pid, "Rapid Blood Saturation Drop Alert", r.getTimestamp()));
                    break;
                }
            }
        }

        return alerts;
    }
}