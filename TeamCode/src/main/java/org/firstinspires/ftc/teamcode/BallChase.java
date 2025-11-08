package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name = "HuskyLensTest")
public class BallChase extends LinearOpMode {
    private HuskyLens huskylens;

    // Motors
    private DcMotor leftFront, rightFront, leftBack, rightBack;

    // PID controllers
    private PID turnPID = new PID(0.01, 0.0, 0.001); // adjust after testing
    private PID forwardPID = new PID(0.005, 0.0, 0.0);

    /*
     * TAG 21 - G P P (2 1 1)
     * TAG 22 - P G P (1 2 1)
     * TAG 23 - P P G (1 1 2)
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

        // Initialize HuskyLens
        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
        telemetry.addData(">>", huskylens.knock() ? "Touch start to continue" : "Problem communicating with HuskyLens");
        huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
        telemetry.update();

        // Initialize motors
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");

        leftFront.setDirection(DcMotor.Direction.FORWARD);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        waitForStart();

        // find order
        while (opModeIsActive() && ballOrder == null) {
            int id = SetAprilTag();
            ballOrder = ballOrders[id - 1];
            telemetry.addData("Ball Order Set", "Tag %d -> Order: %s", id, java.util.Arrays.toString(ballOrder));
            telemetry.update();
        }

        int ballIndex = 0;

        // start autonomous
        while (opModeIsActive()) {
            int targetID = ballOrder[ballIndex];
            boolean ballReached = false;

            while (opModeIsActive() && !ballReached) {
                HuskyLens.Block[] blocks = huskylens.blocks();
                HuskyLens.Block closest = GetClosestBall(blocks, targetID);

                if (closest != null) {
                    // PID-based driving
                    int targetX = 160; // assuming camera width = 320 px
                    double targetArea = 4000; // FIXME: tweak based on testing

                    double turnPower = turnPID.update(targetX, closest.x);
                    double forwardPower = forwardPID.update(targetArea, closest.width * closest.height);

                    turnPower = clamp(turnPower, -1, 1);
                    forwardPower = clamp(forwardPower, -0.5, 0.5);

                    double leftPower = forwardPower + turnPower;
                    double rightPower = forwardPower - turnPower;

                    leftFront.setPower(leftPower);
                    leftBack.setPower(leftPower);
                    rightFront.setPower(rightPower);
                    rightBack.setPower(rightPower);

                    telemetry.addData("Target Ball", "ID=%d, x=%d, y=%d, size=%dx%d",
                            closest.id, closest.x, closest.y, closest.width, closest.height);
                    telemetry.update();

                    if (closest.width * closest.height >= targetArea) { // reached ball
                        ballReached = true;
                        stopMotors();
                        sleep(250); // optional pause before next ball
                    }
                } else {
                    stopMotors();
                    telemetry.addData("Target Ball", "Not found");
                    telemetry.update();
                }

                sleep(50);
            }

            // Move to the next ball in sequence, wrap around infinitely
            ballIndex = (ballIndex + 1) % ballOrder.length;
        }
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

    private void stopMotors() {
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}