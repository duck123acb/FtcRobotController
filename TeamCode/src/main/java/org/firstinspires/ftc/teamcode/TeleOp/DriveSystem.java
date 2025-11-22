package org.firstinspires.ftc.teamcode.TeleOp;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

public class DriveSystem {
    DcMotor frontLeft, frontRight, backLeft, backRight;
    public DriveSystem(DcMotor fl, DcMotor fr, DcMotor bl, DcMotor br) {
        frontLeft = fl;
        frontRight = fr;
        backLeft = bl;
        backRight = br;
    }

    public void drive(Gamepad gamepad) {
        double y = gamepad.left_stick_y;   // forward/backward
        double x = -gamepad.left_stick_x;    // strafe left/right
        double rx = -gamepad.right_stick_x;  // rotate right/left

        // basic mecanum drive math
        double fl = y + x + rx;
        double bl = y - x + rx;
        double fr = y - x - rx;
        double br = y + x - rx;

        // normalize if any value is above 1
        double max = Math.max(Math.max(Math.abs(fl), Math.abs(fr)), Math.max(Math.abs(bl), Math.abs(br)));
        if (max > 1.0) {
            fl /= max;
            fr /= max;
            bl /= max;
            br /= max;
        }

        // apply calculated powers to motors
        frontLeft.setPower(fl);
        backLeft.setPower(bl);
        frontRight.setPower(fr);
        backRight.setPower(br);
    }
}
