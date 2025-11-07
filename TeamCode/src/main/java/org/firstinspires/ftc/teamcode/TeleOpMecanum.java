package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.systems.*;

@TeleOp(name="TeleOp - Mecanum (Linear)")
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
        leftIntake = hardwareMap.get(DcMotor.class, "leftIntakeMotor");
        rightIntake = hardwareMap.get(DcMotor.class, "rightIntakeMotor");
        leftOuttake = hardwareMap.get(DcMotor.class, "leftOuttakeMotor");
        rightOuttake = hardwareMap.get(DcMotor.class, "rightOuttakeMotor");

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        leftIntake.setDirection(DcMotorSimple.Direction.REVERSE);
        rightIntake.setDirection(DcMotorSimple.Direction.REVERSE);

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

            // intake
            leftIntake.setPower(-1);
            rightIntake.setPower(-1);

            if (gamepad2.dpad_down) {
                leftOuttake.setPower(-1);
                rightOuttake.setPower(-1);

//                intakeSystem.spin();
            }
            else {
                leftOuttake.setPower(0);
                rightOuttake.setPower(0);
            }


//            telemetry.update();
        }
    }
}


