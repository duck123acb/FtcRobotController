package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;


public class Spindexer extends PIDControl{

    private DcMotor spinMotor;

    private static final int MOTOR_MAX_RPM = 384; //FIXME: Look at the RPM on the motor
    private static final double TICKS_PER_ROTATION = 1.0; //FIXME: Test, this is simply a guess
    private static boolean isTargetSet = false;
    private double target = 0;

    /**
     *
     * @param spinMotor the motor used for rotating the Spindexer
     * @param Kp empirical correction for Kp
     * @param Ki empirical correction for Ki
     * @param Kd empirical correction for Kd
     * @param timer timer for calculations
     */
    public Spindexer(DcMotor spinMotor,
                     double Kp,
                     double Ki,
                     double Kd,
                     ElapsedTime timer){

        super(Kp, Kd, Ki, timer);

        this.spinMotor = spinMotor;
        this.spinMotor.setDirection(DcMotor.Direction.FORWARD);
        this.spinMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.spinMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.spinMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

   //TODO: Incorporate PID

    @Override
    public double getCurrentValue(){
        if (isMotor()) return spinMotor.getCurrentPosition();
        else return 0.0;
    }

    @Override
    public void applyOutput(double power){ spinMotor.setPower(power); }

    //TODO: Incorporate reverse
    /**
     *
     * @param degrees directional rotation in degrees
     */
    void rotate(double degrees) {

        double current = getCurrentValue();
        double offset = (degrees/360) * TICKS_PER_ROTATION;

        if (!isTargetSet) {

            target = current + offset;
            isTargetSet = true; //ensures target is only set once
        }
        if (target > current) {
            this.update(target);
            return;
        }

        spinMotor.setPower(0);
        isTargetSet = false;
        reset();
    }


    private boolean isMotor(){ return null != spinMotor; }


}
