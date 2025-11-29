package org.firstinspires.ftc.teamcode.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name="TeleOp - Mecanum (Linear)")
public class TeleOpMecanum extends LinearOpMode {
    DcMotor frontLeft, frontRight, backLeft, backRight;
    DcMotor intake, outtake;

    Servo launch;

//    DriveSystem driveSystem;
//    TakeSystem intakeSystem, outtakeSystem;

    double outtakePower = 1;
    int intakePower = 1;

    @Override
    public void runOpMode() {
        // hardware mapping
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        frontRight = hardwareMap.get(DcMotor.class, "frontRightMotor");
        backLeft = hardwareMap.get(DcMotor.class, "backLeftMotor");
        backRight = hardwareMap.get(DcMotor.class, "backRightMotor");
        intake = hardwareMap.get(DcMotor.class, "leftIntakeMotor");
        outtake = hardwareMap.get(DcMotor.class, "leftOuttakeMotor");
        launch = hardwareMap.get(Servo.class, "launchServo");

        // set motor directions
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        // initialize systems
//        driveSystem = new DriveSystem(frontLeft, frontRight, backLeft, backRight);
//        intakeSystem = new TakeSystem(leftIntake, rightIntake);
//        outtakeSystem = new TakeSystem(leftOuttake, rightOuttake);

        telemetry.addLine("Initialized — ready to start!");
        telemetry.update();

        // wait for the start button
        waitForStart();

        // run until stop is pressed
        while (opModeIsActive()) {
            if (gamepad2.x) {
                terminateOpModeNow();
                break;
            }

            drive();

            intake.setPower(intakePower);

            if (gamepad2.dpad_up)
                outtake.setPower(1);
            else if (gamepad2.dpad_down) {
                outtake.setPower(-1);
            }
            else
                outtake.setPower(0);

//            if (gamepad22.dpadUpWasPressed())
//                outtakePower = Math.min(outtakePower + 0.2, 1);
//            else if (gamepad22.dpadDownWasPressed())
//                outtakePower = Math.max(outtakePower - 0.2, 0);

            if (gamepad2.b)
                launch.setPosition(-1.5);
            else
                launch.setPosition(.2);

            // why doesnt this work :(
//            if (gamepad21.aWasPressed())
//                intakePower *= -1;

            // rounding for readability
            double roundedPower = Math.round(outtakePower * 100.0) / 100.0;
            telemetry.addData("Outtake Power", "%.2f", roundedPower);
            telemetry.update();

        }
    }

    public void drive() {
        double y = gamepad2.left_stick_y;   // forward/backward
        double x = -gamepad2.left_stick_x;    // strafe left/right
        double rx = -gamepad2.right_stick_x;  // rotate right/left

        // basic mecanum drive math
        double fl = y + x + rx;
        double bl = y - x + rx;
        double fr = y - x - rx;
        double br = y + x - rx;

        // normalize if any value is above 1
        double max = Math.max(Math.max(Math.abs(fl), Math.abs(fr)), Math.max(Math.abs(bl), Math.abs(br)));
        if (max > 1.0) {
            fl /= max;
            fr /= max;
            bl /= max;
            br /= max;
        }

        // apply calculated powers to motors
        frontLeft.setPower(fl);
        backLeft.setPower(bl);
        frontRight.setPower(fr);
        backRight.setPower(br);
    }
}


