package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@link OutputStrategy} that writes patient health data to label-specific text files.
 * Each health metric label (e.g., "ECG", "Saturation") gets its own file within the
 * configured base directory. Files are created on first write and appended to on subsequent writes.
 *
 * <p>This class is thread-safe for concurrent writes to different labels, as it uses a
 * {@link ConcurrentHashMap} to manage file path mappings.
 */
public class FileOutputStrategy implements OutputStrategy {

    // Google Java Style Guide §5.2.3: non-constant field names use lowerCamelCase; renamed from BaseDirectory
    private String baseDirectory;

    // Google Java Style Guide §5.2.3: non-constant field names use lowerCamelCase; renamed from file_map
    /**
     * Maps each label to the file path where its data is stored.
     * Populated lazily on the first output call for each label.
     */
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Constructs a FileOutputStrategy that writes output files to the specified directory.
     *
     * @param baseDirectory the path to the directory where output files will be created;
     *                      the directory is created automatically if it does not exist
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    /**
     * Writes a patient health data record to a label-specific file within the base directory.
     * The output file is named after the label (e.g., "Saturation.txt"). If the file does not
     * exist it is created; subsequent calls append to the existing file.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time of the measurement in milliseconds since epoch
     * @param label     the health metric label used to determine the output file name
     * @param data      the measurement value to write
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Google Java Style Guide §5.2.7: local variable names use lowerCamelCase; renamed from FilePath
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}
