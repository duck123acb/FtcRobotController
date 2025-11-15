package org.firstinspires.ftc.teamcode.simulation;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

/**
 * A mock implementation of the DcMotor for simulation.
 * This allows testing motor-related logic without a physical robot.
 */
public class MockDcMotor implements DcMotor {

    private int currentPosition = 0;
    private int targetPosition = 0;
    private double power = 0.0;
    private RunMode mode = RunMode.RUN_WITHOUT_ENCODER;
    private Direction direction = Direction.FORWARD;

    @Override
    public void setTargetPosition(int pos) {
        this.targetPosition = pos;
    }

    @Override
    public int getTargetPosition() {
        return targetPosition;
    }

    @Override
    public boolean isBusy() {
        // Simulate isBusy: true if mode is RUN_TO_POSITION and not at target
        if (mode != RunMode.RUN_TO_POSITION) {
            return false;
        }
        // A simple simulation: consider it "busy" if it's not at the target.
        // A more advanced mock could simulate the motor moving over time.
        return Math.abs(currentPosition - targetPosition) > 10; // 10 counts tolerance
    }

    @Override
    public int getCurrentPosition() {
        // If we are in RUN_TO_POSITION, simulate reaching the target instantly.
        if (mode == RunMode.RUN_TO_POSITION) {
            currentPosition = targetPosition;
        }
        return currentPosition;
    }

    @Override
    public void setMode(RunMode mode) {
        this.mode = mode;
        if (mode == RunMode.STOP_AND_RESET_ENCODER) {
            this.currentPosition = 0;
            this.targetPosition = 0;
        }
    }

    @Override
    public RunMode getMode() {
        return mode;
    }

    @Override
    public void setPower(double power) {
        this.power = power;
    }

    @Override
    public double getPower() {
        return power;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    // --- Unimplemented methods required by the interface ---
    // These can be left as-is because they are not used in BallChase.java

    @Override
    public MotorConfigurationType getMotorType() { return null; }

    @Override
    public void setMotorType(MotorConfigurationType motorType) {}

    @Override
    public DcMotorController getController() { return null; }

    @Override
    public int getPortNumber() { return 0; }

    @Override
    public void setZeroPowerBehavior(ZeroPowerBehavior zeroPowerBehavior) {}

    @Override
    public ZeroPowerBehavior getZeroPowerBehavior() { return null; }

    @Override
    public void setPowerFloat() {}

    @Override
    public boolean getPowerFloat() { return false; }

    @Override
    public void setTargetPositionTolerance(int tolerance) {}

    @Override
    public int getTargetPositionTolerance() { return 0; }

    @Override
    public Manufacturer getManufacturer() { return null; }

    @Override
    public String getDeviceName() { return "MockDcMotor"; }

    @Override
    public String getConnectionInfo() { return ""; }

    @Override
    public int getVersion() { return 1; }

    @Override
    public void resetDeviceConfigurationForOpMode() {}

    @Override
    public void close() {}
}

