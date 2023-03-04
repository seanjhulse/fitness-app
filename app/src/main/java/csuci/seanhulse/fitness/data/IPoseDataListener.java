package csuci.seanhulse.fitness.data;

import com.google.mlkit.vision.pose.Pose;

/**
 * Classes who want to have access to the {@link csuci.seanhulse.fitness.camera.Analyzer}'s Pose
 * Data should implement this interface.
 *
 * @since 1.0.0
 */
public interface IPoseDataListener {
    void poseAdded(Pose pose);

    void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped);
}
