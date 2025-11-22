package org.firstinspires.ftc.teamcode;

import com.duck123acb.robotcore.Motor;
import com.qualcomm.robotcore.hardware.DcMotor;

public class RealMotor implements Motor {
    private final DcMotor motor;

    public RealMotor(DcMotor motor) {
        this.motor = motor;
    }

    @Override
    public void setPower(double power) {
        motor.setPower(power);
    }

    @Override
    public double getPower() {
        return motor.getPower();
    }

    @Override
    public void setTargetPosition(int position) {
        motor.setTargetPosition(position);
    }

    @Override
    public int getTargetPosition() {
        return motor.getTargetPosition();
    }

    @Override
    public void setMode(RunMode mode) {
        switch (mode) {
            case RUN_WITHOUT_ENCODER:
                motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                break;
            case RUN_TO_POSITION:
                motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                break;
            case STOP_AND_RESET_ENCODER:
                motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                break;
        }
    }

    @Override
    public RunMode getMode() {
        DcMotor.RunMode m = motor.getMode();
        if (m == DcMotor.RunMode.RUN_WITHOUT_ENCODER) return RunMode.RUN_WITHOUT_ENCODER;
        if (m == DcMotor.RunMode.RUN_TO_POSITION) return RunMode.RUN_TO_POSITION;
        return RunMode.STOP_AND_RESET_ENCODER;
    }
}
