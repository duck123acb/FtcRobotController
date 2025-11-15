package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.systems.TakeSystem;


/**
 * 💡 Decode Auto — "BallChase"
 * runs the DECODE auto routine using HuskyLens tags (1–3) to pick a ball order.
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
 *
 */

@Autonomous(name = "BallChase_Decode_Mid")
public class BallChase extends LinearOpMode {
    private HuskyLens huskylens;

    // Drivetrain
    private DcMotor leftFront, rightFront, leftBack, rightBack;
    DcMotor leftIntake, rightIntake;
    DcMotor leftOuttake, rightOuttake;

    Servo launch;

    TakeSystem intakeSystem, outtakeSystem;


    // IMU
    private BNO055IMU imu;
    // PID controllers
    private final PID headingPID = new PID(0.01, 0.0, 0.0005); // FIXME: tune for turnToAngle

    // Tag -> ball order (IDs from HuskyLens are 1..3)
    private static final int[][] ballOrders = {
            {2, 1, 1}, // tag 1
            {1, 2, 1}, // tag 2
            {1, 1, 2}  // tag 3
    };

    // pre-mapped ball pickup poses (inches) relative to your starting pose.
    // index is ball ID-1. Fill these with your field measurements.
    // format: {forwardFromStart, strafeFromStart, headingDeg}
    private static final double[][] ballSpots = {
            {24.0, -8.0, 0.0},   // ball ID 1 (example: 24 inches forward, -8 strafe)
            {24.0, 0.0, 0.0},    // ball ID 2
            {24.0, 8.0, 0.0}     // ball ID 3
    }; // FIXME: measure real positions for your field


    private static final double SHOOT_HEADING = 90.0; // FIXME: tune to your actual basket location
    private static final double SHOOT_DISTANCE_INCHES = 40.0; // FIXME: measure real distance

    // Encoder / wheel constants
    private static final double COUNTS_PER_MOTOR_REV = 560; // FIXME: this value is how many times it 'ticks' per 1 revolution. try and find out from online sources on our motors

    private static final double WHEEL_DIAMETER_INCHES = 3.5; // FIXME: measure real diameter
    private static final double COUNTS_PER_INCH =
            (COUNTS_PER_MOTOR_REV) / (WHEEL_DIAMETER_INCHES * Math.PI);


    // Movement defaults
    private static final double DRIVE_POWER = 0.6; // FIXME: tune for the robot
    private static final double STRAFE_POWER = 0.5; // FIXME: tune for the robot
    private static final double TURN_POWER = 0.5; // FIXME: tune for the robot

    @Override
    public void runOpMode() {
        initHardware();
        waitForStart();

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
        for (int i = 0; i < ballOrder.length && opModeIsActive(); i++) {
            int ballID = ballOrder[i];
            telemetry.addData("Next Ball", "ID=%d", ballID);
            telemetry.update();

            double[] pose = ballSpots[ballID - 1];
            // drive to approximate spot using encoders (forward then strafe)
            driveToApproxSpot(pose);

            // optionally fine align with HuskyLens color recognition for micro-correction
            alignToBallAndIntake(ballID);

            sleep(150); // let intake settle
        }

        // 3) Drive to shooting spot and shoot
        goToShootingSpotAndShoot();

        // Done
        stopAll();
        telemetry.addLine("Autonomous complete");
        telemetry.update();
    }

