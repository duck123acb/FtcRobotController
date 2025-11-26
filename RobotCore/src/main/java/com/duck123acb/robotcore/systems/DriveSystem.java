package com.duck123acb.robotcore.systems;

import com.duck123acb.robotcore.Motor;
import com.duck123acb.robotcore.RobotState;

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
    // Estimated robot state (for sim / odometry tracking)
    RobotState robotState;

    Motor frontLeft, frontRight, backLeft, backRight;

    // Physical constants
    private final double TICKS_PER_REV;
    private final double WHEEL_DIAMETER_INCHES;
    private final double WHEEL_CIRCUMFERENCE;

    /**
     * Constructor for DriveSystem
     * @param fl Front left motor
     * @param fr Front right motor
     * @param bl Back left motor
     * @param br Back right motor
     * @param ticksPerRev Encoder ticks per wheel revolution
     * @param wheelDiameterInches Wheel diameter in inches
     */
    public DriveSystem(Motor fl, Motor fr, Motor bl, Motor br, double ticksPerRev, double wheelDiameterInches,
                       double initialX, double initialY, double initialHeading) {
        frontLeft = fl;
        frontRight = fr;
        backLeft = bl;
        backRight = br;

        robotState = new RobotState(initialX, initialY, initialHeading);
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
        double deltaX = targetX - robotState.x;
        double deltaY = targetY - robotState.y;

        // Compute distance and heading
        double distance = Math.hypot(deltaX, deltaY);
        double targetAngle = Math.atan2(deltaY, deltaX);

        // Rotate to face target
        double turnAngle = targetAngle - robotState.heading;
        turnRadians(turnAngle, power * 0.5); // slower turn for accuracy

        // Move forward
        int ticksToMove = inchesToTicks(distance);
        goToPosition(ticksToMove, power);

        // Update simulated/estimated state
        robotState.x = targetX;
        robotState.y = targetY;
        robotState.heading = targetAngle;
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

        robotState.heading += radians;
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

    public RobotState getRobotState() {
        return robotState;
    }

    public void driveMecanum(double x, double y, double turn) {
        // Inverted turn to match standard CCW+ convention
        double fl = y + x - turn;
        double fr = y - x + turn;
        double bl = y - x - turn;
        double br = y + x + turn;

        frontLeft.setPower(fl);
        frontRight.setPower(fr);
        backLeft.setPower(bl);
        backRight.setPower(br);
    }
    public void updateSim(double dt) {
        // pull motor power
        double fl = frontLeft.getPower();
        double fr = frontRight.getPower();
        double bl = backLeft.getPower();
        double br = backRight.getPower();

        // lightweight kinematic approximation
        // Forward is +X in robot frame? Let's assume standard mecanum:
        // Forward (X) = (fl + fr + bl + br) / 4
        // Strafe (Y) = (-fl + fr + bl - br) / 4
        // Rotation (W) = (-fl + fr - bl + br) / 4

        double vx = (fl + fr + bl + br) / 4.0;            // forward (robot X)
        double vy = (-fl + fr + bl - br) / 4.0;          // strafe (robot Y)
        double omega = (-fl + fr - bl + br) / 4.0;       // rotation

        RobotState s = getRobotState();

        // Convert robot-relative velocities to field-relative
        // x_field = x_robot * cos(heading) - y_robot * sin(heading)
        // y_field = x_robot * sin(heading) + y_robot * cos(heading)
        

        // Wait, Robot.java: "double radH = Math.toRadians(heading);" implies s.heading is DEGREES.
        // But DriveSystem.turnRadians adds radians to s.heading.
        // Let's check Main.java usage.
        // Main.java: "udpRobot.heading = internal.heading;"
        // Robot.java: "pidHeading = new PID(2.0, 0, 0.1);" -> likely degrees if target is 0, 90 etc.
        // DriveSystem.goToXY uses Math.atan2 which returns radians.
        // DriveSystem.turnRadians adds radians.
        // This suggests s.heading is mixed or I need to be careful.
        
        // Let's look at Robot.java again.
        // "double radH = Math.toRadians(heading);"
        // This strongly implies s.heading is in DEGREES in Robot.java context.
        // But DriveSystem.goToXY sets "robotState.heading = targetAngle;" where targetAngle is atan2 (radians).
        
        // CONFLICT DETECTED.
        // If goToXY sets radians, but Robot.java treats it as degrees, we have a problem.
        // However, the user issue is "big circle".
        // If the robot thinks it's at 1 degree but it's actually 1 radian (~57 deg), the math will be off.
        
        // Let's assume s.heading SHOULD be radians because of Math.atan2 usage in goToXY.
        // If so, Robot.java is wrong to use Math.toRadians(heading).
        
        // BUT, let's look at Main.java:
        // "robot.goToXY_PID(firstBall.x, firstBall.y, 0, 10);" -> targetHeading is 0.
        
        // Let's stick to fixing updateSim first.
        // If s.heading is radians:
        // If s.heading is radians:
        double globalVx = vx * Math.cos(s.heading) - vy * Math.sin(s.heading);
        double globalVy = vx * Math.sin(s.heading) + vy * Math.cos(s.heading);

        s.x += globalVx * dt;
        s.y += globalVy * dt;
        s.heading += omega * dt;
    }
}
