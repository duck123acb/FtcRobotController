package com.duck123acb.sim;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        UdpClient client = new UdpClient();

        // -------------------------------------------------------------
        // ROBOT INITIAL STATE
        // -------------------------------------------------------------
        UdpClient.RobotState robot = new UdpClient.RobotState(
                0,    // x
                0,    // y
                0     // heading deg
        );

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
        client.sendConfig(robot, balls, baskets);

        // -------------------------------------------------------------
        // send an update to prove it's working
        // -------------------------------------------------------------
        robot.x = 5;
        robot.heading = 45;
        client.sendUpdate(robot);

        client.close();
    }
}
