package org.firstinspires.ftc.teamcode.newteleop;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;


/**
 * Main TeleOp Handler.
 */

@TeleOp
public class AssembledBot extends LinearOpMode{

    //private RobotContainer  robot;
    private DriveTrain dt = new DriveTrain();
    private IMUOdometry imu = new IMUOdometry();

    private  static final int   OBJECT_TRACKING = 0;
    private  static final int   FACE_RECOGNITION = 1;
    private  static final int   COLOR_RECOGNITION = 2;
    private  static final int   OBJECT_RECOGNITION = 3;
    private  static final int   TAG_RECOGNITION = 4;
    private static double       driveScalar = 1.0;

    @Override
    public void runOpMode() throws InterruptedException {

        dt.init(hardwareMap);
        imu.init(hardwareMap);
        //robot = new RobotContainer(hardwareMap);

        waitForStart();


        double heading = 0;
     //    robot.selectHuskyMode(TAG_RECOGNITION);

        while (opModeIsActive()){

            double rightJoyStickX = gamepad2.left_stick_x;
            double rightJoyStickY = -gamepad2.left_stick_y;
            double leftJoystickX = gamepad2.right_stick_x;

//            boolean intakeButton = gamepad2.left_bumper;
//            boolean outtakeButton = gamepad2.right_bumper;
//            boolean servoTrigger = gamepad2.a;
//            boolean reverseOuttake = gamepad2.b;



        //    robot.useIntake(intakeButton);
        //    robot.useOuttake(outtakeButton);
        //    robot.setServoState(servoTrigger);

        //    if (robot.checkForTagRecognition())  robot.updateTurret();
            if (null != imu) {heading  = imu.getContinuousHeadingDeg();}


            dt.setSpeedScalar(driveScalar);
            dt.fieldOrientedTranslate(
                    rightJoyStickX,
                    rightJoyStickY,
                    leftJoystickX,
                    heading
            );

            telemetyStuff();

        //    if (reverseOuttake) { robot.intakeOuttake.reverseOuttake();}
        }
    }

    private void telemetyStuff(){
        if (null == imu) {return;}

        double c = imu.getContinuousHeadingDeg();
        YawPitchRollAngles o = imu.getRobotOrientation();
        telemetry.addData("Current Heading", c);
        telemetry.addData("Orientation", o );
        telemetry.update();

    }
}


/*
public class AssembledBot extends LinearOpMode {
    ElapsedTime     elapsedTime = new ElapsedTime();
    DriveTrain      dt = new DriveTrain();
    IMUOdometry     imu = new IMUOdometry();
    HuskyLensCamera husky = new HuskyLensCamera();

    // Placeholders

    RobotFunctions  func = new RobotFunctions(1.0, 1.0, 1.0, husky, elapsedTime);

    private static final double     STANDARD_OUTTAKE_POWER = 1.0;  // Change later when tested
    private static final double     STANDARD_INTAKE_POWER = 1.0;  // Change later when tested
    private static double           driveScalar = 1.0; // will change with controller
    private int                     tagId;
    private boolean                 isTagRead = false;


    public void runOpMode(){

        dt.init(hardwareMap);
        func.init(hardwareMap);
        imu.init(hardwareMap);
        husky.init(hardwareMap);

        waitForStart();

        dt.isMoving = true;


        while (opModeIsActive()) {

            double  targetPowerX = gamepad1.left_stick_x;
            double  targetPowerY = gamepad1.left_stick_y;
            double  targetRotation = gamepad1.right_stick_x;

            boolean intakeButton =  gamepad1.left_bumper;
            boolean outtakeButton = gamepad1.right_bumper;
            boolean servoTrigger =  gamepad1.a;
            boolean decrementDriveSpeed = gamepad1.dpad_down;
            boolean incrementDriveSpeed = gamepad1.dpad_up;

            double  currentHeadingDeg = imu.getContinuousHeadingDeg();

            func.activateIntake(STANDARD_INTAKE_POWER, intakeButton);
            func.activateOuttake(STANDARD_OUTTAKE_POWER, outtakeButton);
            func.servoEvent(servoTrigger);

            // small adjustment to drive speed,
            if (decrementDriveSpeed) driveScalar -= 0.1;
            if (incrementDriveSpeed) driveScalar += 0.1;

            driveScalar = Range.clip(driveScalar, 0.0, 1.0);

            dt.setSpeedScalar(driveScalar);
            dt.fieldOrientedTranslate(targetPowerX, targetPowerY, targetRotation, currentHeadingDeg);

            // if camera is serving other purpose, like colour detection, tag recognition cannot take place

            if (husky.isTagRecognition()){

                func.continuousUpdate();

                if (!isTagRead) {

                    this.tagId = husky.getTagID();
                    isTagRead = true;
                }
            }

            if (servoTrigger) func.isExtended = !func.isExtended; // see RobotFunctions.servoEvent()

        }
    }
}
*/
