package csuci.seanhulse.fitness.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Represents an instance of a Pose.
 */
@Entity
public class Landmark {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private float x;

    private float y;

    private float z;

    private int landmarkType;

    private float confidence;

    @Ignore
    public Landmark() {

    }

    public Landmark(float x, float y, float z, int landmarkType, float confidence) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.landmarkType = landmarkType;
        this.confidence = confidence;
    }


    public int getLandmarkType() {
        return landmarkType;
    }

    public float getZ() {
        return z;
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    public int getId() {
        return id;
    }

    public float getConfidence() {
        return confidence;
    }
}
