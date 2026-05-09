package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BloodPressureStrategy implements AlertStrategy {

    private static final double SYSTOLIC_HIGH = 180.0;
    private static final double SYSTOLIC_LOW = 90.0;
    private static final double DIASTOLIC_HIGH = 120.0;
    private static final double DIASTOLIC_LOW = 60.0;
    private static final double TREND_DELTA = 10.0;

    private final AlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String pid = String.valueOf(patient.getPatientId());

        checkType(pid, records, "SystolicPressure", SYSTOLIC_HIGH, SYSTOLIC_LOW,
                "Critical High Systolic", "Critical Low Systolic",
                "Systolic Increasing Trend", "Systolic Decreasing Trend", alerts);

        checkType(pid, records, "DiastolicPressure", DIASTOLIC_HIGH, DIASTOLIC_LOW,
                "Critical High Diastolic", "Critical Low Diastolic",
                "Diastolic Increasing Trend", "Diastolic Decreasing Trend", alerts);

        return alerts;
    }

    private void checkType(String pid, List<PatientRecord> all,
            String type, double high, double low,
            String highLabel, String lowLabel,
            String incLabel, String decLabel,
            List<Alert> alerts) {

        List<PatientRecord> typed = all.stream()
                .filter(r -> r.getRecordType().equals(type))
                .collect(Collectors.toList());

        // Critical thresholds
        for (PatientRecord r : typed) {
            if (r.getMeasurementValue() > high) {
                alerts.add(factory.createAlert(pid, highLabel, r.getTimestamp()));
            } else if (r.getMeasurementValue() < low) {
                alerts.add(factory.createAlert(pid, lowLabel, r.getTimestamp()));
            }
        }

        // Trend: 3 consecutive readings each changing > 10 mmHg
        for (int i = 2; i < typed.size(); i++) {
            double d1 = typed.get(i - 1).getMeasurementValue() - typed.get(i - 2).getMeasurementValue();
            double d2 = typed.get(i).getMeasurementValue() - typed.get(i - 1).getMeasurementValue();
            if (d1 > TREND_DELTA && d2 > TREND_DELTA) {
                alerts.add(factory.createAlert(pid, incLabel, typed.get(i).getTimestamp()));
            } else if (d1 < -TREND_DELTA && d2 < -TREND_DELTA) {
                alerts.add(factory.createAlert(pid, decLabel, typed.get(i).getTimestamp()));
            }
        }
    }
}