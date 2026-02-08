package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class DriveTrain {
    private double  speedScalar = 0.0;

    // initialize motors
    private DcMotor frontLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backLeftMotor;
    private DcMotor backRightMotor;


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

    public void fieldOrientedTranslate(double targetPowerX,
                                       double targetPowerY,
                                       double rotation,
                                       double currentRotation) {

        // direction robot is facing, in radians
        double robotYawRad = angleWrapRad(Math.toRadians(currentRotation));

        // direction stick is pointing, mapped to [-π, π]
        double stickRotationRad = Math.atan2(targetPowerY, targetPowerX);

        // offset joystick vector to account for robot orientation
        double thetaRad = stickRotationRad - robotYawRad;

        // sets power to be length of joystick vector
        double power = Math.hypot(targetPowerX, targetPowerY);

        // restricting power between [-1, 1] because of a bug:
        // if |targetPowerX|, |targetPowerY| = 1, power = |√2| > 1.
        //However, motor power is [-1, 1]
        power = Range.clip(power, 0.0, 1.0);

        // sin, cos of corrected angle, accounting for mecanum offset
        double sin = Math.sin(thetaRad - Math.PI/4);
        double cos = Math.cos(thetaRad - Math.PI/4);

        // Essentially, sin, cos, are the vertical, horizontal vectors that comprise theta

        double maxSinCos = Math.max(Math.abs(sin), Math.abs(cos));

        double frontRightPower;
        double frontLeftPower;
        double backRightPower;
        double backLeftPower;

        if (1e-6 > maxSinCos) maxSinCos = 1; // avoid division by 0

        frontLeftPower =    power*sin/maxSinCos + rotation; // swap +- to invert rotation direction
        backLeftPower =     power*cos/maxSinCos + rotation;

        frontRightPower =   power*cos/maxSinCos - rotation;
        backRightPower =    power*sin/maxSinCos - rotation;

        double frontMax =   Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        double backMax =    Math.max(Math.abs(backLeftPower), Math.abs(backRightPower));

        double maxPower =   Math.max(frontMax, backMax);

        // a normalization
        if (1.0 < maxPower) {

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


//     android studio was complaining about the complex if statement
    private boolean motorsNotNull() {

        return  null != frontLeftMotor && null != frontRightMotor &&
                null != backLeftMotor && null != backRightMotor;
    }

//     practically same logic as fieldOrientedTranslate(),
//     just without angle math
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

        if (1e-6 > maxSinCos) maxSinCos = 1;

        frontLeftPower =    power*sin/maxSinCos + rotation;
        backLeftPower =     power*cos/maxSinCos + rotation;

        frontRightPower =   power*cos/maxSinCos - rotation;
        backRightPower =    power*sin/maxSinCos - rotation;

        double frontMax = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        double backMax = Math.max(Math.abs(backLeftPower), Math.abs(backRightPower));

        double maxPower = Math.max(frontMax, backMax);

        if (1.0 < maxPower){

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


//    /**
//     *
//     * @param targetPowerX x component of left joystick
//     * @param targetPowerY y component of left
//     * @param rotation x component of right joystick
//     * @param currentRotation robot yaw
//     */
//    public void fieldOrientedTranslate(double targetPowerX,
//                                       double targetPowerY,
//                                       double rotation,
//                                       double currentRotation){
//
//        double robotYawRad = angleWrapRad(Math.toRadians(currentRotation));
//        double stickRotationRad = Math.atan2(targetPowerX, targetPowerY);
//        double thetaRad = stickRotationRad - robotYawRad;
//        double power = Math.hypot(targetPowerX, targetPowerY);
//
//        double[] motorPowers = this.calculateMotorPower(power, rotation, thetaRad);
//        applyPower(motorPowers);
//
//    }
//
//
//    /**
//     *
//     * @param targetPowerX x component of left joystick
//     * @param targetPowerY y component of left joystick
//     * @param rotation x component of right joystick
//     */
//    public void robotOrientedTranslate(double targetPowerX,
//                                       double targetPowerY,
//                                       double rotation){
//
//        double thetaRad = Math.atan2(targetPowerX, targetPowerY);
//        double power = Math.hypot(targetPowerX, targetPowerY);
//
//        double[] motorPowers = calculateMotorPower(power, rotation, thetaRad);
//        applyPower(motorPowers);
//
//    }
//
//    /**
//     *
//     * @param initialPower initial power calculation that is to be corrected, mapped to [0, 1]
//     * @param rotation x component of the right joystick
//     * @param thetaRad angle in standard position of the terminal arm of the point (targetPowerX, targetPowerY).
//     *                 Note theta is [-π, π] because it is calculated using Math.atan2().
//     * @return an array containing the calculated power for each motor, {frontLeft, frontRight, backLeft, backRight}, respectively.
//     */
//    private double[] calculateMotorPower(double initialPower,
//                                         double rotation,
//                                         double thetaRad) {
//
//        double power = initialPower;
//
//        double sin = Math.sin(thetaRad - Math.PI/4);
//        double cos = Math.cos(thetaRad - Math.PI/4);
//
//        double maxSinCos = Math.max(Math.abs(sin), Math.abs(cos));
//
//        double frontRightPower;
//        double frontLeftPower;
//        double backRightPower;
//        double backLeftPower;
//
//        if (1e-6 > maxSinCos) maxSinCos = 1;
//
//        frontLeftPower =    power*sin/maxSinCos + rotation;
//        backLeftPower =     power*cos/maxSinCos + rotation;
//
//        frontRightPower =   power*cos/maxSinCos - rotation;
//        backRightPower =    power*sin/maxSinCos - rotation;
//
//        double frontMax = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
//        double backMax = Math.max(Math.abs(backLeftPower), Math.abs(backRightPower));
//
//        double maxPower = Math.max(frontMax, backMax);
//
//        if (1.0 < maxPower){
//
//            frontLeftPower /= maxPower;
//            frontRightPower /= maxPower;
//
//            backLeftPower /= maxPower;
//            backRightPower /= maxPower;
//        }
//
//        double[] motorPowers = {frontLeftPower, frontRightPower, backLeftPower, backRightPower};
//
//        return motorPowers;
//    }
//
//    /**
//     *
//     * @param motorPowers a 4-element array, {frontLeft, frontRight, backLeft, backRight}
//     */
//    private final void applyPower(double[] motorPowers){
//
//        if (!this.motorsNotNull()) return;;
//        if (4 != motorPowers.length) return;
//
//        frontLeftMotor.setPower(motorPowers[0]);
//        frontRightMotor.setPower(motorPowers[1]);
//
//        backLeftMotor.setPower(motorPowers[2]);
//        backRightMotor.setPower(motorPowers[3]);
//    }


    /**
     *
     * @param change new value for power correction
     */
    public final void setSpeedScalar(double change) {
        // serves as a slowdown -> easier control for driver
        speedScalar = Range.clip(change, 0.0, 1.0);
    }
}


