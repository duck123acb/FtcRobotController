package com.duck123acb.sim;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Test {
    private static final int VIS_PORT = 41234;
    private static final String VIS_HOST = "127.0.0.1";

    private static final Gson gson = new Gson();

    private static DatagramSocket socket;

    private static RobotState robotState = new RobotState();

    public static void main(String[] args) throws Exception {
        socket = new DatagramSocket();

        System.out.println("Test harness will send UDP packets to " + VIS_HOST + ":" + VIS_PORT);

        sendInitialConfig();

        String motionType = args.length > 0 ? args[0] : null;
        List<String> valid = Arrays.asList("random", "tank", "homing");

        Runnable motionFunction;

        if (motionType == null || !valid.contains(motionType)) {
            if (motionType != null) {
                System.out.println("Argument '" + motionType + "' not understood.");
            }
            System.out.println("Usage: java test [random|tank|homing]");
            System.out.println("Falling back to default mode: random");
            motionFunction = Test::startRandomMovements;
        } else {
            System.out.println("Starting motion type: " + motionType);
            switch (motionType) {
                case "tank":
                    motionFunction = Test::startTankMovements;
                    break;
                case "homing":
                    motionFunction = Test::startHomingMovements;
                    break;
                default:
                    motionFunction = Test::startRandomMovements;
            }
        }

        Thread.sleep(500);
        motionFunction.run();
    }

    // ---------------------------------------------------------------------
    // UDP SEND
    // ---------------------------------------------------------------------

    private static void send(String message) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName(VIS_HOST),
                    VIS_PORT
            );
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------
    // INITIAL CONFIG
    // ---------------------------------------------------------------------

    private static void sendInitialConfig() {
        System.out.println("Sending randomized initial configuration...");

        Random r = new Random();
        int fieldBounds = 70;

        int numBalls = 2 + r.nextInt(4);
        List<Ball> balls = new ArrayList<>();

        for (int i = 1; i <= numBalls; i++) {
            balls.add(new Ball(
                    i,
                    (r.nextDouble() - 0.5) * 2 * fieldBounds,
                    (r.nextDouble() - 0.5) * 2 * fieldBounds
            ));
        }

        Basket basket = new Basket(
                (r.nextDouble() - 0.5) * 2 * fieldBounds,
                (r.nextDouble() - 0.5) * 2 * fieldBounds
        );

        Map<String, Object> config = new HashMap<>();
        config.put("type", "config");
        config.put("initialState", new RobotState(0, 0, 0));
        config.put("balls", balls);
        config.put("basket", basket);

        send(gson.toJson(config));
    }

    // ---------------------------------------------------------------------
    // MOTION MODES
    // ---------------------------------------------------------------------

    private static void startRandomMovements() {
        Random r = new Random();
        System.out.println("Starting random robot movements...");

        while (true) {
            double action = r.nextDouble();
            String moveType;

            if (action < 0.3) {
                moveType = "Turn";
                robotState.heading += (r.nextDouble() - 0.5) * 30;
            } else if (action < 0.9) {
                moveType = "Forward";
                double distance = 1 + r.nextDouble() * 4;
                double rad = Math.toRadians(-robotState.heading + 90);
                robotState.x += distance * Math.cos(rad);
                robotState.y += distance * Math.sin(rad);
            } else {
                moveType = "Strafe";
                double distance = 1 + r.nextDouble() * 4;
                if (r.nextBoolean()) distance *= -1;
                double rad = Math.toRadians(-robotState.heading);
                robotState.x += distance * Math.cos(rad);
                robotState.y += distance * Math.sin(rad);
            }

            sendUpdate(moveType);

            sleep(1000);
        }
    }

    private static void startTankMovements() {
        Random r = new Random();
        System.out.println("Starting tank-style robot movements...");

        while (true) {
            double turn = (r.nextDouble() - 0.5) * 46;
            robotState.heading += turn;

            double dist = 1 + (r.nextDouble() * 2);
            double rad = Math.toRadians(-robotState.heading + 90);
            robotState.x += dist * Math.cos(rad);
            robotState.y += dist * Math.sin(rad);

            sendUpdate("Tank");

            sleep(100);
        }
    }

    private static void startHomingMovements() {
        Random r = new Random();
        System.out.println("Starting homing-style robot movements...");

        while (true) {
            double x = robotState.x;
            double y = robotState.y;
            double heading = robotState.heading;

            double dist = Math.sqrt(x * x + y * y);

            double turn;

            if (dist > 72) {
                // Angle toward center
                double angleToCenterRad = Math.atan2(-y, -x);
                double homingAngle = Math.toDegrees(angleToCenterRad);
                homingAngle = (homingAngle + 360) % 360;

                double homingHeading = (-homingAngle + 90 + 360) % 360;

                double turnError = homingHeading - heading;
                if (turnError > 180) turnError -= 360;
                if (turnError < -180) turnError += 360;

                double turnBias = Math.signum(turnError) * 15;
                double randomTurn = (r.nextDouble() - 0.5) * 20;
                turn = turnBias + randomTurn;
            } else {
                turn = (r.nextDouble() - 0.5) * 46;
            }

            robotState.heading += turn;

            double distMove = 1 + r.nextDouble() * 2;
            double rad = Math.toRadians(-robotState.heading + 90);
            robotState.x += distMove * Math.cos(rad);
            robotState.y += distMove * Math.sin(rad);

            sendUpdate("Homing");

            sleep(100);
        }
    }

    // ---------------------------------------------------------------------
    // UTIL
    // ---------------------------------------------------------------------

    private static void sendUpdate(String type) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "update");
        update.put("x", robotState.x);
        update.put("y", robotState.y);
        update.put("heading", robotState.heading);

        System.out.printf(
                "Move: %s | State: X=%.1f, Y=%.1f, H=%.1f%n",
                type, robotState.x, robotState.y, robotState.heading
        );

        send(gson.toJson(update));
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }

    // ---------------------------------------------------------------------
    // DATA CLASSES
    // ---------------------------------------------------------------------

    private static class RobotState {
        @SerializedName("x") double x;
        @SerializedName("y") double y;
        @SerializedName("heading") double heading;

        RobotState() { this(0,0,0); }

        RobotState(double x, double y, double heading) {
            this.x = x;
            this.y = y;
            this.heading = heading;
        }
    }

    private static class Ball {
        int id;
        double x, y;

        Ball(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    private static class Basket {
        double x, y;

        Basket(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
