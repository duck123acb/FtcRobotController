package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name = "HuskyLensTest")
public class BallChase extends LinearOpMode {
    private DcMotor leftFront, rightFront, leftBack, rightBack;
    private PID turnPID = new PID(0.01, 0.0, 0.001); // tweak gains
    private PID forwardPID = new PID(0.005, 0.0, 0.0); // tweak gains
    private HuskyLens huskylens;

    /*
     * TAG 1 - G P P (2 1 1)
     * TAG 2 - P G P (1 2 1)
     * TAG 3 - P P G (1 1 2)
     */
    final static int[][] ballOrders = {
            {2, 1, 1},
            {1, 2, 1},
            {1, 1, 2}
    };
    int[] ballOrder = null;

    @Override
    public void runOpMode() {
        ElapsedTime myElapsedTime = new ElapsedTime();

        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");

        leftFront.setDirection(DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        // Initialize HuskyLens
        telemetry.addData(">>", huskylens.knock() ? "Touch start to continue" : "Problem communicating with HuskyLens");
        huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
        telemetry.update();

        waitForStart();

        // --- Step 1: Detect AprilTag and set ball order ---
        while (opModeIsActive() && ballOrder == null) {
            int id = SetAprilTag();
            if (id != -1 && id >= 21 && id <= 23) { // ensure valid tag
                ballOrder = ballOrders[id - 21]; // map 21->0, 22->1, 23->2
                telemetry.addData("Ball Order Set", "Tag %d -> Order: %s", id, java.util.Arrays.toString(ballOrder));
                telemetry.update();
            }
        }

        // --- Step 2: Loop through each ball in sequence ---
        for (int targetID : ballOrder) {
            boolean ballReached = false;

            while (opModeIsActive() && !ballReached) {
                // Read current blocks
                HuskyLens.Block[] blocks = huskylens.blocks();

                // Find the closest block of target color/ID
                HuskyLens.Block closest = GetClosestBall(blocks, targetID);

                if (closest != null) {
                    // Telemetry for debugging
                    telemetry.addData("Target Ball", "ID=%d, x=%d, y=%d, size=%dx%d",
                            closest.id, closest.x, closest.y, closest.width, closest.height);
                    telemetry.update();

                    // Constants
                    int targetX = 160; // assuming camera width = 320 px
                    double targetArea = 4000; // desired area when “close enough”, tweak

                    // PID calculations
                    double turnPower = turnPID.update(targetX, closest.x);
                    double forwardPower = forwardPID.update(targetArea, closest.width * closest.height);

                    // Clamp powers
                    turnPower = clamp(turnPower, -1, 1);
                    forwardPower = clamp(forwardPower, -0.5, 0.5); // smaller forward speed for control

                    // Compute motor powers
                    double leftPower = forwardPower + turnPower;
                    double rightPower = forwardPower - turnPower;

                    // Apply to motors
                    leftFront.setPower(leftPower);
                    leftBack.setPower(leftPower);
                    rightFront.setPower(rightPower);
                    rightBack.setPower(rightPower);

                    // Check if ball is “reached” by area threshold
                    if (closest.width * closest.height >= targetArea) {
                        ballReached = true;
                        // stop motors
                        leftFront.setPower(0);
                        leftBack.setPower(0);
                        rightFront.setPower(0);
                        rightBack.setPower(0);
                    }


                } else {
                    telemetry.addData("Target Ball", "Not found yet");
                    telemetry.update();
                }

                sleep(50); // small delay to avoid spamming the sensor
            }
        }

        // --- Step 3: Finished all balls ---
        telemetry.addData("Autonomous", "Finished all balls!");
        telemetry.update();

        huskylens.close();
    }

    // --- Helper Methods ---
    int SetAprilTag() {
        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);

        HuskyLens.Block[] blocks = huskylens.blocks();
        if (blocks.length > 0) {
            HuskyLens.Block firstBlock = blocks[0];
            telemetry.addData("First Tag", "ID=%d at (%d,%d) size %dx%d",
                    firstBlock.id, firstBlock.x, firstBlock.y,
                    firstBlock.width, firstBlock.height
            );
            telemetry.update();

            return firstBlock.id;
        } else {
            telemetry.addData("No Tags Found", "");
            telemetry.update();
            return -1;
        }
    }

    HuskyLens.Block GetClosestBall(HuskyLens.Block[] blocks, int targetID) {
        HuskyLens.Block closest = null;
        int maxArea = -1;

        for (HuskyLens.Block block : blocks) {
            if (block.id != targetID) continue;

            int area = block.width * block.height;
            if (area > maxArea) {
                maxArea = area;
                closest = block;
            }
        }

        return closest;
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
