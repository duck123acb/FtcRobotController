package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.ElapsedTime;

public class PID {
    private double kp, ki, kd;

    private double integralSum = 0;
    private double lastError = 0;
    private double lastOutput = 0;

    private final ElapsedTime timer = new ElapsedTime();

    // to avoid integral going crazy
    private double integralLimit = 1.0; // tweak if needed

    public PID(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        timer.reset();
    }

    /**
     * Updates the PID controller output based on the target and current values.
     * <p>
     * This method computes the proportional, integral, and derivative terms
     * using the elapsed time since the last update. Integral windup is prevented
     * by clamping the accumulated error within the defined limits.
     *
     * @param target the desired setpoint value
     * @param current the current measured value
     * @return the computed PID controller output
     *
     * @implNote
     * - The method automatically handles cases where the elapsed time (dt) is zero
     *   by substituting a small default value (1e-3 seconds).
     * - The timer used should measure the time between successive calls to this method.
     */
    public double update(double target, double current) {
        double dt = timer.seconds();
        if (dt <= 0) dt = 1e-3; // safety to not divide by zero

        double error = target - current;

        // integral with anti-windup
        integralSum += error * dt;
        integralSum = clamp(integralSum, -integralLimit, integralLimit);

        double derivative = (error - lastError) / dt;

        double output = (kp * error) + (ki * integralSum) + (kd * derivative);

        lastError = error;
        lastOutput = output;
        timer.reset();

        return output;
    }

    public void reset() {
        integralSum = 0;
        lastError = 0;
        timer.reset();
    }

    public void setIntegralLimit(double limit) {
        this.integralLimit = limit;
    }

    public double getLastOutput() {
        return lastOutput;
    }

    /**
     * Constrains a value to lie within a specified minimum and maximum range.
     * <p>
     * If the given value is less than {@code min}, {@code min} is returned.
     * If it is greater than {@code max}, {@code max} is returned.
     * Otherwise, the original value is returned.
     *
     * @param val the value to be clamped
     * @param min the minimum allowable value
     * @param max the maximum allowable value
     * @return the clamped value within the range [{@code min}, {@code max}]
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
