package csuci.seanhulse.fitness.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents an instance of a Pose.
 */
@Entity
public class Pose {
    @PrimaryKey
    private int id;

    @ColumnInfo(name = "landmark_type")
    private int landmarkType;

    // Pose Landmark (x, y, z) position
    private int x;
    private int y;
    private int z;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLandmarkType() {
        return landmarkType;
    }

    public void setLandmarkType(int landmarkType) {
        this.landmarkType = landmarkType;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
