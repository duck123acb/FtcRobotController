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
        double omega = pidHeading.update(targetHeading, heading);

        // convert to ROBOT space (field-relative drive)
        double radH = heading; // heading is already in radians


        double robotX = vx * Math.sin(radH) - vy * Math.cos(radH);
        double robotY = vx * Math.cos(radH) + vy * Math.sin(radH);

        // send to mecanum mixer
        driveSystem.driveMecanum(robotX, robotY, omega);
        driveSystem.updateSim(0.02);

    }
}