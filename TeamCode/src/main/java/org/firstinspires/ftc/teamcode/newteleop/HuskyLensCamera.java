package org.firstinspires.ftc.teamcode.newteleop;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Handles requests to and returns from the Husky Lens.
 * As of current, is lacking in utilization apart from
 * in RobotFunctions. Will eventually be big component
 * of Autonomous.
 */

public class HuskyLensCamera {
    private HuskyLens   huskyLens;
    public enum         HLMode {
        TAG_RECOGNITION,
        FACE_RECOGNITION,
        OBJECT_TRACKING,
        COLOR_RECOGNITION,
        OBJECT_RECOGNITION
    }
    private HLMode   currentMode = HLMode.TAG_RECOGNITION;
    private double  tagWidthPx;
    private double  tagHeightPx;


    public void init(HardwareMap hwMap) {

        huskyLens = hwMap.get(HuskyLens.class, "huskyLens");
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
    }

    private void setHuskyMode(HLMode currentMode) {

        switch (currentMode) {
            case OBJECT_TRACKING:
                huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_TRACKING);
                break;
            case FACE_RECOGNITION:
                huskyLens.selectAlgorithm(HuskyLens.Algorithm.FACE_RECOGNITION);
                break;
            case COLOR_RECOGNITION:
                huskyLens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);
                break;
            case OBJECT_RECOGNITION:
                huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION);
                break;
            case TAG_RECOGNITION:
                huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
                break;
            default:
                System.out.println("whatever");
        }
    }

    public void setModeUsingIndex(int index){

        if (0 > index || HLMode.values().length < index) { return; }

        currentMode = HLMode.values()[index];
        setHuskyMode(currentMode);

    }


    public int readAndDecodeAprilTag(){

        HuskyLens.Block[]   blocks = huskyLens.blocks();
        int                 tagID = 0;

        if (null != blocks && 0 < blocks.length) tagID = blocks[0].id;
        return tagID;
    }



    private void setAprilTagValues(){

        HuskyLens.Block[] blocks = huskyLens.blocks();

        if (null != blocks && 0 < blocks.length){

            tagWidthPx = blocks[0].width;
            tagHeightPx = blocks[0].height;
        } else {

            tagWidthPx = -1;
            tagHeightPx = -1;
        }
    }

    public double getTagWidth(){ return tagWidthPx;}
    public double getTagHeight(){ return tagHeightPx;}
    public int getTagID(){ return readAndDecodeAprilTag();}

    public double getAprilTagOffset(){

        HuskyLens.Block[]   blocks = huskyLens.blocks();
        double              xOffset;
        final double        noTagDetected = -1.0;

        if (null != blocks && 0 < blocks.length){
            xOffset = blocks[0].x;
            return  xOffset;
        }
        return noTagDetected;
    }

    // Is the Husky Lens set to tag recognition?
    public final boolean isTagRecognition() {return HLMode.TAG_RECOGNITION == currentMode;}
}
