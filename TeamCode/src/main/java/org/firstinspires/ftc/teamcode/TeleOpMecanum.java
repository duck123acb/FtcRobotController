package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


import org.firstinspires.ftc.teamcode.systems.*;

@TeleOp(name="TeleOp - Mecanum (Linear)")
public class TeleOpMecanum extends LinearOpMode {
    DcMotor frontLeft, frontRight, backLeft, backRight;
    DcMotor leftIntake, rightIntake;
    DcMotor leftOuttake, rightOuttake;

    Servo launch;

    DriveSystem driveSystem;
    TakeSystem intakeSystem, outtakeSystem;

    double outtakePower = 1;
    int intakePower = 1;

    @Override
    public void runOpMode() {
        // hardware mapping
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        frontRight = hardwareMap.get(DcMotor.class, "frontRightMotor");
        backLeft = hardwareMap.get(DcMotor.class, "backLeftMotor");
        backRight = hardwareMap.get(DcMotor.class, "backRightMotor");
        leftIntake = hardwareMap.get(DcMotor.class, "leftIntakeMotor");
        rightIntake = hardwareMap.get(DcMotor.class, "rightIntakeMotor");
        leftOuttake = hardwareMap.get(DcMotor.class, "leftOuttakeMotor");
        rightOuttake = hardwareMap.get(DcMotor.class, "rightOuttakeMotor");
        launch = hardwareMap.get(Servo.class, "launchServo");

        // set motor directions

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rightOuttake.setDirection(DcMotorSimple.Direction.REVERSE);

        // initialize systems

        driveSystem = new DriveSystem(frontLeft, frontRight, backLeft, backRight);
        intakeSystem = new TakeSystem(leftIntake, rightIntake);
        outtakeSystem = new TakeSystem(leftOuttake, rightOuttake);

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

            driveSystem.drive(gamepad2);

            intakeSystem.spin(intakePower);

            if (gamepad2.y)
                outtakeSystem.spin(outtakePower);
            else
                outtakeSystem.stop();

            if (gamepad2.dpadUpWasPressed())
                outtakePower = Math.min(outtakePower + 0.2, 1);
            else if (gamepad2.dpadDownWasPressed())
                outtakePower = Math.max(outtakePower - 0.2, 0);

            if (gamepad2.b)
                launch.setPosition(-1.5);
            else
                launch.setPosition(.2);

            if (gamepad1.aWasPressed())
                intakePower *= -1;


            double roundedPower = Math.round(outtakePower * 100.0) / 100.0;
            telemetry.addData("Outtake Power", "%.2f", roundedPower);
            telemetry.update();

        }

        intakeSystem.stop();
        outtakeSystem.stop();
    }
}


