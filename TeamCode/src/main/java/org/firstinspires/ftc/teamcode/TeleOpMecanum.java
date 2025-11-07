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

    double outtakePower = 1;

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
//        leftOuttake.setDirection(DcMotorSimple.Direction.REVERSE);
        rightOuttake.setDirection(DcMotorSimple.Direction.REVERSE);

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

            intakeSystem.spin(1);

            if (gamepad2.y)
                outtakeSystem.spin(outtakePower);
            else
                outtakeSystem.stop();

            if (gamepad2.dpad_up) {
                outtakePower = Math.min(outtakePower + 0.1, 1);
            } else if (gamepad2.dpad_down) {
                outtakePower = Math.max(outtakePower - 0.1, 0);
            }
//            telemetry.update();
        }

        intakeSystem.stop();
    }
}


