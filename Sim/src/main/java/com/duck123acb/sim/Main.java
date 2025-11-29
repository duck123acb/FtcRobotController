package com.duck123acb.sim;

import com.duck123acb.robotcore.Motor;
import com.duck123acb.robotcore.Robot;
import com.duck123acb.robotcore.RobotState;

import java.util.ArrayList;
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
        // STARTING POSITION
        double startX = 0;
        double startY = -60;
        double startHeading = Math.toRadians(45);
        
        Robot robot = new Robot(fl, fr, bl, br, il, ir, ol, or, startX, startY, startHeading);

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


        List<UdpClient.Ball> balls = new ArrayList<>(Arrays.asList(
            new UdpClient.Ball(1, -12, 6,  'g'),
            new UdpClient.Ball(2,   0,  6,  'p'),
            new UdpClient.Ball(3,  12,  6,  'g'),

            new UdpClient.Ball(4, -12, 18, 'p'),
            new UdpClient.Ball(5,   0, 18, 'g'),
            new UdpClient.Ball(6,  12, 18, 'p'),

            new UdpClient.Ball(7, -12, 30, 'g'),
            new UdpClient.Ball(8,   0, 30, 'p'),
            new UdpClient.Ball(9,  12, 30, 'g')
        ));


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

        // simple sim loop
//        while (true) {




        while (!balls.isEmpty()) {
            UdpClient.Ball target = balls.get(0);
            robot.resetPID();

            // Calculate heading to face the target ball
            RobotState current = robot.getState();
            double dx = target.x - current.x;
            double dy = target.y - current.y;
            double targetHeading = Math.atan2(dy, dx);
            
            // Turn to face target
            while (true) {
                robot.turnPID(targetHeading);

                RobotState internal = robot.getState();
                udpRobot.x = internal.x;
                udpRobot.y = internal.y;
                udpRobot.heading = internal.heading;
                client.sendUpdate(udpRobot);

                double diff = targetHeading - internal.heading;
                while (diff > Math.PI) diff -= 2 * Math.PI;
                while (diff < -Math.PI) diff += 2 * Math.PI;

                if (Math.abs(diff) < 0.05) break; // ~3 degrees tolerance

                Thread.sleep(20);
            }

            // Move to target
            while (true) {
                robot.goToXY_PID(target.x, target.y, targetHeading, 30);

                RobotState internal = robot.getState();
                udpRobot.x = internal.x;
                udpRobot.y = internal.y;
                udpRobot.heading = internal.heading;

                client.sendUpdate(udpRobot);

                double distDx = target.x - internal.x;
                double distDy = target.y - internal.y;

                if (Math.hypot(distDx, distDy) < 0.5) break;

                Thread.sleep(20);
            }

            balls.remove(0);
            client.sendBalls(balls);
        }
        client.close();
    }
}
