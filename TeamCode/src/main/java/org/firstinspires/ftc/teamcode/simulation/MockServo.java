package org.firstinspires.ftc.teamcode.simulation;

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

/**
 * A mock implementation of the Servo for simulation.
 */
public class MockServo implements Servo {

    private double position = 0.0;
    private Direction direction = Direction.FORWARD;

    @Override
    public void setPosition(double position) {
        this.position = position;
    }

    @Override
    public double getPosition() {
        return position;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    // --- Unimplemented methods ---

    @Override
    public void scaleRange(double min, double max) {}

    @Override
    public ServoController getController() { return null; }

    @Override
    public int getPortNumber() { return 0; }

    @Override
    public Manufacturer getManufacturer() { return Manufacturer.Other; }

    @Override
    public String getDeviceName() { return "MockServo"; }

    @Override
    public String getConnectionInfo() { return ""; }

    @Override
    public int getVersion() { return 1; }

    @Override
    public void resetDeviceConfigurationForOpMode() {}

    @Override
    public void close() {}
}

