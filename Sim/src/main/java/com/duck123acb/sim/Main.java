package com.duck123acb.sim;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        UdpClient client = new UdpClient();

        UdpClient.RobotState robot = new UdpClient.RobotState(0, 0, 0);

        List<UdpClient.Ball> balls = Arrays.asList(
                new UdpClient.Ball(1, 10, 5),
                new UdpClient.Ball(2, -4, 7)
        );

        UdpClient.Basket basket = new UdpClient.Basket(20, -10);

        client.sendConfig(robot, balls, basket);

        robot.x += 5;
        robot.heading = 45;
        client.sendUpdate(robot);

        client.close();
    }
}