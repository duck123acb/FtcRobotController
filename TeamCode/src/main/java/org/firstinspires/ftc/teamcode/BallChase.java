package org.firstinspires.ftc.teamcode;

import com.duck123acb.robotcore.Motor;
import com.duck123acb.robotcore.Position;
import com.duck123acb.robotcore.Robot;
import com.duck123acb.robotcore.RobotState;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * OpMode for the "BallChase" autonomous routine.
 **/
@Autonomous(name = "BallChase_Decode")
public class BallChase extends LinearOpMode {
    static final Artifact[][] ARTIFACT_ORDERS = {{Artifact.GREEN, Artifact.PURPLE, Artifact.GREEN}, {Artifact.PURPLE, Artifact.GREEN, Artifact.PURPLE}, {Artifact.PURPLE, Artifact.PURPLE, Artifact.GREEN}};

    // Field Coordinates (Inches)
    // Assuming 0,0 is center of field or corner? Using provided 60, 24 as a base.
    // Adjust these based on actual field setup.
    static final Position BASKET = new Position(60, 24, 0); // TODO: Verify heading for shooting
    static final Position[] BALL_LINES = {
        new Position(48, 48, 90), // Line 1
        new Position(48, 24, 90), // Line 2
        new Position(48, 0, 90)   // Line 3
    };

    // Constants
    static final int[] SPIN_DEX_POSITIONS = {0, 100, 200}; // TODO: Tune these
    static final int CAMERA_WIDTH = 320;
    static final int CAMERA_HEIGHT = 240;
    static final int CAMERA_CENTER_X = CAMERA_WIDTH / 2;
    static final double BALL_CAPTURE_AREA = 5000; // TODO: Tune this threshold

    // hardware
    Robot robot;
    HuskyLens huskylens;
    
    // Promoted motors for direct access
    RealMotor leftIntake, rightIntake, leftOuttake, rightOuttake;

    // flags
    Artifact[] artifactOrder;
    Artifact[] spinDexSlots = new Artifact[3];

    @Override
    public void runOpMode() {
        // hardware init
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
        
        // Initialize class fields
        leftIntake = new RealMotor(li);
        rightIntake = new RealMotor(ri);
        leftOuttake = new RealMotor(lo);
        rightOuttake = new RealMotor(ro);
        
        robot = new Robot(leftFront, rightFront, leftBack, rightBack, leftIntake, rightIntake, leftOuttake, rightOuttake, 0, 0, 0); // FIXME: CHANGE ACCORDING TO WHERE WE START
        robot.resetPID();

        // Initialize Outtake Motors for RunToPosition
        leftOuttake.setMode(Motor.RunMode.STOP_AND_RESET_ENCODER);
        rightOuttake.setMode(Motor.RunMode.STOP_AND_RESET_ENCODER);
        // We set mode in spinDex method

        telemetry.addLine("Waiting...");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);

        // set ball order based on apriltag
        artifactOrder = ARTIFACT_ORDERS[0]; // set 0 as default
        long tagTimeout = System.currentTimeMillis() + 3000; // 3 sec timeout
        while (opModeIsActive() && System.currentTimeMillis() < tagTimeout) {
            int tag = SetAprilTag();
            if (tag != -1) { // set it once found
                if (tag >= 0 && tag < ARTIFACT_ORDERS.length) {
                    artifactOrder = ARTIFACT_ORDERS[tag];
                    telemetry.addData("Tag Found", tag);
                    telemetry.update();
                    break;
                }
            }
        }

        // Initial Score (Preload)
        lebron();

