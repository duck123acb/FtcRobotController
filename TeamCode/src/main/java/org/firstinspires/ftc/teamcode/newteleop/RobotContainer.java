package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.newteleop.eventschedule.ScheduleIntakeOuttake;
import org.firstinspires.ftc.teamcode.newteleop.eventschedule.ScheduleSpindexer;
import org.firstinspires.ftc.teamcode.newteleop.eventschedule.ScheduledEvent;
import org.firstinspires.ftc.teamcode.newteleop.eventschedule.Scheduler;

import java.util.ArrayList;


public class RobotContainer {

    private final IntakeOuttakeSystem   intakeOuttake;
    private final TurretSystem          turret;
    private final Spindexer             spindexer;
    private final HuskyLensCamera       husky = new HuskyLensCamera();
    private ElapsedTime                 timer = new ElapsedTime();
    private  final Telemetry telemetry;

    private Scheduler scheduler = new Scheduler();


    //TODO: Tune Kp, Kd, Ki
    private static final double     tKp = 1.0;
    private static final double     tKd = 1.0;
    private static final double     tKi = 1.0;
    private static final double    sKp = 1.0;
    private static final double     sKd = 1.0;
    private static final double sKi = 1.0;


    private static final double STANDARD_INTAKE_POWER = 0.5;
    private static double standardOuttakePower = 1.0; // 1 by default, will be calculable later
    private static final double RAMP_LAUNCH_ANGLE_DEGREES = 70.0; //FIXME: Measure, this is simply a guess
    private static final double GECKO_WHEEL_DIAMETER_METERS = 0.096; //FIXME: Measure, this is simply a guess
    private static final int MOTOR_MAX_RPM = 6000;
    private static final int GEAR_RATIO = 1;
    private static final double LAUNCH_EFFICIENCY = 0.7;
    private static final double SCORE_HEIGHT_INCHES = 25.5;
    private static final double INCHES_TO_METERS = 0.254;
    private static final int HUSKY_HEIGHT_PIXELS = 240;
    private static final int HUSKY_WIDTH_PIXELS = 320;
    private static final double APRIL_TAG_DIMENSIONS_INCHES = 6.5;
    private static final int HUSKY_FOV_DEGREES = 160;

    private boolean typicalOuttakeLoaded = false;
    //----------------------------------------------------------------------------------------------
    //Construction
    //----------------------------------------------------------------------------------------------



    private RobotContainer(HardwareMap hwMap, Telemetry telemetry) {

        husky.init(hwMap);

        intakeOuttake = new IntakeOuttakeSystem(
                hwMap.get(DcMotor.class, "intakeMotor"),
                hwMap.get(DcMotor.class, "outtakeMotor"),
                hwMap.get(Servo.class, "launchServo")
        );

        spindexer = new Spindexer(
                hwMap.get(DcMotor.class, "spinMotor"),
                sKp,
                sKi,
                sKd,
                timer
        );

        turret = new TurretSystem(
                tKp,
                tKi,
                tKd,
                timer,
                husky,
                hwMap.get(DcMotor.class, "turretMotor")
        );

        this.telemetry = telemetry;
    }

