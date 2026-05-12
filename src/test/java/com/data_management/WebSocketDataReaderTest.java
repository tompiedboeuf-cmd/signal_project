package com.data_management;

import com.data_management.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebSocketDataReader
 * Tests initialization, error handling, and basic connection logic
 */
public class WebSocketDataReaderTest {

    private DataStorage storage;
    private WebSocketDataReader reader;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
        reader = new WebSocketDataReader(storage);
    }

    @Test
    void testConstructorInitializesStorage() {
        assertNotNull(reader);
    }

    @Test
    void testReadDataThrowsExceptionWhenStorageIsNull() {
        reader = new WebSocketDataReader(storage);
        
        assertThrows(IllegalArgumentException.class, () -> {
            reader.readData(null);
        }, "Expected IllegalArgumentException when dataStorage is null");
    }

    @Test
    void testReadDataUpdatesStorageReference() throws IOException {
        DataStorage newStorage = new DataStorage();
        
        // Should not throw exception since reader is initialized with storage
        // But will throw IOException because client is not connected
        assertThrows(IOException.class, () -> {
            reader.readData(newStorage);
        });
    }

    @Test
    void testDisconnectDoesNotThrowException() {
        // Should not throw any exception
        assertDoesNotThrow(() -> {
            reader.disconnect();
        });
    }

    @Test
    void testConnectInitializesClient() {
        // Should not throw exception with valid URI format
        assertDoesNotThrow(() -> {
            reader.connect("ws://localhost:8080");
        });
    }
}
