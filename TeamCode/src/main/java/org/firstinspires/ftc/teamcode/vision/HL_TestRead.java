package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/*
TODO: IMPLEMENT INTO TELEOP MECANUM AFTER TEST
 */

@TeleOp(name = "HL_TestRead_Closest")
public class HL_TestRead extends LinearOpMode {
    int GREEN_BALL_ID = 1;
    int PURPLE_BALL_ID = 2;

    @Override
    public void runOpMode() throws InterruptedException {
        HuskyLensI2C husky = new HuskyLensI2C(hardwareMap, "camera");

        telemetry.addLine("Initialized. Press start");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            HuskyLensI2C.HuskyLensBlock[] blocks = husky.getBlocks(Algorithm.COLOR_RECOGNITION);

            HuskyLensI2C.HuskyLensBlock closestGreen = husky.getClosestBlock(blocks, GREEN_BALL_ID);
            HuskyLensI2C.HuskyLensBlock closestPurple = husky.getClosestBlock(blocks, PURPLE_BALL_ID);

            if (closestGreen != null) {
                telemetry.addData("Green Ball", "x=%d y=%d w=%d h=%d", closestGreen.xCenter, closestGreen.yCenter, closestGreen.width, closestGreen.height);
            }
            if (closestPurple != null) {
                telemetry.addData("Purple Ball", "x=%d y=%d w=%d h=%d", closestPurple.xCenter, closestPurple.yCenter, closestPurple.width, closestPurple.height);
            }

            telemetry.update();
        }
    }
}
