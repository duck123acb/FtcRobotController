package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

public class HuskyLensI2C {
    private I2cDeviceSynch device;

    // Default HuskyLens I2C address: 0x32 (decimal 50)
    private static final I2cAddr HUSKYLENS_ADDR = I2cAddr.create7bit(0x32);

    public HuskyLensI2C(HardwareMap hardwareMap, String i2cName) {
        device = hardwareMap.get(I2cDeviceSynch.class, i2cName);
        device.setI2cAddress(HUSKYLENS_ADDR);

        device.engage();
    }

    // Basic request frame for data (HuskyLens protocol)
    // For now, we’ll just read raw bytes so we confirm communication
    public byte[] readRaw(int length) {
        return device.read(0, length);
    }

    public void close() {
        device.disengage();
    }
}