    // Initialization
    private void initHardware() {
        // HuskyLens
        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
        telemetry.addData("HuskyLens", huskylens.knock() ? "connected" : "no comm");
        telemetry.update();

        // Motors
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");

        leftIntake = hardwareMap.get(DcMotor.class, "leftIntakeMotor");
        rightIntake = hardwareMap.get(DcMotor.class, "rightIntakeMotor");
        leftOuttake = hardwareMap.get(DcMotor.class, "leftOuttakeMotor");
        rightOuttake = hardwareMap.get(DcMotor.class, "rightOuttakeMotor");
        launch = hardwareMap.get(Servo.class, "launchServo");

        rightOuttake.setDirection(DcMotorSimple.Direction.REVERSE);

        leftFront.setDirection(DcMotor.Direction.FORWARD);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        intakeSystem = new TakeSystem(leftIntake, rightIntake);
        outtakeSystem = new TakeSystem(leftOuttake, rightOuttake);

        // Reset encoders
        resetAndRunUsingEncoders();

        // IMU init
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters p = new BNO055IMU.Parameters();
        p.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(p);

        // ensure HUD
        telemetry.addLine("Init done");
        telemetry.update();
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

    // Tag reading
    private int readStartTag() {
        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
        // loop a few times to give sensor a chance
        ElapsedTime t = new ElapsedTime();
        while (opModeIsActive() && t.seconds() < 3.0) {
            HuskyLens.Block[] blocks = huskylens.blocks();
            if (blocks.length > 0) {
                int id = blocks[0].id;
                // switch back to color mode for possible fine aligning later
                huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
                return id; // expected 1..3 on HuskyLens
            }
            sleep(50);
        }
        // fallback
        huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
        return -1;
    }

    // Ball helpers
    /**
     * driveToApproxSpot:
     *   1) drive forward/back to approximate forward distance
     *   2) strafe to the approximate lateral offset
     *   3) rotate to the pose heading
     */
    private void driveToApproxSpot(double[] pose) {
        // pose: {forwardInches, strafeInches, headingDeg}
        double forward = pose[0];
        double strafe = pose[1];
        double heading = pose[2];

        // Drive forward (positive forward is forward)
        driveStraightInches(forward, DRIVE_POWER);

        // Strafe (positive strafe right, negative left)
        if (Math.abs(strafe) > 1.0) {
            strafeInches(strafe);
        }

        // Rotate to heading
        turnToAngle(heading);
    }

    // Vision align + intake
    /**
     * Small vision-based alignment using HuskyLens color recognition to nudge into perfect pickup pose.
     * If the ball is not visible quickly, the method returns and we assume position is close enough.
     */
    private void alignToBallAndIntake(int targetID) {
        final int targetX = 160; // camera center
        final double targetArea = 3500; // when we consider "close enough" to intake; tune

        huskylens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);

        ElapsedTime timer = new ElapsedTime();
        timer.reset();
        boolean grabbed = false;

        // try for up to 2 seconds to sight + intake
        while (opModeIsActive() && timer.seconds() < 2.0 && !grabbed) {
            HuskyLens.Block[] blocks = huskylens.blocks();
            HuskyLens.Block closest = getClosestBall(blocks, targetID);

            if (closest != null) {
                // simple visual PID to nudge into center
                double turn = headingPID.update(targetX, closest.x); // reuse headingPID for small yaw correction
                turn = clamp(turn, -0.4, 0.4);

                // small forward/back correction based on area
                double forwardError = targetArea - (closest.width * closest.height);
                double forwardPower = clamp(0.0002 * forwardError, -0.35, 0.35);

                // apply mecanum forward + rotation (no strafe here)
                mecanumDrive(forwardPower, turn);

                telemetry.addData("Aligning", "x=%d area=%d", closest.x, closest.width * closest.height);
                telemetry.update();

                // if close enough, run intake to grab
                if (closest.width * closest.height >= targetArea) {
                    intakeOn();
                    sleep(300); // FIXME: tweak intake timing for the robot
                    intakeOff();
                    grabbed = true;
                    stopAll();
                }
            } else {
                // no vision target; small search sweep or just break
                stopAll();
                sleep(50);
            }
        }

        stopAll();
    }

