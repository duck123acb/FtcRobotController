package com.duck123acb.sim;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class UdpClient {

    private static final int VIS_PORT = 41234;
    private static final String VIS_HOST = "127.0.0.1";

    private final Gson gson = new Gson();
    private DatagramSocket socket;

    public UdpClient() throws Exception {
        socket = new DatagramSocket();
        System.out.println("UDP client ready. Sending to " + VIS_HOST + ":" + VIS_PORT);
    }

    // ---------------------------------------------------------------------
    // CORE SEND
    // ---------------------------------------------------------------------
    private void send(Object payload) {
        try {
            String json = gson.toJson(payload);
            byte[] data = json.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    InetAddress.getByName(VIS_HOST),
                    VIS_PORT
            );
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------
    // PUBLIC SEND FUNCTIONS
    // ---------------------------------------------------------------------

    /** Send initial full config (robot, balls, basket). */
    public void sendConfig(RobotState robot, List<Ball> balls, Basket basket) {
        Map<String, Object> config = new HashMap<>();
        config.put("type", "config");
        config.put("initialState", robot);
        config.put("balls", balls);
        config.put("basket", basket);

        send(config);
    }

    /** Send a robot state update. */
    public void sendUpdate(RobotState robot) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "update");
        update.put("x", robot.x);
        update.put("y", robot.y);
        update.put("heading", robot.heading);

        send(update);
    }

    /** Send only balls list if you ever want updates. */
    public void sendBalls(List<Ball> balls) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "balls");
        msg.put("balls", balls);
        send(msg);
    }

    /** Send only basket. */
    public void sendBasket(Basket basket) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "basket");
        msg.put("basket", basket);
        send(msg);
    }

    /** Clean shutdown. */
    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    // ---------------------------------------------------------------------
    // DATA CLASSES
    // ---------------------------------------------------------------------
    public static class RobotState {
        @SerializedName("x") public double x;
        @SerializedName("y") public double y;
        @SerializedName("heading") public double heading;

        public RobotState(double x, double y, double heading) {
            this.x = x;
            this.y = y;
            this.heading = heading;
        }
    }

    public static class Ball {
        public int id;
        public double x, y;

        public Ball(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    public static class Basket {
        public double x, y;

        public Basket(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
