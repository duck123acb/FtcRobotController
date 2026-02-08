//package org.firstinspires.ftc.teamcode;
//
//
//import com.qualcomm.hardware.bosch.BNO055IMU;
//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import com.qualcomm.robotcore.hardware.HardwareMap;
//import com.qualcomm.robotcore.hardware.IMU;
//
//import org.firstinspires.ftc.ftccommon.internal.manualcontrol.parameters.ImuParameters;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
//import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
//import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
//import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
//
//public class IMUOdometry {
//
//    private IMU imu;
//    private static double  lastRawYawDeg;
//    private static double continuousYawDeg;
//
//
//    public void init(HardwareMap hwMap){
//
//        imu = hwMap.get(IMU.class, "imu");
//
//        RevHubOrientationOnRobot.LogoFacingDirection logo = RevHubOrientationOnRobot.LogoFacingDirection.BACKWARD;
//        RevHubOrientationOnRobot.UsbFacingDirection usb = RevHubOrientationOnRobot.UsbFacingDirection.UP;
//
//        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logo, usb);
////        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
////
////        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
////        params.mode = BNO055IMU.SensorMode.IMU;
//
//        imu.initialize(new IMU.Parameters(orientationOnRobot));
//
//    }
//
//    public Orientation getRobotOrientation(){
//
//        return imu.getRobotOrientation(
//                AxesReference.INTRINSIC,
//                AxesOrder.ZYX,
//                AngleUnit.DEGREES
//        );
//    }
//
//    public double getHeadingDeg(){ return getRobotOrientation().firstAngle; }
//    public double getRollDeg() { return getRobotOrientation().thirdAngle; }
//
//    public double getContinuousHeadingDeg(){
//
//        double rawYawDeg = getHeadingDeg();
//        double deltaDeg = rawYawDeg - lastRawYawDeg;
//
//        // Wrap around handling (-180 to 180)
//        if (deltaDeg < -180) deltaDeg += 360;
//        if (deltaDeg > 180) deltaDeg -= 360;
//
//        continuousYawDeg += deltaDeg;
//        lastRawYawDeg = rawYawDeg;
//
//        return continuousYawDeg;
//
//    }
//}


package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

public class IMUOdometry {

    private IMU     imu;
    private static double  lastRawYawDeg;
    private static double continuousYawDeg;


    public void init(HardwareMap hwMap){

        imu = hwMap.get(IMU.class, "imu");
        IMU.Parameters parameters;

        parameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.BACKWARD,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                )
        );

        imu.initialize(parameters);
    }

    //------------------------------------------------------------------
    //Getters
    // ------------------------------------------------------------------
    public YawPitchRollAngles getRobotOrientation() {return imu.getRobotYawPitchRollAngles();}
    private double getHeadingDeg() {return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);}
    public double getRollDeg() {return imu.getRobotYawPitchRollAngles().getRoll(AngleUnit.DEGREES);}

    public double getContinuousHeadingDeg(){

        double rawYawDeg = getHeadingDeg();
        double deltaDeg = rawYawDeg - lastRawYawDeg;

        if (deltaDeg < -180.0) { deltaDeg += 360.0; }
        if (deltaDeg > 180.0) {deltaDeg -= 360.0; }

        continuousYawDeg += deltaDeg;
        lastRawYawDeg = rawYawDeg;

        return continuousYawDeg;
    }
}