package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

class DataStorageTest {

    @Test
    void testAddAndGetRecords() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size()); // Check if two records are retrieved
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate first record
    }
    @Test
    void testAddAndRetrievePatientData() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 72.0, "HeartRate", 1000L);
        storage.addPatientData(1, 75.0, "HeartRate", 2000L);

        List<PatientRecord> records = storage.getRecords(1, 0L, 3000L);
        assertEquals(2, records.size());
    }

    @Test
    void testGetRecordsTimeFilter() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 72.0, "HeartRate", 1000L);
        storage.addPatientData(1, 75.0, "HeartRate", 5000L);

        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals(1, records.size());
        assertEquals(72.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsNonExistentPatient() {
        DataStorage storage = new DataStorage();
        List<PatientRecord> records = storage.getRecords(999, 0L, 10000L);
        assertTrue(records.isEmpty());
    }
}
