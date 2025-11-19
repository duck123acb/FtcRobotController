DECODE FTC Project - Component READMEs
1. BallOrder System

Purpose:
Manages the sequence of balls the robot must collect.

Inputs:

Real: AprilTag detections that indicate ball positions.

Sim: Predefined array of ball positions passed in for testing.

Outputs:

Current target ball (position/index).

Signals when the ball has been collected.

Notes:

In simulation, you can feed a static sequence instead of using AprilTags.

Provides an interface for the drivetrain/autonomous routines to know the next target.

2. Drivetrain

Purpose:
Moves the robot to target positions.

Inputs:

Target x/y coordinates from BallOrder or basket locations.

Optional heading for rotation control.

Outputs:

Robot position and heading updates (sim).

Commands to motors/encoders (real).

Sim vs Real Notes:

Sim: Use fake motor objects that track position in software.

Real: Uses actual motor/encoder interfaces.

3. Intake / Collector

Purpose:
Collects balls when the robot reaches them.

Inputs:

Command to start/stop intake.

Position info to know when the robot is at the ball.

Outputs:

Ball picked up signal.

Optional animation/visual update in sim.

Sim vs Real Notes:

Sim: Use a placeholder function that “collects” the ball by marking it as collected in your simulated world.

Real: Spins motor/servo to intake.

4. Basket / Scoring System

Purpose:
Handles depositing balls into the basket.

Inputs:

Ball collected flag.

Robot position near basket.

Outputs:

Ball removed from robot inventory.

Score updated / visual change in sim.

Sim vs Real Notes:

Sim: Mark the ball as in basket and trigger any render updates.

Real: Operate motor/servo to deposit.

5. Simulation Renderer

Purpose:
Renders robot, balls, and basket for visualization.

Inputs:

Objects to render (robot position, balls, basket).

Updates from drivetrain, intake, and ball order.

Outputs:

Visual display of current simulation state.

Notes:

Can be simplified to a 2D top-down view.

Optional: color code collected vs uncollected balls.

6. Fake Motor / Servo System (for sim)

Purpose:
Mimics motor/servo behavior so the same code works in sim and real.

Inputs:

Target positions, power levels, or speeds.

Outputs:

Updated position/state in software.

Optional logs for debugging.

Notes:

Helps keep your functions unchanged between sim and real.

Can simulate limits, inertia, or delays for realism.

7. Autonomous Controller

Purpose:
Coordinates the full sequence: move → collect → move → score → next ball.

Inputs:

BallOrder sequence.

Robot/drivetrain state.

Outputs:

Commands to drivetrain, intake, and basket.

Signals when routine is complete.

Sim vs Real Notes:

Sim: Logs steps and triggers render updates.

Real: Sends real motor/servo commands.