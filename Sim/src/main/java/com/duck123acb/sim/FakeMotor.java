package com.duck123acb.sim;

import com.duck123acb.robotcore.Motor;

public class FakeMotor implements Motor {
    private double power;
    private int targetPosition;
    private RunMode mode = RunMode.RUN_WITHOUT_ENCODER;

    public FakeMotor() {}

    @Override
    public void setPower(double power) { this.power = power; }
    @Override
    public double getPower() { return power; }
    @Override
    public void setTargetPosition(int position) { this.targetPosition = position; }
    @Override
    public int getTargetPosition() { return targetPosition; }
    @Override
    public void setMode(Motor.RunMode mode) { this.mode = mode; }
    @Override
    public RunMode getMode() { return mode; }
}
