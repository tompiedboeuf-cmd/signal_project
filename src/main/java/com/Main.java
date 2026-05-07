package com;

import java.io.IOException;

import com.alerts.AlertGenerator;
import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.Patient;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            DataStorage storage = new DataStorage();
            AlertGenerator alertGenerator = new AlertGenerator(storage);
            
            String outputDir = "pathName"; 
            FileDataReader reader = new FileDataReader(outputDir);
            
            try {
                reader.readData(storage);
                System.out.println("Data successfully loaded into storage.");

                for (Patient patient : storage.getAllPatients()) {
                    alertGenerator.evaluateData(patient);
                }
                
            } catch (IOException e) {
                System.err.println("Error reading data: " + e.getMessage());
            }
            
        } else {
            HealthDataSimulator.main(args);
        }
    }
}