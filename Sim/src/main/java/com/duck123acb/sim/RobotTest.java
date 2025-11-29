package com.duck123acb.sim;

import com.duck123acb.robotcore.Motor;
import com.duck123acb.robotcore.Robot;
import com.duck123acb.robotcore.RobotState;
import com.duck123acb.sim.FakeMotor;

public class RobotTest {
    public static void main(String[] args) {
        testFieldCentricDrive();
        testTurnPID();
        testDriveDirection();
        testTurnThenMove();
        testFaceTargetThenMove();
    }

    // ... (existing methods)

    private static void testFaceTargetThenMove() {
        System.out.println("\nRunning testFaceTargetThenMove...");
        Motor fl = new FakeMotor();
        Motor fr = new FakeMotor();
        Motor bl = new FakeMotor();
        Motor br = new FakeMotor();
        Motor il = new FakeMotor();
        Motor ir = new FakeMotor();
        Motor ol = new FakeMotor();
        Motor or = new FakeMotor();
        
        Robot robot = new Robot(fl, fr, bl, br, il, ir, ol, or, 0, 0, 0);
        RobotState state = robot.getState();
        
        // Target is at (10, 10). Angle should be 45 degrees (PI/4).
        double targetX = 10;
        double targetY = 10;
        double dx = targetX - state.x;
        double dy = targetY - state.y;
        double targetHeading = Math.atan2(dy, dx);
        
        System.out.println("Target Heading: " + targetHeading);
        
        // Phase 1: Turn
        System.out.println("Phase 1: Turning...");
        for (int i = 0; i < 100; i++) {
            robot.turnPID(targetHeading);
            double diff = targetHeading - state.heading;
            if (Math.abs(diff) < 0.05) break;
            try { Thread.sleep(20); } catch (InterruptedException e) {}
        }
        
        if (Math.abs(state.heading - targetHeading) < 0.1) {
            System.out.println("PASS: Turned to target. h=" + state.heading);
        } else {
            System.out.println("FAIL: Did not reach target heading. h=" + state.heading);
        }
        
        // Phase 2: Move
        System.out.println("Phase 2: Moving...");
        for (int i = 0; i < 300; i++) {
            robot.goToXY_PID(targetX, targetY, targetHeading, 1.0f);
            if (Math.hypot(targetX - state.x, targetY - state.y) < 0.5) break;
            try { Thread.sleep(20); } catch (InterruptedException e) {}
        }
        
        if (Math.hypot(targetX - state.x, targetY - state.y) < 1.0) {
            System.out.println("PASS: Reached target. x=" + state.x + ", y=" + state.y);
        } else {
            System.out.println("FAIL: Did not reach target. x=" + state.x + ", y=" + state.y);
        }
    }

    // ... (existing methods)

