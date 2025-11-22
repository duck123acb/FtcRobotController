package org.firstinspires.ftc.teamcode.TeleOp;

import com.qualcomm.robotcore.hardware.DcMotor;

public class TakeSystem {
    DcMotor leftMotor, rightMotor;

    public TakeSystem(DcMotor leftMotor, DcMotor rightMotor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
    }

    public void spin(double power) {
        leftMotor.setPower(power);
        rightMotor.setPower(power);
    }

    public void stop() {
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }
}
