package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

public class TakeSystem {
    DcMotor leftMotor, rightMotor;

    TakeSystem(DcMotor leftMotor, DcMotor rightMotor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
    }

    public void spin() {
        leftMotor.setPower(1);
        leftMotor.setPower(1);
    }


    public void stop() {
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }
}
