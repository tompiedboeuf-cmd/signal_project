package com.cardio_generator;

import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthDataSimulatorTest {

    @Test
    void testInitializePatientIds() throws Exception {
        Method method = HealthDataSimulator.class.getDeclaredMethod("initializePatientIds", int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Integer> patientIds = (List<Integer>) method.invoke(null, 5);

        assertNotNull(patientIds);
        assertEquals(5, patientIds.size());
        assertTrue(patientIds.contains(1));
        assertTrue(patientIds.contains(5));
        for (int i = 1; i <= 5; i++) {
            assertTrue(patientIds.contains(i));
        }
    }

    @Test
    void testInitializePatientIdsEmpty() throws Exception {
        Method method = HealthDataSimulator.class.getDeclaredMethod("initializePatientIds", int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Integer> patientIds = (List<Integer>) method.invoke(null, 0);

        assertNotNull(patientIds);
        assertEquals(0, patientIds.size());
    }

    @Test
    void testInitializePatientIdsLarge() throws Exception {
        Method method = HealthDataSimulator.class.getDeclaredMethod("initializePatientIds", int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Integer> patientIds = (List<Integer>) method.invoke(null, 100);

        assertNotNull(patientIds);
        assertEquals(100, patientIds.size());
        assertTrue(patientIds.contains(1));
        assertTrue(patientIds.contains(100));
    }

    @Test
    void testParseArgumentsConsoleOutput() throws Exception {
        // Reset fields
        resetSimulatorState();

        String[] args = {"--output", "console"};
        Method parseMethod = HealthDataSimulator.class.getDeclaredMethod("parseArguments", String[].class);
        parseMethod.setAccessible(true);

        assertDoesNotThrow(() -> parseMethod.invoke(null, (Object) args));

        Field outputStrategyField = HealthDataSimulator.class.getDeclaredField("outputStrategy");
        outputStrategyField.setAccessible(true);
        OutputStrategy strategy = (OutputStrategy) outputStrategyField.get(null);

        assertNotNull(strategy);
        assertTrue(strategy instanceof ConsoleOutputStrategy);
    }

    @Test
    void testParseArgumentsFileOutput(@TempDir Path tempDir) throws Exception {
        resetSimulatorState();

        String[] args = {"--output", "file:" + tempDir.toString()};
        Method parseMethod = HealthDataSimulator.class.getDeclaredMethod("parseArguments", String[].class);
        parseMethod.setAccessible(true);

        assertDoesNotThrow(() -> parseMethod.invoke(null, (Object) args));

        Field outputStrategyField = HealthDataSimulator.class.getDeclaredField("outputStrategy");
        outputStrategyField.setAccessible(true);
        OutputStrategy strategy = (OutputStrategy) outputStrategyField.get(null);

        assertNotNull(strategy);
        assertTrue(strategy instanceof FileOutputStrategy);
    }

    @Test
    void testParseArgumentsPatientCount() throws Exception {
        resetSimulatorState();

        String[] args = {"--patient-count", "75"};
        Method parseMethod = HealthDataSimulator.class.getDeclaredMethod("parseArguments", String[].class);
        parseMethod.setAccessible(true);

        assertDoesNotThrow(() -> parseMethod.invoke(null, (Object) args));

        Field patientCountField = HealthDataSimulator.class.getDeclaredField("patientCount");
        patientCountField.setAccessible(true);
        int count = (int) patientCountField.get(null);

        assertEquals(75, count);
    }

    @Test
    void testParseArgumentsInvalidPatientCount() throws Exception {
        resetSimulatorState();

        String[] args = {"--patient-count", "invalid"};
        Method parseMethod = HealthDataSimulator.class.getDeclaredMethod("parseArguments", String[].class);
        parseMethod.setAccessible(true);

        // Should not throw, but print error message
        assertDoesNotThrow(() -> parseMethod.invoke(null, (Object) args));

        // Should still have default value after parsing error
        Field patientCountField = HealthDataSimulator.class.getDeclaredField("patientCount");
        patientCountField.setAccessible(true);
        int count = (int) patientCountField.get(null);

        assertEquals(50, count); // Default value
    }

    @Test
    void testParseArgumentsMultipleOptions() throws Exception {
        resetSimulatorState();

        String[] args = {"--patient-count", "25", "--output", "console"};
        Method parseMethod = HealthDataSimulator.class.getDeclaredMethod("parseArguments", String[].class);
        parseMethod.setAccessible(true);

        assertDoesNotThrow(() -> parseMethod.invoke(null, (Object) args));

        Field patientCountField = HealthDataSimulator.class.getDeclaredField("patientCount");
        patientCountField.setAccessible(true);
        int count = (int) patientCountField.get(null);

        Field outputStrategyField = HealthDataSimulator.class.getDeclaredField("outputStrategy");
        outputStrategyField.setAccessible(true);
        OutputStrategy strategy = (OutputStrategy) outputStrategyField.get(null);

        assertEquals(25, count);
        assertTrue(strategy instanceof ConsoleOutputStrategy);
    }

    private void resetSimulatorState() throws Exception {
        Field patientCountField = HealthDataSimulator.class.getDeclaredField("patientCount");
        patientCountField.setAccessible(true);
        patientCountField.setInt(null, 50);

        Field outputStrategyField = HealthDataSimulator.class.getDeclaredField("outputStrategy");
        outputStrategyField.setAccessible(true);
        outputStrategyField.set(null, new ConsoleOutputStrategy());
    }
}
