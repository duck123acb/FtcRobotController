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

    public Robot(Motor fl, Motor fr, Motor bl, Motor br, Motor il, Motor ir, Motor ol, Motor or) {
        driveSystem = new DriveSystem(fl, fr, bl, br, 537.6, 4); // FIXME: add required measurements
        intakeSystem = new TakeSystem(il, ir);
        outtakeSystem = new TakeSystem(ol, or);
    }
}