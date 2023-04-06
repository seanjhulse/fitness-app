package csuci.seanhulse.fitness.data;

import com.google.mlkit.vision.pose.Pose;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton class for storing and accessing Pose Landmark data. {@link IPoseDataListener}s will receive any changes to
 * the Pose Data.
 *
 * @since 1.0.0
 */
public class PoseDataManager {
    private final Collection<IPoseDataListener> listeners = new CopyOnWriteArrayList<>();
    private final Deque<Pose> poses = new ConcurrentLinkedDeque<>();

    public void addPose(Pose pose) {
        if (pose != null) {
            poses.addLast(pose);

            // Inform each listener that the Pose has been added
            listeners.forEach(listener -> listener.poseAdded(pose));
        }
    }

    public Pose getPose() {
        return poses.getLast();
    }

    public void clear() {
        poses.clear();
    }

    public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped) {
        listeners.forEach(listener -> listener.setImageSourceInfo(imageWidth, imageHeight, isFlipped));
    }

    public void addPoseDataListener(IPoseDataListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removePoseDataListener(IPoseDataListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void clearPoseDataListeners() {
        listeners.clear();
    }
}
