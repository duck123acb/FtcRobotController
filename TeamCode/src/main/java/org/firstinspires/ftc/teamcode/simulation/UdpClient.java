package org.firstinspires.ftc.teamcode.simulation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A simple UDP client to send data to the visualizer.
 */
public class UdpClient {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public UdpClient(String host, int port) {
        this.port = port;
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(host);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        try {
            byte[] buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        socket.close();
    }
}
