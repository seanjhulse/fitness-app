package csuci.seanhulse.fitness.data;

import com.google.mlkit.vision.pose.Pose;

public interface IPoseDataListener {
    void poseAdded(Pose pose);

    void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped);
}
