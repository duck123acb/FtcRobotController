package org.firstinspires.ftc.teamcode;

import com.duck123acb.robotcore.Position;
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

    // FIXME: tweak values
    static final Position BASKET = new Position(60, 24, 0);
    static final Position BALL_LINE_1 = new Position(60, 24, 90);
    static final Position BALL_LINE_2 = new Position(60, 24, 90);
    static final Position BALL_LINE_3 = new Position(60, 24, 90);

    Robot robot;
    HuskyLens huskylens;

    @Override
    public void runOpMode() {
        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
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

        robot = new Robot(leftFront, rightFront, leftBack, rightBack, leftIntake, rightIntake, leftOuttake, rightOuttake, 0, 0, 0); // FIXME: CHANGE ACCORDING TO WHERE WE START
        robot.resetPID();

        telemetry.addLine("Waiting...");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        // SET BALL ORDER
        int[] ballOrder = BALL_ORDERS[0]; // set 0 as default
        while (opModeIsActive()) {
            int tag = SetAprilTag();
            if (tag != -1) { // actually set it once found
                ballOrder = BALL_ORDERS[tag];
                break;
            }
        }

        /*
        for ball lines {

            go to ball line N

            collect balls / track colours in what space

            go the shooter
            shoot in the right order

        }
        */
    }

    private boolean moveRobotToPosition(double targetX, double targetY, String Ball_reached) {
        robot.goToXY_PID(targetX, targetY, 0, 30);

        RobotState s = robot.getState();
        double dx = targetX - s.x;
        double dy = targetY - s.y;

        if (Math.hypot(dx, dy) < 30.0) {
            robot.intakeSystem.spin(1.0);
        }
        else {
            robot.intakeSystem.stop();
        }
        if (Math.hypot(dx, dy) < 1.0) {
            telemetry.addLine(Ball_reached);
            telemetry.update();

            robot.intakeSystem.stop();

            return true;
        }

        sleep(20);
        return false;
    }

    private HuskyLens.Block getBlock(int targetColor, HuskyLens huskylens) {
        HuskyLens.Block best = null;
        double bestDist = Double.MAX_VALUE;

        while (opModeIsActive()) {
            HuskyLens.Block[] blocks = huskylens.blocks();

            if (blocks != null) {
                for (HuskyLens.Block b : blocks) {
                    if (b.id == targetColor) {
                        // compute distance from cam center
                        double dx = b.x - 160;   // assuming 320px width
                        double dy = b.y - 120;   // assuming 240px height
                        double dist = Math.hypot(dx, dy);

                        if (dist < bestDist) {
                            bestDist = dist;
                            best = b;
                        }
                    }
                }
            }

            if (best != null) {
                return best;
            }

            telemetry.addLine("Looking for correct ball...");
            telemetry.update();
            sleep(40);
        }
        return null;
    }

    void goToBasketAndShoot() { // FIXME: change according to new intake/outtake system
        // ---------------------------------------------------
        // DRIVE TO THE BASKET
        // ---------------------------------------------------

        telemetry.addData("Basket", "Going to (%.1f, %.1f)", BASKET.x, BASKET.y);
        telemetry.update();

        // drive until close enough
        while (opModeIsActive()) {
            if (moveRobotToPosition(BASKET.x, BASKET.y, "At basket")) break;
        }

        RobotState s = robot.getState();
        double dx = BASKET.x - s.x;
        double dy = BASKET.y - s.y;
        double targetHeading = BASKET.heading;

        turnTo(targetHeading);

        // ---------------------------------------------------
        // SHOOT
        // ---------------------------------------------------

        // spin up your outtake motors
        robot.outtakeSystem.spin(1.0);

        // wait for flywheel to settle
        sleep(500);

        // fire servo
        // launch.setPosition(1.0);   // FIXME: uncomment if you have the servo here

        sleep(350);

        // return servo
        // launch.setPosition(0.0);   // FIXME: uncomment if you have the servo here

        // stop motors
        robot.outtakeSystem.stop();

        turnTo(Math.PI/2);

        telemetry.addLine("Scored");
        telemetry.update();
    }

    private void turnTo(double targetHeadingRadians) {
        while (opModeIsActive()) {
            robot.turnPID(targetHeadingRadians);

            RobotState s = robot.getState();

            double diff = targetHeadingRadians - s.heading;
            while (diff > Math.PI) diff -= 2 * Math.PI;
            while (diff < -Math.PI) diff += 2 * Math.PI;

            if (Math.abs(diff) < 0.05) break; // ~3 degrees

            sleep(20);
        }
    }

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
        }
        else
            return -1;
    }
}