const dgram = require('dgram');

const VIS_PORT = 41234;
const VIS_HOST = '127.0.0.1';

const client = dgram.createSocket('udp4');

let robotState = { x: 0, y: 0, heading: 0 };

function send(message) {
    const buffer = Buffer.from(message);
    client.send(buffer, VIS_PORT, VIS_HOST, (err) => {
        if (err) console.error('Failed to send message:', err);
    });
}

function sendInitialConfig() {
    // NOTE The main field is +/- 72 inches in X and Y from center origin
    const fieldBounds = 70; // Place items within a +/- 70 inch box

    // 1. Random number of balls (2 to 5)
    const numBalls = 2 + Math.floor(Math.random() * 4);
    const balls = [];
    for (let i = 1; i <= numBalls; i++) {
        balls.push({
            id: i,
            x: (Math.random() - 0.5) * 2 * fieldBounds,
            y: (Math.random() - 0.5) * 2 * fieldBounds
        });
    }

    // 2. Random basket position
    const basket = {
        x: (Math.random() - 0.5) * 2 * fieldBounds,
        y: (Math.random() - 0.5) * 2 * fieldBounds
    };

    const config = {
        type: 'config',
        initialState: { x: 0, y: 0, heading: 0 },
        balls: balls,
        basket: basket
    };

    const message = JSON.stringify(config);
    console.log('Sending randomized initial configuration...');
    send(message);
}

function startRandomMovements() {
    console.log('Starting random robot movements...');
    setInterval(() => {
        const action = Math.random();
        let moveType = '';

        if (action < 0.3) { // 30% chance to turn
            moveType = 'Turn';
            robotState.heading += (Math.random() - 0.5) * 30; // -15 to 15 degrees
            //robotState.heading = 45; // FIXED HEADING FOR TESTING

        } else if (action < 0.9) { // 60% chance to drive forward (0.3 to 0.9)
            moveType = 'Forward';
            const moveDistance = 1 + (Math.random() * 4);
            const mathAngle = -robotState.heading + 90;
            const angleRad = mathAngle * (Math.PI / 180);
            robotState.x += moveDistance * Math.cos(angleRad);
            robotState.y += moveDistance * Math.sin(angleRad);

        } else { // 10% chance to strafe (0.9 to 1.0)
            moveType = 'Strafe';
            let moveDistance = 1 + (Math.random() * 4);
            if (Math.random() < 0.5) {
                moveDistance *= -1; // Strafe left
            }
            const mathAngle = -robotState.heading;
            const angleRad = mathAngle * (Math.PI / 180);
            robotState.x += moveDistance * Math.cos(angleRad);
            robotState.y += moveDistance * Math.sin(angleRad);
        }

        // 3. Send the update
        const update = {
            type: 'update',
            ...robotState
        };
        const message = JSON.stringify(update);
        console.log(`Move: ${moveType} | State: X=${robotState.x.toFixed(1)}, Y=${robotState.y.toFixed(1)}, H=${robotState.heading.toFixed(1)}`);
        send(message);

    }, 1000); // Send an update every second
}

// --- Main --- //
console.log(`Test harness will send UDP packets to ${VIS_HOST}:${VIS_PORT}`);
sendInitialConfig();

function startTankMotion() {
    console.log('Starting tank-style robot movements...');
    setInterval(() => {
        // 1. Apply a random turn between -23 and 23 degrees
        const turn = (Math.random() - 0.5) * 46;
        robotState.heading += turn;

        // 2. Move forward a random distance
        const moveDistance = 1 + (Math.random() * 2);
        const mathAngle = -robotState.heading + 90;
        const angleRad = mathAngle * (Math.PI / 180);
        robotState.x += moveDistance * Math.cos(angleRad);
        robotState.y += moveDistance * Math.sin(angleRad);

        // 3. Send the update
        const update = {
            type: 'update',
            ...robotState
        };
        const message = JSON.stringify(update);
        console.log(`Move: Tank | State: X=${robotState.x.toFixed(1)}, Y=${robotState.y.toFixed(1)}, H=${robotState.heading.toFixed(1)}`);
        send(message);

    }, 100); // Send an update every 100ms
}

function startHomingMotion() {
    console.log('Starting homing-style robot movements...');
    setInterval(() => {
        const { x, y, heading } = robotState;
        const distanceFromCenter = Math.sqrt(x * x + y * y);

        let turn = 0;

        if (distanceFromCenter > 72) {
            // Calculate the angle from the robot to the center (0,0)
            const angleToCenterRad = Math.atan2(-y, -x);
            let homingAngle = angleToCenterRad * (180 / Math.PI);
            homingAngle = (homingAngle + 360) % 360; // Normalize to 0-360

            // Convert to our robot's heading system (0 is North)
            const homingHeading = (-homingAngle + 90 + 360) % 360;

            // Find the shortest turn to the homing heading
            let turnError = homingHeading - heading;
            if (turnError > 180) turnError -= 360;
            if (turnError < -180) turnError += 360;

            // Bias the turn towards the center, but keep it random
            const turnBias = Math.sign(turnError) * 15; // A strong nudge towards the center
            const randomTurn = (Math.random() - 0.5) * 20; // A smaller random component
            turn = turnBias + randomTurn;
        } else {
            // Standard random turn when inside the circle
            turn = (Math.random() - 0.5) * 46;
        }

        robotState.heading += turn;

        // Move forward a random distance
        const moveDistance = 1 + (Math.random() * 2);
        const mathAngle = -robotState.heading + 90;
        const angleRad = mathAngle * (Math.PI / 180);
        robotState.x += moveDistance * Math.cos(angleRad);
        robotState.y += moveDistance * Math.sin(angleRad);

        // Send the update
        const update = {
            type: 'update',
            ...robotState
        };
        const message = JSON.stringify(update);
        console.log(`Move: Homing | State: X=${robotState.x.toFixed(1)}, Y=${robotState.y.toFixed(1)}, H=${robotState.heading.toFixed(1)}`);
        send(message);

    }, 100); // Send an update every 100ms
}

// --- Main --- //
console.log(`Test harness will send UDP packets to ${VIS_HOST}:${VIS_PORT}`);
sendInitialConfig();

const motionType = process.argv[2];
const validModes = ['random', 'tank', 'homing'];

let motionFunction;

if (!motionType || !validModes.includes(motionType)) {
    if (motionType) { // If an invalid argument was actually provided
        console.log(`Argument '${motionType}' not understood.`);
    }
    console.log('Usage: node test.js [random|tank|homing]');
    console.log('Falling back to default mode: random');
    motionFunction = startRandomMovements;
} else {
    console.log(`Starting motion type: ${motionType}`);
    if (motionType === 'tank') {
        motionFunction = startTankMotion;
    } else if (motionType === 'homing') {
        motionFunction = startHomingMotion;
    } else { // random
        motionFunction = startRandomMovements;
    }
}

// Wait a moment for the config to be processed before starting updates
setTimeout(motionFunction, 500);
