package com.duck123acb.sim;

import com.duck123acb.robotcore.Motor;

public class FakeMotor implements Motor {
    private double power;
    private int targetPosition;
    private RunMode mode = RunMode.RUN_WITHOUT_ENCODER;

    // tie this to your simulated robot's drivetrain
    private final SimRobot robot;
    private final boolean isLeft;

    public FakeMotor(SimRobot robot, boolean isLeft) {
        this.robot = robot;
        this.isLeft = isLeft;
    }

    @Override
    public void setPower(double power) {
        this.power = power;
        robot.applyMotorPower(isLeft, power);
    }

    @Override
    public double getPower() {
        return power;
    }

    @Override
    public void setTargetPosition(int position) {
        this.targetPosition = position;
    }

    @Override
    public int getTargetPosition() {
        return targetPosition;
    }

    @Override
    public void setMode(RunMode mode) {
        this.mode = mode;
    }

    @Override
    public RunMode getMode() {
        return mode;
    }
}
