package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "HuskyLensTest")
public class BallChase extends LinearOpMode {


    private HuskyLens huskylens;

    /**
     * This OpMode illustrates how to use the DFRobot HuskyLens.
     *
     * The HuskyLens is a Vision Sensor with a built-in object detection model. It can
     * detect a number of predefined objects and AprilTags in the 36h11 family, can
     * recognize colors, and can be trained to detect custom objects. See this website for
     * documentation: https://wiki.dfrobot.com/HUSKYLENS_V1.0_SKU_SEN0305_SEN0336
     *
     * This sample illustrates how to detect AprilTags, but can be used to detect other types
     * of objects by changing the algorithm. It assumes that the HuskyLens is configured with
     * a name of "huskylens".
     */
    @Override
    public void runOpMode() {
        ElapsedTime myElapsedTime;

        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");

        // Put initialization blocks here.
        telemetry.addData(">>", huskylens.knock() ? "Touch start to continue" : "Problem communicating with HuskyLens");
//        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
        huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
        telemetry.update();

        myElapsedTime = new ElapsedTime();
        waitForStart();

        if (opModeIsActive()) {
            while (opModeIsActive()) {
                if (myElapsedTime.seconds() >= 1) {
                    myElapsedTime.reset();
                    for (HuskyLens.Block myHuskyLensBlock_item : huskylens.blocks()) {
                        telemetry.addData("Block", "id=" + myHuskyLensBlock_item.id + " size: " + myHuskyLensBlock_item.width + "x" + myHuskyLensBlock_item.height + " position: " + myHuskyLensBlock_item.x + "," + myHuskyLensBlock_item.y);
                    }
                    telemetry.update();
                }
            }
        }

        huskylens.close();
    }

    /*
     * TAG 1 - G P P (2 1 1)
     * TAG 2 - P G P (1 2 1)
     * TAG 3 - P P G (1 1 2)
     */

    void FindAprilTag(HuskyLens.Block[] blocks, int id, int x, int y) {

    }

    HuskyLens.Block GetClosestBall(HuskyLens.Block[] blocks, int targetID, int[] coords) {
        HuskyLens.Block closest = null;
        int maxArea = -1;

        for (HuskyLens.Block block : blocks) {
            if (block.id != targetID)
                continue;

            int area = block.width * block.height;
            if (area > maxArea) {
                maxArea = area;
                closest = block;
            }
        }

        return closest;
    }

}