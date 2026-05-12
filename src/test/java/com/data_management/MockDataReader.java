package com.data_management;

import java.io.IOException;

public class MockDataReader implements DataReader {
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        // Mock implementation: Add some test data to the storage
        dataStorage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        dataStorage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);
        dataStorage.addPatientData(2, 150.0, "RedBloodCells", 1714376789052L);
    }

    @Override
    public void connect(String url) throws Exception {
        // Mock reader does not require an actual connection.
    }

    @Override
    public void disconnect() {
        // Mock reader does not hold resources.
    }

}