    private static void testTurnThenMove() {
        System.out.println("\nRunning testTurnThenMove...");
        Motor fl = new FakeMotor();
        Motor fr = new FakeMotor();
        Motor bl = new FakeMotor();
        Motor br = new FakeMotor();
        Motor il = new FakeMotor();
        Motor ir = new FakeMotor();
        Motor ol = new FakeMotor();
        Motor or = new FakeMotor();
        
        Robot robot = new Robot(fl, fr, bl, br, il, ir, ol, or, 0, 0, 0);
        RobotState state = robot.getState();
        
        double targetHeading = Math.PI / 2;
        
        // Phase 1: Turn
        System.out.println("Phase 1: Turning...");
        for (int i = 0; i < 100; i++) { // Simulate loop
            robot.turnPID(targetHeading);
            double diff = targetHeading - state.heading;
            if (Math.abs(diff) < 0.05) break;
            try { Thread.sleep(20); } catch (InterruptedException e) {}
        }
        
        if (Math.abs(state.heading - targetHeading) < 0.1) {
            System.out.println("PASS: Turned to target. h=" + state.heading);
        } else {
            System.out.println("FAIL: Did not reach target heading. h=" + state.heading);
        }
        
        // Phase 2: Move
        System.out.println("Phase 2: Moving...");
        double targetX = 0;
        double targetY = 10;
        
        for (int i = 0; i < 300; i++) {
            robot.goToXY_PID(targetX, targetY, targetHeading, 1.0f);
            if (state.y > 9.5) break;
            try { Thread.sleep(20); } catch (InterruptedException e) {}
        }
        
        if (state.y > 1.0) {
            System.out.println("PASS: Moved towards target. y=" + state.y);
        } else {
            System.out.println("FAIL: Did not move. y=" + state.y);
        }
        
        // Check heading maintenance
        // Note: Heading might wrap, but target is PI/2, so it should be close.
        double hDiff = state.heading - targetHeading;
        while (hDiff > Math.PI) hDiff -= 2 * Math.PI;
        while (hDiff < -Math.PI) hDiff += 2 * Math.PI;
        
        if (Math.abs(hDiff) < 0.1) {
            System.out.println("PASS: Maintained heading. h=" + state.heading);
        } else {
            System.out.println("FAIL: Lost heading. h=" + state.heading);
        }
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
        
        // Start at 0,0,0
        Robot robot = new Robot(fl, fr, bl, br, il, ir, ol, or, 0, 0, 0);
        
        // Set Robot Heading to 90 degrees (PI/2)
        // We need to access the internal state.
        RobotState state = robot.getState();
        state.heading = Math.PI / 2; // 90 degrees
        
        // Target: Move +X (Global).
        // Current: 0,0.
        // Target: 10,0.
        // We expect the robot to move in +X direction globally.
        // Since robot is facing +Y (90 deg), +X is to its RIGHT.
        // So it should strafe RIGHT.
        
        System.out.println("Initial State: x=" + state.x + ", y=" + state.y + ", h=" + state.heading);
        
        // Run one update of goToXY_PID
        // We use a large P to ensure movement
        robot.goToXY_PID(10, 0, Math.PI / 2, 1.0f);
        
        System.out.println("After Update: x=" + state.x + ", y=" + state.y);
        
        // Check if x increased (moved East)
        if (state.x > 0.001) {
            System.out.println("PASS: Moved +X");
        } else {
            System.out.println("FAIL: Did not move +X. x=" + state.x);
        }
        
        // Check if y changed significantly (should be 0)
        if (Math.abs(state.y) < 0.001) {
            System.out.println("PASS: Did not move Y");
        } else {
            System.out.println("FAIL: Moved Y. y=" + state.y);
        }
    }

    private static void testTurnPID() {
        System.out.println("\nRunning testTurnPID...");
        Motor fl = new FakeMotor();
        Motor fr = new FakeMotor();
        Motor bl = new FakeMotor();
        Motor br = new FakeMotor();
        Motor il = new FakeMotor();
        Motor ir = new FakeMotor();
        Motor ol = new FakeMotor();
        Motor or = new FakeMotor();
        
        Robot robot = new Robot(fl, fr, bl, br, il, ir, ol, or, 0, 0, 0);
        RobotState state = robot.getState();
        
        // Target: 90 degrees (PI/2)
        // Current: 0
        robot.turnPID(Math.PI / 2);
        
        System.out.println("After Turn Update: h=" + state.heading);
        
        // Should have turned positive (CCW)
        if (state.heading > 0.001) {
            System.out.println("PASS: Turned Positive");
        } else {
            System.out.println("FAIL: Did not turn positive. h=" + state.heading);
        }
    }

    private static void testDriveDirection() {
        System.out.println("\nRunning testDriveDirection...");
        Motor fl = new FakeMotor();
        Motor fr = new FakeMotor();
        Motor bl = new FakeMotor();
        Motor br = new FakeMotor();
        Motor il = new FakeMotor();
        Motor ir = new FakeMotor();
        Motor ol = new FakeMotor();
        Motor or = new FakeMotor();
        
        Robot robot = new Robot(fl, fr, bl, br, il, ir, ol, or, 0, 0, 0);
        RobotState state = robot.getState();
        
        // Drive North (PI/2) at speed 1.0, maintaining 0 heading
        robot.driveDirection(Math.PI / 2, 1.0, 0);
        
        System.out.println("After Drive Direction: x=" + state.x + ", y=" + state.y);
        
        // Should move +Y
        if (state.y > 0.001) {
            System.out.println("PASS: Moved +Y");
        } else {
            System.out.println("FAIL: Did not move +Y. y=" + state.y);
        }
        
        // Should not move X
        if (Math.abs(state.x) < 0.001) {
            System.out.println("PASS: Did not move X");
        } else {
            System.out.println("FAIL: Moved X. x=" + state.x);
        }
    }
}
