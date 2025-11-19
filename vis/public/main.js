const canvas = document.getElementById('fieldCanvas');
const ctx = canvas.getContext('2d');

const FIELD_SIZE_INCHES = 144; // A standard FTC field is 12ft x 12ft
const PIXELS_PER_INCH = canvas.width / FIELD_SIZE_INCHES;

let robotState = { x: 0, y: 0, heading: 0 };
let balls = [];
let basket = null;

function toCanvasCoords(x, y) {
    // Convert from field coordinates (center is 0,0) to canvas coordinates (top-left is 0,0)
    const canvasX = (x + FIELD_SIZE_INCHES / 2) * PIXELS_PER_INCH;
    const canvasY = canvas.height - (y + FIELD_SIZE_INCHES / 2) * PIXELS_PER_INCH;
    return { x: canvasX, y: canvasY };
}

function drawField() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.strokeStyle = '#ccc';
    ctx.strokeRect(0, 0, canvas.width, canvas.height);

    // Draw balls
    balls.forEach(ball => {
        const pos = toCanvasCoords(ball.x, ball.y);
        ctx.beginPath();
        ctx.arc(pos.x, pos.y, 8, 0, 2 * Math.PI);
        ctx.fillStyle = 'blue';
        ctx.fill();
        ctx.fillStyle = 'white';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(ball.id, pos.x, pos.y);
    });

    // Draw basket
    if (basket) {
        const basketPos = toCanvasCoords(basket.x, basket.y);
        ctx.fillStyle = 'orange';
        ctx.fillRect(basketPos.x - 10, basketPos.y - 10, 20, 20);
        ctx.strokeStyle = 'black';
        ctx.beginPath();
        ctx.moveTo(basketPos.x - 10, basketPos.y - 10);
        ctx.lineTo(basketPos.x + 10, basketPos.y + 10);
        ctx.moveTo(basketPos.x + 10, basketPos.y - 10);
        ctx.lineTo(basketPos.x - 10, basketPos.y + 10);
        ctx.stroke();
    }
}

function drawRobot() {
    const { x, y, heading } = robotState;
    const pos = toCanvasCoords(x, y);
    const robotRadius = (18 / 2 / 4) * PIXELS_PER_INCH; // Quartered the size

    // Draw robot body (circle)
    ctx.beginPath();
    ctx.arc(pos.x, pos.y, robotRadius, 0, 2 * Math.PI);
    ctx.fillStyle = 'gray';
    ctx.fill();

    // Draw heading indicator (line)
    const mathAngle = (-heading + 90) * (Math.PI / 180);
    const endX = pos.x + robotRadius * Math.cos(mathAngle);
    const endY = pos.y - robotRadius * Math.sin(mathAngle); // Subtract because canvas Y is inverted

    ctx.beginPath();
    ctx.moveTo(pos.x, pos.y);
    ctx.lineTo(endX, endY);
    ctx.strokeStyle = 'red';
    ctx.lineWidth = 3;
    ctx.stroke();
}

function redraw() {
    drawField();
    drawRobot();
}

// WebSocket connection
const ws = new WebSocket(`ws://${window.location.host}`);

ws.onopen = () => {
    console.log('Connected to WebSocket server');
    redraw(); // Initial draw
};

ws.onmessage = event => {
    try {
        const data = JSON.parse(event.data);
        if (data.type === 'config') {
            balls = data.balls || [];
            basket = data.basket || null;
            if (data.initialState) {
                robotState = data.initialState;
            }
        } else if (data.type === 'update') {
            robotState = data;
        }
        redraw();
    } catch (e) {
        console.error('Failed to parse message:', event.data);
    }
};

ws.onclose = () => {
    console.log('Disconnected from WebSocket server');
};
