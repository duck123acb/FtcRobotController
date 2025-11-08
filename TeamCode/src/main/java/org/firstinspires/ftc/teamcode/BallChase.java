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
    private PID turnPID = new PID(0.01, 0.0, 0.001);
    private PID forwardPID = new PID(0.005, 0.0, 0.0);

    // Tag → ball sequences
    final static int[][] ballOrders = {
            {2, 1, 1},
            {1, 2, 1},
            {1, 1, 2}
    };
    int[] ballOrder = null;

    @Override
    public void runOpMode() {
        initHardware();

        waitForStart();

        setBallOrderFromTag();

        int ballIndex = 0;

        while (opModeIsActive()) {
            int targetID = ballOrder[ballIndex];

            chaseBall(targetID);

            // Loop forever through ball order
            ballIndex = (ballIndex + 1) % ballOrder.length;
        }
    }

    // ----------------------------------------------------
    //                SETUP + INIT FUNCTIONS
    // ----------------------------------------------------

    private void initHardware() {
        // HuskyLens
        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
        telemetry.addData(">>", huskylens.knock() ? "Touch start to continue" : "Problem communicating");
        huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
        telemetry.update();

        // Motors
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");

        leftFront.setDirection(DcMotor.Direction.FORWARD);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection(DcMotor.Direction.REVERSE);
    }

    private void setBallOrderFromTag() {
        while (opModeIsActive() && ballOrder == null) {
            int id = readAprilTag();
            if (id != -1) {
                ballOrder = ballOrders[id - 1];
                telemetry.addData("Ball Order Set", "Tag %d -> %s", id, java.util.Arrays.toString(ballOrder));
                telemetry.update();
            }
        }
    }

    // ----------------------------------------------------
    //            MAIN BALL CHASE LOGIC
    // ----------------------------------------------------

    private void chaseBall(int targetID) {
        boolean reached = false;

        while (opModeIsActive() && !reached) {
            HuskyLens.Block[] blocks = huskylens.blocks();
            HuskyLens.Block closest = getClosestBall(blocks, targetID);

            if (closest != null) {
                reached = driveWithPID(closest);
            } else {
                stopMotors();
                telemetry.addData("Target Ball", "Not found");
                telemetry.update();
            }
        }

        stopMotors();
        sleep(10);
    }

    private boolean driveWithPID(HuskyLens.Block ball) {
        int targetX = 160;
        double targetArea = 4000;

        double turnPower = turnPID.update(targetX, ball.x);
        double forwardPower = forwardPID.update(targetArea, ball.width * ball.height);

        turnPower = clamp(turnPower, -1, 1);
        forwardPower = clamp(forwardPower, -0.5, 0.5);

        double leftPower = forwardPower + turnPower;
        double rightPower = forwardPower - turnPower;

        leftFront.setPower(leftPower);
        leftBack.setPower(leftPower);
        rightFront.setPower(rightPower);
        rightBack.setPower(rightPower);

        telemetry.addData("Ball", "ID=%d x=%d y=%d size=%dx%d",
                ball.id, ball.x, ball.y, ball.width, ball.height);
        telemetry.update();

        return ball.width * ball.height >= targetArea;
    }

    // ----------------------------------------------------
    //                   HELPER METHODS
    // ----------------------------------------------------

    private int readAprilTag() {
        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
        HuskyLens.Block[] blocks = huskylens.blocks();

        if (blocks.length > 0) {
            int id = blocks[0].id;
            telemetry.addData("Tag Found", "ID=%d", id);
            telemetry.update();

            huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
            return id;
        }

        telemetry.addData("Tag", "None");
        telemetry.update();
        return -1;
    }

    private HuskyLens.Block getClosestBall(HuskyLens.Block[] blocks, int targetID) {
        HuskyLens.Block best = null;
        int maxArea = -1;

        for (HuskyLens.Block block : blocks) {
            if (block.id != targetID) continue;
            int area = block.width * block.height;
            if (area > maxArea) {
                maxArea = area;
                best = block;
            }
        }
        return best;
    }

    private void stopMotors() {
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}