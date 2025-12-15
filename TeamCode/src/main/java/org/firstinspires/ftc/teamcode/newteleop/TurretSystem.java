package org.firstinspires.ftc.teamcode.newteleop;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

public class TurretSystem extends PIDControl{

    private final DcMotor           turretMotor;
    private final HuskyLensCamera   husky;

    private static final double CENTER_OF_VIEW_X = 160;
    private static final double SLOW_TURN = 0.25;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------
    public TurretSystem(
                double Kp,
                double Ki,
                double Kd,
                ElapsedTime timer,
                HuskyLensCamera husky,
                DcMotor turretMotor
                ) {

        super (Kp, Ki, Kd,timer);
        this.husky = husky;
        this.turretMotor = turretMotor;
        this.turretMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    @Override
    public double getCurrentValue(){ return husky.getAprilTagOffset(); }

    @Override
    public void applyOutput(double output){ turretMotor.setPower(output);}


    public void continousUpdate(){

        if (!husky.isTagRecognition()) return;

        // getAprilTagOffset returns -1 when no tag is detected.
        // turret must spin until it detects a tag.
        if (-1 == getCurrentValue()){
            applyOutput(SLOW_TURN);

            // PID logic reset
            reset();
            return;
        }
        update(CENTER_OF_VIEW_X);
    }
}
