package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="More manual Fallback")
public class AssembledBotOther extends LinearOpMode {
    private RobotContainer  robot;
    private DriveTrain dt = new DriveTrain();
    private IMUOdometry imu = new IMUOdometry();
    private double       driveScalar = 1.0;

    @Override
    public void runOpMode() throws InterruptedException {

        this.dt.init(hardwareMap);
        this.imu.init(hardwareMap);
        this.robot = RobotContainer.createRobotContainer(hardwareMap, telemetry);

        this.waitForStart();


        double heading = 0;
        robot.selectHuskyMode(HLMode.TAG_RECOGNITION);

        while (opModeIsActive()){

            double rightJoyStickX = gamepad1.left_stick_x;
            double rightJoyStickY = -gamepad1.left_stick_y;
            double leftJoystickX = gamepad1.right_stick_x;

            boolean intakeButton = gamepad2.left_bumper;
            boolean outtakeButton = gamepad2.right_bumper;
            boolean servoTrigger = gamepad2.a;
            boolean turnSpindexer = gamepad2.x;

            boolean incrementDriveScalar = gamepad1.dpad_up;
            boolean decrementDriveScalar = gamepad1.dpad_down;

            if (incrementDriveScalar) driveScalar += 0.1;
            if (decrementDriveScalar) driveScalar -= 0.1;

            driveScalar = Range.clip(driveScalar, 0, 1);


            robot.useIntake(intakeButton);
            robot.useOuttake(outtakeButton);
            robot.setServoState(servoTrigger);
            if (turnSpindexer && !robot.isSpindexerBusy()){ robot.rotate(robot.getCurrentSpindexerPos() + 120);}

            if (robot.checkForTagRecognition())  robot.updateTurret();
            if (null != imu) {heading  = imu.getContinuousHeadingDeg();}


            dt.setSpeedScalar(driveScalar);
            dt.fieldOrientedTranslate(
                    rightJoyStickX,
                    rightJoyStickY,
                    leftJoystickX,
                    heading
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
        if (robot.isIOInit()) telemetry.addLine("IO initialized");
        if (robot.isTurretInit()) telemetry.addLine("Turret initialized");
        if (robot.isHuskyInit()) {

            telemetry.addLine("Husky initialized");
            telemetry.addData("Husky mode:", robot.getCurrentMode());
        }

        telemetry.update();
    }
}
