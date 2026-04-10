package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Defines the contract for all patient data generators in the health data simulator.
 * Each implementation is responsible for generating a specific type of health metric
 * (e.g., ECG, blood pressure, blood saturation) and sending it to the provided output strategy.
 *
 * <p>Implementations are expected to maintain per-patient state (e.g., last recorded values)
 * to produce realistic, continuous data streams.
 */
public interface PatientDataGenerator {

    /**
     * Generates a health data record for the specified patient and sends it to the output strategy.
     *
     * @param patientId      the unique identifier of the patient for whom data is being generated
     * @param outputStrategy the strategy used to deliver the generated data to its destination
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
