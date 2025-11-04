package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.ElapsedTime;

public class PIDSystem {
    double kp, ki, kd;
    private double integralSum = 0;
    private double lastError = 0;

    private final ElapsedTime timer = new ElapsedTime();

    public PIDSystem(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public double update(double target, double current) {
        double error = target - current;
        double derivative = (error - lastError) / timer.seconds();
        integralSum += error * timer.seconds();

        lastError = error;
        timer.reset();

        return (kp * error) + (ki * integralSum) + (kd * derivative);
    }

    public void reset() {
        integralSum = 0;
        lastError = 0;
        timer.reset();
    }
}
