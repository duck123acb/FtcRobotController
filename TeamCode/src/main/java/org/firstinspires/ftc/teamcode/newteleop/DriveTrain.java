package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class DriveTrain {
    private double  speedScalar = 0.0;

    // initialize motors
    public DcMotor frontLeftMotor;
    public DcMotor frontRightMotor;
    public DcMotor backLeftMotor;
    public DcMotor backRightMotor;


    public void init(HardwareMap hwMap){

        this.frontLeftMotor = hwMap.get(DcMotor.class, "frontLeftMotor");
        this.frontRightMotor = hwMap.get(DcMotor.class, "frontRightMotor");
        this.backLeftMotor = hwMap.get(DcMotor.class, "backLeftMotor");
        this.backRightMotor = hwMap.get(DcMotor.class, "backRightMotor");

        this.frontLeftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.frontRightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.backLeftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.backRightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        this.frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //-----------------------------------------
        //IF THE ROBOT IS DRIVING IN REVERSE, FLIP THESE
        //---------------------------------------------

        this.frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        this.backLeftMotor.setDirection(DcMotor.Direction.REVERSE);

        this.frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        this.backRightMotor.setDirection(DcMotor.Direction.FORWARD);
    }

    // restrict the angle reading, in radians, from the imu [-π, π]
    private double angleWrapRad(double rad){

        while (rad > Math.PI) { rad -= Math.PI * 2;}
        while (rad < -Math.PI) { rad += Math.PI *2;}
        return rad;
    }

    public void fieldOrientedTranslate(double targetPowerX, double targetPowerY, double rotation, double currentRotation) {

        // direction robot is facing, in degrees
        double robotYawDeg = Math.toDegrees(angleWrapRad(Math.toRadians(currentRotation)));

        // direction stick is pointing, mapped to [-180, 180]
        double stickRotationDeg = Math.toDegrees(Math.atan2(targetPowerY, targetPowerX));

        // offset joystick vector to account for robot orientation
        double thetaDeg = 360.0 - robotYawDeg + stickRotationDeg;

        // sets power to be length of joystick vector
        double power = Math.hypot(targetPowerX, targetPowerY);

        // restricting power between [-1, 1] because of a bug:
        // if abs(targetPowerX), abs(targetPowerY) = 1, then power would be abs(√2) > 1.
        // Ergo resulting in inefficiencies as motor power is [-1, 1]
        power = Range.clip(power, -1.0, 1.0);

        // sin, cos of corrected angle, accounting for mecanum offset
        double sin = Math.sin((thetaDeg*Math.PI/180.0) + Math.PI/4);
        double cos = Math.cos((thetaDeg*Math.PI/180.0) + Math.PI/4);

        double maxSinCos = Math.max(Math.abs(sin), Math.abs(cos));

        double frontRightPower;
        double frontLeftPower;
        double backRightPower;
        double backLeftPower;


    //      Essentially, sin and cos are representative of the vertical and
    //      horizontal vectors that comprise the vector of
    //      the angle theta. By applying sin to the power of one set of wheels,
    //      and cos to other, we recreate that vector.

        frontLeftPower =    power*cos/maxSinCos + rotation; // swap +- to invert rotation direction
        backLeftPower =     power*sin/maxSinCos + rotation;

        frontRightPower =   power*sin/maxSinCos - rotation;
        backRightPower =    power*cos/maxSinCos - rotation;

        double frontMax =   Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        double backMax =    Math.max(Math.abs(backLeftPower), Math.abs(backRightPower));

        double maxPower =   Math.max(frontMax, backMax);

        // a normalization
        if (maxPower > 1.0) {

            frontLeftPower /= maxPower;
            frontRightPower /= maxPower;

            backLeftPower /= maxPower;
            backRightPower /= maxPower;
        }
        // isMoving will serve for autonomous
        if (motorsNotNull()){

            // speedScalar will be set from the main function (whenever its built)
            frontLeftMotor.setPower(frontLeftPower * speedScalar);
            frontRightMotor.setPower(frontRightPower * speedScalar);

            backLeftMotor.setPower(backLeftPower * speedScalar);
            backRightMotor.setPower(backRightPower * speedScalar);
        }
    }
    // android studio was complaining about the complex if statement
    private boolean motorsNotNull() {

        return null != frontLeftMotor && null != frontRightMotor && null != backLeftMotor && null != backRightMotor;
    }

    // practically same logic as fieldOrientedTranslate(),
    // just without angle math
    public void robotOrientedTranslate(double targetPowerX, double targetPowerY, double rotation){

        double thetaRad = Math.atan2(targetPowerY, targetPowerX);
        double power = Math.hypot(targetPowerX,targetPowerY);

        double sin = Math.sin(thetaRad - Math.PI/4);
        double cos = Math.cos(thetaRad - Math.PI/4);

        double maxSinCos = Math.max(Math.abs(sin), Math.abs(cos));

        double frontRightPower;
        double frontLeftPower;
        double backRightPower;
        double backLeftPower;

        frontLeftPower = power*cos/maxSinCos + rotation;
        backLeftPower = power*sin/maxSinCos + rotation;

        frontRightPower = power*sin/maxSinCos - rotation;
        backRightPower = power*cos/maxSinCos - rotation;

        double frontMax = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        double backMax = Math.max(Math.abs(backLeftPower), Math.abs(backRightPower));

        double maxPower = Math.max(frontMax, backMax);

        if (maxPower > 1.0){

            frontLeftPower /= maxPower;
            frontRightPower /= maxPower;

            backLeftPower /= maxPower;
            backRightPower /= maxPower;
        }

        if (motorsNotNull()){

            frontLeftMotor.setPower(frontLeftPower * speedScalar);
            frontRightMotor.setPower(frontRightPower * speedScalar);

            backLeftMotor.setPower(backLeftPower * speedScalar);
            backRightMotor.setPower(backRightPower * speedScalar);
        }
    }
    public final void setSpeedScalar(double change) {
        // serves as a slowdown -> easier control for driver
        speedScalar = Range.clip(change, 0.0, 1.0);
    }
}


