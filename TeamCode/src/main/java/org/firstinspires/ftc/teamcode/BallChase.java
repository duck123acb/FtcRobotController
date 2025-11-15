package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * This is the main OpMode for the "BallChase" autonomous routine.
 * It initializes the real hardware and then passes control to the BallChaseLogic class.
 */
@Autonomous(name = "BallChase_Decode_Mid")
public class BallChase extends LinearOpMode {

    @Override
    public void runOpMode() {
        // Initialize real hardware
        HuskyLens huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
        BNO055IMU imu = hardwareMap.get(BNO055IMU.class, "imu");
        DcMotor leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        DcMotor rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        DcMotor leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        DcMotor rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        DcMotor leftIntake = hardwareMap.get(DcMotor.class, "leftIntakeMotor");
        DcMotor rightIntake = hardwareMap.get(DcMotor.class, "rightIntakeMotor");
        DcMotor leftOuttake = hardwareMap.get(DcMotor.class, "leftOuttakeMotor");
        DcMotor rightOuttake = hardwareMap.get(DcMotor.class, "rightOuttakeMotor");
        Servo launch = hardwareMap.get(Servo.class, "launchServo");

        // Set motor directions
        rightOuttake.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFront.setDirection(DcMotor.Direction.FORWARD);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        // Initialize IMU
        BNO055IMU.Parameters p = new BNO055IMU.Parameters();
        p.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(p);

        telemetry.addLine("Real hardware initialized");
        telemetry.update();

        // Create the logic class and inject the hardware
        BallChaseLogic autonomousLogic = new BallChaseLogic(this, telemetry, huskylens, imu,
                leftFront, rightFront, leftBack, rightBack,
                leftIntake, rightIntake, leftOuttake, rightOuttake, launch);

        waitForStart();

        if (opModeIsActive()) {
            autonomousLogic.run();
        }
    }
}
