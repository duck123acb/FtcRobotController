package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens.Block;

public class Ball {
    public final Artifact artifact;
    public final Block block;

    public Ball(Artifact artifact, Block block) {
        this.artifact = artifact;
        this.block = block;
    }
}