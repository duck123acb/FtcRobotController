package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

public class IntakeSystem {
    DcMotor leftIntakeMotor, rightIntakeMotor;
    DcMotor flingMotor;

    IntakeSystem(DcMotor leftIntakeMotor, DcMotor rightIntakeMotor, DcMotor flingMotor) {
        this.leftIntakeMotor = leftIntakeMotor;
        this.rightIntakeMotor = rightIntakeMotor;

        this.flingMotor = flingMotor;
    }

    public void intake() {
        leftIntakeMotor.setPower(1);
        rightIntakeMotor.setPower(1);
    }
    public void outtake() {
        leftIntakeMotor.setPower(-1);
        rightIntakeMotor.setPower(-1);
    }
    public void stop() {
        leftIntakeMotor.setPower(0);
        rightIntakeMotor.setPower(0);
    }

    public void moveFlingMotor(int position) {
        flingMotor.setTargetPosition(position);
    }
}
