package com.duck123acb.robotcore.systems;

import com.duck123acb.robotcore.Motor;

/**
 * DriveSystem handles all robot movement for FTC DECODE.
 * Works with both real DcMotors (via RealMotor wrapper) and simulated FakeMotors.
 * Features include:
 * - Basic RUN_TO_POSITION encoder movement
 * - Tank drive
 * - Mecanum strafing
 * - Field-relative XY movement
 * - Turning by radians
 * - Encoder and power helpers
 */
public class DriveSystem {
    Motor frontLeft, frontRight, backLeft, backRight;

    // Physical constants
    private final double TICKS_PER_REV;
    private final double WHEEL_DIAMETER_INCHES;
    private final double WHEEL_CIRCUMFERENCE;

    // Estimated robot state (for sim / odometry tracking)
    private double posX = 0;
    private double posY = 0;
    private double heading = 0; // radians, 0 = forward

    /**
     * Constructor for DriveSystem
     * @param fl Front left motor
     * @param fr Front right motor
     * @param bl Back left motor
     * @param br Back right motor
     * @param ticksPerRev Encoder ticks per wheel revolution
     * @param wheelDiameterInches Wheel diameter in inches
     */
    public DriveSystem(Motor fl, Motor fr, Motor bl, Motor br, double ticksPerRev, double wheelDiameterInches) {
        frontLeft = fl;
        frontRight = fr;
        backLeft = bl;
        backRight = br;

        this.TICKS_PER_REV = ticksPerRev;
        this.WHEEL_DIAMETER_INCHES = wheelDiameterInches;
        this.WHEEL_CIRCUMFERENCE = Math.PI * wheelDiameterInches;
    }

    /**
     * Move robot forward/backward a specific number of encoder ticks
     * @param ticks Number of encoder ticks to move
     * @param power Motor power to apply (0-1)
     */
    public void goToPosition(int ticks, double power) {
        resetEncoders();
        setTargetPosition(ticks, ticks, ticks, ticks);
        runToPosition();
        setPower(power);
    }

    /**
     * Move robot to a field-relative X/Y coordinate
     * @param targetX Target X position (in inches)
     * @param targetY Target Y position (in inches)
     * @param power Motor power to use
     */
    public void goToXY(double targetX, double targetY, double power) {
        // Compute delta
        double deltaX = targetX - posX;
        double deltaY = targetY - posY;

        // Compute distance and heading
        double distance = Math.hypot(deltaX, deltaY);
        double targetAngle = Math.atan2(deltaY, deltaX);

        // Rotate to face target
        double turnAngle = targetAngle - heading;
        turnRadians(turnAngle, power * 0.5); // slower turn for accuracy

        // Move forward
        int ticksToMove = inchesToTicks(distance);
        goToPosition(ticksToMove, power);

        // Update simulated/estimated state
        posX = targetX;
        posY = targetY;
        heading = targetAngle;
    }

    /**
     * Turn robot in place by a certain angle in radians
     * @param radians Angle to turn (positive = counterclockwise)
     * @param power Motor power to use for turning
     */
    public void turnRadians(double radians, double power) {
        // Estimate distance each wheel must travel
        double ROBOT_RADIUS = 6; // inches from center to wheel (adjust for your bot)
        double arcLength = ROBOT_RADIUS * radians;
        int ticks = inchesToTicks(arcLength);

        // Turn: left wheels forward, right wheels backward
        setTargetPosition(ticks, -ticks, ticks, -ticks);
        runToPosition();
        setPower(power);

        heading += radians;
    }

    /**
     * Tank drive: forward/backward and turn
     * @param forward Forward/backward power (-1 to 1)
     * @param turn Turning power (-1 to 1)
     */
    public void drive(double forward, double turn) {
        double leftPower = forward + turn;
        double rightPower = forward - turn;
        frontLeft.setPower(leftPower);
        backLeft.setPower(leftPower);
        frontRight.setPower(rightPower);
        backRight.setPower(rightPower);
    }

    /**
     * Simple mecanum strafe
     * @param power Power for strafing (-1 to 1)
     */
    public void strafe(double power) {
        frontLeft.setPower(power);
        backLeft.setPower(-power);
        frontRight.setPower(-power);
        backRight.setPower(power);
    }

    /**
     * Stop all motors
     */
    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }

    /**
     * Reset all motor encoders
     */
    public void resetEncoders() {
        frontLeft.setMode(Motor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(Motor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(Motor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(Motor.RunMode.STOP_AND_RESET_ENCODER);
    }

    /**
     * Set individual target positions for each motor
     * @param flTicks Front left motor ticks
     * @param frTicks Front right motor ticks
     * @param blTicks Back left motor ticks
     * @param brTicks Back right motor ticks
     */
    public void setTargetPosition(int flTicks, int frTicks, int blTicks, int brTicks) {
        frontLeft.setTargetPosition(flTicks);
        frontRight.setTargetPosition(frTicks);
        backLeft.setTargetPosition(blTicks);
        backRight.setTargetPosition(brTicks);
    }

    /**
     * Set all motors to RUN_TO_POSITION mode
     */
    public void runToPosition() {
        frontLeft.setMode(Motor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(Motor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(Motor.RunMode.RUN_TO_POSITION);
        backRight.setMode(Motor.RunMode.RUN_TO_POSITION);
    }

    /**
     * Set power for all motors
     * @param power Motor power to apply (0-1)
     */
    public void setPower(double power) {
        frontLeft.setPower(power);
        frontRight.setPower(power);
        backLeft.setPower(power);
        backRight.setPower(power);
    }

    /**
     * Convert linear distance in inches to encoder ticks
     * @param inches Distance to travel
     * @return Encoder ticks equivalent
     */
    private int inchesToTicks(double inches) {
        return (int)((inches / WHEEL_CIRCUMFERENCE) * TICKS_PER_REV);
    }
}
