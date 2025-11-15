package org.firstinspires.ftc.teamcode.simulation;

import com.qualcomm.hardware.dfrobot.HuskyLens;

import java.util.concurrent.locks.Lock;

/**
 * A mock implementation of the HuskyLens class for simulation.
 * This allows testing autonomous logic without a physical HuskyLens sensor.
 */
public class MockHuskyLens extends HuskyLens {

    // We can't extend HuskyLens directly, but we can create a class that has the same methods.
    // For the purpose of this simulation, we will just create a wrapper that holds the data.
    // Since the original code uses the real HuskyLens class, we will have to create a new class
    // that has the same method signatures.

    private Block[] simulatedBlocks = new Block[0];
    private Algorithm currentAlgorithm = Algorithm.TAG_RECOGNITION;

    public MockHuskyLens() {
        // This constructor is intentionally left empty. We are not initializing a real device.
        super(null);
    }

    @Override
    public Block[] blocks() {
        // Return the blocks that have been set for the current algorithm.
        return simulatedBlocks;
    }

    @Override
    public void selectAlgorithm(Algorithm algorithm) {
        // Simulate selecting an algorithm.
        this.currentAlgorithm = algorithm;
        // In a real scenario, you might clear the blocks or change behavior here.
    }

    @Override
    public boolean knock() {
        // Always return true to simulate a successful connection.
        return true;
    }

    /**
     * A method to prime the mock HuskyLens with a set of blocks to return.
     * @param blocks The blocks to be returned by the blocks() method.
     */
    public void setBlocks(Block[] blocks) {
        this.simulatedBlocks = blocks;
    }

    /**
     * Helper to easily create a single block for testing.
     * @param id The ID of the block.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param width The width of the block.
     * @param height The height of the block.
     * @return A new Block object.
     */
    public static Block createBlock(int id, int x, int y, int width, int height) {
        Block block = new Block();
        block.id = id;
        block.x = x;
        block.y = y;
        block.width = width;
        block.height = height;
        return block;
    }
}

