package com.data_management;

import com.data_management.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.net.URI;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MyPatientDataClient.
 * Tests message parsing and data storage logic.
 */
public class MyPatientDataClientTest {

    private DataStorage storage;
    private MyPatientDataClient client;

    @BeforeEach
    void setUp() throws Exception {
        storage = new DataStorage();
        client = new MyPatientDataClient(new URI("ws://localhost:8080"), storage);
    }

    private void sendMessage(String message) {
        client.onMessage(message);
    }

    @Test
    void testParseAndStoreValidMessage() {
        sendMessage("1,1000,HeartRate,72.0");
        
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals(1, records.size());
        assertEquals(72.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
        assertEquals(1000L, records.get(0).getTimestamp());
    }

    @Test
    void testParseAndStoreMultipleMessages() {
        sendMessage("1,1000,HeartRate,72.0");
        sendMessage("1,2000,Saturation,95.0");
        sendMessage("2,1500,HeartRate,70.0");
        
        List<PatientRecord> patient1Records = storage.getRecords(1, 0L, 3000L);
        assertEquals(2, patient1Records.size());
        
        List<PatientRecord> patient2Records = storage.getRecords(2, 0L, 3000L);
        assertEquals(1, patient2Records.size());
    }

    @Test
    void testParseAndStoreIgnoresBlankMessage() {
        sendMessage("");
        sendMessage("   ");
        
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testParseAndStoreIgnoresInvalidFormat() {
        sendMessage("1,1000,HeartRate");
        sendMessage("1,1000,HeartRate,72.0,extra");
        
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testParseAndStoreIgnoresInvalidNumbers() {
        sendMessage("invalid,1000,HeartRate,72.0");
        sendMessage("1,invalid,HeartRate,72.0");
        sendMessage("1,1000,HeartRate,invalid");
        
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testParseAndStoreWithWhitespace() {
        sendMessage("  1  ,  1000  ,  HeartRate  ,  72.0  ");
        
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals(1, records.size());
        assertEquals(72.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
    }

    @Test
    void testParseAndStoreWithDifferentValueTypes() {
        sendMessage("5,5000,BloodPressure,120.5");
        sendMessage("5,5100,BloodPressure,80.3");
        sendMessage("5,5200,BloodSaturation,98.1");
        
        List<PatientRecord> records = storage.getRecords(5, 0L, 10000L);
        assertEquals(3, records.size());
        assertEquals(120.5, records.get(0).getMeasurementValue());
        assertEquals(80.3, records.get(1).getMeasurementValue());
        assertEquals(98.1, records.get(2).getMeasurementValue());
    }

    @Test
    void testOnOpenDoesNotThrow() {
        assertDoesNotThrow(() -> client.onOpen(null));
    }

    @Test
    void testOnCloseDoesNotThrow() {
        assertDoesNotThrow(() -> client.onClose(1000, "Normal closure", false));
    }

    @Test
    void testOnErrorDoesNotThrow() {
        assertDoesNotThrow(() -> client.onError(new RuntimeException("Test error")));
    }
}
