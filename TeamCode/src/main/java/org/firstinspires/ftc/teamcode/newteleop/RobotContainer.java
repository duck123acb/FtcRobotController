package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;


public class RobotContainer {

    public final IntakeOuttakeSystem    intakeOuttake;
    public final TurretSystem turret;
    public final HuskyLensCamera        husky = new HuskyLensCamera();
    private ElapsedTime                 timer = new ElapsedTime();

    //TODO: Tune Kp, Kd, Ki
    private final double    Kp = 1.0;
    private final double    Kd = 1.0;
    private final double    Ki = 1.0;

    private static final double STANDARD_INTAKE_POWER = 0.5;
    private static double standardOuttakePower = 1.0; // 1 by default, will be calculable later
    private static final double RAMP_LAUNCH_ANGLE_DEGREES = 40.0; //FIXME: Measure, this is simply a guess
    private static final double FLYWHEEL_DIAMETER_CENTIMETERS = 15.0; //FIXME: Measure, this is simply a guess

    private static final int HUSKY_HEIGHT_PIXELS = 240;
    private static final int HUSKY_WIDTH_PIXELS = 320;
    private static final double APRIL_TAG_DIMENSIONS_INCHES = 6.5;
    private static final int HUSKY_FOV_DEGREES = 160;


    //----------------------------------------------------------------------------------------------
    //Construction
    //----------------------------------------------------------------------------------------------

    public RobotContainer(HardwareMap hwMap){

        husky.init(hwMap);

        intakeOuttake = new IntakeOuttakeSystem(
                hwMap.get(DcMotor.class, "intakeMotor"),
                hwMap.get(DcMotor.class, "outtakeMotor"),
                hwMap.get(Servo.class, "launchServo")
        );

        turret = new TurretSystem(
                Kp,
                Ki,
                Kd,
                timer,
                husky,
                hwMap.get(DcMotor.class, "turretMotor")
        );
    }

    //----------------------------------------------------------------------------------------------
    // IntakeOuttakeSystem wrappers
    //----------------------------------------------------------------------------------------------
    public void useIntake(boolean gamepadInput){
        intakeOuttake.applyIntakePower(STANDARD_INTAKE_POWER, gamepadInput);
    }
    public void useOuttake(boolean gamepadInput) {
        intakeOuttake.applyOuttakePower(standardOuttakePower, gamepadInput);
    }
    public void setServoState(boolean isPressed){ intakeOuttake.setServoState(isPressed); }

    //----------------------------------------------------------------------------------------------
    //TurretSystem wrappers
    //----------------------------------------------------------------------------------------------
    public void updateTurret() {turret.continousUpdate();}

    //----------------------------------------------------------------------------------------------
    // HuskyLensCamera wrappers
    //----------------------------------------------------------------------------------------------
    public final boolean checkForTagRecognition(){ return husky.isTagRecognition();}
    public void selectHuskyMode(int mode){ husky.setModeUsingIndex(mode);}

    // ---------------------------Miscellaneous----------------------------------------------------

    public double calculateDistance(){

        // Standard -1 protocol if no tag is detected
        if (-1 == husky.getTagHeight()) {return -1.0;}

        double heightPx = husky.getTagHeight();

        // determine the proportion of the screen occupied by the AprilTag using the total height of
        // the screen in pixels
        double fractionOfScreenHeight = heightPx / HUSKY_HEIGHT_PIXELS;

        // by knowing the dimensions of the April Tag (6.5 x 6.5 inches) we can roughly determine
        // the real height of the image by comparing the proportion occupied by the screen in pixels
        // to what it should be in inches.
        double screenWidthInches = APRIL_TAG_DIMENSIONS_INCHES / fractionOfScreenHeight;

        // we can then imagine the distance as a line that bisects the FOV of the husky lens, which
        // in turn perpendicularly bisects screenWidthInches.
        double halfFOVRadians = Math.toRadians(HUSKY_FOV_DEGREES / 2);

        // Now, we can observe that tan(halfFOVRadians) = ( 0.5 * screenWidthInches) / distance;
        // we can then rearrange to find distance.
        double distance = (screenWidthInches / 2) / Math.tan(halfFOVRadians);

        return  distance;
    }
    /*
    //TODO: Determine logic for outtake power
    public void setStandardOuttakePower(){

        double distance = calculateDistance();

        if (-1.0 == distance) {
            standardOuttakePower = 1.0
            return;
        }



    }


     */
}