    /**
     * @param hwMap hardware map from LinearOpMode
     * @return new instance of RobotContainer
     */
    public static RobotContainer createRobotContainer(HardwareMap hwMap, Telemetry telemetry) {
        return new RobotContainer(hwMap, telemetry);
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
    public void selectHuskyMode(HLMode mode){ husky.setHuskyMode(mode);}

    public HLMode getCurrentMode() { return husky.currentMode; }

    // ---------------------------Miscellaneous----------------------------------------------------

    private double calculateDistance(){

        // Standard -1 protocol if no tag is detected
        if (-1 == husky.getTagHeight()) return -1.0;

        double heightPx = husky.getTagHeight();
        double widthPx = husky.getTagWidth();
        double distanceMeters = 0;

        // determine the proportion of the screen occupied by the AprilTag using the total height of
        // the screen in pixels
        final double scrHeightFrac = heightPx / HUSKY_HEIGHT_PIXELS;
        final double scrWidthFrac = widthPx / HUSKY_WIDTH_PIXELS;

        // by knowing the dimensions of the April Tag (6.5 x 6.5 inches) we can roughly determine
        // the real height of the image by comparing the proportion occupied by the screen in pixels
        // to what it should be in inches.
        double scrHeightInches = APRIL_TAG_DIMENSIONS_INCHES / scrHeightFrac;
        double scrWidthInches = APRIL_TAG_DIMENSIONS_INCHES / scrWidthFrac;

        // we can then imagine the distance as a line that bisects the FOV of the husky lens, which
        // in turn perpendicularly bisects screenHeightInches.
        double halfFOVRadians = Math.toRadians(HUSKY_FOV_DEGREES / 2);

        // Now, we can observe that tan(halfFOVRadians) = ( 0.5 * screenHeightInches) / distance;
        // we can then rearrange to find distance.
        double hDistanceInches = (scrHeightInches / 2) / Math.tan(halfFOVRadians);
        double wDistanceInches = (scrWidthInches / 2) / Math.tan(halfFOVRadians);

        if (isRelative(hDistanceInches,wDistanceInches)) {
            distanceMeters = hDistanceInches / INCHES_TO_METERS;
            return distanceMeters;
        }

        //Convert to meters, more useful

        return  distanceMeters;
    }

    private boolean isRelative(double first, double second ){
        return Math.abs((first - second) )/ 2 < 1;
    }

    /**Presumptions for this distance power application
     * Weight of the ball is negligible
     * No slip; Ball leaves outtake with tangential speed equal to wheel rim  speed
     * Air resistance is negligible
     */


    //TODO: Test for accuracy
    public final void setStandardOuttakePower() {

        final double distance = calculateDistance();

        if (0 <= distance) {
            standardOuttakePower = 1.0;
            return;
        }

        final double g = 9.81;
        final double thetaRad = Math.toRadians(RAMP_LAUNCH_ANGLE_DEGREES);

        final double heightMeters = SCORE_HEIGHT_INCHES * INCHES_TO_METERS;

        final double tan = Math.tan(thetaRad);
        final double cos = Math.cos(thetaRad);

        double denom = 2 * Math.pow(cos, 2) * (distance * tan - heightMeters);

        // Invalid trajectory
        if (0 >= denom) {
            standardOuttakePower = 1.0;
            return;
        }

        double velocitySquared = (g * distance * distance) / denom;
        double velocity = Math.sqrt(velocitySquared);

        final double wheelCircumference = Math.PI * GECKO_WHEEL_DIAMETER_METERS;

        // Convert linear velocity to wheel RPM
        double wheelRPS = velocity / wheelCircumference;
        double wheelRPM = wheelRPS * 60.0;

        // Account for gearing + losses
        double effectiveMaxRPM = MOTOR_MAX_RPM / GEAR_RATIO;

        double theoreticalPower = wheelRPM / effectiveMaxRPM;

        // Empirical correction
        theoreticalPower *= LAUNCH_EFFICIENCY;

        standardOuttakePower = Range.clip(theoreticalPower, 0.0, 1.0);
    }

    //---------------------------------------------------------------------------------------------
    // Schedule
    //---------------------------------------------------------------------------------------------

    public void scheduleIOEvent(
            double power,
            ScheduleIntakeOuttake.Operations operation,
            Telemetry telemetry
    ) {
        scheduler.addEvent( new ScheduleIntakeOuttake(
                "RobotContainer",
                intakeOuttake,
                power,
                operation,
                telemetry
                )
        );
    }

    public void scheduleSpindexerEvent(
            double turnToPos
    ) {
        scheduler.addEvent(
                new ScheduleSpindexer(
                     "RobotContainer",
                        turnToPos,
                        this.spindexer
                )
        );
    }

    public final void toggleScheduledEventExecution(){ scheduler.allowExecution();}

    public final ArrayList<ScheduledEvent> getSchedule(){ return scheduler.getSchedule(); }

    public void loadTypicalSchedule(){

        final double firstTurn = getCurrentSpindexerPos() + 120;
        final double secondTurn = firstTurn + 120;

        this.scheduleIOEvent(
                standardOuttakePower,
                ScheduleIntakeOuttake.Operations.REV_UP_OUTTAKE,
                this.telemetry
        );

        this.scheduleIOEvent(
                0,
                ScheduleIntakeOuttake.Operations.LAUNCH_ARTIFACT,
                telemetry
        );

        this.scheduleSpindexerEvent( firstTurn );

        this.scheduleIOEvent(
                0,
                ScheduleIntakeOuttake.Operations.LAUNCH_ARTIFACT,
                telemetry
        );

        this.scheduleSpindexerEvent( secondTurn);

        this.scheduleIOEvent(
                0,
                ScheduleIntakeOuttake.Operations.LAUNCH_ARTIFACT,
                telemetry
        );

        scheduleIOEvent(
                0,
                ScheduleIntakeOuttake.Operations.STOP_OUTTAKE,
                telemetry
        );

        this.typicalOuttakeLoaded = true;
    }

    public boolean isScheduleLoaded() { return typicalOuttakeLoaded; }
    public void allowExecution() { scheduler.allowExecution(); }

    public void stopExecution() {scheduler.stopExecution();}
    //---------------------------------------------------------------------------------------------
    // Spindexer
    //---------------------------------------------------------------------------------------------

    public void rotate(double degrees) { spindexer.rotate(degrees); }
    public double getCurrentSpindexerPos(){ return spindexer.getCurrentValue(); }

    public boolean isSpindexerBusy() { return spindexer.isBusy(); }
    //----------------------------------------------------------------------------------------------
    // State checkers
    //----------------------------------------------------------------------------------------------

    public boolean isIOInit() {return null != intakeOuttake; };
    public boolean isTurretInit(){ return null != turret; };
    public boolean isHuskyInit(){ return null != husky; };

}
