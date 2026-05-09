package com.data_management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FileDataReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadValidData() throws Exception {
        // Crée un fichier de test
        Path file = tempDir.resolve("output.txt");
        Files.writeString(file,
                "1,1000,HeartRate,72.0\n" +
                        "1,2000,SystolicPressure,120.0\n" +
                        "2,1500,Saturation,95.0\n");

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, 5000L);
        assertEquals(2, records.size());

        List<PatientRecord> p2Records = storage.getRecords(2, 0L, 5000L);
        assertEquals(1, p2Records.size());
        assertEquals(95.0, p2Records.get(0).getMeasurementValue());
    }

    @Test
    void testReadInvalidLinesIgnored() throws Exception {
        Path file = tempDir.resolve("output.txt");
        Files.writeString(file,
                "INVALID_LINE\n" +
                        "1,1000,HeartRate,72.0\n" +
                        "also,bad,data\n");

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, 5000L);
        assertEquals(1, records.size());
    }

    @Test
    void testEmptyDirectory() throws Exception {
        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        assertTrue(storage.getAllPatients().isEmpty());
    }
}