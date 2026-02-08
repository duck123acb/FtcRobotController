package org.firstinspires.ftc.teamcode.newteleop.eventschedule;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.newteleop.IntakeOuttakeSystem;

public class ScheduleIntakeOuttake extends ScheduledEvent{

    private final IntakeOuttakeSystem intakeOuttakeSystem;
    private final double power;
    private boolean isFinished;
    private final static int MS_TO_REV_UP_OUTTAKE = 500;
    private static final int MS_IDLE_FOR_SERVO = 100;
    private static final double SERVO_LAUNCH_POS = 0.8;
    private final Operations operation;
    private final ElapsedTime timer = new ElapsedTime();
    private final Telemetry telemetry;

    public enum Operations {

        REV_UP_OUTTAKE,
        LAUNCH_ARTIFACT,
        STOP_OUTTAKE
    }
    public ScheduleIntakeOuttake(String caller,
                                 IntakeOuttakeSystem intakeOuttakeSystem,
                                 double power,
                                 Operations operation,
                                 Telemetry telemetry) {
        super("io", caller);
        this.intakeOuttakeSystem = intakeOuttakeSystem;
        this.power = power;
        this.operation = operation;
        this.telemetry = telemetry;
    }

    @Override
    public void start(){

        timer.reset();
        switch (operation){

            case REV_UP_OUTTAKE:

                this.intakeOuttakeSystem.applyOuttakePower(power, true);
                telemetry.addLine("Reving outtake");
                break;
            case LAUNCH_ARTIFACT:

                this.intakeOuttakeSystem.customExtend(SERVO_LAUNCH_POS);
                telemetry.addLine("Extending Servo");
                break;

            case STOP_OUTTAKE:
                this.intakeOuttakeSystem.applyOuttakePower(0, false);
                break;

            default:
                this.telemetry.addLine("No proper argument given for ScheduleIntakeOuttake");
                this.telemetry.update();
                break;
        }
        telemetry.update();
    }

    @Override
    public void update(){
        switch (operation) {

            case REV_UP_OUTTAKE:
                isFinished = timer.milliseconds() >= MS_TO_REV_UP_OUTTAKE;
                if (isFinished) telemetry.addLine("Outtake revving completed");
                break;

            case LAUNCH_ARTIFACT:
                isFinished = timer.milliseconds() >= MS_IDLE_FOR_SERVO;
                if (isFinished) {
                    intakeOuttakeSystem.close();
                    telemetry.addLine("Servo should be closed");
                }
                break;

            case STOP_OUTTAKE:
                isFinished = !this.intakeOuttakeSystem.isOuttakeBusy();
                break;

            default:
                isFinished = true;
                break;
        }
        telemetry.update();
    }

    @Override
    public boolean isFinished(){ return isFinished; }
}
