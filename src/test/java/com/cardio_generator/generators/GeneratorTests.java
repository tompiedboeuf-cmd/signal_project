package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTests {

    private static class CollectingOutputStrategy implements OutputStrategy {
        final List<String> messages = new ArrayList<>();

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            messages.add(patientId + "," + label + "," + data);
        }
    }

    @Test
    void testAlertGeneratorTriggersAndResolves() {
        AlertGenerator.randomGenerator.setSeed(4129L);
        CollectingOutputStrategy outputStrategy = new CollectingOutputStrategy();
        AlertGenerator generator = new AlertGenerator(1);

        generator.generate(1, outputStrategy);
        assertFalse(outputStrategy.messages.isEmpty(), "AlertGenerator should produce an output event");
        assertTrue(outputStrategy.messages.get(0).contains("Alert,triggered"), "First event should be a triggered alert");

        outputStrategy.messages.clear();
        generator.generate(1, outputStrategy);
        assertTrue(outputStrategy.messages.stream().anyMatch(msg -> msg.contains("Alert,resolved")),
            "Second event should resolve the alert");
    }

    @Test
    void testBloodPressureDataGeneratorOutputsSystolicAndDiastolic() {
        CollectingOutputStrategy outputStrategy = new CollectingOutputStrategy();
        BloodPressureDataGenerator generator = new BloodPressureDataGenerator(1);

        generator.generate(1, outputStrategy);

        assertEquals(2, outputStrategy.messages.size());
        assertTrue(outputStrategy.messages.get(0).contains("SystolicPressure"));
        assertTrue(outputStrategy.messages.get(1).contains("DiastolicPressure"));
    }

    @Test
    void testBloodSaturationDataGeneratorOutputsSaturationPercentage() {
        CollectingOutputStrategy outputStrategy = new CollectingOutputStrategy();
        BloodSaturationDataGenerator generator = new BloodSaturationDataGenerator(1);

        generator.generate(1, outputStrategy);

        assertEquals(1, outputStrategy.messages.size());
        assertTrue(outputStrategy.messages.get(0).contains("Saturation"));
        assertTrue(outputStrategy.messages.get(0).endsWith("%"));
    }

    @Test
    void testEcgDataGeneratorOutputsNumericValue() {
        CollectingOutputStrategy outputStrategy = new CollectingOutputStrategy();
        ECGDataGenerator generator = new ECGDataGenerator(1);

        generator.generate(1, outputStrategy);

        assertEquals(1, outputStrategy.messages.size());
        assertTrue(outputStrategy.messages.get(0).contains("ECG"));
        String[] parts = outputStrategy.messages.get(0).split(",", 3);
        assertDoesNotThrow(() -> Double.parseDouble(parts[2]));
    }

    @Test
    void testBloodLevelsDataGeneratorOutputsThreeMetrics() {
        CollectingOutputStrategy outputStrategy = new CollectingOutputStrategy();
        BloodLevelsDataGenerator generator = new BloodLevelsDataGenerator(1);

        generator.generate(1, outputStrategy);

        assertEquals(3, outputStrategy.messages.size());
        assertTrue(outputStrategy.messages.get(0).contains("Cholesterol"));
        assertTrue(outputStrategy.messages.get(1).contains("WhiteBloodCells"));
        assertTrue(outputStrategy.messages.get(2).contains("RedBloodCells"));
    }
}
