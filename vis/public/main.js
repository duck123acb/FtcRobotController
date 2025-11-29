/**
 * Canvas + context handle the 2D field render.
 */
const canvas = document.getElementById('fieldCanvas');
const ctx = canvas.getContext('2d');

/**
 * FTC field size in inches (12ft). Used to convert field coords → pixels.
 */
const FIELD_SIZE_INCHES = 144;

/**
 * How many pixels represent one inch on the canvas.
 */
const PIXELS_PER_INCH = canvas.width / FIELD_SIZE_INCHES;

/**
 * Robot state object updated by the server.
 * @type {{x: number, y: number, heading: number}}
 */
let robotState = { x: 0, y: 0, heading: 0 };

/**
 * Ball objects received from server.
 * Each ball: { id:number, x:number, y:number, colour:'g'|'p' }
 */
let balls = [];

/**
 * Basket objects received from server.
 * Each basket: { x:number, y:number, colour:'r'|'b', ballCount:number }
 */
let baskets = [];

/**
 * Convert FTC field coordinates (0,0 at field center)
 * into canvas coordinates (0,0 at top-left).
 *
 * @param {number} x - field X coordinate in inches
 * @param {number} y - field Y coordinate in inches
 * @returns {{x:number, y:number}} canvas-space coordinates
 */
function toCanvasCoords(x, y) {
    const canvasX = (x + FIELD_SIZE_INCHES / 2) * PIXELS_PER_INCH;
    const canvasY = canvas.height - (y + FIELD_SIZE_INCHES / 2) * PIXELS_PER_INCH;
    return { x: canvasX, y: canvasY };
}

/**
 * Draw the field boundary, all balls, and all baskets.
 * Called every frame.
 */
function drawField() {
    // clear field
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // draw outer border
    ctx.strokeStyle = '#ccc';
    ctx.strokeRect(0, 0, canvas.width, canvas.height);

    // -------------------------
    // Draw balls
    // -------------------------
    balls.forEach(ball => {
        const pos = toCanvasCoords(ball.x, ball.y);

        ctx.beginPath();
        ctx.arc(pos.x, pos.y, 8, 0, 2 * Math.PI);

        // ball color based on sim data
        ctx.fillStyle = ball.colour === 'g' ? '#00dd00' : '#4444ff';
        ctx.fill();

        // ID text
        ctx.fillStyle = 'white';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(ball.id, pos.x, pos.y);
    });

    // -------------------------
    // Draw baskets
    // -------------------------
    baskets.forEach(basket => {
        const pos = toCanvasCoords(basket.x, basket.y);

        // draw basket square
        ctx.fillStyle = basket.colour === 'r' ? 'red' : 'blue';
        ctx.fillRect(pos.x - 10, pos.y - 10, 20, 20);

        // draw X inside for visibility
        ctx.strokeStyle = 'black';
        ctx.beginPath();
        ctx.moveTo(pos.x - 10, pos.y - 10);
        ctx.lineTo(pos.x + 10, pos.y + 10);
        ctx.moveTo(pos.x + 10, pos.y - 10);
        ctx.lineTo(pos.x - 10, pos.y + 10);
        ctx.stroke();

        // ball count label under the basket
        ctx.fillStyle = 'white';
        ctx.textAlign = 'center';
        ctx.fillText(String(basket.ballCount), pos.x, pos.y + 18);
    });
}

/**
 * Draw the robot as a circle with a heading arrow.
 */
function drawRobot() {
    const { x, y, heading } = robotState;
    const pos = toCanvasCoords(x, y);

    // 18" robot width → radius → scaled down
    const robotRadius = (18 / 2 / 4) * PIXELS_PER_INCH;

    // robot body
    ctx.beginPath();
    ctx.arc(pos.x, pos.y, robotRadius, 0, 2 * Math.PI);
    ctx.fillStyle = 'gray';
    ctx.fill();

    // heading arrow
    // heading arrow
    // heading is already in radians (0 = East, + = CCW)
    const mathAngle = heading;
    const endX = pos.x + robotRadius * Math.cos(mathAngle);
    const endY = pos.y - robotRadius * Math.sin(mathAngle);

    ctx.beginPath();
    ctx.moveTo(pos.x, pos.y);
    ctx.lineTo(endX, endY);
    ctx.strokeStyle = 'red';
    ctx.lineWidth = 3;
    ctx.stroke();
}

/**
 * Redraw the whole scene (field + robot).
 */
function redraw() {
    drawField();
    drawRobot();
}

/**
 * WebSocket connection to the Java simulator.
 */
const ws = new WebSocket(`ws://${window.location.host}`);

/**
 * Fired when WebSocket connects.
 */
ws.onopen = () => {
    console.log('Connected to WebSocket server');
    redraw();
};

/**
 * Handle messages from the server.
 *
 * Expected payloads:
 *  - {type:'config', balls:[], baskets:[], initialState:{}}
 *  - {type:'update', x,y,heading}
 *  - {type:'balls', balls:[]}
 *  - {type:'basket', basket:{}}
 */
ws.onmessage = event => {
    try {
        const data = JSON.parse(event.data);

        if (data.type === 'config') {
            balls = data.balls || [];
            baskets = data.baskets || [];
            if (data.initialState) robotState = data.initialState;

        } else if (data.type === 'update') {
            robotState = {
                x: data.x,
                y: data.y,
                heading: data.heading
            };

        } else if (data.type === 'balls') {
            balls = data.balls;

        } else if (data.type === 'basket') {
            // overwrite basket array w/ single update
            if (!Array.isArray(baskets)) baskets = [];
            baskets = [data.basket];
        }

        redraw();
    } catch (e) {
        console.error('Failed to parse message:', event.data, e);
    }
};

/**
 * Fired when WebSocket disconnects.
 */
ws.onclose = () => {
    console.log('Disconnected from WebSocket server');
};