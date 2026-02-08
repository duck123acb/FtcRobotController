package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;


/**
 * Main TeleOp Handler.
 */

@TeleOp(name= "More auto AssembledBot")
public class AssembledBot extends LinearOpMode{

//    private RobotContainer  robot;
    private DriveTrain dt = new DriveTrain();
    private IMUOdometry imu = new IMUOdometry();
    private double       driveScalar = 1.0;

    @Override
    public void runOpMode() throws InterruptedException {

        this.dt.init(hardwareMap);
        this.imu.init(hardwareMap);
//        this.robot = RobotContainer.createRobotContainer(hardwareMap, telemetry);

        this.waitForStart();


        double heading = 0;
//        robot.selectHuskyMode(HLMode.TAG_RECOGNITION);

        while (opModeIsActive()){

            double rightJoyStickX = -gamepad1.left_stick_x;
            double rightJoyStickY = -gamepad1.left_stick_y;
            double leftJoystickX = gamepad1.right_stick_x;

            boolean intakeButton = gamepad1.left_bumper;
            boolean incrementDriveScalar = gamepad1.dpad_up;
            boolean decrementDriveScalar = gamepad1.dpad_down;
            boolean randomButton = gamepad1.b;

            if (incrementDriveScalar) driveScalar += 0.1;
            if (decrementDriveScalar) driveScalar -= 0.1;

            driveScalar = Range.clip(driveScalar, 0, 1);
            

//            robot.useIntake(intakeButton);


//            if (randomButton && !(robot.isScheduleLoaded())) {
//
//                robot.loadTypicalSchedule();
//                robot.allowExecution();
//            }
//
//            if (robot.getSchedule().isEmpty()) robot.stopExecution();
//            if (robot.checkForTagRecognition())  robot.updateTurret();
            if (null != imu) {heading  = imu.getContinuousHeadingDeg();}


            dt.setSpeedScalar(driveScalar);
            dt.robotOrientedTranslate(
                    rightJoyStickX,
                    rightJoyStickY,
                    leftJoystickX
            );

            telemetyStuff();
        }
    }

    private void telemetyStuff(){

        if (null != dt) telemetry.addLine("DriveTrain initialized");
        if (null != imu) {
            telemetry.addLine("IMU initialized");
            telemetry.addData("Orientation", imu.getRobotOrientation());
            telemetry.addData("ContinuousYaw", imu.getContinuousHeadingDeg());
        }
//        if (robot.isIOInit()) telemetry.addLine("IO initialized");
//        if (robot.isTurretInit()) telemetry.addLine("Turret initialized");
//        if (robot.isHuskyInit()) {
//
//            telemetry.addLine("Husky initialized");
//            telemetry.addData("Husky mode:", robot.getCurrentMode());
//        }

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
