package com.duck123acb.robotcore;

/**
 * A lightweight, FTC-independent PID controller using real-time delta calculations.
 * Tracks proportional, integral, and derivative components with anti-windup.
 */
public class PID {

    /** Proportional, integral, and derivative gains. */
    private double kp, ki, kd;

    /** Accumulated integral term (clamped). */
    private double integralSum = 0;

    /** Last cycle's error value, used for derivative. */
    private double lastError = 0;

    /** Most recently computed PID output. */
    private double lastOutput = 0;

    /** Maximum magnitude allowed for the integral term. */
    private double integralLimit = 1.0;

    /** Timestamp (nanoseconds) from the previous update. */
    private long lastTime = System.nanoTime();

    /** Flag to handle the first update after reset/init. */
    private boolean firstRun = true;

    /**
     * Create a new PID controller.
     *
     * @param kp proportional gain
     * @param ki integral gain
     * @param kd derivative gain
     */
    public PID(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    /**
     * Computes a single PID update based on the target and current measurement.
     *
     * @param target the desired setpoint value
     * @param current the current measured value
     * @return the PID output for this timestep
     *
     * Notes:
     * - Uses System.nanoTime() to compute an accurate delta time (dt).
     * - dt is converted to seconds.
     * - If dt is zero or negative, a small fallback dt is used.
     * - Integral term is clamped to prevent windup.
     */
    public double update(double target, double current) {
        long now = System.nanoTime();
        double dt = (now - lastTime) / 1e9; // convert ns → seconds

        if (dt <= 0) dt = 1e-3;

        double error = target - current;

        return calculate(error, dt);
    }

    /**
     * Computes a PID update for an angle (handles wrapping -PI to PI).
     * Assumes input angles are in radians.
     *
     * @param targetAngle target angle in radians
     * @param currentAngle current angle in radians
     * @return PID output
     */
    public double updateAngle(double targetAngle, double currentAngle) {
        long now = System.nanoTime();
        double dt = (now - lastTime) / 1e9; // convert ns → seconds

        if (dt <= 0) dt = 1e-3;

        double error = targetAngle - currentAngle;

        // Normalize error to -PI to +PI
        while (error > Math.PI) error -= 2 * Math.PI;
        while (error < -Math.PI) error += 2 * Math.PI;

        return calculate(error, dt);
    }

    private double calculate(double error, double dt) {
        // If this is the first run, we don't have a valid lastError for derivative.
        // Assume 0 derivative (lastError = error) to prevent spike.
        if (firstRun) {
            lastError = error;
            firstRun = false;
        }

        // Compute integral (with anti-windup)
        integralSum += error * dt;
        integralSum = clamp(integralSum, -integralLimit, integralLimit);

        // Derivative term
        double derivative = (error - lastError) / dt;

        // PID output
        double output = (kp * error) + (ki * integralSum) + (kd * derivative);

        // Update stored values for next cycle
        lastError = error;
        lastOutput = output;
        lastTime = System.nanoTime(); // Update time here

        return output;
    }

    /**
     * Reset all internal PID tracking: integral, derivative history, and timer.
     */
    public void reset() {
        integralSum = 0;
        lastError = 0;
        lastOutput = 0;
        firstRun = true;
        lastTime = System.nanoTime();
    }

    /**
     * Set the maximum magnitude for the integral accumulation.
     *
     * @param limit max allowed absolute integral value
     */
    public void setIntegralLimit(double limit) {
        this.integralLimit = limit;
    }

    /**
     * @return the latest output returned by the update() function
     */
    public double getLastOutput() {
        return lastOutput;
    }

    /**
     * Clamp a value between a minimum and maximum.
     *
     * @param val the value to clamp
     * @param min minimum allowed
     * @param max maximum allowed
     * @return val constrained to the [min, max] range
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
