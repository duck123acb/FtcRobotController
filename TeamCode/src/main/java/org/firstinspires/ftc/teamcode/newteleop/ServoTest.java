package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;

public class ServoTest extends LinearOpMode {

    Servo servo;

    double[] arr;



    public void runOpMode() throws InterruptedException{

        servo = hardwareMap.get(Servo.class, "launchServo");
        waitForStart();

        while (opModeIsActive()){

            boolean servoTrigger = gamepad1.a;

            if (servoTrigger) servo.setPosition(1);
            else servo.setPosition(0);
        }

    }

}

