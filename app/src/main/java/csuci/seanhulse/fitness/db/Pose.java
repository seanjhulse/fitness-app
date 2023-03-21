package csuci.seanhulse.fitness.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.UUID;

@Entity
public class Pose {

    @PrimaryKey
    @NonNull
    private UUID id;

    @ColumnInfo(name = "datetime")
    private String datetime;

    @ColumnInfo(name = "state")
    private String state;

    @ColumnInfo(name = "landmarks")
    private List<Landmark> landmarks;

    @ColumnInfo(name = "exerciseName")
    private String exerciseName;

    @Ignore
    public Pose() {

    }

    public Pose(List<Landmark> landmarks, String datetime, String state, String exerciseName) {
        this.landmarks = landmarks;
        this.datetime = datetime;
        this.state = state;
        this.exerciseName = exerciseName;
    }

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public List<Landmark> getLandmarks() {
        return landmarks;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setLandmarks(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
