package org.firstinspires.ftc.teamcode.newteleop;


import java.util.Map;

public class Scheduler {


    private Map<String, Boolean> schedule;

    private void addEvent(String event){ schedule.put(event, Boolean.FALSE);}
    private boolean getEventState(String event) {return schedule.get(event).booleanValue();}

    public void removeEvent(String event){ schedule.remove(event); }



    //TODO: Build
    public void eventHandler(){}

}
