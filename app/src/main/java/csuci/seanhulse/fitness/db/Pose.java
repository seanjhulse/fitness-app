package csuci.seanhulse.fitness.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.UUID;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Pose.class,
                        parentColumns = "id",
                        childColumns = "exercise_id"
                )
        }
)
public class Pose {

    @PrimaryKey
    @NonNull
    private UUID id;

    @ColumnInfo(name = "exercise_id")
    public UUID exerciseId;

    @ColumnInfo(name = "datetime")
    private String datetime;

    @ColumnInfo(name = "state")
    private String state;

    @ColumnInfo(name = "landmarks")
    private List<Landmark> landmarks;

    @Ignore
    public Pose() {

    }

    public Pose(UUID id, List<Landmark> landmarks, String datetime, String state, UUID exerciseId) {
        this.id = id;
        this.landmarks = landmarks;
        this.datetime = datetime;
        this.state = state;
        this.exerciseId = exerciseId;
    }

    public List<Landmark> getLandmarks() {
        return landmarks;
    }

    public String getDatetime() {
        return datetime;
    }

    public UUID getExerciseId() {
        return exerciseId;
    }

    public String getState() {
        return state;
    }

    public UUID getId() {
        return id;
    }
}
