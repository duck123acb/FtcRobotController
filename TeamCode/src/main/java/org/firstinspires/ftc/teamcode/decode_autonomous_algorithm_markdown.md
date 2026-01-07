# High-level Goal (DECODE)

1. Read AprilTag to determine the correct artifact scoring order.
2. Go to each ball line.
3. Detect balls with HuskyLens.
4. Pick up balls and sort/store them correctly.
5. Return to the basket.
6. Shoot balls in the decoded order.

---

# Step-by-step Autonomous Algorithm

## 1. Initialization Phase

- Initialize all motors, robot odometry, and HuskyLens.
- Set robot starting pose (x, y, heading).
- Reset PID controllers.
- Display `Waiting...` on telemetry.

---

## 2. Start & AprilTag Decode

- Wait for start.
- Switch HuskyLens to **TAG_RECOGNITION** mode.
- Loop until:
  - At least one AprilTag is detected, or
  - Autonomous stops.
- Read the **first detected tag ID**.
- Use the tag ID to select the artifact order:
  - Tag 0 → `GREEN, PURPLE, GREEN`
  - Tag 1 → `PURPLE, GREEN, PURPLE`
  - Tag 2 → `PURPLE, PURPLE, GREEN`
- Lock this order for the rest of autonomous.

---

## 3. Initial Basket Trip (Preload Score)

- Drive to the basket position.
- Shoot preload balls in the decoded order:
  - For each artifact in `artifactOrder`:
    - Rotate spin-dex to the correct slot.
    - Run outtake motor to score.

---

## 4. Switch to Ball Detection Mode

- Change HuskyLens to **OBJECT_RECOGNITION** mode.
- Enables color-based ball detection (green / purple).

---

## 5. Loop Through Ball Lines

For **each ball line position** in `BALL_LINES`:

### 5.1 Navigate to Ball Line

- Drive to the line’s field position.
- Align heading for optimal camera view.

### 5.2 Intake Phase

- Turn intake motors **ON**.
- Repeat until **3 balls are collected**:

#### a. Detect a Ball

- Scan HuskyLens blocks.
- Select the **largest visible block** (closest ball).
- Determine artifact type:
  - ID 1 → GREEN
  - ID 2 → PURPLE

#### b. Acquire the Ball

- Strafe or drive toward the detected ball.
- Center the ball in the camera view.
- Drive forward slowly until the intake captures it.

#### c. Store the Ball

- Rotate spin-dex to the correct internal slot based on color.
- Stop intake once the ball is secured.

---

## 6. Return to Basket

- Turn intake **OFF**.
- Drive back to the basket position.

---

## 7. Score Collected Balls

- For each artifact in `artifactOrder`:
  - Rotate spin-dex to the correct position.
  - Activate outtake to shoot the ball.
- Confirm scoring is complete.

---

## 8. Repeat for Remaining Ball Lines

- Return to **Step 5** until all ball lines are cleared.

---

## 9. End of Autonomous

- Stop all motors.
- Hold position or park if required.

---

# What Still Needs to Be Implemented

- Spin-dex position mapping for GREEN vs PURPLE.
- Ball-centering movement logic (camera → drivetrain).
- Intake stop condition (beam break / motor current / timer).
- Timeout failsafes to prevent stalling.
- Correct field coordinates for:
  - Basket
  - Each ball line
- Accurate starting pose (critical for PID reliability).

