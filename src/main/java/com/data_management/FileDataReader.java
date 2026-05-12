package com.data_management;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDataReader implements DataReader {
    private String outputDir;

    public FileDataReader(String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void readData(DataStorage storage) throws IOException {
        Path path = Paths.get(outputDir);
        Files.walk(path)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        parseAndStore(line, storage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    @Override
    public void connect(String url) throws Exception {
        // No network connection required for file-based data reading.
    }

    @Override
    public void disconnect() {
        // Nothing to close for file-based data reader.
    }

    private void parseAndStore(String line, DataStorage storage) {
        // Expected Format: PatientID, Timestamp, Label, Value
        String[] parts = line.split(",");
        if (parts.length != 4) {
            return; // Ignore invalid lines
        }
        try {
            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label = parts[2].trim();
            double value = Double.parseDouble(parts[3].trim());

            storage.addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            // Ignore lines with invalid number formats
        }
    }
}