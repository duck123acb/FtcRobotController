const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const dgram = require('dgram');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

const UDP_PORT = 41234;

// Serve static files from the 'public' directory
app.use(express.static('public'));

// WebSocket connection handler
wss.on('connection', ws => {
    console.log('Client connected');
    ws.on('close', () => {
        console.log('Client disconnected');
    });
});

// Broadcast data to all connected clients
function broadcast(data) {
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(data);
        }
    });
}

// UDP Socket setup
const udpSocket = dgram.createSocket('udp4');

udpSocket.on('listening', () => {
    const address = udpSocket.address();
    console.log(`UDP socket listening on ${address.address}:${address.port}`);
});

udpSocket.on('message', (msg, rinfo) => {
    // Received a message from the Java simulation, broadcast it to the web clients
    const message = msg.toString();
    console.log(`Received UDP message: ${message}`);
    broadcast(message);
});

udpSocket.bind(UDP_PORT);

// Start the server
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Server started on http://localhost:${PORT}`);
});