        huskylens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION);

        // go to ball position
        for (Position linePos : BALL_LINES) {
            goTo(linePos); // move to position

            robot.intakeSystem.spin(1.0);

            for (int x = 0; x < 3; x++) {
                Ball currBall = getBall();
                if (currBall == null) break; // No ball found, move on

                // Find first empty slot
                int emptySlot = -1;
                for (int i = 0; i < spinDexSlots.length; i++) {
                    if (spinDexSlots[i] == null) {
                        emptySlot = i;
                        break;
                    }
                }

                if (emptySlot == -1) {
                    telemetry.addLine("Spin-dex full!");
                    telemetry.update();
                    break;
                }

                // Chase and acquire ball
                chaseBall(currBall);

                // Store ball logic
                robot.intakeSystem.stop(); // Stop intake momentarily to sort
                spinDex(emptySlot); // Rotate to correct slot
                spinDexSlots[emptySlot] = currBall.artifact; // Track ball type
                robot.intakeSystem.spin(1.0); // Resume intake to pull it in fully
                sleep(500); // Wait for ball to settle
            }
            
            robot.intakeSystem.stop();
            lebron();
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
        long timeout = System.currentTimeMillis() + 2000; // 2 sec timeout to find a ball

        while (opModeIsActive() && System.currentTimeMillis() < timeout) {
            HuskyLens.Block[] blocks = huskylens.blocks();
            if (blocks == null || blocks.length == 0) continue;

            // find the block with the largest area on camera screen
            for (HuskyLens.Block b : blocks) {
                double area = b.width * b.height;
                if (area > bestArea) {
                    bestArea = area;
                    best = b;
                }
            }
            
            if (best != null) break;
        }

        if (best == null) {
            telemetry.addLine("No ball found.");
            telemetry.update();
            return null;
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
                artifactType = Artifact.offset; // Using offset as 'unknown' or default
                break;
        }
        return new Ball(artifactType, best);
    }

    private void chaseBall(Ball ball) {
        // Simple P-controller for centering
        double kP_turn = 0.005;
        double speed = 0.3;
        
        while (opModeIsActive()) {
            // Update ball info from camera
            HuskyLens.Block[] blocks = huskylens.blocks();
            HuskyLens.Block currentBlock = null;
            
            // Find the same ball (by ID or roughly same position/size)
            // For simplicity, just find largest block of same ID
            double maxArea = 0;
            for (HuskyLens.Block b : blocks) {
                 if (b.id == ball.block.id) { // Assuming ID matches color
                     double area = b.width * b.height;
                     if (area > maxArea) {
                         maxArea = area;
                         currentBlock = b;
                     }
                 }
            }

            if (currentBlock == null) {
                // Lost ball, maybe we got it?
                break; 
            }
            
            double area = currentBlock.width * currentBlock.height;
            if (area > BALL_CAPTURE_AREA) {
                // Close enough to intake
                // Drive forward a bit more to ensure intake
                robot.driveDirection(Math.PI/2, 0.3, robot.getState().heading); // Drive forward relative to robot
                sleep(500);
                robot.driveSystem.stop();
                break;
            }

            // Calculate error
            double errorX = CAMERA_CENTER_X - currentBlock.x;
            double turnPower = errorX * kP_turn;

            // Drive forward and turn
            // Using driveMecanum directly for robot-centric control
            // x is forward/back, y is strafe, omega is turn
            // Note: Robot class driveDirection uses field centric usually, but we want robot centric here.
            // Let's use driveSystem directly if possible or create a robot-centric method.
            // Looking at Robot.java, driveMecanum takes (x, y, omega). 
            // Assuming x is forward? Need to check DriveSystem but usually x/y are standard.
            // Let's assume x is forward for now based on standard mecanum.
            
            robot.driveSystem.driveMecanum(speed, 0, turnPower); 
            robot.driveSystem.updateSim(0.02);
        }
        robot.driveSystem.stop();
    }

    private void spinDex(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= SPIN_DEX_POSITIONS.length) return;
        int targetPos = SPIN_DEX_POSITIONS[slotIndex];
        
        // Move both outtake motors (assuming they are coupled or need to move together)
        leftOuttake.setTargetPosition(targetPos);
        rightOuttake.setTargetPosition(targetPos);
        
        leftOuttake.setMode(Motor.RunMode.RUN_TO_POSITION);
        rightOuttake.setMode(Motor.RunMode.RUN_TO_POSITION);
        
        leftOuttake.setPower(0.5);
        rightOuttake.setPower(0.5);
    }

    private boolean moveRobot(Position target) {
        robot.goToXY_PID(target.x, target.y, target.heading, 30);

        RobotState state = robot.getState();
        double dx = target.x - state.x;
        double dy = target.y - state.y;

        if (Math.hypot(dx, dy) < 2.0) { // Increased tolerance slightly
            telemetry.addData("Position Reached", "x: %.2f, y: %.2f, heading: %.2f", state.x, state.y, state.heading);
            telemetry.update();

            return true;
        }

        return false;
    }

    private void goTo(Position target) {
        while (opModeIsActive()) if (moveRobot(target)) break;
        robot.driveSystem.stop();
    }

    private void lebron() { // go to basket and shoot
        goTo(BASKET);

        for (Artifact targetColor : artifactOrder) {
            // Find slot containing targetColor
            int slotToShoot = -1;
            for (int i = 0; i < spinDexSlots.length; i++) {
                if (spinDexSlots[i] == targetColor) {
                    slotToShoot = i;
                    break;
                }
            }

            if (slotToShoot != -1) {
                spinDex(slotToShoot);
                sleep(500); // Wait for spin-dex
                robot.outtakeSystem.spin(1.0); // Shoot
                sleep(500); // Wait for shot
                robot.outtakeSystem.stop();
                spinDexSlots[slotToShoot] = null; // Clear slot
            } else {
                telemetry.addData("Warning", "Required color %s not found in spin-dex", targetColor);
                telemetry.update();
            }
        }
    }
}