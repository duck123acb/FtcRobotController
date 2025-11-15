package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.systems.TakeSystem;
import org.firstinspires.ftc.teamcode.simulation.UdpClient;

/**
 * 💡 Contains the core autonomous logic for the "BallChase" routine.
 * This class is hardware-agnostic and can be used with both real and mock hardware.
 *
 *  flow:
 *  1. reads april tag (1–3) → decides color pattern order
 *  2. moves to each pre-mapped ball position + intakes
 *  3. drives to the basket, shoots, and resets
 *
 * things you’ll need to tune before comp:
 *  - FIXME: ballSpots → actual field distances
 *  - FIXME: PID gains (headingPID)
 *  - FIXME: intakeOn/off logic for your TakeSystem
 *  - FIXME: shootSequence timing + servo position
 *  - FIXME: power constants (DRIVE_POWER, STRAFE_POWER, TURN_POWER)
 */
public class BallChaseLogic {

    private void printRobotState(String moveDescription, double value) {
        String state = String.format("X: %.1f, Y: %.1f, H: %.1f", robotX, robotY, robotHeading);
        telemetry.addData("Last Move", moveDescription, value);
        telemetry.addData("Robot State", state);
        telemetry.update();
        System.out.println("MOVE: " + String.format(moveDescription, value) + " | STATE: " + state);

        // Send state to visualizer
        if (udpClient != null) {
            String jsonState = String.format("{\"type\":\"update\", \"x\":%.1f, \"y\":%.1f, \"heading\":%.1f}", robotX, robotY, robotHeading);
            udpClient.send(jsonState);
        }

        opMode.sleep(1000); // Pause to see the telemetry
    }


    // OpMode context and hardware dependencies
    private final LinearOpMode opMode;
    private final Telemetry telemetry;
    private final HuskyLens huskylens;
    private final DcMotor leftFront, rightFront, leftBack, rightBack; // Drivetrain
    private final DcMotor leftIntake, rightIntake; // Intake
    private final DcMotor leftOuttake, rightOuttake; // Outtake
    private final Servo launch;
    private final BNO055IMU imu;
    private UdpClient udpClient;


    // --- State & Constants ---

    // Simulated robot state
    private double robotX = 0.0; // inches
    private double robotY = 0.0; // inches
    private double robotHeading = 0.0; // degrees

    // Systems
    private final TakeSystem intakeSystem;
    private final TakeSystem outtakeSystem;

    // PID controller
    private final PID headingPID = new PID(0.01, 0.0, 0.0005);

