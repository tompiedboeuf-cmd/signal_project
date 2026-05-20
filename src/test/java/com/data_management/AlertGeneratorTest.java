package com.data_management;

import com.alerts.*;
import com.cardio_generator.HealthDataSimulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AlertGeneratorTest {

    private DataStorage storage;
    private List<Alert> triggeredAlerts;
    private AlertGenerator generator;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
        triggeredAlerts = new ArrayList<>();

        // Surcharge triggerAlert pour capturer les alertes dans les tests
        generator = new AlertGenerator(storage) {
            protected void triggerAlert(Alert alert) {
                triggeredAlerts.add(alert);
            }
        };
    }

    // ── BLOOD PRESSURE TREND ──────────────────

    @Test
    void testSystolicIncreasingTrendAlert() {
        Patient p = new Patient(1);
        p.addRecord(120.0, "SystolicPressure", 1000L);
        p.addRecord(135.0, "SystolicPressure", 2000L);
        p.addRecord(150.0, "SystolicPressure", 3000L);

        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().contains("Increasing Trend")));
    }

    @Test
    void testSystolicDecreasingTrendAlert() {
        Patient p = new Patient(1);
        p.addRecord(150.0, "SystolicPressure", 1000L);
        p.addRecord(135.0, "SystolicPressure", 2000L);
        p.addRecord(120.0, "SystolicPressure", 3000L);

        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().contains("Decreasing Trend")));
    }

    @Test
    void testNoTrendAlertWhenChangeLessThan10() {
        Patient p = new Patient(1);
        p.addRecord(120.0, "SystolicPressure", 1000L);
        p.addRecord(125.0, "SystolicPressure", 2000L);
        p.addRecord(130.0, "SystolicPressure", 3000L);

        generator.evaluateData(p);

        assertFalse(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().contains("Increasing Trend")));
    }

    // ── CRITICAL THRESHOLDS ───────────────────

    @Test
    void testCriticalHighSystolicAlert() {
        Patient p = new Patient(1);
        p.addRecord(185.0, "SystolicPressure", 1000L);
        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().contains("Critical High Systolic")));
    }

    @Test
    void testCriticalLowSystolicAlert() {
        Patient p = new Patient(1);
        p.addRecord(85.0, "SystolicPressure", 1000L);
        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().contains("Critical Low Systolic")));
    }

    @Test
    void testCriticalHighDiastolicAlert() {
        Patient p = new Patient(1);
        p.addRecord(125.0, "DiastolicPressure", 1000L);
        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().contains("Critical High Diastolic")));
    }

    @Test
    void testCriticalLowDiastolicAlert() {
        Patient p = new Patient(1);
        p.addRecord(55.0, "DiastolicPressure", 1000L);
        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().contains("Critical Low Diastolic")));
    }

    // ── BLOOD SATURATION ─────────────────────

    @Test
    void testLowSaturationAlert() {
        Patient p = new Patient(1);
        p.addRecord(90.0, "Saturation", 1000L);
        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().equals("Low Blood Saturation Alert")));
    }

    @Test
    void testNoLowSaturationAlertAbove92() {
        Patient p = new Patient(1);
        p.addRecord(95.0, "Saturation", 1000L);
        generator.evaluateData(p);

        assertFalse(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().equals("Low Blood Saturation Alert")));
    }

    @Test
    void testRapidSaturationDropAlert() {
        long now = System.currentTimeMillis();
        Patient p = new Patient(1);
        p.addRecord(98.0, "Saturation", now - 500000); // hors fenêtre
        p.addRecord(97.0, "Saturation", now - 300000);
        p.addRecord(91.0, "Saturation", now - 60000); // chute de 6%

        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().equals("Rapid Blood Saturation Drop Alert")));
    }

    // ── HYPOTENSIVE HYPOXEMIA ─────────────────

    @Test
    void testHypotensiveHypoxemiaAlert() {
        Patient p = new Patient(1);
        p.addRecord(85.0, "SystolicPressure", 1000L); // < 90
        p.addRecord(88.0, "Saturation", 1000L); // < 92

        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().equals("Hypotensive Hypoxemia Alert")));
    }

    @Test
    void testNoHypotensiveHypoxemiaWhenOnlyOneLow() {
        Patient p = new Patient(1);
        p.addRecord(85.0, "SystolicPressure", 1000L); // < 90
        p.addRecord(95.0, "Saturation", 1000L); // normal

        generator.evaluateData(p);

        assertFalse(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().equals("Hypotensive Hypoxemia Alert")));
    }

    // ── ECG ───────────────────────────────────

    @Test
    void testECGAbnormalPeakAlert() {
        Patient p = new Patient(1);
        // 10 lectures normales
        for (int i = 0; i < 10; i++) {
            p.addRecord(1.0, "ECG", 1000L + i * 100);
        }
        // Pic anormal (plus de 2x la moyenne)
        p.addRecord(10.0, "ECG", 2000L);

        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().equals("ECG Abnormal Peak Alert")));
    }

    // ── MANUAL TRIGGER ────────────────────────

    @Test
    void testManualAlertTriggered() {
        Patient p = new Patient(1);
        p.addRecord(1.0, "Alert", 1000L);
        generator.evaluateData(p);

        assertTrue(triggeredAlerts.stream()
                .anyMatch(a -> a.getCondition().equals("Manual Alert Triggered")));
    }

    // ── FACTORY METHOD PATTERN ────────────────

    @Test
    void testBloodPressureAlertFactoryCreatesAlert() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("1", "Critical High Systolic", 1000L);
        assertNotNull(alert);
        assertEquals("1", alert.getPatientId());
        assertEquals("Critical High Systolic", alert.getCondition());
        assertEquals(1000L, alert.getTimestamp());
    }

    @Test
    void testBloodOxygenAlertFactoryCreatesAlert() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("2", "Low Blood Saturation Alert", 2000L);
        assertNotNull(alert);
        assertEquals("Low Blood Saturation Alert", alert.getCondition());
    }

    @Test
    void testECGAlertFactoryCreatesAlert() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("3", "ECG Abnormal Peak Alert", 3000L);
        assertNotNull(alert);
        assertEquals("ECG Abnormal Peak Alert", alert.getCondition());
    }

    // ── DECORATOR PATTERN ─────────────────────

    @Test
    void testPriorityAlertDecoratorAddsCriticalTag() {
        Alert base = new Alert("1", "Critical High Systolic", 1000L);
        Alert decorated = new PriorityAlertDecorator(base, PriorityAlertDecorator.Priority.CRITICAL);
        assertTrue(decorated.getCondition().startsWith("[CRITICAL]"));
        assertEquals("1", decorated.getPatientId());
        assertEquals(1000L, decorated.getTimestamp());
    }

    @Test
    void testRepeatedAlertDecoratorShouldRepeat() {
        Alert base = new Alert("1", "Low Blood Saturation Alert", 1000L);
        RepeatedAlertDecorator repeated = new RepeatedAlertDecorator(base, 5000L);
        assertTrue(repeated.shouldRepeat(7000L));
        assertFalse(repeated.shouldRepeat(3000L));
    }

    // ── SINGLETON PATTERN ─────────────────────

    @Test
    void testDataStorageSingletonReturnsSameInstance() {
        DataStorage instance1 = DataStorage.getInstance();
        DataStorage instance2 = DataStorage.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testHealtDataSimulatorSingletonReturnsSameInstance() {
        HealthDataSimulator instance1 = HealthDataSimulator.getInstance();
        HealthDataSimulator instance2 = HealthDataSimulator.getInstance();
        assertSame(instance1, instance2);
    }

    // ── STRATEGY PATTERN DIRECT TESTS ─────────

    @Test
    void testBloodPressureStrategyHighSystolic() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        Patient p = new Patient(1);
        p.addRecord(185.0, "SystolicPressure", 1000L);
        List<Alert> alerts = strategy.checkAlert(p, p.getRecords(0, Long.MAX_VALUE));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("Critical High Systolic")));
    }

    @Test
    void testBloodPressureStrategyIncreasingTrend() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        Patient p = new Patient(1);
        p.addRecord(120.0, "SystolicPressure", 1000L);
        p.addRecord(135.0, "SystolicPressure", 2000L);
        p.addRecord(150.0, "SystolicPressure", 3000L);
        List<Alert> alerts = strategy.checkAlert(p, p.getRecords(0, Long.MAX_VALUE));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("Increasing Trend")));
    }

    @Test
    void testOxygenSaturationStrategyLowSaturation() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        Patient p = new Patient(1);
        p.addRecord(90.0, "Saturation", 1000L);
        List<Alert> alerts = strategy.checkAlert(p, p.getRecords(0, Long.MAX_VALUE));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("Low Blood Saturation Alert")));
    }

    @Test
    void testHeartRateStrategyECGPeak() {
        HeartRateStrategy strategy = new HeartRateStrategy();
        Patient p = new Patient(1);
        for (int i = 0; i < 10; i++) {
            p.addRecord(1.0, "ECG", 1000L + i * 100);
        }
        p.addRecord(10.0, "ECG", 2000L);
        List<Alert> alerts = strategy.checkAlert(p, p.getRecords(0, Long.MAX_VALUE));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("ECG Abnormal Peak Alert")));
    }

    @Test
    void testHypotensiveHypoxemiaStrategyTriggered() {
        HypotensiveHypoxemiaStrategy strategy = new HypotensiveHypoxemiaStrategy();
        Patient p = new Patient(1);
        p.addRecord(85.0, "SystolicPressure", 1000L);
        p.addRecord(88.0, "Saturation", 1000L);
        List<Alert> alerts = strategy.checkAlert(p, p.getRecords(0, Long.MAX_VALUE));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("Hypotensive Hypoxemia Alert")));
    }
}