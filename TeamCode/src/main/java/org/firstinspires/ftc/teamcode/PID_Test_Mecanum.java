package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.PID.PID;

@TeleOp(name = "PID_Test_Mecanum")
public class PID_Test_Mecanum extends LinearOpMode {

    // PID controller for forward/back movement
    private final PID forwardPID = new PID(0.01, 0, 0); // tune kp

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize motors
        DcMotor frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        DcMotor frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        DcMotor backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        DcMotor  backRight = hardwareMap.get(DcMotor.class, "backRight");

        // Reverse right side motors if needed
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        backRight.setDirection(DcMotor.Direction.REVERSE);

        // Reset encoders
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addLine("PID TeleOp Initialized. Press Start");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            // Read current forward position (average of all motors)
            double currentPosition = (frontLeft.getCurrentPosition() + frontRight.getCurrentPosition()
                    + backLeft.getCurrentPosition() + backRight.getCurrentPosition()) / 4.0;

            // Update PID
            // example target encoder ticks
            double targetPosition = 1000;
            double power = forwardPID.update(targetPosition, currentPosition);

            // Optional: clamp power
            power = Math.max(-1, Math.min(1, power));

            // Mecanum drive forward/back (no strafing for now)
            frontLeft.setPower(power);
            frontRight.setPower(power);
            backLeft.setPower(power);
            backRight.setPower(power);

            telemetry.addData("TargetPos", targetPosition);
            telemetry.addData("CurrentPos", currentPosition);
            telemetry.addData("Power", power);
            telemetry.update();
        }
    }
}
