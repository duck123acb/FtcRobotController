# FTC Robot Visualizer

This directory contains a simple web-based visualizer for simulating and debugging FTC robot movements. It consists of a Node.js server that serves a static HTML/Canvas page and a test harness that sends simulated robot position data to the server via UDP.

## How It Works

-   `server.js`: A Node.js Express server that serves the `public` directory and listens for UDP packets containing robot state updates.
-   `public/index.html`: The main page that contains the canvas for visualization.
-   `public/main.js`: The client-side JavaScript that connects to the server via WebSocket, receives state updates, and draws the robot, balls, and basket on the canvas.
-   `test.js`: A test harness that can be run to simulate robot movements and send data to the server. It can be run in several modes.

## How to Run

### 1. Install Dependencies

First, you need to install the Node.js dependencies. Open a terminal in this directory (`TeamCode/src/vis`) and run:

```bash
npm install
```

### 2. Start the Visualizer Server

In the same terminal, start the server:

```bash
npm start
```

This will start the web server. You can now open a web browser and navigate to `http://localhost:3000` to see the visualizer field.

## Receiving Events

The visualizer listens for UDP packets on port 41234. These packets should be JSON strings representing the robot's state. There are two primary ways to send data to the visualizer.

### Receiving Events from an FTC Simulation

This is the primary use case. The Java code running in the FTC Robot Controller simulation (e.g., `BallChase_Simulation.java`) can be configured to send its state to this visualizer. This allows you to see a real-time representation of your robot's calculated position as your OpMode runs.

*To enable this, ensure the `UdpClient` in the simulation Java code is pointing to `127.0.0.1:41234`.*

### Using the Visualization Test Harness (for testing and fun)

For testing the visualizer itself or for generating interesting movement patterns, you can use the included Node.js test harness.

**Open a second, separate terminal** in this same directory (`TeamCode/src/vis`) and run one of the following commands.

#### Random Motion (Default)

This mode makes the robot move randomly with a mix of turning, driving forward, and strafing.

```bash
node test.js random
```

Or, since it's the default:

```bash
node test.js
```

#### Tank Motion

This mode simulates a "tank-style" robot that only turns and drives forward.

```bash
node test.js tank
```

#### Homing Motion

This mode simulates a robot that moves randomly within the center of the field but will attempt to turn back towards the center if it drives outside a 72-inch radius.

```bash
node test.js homing
```
