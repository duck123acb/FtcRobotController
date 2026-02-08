package org.firstinspires.ftc.teamcode.newteleop.eventschedule;

import org.firstinspires.ftc.teamcode.newteleop.Spindexer;

public class ScheduleSpindexer extends ScheduledEvent{

    private final double turnToPos;
    private final double currentPos;
    private final Spindexer spindexer;
    private boolean isFinished = false;


    public ScheduleSpindexer(String caller,
                             double turnToPos,
                             Spindexer spindexer){
        super ("spindexer", caller);

        this.spindexer = spindexer;
        this.turnToPos = turnToPos;
        this.currentPos = spindexer.getCurrentValue();

    }


    @Override
    public void start(){

        spindexer.reset();
        spindexer.rotate(turnToPos);
    }

    @Override
    public void update(){
        isFinished = !spindexer.isBusy();
    }

    public boolean isFinished(){ return isFinished; }
}
