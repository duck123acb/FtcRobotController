package com.duck123acb.robotcore;

import com.duck123acb.robotcore.systems.DriveSystem;
import com.duck123acb.robotcore.systems.TakeSystem;

/*
MECANUM WHEEL ROBOT
INTAKE/OUTTAKE
*/
public class Robot {
    DriveSystem driveSystem;
    TakeSystem intakeSystem, outtakeSystem;

    // --- PID controllers for XY + heading ---
    private PID pidX;
    private PID pidY;
    private PID pidHeading;

    public Robot(Motor fl, Motor fr, Motor bl, Motor br,
                 Motor il, Motor ir, Motor ol, Motor or) {

        driveSystem = new DriveSystem(fl, fr, bl, br, 537.6, 4); // FIXME: measurements
        intakeSystem = new TakeSystem(il, ir);
        outtakeSystem = new TakeSystem(ol, or);

        // PID init (tune later)
        pidX = new PID(0.05, 0, 0.002);
        pidY = new PID(0.05, 0, 0.002);
        pidHeading = new PID(2.0, 0, 0.1);
    }

    public RobotState getState() {
        return driveSystem.getRobotState();
    }

    // ============================================================
    //                  PID XY + Heading Control
    // ============================================================

    public void goToXY_PID(double targetX, double targetY, double targetHeading) {
        RobotState state = driveSystem.getRobotState();

        double x = state.x;
        double y = state.y;
        double heading = state.heading;

        // PID outputs in FIELD space
        double vx = pidX.update(targetX, x);
        double vy = pidY.update(targetY, y);
        double omega = pidHeading.update(targetHeading, heading);

        // convert to ROBOT space (field-relative drive)
        double cosH = Math.cos(heading);
        double sinH = Math.sin(heading);

        double robotX = vx * cosH - vy * sinH;
        double robotY = vx * sinH + vy * cosH;

        // send to mecanum mixer
        driveSystem.driveMecanum(robotX, robotY, omega);
        driveSystem.updateSim(0.02);

    }
}