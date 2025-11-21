package com.duck123acb.sim;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * UDP client to send simulation data from Java to a visualization frontend.
 * Supports sending robot state, balls, and basket data over UDP as JSON.
 */
public class UdpClient {

    /** Default port for visualization WebSocket/UDP receiver. */
    private static final int VIS_PORT = 41234;

    /** Host for visualization frontend (usually localhost). */
    private static final String VIS_HOST = "127.0.0.1";

    /** Gson instance for JSON serialization. */
    private final Gson gson = new Gson();

    /** UDP socket used to send packets. */
    private final DatagramSocket socket;

    /**
     * Construct a new UDP client and initialize the socket.
     *
     * @throws Exception if socket creation fails
     */
    public UdpClient() throws Exception {
        socket = new DatagramSocket();
        System.out.println("UDP client ready. Sending to " + VIS_HOST + ":" + VIS_PORT);
    }

    // ---------------------------------------------------------------------
    // CORE SEND
    // ---------------------------------------------------------------------

    /**
     * Serialize an object as JSON and send via UDP to the visualization server.
     *
     * @param payload the object to serialize and send
     */
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

    /**
     * Send the initial full configuration to the frontend.
     * Includes robot state, all balls, and all baskets.
     *
     * @param robot  current robot state
     * @param balls  list of balls on the field
     * @param baskets array of baskets on the field
     */
    public void sendConfig(RobotState robot, List<Ball> balls, Basket[] baskets) {
        Map<String, Object> config = new HashMap<>();
        config.put("type", "config");
        config.put("initialState", robot);
        config.put("balls", balls);
        config.put("baskets", baskets);

        send(config);
    }

    /**
     * Send an updated robot state.
     *
     * @param robot current robot state
     */
    public void sendUpdate(RobotState robot) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "update");
        update.put("x", robot.x);
        update.put("y", robot.y);
        update.put("heading", robot.heading);

        send(update);
    }

    /**
     * Send only the current list of balls.
     *
     * @param balls list of balls to send
     */
    public void sendBalls(List<Ball> balls) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "balls");
        msg.put("balls", balls);
        send(msg);
    }

    /**
     * Send only a single basket update.
     *
     * @param basket the basket to send
     */
    public void sendBasket(Basket basket) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "basket");
        msg.put("basket", basket);
        send(msg);
    }

    /**
     * Close the UDP socket cleanly.
     */
    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    // ---------------------------------------------------------------------
    // DATA CLASSES
    // ---------------------------------------------------------------------

    /**
     * Represents the robot's position and heading on the field.
     */
    public static class RobotState {
        @SerializedName("x") public double x;
        @SerializedName("y") public double y;
        @SerializedName("heading") public double heading;

        /**
         * @param x robot X coordinate
         * @param y robot Y coordinate
         * @param heading robot heading in degrees
         */
        public RobotState(double x, double y, double heading) {
            this.x = x;
            this.y = y;
            this.heading = heading;
        }
    }

    /**
     * Represents a ball on the field.
     */
    public static class Ball {
        public int id;
        public double x, y;
        public char colour; // 'g' or 'p'

        /**
         * @param id unique ball ID
         * @param x X coordinate
         * @param y Y coordinate
         * @param colour color character ('g' or 'p')
         */
        public Ball(int id, double x, double y, char colour) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.colour = colour;
        }
    }

    /**
     * Represents a basket on the field.
     */
    public static class Basket {
        public double x, y;
        public char colour;   // 'r' or 'b'
        public double ballCount; // number of balls currently in basket

        /**
         * @param x X coordinate
         * @param y Y coordinate
         * @param colour color character ('r' or 'b')
         * @param ballCount number of balls in this basket
         */
        public Basket(double x, double y, char colour, int ballCount) {
            this.x = x;
            this.y = y;
            this.colour = colour;
            this.ballCount = ballCount;
        }
    }
}