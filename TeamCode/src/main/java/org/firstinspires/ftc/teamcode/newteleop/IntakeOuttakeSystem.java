package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.hardware.DcMotor;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

public class IntakeOuttakeSystem {

    private final DcMotor intakeMotor, outtakeMotor;
    private final Servo launchServo;
    private static final double CUSTOM_SERVO_EXTEND = 0.8;
    private boolean samePress;
    private boolean isExtended;

    public IntakeOuttakeSystem(final DcMotor intakeMotor, final DcMotor outtakeMotor, final Servo launchServo ){

        this.intakeMotor = intakeMotor;
        this.outtakeMotor = outtakeMotor;
        this.launchServo = launchServo;
        this.intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.outtakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.outtakeMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        samePress = false;
        isExtended = false;
        this.close();
    }

    public void fullExtend(){
        this.launchServo.setPosition(1.0);}
    public void customExtend(final double customExtend){
        this.launchServo.setPosition(customExtend);}
    public final void close(){
        this.launchServo.setPosition(0.0);}

    public void applyOuttakePower(final double power, final boolean isHeld) {
        this.outtakeMotor.setPower(isHeld ? power: 0); }

    public void applyIntakePower(final double power, final boolean isHeld){
        this.intakeMotor.setPower(isHeld ? power: 0); }

    public void reverseOuttake(){
        this.outtakeMotor.setPower(-1.0);
    }

    public boolean isOuttakeBusy() { return this.outtakeMotor.isBusy(); }
   public void setServoState(final boolean isPressed){

        //samePress prevents Servo volatility; if isPressed were continually true, then every cycle
        // the Servo would switch
        if (!this.samePress && isPressed){

            if (this.isExtended) this.close();
            else this.customExtend(IntakeOuttakeSystem.CUSTOM_SERVO_EXTEND);

            // Inversion to swap operations on next input.
            this.isExtended = !this.isExtended;
            return;
        }
        // Ensures the above logic only occurs on rising edge of isPressed.
       this.samePress = isPressed;
   }
}
