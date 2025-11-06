package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="TeleOp - Basic Mecanum (Linear)")
public class TeleOpMecanum extends LinearOpMode {
    DcMotor frontLeft, frontRight, backLeft, backRight;
    DcMotor leftIntake, rightIntake;
    DcMotor leftOuttake, rightOuttake;

    DriveSystem driveSystem;
    TakeSystem intakeSystem, outtakeSystem;

    @Override
    public void runOpMode() {
        // hardware mapping
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        frontRight = hardwareMap.get(DcMotor.class, "frontRightMotor");
        backLeft = hardwareMap.get(DcMotor.class, "backLeftMotor");
        backRight = hardwareMap.get(DcMotor.class, "backRightMotor");
        leftOuttake = hardwareMap.get(DcMotor.class, "leftOuttakeMotor");
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        // TODO: init intake motors/fling motor

        driveSystem = new DriveSystem(frontLeft, frontRight, backLeft, backRight);
        intakeSystem = new TakeSystem(leftIntake, rightIntake);

        telemetry.addLine("Initialized — ready to start!");
        telemetry.update();

        // wait for the start button
        waitForStart();

        // run until stop is pressed
        while (opModeIsActive()) {
            driveSystem.drive(gamepad2);

            if (gamepad2.aWasPressed()) {
                intakeSystem.spin();
            }
            else {
                intakeSystem.stop();
            }

            if (gamepad2.bWasPressed()) {
                outtakeSystem.spin();
            }
            else {
                outtakeSystem.stop();
            }

            leftOuttake.setPower(1);

//            telemetry.update();
        }
    }
}
