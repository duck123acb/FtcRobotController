package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="TeleOp - Basic Mecanum (Linear)")
public class TeleOpMecanum extends LinearOpMode {
    DcMotor frontLeft, frontRight, backLeft, backRight;
    DriveSystem driveSystem;

    @Override
    public void runOpMode() {
        // hardware mapping
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        frontRight = hardwareMap.get(DcMotor.class, "frontRightMotor");
        backLeft = hardwareMap.get(DcMotor.class, "backLeftMotor");
        backRight = hardwareMap.get(DcMotor.class, "backRightMotor");
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        driveSystem.init(frontLeft, frontRight, backLeft, backRight);

        telemetry.addLine("Initialized — ready to start!");
        telemetry.update();

        // wait for the start button
        waitForStart();

        // run until stop is pressed
        while (opModeIsActive()) {
           driveSystem.drive(gamepad2);
        }
    }
}
