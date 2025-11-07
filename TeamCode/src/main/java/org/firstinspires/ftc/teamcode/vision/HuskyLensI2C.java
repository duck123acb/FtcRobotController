package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

import java.util.ArrayList;


/*
TODO:

Phase 1: Hardware Setup
    Plug HuskyLens into the I2C port on your Control Hub using the 4-wire cable.
    Power it on; the screen should light up with a live camera feed.
    Open your FTC Robot Configuration and give it a name (like "camera").
    Make sure your Control Hub recognizes it—no error messages on startup.

Phase 2: Training the Balls
    On HuskyLens, tap “Learn” → Color Recognition.
    Place the first ball (green) in front of the camera.
    Tap “Add” to capture it; note the ID assigned.
    Repeat for the second ball (purple). Record its ID too.

Tap “Detect” to make sure both balls are being tracked correctly.

Phase 3: Code Preparation
    Update your HuskyLensI2C class (you already have a modern version).
    In your OpMode, define the IDs you just noted:
    private static final int GREEN_BALL_ID = 1;
    private static final int PURPLE_BALL_ID = 2;
    Use the “closest ball” logic: largest bounding box as a proxy for distance.

Phase 4: Testing & Telemetry
    Run your test OpMode.
    Check telemetry for xCenter, yCenter, width, height of each ball.
    Make sure values change as you move the balls in front of the camera.
    If all zeros or errors, troubleshoot:
        Recheck I2C connection.
        Make sure IDs in code match IDs on HuskyLens.
        Make sure you selected Detect mode.

Phase 5: PID Integration
    Feed xCenter into your PID controller for horizontal movement.
    Feed yCenter (or width/area) for forward/back distance control.
    Test with one ball first, then expand to handle both balls.

 */

public class HuskyLensI2C {
    private I2cDeviceSynch device;

    // default HuskyLens I2C address: 0
    // x32 (decimal 50)
    private static final I2cAddr HUSKYLENS_ADDRESS = I2cAddr.create7bit(0x32);

    public static class HuskyLensBlock {
        public int xCenter;
        public int yCenter;
        public int width;
        public int height;
        public int ID;

        public HuskyLensBlock(int xCenter, int yCenter, int width, int height, int ID) {
            this.xCenter = xCenter;
            this.yCenter = yCenter;
            this.width = width;
            this.height = height;
            this.ID = ID;
        }
    }

    public HuskyLensI2C(HardwareMap hardwareMap, String i2cName) {
        device = hardwareMap.get(I2cDeviceSynch.class, i2cName);
        device.setI2cAddress(HUSKYLENS_ADDRESS);
        device.engage();
    }

    public byte[] readRaw(int length) {
        return device.read(0, length);
    }

    public void close() {
        device.disengage();
    }

    /**
     * Reads all detected blocks from HuskyLens.
     * @param algorithm Either BLOCK or COLOR_RECOGNITION
     * @return Array of detected blocks
     */
    public HuskyLensBlock[] getBlocks(Algorithm algorithm) {
        // Example read length, actual may vary by HuskyLens protocol
        byte[] raw = readRaw(32); // you may need to adjust this based on how many blocks you expect

        ArrayList<HuskyLensBlock> blocks = new ArrayList<>();

        // Simple parser placeholder: assume each block takes 6 bytes
        // byte layout (example):
        // [ID][xHigh][xLow][yHigh][yLow][widthHigh][widthLow][heightHigh][heightLow]
        for (int i = 0; i + 8 <= raw.length; i += 9) {
            int id = raw[i] & 0xFF;
            int x = ((raw[i + 1] & 0xFF) << 8) | (raw[i + 2] & 0xFF);
            int y = ((raw[i + 3] & 0xFF) << 8) | (raw[i + 4] & 0xFF);
            int w = ((raw[i + 5] & 0xFF) << 8) | (raw[i + 6] & 0xFF);
            int h = ((raw[i + 7] & 0xFF) << 8) | (raw[i + 8] & 0xFF);

            blocks.add(new HuskyLensBlock(x, y, w, h, id));
        }

        return blocks.toArray(new HuskyLensBlock[0]);
    }

    public HuskyLensBlock getClosestBlock(HuskyLensBlock[] blocks, int targetID) {
        HuskyLensBlock closest = null;
        for (HuskyLensBlock b : blocks) {
            if (b.ID == targetID) {
                int area = b.width * b.height;
                if (closest == null || area > closest.width * closest.height) {
                    closest = b;
                }
            }
        }
        return closest;
    }
}
