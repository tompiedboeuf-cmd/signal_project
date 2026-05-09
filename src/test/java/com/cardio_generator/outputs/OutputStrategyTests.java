package com.cardio_generator.outputs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OutputStrategyTests {

    @Test
    void testFileOutputStrategyWritesDataFiles(@TempDir Path tempDir) throws Exception {
        FileOutputStrategy outputStrategy = new FileOutputStrategy(tempDir.toString());

        outputStrategy.output(1, 1000L, "ECG", "1.23");
        outputStrategy.output(2, 1001L, "Saturation", "97%");

        Path ecgFile = tempDir.resolve("ECG.txt");
        Path saturationFile = tempDir.resolve("Saturation.txt");

        assertTrue(Files.exists(ecgFile), "ECG output file should exist");
        assertTrue(Files.exists(saturationFile), "Saturation output file should exist");
        assertTrue(outputStrategy.fileMap.containsKey("ECG"));
        assertTrue(outputStrategy.fileMap.containsKey("Saturation"));

        List<String> ecgLines = Files.readAllLines(ecgFile);
        List<String> saturationLines = Files.readAllLines(saturationFile);

        assertEquals(1, ecgLines.size());
        assertTrue(ecgLines.get(0).contains("Patient ID: 1"));
        assertTrue(ecgLines.get(0).contains("Label: ECG"));

        assertEquals(1, saturationLines.size());
        assertTrue(saturationLines.get(0).contains("Patient ID: 2"));
        assertTrue(saturationLines.get(0).contains("Label: Saturation"));
    }

    @Test
    void testConsoleOutputStrategyPrintsFormattedText() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            ConsoleOutputStrategy outputStrategy = new ConsoleOutputStrategy();
            outputStrategy.output(1, 1000L, "HeartRate", "72.0");
            String printed = outputStream.toString().trim();

            assertTrue(printed.contains("Patient ID: 1"));
            assertTrue(printed.contains("Label: HeartRate"));
            assertTrue(printed.contains("Data: 72.0"));
        } finally {
            System.setOut(originalOut);
        }
    }
}
