package com.duck123acb.sim;

import com.duck123acb.robotcore.Motor;
import com.duck123acb.robotcore.systems.DriveSystem;
import com.duck123acb.sim.FakeMotor;

public class SimTest {
    public static void main(String[] args) {
        testForwardMovement();
        testStrafeMovement();
        testRotatedMovement();
    }

    private static void testForwardMovement() {
        System.out.println("Running testForwardMovement...");
        DriveSystem drive = createDriveSystem();
        
        // Set motors to move forward
        // Forward: all positive? No, let's check DriveSystem.driveMecanum
        // fl = y + x + turn
        // fr = y - x - turn
        // bl = y - x + turn
        // br = y + x - turn
        // If x=1 (forward?), y=0, turn=0:
        // fl=1, fr=-1, bl=-1, br=1.
        // Wait, usually Y is forward in FTC.
        // If y=1, x=0, turn=0:
        // fl=1, fr=1, bl=1, br=1.
        // This looks like forward.
        
        // So let's set all motors to 1.
        setPowers(drive, 1, 1, 1, 1);
        
        // Update sim
        drive.updateSim(1.0); // 1 second
        
        // Check state
        // vx = (1+1+1+1)/4 = 1.
        // vy = (-1+1+1-1)/4 = 0.
        // omega = (-1+1-1+1)/4 = 0.
        // x += 1*1 = 1.
        // y += 0*1 = 0.
        // heading += 0.
        
        // Wait, in updateSim:
        // vx = (fl + fr + bl + br) / 4.0;
        // This corresponds to Y (forward) if all are 1.
        // But the variable is named vx.
        // And in driveMecanum:
        // double fl = y + x + turn;
        // If y=1, fl=1.
        // So vx in updateSim corresponds to Y input in driveMecanum.
        // This is confusing naming.
        // driveMecanum(x, y, turn) -> x is usually strafe, y is forward.
        // updateSim: vx is sum of all / 4. This is Forward (Y).
        // vy is (-fl + fr + bl - br) / 4.
        // If x=1 (strafe), y=0: fl=1, fr=-1, bl=-1, br=1.
        // vy = (-1 + -1 + -1 - 1) / 4 = -1.
        // So vy corresponds to -Strafe?
        
        // Let's verify standard mecanum kinematics.
        // Forward (Y): + + + +
        // Strafe Right (X): + - - +
        // Turn Right (W): + - + -
        
        // updateSim:
        // vx = sum/4. Matches Forward (Y).
        // vy = (-fl + fr + bl - br)/4.
        // If Strafe Right (fl=1, fr=-1, bl=-1, br=1):
        // vy = (-1 + -1 + -1 - 1)/4 = -1.
        // So vy is NEGATIVE Strafe (X).
        
        // omega = (-fl + fr - bl + br)/4.
        // If Turn Right (fl=1, fr=-1, bl=1, br=-1):
        // omega = (-1 + -1 - 1 + -1)/4 = -1.
        // So omega is NEGATIVE Turn.
        
        // This suggests coordinate system mismatch.
        // But let's check if my fix handles it.
        // My fix:
        // globalVx = vx * cos - vy * sin
        // globalVy = vx * sin + vy * cos
        // This assumes vx is local X, vy is local Y.
        // But vx seems to be Forward (usually Y).
        
        // If vx is Forward (Robot Y) and vy is Strafe (Robot X)...
        // Then we should swap them or rename them.
        
        // Let's assert what happens.
        if (Math.abs(drive.getRobotState().x - 1.0) < 0.001) {
             System.out.println("PASS: Moved in X (Forward?)");
        } else if (Math.abs(drive.getRobotState().y - 1.0) < 0.001) {
             System.out.println("PASS: Moved in Y (Forward?)");
        } else {
             System.out.println("FAIL: " + drive.getRobotState().x + ", " + drive.getRobotState().y);
        }
    }

