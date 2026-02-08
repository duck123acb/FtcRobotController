package org.firstinspires.ftc.teamcode.newteleop.eventschedule;

import java.util.ArrayList;

public class Scheduler{

    private final ArrayList<ScheduledEvent> schedule = new ArrayList<>();

    /*
    * execution flag, prevents commands from being prematurely whilst they are still being loaded
    * into schedule
     */
    private boolean execute = false;
    public void addEvent(ScheduledEvent event){ schedule.add(event); }

    public void update(){

        if (schedule.isEmpty()) execute = false;

        if (execute){
            ScheduledEvent e = schedule.get(0);
            e.start();
            e.update();

            if (e.isFinished())  schedule.remove(0);
        }
    }

    public void allowExecution(){
            execute = true;
        }

    public void stopExecution(){
        execute = false;
    }

    public ArrayList<ScheduledEvent> getSchedule(){ return this.schedule; }
}
