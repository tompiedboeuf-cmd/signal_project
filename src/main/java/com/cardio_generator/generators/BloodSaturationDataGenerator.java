package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood oxygen saturation (SpO2) data for patients.
 * Each patient is initialised with a baseline saturation level between 95% and 100%.
 * On every call to {@link #generate}, the saturation fluctuates slightly (±1%) and
 * is clamped to the realistic range of 90%–100% to avoid physiologically impossible values.
 *
 * <p>This generator implements {@link PatientDataGenerator} and is intended to be
 * scheduled at regular intervals by {@code HealthDataSimulator}.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();

    /**
     * Stores the most recently generated saturation value for each patient.
     * Indexed by patient ID; index 0 is unused.
     */
    private int[] lastSaturationValues;

    /**
     * Constructs a BloodSaturationDataGenerator and initialises each patient's
     * saturation to a random baseline value between 95% and 100%.
     *
     * @param patientCount the total number of patients to simulate; determines the
     *                     size of the internal state array (indices 1 to patientCount)
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Generates a new blood saturation reading for the specified patient and sends it
     * to the provided output strategy.
     *
     * <p>The new value is derived by applying a random fluctuation of -1, 0, or +1 to
     * the patient's previous saturation, then clamping the result to [90, 100].
     * The result is output as a percentage string (e.g., {@code "97%"}) under the
     * label {@code "Saturation"}.
     *
     * @param patientId      the unique identifier of the patient; must be in the range
     *                       1 to patientCount (as supplied to the constructor)
     * @param outputStrategy the strategy used to deliver the generated reading
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
