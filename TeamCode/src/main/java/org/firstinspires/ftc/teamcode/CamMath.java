package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.duck123acb.robotcore.Robot;

public class CamMath {
    // HuskyLens resolution
    static final double CAM_W = 320.0;
    static final double CAM_H = 240.0;

    // HuskyLens FOV (in degrees)
    static final double FOV_H = 52.0; // FIXME: tune from 50-55 degrees
    static final double FOV_V = 39.0;

    // crude distance model based on pixel Y
    // tune this with real data
    public static double estimateDistance(HuskyLens.Block b) {
        double cy = (CAM_H / 2.0) - b.y;  // positive = closer to center
        return 24 + (cy * 0.25);         // FIXME: tweak numbers after testing
    }

    // converts a block into a field-space (x,y) target
    public static double[] cameraToField(HuskyLens.Block b, Robot robot) {
        // --- 1. pixel → angle ---
        double cx = b.x - (CAM_W / 2.0);  // -160 → +160
        double cy = (CAM_H / 2.0) - b.y;  // +up

        double yawOffsetDeg   = (cx / (CAM_W / 2.0)) * (FOV_H / 2.0);
        double pitchOffsetDeg = (cy / (CAM_H / 2.0)) * (FOV_V / 2.0);

        // yaw offset is the one we actually care about
        double yaw = Math.toRadians(yawOffsetDeg);

        // --- 2. estimate distance ---
        double dist = estimateDistance(b);   // in inches

        // --- 3. point in ROBOT frame ---
        // x = forward, y = left
        double rx = dist * Math.cos(yaw);
        double ry = dist * Math.sin(yaw);

        // --- 4. rotate into FIELD frame ---
        double headingRad = Math.toRadians(robot.getState().heading);

        double fx = robot.getState().x + (rx * Math.cos(headingRad) - ry * Math.sin(headingRad));
        double fy = robot.getState().y + (rx * Math.sin(headingRad) + ry * Math.cos(headingRad));

        return new double[]{fx, fy};
    }
}
