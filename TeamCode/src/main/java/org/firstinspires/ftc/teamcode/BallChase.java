package org.firstinspires.ftc.teamcode;

import com.duck123acb.robotcore.Robot;
import com.duck123acb.robotcore.RobotState;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * This is the main OpMode for the "BallChase" autonomous routine.
 * It initializes the real hardware and then passes control to the BallChaseLogic class.
 */
@Autonomous(name = "BallChase_Decode")
public class BallChase extends LinearOpMode {
    static final int PURPLE = 1;
    static final int GREEN = 2;
    static final int[][] BALL_ORDERS = {{GREEN, PURPLE, GREEN}, {PURPLE, GREEN, PURPLE}, {PURPLE, PURPLE, GREEN}};

//    @Override
//    public void runOpMode() {
//        // Initialize real hardware
//        HuskyLens huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
//        BNO055IMU imu = hardwareMap.get(BNO055IMU.class, "imu");
//        DcMotor leftFront = hardwareMap.get(DcMotor.class, "leftFront");
//        DcMotor rightFront = hardwareMap.get(DcMotor.class, "rightFront");
//        DcMotor leftBack = hardwareMap.get(DcMotor.class, "leftBack");
//        DcMotor rightBack = hardwareMap.get(DcMotor.class, "rightBack");
//        DcMotor leftIntake = hardwareMap.get(DcMotor.class, "leftIntakeMotor");
//        DcMotor rightIntake = hardwareMap.get(DcMotor.class, "rightIntakeMotor");
//        DcMotor leftOuttake = hardwareMap.get(DcMotor.class, "leftOuttakeMotor");
//        DcMotor rightOuttake = hardwareMap.get(DcMotor.class, "rightOuttakeMotor");
//        Servo launch = hardwareMap.get(Servo.class, "launchServo");
//
//        // Set motor directions
//        rightOuttake.setDirection(DcMotorSimple.Direction.REVERSE);
//        leftFront.setDirection(DcMotor.Direction.FORWARD);
//        leftBack.setDirection(DcMotor.Direction.FORWARD);
//        rightFront.setDirection(DcMotor.Direction.REVERSE);
//        rightBack.setDirection(DcMotor.Direction.REVERSE);
//
//        // Initialize IMU
//        BNO055IMU.Parameters p = new BNO055IMU.Parameters();
//        p.angleUnit = BNO055IMU.AngleUnit.DEGREES;
//        imu.initialize(p);
//
//        telemetry.addLine("Real hardware initialized");
//        telemetry.update();
//
//        // Create the logic class and inject the hardware
//        BallChaseLogic autonomousLogic = new BallChaseLogic(this, telemetry, huskylens, imu,
//                leftFront, rightFront, leftBack, rightBack,
//                leftIntake, rightIntake, leftOuttake, rightOuttake, launch);`
//
//        waitForStart();
//
//        if (opModeIsActive()) {
//            autonomousLogic.run();
//        }
//    }

    // TODO: reimplement apriltags

    @Override
    public void runOpMode() {
        HuskyLens huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
        DcMotor lf = hardwareMap.get(DcMotor.class, "leftFront");
        DcMotor rf = hardwareMap.get(DcMotor.class, "rightFront");
        DcMotor lb = hardwareMap.get(DcMotor.class, "leftBack");
        DcMotor rb = hardwareMap.get(DcMotor.class, "rightBack");
        DcMotor li = hardwareMap.get(DcMotor.class, "leftIntakeMotor");
        DcMotor ri = hardwareMap.get(DcMotor.class, "rightIntakeMotor");
        DcMotor lo = hardwareMap.get(DcMotor.class, "leftOuttakeMotor");
        DcMotor ro = hardwareMap.get(DcMotor.class, "rightOuttakeMotor");

        RealMotor leftFront = new RealMotor(lf);
        RealMotor rightFront = new RealMotor(rf);
        RealMotor leftBack = new RealMotor(lb);
        RealMotor rightBack = new RealMotor(rb);

        RealMotor leftIntake = new RealMotor(li);
        RealMotor rightIntake = new RealMotor(ri);
        RealMotor leftOuttake = new RealMotor(lo);
        RealMotor rightOuttake = new RealMotor(ro);

        Robot robot = new Robot(leftFront, rightFront, leftBack, rightBack,
                leftIntake, rightIntake, leftOuttake, rightOuttake,
                0, 0, 0);
        robot.resetPID();

        telemetry.addLine("Waiting...");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        // choose pattern 0 for now
        int[] pattern = BALL_ORDERS[0];

        for (int targetColor : pattern) {

            telemetry.addData("Next Target Ball", targetColor == PURPLE ? "PURPLE" : "GREEN");
            telemetry.update();

            HuskyLens.Block targetBlock = null;

            // ---------------------------------------------------
            // WAIT until we see the ball matching the next color
            // ---------------------------------------------------
            while (opModeIsActive()) {
                HuskyLens.Block[] blocks = huskylens.blocks();

                if (blocks != null) {
                    for (HuskyLens.Block b : blocks) {
                        if (b.id == targetColor) {
                            targetBlock = b;
                            break;
                        }
                    }
                }

                if (targetBlock != null) break;

                telemetry.addLine("Looking for correct ball...");
                telemetry.update();
                sleep(40);
            }

            if (targetBlock == null) break; // failsafe

            // extract cam coords
            int camX = targetBlock.x;
            int camY = targetBlock.y;

            // TODO: real conversion here
            double targetX = camX;
            double targetY = camY;

            telemetry.addData("Chasing Ball", "ID=%d cam(%d,%d)", targetColor, camX, camY);
            telemetry.update();

            // ---------------------------------------------------
            // GO TO THE COORDS
            // ---------------------------------------------------
            while (opModeIsActive()) {
                robot.goToXY_PID(targetX, targetY, 0, 30);

                RobotState s = robot.getState();
                double dx = targetX - s.x;
                double dy = targetY - s.y;

                if (Math.hypot(dx, dy) < 1.0) {
                    telemetry.addLine("Ball reached");
                    telemetry.update();
                    break;
                }

                sleep(20);
            }
        }

        telemetry.addLine("Finished full pattern");
        telemetry.update();
    }
}