    // Shooting flow
    private void goToShootingSpotAndShoot() {
        // Back off a little from ball zone (so we can orient freely)
        driveStraightInches(-6.0, 0.3); // back up 6 inches

        // Turn to shoot heading
        turnToAngle(SHOOT_HEADING);

        // Drive to shooting distance
        driveStraightInches(SHOOT_DISTANCE_INCHES, DRIVE_POWER);

        // Shoot action (placeholder)
        shootSequence();

        // Optionally back away to continue cycles (here we back up a bit)
        driveStraightInches(-12.0, 0.5);
        // face ball pickup zone heading (0 deg expected start)
        turnToAngle(0.0);
    }

    // Drive primitive functions
    private void driveStraightInches(double inches, double power) {
        int counts = (int) Math.round(inches * COUNTS_PER_INCH);

        // set target for each motor (simple forward/back)
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
        setMotorPowers(left,
                left);

        // simple timeout
        ElapsedTime t = new ElapsedTime();
        while (opModeIsActive() && (leftFront.isBusy() || rightFront.isBusy() || leftBack.isBusy() || rightBack.isBusy())
                && t.seconds() < Math.max(2.0, Math.abs(inches) / 5.0) ) {
            idle();
        }

        stopAll();
        resetAndRunUsingEncoders();
    }

    // Strafes right (positive) or left (negative) by inches. Mecanum approximation using same counts.
    private void strafeInches(double inches) {
        int counts = (int) Math.round(inches * COUNTS_PER_INCH);

        // mecanum strafing target:
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

        double left = Math.signum(inches) * Math.abs(BallChase.STRAFE_POWER);
        setMotorPowers(left,
                left);

        ElapsedTime t = new ElapsedTime();
        while (opModeIsActive() && (leftFront.isBusy() || rightFront.isBusy() || leftBack.isBusy() || rightBack.isBusy())
                && t.seconds() < Math.max(2.0, Math.abs(inches) / 5.0) ) {
            idle();
        }

        stopAll();
        resetAndRunUsingEncoders();
    }

     // Simple IMU-based rotate to absolute heading (degrees). Uses headingPID to stabilize.
    private void turnToAngle(double targetHeading) {
        // normalize target to -180..180
        targetHeading = normalizeAngle(targetHeading);

        headingPID.reset();
        ElapsedTime timeout = new ElapsedTime();
        timeout.reset();

        while (opModeIsActive()) {
            double current = imu.getAngularOrientation().firstAngle; // degrees
            double error = angleDiff(targetHeading, current);
            double output = headingPID.update(0.0, error); // control: try to drive error->0
            output = clamp(output, -TURN_POWER, TURN_POWER);

            // apply rotation: left positive, right negative for turning
            setMotorPowers(output, -output);

            telemetry.addData("TurnTo", "target=%.1f cur=%.1f err=%.1f out=%.2f",
                    targetHeading, current, error, output);
            telemetry.update();

            if (Math.abs(error) < 2.0 || timeout.seconds() > 3.0) break;
            idle();
        }
        stopAll();
    }

    // mecanum drive: forward, strafe, rotate (-1..1)
    private void mecanumDrive(double forward, double rotate) {
        // Basic decomposition for 4-motor mecanum
        double lf = forward + 0.0 + rotate;
        double rf = forward - 0.0 - rotate;
        double lb = forward - 0.0 + rotate;
        double rb = forward + 0.0 - rotate;

        // normalize
        double max = Math.max(Math.max(Math.abs(lf), Math.abs(rf)),
                Math.max(Math.abs(lb), Math.abs(rb)));
        if (max > 1.0) {
            lf /= max; rf /= max; lb /= max; rb /= max;
        }

        leftFront.setPower(lf);
        rightFront.setPower(rf);
        leftBack.setPower(lb);
        rightBack.setPower(rb);
    }

    // Intake & Shoot (placeholders)

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
        sleep(700); // FIXME: adjust shooting time / servo positions
        outtakeSystem.stop();
        launch.setPosition(0.1);
        turnBy(180);
        telemetry.addLine("Shot done");
        telemetry.update();
    }

    // ------------------------- Small helpers -------------------------------

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
