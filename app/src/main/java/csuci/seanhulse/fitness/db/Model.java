package csuci.seanhulse.fitness.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity
public class Model {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "exercise_id")
    public UUID exerciseId;

    @ColumnInfo(name = "datetime")
    private String datetime;

    // Originating Task ID
    @ColumnInfo(name = "task_id")
    private String taskId;

    // Name of the ML Model
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "status")
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UUID getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(UUID exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
