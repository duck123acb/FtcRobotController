//package com.duck123acb.sim;
//
//import com.qualcomm.hardware.bosch.BNO055IMU;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.Servo;
//
//import org.firstinspires.ftc.teamcode.simulation.MockBNO055IMU;
//import org.firstinspires.ftc.teamcode.simulation.MockDcMotor;
//import org.firstinspires.ftc.teamcode.simulation.MockHuskyLens;
//import org.firstinspires.ftc.teamcode.simulation.MockServo;
//
///**
// * This is the simulation OpMode for the "BallChase" autonomous routine.
// * It initializes mock hardware and then passes control to the BallChaseLogic class.
// * This allows testing the autonomous logic without a physical robot.
// */
//@Autonomous(name = "SIMULATION: Ball Chase")
//public class BallChase_Simulation extends LinearOpMode {
//
//    private MockHuskyLens huskylens;
//
//    @Override
//    public void runOpMode() {
//        // Initialize mock hardware
//        huskylens = new MockHuskyLens();
//        MockBNO055IMU imu = new MockBNO055IMU();
//        DcMotor leftFront = new MockDcMotor();
//        DcMotor rightFront = new MockDcMotor();
//        DcMotor leftBack = new MockDcMotor();
//        DcMotor rightBack = new MockDcMotor();
//        DcMotor leftIntake = new MockDcMotor();
//        DcMotor rightIntake = new MockDcMotor();
//        DcMotor leftOuttake = new MockDcMotor();
//        DcMotor rightOuttake = new MockDcMotor();
//        Servo launch = new MockServo();
//
//        // Set motor directions
//        rightOuttake.setDirection(DcMotorSimple.Direction.REVERSE);
//        leftFront.setDirection(DcMotor.Direction.FORWARD);
//        leftBack.setDirection(DcMotor.Direction.FORWARD);
//        rightFront.setDirection(DcMotor.Direction.REVERSE);
//        rightBack.setDirection(DcMotor.Direction.REVERSE);
//
//        // Initialize IMU
//        BNO055IMU.Parameters p = new BNO055IMU.Parameters();
//        p.angleUnit = BNO055IMU.AngleUnit.DEGREES;
//        imu.initialize(p);
//
//        primeHuskyLensWithTag(1); // Default to seeing tag 1
//
//        telemetry.addLine("Mock hardware initialized");
//        telemetry.update();
//
//        // Create the logic class and inject the hardware
//        BallChaseLogic autonomousLogic = new BallChaseLogic(this, telemetry, huskylens, imu,
//                leftFront, rightFront, leftBack, rightBack,
//                leftIntake, rightIntake, leftOuttake, rightOuttake, launch);
//
//        waitForStart();
//
//        if (opModeIsActive()) {
//            autonomousLogic.run();
//        }
//    }
//
//    /**
//     * Primes the mock HuskyLens with a simulated AprilTag.
//     * @param tagId The ID of the tag to simulate (1, 2, or 3).
//     */
//    private void primeHuskyLensWithTag(int tagId) {
//        huskylens.setBlocks(new MockHuskyLens.Block[] { MockHuskyLens.createBlock(tagId, 160, 120, 30, 30) });
//    }
//}
//
