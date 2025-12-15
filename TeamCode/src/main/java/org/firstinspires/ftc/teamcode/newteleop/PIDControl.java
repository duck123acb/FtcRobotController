package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.robotcore.util.ElapsedTime;


/**
 * PIDControl serves to streamline and standardize PID systems
 * by abstracting the fundamental logical components. It makes
 * it so similar code does not need to be repeated for multiple
 * systems.
 */
public abstract class PIDControl {

    protected double    Kp, Ki, Kd;
    protected double    integral, lastError, lastTime;
    ElapsedTime elapsedTime;

    //----------------------------------------------------------------------------------------------
    //Construction
    //----------------------------------------------------------------------------------------------
    public PIDControl(double Kp, double Ki, double Kd, ElapsedTime elapsedTime) {
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
        this.integral = 0;
        this.lastError = 0;
        this.lastTime = elapsedTime.milliseconds();
        this.elapsedTime = elapsedTime;
    }

    // --- Abstract methods (must be implemented by subclass) --------------------------------------
    protected abstract double getCurrentValue();  // read sensor
    protected abstract void applyOutput(double output); // apply to motor or servo

    // --- Shared PID logic ------------------------------------------------------------------------
    public void update(double target) {
        double current = getCurrentValue();
        double currentTime = elapsedTime.milliseconds();
        double deltaTime = (currentTime - lastTime) / 1000.0;

        if (deltaTime <= 0) deltaTime = 0.01; // Safety; avoid dividing by 0

        double error = target - current;
        integral += error * deltaTime;
        double derivative = (error - lastError) / deltaTime;

        double output = (Kp * error) + (Ki * integral) + (Kd * derivative);

        // Apply the control signal
        applyOutput(output);

        // Update history
        lastError = error;
        lastTime = currentTime;
    }

    public final void reset() {
        integral = 0;
        lastError = 0;
    }


}

