package com.duck123acb.sim;

import com.duck123acb.robotcore.Motor;
import com.duck123acb.robotcore.Robot;
import com.duck123acb.robotcore.RobotState;
import com.duck123acb.sim.FakeMotor;

public class RobotTest {
    public static void main(String[] args) {
        testFieldCentricDrive();
    }

    private static void testFieldCentricDrive() {
        System.out.println("Running testFieldCentricDrive...");
        
        // Setup Robot
        Motor fl = new FakeMotor();
        Motor fr = new FakeMotor();
        Motor bl = new FakeMotor();
        Motor br = new FakeMotor();
        Motor il = new FakeMotor();
        Motor ir = new FakeMotor();
        Motor ol = new FakeMotor();
        Motor or = new FakeMotor();
        
        Robot robot = new Robot(fl, fr, bl, br, il, ir, ol, or, 0, 0, 0);
        
        // Set Robot Heading to 90 degrees (PI/2)
        // We need to access the internal state.
        // Robot.getState() returns the state object from DriveSystem.
        RobotState state = robot.getState();
        state.heading = Math.PI / 2; // 90 degrees
        
        // Target: Move +X (Global).
        // Current: 0,0.
        // Target: 10,0.
        // PID X error = 10. Output ~ 0.5 (0.05 * 10).
        // PID Y error = 0. Output 0.
        // PID Heading error = 0 - PI/2 = -PI/2. Output ~ -3.
        
        // We want to test pure translation first to verify the coordinate transform.
        // Let's mock the PIDs? No, we can't easily.
        // But we can check the motor powers or the resulting movement.
        
        // Let's run one update of goToXY_PID.
        robot.goToXY_PID(10, 0, Math.PI / 2, 1.0f);
        
        // Analysis:
        // Heading = 90 deg.
        // Target = (10, 0).
        // PID X output (Global Vx) > 0.
        // PID Y output (Global Vy) = 0.
        // PID Heading output = 0.
        
        // Robot Frame Transformation:
        // robotX = globalVx * cos(90) - globalVy * sin(90)
        //        = globalVx * 0 - 0 * 1 = 0.
        // robotY = globalVx * sin(90) + globalVy * cos(90)
        //        = globalVx * 1 + 0 = globalVx.
        
        // So Robot should move in +Y (Robot Forward).
        // Since Robot is facing +Y (Global), moving Robot Forward (+Y) means moving Global +Y.
        // WAIT.
        // If Robot is facing +Y (Global), and we want to move +X (Global).
        // That means we need to Strafe Right.
        // Strafe Right is -Y (Robot Frame) or +X (Robot Frame)?
        // In SimTest, we found:
        // Forward is +X (Robot Frame).
        // Strafe Right is -Y (Robot Frame).
        
        // So if we want to move +X (Global), and we are facing +Y (Global).
        // We need to move Right relative to Robot.
        // Right relative to Robot is Strafe Right.
        // Strafe Right is -Y (Robot Frame).
        
        // Let's check the math in Robot.java:
        // robotX = vx * cos - vy * sin
        // robotY = vx * sin + vy * cos
        // Here vx is globalVx, vy is globalVy.
        // vx > 0, vy = 0.
        // robotX = vx * 0 - 0 = 0.
        // robotY = vx * 1 + 0 = vx.
        
        // So robotY gets positive power.
        // robotX gets 0.
        
        // In DriveSystem.driveMecanum(robotX, robotY, omega):
        // x = robotX = 0.
        // y = robotY = positive.
        // turn = 0.
        
        // fl = y + x = positive.
        // fr = y - x = positive.
        // ...
        // This is Forward movement.
        // Forward movement is +X (Robot Frame) in SimTest?
        // Wait, SimTest said:
        // testForwardMovement (all 1) -> Moved in X.
        // So Forward IS +X.
        
        // But driveMecanum uses `y` as the first term `fl = y + x`.
        // Usually `y` is forward.
        // So if `y` is positive, it moves Forward.
        // And Forward is +X.
        
        // So `robotY` corresponds to Forward (+X).
        // And `robotX` corresponds to Strafe (-Y).
        
        // So if `robotY` is positive, we move Forward (+X).
        // But we wanted to move Strafe Right (-Y).
        // Because we are facing +Y and want to move +X.
        // That is a Right Strafe.
        
        // So `robotY` (Forward) is WRONG. We should be Strafing.
        
        // Why did we get `robotY = vx`?
        // robotY = vx * sin(90) + vy * cos(90).
        // sin(90) = 1.
        // So robotY = vx.
        
        // This rotation matrix:
        // x' = x cos - y sin
        // y' = x sin + y cos
        // This rotates vector (x,y) by angle theta.
        // If we have a global vector (vx, vy) and we want to express it in a frame rotated by theta.
        // We should rotate by -theta.
        // x_local = x_global * cos(-theta) - y_global * sin(-theta)
        //         = x_global * cos(theta) + y_global * sin(theta)
        // y_local = x_global * sin(-theta) + y_global * cos(-theta)
        //         = -x_global * sin(theta) + y_global * cos(theta)
        
        // The code in Robot.java does:
        // robotX = vx * cos - vy * sin
        // robotY = vx * sin + vy * cos
        // This is rotation by +theta.
        
        // So if we have a vector in Global Frame, and we rotate it by +Theta...
        // That gives us the vector in a frame rotated by -Theta? No.
        
        // Let's visualize.
        // Global X vector (1, 0).
        // Robot rotated 90 deg (+Y).
        // We want Local vector.
        // Local vector should be "Strafe Right".
        // If Forward is +X (Robot), Left is +Y (Robot).
        // Then Strafe Right is -Y (Robot).
        // So we expect (0, -1).
        
        // Code gives:
        // robotX = 1 * 0 - 0 * 1 = 0.
        // robotY = 1 * 1 + 0 * 0 = 1.
        // Result: (0, 1).
        // (0, 1) in Robot Frame.
        // If X is Forward, Y is Left.
        // (0, 1) is Left.
        
        // So the code thinks we should move Left.
        // But we want to move Right.
        // Because if we are facing +Y, and want to move +X, that is to our Right.
        
        // So the rotation is INVERTED.
        // We need to rotate by -heading.
        
        // So Robot.java has TWO bugs.
        // 1. Math.toRadians (Fixed).
        // 2. Rotation direction (Needs fix).
        
        // Let's verify this with the test.
        // If I run the test as is, it should show "Moved Left" (or similar) instead of Right.
        
        // I'll add assertions to the test.
        
        // We need to see what the robot actually does.
        // We can check `driveSystem.getRobotState()`?
        // No, `updateSim` hasn't run yet?
        // `goToXY_PID` calls `driveSystem.updateSim(0.02)`.
        // So it DOES run.
        
        // So we can check `state.x` and `state.y`.
        // Initial: 0,0.
        // Target: 10,0.
        // Heading: 90.
        // Expected: Move +X (Global).
        // Actual (if bug): Move Left (Robot) -> Global -X?
        // Wait.
        // If Robot moves Left (Robot +Y).
        // Robot is facing +Y (Global).
        // Left of +Y is -X (Global).
        // So Robot will move -X.
        // But we wanted +X.
        
        System.out.println("After Update: " + state.x + ", " + state.y);
        
        System.out.println("After Update: " + state.x + ", " + state.y);
        
        if (state.x > 0.1) System.out.println("PASS: Moved +X");
        else System.out.println("FAIL: Moved " + state.x + ", " + state.y);
    }
}
