package com.duck123acb.sim;

import java.util.Arrays;
import java.util.List;

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
        List<Integer> ballOrder = Arrays.asList(
                3, 1, 4, 2   // customize this
        );

        // -------------------------------------------------------------
        // BALL POSITIONS
        // (IDs must match the ones in ballOrder)
        // -------------------------------------------------------------
        List<UdpClient.Ball> balls = Arrays.asList(
                new UdpClient.Ball(1,  15,  20),
                new UdpClient.Ball(2, -10,  18),
                new UdpClient.Ball(3,  22, -12),
                new UdpClient.Ball(4,  -6, -15)
        );

        // -------------------------------------------------------------
        // BASKET POSITION
        // -------------------------------------------------------------
        UdpClient.Basket basket = new UdpClient.Basket(
                30, // x
                0   // y
        );

        // -------------------------------------------------------------
        // SEND CONFIG TO VISUALIZER
        // -------------------------------------------------------------
        client.sendConfig(robot, balls, basket);


        // -------------------------------------------------------------
        // OPTIONALLY: send an update to prove it's working
        // -------------------------------------------------------------
        robot.x = 5;
        robot.heading = 45;
        client.sendUpdate(robot);

        client.close();
    }
}
