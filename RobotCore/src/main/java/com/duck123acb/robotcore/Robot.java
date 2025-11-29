package com.duck123acb.robotcore;

import com.duck123acb.robotcore.systems.DriveSystem;
import com.duck123acb.robotcore.systems.TakeSystem;

/*
MECANUM WHEEL ROBOT
INTAKE/OUTTAKE
*/
public class Robot {
    public DriveSystem driveSystem;
    public TakeSystem intakeSystem, outtakeSystem;

    // --- PID controllers for XY + heading ---
    private final PID pidX;
    private final PID pidY;
    private final PID pidHeading;

    public Robot(Motor fl, Motor fr, Motor bl, Motor br,
                 Motor il, Motor ir, Motor ol, Motor or,
                 double initialX, double initialY, double initialHeading) {

        driveSystem = new DriveSystem(fl, fr, bl, br, 537.6, 4, initialX, initialY, initialHeading); // FIXME: measurements
        intakeSystem = new TakeSystem(il, ir);
        outtakeSystem = new TakeSystem(ol, or);

        // PID init (tune later)
        pidX = new PID(0.05, 0, 0.002);
        pidY = new PID(0.05, 0, 0.002);
        pidHeading = new PID(2.0, 0, 0.1);
    }

    public void resetPID() {
        pidX.reset();
        pidY.reset();
        pidHeading.reset();
    }

    public RobotState getState() {
        return driveSystem.getRobotState();
    }

    // ============================================================
    //                  PID XY + Heading Control
    // ============================================================

    public void goToXY_PID(double targetX, double targetY, double targetHeading, float speed) {
        RobotState state = driveSystem.getRobotState();

        double x = state.x;
        double y = state.y;
        double heading = state.heading;

        // PID outputs in FIELD space
        double vx = pidX.update(targetX, x) * speed;
        double vy = pidY.update(targetY, y) * speed;
        double omega = pidHeading.updateAngle(targetHeading, heading);

        // convert to ROBOT space (field-relative drive)
        double radH = heading; // heading is in radians

        double robotX = vx * Math.sin(radH) - vy * Math.cos(radH);
        double robotY = vx * Math.cos(radH) + vy * Math.sin(radH);

        // send to mecanum mixer
        driveSystem.driveMecanum(robotX, robotY, omega);
        driveSystem.updateSim(0.02);
    }

    /**
     * Turn the robot to a specific heading using PID.
     * @param targetHeading Target heading in radians.
     */
    public void turnPID(double targetHeading) {
        RobotState state = driveSystem.getRobotState();
        double omega = pidHeading.updateAngle(targetHeading, state.heading);
        driveSystem.driveMecanum(0, 0, omega);
        driveSystem.updateSim(0.02);
    }

    /**
     * Drive the robot in a specific direction (field-centric) with a given speed.
     * @param directionRadians Direction to move in radians (0 is East, PI/2 is North).
     * @param speed Speed magnitude (0-1).
     * @param targetHeading Target heading to maintain (radians).
     */
    public void driveDirection(double directionRadians, double speed, double targetHeading) {
        RobotState state = driveSystem.getRobotState();
        
        // Calculate field-centric velocities
        double vx = speed * Math.cos(directionRadians);
        double vy = speed * Math.sin(directionRadians);
        
        // Calculate rotation to maintain heading
        double omega = pidHeading.updateAngle(targetHeading, state.heading);
        
        // Convert to robot-centric
        double radH = state.heading;
        double robotX = vx * Math.sin(radH) - vy * Math.cos(radH);
        double robotY = vx * Math.cos(radH) + vy * Math.sin(radH);
        
        driveSystem.driveMecanum(robotX, robotY, omega);
        driveSystem.updateSim(0.02);
    }
}