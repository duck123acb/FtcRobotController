package org.firstinspires.ftc.teamcode.newteleop.eventschedule;


/**Super class for scheduling events, like motors, servos, etc.
 * Any process to be scheduled through Scheduler must implement this class.
 */

abstract public class ScheduledEvent {
    protected final String name;
    protected final String caller;

    protected ScheduledEvent(String name, String caller){

        this.name = name;
        this.caller = caller;
    }

    public String getName() { return name;}
    public String getCaller() { return caller; }

    abstract public void start();
    abstract public void update();
    abstract public boolean isFinished();
}
