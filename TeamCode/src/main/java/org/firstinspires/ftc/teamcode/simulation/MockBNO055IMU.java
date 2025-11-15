package org.firstinspires.ftc.teamcode.simulation;

import com.qualcomm.hardware.bosch.BNO055IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * A mock implementation of the BNO055IMU for simulation.
 * This allows testing orientation-related logic without a physical IMU.
 */
public class MockBNO055IMU implements BNO055IMU {

    private double simulatedAngle = 0.0;

    @Override
    public boolean initialize(Parameters parameters) {
        // Simulate successful initialization
        return true;
    }

    @Override
    public Orientation getAngularOrientation() {
        // Return a new Orientation object with the simulated angle.
        // The other values are not used in BallChase.java, so they can be 0.
        return new Orientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES,
                (float) simulatedAngle, 0, 0, 0);
    }

    /**
     * Sets the simulated angle for the IMU.
     * @param angle The angle in degrees to simulate.
     */
    public void setSimulatedAngle(double angle) {
        this.simulatedAngle = angle;
    }

    // --- Unimplemented methods required by the interface ---
    // These can be left as-is because they are not used in BallChase.java

    @Override
    public Orientation getAngularOrientation(AxesReference reference, AxesOrder order, AngleUnit angleUnit) {
        return getAngularOrientation();
    }

    // Other BNO055IMU methods would go here, but are not needed for this simulation.

    @Override
    public SystemStatus getSystemStatus() { return null; }

    @Override
    public SystemError getSystemError() { return null; }

    @Override
    public CalibrationStatus getCalibrationStatus() { return null; }

    @Override
    public Manufacturer getManufacturer() { return Manufacturer.Other; }

    @Override
    public String getDeviceName() { return "MockBNO055IMU"; }

    @Override
    public String getConnectionInfo() { return ""; }

    @Override
    public int getVersion() { return 1; }

    @Override
    public void resetDeviceConfigurationForOpMode() {}

    @Override
    public void close() {}
}

