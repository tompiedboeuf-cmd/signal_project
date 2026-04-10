package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated alert events for patients in the health monitoring system.
 * Each patient can be in one of two states: alert active ("triggered") or no alert ("resolved").
 * The generator uses a Poisson-process model to determine when a new alert fires, and a fixed
 * 90% probability to resolve an active alert on each evaluation cycle.
 *
 * <p>Alert state is maintained per patient across calls, so consecutive invocations of
 * {@link #generate} produce a realistic sequence of triggered and resolved events.
 * This class implements {@link PatientDataGenerator} and is scheduled by
 * {@code HealthDataSimulator}.
 */
public class AlertGenerator implements PatientDataGenerator {

    /** Shared random number generator used for all probabilistic decisions. */
    public static final Random randomGenerator = new Random();

    // Google Java Style Guide §5.2.3: non-constant field names use lowerCamelCase; renamed from AlertStates
    /**
     * Tracks the current alert state for each patient.
     * {@code true} means an alert is currently active; {@code false} means no active alert.
     * Indexed by patient ID; index 0 is unused.
     */
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Constructs an AlertGenerator and initialises all patients to the no-alert state.
     *
     * @param patientCount the total number of patients to simulate; determines the
     *                     size of the internal state array (indices 1 to patientCount)
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Evaluates and updates the alert state for the specified patient, then outputs the result.
     *
     * <p>If the patient currently has an active alert, there is a 90% chance it is resolved
     * and a {@code "resolved"} event is sent to the output strategy. If no alert is active,
     * a Poisson-process probability is used to decide whether a new alert is triggered; if so,
     * a {@code "triggered"} event is sent.
     *
     * <p>The Poisson rate parameter {@code lambda} is set to 0.1 alerts per period, giving a
     * per-period trigger probability of approximately 9.5%.
     *
     * @param patientId      the unique identifier of the patient; must be in the range
     *                       1 to patientCount (as supplied to the constructor)
     * @param outputStrategy the strategy used to deliver alert events
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Google Java Style Guide §5.2.7: local variable names use lowerCamelCase; renamed from Lambda
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
