package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.hardware.DcMotor;

import com.qualcomm.robotcore.hardware.Servo;

public class IntakeOuttakeSystem {

    private final DcMotor intakeMotor, outtakeMotor;
    public final Servo launchServo;

    private static final double CUSTOM_SERVO_EXTEND = 0.55;
    private static boolean samePress;
    private static boolean isExtended;

    public IntakeOuttakeSystem(DcMotor intakeMotor, DcMotor outtakeMotor, Servo launchServo ){

        this.intakeMotor = intakeMotor;
        this.outtakeMotor = outtakeMotor;
        this.launchServo = launchServo;
        this.intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.outtakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.outtakeMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    public void fullExtend(){ launchServo.setPosition(1.0);}
    public void customExtend(){ launchServo.setPosition(CUSTOM_SERVO_EXTEND);}
    public void close(){ launchServo.setPosition(0.0);}

    public void applyOuttakePower(double power, boolean isHeld) { outtakeMotor.setPower(isHeld ? power: 0); }

    public void applyIntakePower(double power, boolean isHeld){ intakeMotor.setPower(isHeld ? power: 0); }

    public void reverseOuttake(){
        outtakeMotor.setPower(-1.0);
    }

   public void setServoState(boolean isPressed){

        //samePress prevents Servo volatility; if isPressed were continually true, then every cycle
        // the Servo would switch
        if (!samePress && isPressed){

            if (isExtended) close();
            else customExtend();

            // Inversion to swap operations on next input.
            isExtended = !isExtended;
        }
        // Ensures the above logic only occurs on rising edge of isPressed.
        samePress = isPressed;
   }


}
