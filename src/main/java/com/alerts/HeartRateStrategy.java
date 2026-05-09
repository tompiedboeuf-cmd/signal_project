package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HeartRateStrategy implements AlertStrategy {

    private static final double PEAK_MULTIPLIER = 2.0;
    private final AlertFactory ecgFactory = new ECGAlertFactory();
    private final AlertFactory bpFactory = new BloodPressureAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String pid = String.valueOf(patient.getPatientId());

        // ECG peak detection using sliding window average
        List<PatientRecord> ecgRecords = records.stream()
                .filter(r -> r.getRecordType().equals("ECG"))
                .collect(Collectors.toList());

        for (int i = 1; i < ecgRecords.size(); i++) {
            double avg = ecgRecords.subList(0, i).stream()
                    .mapToDouble(PatientRecord::getMeasurementValue)
                    .average()
                    .orElse(0);
            if (avg > 0 && ecgRecords.get(i).getMeasurementValue() > PEAK_MULTIPLIER * avg) {
                alerts.add(ecgFactory.createAlert(pid, "ECG Abnormal Peak Alert",
                        ecgRecords.get(i).getTimestamp()));
            }
        }

        // Manual/triggered alerts from nurses or patients
        List<PatientRecord> manualAlerts = records.stream()
                .filter(r -> r.getRecordType().equals("Alert"))
                .collect(Collectors.toList());

        for (PatientRecord r : manualAlerts) {
            if (r.getMeasurementValue() == 1.0) {
                alerts.add(bpFactory.createAlert(pid, "Manual Alert Triggered", r.getTimestamp()));
            }
        }

        return alerts;
    }
}