package com.data_management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

public class PatientTest {
    @Test
    void testGetRecordsWithinRange() {
        Patient p = new Patient(1);
        p.addRecord(70.0, "HeartRate", 1000L);
        p.addRecord(80.0, "HeartRate", 3000L);
        p.addRecord(90.0, "HeartRate", 6000L);

        List<PatientRecord> result = p.getRecords(1000L, 3000L);
        assertEquals(2, result.size());
    }

    @Test
    void testGetRecordsEmptyRange() {
        Patient p = new Patient(1);
        p.addRecord(70.0, "HeartRate", 1000L);
        List<PatientRecord> result = p.getRecords(5000L, 9000L);
        assertTrue(result.isEmpty());
    }
}
