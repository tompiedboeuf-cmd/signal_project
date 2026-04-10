package com.cardio_generator.outputs;

/**
 * Defines the contract for all output strategies used in the health data simulator.
 * Implementations of this interface are responsible for delivering simulated patient
 * data to a specific destination, such as the console, a file, or a network socket.
 *
 * <p>This interface is used throughout the simulator to decouple data generation
 * from data delivery, allowing different output destinations to be swapped at runtime.
 */
public interface OutputStrategy {

    /**
     * Outputs a single health data record for a patient.
     *
     * @param patientId the unique identifier of the patient whose data is being recorded
     * @param timestamp the time at which the measurement was taken, in milliseconds since epoch
     * @param label     the type of health metric being recorded (e.g., "ECG", "Saturation")
     * @param data      the value of the health metric as a string
     */
    void output(int patientId, long timestamp, String label, String data);
}
