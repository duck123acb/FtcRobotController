package com.duck123acb.sim;

import com.duck123acb.robotcore.Motor;
import com.duck123acb.robotcore.Robot;
import com.duck123acb.robotcore.RobotState;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        UdpClient client = new UdpClient();

        // -------------------------------------------------------------
        // ROBOT INITIAL STATE
        // -------------------------------------------------------------
        UdpClient.RobotState udpRobot = new UdpClient.RobotState(0, 0, 0);


        // drivetrain motors
        Motor fl = new FakeMotor();
        Motor fr = new FakeMotor();
        Motor bl = new FakeMotor();
        Motor br = new FakeMotor();
        // intake/outtake
        Motor il = new FakeMotor();
        Motor ir = new FakeMotor();
        Motor ol = new FakeMotor();
        Motor or = new FakeMotor();

        // create robot
        Robot robot = new Robot(fl, fr, bl, br, il, ir, ol, or);


        // -------------------------------------------------------------
        // BALL ORDER (as would come from AprilTags)
        // Example: ball IDs in the order you must collect them
        // -------------------------------------------------------------
        char[][] motifs = {
            {'g', 'p', 'p'},
            {'p', 'g', 'p'},
            {'p', 'p', 'g'}
        };

        Random rand = new Random();
        char[] chosen = motifs[rand.nextInt(motifs.length)];


        List<UdpClient.Ball> balls = Arrays.asList(
            /*
            Ball	X (inches)	Y (inches)	Row/Column
            1	-12	30	Top-left
            2	0	30	Top-center
            3	12	30	Top-right
            4	-12	18	Middle-left
            5	0	18	Middle-center
            6	12	18	Middle-right
            7	-12	6	Bottom-left
            8	0	6	Bottom-center
            9	12	6	Bottom-right

             */
            new UdpClient.Ball(1,  -12, 30, 'g'),
            new UdpClient.Ball(2,   0, 30, 'p'),
            new UdpClient.Ball(3,  12, 30, 'g'),
            new UdpClient.Ball(4, -12, 18, 'p'),
            new UdpClient.Ball(5,  0, 18, 'g'),
            new UdpClient.Ball(6,  12, 18, 'p'),
            new UdpClient.Ball(7, -12, 6, 'g'),
            new UdpClient.Ball(8,  0, 6, 'p'),
            new UdpClient.Ball(9,  12, 6, 'g')
        );

        // -------------------------------------------------------------
        // BASKET POSITION
        // -------------------------------------------------------------
        UdpClient.Basket[] baskets = {
            new UdpClient.Basket(-65, 65, 'r', 0),
            new UdpClient.Basket(65, 65, 'b', 0)
        };

        // -------------------------------------------------------------
        // SEND CONFIG TO VISUALIZER
        // -------------------------------------------------------------
        client.sendConfig(udpRobot, balls, baskets);

        // target: first ball
        UdpClient.Ball firstBall = balls.get(0);

        // simple sim loop
        while (true) {
            // move robot toward target
            robot.goToXY_PID(firstBall.x, firstBall.y, 0);

            // copy internal robot state to UDP visualizer
            RobotState internal = robot.getState();
            udpRobot.x = internal.x;
            udpRobot.y = internal.y;
            udpRobot.heading = internal.heading;

            // send update to visualizer
            client.sendUpdate(udpRobot);

            double dx = firstBall.x - internal.x;
            double dy = firstBall.y - internal.y;
            if (Math.hypot(dx, dy) < 0.5) break;

            Thread.sleep(20); // ~50Hz update
        }

        client.close();
    }
}
