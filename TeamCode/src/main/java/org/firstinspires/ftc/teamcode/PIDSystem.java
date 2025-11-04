package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

public class PIDSystem {

    private final double kp, ki, kd;
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

    /**
     * Example method to drive the robot to a specific position using PID.
     * This should be adapted and used within a LinearOpMode.
     *
     * @param opMode The active LinearOpMode.
     * @param frontLeft The front left motor.
     * @param frontRight The front right motor.
     * @param backLeft The back left motor.
     * @param backRight The back right motor.
     * @param targetPosition The target position for the encoders.
     * @param tolerance The allowed error tolerance.
     */
    public void driveToPosition(LinearOpMode opMode,
        DcMotor frontLeft, DcMotor frontRight,
        DcMotor backLeft, DcMotor backRight,
        int targetPosition, double tolerance) {

        // It's better to use RUN_TO_POSITION mode if available, but for a manual PID, here is an example.
        // Reset encoders if needed.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        reset(); // Reset PID controller

        while (opMode.opModeIsActive() && Math.abs(targetPosition - frontLeft.getCurrentPosition()) > tolerance) {
            double currentPosition = frontLeft.getCurrentPosition();
            double power = update(targetPosition, currentPosition);

            // Apply power to all motors to move forward/backward
            frontLeft.setPower(power);
            frontRight.setPower(power);
            backLeft.setPower(power);
            backRight.setPower(power);

            opMode.telemetry.addData("Current Position", currentPosition);
            opMode.telemetry.addData("Target Position", targetPosition);
            opMode.telemetry.addData("Power", power);
            opMode.telemetry.update();
        }

        // Stop all motors
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}
