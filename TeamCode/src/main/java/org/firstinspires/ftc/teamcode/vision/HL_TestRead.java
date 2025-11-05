package org.firstinspires.ftc.teamcode.vision;

// testing the husky cam
/*
    STEPS:
    Plug HuskyLens into I2C port on Control Hub
    Name it “camera” in configuration
    Run this OpMode
    Check telemetry

    FAILS IF:
    all zeros
    throwing I2C errors
    Hex values not changing when the HuskyLens is running
 */

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "HL_TestRead")
public class HL_TestRead extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        HuskyLensI2C husky = new HuskyLensI2C(hardwareMap, "camera");

        telemetry.addLine("Initialized. Press start to read I2C");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            byte[] data = husky.readRaw(16); // read first 16 bytes

            telemetry.addData("Raw Data", bytesToHex(data));
            telemetry.update();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