    private static void testStrafeMovement() {
        System.out.println("Running testStrafeMovement...");
        DriveSystem drive = createDriveSystem();
        // Strafe Right: fl=1, fr=-1, bl=-1, br=1
        setPowers(drive, 1, -1, -1, 1);
        drive.updateSim(1.0);
        
        // vx = (1-1-1+1)/4 = 0.
        // vy = (-1-1-1-1)/4 = -1.
        // omega = (-1-1+1+1)/4 = 0.
        
        // My fix:
        // globalVx = 0 - (-1)*0 = 0.
        // globalVy = 0 + (-1)*1 = -1.
        // So it moves in -Y?
        
        System.out.println("Strafe Result: " + drive.getRobotState().x + ", " + drive.getRobotState().y);
    }

    private static void testRotatedMovement() {
        System.out.println("Running testRotatedMovement...");
        DriveSystem drive = createDriveSystem();
        
        // Set heading to 90 degrees (PI/2)
        drive.getRobotState().heading = Math.PI / 2;
        
        // Move Forward (all 1)
        setPowers(drive, 1, 1, 1, 1);
        drive.updateSim(1.0);
        
        // vx = 1 (Forward).
        // vy = 0.
        // heading = PI/2.
        
        // My fix:
        // globalVx = 1 * cos(PI/2) - 0 = 0.
        // globalVy = 1 * sin(PI/2) + 0 = 1.
        
        // So it moves in +Y.
        // If Forward was +X, now it moves in +Y.
        // If Forward was +Y, now it moves in -X?
        // Wait.
        // If robot is facing +Y (90 deg).
        // Moving "Forward" (Robot Y) should be Global Y?
        // No, if robot is at 0 deg (facing +X), Forward is +X.
        // If robot is at 90 deg (facing +Y), Forward is +Y.
        
        // In my testForwardMovement (Heading 0):
        // vx=1 -> x+=1.
        // So Forward is +X.
        
        // In testRotatedMovement (Heading 90):
        // vx=1 -> globalVy=1.
        // So Forward is +Y.
        
        // This seems correct for a robot where Forward is +X.
        // But usually in FTC, Forward is +Y.
        // If Forward is +Y, then at Heading 0, it should move +Y.
        // But my code moved +X.
        
        // So my code assumes Forward is +X.
        // But DriveSystem.driveMecanum uses Y for forward.
        // "double fl = y + x + turn;"
        // usually y is forward power.
        
        // So there is a coordinate system confusion.
        // I will run this test to see what happens.
        
        System.out.println("Rotated Result: " + drive.getRobotState().x + ", " + drive.getRobotState().y);
    }

    private static DriveSystem createDriveSystem() {
        return new DriveSystem(new FakeMotor(), new FakeMotor(), new FakeMotor(), new FakeMotor(), 100, 4, 0, 0, 0);
    }

    private static void setPowers(DriveSystem drive, double fl, double fr, double bl, double br) {
        // We can't set motors directly easily, so let's just use drive.driveMecanum?
        // No, updateSim pulls from motors.
        // So we need access to motors.
        // DriveSystem doesn't expose them.
        // But we can use reflection or just subclass?
        // Or just use drive.driveMecanum to set them.
        // But driveMecanum mixes them.
        
        // Let's use drive.setPower(0) then manually set them?
        // No, DriveSystem has private motors? No, package-private.
        // SimTest is in same package? No, com.duck123acb.sim vs com.duck123acb.robotcore.systems.
        // So I can't access them.
        
        // I'll use drive.driveMecanum to set powers.
        // To set specific powers:
        // fl = y+x+t
        // fr = y-x-t
        // bl = y-x+t
        // br = y+x-t
        
        // If I want fl=1, fr=1, bl=1, br=1:
        // y=1, x=0, t=0.
        drive.driveMecanum(0, 1, 0); // x, y, turn.
        // But wait, if I want fl=1, fr=-1...
        // y=0, x=1, t=0 -> fl=1, fr=-1, bl=-1, br=1.
        
        if (fl == 1 && fr == 1) drive.driveMecanum(0, 1, 0);
        else if (fl == 1 && fr == -1) drive.driveMecanum(1, 0, 0);
    }
}
