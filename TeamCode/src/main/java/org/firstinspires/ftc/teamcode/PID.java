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

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
