package com.duck123acb.robotcore;

public interface Motor {
    void setPower(double power);
    double getPower();

    void setTargetPosition(int position);
    int getTargetPosition();

    void setMode(RunMode mode);
    RunMode getMode();

    enum RunMode {
        RUN_WITHOUT_ENCODER,
        RUN_TO_POSITION,
        STOP_AND_RESET_ENCODER
    }
}
