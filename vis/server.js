/**
 * Basic Node.js server to bridge the Java UDP simulator with web clients.
 * Uses Express to serve static files, WebSocket for live updates, and UDP to receive data from the Java sim.
 */

const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const dgram = require('dgram');

/**
 * Express app instance for serving static files.
 */
const app = express();

/**
 * HTTP server wrapping the Express app.
 */
const server = http.createServer(app);

/**
 * WebSocket server attached to the HTTP server.
 */
const wss = new WebSocket.Server({ server });

/**
 * UDP port to listen for Java simulation messages.
 */
const UDP_PORT = 41234;

/**
 * Serve static files from the 'public' directory.
 * Any HTML/JS/CSS placed there is accessible to web clients.
 */
app.use(express.static('public'));

/**
 * WebSocket connection handler.
 * Fired when a new client connects.
 *
 * @param {WebSocket} ws - The WebSocket connection object for this client
 */
wss.on('connection', ws => {
    console.log('Client connected');

    /**
     * Fired when a client disconnects.
     */
    ws.on('close', () => {
        console.log('Client disconnected');
    });
});

/**
 * Broadcast a message to all connected WebSocket clients.
 *
 * @param {string} data - The message to broadcast (JSON string)
 */
function broadcast(data) {
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(data);
        }
    });
}

/**
 * UDP socket to receive messages from the Java simulation.
 */
const udpSocket = dgram.createSocket('udp4');

/**
 * Fired when the UDP socket starts listening.
 */
udpSocket.on('listening', () => {
    const address = udpSocket.address();
    console.log(`UDP socket listening on ${address.address}:${address.port}`);
});

/**
 * Fired when a UDP message is received.
 *
 * @param {Buffer} msg - The message data
 * @param {dgram.RemoteInfo} rinfo - Remote info of the sender
 */
udpSocket.on('message', (msg, rinfo) => {
    const message = msg.toString();
    console.log(`Received UDP message: ${message}`);

    // Broadcast the message to all connected WebSocket clients
    broadcast(message);
});

/**
 * Bind the UDP socket to the specified port.
 */
udpSocket.bind(UDP_PORT);

/**
 * Start the HTTP + WebSocket server.
 */
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Server started on http://localhost:${PORT}`);
});