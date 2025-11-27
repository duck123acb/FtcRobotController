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

        Robot robot = new Robot(leftFront, rightFront, leftBack, rightBack, leftIntake, rightIntake, leftOuttake, rightOuttake, 0, 0, 0);
        robot.resetPID();

        telemetry.addLine("Waiting...");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        // ----------------------------
        // MAIN LOOP: drive to 1st ball
        // ----------------------------
        while (opModeIsActive()) {
            // TODO: implement apriltags again
            // TODO: follow ball orders
            HuskyLens.Block[] blocks = huskylens.blocks();
            if (blocks == null || blocks.length == 0) {
                telemetry.addLine("no balls found");
                telemetry.update();
                continue;
            }

            // take the FIRST block HuskyLens gives
            HuskyLens.Block b = blocks[0];

            int camX = b.x;
            int camY = b.y;

            // ------------------------------------------------------------
            // TODO: replace this with actual cam→field conversion
            // right now it's "fake field coords" just like your placeholder
            // ------------------------------------------------------------
            double targetX = camX;
            double targetY = camY;

            telemetry.addData("Target", "cam(%d,%d) -> field(%.1f, %.1f)", camX, camY, targetX, targetY);
            telemetry.update();

            // ------------------------------------------------------------
            // move like the sim: keep stepping until we reach the point
            // ------------------------------------------------------------
            while (opModeIsActive()) {
                robot.goToXY_PID(targetX, targetY, 0, 30);

                RobotState s = robot.getState();
                double dx = targetX - s.x;
                double dy = targetY - s.y;

                if (Math.hypot(dx, dy) < 1.0) {
                    telemetry.addLine("ball reached");
                    telemetry.update();
                    break;
                }

                sleep(20);
            }

            // once we reach the first ball, stop looping
            break;
        }
    }
}