    // Tag -> ball order (IDs from HuskyLens are 1..3)
    private static final int[][] ballOrders = {{2, 1, 1}, {1, 2, 1}, {1, 1, 2}};
    private static final double[][] ballSpots = {{24.0, -8.0, 0.0}, {24.0, 0.0, 0.0}, {24.0, 8.0, 0.0}};
    private static final double SHOOT_HEADING = 90.0;
    private static final double SHOOT_DISTANCE_INCHES = 40.0;
    private static final double COUNTS_PER_MOTOR_REV = 560;
    private static final double WHEEL_DIAMETER_INCHES = 3.5;
    private static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV) / (WHEEL_DIAMETER_INCHES * Math.PI);
    private static final double DRIVE_POWER = 0.6;
    private static final double STRAFE_POWER = 0.5;
    private static final double TURN_POWER = 0.5;

    public BallChaseLogic(LinearOpMode opMode, Telemetry telemetry, HuskyLens huskylens, BNO055IMU imu,
                          DcMotor leftFront, DcMotor rightFront, DcMotor leftBack, DcMotor rightBack,
                          DcMotor leftIntake, DcMotor rightIntake, DcMotor leftOuttake, DcMotor rightOuttake,
                          Servo launch) {
        this.opMode = opMode;
        this.telemetry = telemetry;
        this.huskylens = huskylens;
        this.imu = imu;
        this.leftFront = leftFront;
        this.rightFront = rightFront;
        this.leftBack = leftBack;
        this.rightBack = rightBack;
        this.leftIntake = leftIntake;
        this.rightIntake = rightIntake;
        this.leftOuttake = leftOuttake;
        this.rightOuttake = rightOuttake;
        this.launch = launch;

        this.intakeSystem = new TakeSystem(leftIntake, rightIntake);
        this.outtakeSystem = new TakeSystem(leftOuttake, rightOuttake);

        // Only initialize UDP client if in a simulation context (where opMode is BallChase_Simulation)
        if (opMode.getClass().getSimpleName().contains("Simulation")) {
            this.udpClient = new UdpClient("localhost", 41234);
        }
    }

    private void sendFieldConfiguration() {
        if (udpClient == null) return;

        StringBuilder json = new StringBuilder();
        json.append("{\"type\":\"config\",");
        json.append(String.format("\"initialState\":{\"x\":%.1f, \"y\":%.1f, \"heading\":%.1f},", robotX, robotY, robotHeading));
        json.append("\"balls\":[");
        for (int i = 0; i < ballSpots.length; i++) {
            json.append(String.format("{\"id\":%d, \"x\":%.1f, \"y\":%.1f}", i + 1, ballSpots[i][1], ballSpots[i][0]));
            if (i < ballSpots.length - 1) {
                json.append(",");
            }
        }
        json.append("], \"basket\":{\"x\":0, \"y\":60}}"); // Example basket position

        udpClient.send(json.toString());
    }

    public void run() {
        sendFieldConfiguration();
        opMode.sleep(100); // Give vis time to process config
        // 1) Read tag and get order
        int tag = readStartTag();
        if (tag < 1 || tag > 3) {
            telemetry.addLine("No valid tag found; defaulting to tag 1");
            telemetry.update();
            tag = 1;
        }
        int[] ballOrder = ballOrders[tag - 1];
        telemetry.addData("Pattern", "tag=%d order=%s", tag, java.util.Arrays.toString(ballOrder));
        telemetry.update();

        // 2) For each ball in the order: drive to known spot, align with vision if needed, intake
        for (int i = 0; i < ballOrder.length && opMode.opModeIsActive(); i++) {
            int ballID = ballOrder[i];
            telemetry.addData("Next Ball", "ID=%d", ballID);
            telemetry.update();

            double[] pose = getBallSpot(ballID);
            driveToApproxSpot(pose);
            alignToBallAndIntake(ballID);
            opMode.sleep(150);
        }

        // 3) Drive to shooting spot and shoot
        goToShootingSpotAndShoot();

        // Done
        stopAll();
        telemetry.addLine("Autonomous complete");
        telemetry.update();
    }

    private int readStartTag() {
        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
        ElapsedTime t = new ElapsedTime();
        while (opMode.opModeIsActive() && t.seconds() < 3.0) {
            HuskyLens.Block[] blocks = huskylens.blocks();
            if (blocks.length > 0) {
                int id = blocks[0].id;
                huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
                return id;
            }
            opMode.sleep(50);
        }
        huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
        return -1;
    }

    private double[] getBallSpot(int ballID) {
        return ballSpots[ballID - 1];
    }

    private void driveToApproxSpot(double[] pose) {
        double forward = pose[0];
        double strafe = pose[1];
        double heading = pose[2];

        driveStraightInches(forward, DRIVE_POWER);
        if (Math.abs(strafe) > 1.0) {
            strafeInches(strafe);
        }
        turnToAngle(heading);
    }

    private void alignToBallAndIntake(int targetID) {
        final int targetX = 160;
        final double targetArea = 3500;

        huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
        ElapsedTime timer = new ElapsedTime();
        boolean grabbed = false;

        while (opMode.opModeIsActive() && timer.seconds() < 2.0 && !grabbed) {
            HuskyLens.Block[] blocks = huskylens.blocks();
            HuskyLens.Block closest = getClosestBall(blocks, targetID);

            if (closest != null) {
                double turn = headingPID.update(targetX, closest.x);
                turn = clamp(turn, -0.4, 0.4);
                double forwardError = targetArea - (closest.width * closest.height);
                double forwardPower = clamp(0.0002 * forwardError, -0.35, 0.35);
                mecanumDrive(forwardPower, turn);

                telemetry.addData("Aligning", "x=%d area=%d", closest.x, closest.width * closest.height);
                telemetry.update();

                if (closest.width * closest.height >= targetArea) {
                    intakeOn();
                    opMode.sleep(300);
                    intakeOff();
                    grabbed = true;
                    stopAll();
                }
            } else {
                stopAll();
                opMode.sleep(50);
            }
        }
        stopAll();
    }

    private void goToShootingSpotAndShoot() {
        driveStraightInches(-6.0, 0.3);
        turnToAngle(SHOOT_HEADING);
        driveStraightInches(SHOOT_DISTANCE_INCHES, DRIVE_POWER);
        shootSequence();
        driveStraightInches(-12.0, 0.5);
        turnToAngle(0.0);
    }

    private void driveStraightInches(double inches, double power) {
        int counts = (int) Math.round(inches * COUNTS_PER_INCH);
        int lfTarget = leftFront.getCurrentPosition() + counts;
        int rfTarget = rightFront.getCurrentPosition() + counts;
        int lbTarget = leftBack.getCurrentPosition() + counts;
        int rbTarget = rightBack.getCurrentPosition() + counts;

        leftFront.setTargetPosition(lfTarget);
        rightFront.setTargetPosition(rfTarget);
        leftBack.setTargetPosition(lbTarget);
        rightBack.setTargetPosition(rbTarget);

        leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        double left = Math.signum(inches) * Math.abs(power);
        setMotorPowers(left, left);

        ElapsedTime t = new ElapsedTime();
        while (opMode.opModeIsActive() && (leftFront.isBusy() || rightFront.isBusy() || leftBack.isBusy() || rightBack.isBusy()) && t.seconds() < Math.max(2.0, Math.abs(inches) / 5.0)) {
            opMode.idle();
        }

        stopAll();
        resetAndRunUsingEncoders();

        // Update simulated position
        double mathAngleRad = Math.toRadians(-robotHeading + 90);
        robotX += inches * Math.cos(mathAngleRad);
        robotY += inches * Math.sin(mathAngleRad);
        printRobotState("Drove %.1f inches", inches);
    }

    private void strafeInches(double inches) {
        int counts = (int) Math.round(inches * COUNTS_PER_INCH);
        int lfTarget = leftFront.getCurrentPosition() + counts;
        int rfTarget = rightFront.getCurrentPosition() - counts;
        int lbTarget = leftBack.getCurrentPosition() - counts;
        int rbTarget = rightBack.getCurrentPosition() + counts;

        leftFront.setTargetPosition(lfTarget);
        rightFront.setTargetPosition(rfTarget);
        leftBack.setTargetPosition(lbTarget);
        rightBack.setTargetPosition(rbTarget);

        leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        double left = Math.signum(inches) * Math.abs(STRAFE_POWER);
        setMotorPowers(left, left);

        ElapsedTime t = new ElapsedTime();
        while (opMode.opModeIsActive() && (leftFront.isBusy() || rightFront.isBusy() || leftBack.isBusy() || rightBack.isBusy()) && t.seconds() < Math.max(2.0, Math.abs(inches) / 5.0)) {
            opMode.idle();
        }

        stopAll();
        resetAndRunUsingEncoders();

        // Update simulated position (strafing is 90 deg from heading)
        double mathAngleRad = Math.toRadians(-robotHeading); 
        robotX += inches * Math.cos(mathAngleRad);
        robotY += inches * Math.sin(mathAngleRad);
        printRobotState("Strafed %.1f inches", inches);
    }

    private void turnToAngle(double targetHeading) {
        targetHeading = normalizeAngle(targetHeading);
        headingPID.reset();
        ElapsedTime timeout = new ElapsedTime();

        while (opMode.opModeIsActive()) {
            double current = imu.getAngularOrientation().firstAngle;
            double error = angleDiff(targetHeading, current);
            double output = headingPID.update(0.0, error);
            output = clamp(output, -TURN_POWER, TURN_POWER);

            setMotorPowers(output, -output);

            telemetry.addData("TurnTo", "target=%.1f cur=%.1f err=%.1f out=%.2f", targetHeading, current, error, output);
            telemetry.update();

            if (Math.abs(error) < 2.0 || timeout.seconds() > 3.0) break;
            opMode.idle();
        }
        stopAll();

        // Update simulated heading
        robotHeading = targetHeading;
        printRobotState("Turned to %.1f deg", targetHeading);
    }
    
    private void resetAndRunUsingEncoders() {
        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void mecanumDrive(double forward, double rotate) {
        double lf = forward + rotate;
        double rf = forward - rotate;
        double lb = forward + rotate;
        double rb = forward - rotate;

        double max = Math.max(Math.max(Math.abs(lf), Math.abs(rf)), Math.max(Math.abs(lb), Math.abs(rb)));
        if (max > 1.0) {
            lf /= max; rf /= max; lb /= max; rb /= max;
        }

        leftFront.setPower(lf);
        rightFront.setPower(rf);
        leftBack.setPower(lb);
        rightBack.setPower(rb);
    }

    private void intakeOn() {
        intakeSystem.spin(1);
        telemetry.addLine("intake on");
        telemetry.update();
    }

    private void intakeOff() {
        intakeSystem.stop();
        telemetry.addLine("intake off");
        telemetry.update();
    }

    private void turnBy(double deltaAngle) {
        double current = imu.getAngularOrientation().firstAngle;
        double target = current + deltaAngle;
        turnToAngle(target);
    }

    private void shootSequence() {
        turnBy(180);
        outtakeSystem.spin(1);
        launch.setPosition(1);
        telemetry.addLine("Shooting...");
        telemetry.update();
        opMode.sleep(700);
        outtakeSystem.stop();
        launch.setPosition(0.1);
        turnBy(180);
        telemetry.addLine("Shot done");
        telemetry.update();
    }

    private HuskyLens.Block getClosestBall(HuskyLens.Block[] blocks, int targetID) {
        HuskyLens.Block best = null;
        int maxArea = -1;
        for (HuskyLens.Block b : blocks) {
            if (b.id != targetID) continue;
            int area = b.width * b.height;
            if (area > maxArea) { maxArea = area; best = b; }
        }
        return best;
    }

    private void setMotorPowers(double left, double right) {
        leftFront.setPower(left);
        leftBack.setPower(left);
        rightFront.setPower(right);
        rightBack.setPower(right);
    }

    private void stopAll() {
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double normalizeAngle(double a) {
        double v = a;
        while (v > 180.0) v -= 360.0;
        while (v <= -180.0) v += 360.0;
        return v;
    }

    private double angleDiff(double target, double current) {
        return normalizeAngle(target - current);
    }
}

