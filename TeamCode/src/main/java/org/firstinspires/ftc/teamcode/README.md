# FTC Robot Controller Project

## Project Purpose

This project is designed to control a FIRST Tech Challenge (FTC) robot. The robot is equipped with a mecanum drivetrain, an intake/outtake system for collecting and scoring "balls," and a HuskyLens vision sensor for autonomous navigation. The primary goal is to compete in a game that involves both autonomous and driver-controlled periods.

## General Setup

The codebase is organized into two primary operational modes and a `systems` package for modular hardware control.

- **Hardware:** The robot utilizes a mecanum drive for omnidirectional movement, an IMU for orientation tracking, motor encoders for distance measurement, and a HuskyLens camera for vision-based object detection (AprilTags and colored balls).
- **Control Systems:** PID controllers are used for precise turning and alignment during the autonomous phase.
- **Modularity:** Hardware control is abstracted into a `systems` package, promoting clean and reusable code.

## Operational Modes

The robot has two main operational modes:

1.  **`TeleOpMecanum.java` (Driver-Controlled):**
    - This script enables full manual control of the robot using a gamepad.
    - It manages the mecanum drive, the intake/outtake motors, and a servo-controlled launcher.

2.  **`BallChase.java` (Autonomous):**
    - This script defines the robot's behavior during the autonomous period.
    - It uses the HuskyLens camera to detect an AprilTag, which determines the sequence for collecting colored balls.
    - The robot navigates to predefined field locations using a combination of motor encoders, the IMU, and PID control for accurate movements.
    - **Note:** This autonomous routine is currently a work in progress and requires significant tuning of PID gains, power levels, and field coordinates.

## Code Structure

-   `BallChase.java`: The main autonomous script that uses HuskyLens vision to detect AprilTags and collect balls.
-   `TeleOpMecanum.java`: The main tele-operated script for driver control of the mecanum robot.
-   `PID.java`: A basic PID controller implementation used for precise movements.
-   `PID_Test_Mecanum.java`: A test OpMode for tuning the PID controller.
-   `systems/`: A package containing modular classes for hardware systems.
    -   `DriveSystem.java`: Manages the mecanum drivetrain logic.
    -   `TakeSystem.java`: Controls the intake and outtake motor systems.