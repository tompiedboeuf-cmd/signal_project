package com.alerts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private List<Alert> triggeredAlerts;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.triggeredAlerts = new ArrayList<>();
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);

        checkBloodPressure(patient, records);
        checkBloodSaturation(patient, records);
        checkHypotensiveHypoxemia(patient, records);
        checkECG(patient, records);
        checkManualAlerts(patient, records);
    }

    /**
     * @assumption ECG Alert: A peak is defined as any value exceeding 2.0x (200%) 
    * of the current sliding window average.
    */
    private void checkECG(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecgRecords = filterByType(records, "ECG");
        if (ecgRecords.size() < 10) return; 

        double sum = 0;
        for (PatientRecord r : ecgRecords) {
            sum += r.getMeasurementValue();
        }
        double average = sum / ecgRecords.size();

        PatientRecord latest = ecgRecords.get(ecgRecords.size() - 1);
        if (latest.getMeasurementValue() > average * 2.0) { 
            triggerAlert(new Alert(patient.getPatientId(), "ABNORMAL_ECG_PEAK", latest.getTimestamp()));
        }
    }

    private void checkManualAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> manualAlerts = filterByType(records, "Alert");

        for (PatientRecord record : manualAlerts) {
            if (record.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(patient.getPatientId(), "MANUAL_TRIGGER_ALERT", record.getTimestamp()));
            }
        }
    }

    private void checkBloodPressure(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> systolic = filterByType(records, "SystolicBP");
        List<PatientRecord> diastolic = filterByType(records, "DiastolicBP");

        // 1. Critical Thresholds
        checkThreshold(patient, systolic, 180, 90, "CRITICAL_SYSTOLIC");
        checkThreshold(patient, diastolic, 120, 60, "CRITICAL_DIASTOLIC");

        // 2. Trend Alerts (3 consecutive readings changing by > 10mmHg)
        evaluateTrend(patient, systolic, "TREND_SYSTOLIC");
        evaluateTrend(patient, diastolic, "TREND_DIASTOLIC");
    }

    private void checkBloodSaturation(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> saturation = filterByType(records, "Saturation");

        for (int i = 0; i < saturation.size(); i++) {
            PatientRecord current = saturation.get(i);

            if (current.getMeasurementValue() < 92) {
                triggerAlert(new Alert(patient.getPatientId(), "LOW_SATURATION", current.getTimestamp()));
            }

            for (int j = 0; j < i; j++) {
                PatientRecord previous = saturation.get(j);
                long timeDiff = current.getTimestamp() - previous.getTimestamp();
                double valDiff = previous.getMeasurementValue() - current.getMeasurementValue();

                if (timeDiff <= 600000 && valDiff >= 5) { 
                    triggerAlert(new Alert(patient.getPatientId(), "RAPID_SATURATION_DROP", current.getTimestamp()));
                    break; 
                }   
            }
        }   
    }

    /**
    * @assumption Hypotensive Hypoxemia: Because sensors for Blood Pressure and 
    * Oxygen Saturation may not sync perfectly, a 1-minute (60,000ms) time window 
    * is used to correlate these two metrics.
    */
    private void checkHypotensiveHypoxemia(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> systolic = filterByType(records, "SystolicBP");
        List<PatientRecord> saturation = filterByType(records, "Saturation");

        for (PatientRecord s : systolic) {
            if (s.getMeasurementValue() < 90) {

                boolean lowSat = saturation.stream().anyMatch(sat -> Math.abs(sat.getTimestamp() - s.getTimestamp()) <= 60000 && sat.getMeasurementValue() < 92);
                if (lowSat) {
                    triggerAlert(new Alert(patient.getPatientId(), "HYPOTENSIVE_HYPOXEMIA", s.getTimestamp()));
                }
            }
        }
    }

    private void evaluateTrend(Patient patient, List<PatientRecord> readings, String alertType) {
        for (int i = 2; i < readings.size(); i++) {
            double v1 = readings.get(i - 2).getMeasurementValue();
            double v2 = readings.get(i - 1).getMeasurementValue();
            double v3 = readings.get(i).getMeasurementValue();

            double diff1 = v2 - v1;
            double diff2 = v3 - v2;

            if ((diff1 > 10 && diff2 > 10) || (diff1 < -10 && diff2 < -10)) {
                triggerAlert(new Alert(patient.getPatientId(), alertType, readings.get(i).getTimestamp()));
            }
    }   
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        this.triggeredAlerts.add(alert);
       
        System.out.println("ALERT TRIGGERED: " + alert.getCondition() + 
                           " for Patient " + alert.getPatientId() + 
                           " at " + alert.getTimestamp());
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        return records.stream().filter(r -> r.getRecordType().equals(type)).collect(Collectors.toList());
    }

    private void checkThreshold(Patient patient, List<PatientRecord> readings, double max, double min, String alertType) {
        for (PatientRecord record : readings) {
            double value = record.getMeasurementValue();
            if (value > max || value < min) {
                triggerAlert(new Alert(patient.getPatientId(), alertType, record.getTimestamp()));
            }
        }
    }

    public List<Alert> getTriggeredAlerts() {
        return new ArrayList<>(this.triggeredAlerts); // Retourne une copie pour protéger la liste originale
    }
}