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
    static final Artifact[][] ARTIFACT_ORDERS = {{Artifact.GREEN, Artifact.PURPLE, Artifact.GREEN}, {Artifact.PURPLE, Artifact.GREEN, Artifact.PURPLE}, {Artifact.PURPLE, Artifact.PURPLE, Artifact.GREEN}};

    // FIXME: tweak values based on which side, AND ADD real field coordinates
    static final Position BASKET = new Position(60, 24, 0);
    static final Position[] BALL_LINES = {
        new Position(60, 24, 90),
        new Position(60, 24, 90),
        new Position(60, 24, 90)
    };

    Robot robot;
    HuskyLens huskylens;

    public static class Ball {
        public final Artifact artifact;
        public final HuskyLens.Block block;

        public Ball(Artifact artifact, HuskyLens.Block block) {
            this.artifact = artifact;
            this.block = block;
        }
    }

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

        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);

        // SET BALL ORDER
        Artifact[] artifactOrder = ARTIFACT_ORDERS[0]; // set 0 as default
        while (opModeIsActive()) {
            int tag = SetAprilTag();
            if (tag != -1) { // actually set it once found
                artifactOrder = ARTIFACT_ORDERS[tag];
                break;
            }
        }

        huskylens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION);

        /*
        for ball lines {

            go to ball line N



            go the shooter
            shoot in the right order

        }
        */

        for (Position linePos : BALL_LINES) {
            goTo(linePos); // move to position

            robot.intakeSystem.spin(1.0);

            for (int x = 0; x < 3; x++) {
                Ball currBall = getBall();
                // turn intake on
                // move to ball
                // turn spin-dex to correct position
                // store ball in that spot
                // intake off
            }

            goTo(BASKET);

            for (Artifact targetColor : artifactOrder) {
                // spin spin-dex to position
                robot.outtakeSystem.spin(1.0);
            }
        }
    }

    int SetAprilTag() {
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

    private Ball getBall() {
        HuskyLens.Block best = null;
        double bestArea = 0;

        while (opModeIsActive()) {
            HuskyLens.Block[] blocks = huskylens.blocks();
            if (blocks == null) continue; // go to next iteration if no blocks found

            // find the block with the largest area on camera screen
            for (HuskyLens.Block b : blocks) {
                double area = b.width * b.height;
                if (!(area > bestArea)) continue; // skip if area is too small

                bestArea = area;
                best = b;
            }

            // another failsafe if no blocks found
            if (best == null) {
                telemetry.addLine("Looking for a ball...");
                telemetry.update();
                sleep(40);

                continue;
            }

            // setting the type of artifact based on ID
            Artifact artifactType;
            switch (best.id) {
                case 1:
                    artifactType = Artifact.GREEN;
                    break;
                case 2:
                    artifactType = Artifact.PURPLE;
                    break;
                default:
                    artifactType = Artifact.offset;
                    break;
            }
            return new Ball(artifactType, best);
        }

        return null; // ANOTHER failsafe
    }

    private boolean moveRobot(Position target) {
        robot.goToXY_PID(target.x, target.y, target.heading, 30);

        RobotState state = robot.getState();
        double dx = target.x - state.x;
        double dy = target.y - state.y;

        if (Math.hypot(dx, dy) < 1.0) {
            telemetry.addData("Position Reached", "x: %.2f, y: %.2f, heading: %.2f", state.x, state.y, state.heading);
            telemetry.update();

            return true;
        }

        return false;
    }

    private void goTo(Position target) {
        while (opModeIsActive()) if (moveRobot(target)) break;
    }
}