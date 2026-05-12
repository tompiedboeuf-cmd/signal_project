package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HypotensiveHypoxemiaStrategy implements AlertStrategy {

    private static final double SYSTOLIC_LOW = 90.0;
    private static final double SAT_LOW = 92.0;

    private final AlertFactory factory = new BloodOxygenAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String pid = String.valueOf(patient.getPatientId());

        boolean lowSystolic = records.stream()
                .filter(r -> r.getRecordType().equals("SystolicPressure"))
                .anyMatch(r -> r.getMeasurementValue() < SYSTOLIC_LOW);

        boolean lowSaturation = records.stream()
                .filter(r -> r.getRecordType().equals("Saturation"))
                .anyMatch(r -> r.getMeasurementValue() < SAT_LOW);

        if (lowSystolic && lowSaturation) {
            long timestamp = records.stream()
                    .mapToLong(PatientRecord::getTimestamp)
                    .max()
                    .orElse(System.currentTimeMillis());
            alerts.add(factory.createAlert(pid, "Hypotensive Hypoxemia Alert", timestamp));
        }

        return alerts;
    }
}